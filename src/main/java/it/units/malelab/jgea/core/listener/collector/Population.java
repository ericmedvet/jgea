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

import it.units.malelab.jgea.core.Individual;
import it.units.malelab.jgea.core.listener.Event;

import java.util.List;

/**
 * @author eric
 */
public class Population implements DataCollector<Object, Object, Object> {

  @Override
  public List<Item> collect(Event<?, ?, ?> event) {
    double genoCount = 0;
    double solutionCount = 0;
    double genoSizeSum = 0;
    double solutionSizeSum = 0;
    double ageSum = 0;
    double count = 0;
    for (Individual<?, ?, ?> individual : event.getOrderedPopulation().all()) {
      Integer genoSize = IndividualBasicInfo.size(individual.getGenotype());
      if (genoSize != null) {
        genoSizeSum = genoSizeSum + genoSize;
        genoCount = genoCount + 1;
      }
      Integer solutionSize = IndividualBasicInfo.size(individual.getSolution());
      if (solutionSize != null) {
        solutionSizeSum = solutionSizeSum + solutionSize;
        solutionCount = solutionCount + 1;
      }
      ageSum = ageSum + event.getState().getIterations() - individual.getBirthIteration();
      count = count + 1;
    }
    return List.of(
        new Item("population.genotype.size.average", (int) Math.round(genoSizeSum / genoCount), "%5d"),
        new Item("population.solution.size.average", (int) Math.round(solutionSizeSum / solutionCount), "%5d"),
        new Item("population.age.average", (int) Math.round(ageSum / count), "%2d"),
        new Item("population.size", (int) count, "%4d"),
        new Item("population.first.size", event.getOrderedPopulation().firsts().size(), "%4d"),
        new Item("population.lasts.size", event.getOrderedPopulation().lasts().size(), "%4d")
    );
  }

}
