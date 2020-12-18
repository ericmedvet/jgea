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

package it.units.malelab.jgea.core.listener.collector;

import it.units.malelab.jgea.core.listener.Event;
import it.units.malelab.jgea.core.util.TextPlotter;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author eric
 */
public class PopulationHistograms implements DataCollector<Object, Object, Object> {

  private final int bins;

  public PopulationHistograms(int bins) {
    this.bins = bins;
  }

  public PopulationHistograms() {
    this(8);
  }

  @Override
  public List<Item> collect(Event<?, ?, ?> event) {
    List<Integer> genoSizes = event.getOrderedPopulation().all().stream()
        .map(i -> IndividualBasicInfo.size(i.getGenotype()))
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
    List<Integer> solutionSizes = event.getOrderedPopulation().all().stream()
        .map(i -> IndividualBasicInfo.size(i.getSolution()))
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
    List<Integer> ages = event.getOrderedPopulation().all().stream()
        .map(i -> event.getState().getIterations() - i.getBirthIteration())
        .collect(Collectors.toList());
    return List.of(
        new Item("population.genotype.size.barplot", TextPlotter.histogram(genoSizes, bins), "%" + bins + "s"),
        new Item("population.solution.size.barplot", TextPlotter.histogram(solutionSizes, bins), "%" + bins + "s"),
        new Item("population.age.barplot", TextPlotter.histogram(ages, bins), "%" + bins + "s")
    );
  }

}
