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

import it.units.malelab.jgea.core.operator.Mutation;
import it.units.malelab.jgea.core.util.Misc;

import java.util.List;
import java.util.Random;

/**
 * @author eric
 * @created 2020/08/05
 * @project jgea
 */
public class SubtreeMutation<N> implements Mutation<Tree<N>> {

  private final int maxHeight;
  private final TreeBuilder<N> builder;

  public SubtreeMutation(int maxHeight, TreeBuilder<N> builder) {
    this.maxHeight = maxHeight;
    this.builder = builder;
  }

  @Override
  public Tree<N> mutate(Tree<N> parent, Random random) {
    if (parent.height() > maxHeight) {
      return parent;
    }
    List<Tree<N>> subtrees = parent.topSubtrees();
    Tree<N> toReplaceSubtree = Misc.pickRandomly(subtrees, random);
    int maxDepth = subtrees.stream().filter(s -> s.equals(toReplaceSubtree)).mapToInt(Tree::depth).max().orElse(0);
    Tree<N> newSubtree = builder.build(random, maxHeight - maxDepth);
    return TreeUtils.replaceAll(Tree.copyOf(parent), toReplaceSubtree, newSubtree);
  }

}
