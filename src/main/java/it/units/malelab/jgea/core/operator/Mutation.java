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

import it.units.malelab.jgea.core.util.Misc;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Predicate;

/**
 * @author eric
 */
@FunctionalInterface
public interface Mutation<G> extends GeneticOperator<G> {

  @Override
  default int arity() {
    return 1;
  }

  @Override
  default List<? extends G> apply(List<? extends G> gs, Random random) {
    return Collections.singletonList(mutate(gs.get(0), random));
  }

  G mutate(G g, Random random);

  static <K> Mutation<K> copy() {
    return (k, random) -> k;
  }

  static <K> Mutation<K> oneOf(Map<Mutation<K>, Double> operators) {
    return (k, random) -> Misc.pickRandomly(operators, random).mutate(k, random);
  }

  default Mutation<G> withChecker(Predicate<? super G> checker) {
    Mutation<G> thisMutation = this;
    return (parent, random) -> {
      G child = thisMutation.mutate(parent, random);
      return checker.test(child) ? child : parent;
    };
  }

}
