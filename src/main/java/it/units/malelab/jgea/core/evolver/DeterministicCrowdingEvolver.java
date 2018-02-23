/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core.evolver;

import it.units.malelab.jgea.core.Factory;
import it.units.malelab.jgea.core.Individual;
import it.units.malelab.jgea.core.evolver.stopcondition.StopCondition;
import it.units.malelab.jgea.core.function.NonDeterministicFunction;
import it.units.malelab.jgea.core.operator.GeneticOperator;
import it.units.malelab.jgea.core.ranker.Ranker;
import it.units.malelab.jgea.core.ranker.selector.Selector;
import it.units.malelab.jgea.distance.Distance;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author eric
 */
public class DeterministicCrowdingEvolver<G, S, F> extends StandardEvolver<G, S, F> {

  private final Distance<Individual<G, S, F>> distance;
  private final boolean localSaveAncestry;

  public DeterministicCrowdingEvolver(Distance<Individual<G, S, F>> distance, int populationSize, Factory<G> genotypeBuilder, Ranker<Individual<G, S, F>> ranker, NonDeterministicFunction<G, S> mapper, Map<GeneticOperator<G>, Double> operators, Selector<Individual<G, S, F>> parentSelector, Selector<Individual<G, S, F>> unsurvivalSelector, List<StopCondition> stoppingConditions, long cacheSize, boolean saveAncestry) {
    super(populationSize, genotypeBuilder, ranker, mapper, operators, parentSelector, unsurvivalSelector, populationSize, true, stoppingConditions, cacheSize, true);
    this.distance = distance;
    this.localSaveAncestry = saveAncestry;
  }

  @Override
  protected <I extends Individual<G, S, F>> void updatePopulation(final List<I> population, final List<I> newPopulation, Random random, AtomicInteger fitnessEvaluations) {
    for (I newIndividual : newPopulation) {
      //find parents
      List<Individual<G, S, F>> parents = new ArrayList<>(newIndividual.getParents());
      parents.retainAll(population);
      if (parents.isEmpty()) {
        population.add(newIndividual);
        continue;
      }
      //find closest parent
      I closestParent = (I)parents.stream()
              .sorted((i1, i2) -> (Double.compare(distance.apply(newIndividual, i1), distance.apply(newIndividual, i2))))
              .findFirst()
              .get();
      //rank parent and child
      if (ranker.compare(newIndividual, closestParent, random)==-1) {
        population.remove(closestParent);
        population.add(newIndividual);
        if (!localSaveAncestry) {
          //remove ancestry info
          newIndividual.getParents().clear();
        }
      }
    }
  }

}
