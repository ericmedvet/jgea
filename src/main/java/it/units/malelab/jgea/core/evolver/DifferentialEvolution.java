/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core.evolver;

import com.google.common.base.Stopwatch;
import it.units.malelab.jgea.core.Individual;
import it.units.malelab.jgea.core.Problem;
import it.units.malelab.jgea.core.evolver.stopcondition.StopCondition;
import it.units.malelab.jgea.core.function.Bounded;
import it.units.malelab.jgea.core.function.CachedBoundedNonDeterministicFunction;
import it.units.malelab.jgea.core.function.CachedNonDeterministicFunction;
import it.units.malelab.jgea.core.function.NonDeterministicFunction;
import it.units.malelab.jgea.core.listener.Listener;
import it.units.malelab.jgea.core.listener.event.Capturer;
import it.units.malelab.jgea.core.listener.event.EvolutionEndEvent;
import it.units.malelab.jgea.core.listener.event.EvolutionEvent;
import it.units.malelab.jgea.core.listener.event.FunctionEvent;
import it.units.malelab.jgea.core.listener.event.TimedEvent;
import it.units.malelab.jgea.core.ranker.Ranker;
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
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author eric
 */
public class DifferentialEvolution<F> implements Evolver<double[], double[], F> {

  protected final int populationSize;
  protected final int offspringSize;
  protected final double crossoverRate;
  protected final double differentialWeight;
  protected final int size;
  protected final double initMean;
  protected final double initStandardDeviation;
  protected final Ranker<Individual<double[], double[], F>> ranker;
  protected final List<StopCondition> stopConditions;
  protected final long cacheSize;

  public DifferentialEvolution(
          int populationSize,
          int offspringSize,
          double crossoverRate,
          double differentialWeight,
          int size,
          double initMean,
          double initStandardDeviation,
          Ranker<Individual<double[], double[], F>> ranker,
          List<StopCondition> stopConditions,
          long cacheSize) {
    this.populationSize = populationSize;
    this.offspringSize = offspringSize;
    this.crossoverRate = crossoverRate;
    this.differentialWeight = differentialWeight;
    this.size = size;
    this.initMean = initMean;
    this.initStandardDeviation = initStandardDeviation;
    this.ranker = ranker;
    this.stopConditions = stopConditions;
    this.cacheSize = cacheSize;
  }

  @Override
  public Collection<double[]> solve(Problem<double[], F> problem, Random random, ExecutorService executor, Listener listener) throws InterruptedException, ExecutionException {
    int generations = 0;
    AtomicInteger births = new AtomicInteger();
    AtomicInteger fitnessEvaluations = new AtomicInteger();
    Stopwatch stopwatch = Stopwatch.createStarted();
    NonDeterministicFunction<double[], F> fitnessFunction = problem.getFitnessFunction();
    if (cacheSize > 0) {
      if (fitnessFunction instanceof Bounded) {
        fitnessFunction = new CachedBoundedNonDeterministicFunction<>(fitnessFunction, cacheSize);
      } else {
        fitnessFunction = fitnessFunction.cached(cacheSize);
      }
    }
    //init population
    List<Callable<Individual<double[], double[], F>>> tasks = new ArrayList<>();
    while (tasks.size() < populationSize) {
      double[] point = new double[size];
      for (int i = 0; i < point.length; i++) {
        point[i] = random.nextGaussian() * initStandardDeviation + initMean;
      }
      tasks.add(birthCallable(point, 0, fitnessFunction, random, listener));
    }
    births.addAndGet(populationSize);
    fitnessEvaluations.addAndGet(populationSize);
    List<Individual<double[], double[], F>> population = Misc.getAll(executor.invokeAll(tasks));
    //iterate
    while (true) {
      generations = generations + 1;
      //generate offsprings
      List<Individual<double[], double[], F>> firstParents = new ArrayList<>();
      tasks.clear();
      while (tasks.size() < offspringSize) {
        Collections.shuffle(population);
        double[] newPoint = new double[size];
        for (int i = 0; i < newPoint.length; i++) {
          double r = random.nextDouble();
          if (r < crossoverRate) {
            newPoint[i] = population.get(1).getGenotype()[i] + differentialWeight * (population.get(2).getGenotype()[i] - population.get(3).getGenotype()[i]);
          } else {
            newPoint[i] = population.get(0).getGenotype()[i];
          }
        }
        firstParents.add(population.get(0));
        tasks.add(birthCallable(newPoint, generations, fitnessFunction, random, listener));
        births.incrementAndGet();
        fitnessEvaluations.incrementAndGet();
      }
      List<Individual<double[], double[], F>> offspring = Misc.getAll(executor.invokeAll(tasks));
      //replace
      for (int i = 0; i<firstParents.size(); i++) {
        if (ranker.compare(firstParents.get(i), offspring.get(i), random)>0) {
          population.remove(firstParents.get(i));
          population.add(offspring.get(i));
        }
      }
      //send event
      List<Collection<Individual<double[], double[], F>>> rankedPopulation = ranker.rank(population, random);
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
    List<Collection<Individual<double[], double[], F>>> rankedPopulation = ranker.rank(population, random);
    Collection<double[]> solutions = new ArrayList<>();
    for (Individual<double[], double[], F> individual : rankedPopulation.get(0)) {
      solutions.add(individual.getSolution());
    }
    return solutions;
  }

  protected Callable<Individual<double[], double[], F>> birthCallable(
          final double[] point,
          final int birthIteration,
          final NonDeterministicFunction<double[], F> fitnessFunction,
          final Random random,
          final Listener listener) {
    return () -> {
      Stopwatch stopwatch = Stopwatch.createUnstarted();
      Capturer capturer = new Capturer();
      long elapsed;
      //solution -> fitness
      stopwatch.reset().start();
      F fitness = fitnessFunction.apply(point, random, capturer);
      elapsed = stopwatch.stop().elapsed(TimeUnit.NANOSECONDS);
      Map<String, Object> fitnessInfo = Misc.fromInfoEvents(capturer.getEvents(), "fitness.");
      listener.listen(new TimedEvent(elapsed, TimeUnit.NANOSECONDS, new FunctionEvent(point, fitness, fitnessInfo)));
      //merge info
      return new Individual<>(point, point, fitness, birthIteration, null, fitnessInfo);
    };
  }

  protected StopCondition checkStopConditions(EvolutionEvent event) {
    for (StopCondition stopCondition : stopConditions) {
      if (stopCondition.shouldStop(event)) {
        return stopCondition;
      }
    }
    return null;
  }

}
