/*-
 * ========================LICENSE_START=================================
 * jgea-experimenter
 * %%
 * Copyright (C) 2018 - 2026 Eric Medvet
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */
/*
 * Copyright 2026 eric
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.ericmedvet.jgea.experimenter.builders;

import io.github.ericmedvet.jgea.core.solver.mapelites.archive.GridArchive;
import io.github.ericmedvet.jgea.core.solver.mapelites.archive.GridArchive.Axis;
import io.github.ericmedvet.jgea.core.solver.mapelites.archive.NumericalKeyArchive;
import io.github.ericmedvet.jgea.core.solver.mapelites.archive.NumericalKeyArchive.Provider;
import io.github.ericmedvet.jgea.core.solver.mapelites.archive.TwoDGridArchive;
import io.github.ericmedvet.jnb.core.Cacheable;
import io.github.ericmedvet.jnb.core.Discoverable;
import io.github.ericmedvet.jnb.core.Param;
import io.github.ericmedvet.jnb.datastructure.DoubleRange;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.IntStream;

@Discoverable(prefixTemplate = "ea.solver|s.mapelites|me.archive|a")
public class ArchiveProviders {

  private ArchiveProviders() {
  }

  @Cacheable
  public static NumericalKeyArchive.Provider grid(
      @Param("ranges") List<DoubleRange> ranges,
      @Param("nsOfBins") List<Integer> nsOfBins
  ) {
    if (ranges.size() != nsOfBins.size()) {
      throw new IllegalArgumentException(
          "Different size of ranges and nsOfBins: %d vs. %d".formatted(ranges.size(), nsOfBins.size())
      );
    }
    return new Provider() {
      @Override
      public <V, C> NumericalKeyArchive<V, C> provide(
          int arity,
          Function<V, C> contentInitializer,
          BiFunction<C, V, C> contentUpdater
      ) {
        return new GridArchive<>(
            IntStream.range(0, ranges.size()).mapToObj(i -> new Axis(ranges.get(i), nsOfBins.get(i))).toList(),
            contentInitializer,
            contentUpdater
        );
      }
    };
  }

  @Cacheable
  public static NumericalKeyArchive.Provider grid2d(
      @Param("ranges1") DoubleRange range1,
      @Param("ranges2") DoubleRange range2,
      @Param("nOfBins1") int nOfBins1,
      @Param("nOfBins2") int nOfBins2
  ) {
    return new Provider() {
      @Override
      public <V, C> NumericalKeyArchive<V, C> provide(
          int arity,
          Function<V, C> contentInitializer,
          BiFunction<C, V, C> contentUpdater
      ) {
        return new TwoDGridArchive<>(
            new Axis(range1, nOfBins1),
            new Axis(range2, nOfBins2),
            contentInitializer,
            contentUpdater
        );
      }
    };
  }

}