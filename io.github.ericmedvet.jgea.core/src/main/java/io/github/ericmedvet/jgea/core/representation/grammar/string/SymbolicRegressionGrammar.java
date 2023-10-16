
package io.github.ericmedvet.jgea.core.representation.grammar.string;

import io.github.ericmedvet.jgea.core.representation.tree.numeric.Element;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
public class SymbolicRegressionGrammar extends StringGrammar<String> {
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
    rules().put("<e>", eProductions);
    arities.forEach(a -> rules().put(
        String.format("<o%d>", a),
        operators.stream().filter(o -> o.arity() == a).map(o -> List.of(o.toString())).toList()
    ));
    rules().put("<v>", variables.stream().map(List::of).toList());
    rules().put("<c>", constants.stream().map(c -> List.of(Double.toString(c))).toList());
  }
}
