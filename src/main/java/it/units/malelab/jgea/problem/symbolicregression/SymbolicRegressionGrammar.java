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

package it.units.malelab.jgea.problem.symbolicregression;

import it.units.malelab.jgea.problem.symbolicregression.element.Operator;
import it.units.malelab.jgea.representation.grammar.Grammar;

import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author eric
 * @created 2020/08/05
 * @project jgea
 */
public class SymbolicRegressionGrammar extends Grammar<String> {
  public SymbolicRegressionGrammar(List<Operator> operators, List<String> variables, List<Double> constants) {
    setStartingSymbol("<e>");
    SortedSet<Integer> arities = operators.stream()
        .map(Operator::arity)
        .collect(Collectors.toCollection(TreeSet::new));
    List<List<String>> eProductions = arities.stream()
        .map(a -> Stream.concat(List.of(String.format("<o%d>", a)).stream(), Collections.nCopies(a, "<e>").stream())
            .collect(Collectors.toList()))
        .collect(Collectors.toList());
    eProductions.add(List.of("<v>"));
    eProductions.add(List.of("<c>"));
    getRules().put("<e>", eProductions);
    arities.forEach(a -> getRules().put(String.format("<o%d>", a), operators.stream().filter(o -> o.arity() == a)
        .map(o -> List.of(o.toString()))
        .collect(Collectors.toList())));
    getRules().put("<v>", variables.stream()
        .map(List::of)
        .collect(Collectors.toList()));
    getRules().put("<c>", constants.stream()
        .map(c -> List.of(Double.toString(c)))
        .collect(Collectors.toList()));
  }
}
