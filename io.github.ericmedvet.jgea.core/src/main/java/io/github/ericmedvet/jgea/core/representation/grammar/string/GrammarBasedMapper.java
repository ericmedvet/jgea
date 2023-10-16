
package io.github.ericmedvet.jgea.core.representation.grammar.string;

import io.github.ericmedvet.jgea.core.representation.tree.Tree;

import java.util.function.Function;
public abstract class GrammarBasedMapper<G, T> implements Function<G, Tree<T>> {

  protected final StringGrammar<T> grammar;

  public GrammarBasedMapper(StringGrammar<T> grammar) {
    this.grammar = grammar;
  }

  public StringGrammar<T> getGrammar() {
    return grammar;
  }

}
