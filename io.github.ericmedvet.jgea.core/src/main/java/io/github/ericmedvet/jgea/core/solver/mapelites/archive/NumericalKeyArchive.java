/*-
 * ========================LICENSE_START=================================
 * jgea-core
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
package io.github.ericmedvet.jgea.core.solver.mapelites.archive;

import io.github.ericmedvet.jnb.datastructure.TriFunction;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import org.jcodec.common.DictionaryCompressor.Int;

public interface NumericalKeyArchive<V, C> extends Archive<List<Double>, V, C> {

  interface Provider<V, C> {

    NumericalKeyArchive<V, C> provide(int arity,
        Function<V, C> contentInitializer, BiFunction<C, V, C> contentUpdater);
  }

  int arity();

  @Override
  default NumericalKeyArchive<V, C> withAll(Collection<V> values,
      Function<V, List<Double>> keyExtractor, BiPredicate<V, C> predicate) {
    values.forEach(value -> putIf(keyExtractor.apply(value), value, predicate));
    return this;
  }
}