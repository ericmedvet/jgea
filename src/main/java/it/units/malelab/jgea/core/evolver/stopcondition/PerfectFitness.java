/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core.evolver.stopcondition;

import it.units.malelab.jgea.core.Individual;
import it.units.malelab.jgea.core.listener.event.EvolutionEvent;
import it.units.malelab.jgea.core.mapper.BoundMapper;
import java.util.Collection;

/**
 *
 * @author eric
 */
public class PerfectFitness<G, S, F> implements StopCondition<G, S, F> {

  private final F targetFitness;

  public PerfectFitness(F targetFitness) {
    this.targetFitness = targetFitness;
  }

  @Override
  public boolean shouldStop(EvolutionEvent<G, S, F> evolutionEvent) {
    for (Collection<Individual<G, S, F>> rank : evolutionEvent.getRankedPopulation()) {
      for (Individual<G, S, F> individual : rank) {
        if (individual.getFitness().equals(targetFitness)) {
          return true;
        }
      }
    }
    return false;
  }

}
