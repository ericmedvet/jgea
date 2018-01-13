/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core.listener.event;

import it.units.malelab.jgea.core.Individual;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author eric
 */
public class EvolutionEvent<G, S, F> implements Event {
  
  private final int iteration;
  private final List<Collection<Individual<G, S, F>>> rankedPopulation;

  public EvolutionEvent(int iteration, List<Collection<Individual<G, S, F>>> rankedPopulation) {
    this.iteration = iteration;
    this.rankedPopulation = rankedPopulation;
  }

  public int getIteration() {
    return iteration;
  }

  public List<Collection<Individual<G, S, F>>> getRankedPopulation() {
    return rankedPopulation;
  }
  
}
