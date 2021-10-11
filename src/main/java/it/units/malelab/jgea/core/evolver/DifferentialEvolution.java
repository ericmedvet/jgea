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

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
      List<Individual<List<Double>, S, F>> offspring = new ArrayList<>(population.all());
      if (this.remap) {
        // we remap all parents, regardless of their fate
        offspring.addAll(map(List.of(), population.all(), this.solutionMapper, function, executorService, state));
      }
      else {
        offspring.addAll(population.all());
      }
      List<Integer> indexes = IntStream.range(0, population.size()).boxed().collect(Collectors.toList());
      int i = 0;
      for (Individual<List<Double>, S, F> parent : offspring) {
        List<Double> x = parent.getGenotype();
        List<Double> trial = new ArrayList<>(x.size());
        int[] abc = pickParents(indexes, random);
        for (int j = 0; j < x.size(); ++j) {
          if (random.nextDouble() < this.crossoverProb) {
            trial.add(offspring.get(abc[0]).getGenotype().get(j) + this.differentialWeight * (offspring.get(abc[1]).getGenotype().get(j) - offspring.get(abc[2]).getGenotype().get(j)));
          } else {
            trial.add(x.get(j));
          }
        }
        Individual<List<Double>, S, F> trialIndividual = (Individual<List<Double>, S, F>) map(List.of(trial), List.of(), this.solutionMapper, function, executorService, state).get(0);
        if (this.individualComparator.compare(trialIndividual, parent).equals(PartialComparator.PartialComparatorOutcome.BEFORE)) {
          offspring.set(i, trialIndividual);
        }
        ++i;
      }
      L.fine(String.format("Trials computed: %d individuals", offspring.size()));
      return offspring;
    }

    protected static int[] pickParents(List<Integer> indexes, Random random) {
      int a = indexes.get(random.nextInt(indexes.size()));
      int b = -1;
      while (b != a) {
        b = indexes.get(random.nextInt(indexes.size()));
      }
      int c = -1;
      while (c != b) {
        c = indexes.get(random.nextInt(indexes.size()));
      }
      return new int[] {a, b, c};
    }

}
