/*-
 * ========================LICENSE_START=================================
 * jgea-core
 * %%
 * Copyright (C) 2018 - 2023 Eric Medvet
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */

package io.github.ericmedvet.jgea.core.operator;

import io.github.ericmedvet.jgea.core.util.Misc;
import io.github.ericmedvet.jgea.core.util.Pair;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.random.RandomGenerator;

@FunctionalInterface
public interface Mutation<G> extends GeneticOperator<G> {

  G mutate(G g, RandomGenerator random);

  static <K> Mutation<K> copy() {
    return (k, random) -> k;
  }

  static <K> Mutation<K> oneOf(Map<Mutation<K>, Double> operators) {
    return (k, random) -> Misc.pickRandomly(operators, random).mutate(k, random);
  }

  static <G1, G2> Mutation<Pair<G1, G2>> pair(Mutation<G1> mutation1, Mutation<G2> mutation2) {
    return (p, random) -> Pair.of(mutation1.mutate(p.first(), random), mutation2.mutate(p.second(), random));
  }

  @Override
  default List<? extends G> apply(List<? extends G> gs, RandomGenerator random) {
    return Collections.singletonList(mutate(gs.get(0), random));
  }

  @Override
  default int arity() {
    return 1;
  }

  default Mutation<G> withChecker(Predicate<? super G> checker) {
    Mutation<G> thisMutation = this;
    return (parent, random) -> {
      G child = thisMutation.mutate(parent, random);
      return checker.test(child) ? child : parent;
    };
  }
}
