/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 *
 * @author eric
 */
public class Individual<G, S, F> implements Serializable {
  
  private final G genotype;
  private final S solution;
  private final F fitness;
  private final int birthIteration;
  private final List<Individual<G, S, F>> parents;
  private final Map<String, Object> info;

  public Individual(G genotype, S solution, F fitness, int birthIteration, List<Individual<G, S, F>> parents, Map<String, Object> info) {
    this.genotype = genotype;
    this.solution = solution;
    this.fitness = fitness;
    this.birthIteration = birthIteration;
    this.parents = parents;
    this.info = info;
  }

  public G getGenotype() {
    return genotype;
  }

  public S getSolution() {
    return solution;
  }

  public F getFitness() {
    return fitness;
  }

  public int getBirthIteration() {
    return birthIteration;
  }

  public List<Individual<G, S, F>> getParents() {
    return parents;
  }

  public Map<String, Object> getInfo() {
    return info;
  }
    
}
