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

package it.units.malelab.jgea.representation.tree;

import it.units.malelab.jgea.core.Factory;
import it.units.malelab.jgea.core.IndependentFactory;

import java.util.Random;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

/**
 * @author eric
 * @created 2020/08/04
 * @project jgea
 */
public class GrowTreeFactory<N> extends FullTreeFactory<N> {
  public GrowTreeFactory(int height, ToIntFunction<N> arityFunction, IndependentFactory<N> nonTerminalFactory, IndependentFactory<N> terminalFactory) {
    super(height, arityFunction, nonTerminalFactory, terminalFactory);
  }

  @Override
  public Tree<N> build(Random random, int h) {
    if (h == 1) {
      return Tree.of(terminalFactory.build(random));
    }
    Tree<N> t = Tree.of(nonTerminalFactory.build(random));
    int nChildren = arityFunction.applyAsInt(t.content());
    int[] heights = new int[nChildren];
    int maxHeight = 0;
    for (int i = 0; i < nChildren; i++) {
      heights[i] = random.nextInt(h - 1) + 1;
      maxHeight = Math.max(maxHeight, heights[i]);
    }
    if (maxHeight < h - 1) {
      heights[random.nextInt(nChildren)] = h - 1;
    }
    for (int i = 0; i < nChildren; i++) {
      t.addChild(build(random, heights[i]));
    }
    return t;
  }

}
