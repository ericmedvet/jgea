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

package io.github.ericmedvet.jgea.problem.synthetic;

import io.github.ericmedvet.jgea.core.problem.ComparableQualityBasedProblem;
import io.github.ericmedvet.jgea.core.representation.grammar.Grammar;
import io.github.ericmedvet.jgea.core.representation.grammar.GrammarBasedProblem;
import io.github.ericmedvet.jgea.core.representation.tree.Tree;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * @author eric
 */
public class TreeSize implements GrammarBasedProblem<Boolean, Tree<Boolean>>,
    ComparableQualityBasedProblem<Tree<Boolean>, Double> {

  private final static Function<Tree<Boolean>, Double> FITNESS_FUNCTION = t -> 1d / (double) t.size();
  private final Grammar<Boolean> grammar;

  public TreeSize(int nonTerminals, int terminals) {
    this.grammar = new Grammar<>();
    grammar.setStartingSymbol(false);
    grammar.getRules().put(false, List.of(r(nonTerminals, false), r(terminals, true)));
  }

  @SafeVarargs
  private static <T> List<T> r(int n, T... ts) {
    List<T> list = new ArrayList<>(n * ts.length);
    for (int i = 0; i < n; i++) {
      list.addAll(List.of(ts));
    }
    return list;
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
  public Function<Tree<Boolean>, Double> qualityFunction() {
    return FITNESS_FUNCTION;
  }
}
