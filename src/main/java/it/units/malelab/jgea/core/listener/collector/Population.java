/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core.listener.collector;

import it.units.malelab.jgea.core.Individual;
import it.units.malelab.jgea.core.Sized;
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
public class Population<G, S, F> implements DataCollector<G, S, F> {

  @Override
  public Map<String, Object> collect(EvolutionEvent<G, S, F> evolutionEvent) {
    List<Collection<Individual<G, S, F>>> rankedPopulation = new ArrayList<>(evolutionEvent.getRankedPopulation());
    Map<String, Object> indexes = new LinkedHashMap<>();
    double genoCount = 0;
    double solutionCount = 0;
    double genoSizeSum = 0;
    double solutionSizeSum = 0;
    double ageSum = 0;
    double count = 0;
    for (Collection<Individual<G, S, F>> rank : rankedPopulation) {
      for (Individual<G, S, F> individual : rank) {
        if (individual.getGenotype() instanceof Sized) {
          genoSizeSum = genoSizeSum + ((Sized) individual.getGenotype()).size();
          genoCount = genoCount + 1;
        }
        if (individual.getSolution() instanceof Sized) {
          solutionSizeSum = solutionSizeSum + ((Sized) individual.getSolution()).size();
          solutionCount = solutionCount + 1;
        }
        ageSum = ageSum + evolutionEvent.getIteration() - individual.getBirthIteration();
        count = count + 1;
      }
    }
    indexes.put("population.genotype.size.average", (int) Math.round(genoSizeSum / genoCount));
    indexes.put("population.solution.size.average", (int) Math.round(solutionSizeSum / solutionCount));
    indexes.put("population.age.average", (int) Math.round(ageSum / count));
    indexes.put("population.size", (int) count);
    indexes.put("population.ranks", rankedPopulation.size());
    indexes.put("population.rank0.size", rankedPopulation.get(0).size());
    return indexes;
  }

  @Override
  public Map<String, String> getFormattedNames() {
    LinkedHashMap<String, String> formattedNames = new LinkedHashMap<>();
    formattedNames.put("population.size", "%5d");
    formattedNames.put("population.ranks", "%3d");
    formattedNames.put("population.rank0.size", "%3d");
    formattedNames.put("population.genotype.size.average", "%5d");
    formattedNames.put("population.solution.size.average", "%4d");
    formattedNames.put("population.age.average", "%5d");
    return formattedNames;
  }

}
