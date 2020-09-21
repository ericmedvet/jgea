/*
 * Copyright 2020 Eric Medvet <eric.medvet@gmail.com> (as eric)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it.units.malelab.jgea.problem.synthetic;

import it.units.malelab.jgea.representation.grammar.Grammar;
import it.units.malelab.jgea.representation.grammar.GrammarBasedProblem;
import it.units.malelab.jgea.representation.tree.Tree;

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
