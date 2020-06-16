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

package it.units.malelab.jgea.core.listener;

import it.units.malelab.jgea.core.Individual;
import it.units.malelab.jgea.core.PartiallyOrderedCollection;

import java.io.Serializable;

/**
 * @author eric
 * @created 2020/06/16
 * @project jgea
 */
public class Event<G, S, F> implements Serializable {

  private final int iteration;
  private final int births;
  private final int fitnessEvaluations;
  private final long elapsedMillis;
  private final PartiallyOrderedCollection<Individual<G, S, F>> orderedPopulation;

  public Event(int iteration, int births, int fitnessEvaluations, long elapsedMillis, PartiallyOrderedCollection<Individual<G, S, F>> orderedPopulation) {
    this.iteration = iteration;
    this.births = births;
    this.fitnessEvaluations = fitnessEvaluations;
    this.elapsedMillis = elapsedMillis;
    this.orderedPopulation = orderedPopulation;
  }

  public int getIteration() {
    return iteration;
  }

  public int getBirths() {
    return births;
  }

  public int getFitnessEvaluations() {
    return fitnessEvaluations;
  }

  public long getElapsedMillis() {
    return elapsedMillis;
  }

  public PartiallyOrderedCollection<Individual<G, S, F>> getOrderedPopulation() {
    return orderedPopulation;
  }
}
