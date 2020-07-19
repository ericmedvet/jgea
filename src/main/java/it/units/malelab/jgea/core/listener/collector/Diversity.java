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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
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
