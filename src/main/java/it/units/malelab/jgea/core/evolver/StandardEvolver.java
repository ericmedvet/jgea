/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core.evolver;

import it.units.malelab.jgea.core.Factory;
import it.units.malelab.jgea.core.Individual;
import it.units.malelab.jgea.core.Problem;
import it.units.malelab.jgea.core.listener.Listener;
import it.units.malelab.jgea.core.mapper.Mapper;
import it.units.malelab.jgea.core.ranker.Ranker;
import it.units.malelab.jgea.core.ranker.selector.Selector;
import it.units.malelab.jgea.core.util.Misc;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 *
 * @author eric
 */
public class StandardEvolver<G, S, F> implements Evolver<G, S, F> {

  private final int populationSize;
  private final long maxEvaluations;
  private final Factory<G> genotypeBuilder;
  private final Ranker<Individual<G, S, F>> ranker;
  private final Mapper<G, S> mapper;
  private final Map<Mapper<List<G>, List<G>>, Double> operators;
  private final Selector<Individual<G, S, F>> parentSelector;
  private final Selector<Individual<G, S, F>> unsurvivalSelector;
  private final int offspringSize;
  private final boolean overlapping;
  private final int numberOfGenerationWithoutImprovements;
  private final boolean saveAncestry;

  public StandardEvolver(int populationSize, long maxEvaluations, Factory<G> genotypeBuilder, Ranker<Individual<G, S, F>> ranker, Mapper<G, S> mapper, Map<Mapper<List<G>, List<G>>, Double> operators, Selector<Individual<G, S, F>> parentSelector, Selector<Individual<G, S, F>> unsurvivalSelector, int offspringSize, boolean overlapping, int numberOfGenerationWithoutImprovements, boolean saveAncestry) {
    this.populationSize = populationSize;
    this.maxEvaluations = maxEvaluations;
    this.genotypeBuilder = genotypeBuilder;
    this.ranker = ranker;
    this.mapper = mapper;
    this.operators = operators;
    this.parentSelector = parentSelector;
    this.unsurvivalSelector = unsurvivalSelector;
    this.offspringSize = offspringSize;
    this.overlapping = overlapping;
    this.numberOfGenerationWithoutImprovements = numberOfGenerationWithoutImprovements;
    this.saveAncestry = saveAncestry;
  }
  
  @Override
  public Collection<S> solve(Problem<S, F> problem, Random random, ExecutorService executor, Listener listener) throws InterruptedException, ExecutionException {
    List<Callable<Individual<G, S, F>>> birthTasks = new ArrayList<>();
    int births = 0;
    //initialize population
    List<Individual<G, S, F>> population = new ArrayList<>();
    for (G genotype : genotypeBuilder.build(populationSize, random)) {
      birthTasks.add(new BirthCallable<>(genotype, 0, Collections.EMPTY_LIST, mapper, problem.getFitnessMapper(), random, listener));
    }
    population.addAll(Misc.getAll(executor.invokeAll(birthTasks)));
    //take out solutions
    List<Collection<Individual<G, S, F>>> rankedPopulation = ranker.rank(population, random);
    Collection<S> solutions = new ArrayList<>();
    for (Individual<G, S, F> individual : rankedPopulation.get(0)) {
      solutions.add(individual.getSolution());
    }
    return solutions;
  }
  
}
