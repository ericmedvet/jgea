/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core.listener.collector;

import it.units.malelab.jgea.core.Individual;
import it.units.malelab.jgea.core.Sized;
import it.units.malelab.jgea.core.function.Function;
import it.units.malelab.jgea.core.listener.event.EvolutionEvent;
import it.units.malelab.jgea.core.function.FunctionException;
import it.units.malelab.jgea.core.util.Misc;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author eric
 */
public abstract class Best<G, S, F> implements Collector<G, S, F> {

  private final Function<S, F> validationFitnessFunction;

  public Best(Function<S, F> validationFitnessFunction) {
    this.validationFitnessFunction = validationFitnessFunction;
  }

  @Override
  public Map<String, Object> collect(EvolutionEvent<G, S, F> evolutionEvent) {
    List<Collection<Individual<G, S, F>>> rankedPopulation = new ArrayList<>(evolutionEvent.getRankedPopulation());
    Individual<G, S, F> best = Misc.first(rankedPopulation.get(0));
    Map<String, Object> indexes = new LinkedHashMap<>();
    for (Map.Entry<String, Object> fitnessEntry : getFitnessIndexes(best.getFitness()).entrySet()) {
      indexes.put(
              augmentFitnessName("best.fitness", fitnessEntry.getKey()),
              fitnessEntry.getValue());
    }
    if (validationFitnessFunction != null) {
      try {
        F validationFitness = validationFitnessFunction.apply(best.getSolution());
        for (Map.Entry<String, Object> fitnessEntry : getFitnessIndexes(validationFitness).entrySet()) {
          indexes.put(
                  augmentFitnessName("best.validation.fitness", fitnessEntry.getKey()),
                  fitnessEntry.getValue());
        }
      } catch (FunctionException ex) {
        Logger.getLogger(Best.class.getName()).log(Level.WARNING, "Cannot compute best validation fitness.", ex);
      }
    }
    if (best.getGenotype() instanceof Sized) {
      indexes.put("best.genotype.size", ((Sized)best.getGenotype()).size());
    }
    if (best.getSolution() instanceof Sized) {
      indexes.put("best.solution.size", ((Sized)best.getSolution()).size());
    }
    indexes.put("best.age", evolutionEvent.getIteration()-best.getBirthIteration());
    return indexes;
  }

  @Override
  public Map<String, String> getFormattedNames() {
    LinkedHashMap<String, String> formattedNames = new LinkedHashMap<>();
    for (Map.Entry<String, String> fitnessEntry : getFitnessFormattedNames().entrySet()) {
      formattedNames.put(
              augmentFitnessName("best.fitness", fitnessEntry.getKey()),
              fitnessEntry.getValue());
    }
    for (Map.Entry<String, String> fitnessEntry : getFitnessFormattedNames().entrySet()) {
      formattedNames.put(
              augmentFitnessName("best.validation.fitness", fitnessEntry.getKey()),
              fitnessEntry.getValue());
    }
    formattedNames.put("best.genotype.size", "%4d");
    formattedNames.put("best.solution.size", "%4d");
    formattedNames.put("best.age", "%5d");
    return formattedNames;
  }

  private int getAncestrySize(Individual<G, S, F> individual) {
    int count = 1;
    for (Individual<G, S, F> parent : individual.getParents()) {
      count = count + getAncestrySize(parent);
    }
    return count;
  }

  private int getAncestryDepth(Individual<G, S, F> individual) {
    int count = 1;
    for (Individual<G, S, F> parent : individual.getParents()) {
      count = Math.max(count, getAncestryDepth(parent) + 1);
    }
    return count;
  }

  protected abstract Map<String, String> getFitnessFormattedNames();

  protected abstract Map<String, Object> getFitnessIndexes(F fitness);

  private String augmentFitnessName(String prefix, String fitnessName) {
    if (fitnessName.isEmpty()) {
      return prefix;
    }
    return prefix + "." + fitnessName;
  }

}
