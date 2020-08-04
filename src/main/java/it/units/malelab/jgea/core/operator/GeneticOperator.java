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

import java.util.List;
import java.util.Random;

/**
 * @author eric
 */
public interface GeneticOperator<G> {

  int arity();

  List<? extends G> apply(List<? extends G> parents, Random random);

  default GeneticOperator<G> andThen(GeneticOperator<G> other) {
    final GeneticOperator<G> thisOperator = this;
    return new GeneticOperator<G>() {
      @Override
      public int arity() {
        return thisOperator.arity();
      }

      @Override
      public List<? extends G> apply(List<? extends G> parents, Random random) {
        List<? extends G> intermediate = thisOperator.apply(parents, random);
        if (intermediate.size() < other.arity()) {
          throw new IllegalArgumentException(String.format("Cannot apply composed operator: 2nd operator expected %d parents and found %d", other.arity(), intermediate.size()));
        }
        return other.apply(intermediate, random);
      }
    };
  }

}
