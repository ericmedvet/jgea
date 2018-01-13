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
import it.units.malelab.jgea.core.mapper.Mapper;
import it.units.malelab.jgea.core.util.Misc;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author eric
 */
public class BirthCallable<G, S, F> implements Callable<Individual<G, S, F>> {
  
  private final G genotype;
  private final double birthDate;
  private final List<Individual<G, S, F>> parents;
  private final Mapper<G, S> solutionMapper;
  private final Mapper<S, F> fitnessMapper;
  private final Random random;
  private final Listener listener;

  public BirthCallable(G genotype, double birthDate, List<Individual<G, S, F>> parents, Mapper<G, S> solutionMapper, Mapper<S, F> fitnessMapper, Random random, Listener listener) {
    this.genotype = genotype;
    this.birthDate = birthDate;
    this.parents = parents;
    this.solutionMapper = solutionMapper;
    this.fitnessMapper = fitnessMapper;
    this.random = random;
    this.listener = listener;
  }

  @Override
  public Individual<G, S, F> call() throws Exception {
    Stopwatch stopwatch = Stopwatch.createStarted();
    Capturer capturer = new Capturer();
    Map<String, Object> info = new LinkedHashMap<>();
    long elapsed;
    stopwatch.start();
    S solution = solutionMapper.map(genotype, random, capturer);
    elapsed = stopwatch.stop().elapsed(TimeUnit.NANOSECONDS);
    info.putAll(Misc.keyPrefix("solution.", ((MapperEvent)capturer.getLastEvent()).getInfo()));
    listener.listen(new TimedEvent(elapsed, TimeUnit.NANOSECONDS, capturer.getLastEvent()));    
    stopwatch.reset().start();
    F fitness = fitnessMapper.map(solution, random, capturer);
    elapsed = stopwatch.stop().elapsed(TimeUnit.NANOSECONDS);
    info.putAll(Misc.keyPrefix("fitness.", ((MapperEvent)capturer.getLastEvent()).getInfo()));
    listener.listen(new TimedEvent(elapsed, TimeUnit.NANOSECONDS, capturer.getLastEvent()));
    return new Individual<>(genotype, solution, fitness, birthDate, parents, info);
  }  
  
}
