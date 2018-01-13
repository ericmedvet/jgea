/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core.evolver;

import com.google.common.base.Stopwatch;
import it.units.malelab.jgea.core.Factory;
import it.units.malelab.jgea.core.Individual;
import it.units.malelab.jgea.core.Problem;
import it.units.malelab.jgea.core.evolver.stopcriterion.Births;
import it.units.malelab.jgea.core.evolver.stopcriterion.ElapsedTime;
import it.units.malelab.jgea.core.evolver.stopcriterion.FitnessEvaluations;
import it.units.malelab.jgea.core.evolver.stopcriterion.Iterations;
import it.units.malelab.jgea.core.evolver.stopcriterion.RelativeElapsedTime;
import it.units.malelab.jgea.core.evolver.stopcriterion.StoppingCondition;
import it.units.malelab.jgea.core.listener.Listener;
import it.units.malelab.jgea.core.mapper.CachedMapper;
import it.units.malelab.jgea.core.mapper.Mapper;
import it.units.malelab.jgea.core.mapper.MappingException;
import it.units.malelab.jgea.core.operator.GeneticOperator;
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
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author eric
 */
public class StandardEvolver<G, S, F> implements Evolver<G, S, F> {

  private final int populationSize;
  private final Factory<G> genotypeBuilder;
  private final Ranker<Individual<G, S, F>> ranker;
  private final Mapper<G, S> mapper;
  private final Map<GeneticOperator<G>, Double> operators;
  private final Selector<Individual<G, S, F>> parentSelector;
  private final Selector<Individual<G, S, F>> unsurvivalSelector;
  private final int offspringSize;
  private final boolean overlapping;
  private final List<StoppingCondition> stoppingConditions;
  private final boolean saveAncestry;

  public StandardEvolver(int populationSize, Factory<G> genotypeBuilder, Ranker<Individual<G, S, F>> ranker, Mapper<G, S> mapper, Map<GeneticOperator<G>, Double> operators, Selector<Individual<G, S, F>> parentSelector, Selector<Individual<G, S, F>> unsurvivalSelector, int offspringSize, boolean overlapping, List<StoppingCondition> stoppingConditions, boolean saveAncestry) {
    this.populationSize = populationSize;
    this.genotypeBuilder = genotypeBuilder;
    this.ranker = ranker;
    this.mapper = mapper;
    this.operators = operators;
    this.parentSelector = parentSelector;
    this.unsurvivalSelector = unsurvivalSelector;
    this.offspringSize = offspringSize;
    this.overlapping = overlapping;
    this.stoppingConditions = stoppingConditions;
    this.saveAncestry = saveAncestry;
  }
  
  @Override
  public Collection<S> solve(Problem<S, F> problem, Random random, ExecutorService executor, Listener listener) throws InterruptedException, ExecutionException {
    List<Callable<Individual<G, S, F>>> tasks = new ArrayList<>();
    int births = 0;
    int generations = 0;
    Stopwatch stopwatch = Stopwatch.createStarted();
    //initialize population
    List<Individual<G, S, F>> population = new ArrayList<>();
    for (G genotype : genotypeBuilder.build(populationSize, random)) {
      tasks.add(new BirthCallable<>(genotype, 0, Collections.EMPTY_LIST, mapper, problem.getFitnessMapper(), random, listener));
    }
    population.addAll(Misc.getAll(executor.invokeAll(tasks)));
    births = births+populationSize;
    //iterate
    while (true) {
      generations = generations+1;
      //re-rank
      List<Collection<Individual<G, S, F>>> rankedPopulation = ranker.rank(population, random);
      //build offsprings
      int i = 0;
      tasks.clear();
      while (i<offspringSize) {
        GeneticOperator<G> operator = Misc.selectRandom(operators, random);
        List<Individual<G, S, F>> parents = new ArrayList<>(operator.arity());
        List<G> parentGenotypes = new ArrayList<>(operator.arity());
        for (int j = 0; j < operator.arity(); j++) {
          Individual<G, S, F> parent = parentSelector.select(rankedPopulation, random);
          parents.add(parent);
          parentGenotypes.add(parent.getGenotype());
        }
        try {
          List<G> childGenotypes = operator.map(parentGenotypes, random, listener);
          for (G childGenotype : childGenotypes) {
            tasks.add(new BirthCallable<>(childGenotype, births, parents, mapper, problem.getFitnessMapper(), random, listener));
          }
          births = births+childGenotypes.size();
          i = i+childGenotypes.size();          
        } catch (MappingException ex) {
          //just ignore
        }
      }
      //update population
      List<Individual<G, S, F>> newPopulation = Misc.getAll(executor.invokeAll(tasks));
      if (overlapping) {
        population.addAll(newPopulation);
      } else {
        if (newPopulation.size() >= populationSize) {
          population = newPopulation;
        } else {
          //keep missing individuals from old population
          int targetSize = population.size() - newPopulation.size();
          while (population.size() > targetSize) {
            Individual<G, S, F> individual = unsurvivalSelector.select(rankedPopulation, random);
            population.remove(individual);
          }
          population.addAll(newPopulation);
        }
      }
      //select survivals
      while (population.size() > populationSize) {
        //re-rank
        rankedPopulation = ranker.rank(population, random);
        Individual<G, S, F> individual = unsurvivalSelector.select(rankedPopulation, random);
        population.remove(individual);
      }
      //check stopping conditions
      if (checkStoppingConditions(generations, births, stopwatch, problem.getFitnessMapper())!=null) {
        break;
      }
    }        
    //take out solutions
    List<Collection<Individual<G, S, F>>> rankedPopulation = ranker.rank(population, random);
    Collection<S> solutions = new ArrayList<>();
    for (Individual<G, S, F> individual : rankedPopulation.get(0)) {
      solutions.add(individual.getSolution());
    }
    return solutions;
  }
  
  protected StoppingCondition checkStoppingConditions(int iterations, int births, Stopwatch stopwatch, Mapper<S, F> fitnessMapper) {
    for (StoppingCondition stoppingCondition : stoppingConditions) {
      if (stoppingCondition.getClass().equals(Births.class)) {
        if (births>((Births)stoppingCondition).getN()) {
          return stoppingCondition;
        }
      } else if (stoppingCondition.getClass().equals(ElapsedTime.class)) {
        if (stopwatch.elapsed(((ElapsedTime)stoppingCondition).getTimeUnit())>((ElapsedTime)stoppingCondition).getT()) {
          return stoppingCondition;
        }        
      } else if (stoppingCondition.getClass().equals(RelativeElapsedTime.class)) {
        if (fitnessMapper instanceof CachedMapper) {
          double avgFitnessEvaluationNanos = ((CachedMapper)fitnessMapper).getCacheStats().averageLoadPenalty();
          double elapsedNanos = stopwatch.elapsed(TimeUnit.NANOSECONDS);
          if ((elapsedNanos/avgFitnessEvaluationNanos)>((RelativeElapsedTime)stoppingCondition).getR()) {
            return stoppingCondition;
          }          
        }
      } else if (stoppingCondition.getClass().equals(Iterations.class)) {
        if (iterations>((Iterations)stoppingCondition).getN()) {
          return stoppingCondition;
        }        
      } else if (stoppingCondition.getClass().equals(FitnessEvaluations.class)) {
        if (fitnessMapper instanceof CachedMapper) {
          long actualEvaluations = ((CachedMapper)fitnessMapper).getActualCount();
          if (actualEvaluations>((FitnessEvaluations)stoppingCondition).getN()) {
            return stoppingCondition;
          }          
        }        
      }
    }
    return null;
  }
  
}
