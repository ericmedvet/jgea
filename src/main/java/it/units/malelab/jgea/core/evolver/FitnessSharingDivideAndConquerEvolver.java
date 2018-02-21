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
import it.units.malelab.jgea.core.function.BiFunction;
import it.units.malelab.jgea.core.function.Bounded;
import it.units.malelab.jgea.core.function.CachedBoundedFunction;
import it.units.malelab.jgea.core.function.ComposedFunction;
import it.units.malelab.jgea.core.function.Function;
import it.units.malelab.jgea.core.function.FunctionException;
import it.units.malelab.jgea.core.function.NonDeterministicFunction;
import it.units.malelab.jgea.core.listener.Listener;
import it.units.malelab.jgea.core.listener.event.EvolutionEndEvent;
import it.units.malelab.jgea.core.listener.event.EvolutionEvent;
import it.units.malelab.jgea.core.operator.GeneticOperator;
import it.units.malelab.jgea.core.ranker.Ranker;
import it.units.malelab.jgea.core.ranker.selector.Selector;
import it.units.malelab.jgea.core.util.Misc;
import it.units.malelab.jgea.core.util.Triplet;
import it.units.malelab.jgea.distance.Distance;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 *
 * @author eric
 */
public class FitnessSharingDivideAndConquerEvolver<G, S, F, B> extends StandardEvolver<G, S, F> {

  private class EnhancedIndividual extends Individual<G, S, F> {

    private S compositeSolution;
    private B compositeSemantics;
    private F compositeFitness;
    private final List<EnhancedIndividual> all;

    public EnhancedIndividual(G genotype, S solution, F fitness, B semantics, int birthIteration, Map<String, Object> info) {
      super(genotype, solution, fitness, birthIteration, Collections.EMPTY_LIST, info);
      compositeSolution = solution;
      compositeSemantics = semantics;
      compositeFitness = fitness;
      all = new ArrayList<EnhancedIndividual>();
      all.add(this);
    }

    public B getCompositeSemantics() {
      return compositeSemantics;
    }

    public void setCompositeSemantics(B compositeSemantics) {
      this.compositeSemantics = compositeSemantics;
    }

    public F getCompositeFitness() {
      return compositeFitness;
    }

    public void setCompositeFitness(F compositeFitness) {
      this.compositeFitness = compositeFitness;
    }

    public S getCompositeSolution() {
      return compositeSolution;
    }

    public void setCompositeSolution(S compositeSolution) {
      this.compositeSolution = compositeSolution;
    }

    public List<EnhancedIndividual> getAll() {
      return all;
    }

  }

  private final BiFunction<S, S, S> solutionReducer;
  private final BiFunction<B, B, B> semanticsReducer;
  private final Distance<B> distance;

  public FitnessSharingDivideAndConquerEvolver(BiFunction<S, S, S> solutionReducer, BiFunction<B, B, B> semanticsReducer, Distance<B> distance, int populationSize, Factory<G> genotypeBuilder, Ranker<Individual<G, S, F>> ranker, NonDeterministicFunction<G, S> mapper, Map<GeneticOperator<G>, Double> operators, Selector<Individual<G, S, F>> parentSelector, Selector<Individual<G, S, F>> unsurvivalSelector, int offspringSize, boolean overlapping, List<StopCondition> stoppingConditions, long cacheSize) {
    super(populationSize, genotypeBuilder, ranker, mapper, operators, parentSelector, unsurvivalSelector, offspringSize, overlapping, stoppingConditions, cacheSize, false);
    this.solutionReducer = solutionReducer;
    this.semanticsReducer = semanticsReducer;
    this.distance = distance;
  }

  @Override
  public Collection<S> solve(final Problem<S, F> problem, Random random, ExecutorService executor, Listener listener) throws InterruptedException, ExecutionException {
    List<Callable<Individual<G, S, B>>> tasks = new ArrayList<>();
    int births = 0;
    int fitnessEvaluations = 0;
    int generations = 0;
    Stopwatch stopwatch = Stopwatch.createStarted();
    final Function<S, B> semanticsFunction;
    final Function<? super B, ? extends F> fitnessFunction;
    if (cacheSize > 0) {
      semanticsFunction = ((ComposedFunction< S, B, F>) problem.getFitnessFunction()).first().cached(cacheSize);
      if (((ComposedFunction< S, B, F>) problem.getFitnessFunction()).second() instanceof Bounded) {
        fitnessFunction = (Function<? super B, ? extends F>) (new CachedBoundedFunction<>(((ComposedFunction< S, B, F>) problem.getFitnessFunction()).second(), cacheSize));
      } else {
        fitnessFunction = ((ComposedFunction< S, B, F>) problem.getFitnessFunction()).second().cached(cacheSize);
      }
    } else {
      semanticsFunction = ((ComposedFunction< S, B, F>) problem.getFitnessFunction()).first();
      fitnessFunction = ((ComposedFunction< S, B, F>) problem.getFitnessFunction()).second();
    }
    //initialize population
    List<Individual<G, S, B>> semanticsPopulation = new ArrayList<>();
    for (G genotype : genotypeBuilder.build(populationSize, random)) {
      tasks.add(new BirthCallable<>(
              genotype,
              generations,
              Collections.EMPTY_LIST,
              mapper,
              semanticsFunction,
              random,
              listener
      ));
    }
    semanticsPopulation.addAll(Misc.getAll(executor.invokeAll(tasks)));
    births = births + populationSize;
    fitnessEvaluations = fitnessEvaluations + populationSize;
    //iterate
    while (true) {
      generations = generations + 1;
      //associate individuals
      List<EnhancedIndividual> enhancedPopulation = semanticsPopulation.stream()
              .map(i -> new EnhancedIndividual(
                              i.getGenotype(),
                              i.getSolution(),
                              fitnessFunction.apply(i.getFitness()),
                              i.getFitness(),
                              i.getBirthIteration(),
                              i.getInfo()))
              .collect(Collectors.toList());
      //rank by fitness
      List<Collection<EnhancedIndividual>> rankedEnhancedPopulation = ranker.rank((Collection) enhancedPopulation, random);
      for (Collection<EnhancedIndividual> rank : rankedEnhancedPopulation) {
        for (EnhancedIndividual individual : rank) {
          //sort other by distance to this
          List<EnhancedIndividual> others = enhancedPopulation.stream()
                  .filter(i -> i.getAll().size()==1)
                  .sorted((i1, i2) -> {
                    double d1 = distance.apply(i1.getCompositeSemantics(), individual.getCompositeSemantics());
                    double d2 = distance.apply(i2.getCompositeSemantics(), individual.getCompositeSemantics());
                    return -Double.compare(d1, d2);
                  })
                  .collect(Collectors.toList());
          //iterate on others
          for (EnhancedIndividual other : others) {
            fitnessEvaluations = fitnessEvaluations + 1;
            S compositeSolution = solutionReducer.apply(individual.getCompositeSolution(), other.getCompositeSolution());
            B compositeSemantics = semanticsReducer.apply(individual.getCompositeSemantics(), other.getCompositeSemantics());
            F compositeFitness;
            if (semanticsReducer != null) {
              compositeFitness = fitnessFunction.apply(compositeSemantics, listener);
            } else {
              compositeFitness = fitnessFunction.apply(semanticsFunction.apply(compositeSolution, listener), listener);
            }
            //compare composed vs original
            if (ranker.compare(
                    new Individual<>(
                            individual.getGenotype(),
                            individual.getCompositeSolution(),
                            individual.getCompositeFitness(),
                            individual.getBirthIteration(),
                            Collections.EMPTY_LIST,
                            individual.getInfo()
                    ),
                    new Individual<>(
                            individual.getGenotype(),
                            compositeSolution,
                            compositeFitness,
                            individual.getBirthIteration(),
                            Collections.EMPTY_LIST,
                            individual.getInfo()
                    ),
                    random) == 1) {
              //update both individuals
              individual.setCompositeSolution(compositeSolution);
              individual.setCompositeSemantics(compositeSemantics);
              individual.setCompositeFitness(compositeFitness);
              for (EnhancedIndividual associate : individual.getAll()) {
                associate.getAll().add(other);
              }
              individual.getAll().add(other);
              for (EnhancedIndividual associate : individual.getAll()) {
                associate.setCompositeSolution(compositeSolution);
                associate.setCompositeSemantics(compositeSemantics);
                associate.setCompositeFitness(compositeFitness);               
              }
            }
          }
        }
      }

    }
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
          tasks.add(new BirthCallable<>(
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
    EvolutionEvent<G, S, F> event = new EvolutionEvent<>(
            generations,
            births,
            fitnessEvaluations(fitnessFunction, births),
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
  for (Individual<G, S, F> individual

  : rankedPopulation.get ( 
    0)) {
      solutions.add(individual.getSolution());
  }
  return solutions ;
}

}
