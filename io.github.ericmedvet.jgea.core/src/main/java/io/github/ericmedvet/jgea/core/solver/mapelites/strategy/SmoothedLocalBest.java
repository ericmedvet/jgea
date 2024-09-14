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
import java.util.Collection;
import java.util.List;
import java.util.stream.IntStream;

public class SmoothedLocalBest extends LocalBest {
  private final double step;

  public SmoothedLocalBest(double step) {
    this.step = step;
  }

  @Override
  protected <Q> PartialObservation<Q> computeNewOtherCoords(
      List<Double> theseCoords,
      Collection<PartialObservation<Q>> observations,
      PartialComparator<Q> qComparator) {
    Collection<PartialObservation<Q>> firsts = PartiallyOrderedCollection.from(
            observations, (PartialComparator<? super PartialObservation<Q>>)
                (po1, po2) -> qComparator.compare(po1.q(), po2.q()))
        .firsts();
    PartialObservation<Q> bestPO = firsts.stream().findFirst().orElseThrow();
    List<Double> currentOtherCoords = getOtherCoords(theseCoords);
    List<Double> newOtherCoords = IntStream.range(0, currentOtherCoords.size())
        .mapToObj(i ->
            step(currentOtherCoords.get(i), bestPO.otherCoords().get(i)))
        .toList();
    return new PartialObservation<>(newOtherCoords, bestPO.q());
  }

  private double step(double current, double target) {
    double d = target - current;
    if (Math.abs(d) <= step) {
      return target;
    }
    if (target < current) {
      return current - step;
    }
    return current + step;
  }
}
