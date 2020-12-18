/*
 * Copyright 2020 Eric Medvet <eric.medvet@gmail.com> (as eric)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it.units.malelab.jgea.core.evolver.speciation;

import it.units.malelab.jgea.core.Individual;
import it.units.malelab.jgea.core.order.PartiallyOrderedCollection;
import it.units.malelab.jgea.distance.Distance;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author federico
 */
public class LazySpeciator<G, S, F> implements Speciator<Individual<G, S, F>> {
  private final Distance<Individual<G, S, F>> distance;
  private final double distanceThreshold;
  private final Function<Collection<Individual<G, S, F>>, Individual<G, S, F>> representerSelector;

  public LazySpeciator(Distance<Individual<G, S, F>> distance, double distanceThreshold,
                       Function<Collection<Individual<G, S, F>>, Individual<G, S, F>> representerSelector) {
    this.distance = distance;
    this.distanceThreshold = distanceThreshold;
    this.representerSelector = representerSelector;
  }

  @Override
  public Collection<Species<Individual<G, S, F>>> speciate(PartiallyOrderedCollection<Individual<G, S, F>> population) {
    List<Species<Individual<G, S, F>>> allSpecies = new ArrayList<>();
    for (Individual<G, S, F> individual : population.all()) {
      List<Double> distances = allSpecies.stream()
          .map(s -> distance.apply(individual, s.getRepresentative()))
          .collect(Collectors.toList());
      if (distances.isEmpty()) {
        allSpecies.add(new Species<>(List.of(individual), representerSelector));
      } else {
        int closestIndex = 0;
        for (int i = 1; i < distances.size(); i++) {
          if (distances.get(i) < distances.get(closestIndex)) {
            closestIndex = i;
          }
        }
        if (distances.get(closestIndex) < distanceThreshold) {
          allSpecies.get(closestIndex).addElement(individual);
        } else {
          allSpecies.add(new Species<>(List.of(individual), representerSelector));
        }
      }
    }
    return allSpecies;
  }

}
