/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core.evolver.stopcondition;

import it.units.malelab.jgea.core.listener.event.EvolutionEvent;
import it.units.malelab.jgea.core.function.CachedNonDeterministicFunction;

/**
 *
 * @author eric
 */
public class ActualFitnessEvaluations<G, S, F> implements StopCondition<G, S, F> {
  
  private final long n;
  private final CachedNonDeterministicFunction<S, F> cachedFitnessMapper;

  public ActualFitnessEvaluations(long n, CachedNonDeterministicFunction<S, F> cachedFitnessMapper) {
    this.n = n;
    this.cachedFitnessMapper = cachedFitnessMapper;
  }

  public long getN() {
    return n;
  }

  public CachedNonDeterministicFunction<S, F> getCachedFitnessMapper() {
    return cachedFitnessMapper;
  }

  @Override
  public boolean shouldStop(EvolutionEvent<G, S, F> evolutionEvent) {
    return cachedFitnessMapper.getActualCount()>n;
  }

}
