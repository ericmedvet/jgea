/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.grammarbased;

import it.units.malelab.jgea.core.Node;
import it.units.malelab.jgea.core.Problem;
import it.units.malelab.jgea.core.mapper.BoundMapper;

/**
 *
 * @author eric
 */
public class GrammarBasedProblem<T, S, F> extends Problem<Node<S>, F> {

  private final Grammar<T> grammar;

  public GrammarBasedProblem(Grammar<T> grammar, BoundMapper<Node<S>, F> fitnessMapper) {
    super(fitnessMapper);
    this.grammar = grammar;
  }

  public Grammar<T> getGrammar() {
    return grammar;
  }
  
}
