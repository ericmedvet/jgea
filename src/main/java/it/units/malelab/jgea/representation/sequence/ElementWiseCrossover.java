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

package it.units.malelab.jgea.representation.sequence;

import it.units.malelab.jgea.core.IndependentFactory;
import it.units.malelab.jgea.core.operator.Crossover;

import java.util.List;
import java.util.Random;

/**
 * @author eric
 */
public class ElementWiseCrossover<E, L extends List<E>> implements Crossover<L> {
  private final IndependentFactory<L> factory;
  private final Crossover<E> crossover;

  public ElementWiseCrossover(IndependentFactory<L> factory, Crossover<E> crossover) {
    this.factory = factory;
    this.crossover = crossover;
  }

  @Override
  public L recombine(L parent1, L parent2, Random random) {
    L child = factory.build(random);
    for (int i = 0; i < Math.min(parent1.size(), parent2.size()); i++) {
      E e = crossover.recombine(parent1.get(i), parent2.get(i), random);
      if (child.size() > i) {
        child.set(i, e);
      } else {
        child.add(e);
      }
    }
    return child;
  }

}
