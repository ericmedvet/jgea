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

    private static final Logger L = Logger.getLogger(DifferentialEvolution.class.getName());

    public DifferentialEvolution(
        Function<? super List<Double>, ? extends S> solutionMapper,
        Factory<? extends List<Double>> genotypeFactory,
        PartialComparator<? super Individual<List<Double>, S, F>> individualComparator,
        int populationSize,
        double differentialWeight,
        double crossoverProb) {
      super(solutionMapper, genotypeFactory, individualComparator);
      this.populationSize = populationSize;
      this.differentialWeight = differentialWeight;
      this.crossoverProb = crossoverProb;
    }

    @Override
    protected Collection<Individual<List<Double>, S, F>> initPopulation(Function<S, F> function, Random random, ExecutorService executorService, State state) throws ExecutionException, InterruptedException {
      return this.initPopulation(this.populationSize, function, random, executorService, state);
    }

    @Override
    protected Collection<Individual<List<Double>, S, F>> updatePopulation(PartiallyOrderedCollection<Individual<List<Double>, S, F>> population, Function<S, F> function, Random random, ExecutorService executorService, State state) throws ExecutionException, InterruptedException {
      List<Individual<List<Double>, S, F>> parents = new ArrayList<>(population.all());
      Collection<List<Double>> us = new ArrayList<>();
      for (Individual<List<Double>, S, F> parent : parents) {
        List<Double> x = parent.getGenotype();
        List<Double> u = new ArrayList<>(x.size());
        List<Individual<List<Double>, S, F>> abc = this.pickParents(parents, random);
        for (int j = 0; j < x.size(); ++j) {
          if (random.nextDouble() < this.crossoverProb) {
            u.add(abc.get(0).getGenotype().get(j) + this.differentialWeight * (abc.get(1).getGenotype().get(j) - abc.get(2).getGenotype().get(j)));
          } else {
            u.add(x.get(j));
          }
        }
        us.add(u);
      }
      L.fine(String.format("Trials computed: %d individuals", us.size()));
      Collection<Individual<List<Double>, S, F>> trials = map(us, List.of(), this.solutionMapper, function, executorService, state);
      L.fine(String.format("Trials evaluated: %d individuals", trials.size()));
      Collection<Individual<List<Double>, S, F>> offspring = selectPopulation(parents, trials);
      L.fine(String.format("Population selected: %d individuals", offspring.size()));
      return offspring;
    }

    protected List<Individual<List<Double>, S, F>> pickParents(List<Individual<List<Double>, S, F>> population, Random random) {
      List<Integer> indexes = IntStream.range(0, population.size()).boxed().collect(Collectors.toList());
      int a = indexes.get(random.nextInt(indexes.size()));
      int b = -1;
      while (b != a) {
        b = indexes.get(random.nextInt(indexes.size()));
      }
      int c = -1;
      while (c != b) {
        c = indexes.get(random.nextInt(indexes.size()));
      }
      return List.of(population.get(a), population.get(b), population.get(c));
    }

    protected Collection<Individual<List<Double>, S, F>> selectPopulation(Collection<Individual<List<Double>, S, F>> parents, Collection<Individual<List<Double>, S, F>> trials) {
      Collection<Individual<List<Double>, S, F>> offspring = new ArrayList<>();
      // we know by invariance that each parent is paired with a trial vector at the same index
      while (parents.iterator().hasNext() && trials.iterator().hasNext()) {
        Individual<List<Double>, S, F> trial = trials.iterator().next();
        Individual<List<Double>, S, F> parent = parents.iterator().next();
        if (this.individualComparator.compare(trial, parent).equals(PartialComparator.PartialComparatorOutcome.BEFORE)) {
          offspring.add(trial);
        } else {
          offspring.add(parent);
        }
      }
      return offspring;
    }

}
