/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core.listener.collector;

import it.units.malelab.jgea.core.Individual;
import it.units.malelab.jgea.core.listener.event.EvolutionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author eric
 */
public class Diversity implements DataCollector {

  @Override
  public List<Item> collect(EvolutionEvent evolutionEvent) {
    List<Collection<Individual>> rankedPopulation = new ArrayList<>((List)evolutionEvent.getRankedPopulation());
    Set genotypes = new HashSet<>();
    Set solutions = new HashSet<>();
    Set fitnesses = new HashSet<>();
    double count = 0;
    for (Collection<Individual> rank : rankedPopulation) {
      for (Individual individual : rank) {
        genotypes.add(individual.getGenotype());
        solutions.add(individual.getSolution());
        fitnesses.add(individual.getFitness());
        count = count + 1;
      }
    }
    return Arrays.asList(
            new Item<>("diversity.genotype", (double) genotypes.size() / count, "%4.2f"),
            new Item<>("diversity.solution", (double) solutions.size() / count, "%4.2f"),
            new Item<>("diversity.fitness", (double) fitnesses.size() / count, "%4.2f")
    );
  }

}
