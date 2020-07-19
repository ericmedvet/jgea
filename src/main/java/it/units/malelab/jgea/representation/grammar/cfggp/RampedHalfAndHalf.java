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

package it.units.malelab.jgea.representation.grammar.cfggp;

import it.units.malelab.jgea.core.Factory;
import it.units.malelab.jgea.representation.tree.Node;
import it.units.malelab.jgea.representation.grammar.Grammar;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author eric
 */
public class RampedHalfAndHalf<T> implements Factory<Node<T>> {

  private final int minDepth;
  private final int maxDepth;
  private final FullTreeFactory<T> fullTreeFactory;
  private final GrowTreeFactory<T> growTreeFactory;

  public RampedHalfAndHalf(int minDepth, int maxDepth, Grammar<T> grammar) {
    this.minDepth = minDepth;
    this.maxDepth = maxDepth;
    fullTreeFactory = new FullTreeFactory<>(maxDepth, grammar);
    growTreeFactory = new GrowTreeFactory<>(maxDepth, grammar);
  }

  @Override
  public List<Node<T>> build(int n, Random random) {
    List<Node<T>> trees = new ArrayList<>();
    //full
    int depth = minDepth;
    while (trees.size() < n / 2) {
      Node<T> tree = fullTreeFactory.build(random, depth);
      if (tree != null) {
        trees.add(tree);
      }
      depth = depth + 1;
      if (depth > maxDepth) {
        depth = minDepth;
      }
    }
    //grow
    while (trees.size() < n) {
      Node<T> tree = growTreeFactory.build(random, depth);
      if (tree != null) {
        trees.add(tree);
      }
      depth = depth + 1;
      if (depth > maxDepth) {
        depth = minDepth;
      }
    }
    return trees;
  }

}
