/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core.evolver.stopcondition;

import it.units.malelab.jgea.core.listener.event.EvolutionEvent;
import it.units.malelab.jgea.core.function.CachedFunction;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author eric
 */
public class RelativeElapsedTime<G, S, F> implements StopCondition<G, S, F> {
  
  private final double r;
  private final CachedFunction<S, F> cachedFitnessFunction;

  public RelativeElapsedTime(double r, CachedFunction<S, F> cachedFitnessFunction) {
    this.r = r;
    this.cachedFitnessFunction = cachedFitnessFunction;
  }

  public double getR() {
    return r;
  }

  public CachedFunction<S, F> getCachedFitnessMapper() {
    return cachedFitnessFunction;
  }

  @Override
  public boolean shouldStop(EvolutionEvent<G, S, F> evolutionEvent) {
    double elapsedNanos = TimeUnit.NANOSECONDS.convert(evolutionEvent.getElapsedMillis(), TimeUnit.MILLISECONDS);
    double avgNanos = cachedFitnessFunction.getCacheStats().averageLoadPenalty();
    return elapsedNanos/avgNanos>r;
  }

}
