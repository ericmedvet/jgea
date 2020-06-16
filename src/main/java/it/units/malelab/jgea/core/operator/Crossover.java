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


import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * @author eric
 */
@FunctionalInterface
public interface Crossover<G> extends GeneticOperator<G> {

  @Override
  default int arity() {
    return 2;
  }

  @Override
  default List<G> apply(List<G> gs, Random random) {
    return Collections.singletonList(recombine(gs.get(0), gs.get(1), random));
  }

  G recombine(G g1, G g2, Random random);

}
