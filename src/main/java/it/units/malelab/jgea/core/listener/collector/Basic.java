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
public class Basic implements DataCollector {

  @Override
  public Map<String, Object> collect(EvolutionEvent evolutionEvent) {
    List<Collection<Individual>> rankedPopulation = new ArrayList<>((List)evolutionEvent.getRankedPopulation());
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
    formattedNames.put("iterations", "%4d");
    formattedNames.put("births", "%8d");
    formattedNames.put("fitness.evaluations", "%6d");
    formattedNames.put("elapsed.sec", "%6.1f");
    return formattedNames;
  }

}
