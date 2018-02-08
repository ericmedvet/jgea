/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core.listener.collector;

import it.units.malelab.jgea.core.Individual;
import it.units.malelab.jgea.core.listener.event.EvolutionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author eric
 */
public class Basic<G, S, F> implements Collector<G, S, F> {

  @Override
  public Map<String, Object> collect(EvolutionEvent<G, S, F> evolutionEvent) {
    List<Collection<Individual<G, S, F>>> rankedPopulation = new ArrayList<>(evolutionEvent.getRankedPopulation());
    Map<String, Object> indexes = new LinkedHashMap<>();
    indexes.put("iterations", evolutionEvent.getIteration());
    indexes.put("births", evolutionEvent.getBirths());
    indexes.put("fitness.evaluations", evolutionEvent.getFitnessEvaluations());
    indexes.put("elapsed.sec", (double)evolutionEvent.getElapsedMillis()/1000d);
    return indexes;
  }

  @Override
  public Map<String, String> getFormattedNames() {
    LinkedHashMap<String, String> formattedNames = new LinkedHashMap<>();
    formattedNames.put("iterations", "%3d");
    formattedNames.put("births", "%8d");
    formattedNames.put("fitness.evaluations", "%6d");
    formattedNames.put("elapsed.sec", "%6.1f");
    return formattedNames;
  }

}
