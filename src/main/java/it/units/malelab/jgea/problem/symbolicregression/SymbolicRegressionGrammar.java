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

package it.units.malelab.jgea.problem.symbolicregression;

import it.units.malelab.jgea.core.representation.grammar.Grammar;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author eric
 */
public class SymbolicRegressionGrammar extends Grammar<String> {
  public SymbolicRegressionGrammar(List<Element.Operator> operators, List<String> variables, List<Double> constants) {
    setStartingSymbol("<e>");
    SortedSet<Integer> arities = operators.stream()
        .map(Element.Operator::arity)
        .collect(Collectors.toCollection(TreeSet::new));
    List<List<String>> eProductions = new ArrayList<>(arities.stream()
        .map(a -> Stream.concat(Stream.of(String.format("<o%d>", a)), Collections.nCopies(a, "<e>").stream()).toList())
        .toList());
    eProductions.add(List.of("<v>"));
    eProductions.add(List.of("<c>"));
    getRules().put("<e>", eProductions);
    arities.forEach(a -> getRules().put(
        String.format("<o%d>", a),
        operators.stream().filter(o -> o.arity() == a).map(o -> List.of(o.toString())).toList()
    ));
    getRules().put("<v>", variables.stream().map(List::of).toList());
    getRules().put("<c>", constants.stream().map(c -> List.of(Double.toString(c))).toList());
  }
}
