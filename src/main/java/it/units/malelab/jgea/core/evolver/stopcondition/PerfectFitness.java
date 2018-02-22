/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core.evolver.stopcondition;

import it.units.malelab.jgea.core.Individual;
import it.units.malelab.jgea.core.function.Bounded;
import it.units.malelab.jgea.core.function.NonDeterministicFunction;
import it.units.malelab.jgea.core.listener.event.EvolutionEvent;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author eric
 */
public class PerfectFitness<F> implements StopCondition {

  private final F targetFitness;

  public PerfectFitness(F targetFitness) {
    this.targetFitness = targetFitness;
  }
  
  public PerfectFitness(NonDeterministicFunction<?, F> fitnessFunction) {
    if (fitnessFunction instanceof Bounded) {
      targetFitness = ((Bounded<F>) fitnessFunction).bestValue();
    } else {
      targetFitness = null;
    }
  }

  @Override
  public boolean shouldStop(EvolutionEvent evolutionEvent) {
    if (targetFitness==null) {
      return false;
    }
    List<Collection<Individual>> rankedPopulation = (List)evolutionEvent.getRankedPopulation();
    for (Collection<Individual> rank : rankedPopulation) {
      for (Individual individual : rank) {
        if (individual.getFitness().equals(targetFitness)) {
          return true;
        }
      }
    }
    return false;
  }

}
