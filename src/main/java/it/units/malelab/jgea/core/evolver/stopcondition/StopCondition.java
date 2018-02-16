/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core.evolver.stopcondition;

import it.units.malelab.jgea.core.listener.event.EvolutionEvent;
import java.io.Serializable;

/**
 *
 * @author eric
 */
@FunctionalInterface
public interface StopCondition<G, S, F> extends Serializable {
  
  public boolean shouldStop(EvolutionEvent<G, S, F> evolutionEvent);
  
}
