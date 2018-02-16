/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core.evolver;

import com.google.common.base.Stopwatch;
import it.units.malelab.jgea.core.Individual;
import it.units.malelab.jgea.core.listener.Listener;
import it.units.malelab.jgea.core.listener.event.Capturer;
import it.units.malelab.jgea.core.listener.event.MapperEvent;
import it.units.malelab.jgea.core.listener.event.TimedEvent;
import it.units.malelab.jgea.core.function.FunctionException;
import it.units.malelab.jgea.core.function.NonDeterministicFunction;
import it.units.malelab.jgea.core.util.Misc;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import it.units.malelab.jgea.core.function.Bounded;

/**
 *
 * @author eric
 */
public class BirthCallable<G, S, F> implements Callable<Individual<G, S, F>> {
  
  private final G genotype;
  private final int birthIteration;
  private final List<Individual<G, S, F>> parents;
  private final NonDeterministicFunction<G, S> solutionMapper;
  private final NonDeterministicFunction<S, F> fitnessFunction;
  private final Random random;
  private final Listener listener;

  public BirthCallable(G genotype, int birthIteration, List<Individual<G, S, F>> parents, NonDeterministicFunction<G, S> solutionMapper, NonDeterministicFunction<S, F> fitnessMapper, Random random, Listener listener) {
    this.genotype = genotype;
    this.birthIteration = birthIteration;
    this.parents = parents;
    this.solutionMapper = solutionMapper;
    this.fitnessFunction = fitnessMapper;
    this.random = random;
    this.listener = listener;
  }
  
  @Override
  public Individual<G, S, F> call() throws Exception {
    Stopwatch stopwatch = Stopwatch.createUnstarted();
    Capturer capturer = new Capturer();
    long elapsed;
    //genotype -> solution
    stopwatch.start();
    S solution = null;
    try {
      solution = solutionMapper.apply(genotype, random, capturer);
    } catch (FunctionException ex) {
      //invalid solution
      //TODO log to listener
    }
    elapsed = stopwatch.stop().elapsed(TimeUnit.NANOSECONDS);
    Map<String, Object> solutionInfo = Misc.fromInfoEvents(capturer.getEvents(), "solution.");
    listener.listen(new TimedEvent(elapsed, TimeUnit.NANOSECONDS, new MapperEvent(genotype, solution, solutionInfo)));
    capturer.clear();
    //solution -> fitness
    stopwatch.reset().start();
    F fitness = null;
    if (solution!=null) {
      fitness = fitnessFunction.apply(solution, random, capturer);
    } else {
      if (fitness instanceof Bounded) {
        fitness = ((Bounded<F>)fitness).worstValue();
      }
    }
    elapsed = stopwatch.stop().elapsed(TimeUnit.NANOSECONDS);
    Map<String, Object> fitnessInfo = Misc.fromInfoEvents(capturer.getEvents(), "fitness.");
    listener.listen(new TimedEvent(elapsed, TimeUnit.NANOSECONDS, new MapperEvent(genotype, solution, fitnessInfo)));
    //merge info
    return new Individual<>(genotype, solution, fitness, birthIteration, parents, Misc.merge(solutionInfo, fitnessInfo));
  }  
  
}
