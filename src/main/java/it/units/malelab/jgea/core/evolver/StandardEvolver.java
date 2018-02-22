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
import it.units.malelab.jgea.core.evolver.stopcondition.StopCondition;
import it.units.malelab.jgea.core.listener.Listener;
import it.units.malelab.jgea.core.listener.event.EvolutionEndEvent;
import it.units.malelab.jgea.core.listener.event.EvolutionEvent;
import it.units.malelab.jgea.core.function.CachedNonDeterministicFunction;
import it.units.malelab.jgea.core.function.FunctionException;
import it.units.malelab.jgea.core.function.NonDeterministicFunction;
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
import java.util.concurrent.TimeUnit;
import it.units.malelab.jgea.core.function.Bounded;
import it.units.malelab.jgea.core.function.CachedBoundedFunction;
import it.units.malelab.jgea.core.listener.event.Capturer;
import it.units.malelab.jgea.core.listener.event.FunctionEvent;
import it.units.malelab.jgea.core.listener.event.TimedEvent;

/**
 *
 * @author eric
 */
public class StandardEvolver<G, S, F> implements Evolver<G, S, F> {

  protected final int populationSize;
  protected final Factory<G> genotypeBuilder;
  protected final Ranker<Individual<G, S, F>> ranker;
  protected final NonDeterministicFunction<G, S> mapper;
  protected final Map<GeneticOperator<G>, Double> operators;
  protected final Selector<Individual<G, S, F>> parentSelector;
  protected final Selector<Individual<G, S, F>> unsurvivalSelector;
  protected final int offspringSize;
  protected final boolean overlapping;
  protected final List<StopCondition> stopConditions;
  protected final boolean saveAncestry;
  protected final long cacheSize;

  public StandardEvolver(int populationSize, Factory<G> genotypeBuilder, Ranker<Individual<G, S, F>> ranker, NonDeterministicFunction<G, S> mapper, Map<GeneticOperator<G>, Double> operators, Selector<Individual<G, S, F>> parentSelector, Selector<Individual<G, S, F>> unsurvivalSelector, int offspringSize, boolean overlapping, List<StopCondition> stoppingConditions, long cacheSize, boolean saveAncestry) {
    this.populationSize = populationSize;
    this.genotypeBuilder = genotypeBuilder;
    this.ranker = ranker;
    this.mapper = mapper;
    this.operators = operators;
    this.parentSelector = parentSelector;
    this.unsurvivalSelector = unsurvivalSelector;
    this.offspringSize = offspringSize;
    this.overlapping = overlapping;
    this.stopConditions = stoppingConditions;
    this.cacheSize = cacheSize;
    this.saveAncestry = saveAncestry;
  }

  @Override
  public Collection<S> solve(final Problem<S, F> problem, Random random, ExecutorService executor, Listener listener) throws InterruptedException, ExecutionException {
    List<Callable<Individual<G, S, F>>> tasks = new ArrayList<>();
    int births = 0;
    int generations = 0;
    Stopwatch stopwatch = Stopwatch.createStarted();
    NonDeterministicFunction<S, F> fitnessFunction = problem.getFitnessFunction();
    if (cacheSize > 0) {
      if (fitnessFunction instanceof Bounded) {
        fitnessFunction = new CachedBoundedFunction<>(fitnessFunction, cacheSize);
      } else {
        fitnessFunction = fitnessFunction.cached(cacheSize);
      }
    }
    //initialize population
    List<Individual<G, S, F>> population = new ArrayList<>();
    for (G genotype : genotypeBuilder.build(populationSize, random)) {
      tasks.add(birthCallable(
              genotype,
              generations,
              Collections.EMPTY_LIST,
              mapper,
              fitnessFunction,
              random,
              listener
      ));
    }
    population.addAll(Misc.getAll(executor.invokeAll(tasks)));
    births = births + populationSize;
    //iterate
    while (true) {
      generations = generations + 1;
      //re-rank
      List<Collection<Individual<G, S, F>>> rankedPopulation = ranker.rank(population, random);
      //build offsprings
      int i = 0;
      tasks.clear();
      while (i < offspringSize) {
        GeneticOperator<G> operator = Misc.pickRandomly(operators, random);
        List<Individual<G, S, F>> parents = new ArrayList<>(operator.arity());
        List<G> parentGenotypes = new ArrayList<>(operator.arity());
        for (int j = 0; j < operator.arity(); j++) {
          Individual<G, S, F> parent = parentSelector.select(rankedPopulation, random);
          parents.add(parent);
          parentGenotypes.add(parent.getGenotype());
        }
        try {
          List<G> childGenotypes = operator.apply(parentGenotypes, random, listener);
          for (G childGenotype : childGenotypes) {
            tasks.add(birthCallable(
                    childGenotype,
                    generations,
                    saveAncestry ? parents : null,
                    mapper,
                    fitnessFunction,
                    random,
                    listener
            ));
          }
          births = births + childGenotypes.size();
          i = i + childGenotypes.size();
        } catch (FunctionException ex) {
          //just ignore: will not be added to the population
        }
      }
      //update population
      List<Individual<G, S, F>> newPopulation = Misc.getAll(executor.invokeAll(tasks));
      population = updatePopulation(population, newPopulation, rankedPopulation, random);
      //select survivals
      while (population.size() > populationSize) {
        //re-rank
        rankedPopulation = ranker.rank(population, random);
        Individual<G, S, F> individual = unsurvivalSelector.select(rankedPopulation, random);
        population.remove(individual);
      }
      EvolutionEvent event = new EvolutionEvent(
              generations,
              births,
              fitnessEvaluations(fitnessFunction, births),
              (List)rankedPopulation,
              stopwatch.elapsed(TimeUnit.MILLISECONDS)
      );
      listener.listen(event);
      //check stopping conditions
      StopCondition stopCondition = checkStopConditions(event);
      if (stopCondition != null) {
        listener.listen(new EvolutionEndEvent(
                stopCondition,
                generations,
                births,
                fitnessEvaluations(fitnessFunction, births),
                rankedPopulation,
                stopwatch.elapsed(TimeUnit.MILLISECONDS))
        );
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

  protected <I extends Individual<G, S, F>> List<I> updatePopulation(List<I> population, List<I> newPopulation, List<Collection<I>> rankedPopulation, Random random) {
    if (overlapping) {
      population.addAll(newPopulation);
    } else {
      if (newPopulation.size() >= populationSize) {
        population = newPopulation;
      } else {
        //keep missing individuals from old population
        int targetSize = population.size() - newPopulation.size();
        while (population.size() > targetSize) {
          I individual = unsurvivalSelector.select(rankedPopulation, random);
          population.remove(individual);
        }
        population.addAll(newPopulation);
      }
    }
    return population;
  }

  protected long fitnessEvaluations(NonDeterministicFunction fitnessFunction, int births) {
    return (fitnessFunction instanceof CachedNonDeterministicFunction) ? ((CachedNonDeterministicFunction) fitnessFunction).getActualCount() : births;
  }

  protected StopCondition checkStopConditions(EvolutionEvent event) {
    for (StopCondition stopCondition : stopConditions) {
      if (stopCondition.shouldStop(event)) {
        return stopCondition;
      }
    }
    return null;
  }

  protected Callable<Individual<G, S, F>> birthCallable(G genotype, int birthIteration, List<Individual<G, S, F>> parents, NonDeterministicFunction<G, S> solutionFunction, NonDeterministicFunction<S, F> fitnessFunction, Random random, Listener listener) {
    return () -> {
      Stopwatch stopwatch = Stopwatch.createUnstarted();
      Capturer capturer = new Capturer();
      long elapsed;
      //genotype -> solution
      stopwatch.start();
      S solution = null;
      try {
        solution = solutionFunction.apply(genotype, random, capturer);
      } catch (FunctionException ex) {
        //invalid solution
        //TODO log to listener
      }
      elapsed = stopwatch.stop().elapsed(TimeUnit.NANOSECONDS);
      Map<String, Object> solutionInfo = Misc.fromInfoEvents(capturer.getEvents(), "solution.");
      listener.listen(new TimedEvent(elapsed, TimeUnit.NANOSECONDS, new FunctionEvent(genotype, solution, solutionInfo)));
      capturer.clear();
      //solution -> fitness
      stopwatch.reset().start();
      F fitness = null;
      if (solution != null) {
        fitness = fitnessFunction.apply(solution, random, capturer);
      } else {
        if (fitnessFunction instanceof Bounded) {
          fitness = ((Bounded<F>) fitnessFunction).worstValue();
        }
      }
      elapsed = stopwatch.stop().elapsed(TimeUnit.NANOSECONDS);
      Map<String, Object> fitnessInfo = Misc.fromInfoEvents(capturer.getEvents(), "fitness.");
      listener.listen(new TimedEvent(elapsed, TimeUnit.NANOSECONDS, new FunctionEvent(genotype, solution, fitnessInfo)));
      //merge info
      return new Individual<>(genotype, solution, fitness, birthIteration, parents, Misc.merge(solutionInfo, fitnessInfo));
    };
  }

}
