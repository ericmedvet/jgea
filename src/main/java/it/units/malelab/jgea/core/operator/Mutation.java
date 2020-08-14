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

package it.units.malelab.jgea.core.operator;

import it.units.malelab.jgea.core.util.Misc;
import it.units.malelab.jgea.core.util.Pair;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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
