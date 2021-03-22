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
import it.units.malelab.jgea.core.operator.Mutation;

import java.util.List;
import java.util.Random;

/**
 * @author eric
 */
public class ProbabilisticMutation<E, L extends List<E>> implements Mutation<L> {
  private final double p;
  private final IndependentFactory<L> factory;
  private final Mutation<E> mutation;

  public ProbabilisticMutation(double p, IndependentFactory<L> factory, Mutation<E> mutation) {
    this.p = p;
    this.factory = factory;
    this.mutation = mutation;
  }

  @Override
  public L mutate(L parent, Random random) {
    L child = factory.build(random);
    for (int i = 0; i < parent.size(); i++) {
      E e = (random.nextDouble() < p) ? mutation.mutate(parent.get(i), random) : parent.get(i);
      if (child.size() > i) {
        child.set(i, e);
      } else {
        child.add(e);
      }
    }
    return child;
  }
}
