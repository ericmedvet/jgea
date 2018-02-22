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
public class EvolutionEvent implements Event {
  
  private final int iteration;
  private final long births;
  private final long fitnessEvaluations;
  private final List<Collection<? extends Individual>> rankedPopulation;
  private final long elapsedMillis;

  public EvolutionEvent(int iteration, long births, long fitnessEvaluations, List<Collection<? extends Individual>> rankedPopulation, long elapsedMillis) {
    this.iteration = iteration;
    this.births = births;
    this.fitnessEvaluations = fitnessEvaluations;
    this.rankedPopulation = rankedPopulation;
    this.elapsedMillis = elapsedMillis;
  }

  public int getIteration() {
    return iteration;
  }

  public long getBirths() {
    return births;
  }

  public long getFitnessEvaluations() {
    return fitnessEvaluations;
  }

  public List<Collection<? extends Individual>> getRankedPopulation() {
    return rankedPopulation;
  }

  public long getElapsedMillis() {
    return elapsedMillis;
  }

}
