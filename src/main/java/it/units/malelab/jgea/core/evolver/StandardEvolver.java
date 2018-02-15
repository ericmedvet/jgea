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
import it.units.malelab.jgea.core.mapper.BoundMapper;
import it.units.malelab.jgea.core.mapper.CachedMapper;
import it.units.malelab.jgea.core.mapper.DeterministicMapper;
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
import java.util.concurrent.TimeUnit;

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
  private final List<StopCondition> stopConditions;
  private final boolean saveAncestry;
  private final long cacheSize;

  public StandardEvolver(int populationSize, Factory<G> genotypeBuilder, Ranker<Individual<G, S, F>> ranker, Mapper<G, S> mapper, Map<GeneticOperator<G>, Double> operators, Selector<Individual<G, S, F>> parentSelector, Selector<Individual<G, S, F>> unsurvivalSelector, int offspringSize, boolean overlapping, List<StopCondition> stoppingConditions, long cacheSize, boolean saveAncestry) {
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
    BoundMapper<S, F> fitnessMapper = buildFitnessMapper(problem);
    //initialize population
    List<Individual<G, S, F>> population = new ArrayList<>();
    for (G genotype : genotypeBuilder.build(populationSize, random)) {
      if (genotype==null) {
        System.out.println("NULL GENO");
      }
      tasks.add(new BirthCallable<>(
              genotype,
              generations,
              Collections.EMPTY_LIST,
              mapper,
              fitnessMapper,
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
          List<G> childGenotypes = operator.map(parentGenotypes, random, listener);
          for (G childGenotype : childGenotypes) {
            tasks.add(new BirthCallable<>(
                    childGenotype,
                    generations,
                    saveAncestry ? parents : null,
                    mapper,
                    fitnessMapper,
                    random,
                    listener
            ));
          }
          births = births + childGenotypes.size();
          i = i + childGenotypes.size();
        } catch (MappingException ex) {
          //just ignore: will not be added to the population
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
      EvolutionEvent<G, S, F> event = new EvolutionEvent<>(
              generations,
              births,
              fitnessEvaluations(fitnessMapper, births),
              rankedPopulation,
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
                fitnessEvaluations(fitnessMapper, births),
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

  protected long fitnessEvaluations(Mapper<S, F> fitnessMapper, int births) {
    return (fitnessMapper instanceof CachedMapper) ? ((CachedMapper) fitnessMapper).getActualCount() : births;
  }

  protected BoundMapper<S, F> buildFitnessMapper(final Problem<S, F> problem) {
    if ((cacheSize > 0) && (problem.getFitnessMapper() instanceof DeterministicMapper)) {
      return new CachedBoundMapper<>((DeterministicMapper) problem.getFitnessMapper(), cacheSize);
    }
    return problem.getFitnessMapper();
  }

  protected StopCondition checkStopConditions(EvolutionEvent<G, S, F> event) {
    for (StopCondition stopCondition : stopConditions) {
      if (stopCondition.shouldStop(event)) {
        return stopCondition;
      }
    }
    return null;
  }
  
  private class CachedBoundMapper<S, F> extends CachedMapper<S, F> implements BoundMapper<S, F> {
    
    private final BoundMapper<S, F> boundInnerMapper;

    public CachedBoundMapper(DeterministicMapper<S, F> innerMapper, long cacheSize) {
      super(innerMapper, cacheSize);
      boundInnerMapper = (BoundMapper<S, F>)innerMapper;
    }

    @Override
    public F worstValue() {
      return boundInnerMapper.worstValue();
    }

    @Override
    public F bestValue() {
      return boundInnerMapper.bestValue();
    }
    
  }

}
