/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.problem.mapper;

import it.units.malelab.jgea.distance.Distance;
import it.units.malelab.jgea.grammarbased.GrammarBasedProblem;

/**
 *
 * @author eric
 */
public class EnhancedProblem<N, S, F> {

  private final GrammarBasedProblem<N, S, F> problem;
  private final Distance<S> distance;

  public EnhancedProblem(GrammarBasedProblem<N, S, F> problem, Distance<S> distance) {
    this.problem = problem;
    this.distance = distance;
  }

  public GrammarBasedProblem<N, S, F> getProblem() {
    return problem;
  }

  public Distance<S> getDistance() {
    return distance;
  }

}
