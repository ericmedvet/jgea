/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.representation.grammar;

import it.units.malelab.jgea.representation.tree.Node;
import it.units.malelab.jgea.core.function.Function;

/**
 *
 * @author eric
 */
public abstract class GrammarBasedMapper<G, T> implements Function<G, Node<T>> {
  
  protected final Grammar<T> grammar;

  public GrammarBasedMapper(Grammar<T> grammar) {
    this.grammar = grammar;
  }

  public Grammar<T> getGrammar() {
    return grammar;
  }
  
}
