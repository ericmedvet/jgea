/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core.listener.collector;

import it.units.malelab.jgea.core.Individual;
import it.units.malelab.jgea.core.Sized;
import it.units.malelab.jgea.core.function.Function;
import it.units.malelab.jgea.core.function.FunctionException;
import it.units.malelab.jgea.core.listener.event.EvolutionEvent;
import it.units.malelab.jgea.core.util.Misc;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author eric
 */
public class FunctionOfBest<G, S, F, V> implements Collector<G, S, F> {
  
  private final String name;
  private final Function<S, V> validationFunction;
  private final Function<V, Map<String, Object>> fitnessSplitter;
  private final Function<String, String> formatFunction;

  public FunctionOfBest(String name, Function<S, V> validationFunction, Function<V, Map<String, Object>> fitnessSplitter, Function<String, String> formatFunction) {
    this.name = name;
    this.validationFunction = validationFunction;
    this.fitnessSplitter = fitnessSplitter;
    this.formatFunction = formatFunction;
  }

  @Override
  public Map<String, Object> collect(EvolutionEvent<G, S, F> evolutionEvent) {
    List<Collection<Individual<G, S, F>>> rankedPopulation = new ArrayList<>(evolutionEvent.getRankedPopulation());
    Individual<G, S, F> best = Misc.first(rankedPopulation.get(0));
    Map<String, Object> indexes = new LinkedHashMap<>();
    try {
      for (Map.Entry<String, Object> fitnessEntry : fitnessSplitter.apply(validationFunction.apply(best.getSolution())).entrySet()) {
        indexes.put(
                augmentName("best."+name, fitnessEntry.getKey()),
                fitnessEntry.getValue());
      }
    } catch (FunctionException ex) {
      //ignore: leave null
    }
    return indexes;
  }

  @Override
  public Map<String, String> getFormattedNames() {
    LinkedHashMap<String, String> formattedNames = new LinkedHashMap<>();
    try {
      for (Map.Entry<String, Object> fitnessEntry : fitnessSplitter.apply(null).entrySet()) {
        formattedNames.put(
                augmentName("best."+name, fitnessEntry.getKey()),
                formatFunction.apply(fitnessEntry.getKey()));
      }
    } catch (FunctionException ex) {
      //ignore
    }
    return formattedNames;
  }

  private String augmentName(String prefix, String fitnessName) {
    if (fitnessName.isEmpty()) {
      return prefix;
    }
    return prefix + "." + fitnessName;
  }

}
