/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core.evolver.stopcondition;

import it.units.malelab.jgea.core.listener.event.EvolutionEvent;
import it.units.malelab.jgea.core.mapper.CachedMapper;

/**
 *
 * @author eric
 */
public class FitnessEvaluations<G, S, F> implements StopCondition<G, S, F>{
  
  private final long n;

  public FitnessEvaluations(long n) {
    this.n = n;
  }

  @Override
  public boolean shouldStop(EvolutionEvent<G, S, F> evolutionEvent) {
    return evolutionEvent.getFitnessEvaluations()>=n;
  }
  
}
