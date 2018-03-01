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
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author eric
 */
public class Population implements DataCollector {

  @Override
  public List<Item> collect(EvolutionEvent evolutionEvent) {
    List<Collection<Individual>> rankedPopulation = new ArrayList<>((List)evolutionEvent.getRankedPopulation());
    double genoCount = 0;
    double solutionCount = 0;
    double genoSizeSum = 0;
    double solutionSizeSum = 0;
    double ageSum = 0;
    double count = 0;
    for (Collection<Individual> rank : rankedPopulation) {
      for (Individual individual : rank) {
        Integer genoSize = BestInfo.size(individual.getGenotype());
        if (genoSize!=null) {
          genoSizeSum = genoSizeSum + genoSize;
          genoCount = genoCount + 1;
        }
        Integer solutionSize = BestInfo.size(individual.getSolution());
        if (solutionSize!=null) {
          solutionSizeSum = solutionSizeSum + solutionSize;
          solutionCount = solutionCount + 1;
        }
        ageSum = ageSum + evolutionEvent.getIteration() - individual.getBirthIteration();
        count = count + 1;
      }
    }
    return Arrays.asList(
            new Item<>("population.genotype.size.average", (int) Math.round(genoSizeSum / genoCount), "%5d"),
            new Item<>("population.solution.size.average", (int) Math.round(solutionSizeSum / solutionCount), "%5d"),
            new Item<>("population.age.average", (int) Math.round(ageSum / count), "%2d"),
            new Item<>("population.size", (int) count, "%4d"),
            new Item<>("population.ranks", rankedPopulation.size(), "%3d"),
            new Item<>("population.rank0.size", rankedPopulation.get(0).size(), "%4d")
    );
  }

}
