/*-
 * ========================LICENSE_START=================================
 * jgea-core
 * %%
 * Copyright (C) 2018 - 2024 Eric Medvet
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
package io.github.ericmedvet.jgea.core.solver.mapelites;

import io.github.ericmedvet.jgea.core.order.PartialComparator;
import io.github.ericmedvet.jnb.datastructure.Pair;

import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.IntStream;

public interface CoMEStrategy {
  enum Prepared implements Supplier<CoMEStrategy> {
    IDENTITY(() -> tc -> tc),
    CENTRAL(() -> tc -> IntStream.range(0, tc.length).mapToDouble(i -> 0.5).toArray()),
    BEST(null), // TODO fill
    M1(null); // TODO fill
    private final Supplier<CoMEStrategy> supplier;

    Prepared(Supplier<CoMEStrategy> supplier) {
      this.supplier = supplier;
    }

    @Override
    public CoMEStrategy get() {
      return supplier.get();
    }
  }

  double[] getOtherCoords(double[] theseCoords);

  default <Q> void update(Map<Pair<double[], double[]>, Q> newQs, PartialComparator<Q> qComparator) {}
}
