/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.representation.grammar;

import it.units.malelab.jgea.representation.tree.Node;
import it.units.malelab.jgea.core.Problem;
import it.units.malelab.jgea.core.function.Function;

/**
 *
 * @author eric
 */
public interface GrammarBasedProblem<N, S, F> extends Problem<S, F> {

  public Grammar<N> getGrammar();
  public Function<Node<N>, S> getSolutionMapper();
  
}
