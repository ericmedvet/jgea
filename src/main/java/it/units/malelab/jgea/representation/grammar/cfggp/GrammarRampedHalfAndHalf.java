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
import it.units.malelab.jgea.representation.grammar.Grammar;
import it.units.malelab.jgea.representation.tree.Tree;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author eric
 */
public class GrammarRampedHalfAndHalf<T> implements Factory<Tree<T>> {

  private final int minHeight;
  private final int maxHeight;
  private final FullGrammarGrammarTreeFactory<T> fullGrammarTreeFactory;
  private final GrowGrammarTreeFactory<T> growGrammarTreeFactory;

  public GrammarRampedHalfAndHalf(int minHeight, int maxHeight, Grammar<T> grammar) {
    this.minHeight = minHeight;
    this.maxHeight = maxHeight;
    fullGrammarTreeFactory = new FullGrammarGrammarTreeFactory<>(maxHeight, grammar);
    growGrammarTreeFactory = new GrowGrammarTreeFactory<>(maxHeight, grammar);
  }

  @Override
  public List<Tree<T>> build(int n, Random random) {
    List<Tree<T>> trees = new ArrayList<>();
    //full
    int height = minHeight;
    while (trees.size() < n / 2) {
      Tree<T> tree = fullGrammarTreeFactory.build(random, height);
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
      Tree<T> tree = growGrammarTreeFactory.build(random, height);
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
