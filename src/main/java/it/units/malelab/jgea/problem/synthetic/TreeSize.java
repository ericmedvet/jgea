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

package it.units.malelab.jgea.problem.synthetic;

import it.units.malelab.jgea.representation.tree.Tree;
import it.units.malelab.jgea.representation.grammar.Grammar;
import it.units.malelab.jgea.representation.grammar.GrammarBasedProblem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

/**
 * @author eric
 */
public class TreeSize implements GrammarBasedProblem<Boolean, Tree<Boolean>, Double> {

  private final Grammar<Boolean> grammar;
  private final Function<Tree<Boolean>, Double> fitnessFunction;

  public TreeSize(int nonTerminals, int terminals) {
    this.grammar = new Grammar<>();
    grammar.setStartingSymbol(false);
    grammar.getRules().put(false, l(r(nonTerminals, false), r(terminals, true)));
    fitnessFunction = (Tree<Boolean> tree) -> 1d / (double) tree.size();
  }

  @Override
  public Grammar<Boolean> getGrammar() {
    return grammar;
  }

  @Override
  public Function<Tree<Boolean>, Tree<Boolean>> getSolutionMapper() {
    return Function.identity();
  }

  @Override
  public Function<Tree<Boolean>, Double> getFitnessFunction() {
    return fitnessFunction;
  }

  private static <T> List<T> l(T... ts) {
    return Arrays.asList(ts);
  }

  private static <T> List<T> r(int n, T... ts) {
    List<T> list = new ArrayList<>(n * ts.length);
    for (int i = 0; i < n; i++) {
      list.addAll(l(ts));
    }
    return list;
  }

}
