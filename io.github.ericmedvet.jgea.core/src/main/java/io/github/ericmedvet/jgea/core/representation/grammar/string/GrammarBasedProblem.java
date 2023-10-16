
package io.github.ericmedvet.jgea.core.representation.grammar.string;

import io.github.ericmedvet.jgea.core.problem.Problem;
import io.github.ericmedvet.jgea.core.representation.tree.Tree;

import java.util.function.Function;
public interface GrammarBasedProblem<N, S> extends Problem<S> {

  StringGrammar<N> getGrammar();

  Function<Tree<N>, S> getSolutionMapper();

}
