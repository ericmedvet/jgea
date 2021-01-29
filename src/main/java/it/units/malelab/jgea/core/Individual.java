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

package it.units.malelab.jgea.core;


import java.io.Serializable;
import java.util.Objects;

/**
 * @author eric
 */
public class Individual<G, S, F> implements Serializable {

  private final G genotype;
  private final S solution;
  private final F fitness;
  private final int birthIteration;
  private final int genotypeBirthIteration;

  public Individual(G genotype, S solution, F fitness, int birthIteration, int genotypeBirthIteration) {
    this.genotype = genotype;
    this.solution = solution;
    this.fitness = fitness;
    this.birthIteration = birthIteration;
    this.genotypeBirthIteration = genotypeBirthIteration;
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

  public int getGenotypeBirthIteration() {
    return genotypeBirthIteration;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Individual<?, ?, ?> that = (Individual<?, ?, ?>) o;
    return birthIteration == that.birthIteration && genotypeBirthIteration == that.genotypeBirthIteration && genotype.equals(that.genotype) && solution.equals(that.solution) && fitness.equals(that.fitness);
  }

  @Override
  public int hashCode() {
    return Objects.hash(genotype, solution, fitness, birthIteration, genotypeBirthIteration);
  }

  @Override
  public String toString() {
    return "Individual{" +
        "genotype=" + genotype +
        ", solution=" + solution +
        ", fitness=" + fitness +
        ", birthIteration=" + birthIteration +
        ", genotypeBirthIteration=" + genotypeBirthIteration +
        '}';
  }
}
