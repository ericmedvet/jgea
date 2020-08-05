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

import it.units.malelab.jgea.core.IndependentFactory;
import it.units.malelab.jgea.core.operator.Crossover;

import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * @author eric
 * @created 2020/08/05
 * @project jgea
 */
public class SubtreeCrossover<N> implements Crossover<Tree<N>> {

  private final int maxHeight;

  public SubtreeCrossover(int maxHeight) {
    this.maxHeight = maxHeight;
  }

  @Override
  public Tree<N> recombine(Tree<N> parent1, Tree<N> parent2, Random random) {
    List<Tree<N>> subtrees1 = parent1.topSubtrees();
    List<Tree<N>> subtrees2 = parent2.topSubtrees();
    Collections.shuffle(subtrees1, random);
    Collections.shuffle(subtrees2, random);
    for (Tree<N> subtree1 : subtrees1) {
      for (Tree<N> subtree2 : subtrees2) {
        System.out.printf("\t%s <-> %s%n", subtree1, subtree2);
        if (subtree1.depth() + subtree2.height() <= maxHeight) {
          return TreeUtils.replaceFirst(parent1, subtree1, subtree2);
        }
      }
    }
    return Tree.copyOf(parent1);
  }

  public static void main(String[] args) {
    Random r = new Random();
    Tree<String> t1 = new GrowTreeBuilder<>(s -> 2, IndependentFactory.picker("+-".split("")), IndependentFactory.picker("abc".split(""))).build(r, 5);
    Tree<String> t2 = new GrowTreeBuilder<>(s -> 2, IndependentFactory.picker("+-".split("")), IndependentFactory.picker("012".split(""))).build(r, 5);
    SubtreeCrossover<String> crossover = new SubtreeCrossover<>(7);
    System.out.println(t1);
    System.out.println(t2);
    System.out.println(crossover.recombine(t1, t2, r));
  }
}
