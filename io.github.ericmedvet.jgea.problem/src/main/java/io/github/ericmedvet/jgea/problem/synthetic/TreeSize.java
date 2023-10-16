
package io.github.ericmedvet.jgea.problem.synthetic;

import io.github.ericmedvet.jgea.core.problem.ComparableQualityBasedProblem;
import io.github.ericmedvet.jgea.core.representation.grammar.string.GrammarBasedProblem;
import io.github.ericmedvet.jgea.core.representation.grammar.string.StringGrammar;
import io.github.ericmedvet.jgea.core.representation.tree.Tree;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
public class TreeSize implements GrammarBasedProblem<Boolean, Tree<Boolean>>,
    ComparableQualityBasedProblem<Tree<Boolean>, Double> {

  private final static Function<Tree<Boolean>, Double> FITNESS_FUNCTION = t -> 1d / (double) t.size();
  private final StringGrammar<Boolean> grammar;

  public TreeSize(int nonTerminals, int terminals) {
    this.grammar = new StringGrammar<>();
    grammar.setStartingSymbol(false);
    grammar.rules().put(false, List.of(r(nonTerminals, false), r(terminals, true)));
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
  public StringGrammar<Boolean> getGrammar() {
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
