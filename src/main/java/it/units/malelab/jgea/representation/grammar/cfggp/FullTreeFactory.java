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

import it.units.malelab.jgea.representation.tree.Tree;
import it.units.malelab.jgea.core.util.Pair;
import it.units.malelab.jgea.representation.grammar.Grammar;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author eric
 */
public class FullTreeFactory<T> extends GrowTreeFactory<T> {

  public FullTreeFactory(int maxDepth, Grammar<T> grammar) {
    super(maxDepth, grammar);
  }

  public Tree<T> build(Random random, T symbol, int targetDepth) {
    if (targetDepth < 0) {
      return null;
    }
    Tree<T> tree = Tree.of(symbol);
    if (grammar.getRules().containsKey(symbol)) {
      //a non-terminal
      List<List<T>> options = grammar.getRules().get(symbol);
      List<List<T>> availableOptions = new ArrayList<>();
      //general idea: try the following
      //1. choose expansion with min,max including target depth
      //2. choose expansion
      for (List<T> option : options) {
        Pair<Double, Double> minMax = optionMinMaxDepth(option);
        if (((targetDepth - 1) >= minMax.first()) && ((targetDepth - 1) <= minMax.second())) {
          availableOptions.add(option);
        }
      }
      if (availableOptions.isEmpty()) {
        availableOptions.addAll(options);
      }
      int optionIndex = random.nextInt(availableOptions.size());
      for (int i = 0; i < availableOptions.get(optionIndex).size(); i++) {
        Tree<T> child = build(random, availableOptions.get(optionIndex).get(i), targetDepth - 1);
        if (child == null) {
          return null;
        }
        tree.addChild(child);
      }
    }
    return tree;
  }

}
