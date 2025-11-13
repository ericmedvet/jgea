/*-
 * ========================LICENSE_START=================================
 * jgea-experimenter
 * %%
 * Copyright (C) 2018 - 2025 Eric Medvet
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

package io.github.ericmedvet.jgea.experimenter.builders;

import io.github.ericmedvet.jgea.core.listener.AccumulatorFactory;
import io.github.ericmedvet.jgea.core.solver.POCPopulationState;
import io.github.ericmedvet.jgea.experimenter.Run;
import io.github.ericmedvet.jnb.core.*;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Discoverable(prefixTemplate = "ea.accumulator|acc|a")
public class Accumulators {

  private Accumulators() {
  }

  @Alias(name = "bests", value = "all(eFunction = ea.f.best())")
  @Alias(name = "first", value = "all(listFunction = f.nTh(n = 1))")
  @SuppressWarnings("unused")
  public static <E, F, O, R> AccumulatorFactory<E, O, R> all(
      @Param(value = "eFunction", dNPM = "f.identity()") Function<E, F> eFunction,
      @Param(value = "listFunction", dNPM = "f.identity()") Function<List<F>, O> listFunction
  ) {
    return AccumulatorFactory.<E, F, R>collector(eFunction).then(listFunction);
  }

  @Alias(name = "lastBest", value = "last(function = ea.f.best())")
  @SuppressWarnings("unused")
  public static <E, O, R> AccumulatorFactory<E, O, R> last(
      @Param(value = "function", dNPM = "f.identity()") Function<E, O> function
  ) {
    return AccumulatorFactory.last((e, r) -> function.apply(e));
  }

  @SuppressWarnings("unused")
  public static <G> AccumulatorFactory<POCPopulationState<?, G, ?, ?, ?>, NamedParamMap, Run<?, G, ?, ?>> lastPopulationMap(
      @Param(value = "serializerF", dNPM = "f.toBase64()") Function<Object, String> serializer
  ) {
    return AccumulatorFactory.last(
        (s, run) -> new MapNamedParamMap(
            "ea.runOutcome",
            Map.ofEntries(
                Map.entry("index", run.index()),
                Map.entry("run", run.map()),
                Map.entry(
                    "serializedGenotypes",
                    s.pocPopulation()
                        .all()
                        .stream()
                        .map(i -> serializer.apply(i.genotype()))
                        .toList()
                )
            )
        )
    );
  }
}
