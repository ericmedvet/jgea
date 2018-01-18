/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core.evolver.stopcondition;

import it.units.malelab.jgea.core.listener.event.EvolutionEvent;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author eric
 */
public class ElapsedTime implements StopCondition {
  
  private final double t;
  private final TimeUnit timeUnit;

  public ElapsedTime(double t, TimeUnit timeUnit) {
    this.t = t;
    this.timeUnit = timeUnit;
  }

  public double getT() {
    return t;
  }

  public TimeUnit getTimeUnit() {
    return timeUnit;
  }

  @Override
  public boolean shouldStop(EvolutionEvent evolutionEvent) {
    long tMillis = TimeUnit.MILLISECONDS.convert((long)t, timeUnit);
    return evolutionEvent.getElapsedMillis()>tMillis;
  }
  
  
}
