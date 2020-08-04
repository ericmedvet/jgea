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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.ToIntFunction;

/**
 * @author eric
 * @created 2020/08/04
 * @project jgea
 */
public class RampedHalfAndHalf<N> implements Factory<Tree<N>> {
  private final int minHeight;
  private final int maxHeight;
  private final FullTreeFactory<N> fullTreeFactory;
  private final GrowTreeFactory<N> growTreeFactory;

  public RampedHalfAndHalf(int minHeight, int maxHeight, ToIntFunction<N> arityFunction, IndependentFactory<N> nonTerminalFactory, IndependentFactory<N> terminalFactory) {
    this.minHeight = minHeight;
    this.maxHeight = maxHeight;
    fullTreeFactory = new FullTreeFactory<>(maxHeight, arityFunction, nonTerminalFactory, terminalFactory);
    growTreeFactory = new GrowTreeFactory<>(maxHeight, arityFunction, nonTerminalFactory, terminalFactory);
  }

  @Override
  public List<Tree<N>> build(int n, Random random) {
    List<Tree<N>> trees = new ArrayList<>();
    //full
    int height = minHeight;
    while (trees.size() < n / 2) {
      Tree<N> tree = fullTreeFactory.build(random, height);
      if (tree != null) {
        trees.add(tree);
      }
      height = height + 1;
      if (height > maxHeight) {
        height = minHeight;
      }
    }
    //grow
    while (trees.size() < n) {
      Tree<N> tree = growTreeFactory.build(random, height);
      if (tree != null) {
        trees.add(tree);
      }
      height = height + 1;
      if (height > maxHeight) {
        height = minHeight;
      }
    }
    return trees;
  }
}
