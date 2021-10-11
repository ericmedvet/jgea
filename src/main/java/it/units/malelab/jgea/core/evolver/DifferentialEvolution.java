/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core.evolver;

import it.units.malelab.jgea.core.Factory;
import it.units.malelab.jgea.core.Individual;
import it.units.malelab.jgea.core.order.PartialComparator;
import it.units.malelab.jgea.core.order.PartiallyOrderedCollection;
import it.units.malelab.jgea.core.util.Misc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.logging.Logger;

/**
 * @author federico
 * DE/rand/1
 */
public class DifferentialEvolution<S, F> extends AbstractIterativeEvolver<List<Double>, S, F> {

  protected final int populationSize;
  protected final double differentialWeight;
  protected final double crossoverProb;
  protected final boolean remap;

  private static final Logger L = Logger.getLogger(DifferentialEvolution.class.getName());

  public DifferentialEvolution(
      Function<? super List<Double>, ? extends S> solutionMapper,
      Factory<? extends List<Double>> genotypeFactory,
      PartialComparator<? super Individual<List<Double>, S, F>> individualComparator,
      int populationSize,
      double differentialWeight,
      double crossoverProb,
      boolean remap) {
    super(solutionMapper, genotypeFactory, individualComparator);
    this.populationSize = populationSize;
    this.differentialWeight = differentialWeight;
    this.crossoverProb = crossoverProb;
    this.remap = remap;
  }

  @Override
  protected Collection<Individual<List<Double>, S, F>> initPopulation(Function<S, F> function, Random random, ExecutorService executorService, State state) throws ExecutionException, InterruptedException {
    return this.initPopulation(this.populationSize, function, random, executorService, state);
  }

  @Override
  protected Collection<Individual<List<Double>, S, F>> updatePopulation(PartiallyOrderedCollection<Individual<List<Double>, S, F>> population, Function<S, F> function, Random random, ExecutorService executorService, State state) throws ExecutionException, InterruptedException {
    List<Individual<List<Double>, S, F>> offspring = new ArrayList<>(this.populationSize * 2);
    Collection<List<Double>> trialGenotypes = this.computeTrials(population, random);
    L.fine(String.format("Trials computed: %d individuals", trialGenotypes.size()));
    if (this.remap) {
      // we remap all parents, regardless of their fate
      offspring.addAll(map(trialGenotypes, population.all(), this.solutionMapper, function, executorService, state));
    }
    else {
      offspring.addAll(map(trialGenotypes, List.of(), this.solutionMapper, function, executorService, state));
      offspring.addAll(population.all());
    }
    L.fine(String.format("Trials evaluated: %d individuals", trialGenotypes.size()));
    for (int i = 0, j = this.populationSize; i < this.populationSize && j < offspring.size(); ++i, ++j) {
      if (this.individualComparator.compare(offspring.get(i), offspring.get(j)).equals(PartialComparator.PartialComparatorOutcome.BEFORE)) {
        offspring.remove(j--);
      }
      else {
        offspring.remove(i--);
        --j;
      }
    }
    L.fine(String.format("Population selected: %d individuals", offspring.size()));
    return offspring;
  }

  protected List<Double> pickParents(PartiallyOrderedCollection<Individual<List<Double>, S, F>> population, Random random, List<Double> prev) {//List<Integer> indexes, Random random) {
    List<Double> current = prev;
    while (current == prev) {
      current = Misc.pickRandomly(population.all(), random).getGenotype();
    }
    return current;
  }

  protected Collection<List<Double>> computeTrials(PartiallyOrderedCollection<Individual<List<Double>, S, F>> population, Random random) {
    Collection<List<Double>> trialGenotypes = new ArrayList<>(this.populationSize);
    for (Individual<List<Double>, S, F> parent : population.all()) {
      List<Double> x = parent.getGenotype();
      List<Double> trial = new ArrayList<>(x.size());
      List<Double> a = this.pickParents(population, random, x);
      List<Double> b = this.pickParents(population, random, a);
      List<Double> c = this.pickParents(population, random, b);
      for (int j = 0; j < x.size(); ++j) {
        if (random.nextDouble() < this.crossoverProb) {
          trial.add(a.get(j) + this.differentialWeight * (b.get(j) - c.get(j)));
        } else {
          trial.add(x.get(j));
        }
      }
      trialGenotypes.add(trial);
    }
    return trialGenotypes;
  }

}
