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

package it.units.malelab.jgea.core.operator;


import it.units.malelab.jgea.core.util.Pair;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.random.RandomGenerator;

/**
 * @author eric
 */
@FunctionalInterface
public interface Crossover<G> extends GeneticOperator<G> {

  G recombine(G g1, G g2, RandomGenerator random);

  static <G1, G2> Crossover<Pair<G1, G2>> pair(Crossover<G1> crossover1, Crossover<G2> crossover2) {
    return (p1, p2, random) -> Pair.of(
        crossover1.recombine(p1.first(), p2.first(), random),
        crossover2.recombine(p1.second(), p2.second(), random)
    );
  }

  static <K> Crossover<K> randomCopy() {
    return (g1, g2, random) -> random.nextBoolean() ? g1 : g2;
  }

  @Override
  default List<? extends G> apply(List<? extends G> gs, RandomGenerator random) {
    return Collections.singletonList(recombine(gs.get(0), gs.get(1), random));
  }

  @Override
  default int arity() {
    return 2;
  }

  default Crossover<G> withChecker(Predicate<G> checker) {
    Crossover<G> thisCrossover = this;
    return (parent1, parent2, random) -> {
      G child = thisCrossover.recombine(parent1, parent2, random);
      return checker.test(child) ? child : (random.nextBoolean() ? parent1 : parent2);
    };
  }

}
