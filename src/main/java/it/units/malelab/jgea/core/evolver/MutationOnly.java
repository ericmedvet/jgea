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
import it.units.malelab.jgea.core.function.Bounded;
import it.units.malelab.jgea.core.function.CachedBoundedNonDeterministicFunction;
import it.units.malelab.jgea.core.function.CachedNonDeterministicFunction;
import it.units.malelab.jgea.core.function.NonDeterministicFunction;
import it.units.malelab.jgea.core.listener.Listener;
import it.units.malelab.jgea.core.listener.event.EvolutionEndEvent;
import it.units.malelab.jgea.core.listener.event.EvolutionEvent;
import it.units.malelab.jgea.core.operator.AbstractMutation;
import it.units.malelab.jgea.core.operator.GeneticOperator;
import it.units.malelab.jgea.core.ranker.Ranker;
import it.units.malelab.jgea.core.ranker.selector.Selector;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author Eric Medvet <eric.medvet@gmail.com>
 */
public class MutationOnly<G, S, F> extends StandardEvolver<G, S, F> {

  protected final AbstractMutation<G> mutation;

  public MutationOnly(
          int populationSize,
          Factory<G> genotypeBuilder,
          Ranker<Individual<G, S, F>> ranker,
          NonDeterministicFunction<G, S> mapper,
          AbstractMutation<G> mutation,
          List<StopCondition> stoppingConditions,
          long cacheSize,
          boolean saveAncestry
  ) {
    super(populationSize, genotypeBuilder, ranker, mapper, null, null, null, 0, false, stoppingConditions, cacheSize, saveAncestry);
    this.mutation = mutation;
  }

  @Override
  public Collection<S> solve(Problem<S, F> problem, Random random, ExecutorService executor, Listener listener) throws InterruptedException, ExecutionException {
    int generations = 0;
    AtomicInteger births = new AtomicInteger();
    AtomicInteger fitnessEvaluations = new AtomicInteger();
    Stopwatch stopwatch = Stopwatch.createStarted();
    NonDeterministicFunction<S, F> fitnessFunction = problem.getFitnessFunction();
    if (cacheSize > 0) {
      if (fitnessFunction instanceof Bounded) {
        fitnessFunction = new CachedBoundedNonDeterministicFunction<>(fitnessFunction, cacheSize);
      } else {
        fitnessFunction = fitnessFunction.cached(cacheSize);
      }
    }
    //initialize population
    List<Individual<G, S, F>> population = initPopulation(fitnessFunction, births, fitnessEvaluations, random, listener, executor);
    //iterate
    while (true) {
      generations = generations + 1;
      //mutate al individuals
      List<Callable<Individual<G, S, F>>> tasks = new ArrayList<>();
      for (Individual<G, S, F> parent : population) {
        
        //TODO
        
      }
      List<Individual<G, S, F>> newPopulation = new ArrayList<>(populationSize+1);
      
      //TODO
      
      //send event
      List<Collection<Individual<G, S, F>>> rankedPopulation = ranker.rank(population, random);
      EvolutionEvent event = new EvolutionEvent(
              generations,
              births.get(),
              (fitnessFunction instanceof CachedNonDeterministicFunction) ? ((CachedNonDeterministicFunction) fitnessFunction).getActualCount() : fitnessEvaluations.get(),
              (List) rankedPopulation,
              stopwatch.elapsed(TimeUnit.MILLISECONDS)
      );
      listener.listen(event);
      //check stopping conditions
      StopCondition stopCondition = checkStopConditions(event);
      if (stopCondition != null) {
        listener.listen(new EvolutionEndEvent(
                stopCondition,
                generations,
                births.get(),
                (fitnessFunction instanceof CachedNonDeterministicFunction) ? ((CachedNonDeterministicFunction) fitnessFunction).getActualCount() : fitnessEvaluations.get(),
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

}
