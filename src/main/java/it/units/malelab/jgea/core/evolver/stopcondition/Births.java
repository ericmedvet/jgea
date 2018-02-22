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
public class Births implements StopCondition {

  private final int n;

  public Births(int n) {
    this.n = n;
  }

  public int getN() {
    return n;
  }

  @Override
  public boolean shouldStop(EvolutionEvent evolutionEvent) {
    return evolutionEvent.getBirths() > n;
  }

}
