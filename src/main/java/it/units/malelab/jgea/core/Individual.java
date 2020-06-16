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

package it.units.malelab.jgea.core;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author eric
 */
public class Individual<G, S, F> implements Serializable {

  private final G genotype;
  private final S solution;
  private final F fitness;
  private final int birthIteration;

  public Individual(G genotype, S solution, F fitness, int birthIteration) {
    this.genotype = genotype;
    this.solution = solution;
    this.fitness = fitness;
    this.birthIteration = birthIteration;
  }

  public G getGenotype() {
    return genotype;
  }

  public S getSolution() {
    return solution;
  }

  public F getFitness() {
    return fitness;
  }

  public int getBirthIteration() {
    return birthIteration;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Individual<?, ?, ?> that = (Individual<?, ?, ?>) o;
    return birthIteration == that.birthIteration &&
        genotype.equals(that.genotype) &&
        solution.equals(that.solution) &&
        fitness.equals(that.fitness);
  }

  @Override
  public int hashCode() {
    return Objects.hash(genotype, solution, fitness, birthIteration);
  }

  @Override
  public String toString() {
    return "Individual{" +
        "genotype=" + genotype +
        ", solution=" + solution +
        ", fitness=" + fitness +
        ", birthIteration=" + birthIteration +
        '}';
  }
}
