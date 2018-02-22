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
import it.units.malelab.jgea.core.listener.Listener;
import it.units.malelab.jgea.core.listener.event.EvolutionEvent;
import it.units.malelab.jgea.core.util.Misc;
import it.units.malelab.jgea.core.util.WithNames;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 * @author eric
 */
public class BestInfo<F> implements DataCollector {

  private final Function<F, Map<String, Object>> fitnessSplitter;
  private final Function<String, String> formatFunction;

  public BestInfo(Function<F, Map<String, Object>> fitnessSplitter, Function<String, String> formatFunction) {
    this.fitnessSplitter = fitnessSplitter;
    this.formatFunction = formatFunction;
  }

  public static <L extends List> Function<L, Map<String, Object>> fromNames(final WithNames withNames) {
    return (L list, Listener listener) -> {
      if (list==null) {
        return withNames.names().stream().collect(Collectors.toMap(name -> name, name -> ""));
      }
      Map<String, Object> map = new HashMap<>();
      for (int i = 0; i<Math.min(withNames.names().size(), list.size()); i++) {
        map.put(withNames.names().get(i), list.get(i));
      }
      return map;
    };
  }

  @Override
  public Map<String, Object> collect(EvolutionEvent evolutionEvent) {
    List<Collection<Individual>> rankedPopulation = new ArrayList<>((List)evolutionEvent.getRankedPopulation());
    Individual best = Misc.first(rankedPopulation.get(0));
    Map<String, Object> indexes = new LinkedHashMap<>();
    try {
      for (Map.Entry<String, Object> fitnessEntry : fitnessSplitter.apply((F)best.getFitness()).entrySet()) {
        indexes.put(
                augmentFitnessName("best.fitness", fitnessEntry.getKey()),
                fitnessEntry.getValue());
      }
    } catch (FunctionException ex) {
      //ignore: leave null
    }
    indexes.put("best.genotype.size", size(best.getGenotype()));
    indexes.put("best.solution.size", size(best.getSolution()));
    indexes.put("best.age", evolutionEvent.getIteration() - best.getBirthIteration());
    return indexes;
  }
  
  private Integer size(Object o) {
    if (o instanceof Sized) {
      return ((Sized)o).size();
    }
    if (o instanceof Collection) {
      return ((Collection)o).size();
    }
    if (o instanceof String) {
      return ((String)o).length();
    }
    return null;
  }

  @Override
  public Map<String, String> getFormattedNames() {
    LinkedHashMap<String, String> formattedNames = new LinkedHashMap<>();
    try {
      for (Map.Entry<String, Object> fitnessEntry : fitnessSplitter.apply(null).entrySet()) {
        formattedNames.put(
                augmentFitnessName("best.fitness", fitnessEntry.getKey()),
                formatFunction.apply(fitnessEntry.getKey()));
      }
    } catch (FunctionException ex) {
      //ignore
    }
    formattedNames.put("best.genotype.size", "%4d");
    formattedNames.put("best.solution.size", "%4d");
    formattedNames.put("best.age", "%5d");
    return formattedNames;
  }

  private String augmentFitnessName(String prefix, String fitnessName) {
    if (fitnessName.isEmpty()) {
      return prefix;
    }
    return prefix + "." + fitnessName;
  }

}
