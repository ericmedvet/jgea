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
import it.units.malelab.jgea.core.util.Misc;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author eric
 */
public class FitnessHistogram implements DataCollector<Object, Object, Number> {

  private final int bins;

  public FitnessHistogram(int bins) {
    this.bins = bins;
  }

  public FitnessHistogram() {
    this(8);
  }

  @Override
  public List<Item> collect(Event<?, ?, ? extends Number> event) {
    List<Number> fitnesses = event.getOrderedPopulation().all().stream()
        .map(Individual::getFitness)
        .collect(Collectors.toList());
    return List.of(
        new Item("population.fitness.histogram", Misc.histogram(fitnesses, bins), "%" + bins + "s")
    );
  }

}
