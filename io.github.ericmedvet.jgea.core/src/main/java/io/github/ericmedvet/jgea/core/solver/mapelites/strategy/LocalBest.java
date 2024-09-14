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
package io.github.ericmedvet.jgea.core.solver.mapelites.strategy;

import io.github.ericmedvet.jgea.core.order.PartialComparator;
import io.github.ericmedvet.jgea.core.order.PartiallyOrderedCollection;
import io.github.ericmedvet.jnb.datastructure.Pair;
import java.util.*;
import java.util.stream.Collectors;

public class LocalBest implements CoMEStrategy {

  private final Map<List<Double>, Pair<List<Double>, Object>> bests;

  public LocalBest() {
    bests = new HashMap<>();
  }

  protected record PartialObservation<Q>(List<Double> otherCoords, Q q) {}

  @Override
  public List<Double> getOtherCoords(List<Double> theseCoords) {
    return bests.getOrDefault(theseCoords, new Pair<>(theseCoords, null)).first();
  }

  @Override
  public <Q> void update(Collection<Observation<Q>> newObservations, PartialComparator<Q> qComparator) {
    newObservations.stream()
        .collect(Collectors.groupingBy(Observation::theseCoords))
        .forEach((tc, os) -> {
          PartialObservation<Q> newOtherCoords = computeNewOtherCoords(
              tc,
              os.stream()
                  .map(o -> new PartialObservation<>(o.otherCoords(), o.q()))
                  .toList(),
              qComparator);
          //noinspection unchecked
          bests.merge(
              tc,
              new Pair<>(newOtherCoords.otherCoords(), newOtherCoords.q()),
              (currentPair, newPair) ->
                  (qComparator.compare((Q) newPair.second(), (Q) currentPair.second())
                          == PartialComparator.PartialComparatorOutcome.BEFORE)
                      ? newPair
                      : currentPair);
        });
  }

  protected <Q> PartialObservation<Q> computeNewOtherCoords(
      List<Double> theseCoords,
      Collection<PartialObservation<Q>> observations,
      PartialComparator<Q> qComparator) {
    Collection<PartialObservation<Q>> firsts = PartiallyOrderedCollection.from(
            observations, (PartialComparator<? super PartialObservation<Q>>)
                (po1, po2) -> qComparator.compare(po1.q(), po2.q()))
        .firsts();
    return firsts.stream().findFirst().orElseThrow();
  }
}
