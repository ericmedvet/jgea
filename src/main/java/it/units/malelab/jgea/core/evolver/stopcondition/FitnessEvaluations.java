/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core.evolver.stopcondition;

import it.units.malelab.jgea.core.listener.event.EvolutionEvent;

/**
 *
 * @author eric
 */
public class FitnessEvaluations implements StopCondition {

  private final long n;

  public FitnessEvaluations(long n) {
    this.n = n;
  }

  @Override
  public boolean shouldStop(EvolutionEvent evolutionEvent) {
    return evolutionEvent.getFitnessEvaluations() >= n;
  }

}
