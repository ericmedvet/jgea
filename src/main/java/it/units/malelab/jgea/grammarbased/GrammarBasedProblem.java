/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.grammarbased;

import it.units.malelab.jgea.core.Node;
import it.units.malelab.jgea.core.Problem;
import it.units.malelab.jgea.core.mapper.BoundMapper;
import it.units.malelab.jgea.core.mapper.DeterministicMapper;

/**
 *
 * @author eric
 */
public class GrammarBasedProblem<N, S, F> extends Problem<S, F> {

  private final Grammar<N> grammar;
  private final DeterministicMapper<Node<N>, S> solutionMapper;

  public GrammarBasedProblem(Grammar<N> grammar, DeterministicMapper<Node<N>, S> solutionMapper, BoundMapper<S, F> fitnessMapper) {
    super(fitnessMapper);
    this.grammar = grammar;
    this.solutionMapper = solutionMapper;
  }

  public Grammar<N> getGrammar() {
    return grammar;
  }

  public DeterministicMapper<Node<N>, S> getSolutionMapper() {
    return solutionMapper;
  }
  
}
