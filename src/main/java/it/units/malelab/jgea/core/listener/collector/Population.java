/*
 * Copyright (C) 2020 Eric Medvet <eric.medvet@gmail.com> (as eric)
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
