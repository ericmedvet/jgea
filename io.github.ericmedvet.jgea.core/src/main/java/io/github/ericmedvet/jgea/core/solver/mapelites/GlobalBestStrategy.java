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
import io.github.ericmedvet.jgea.core.order.PartiallyOrderedCollection;
import java.util.*;

public class GlobalBestStrategy implements CoMEStrategy {

  private List<Double> bestCoords;
  private Object bestQ;

  public GlobalBestStrategy() {
    bestCoords = null;
    bestQ = null;
  }

  @Override
  public List<Double> getOtherCoords(List<Double> theseCoords) {
    if (bestCoords == null) {
      return Collections.nCopies(theseCoords.size(), 0.5d);
    }
    return bestCoords;
  }

  @Override
  public <Q> void update(Collection<Observation<Q>> newObservations, PartialComparator<Q> qComparator) {
    Optional<Observation<Q>> oBestObservation =
        PartiallyOrderedCollection.from(newObservations, qComparator.comparing(Observation::q))
            .firsts()
            .stream()
            .findAny();
    if (oBestObservation.isPresent()) {
      Observation<Q> bestObservation = oBestObservation.get();
      //noinspection unchecked
      if ((bestQ == null)
          || (qComparator.compare(bestObservation.q(), (Q) bestQ)
              == PartialComparator.PartialComparatorOutcome.BEFORE)) {
        bestCoords = bestObservation.otherCoords();
        bestQ = bestObservation.q();
      }
    }
  }
}
