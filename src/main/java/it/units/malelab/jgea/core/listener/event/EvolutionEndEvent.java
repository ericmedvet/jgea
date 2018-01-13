/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core.listener.event;

import it.units.malelab.jgea.core.evolver.stopcondition.StopCondition;
import java.util.List;

/**
 *
 * @author eric
 */
public class EvolutionEndEvent extends EvolutionEvent {
  
  private final StopCondition stopCondition;

  public EvolutionEndEvent(StopCondition stopCondition, int iteration, List rankedPopulation) {
    super(iteration, rankedPopulation);
    this.stopCondition = stopCondition;
  }

  public StopCondition getStopCondition() {
    return stopCondition;
  }
  
}
