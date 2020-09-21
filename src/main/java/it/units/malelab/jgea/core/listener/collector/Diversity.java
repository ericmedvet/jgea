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

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author eric
 */
public class Diversity implements DataCollector<Object, Object, Object> {

  @Override
  public List<Item> collect(Event<?, ?, ?> event) {
    Collection<? extends Individual<?, ?, ?>> all = new ArrayList<>(event.getOrderedPopulation().all());
    Set<?> genotypes = all.stream().map(Individual::getGenotype).collect(Collectors.toSet());
    Set<?> solutions = all.stream().map(Individual::getSolution).collect(Collectors.toSet());
    Set<?> fitnesses = all.stream().map(Individual::getFitness).collect(Collectors.toSet());
    double count = all.size();
    return Arrays.asList(
        new Item("diversity.genotype", (double) genotypes.size() / count, "%4.2f"),
        new Item("diversity.solution", (double) solutions.size() / count, "%4.2f"),
        new Item("diversity.fitness", (double) fitnesses.size() / count, "%4.2f")
    );
  }

}
