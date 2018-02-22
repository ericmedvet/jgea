/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core.evolver.stopcondition;

import it.units.malelab.jgea.core.listener.event.EvolutionEvent;
import it.units.malelab.jgea.core.function.CachedNonDeterministicFunction;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author eric
 */
public class RelativeElapsedTime implements StopCondition {
  
  private final double r;
  private final CachedNonDeterministicFunction cachedFitnessFunction;

  public RelativeElapsedTime(double r, CachedNonDeterministicFunction cachedFitnessFunction) {
    this.r = r;
    this.cachedFitnessFunction = cachedFitnessFunction;
  }

  public double getR() {
    return r;
  }

  public CachedNonDeterministicFunction getCachedFitnessMapper() {
    return cachedFitnessFunction;
  }

  @Override
  public boolean shouldStop(EvolutionEvent evolutionEvent) {
    double elapsedNanos = TimeUnit.NANOSECONDS.convert(evolutionEvent.getElapsedMillis(), TimeUnit.MILLISECONDS);
    double avgNanos = cachedFitnessFunction.getCacheStats().averageLoadPenalty();
    return elapsedNanos/avgNanos>r;
  }

}
