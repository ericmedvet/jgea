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
import it.units.malelab.jgea.core.listener.event.Capturer;
import it.units.malelab.jgea.core.listener.event.EvolutionEndEvent;
import it.units.malelab.jgea.core.listener.event.EvolutionEvent;
import it.units.malelab.jgea.core.listener.event.FunctionEvent;
import it.units.malelab.jgea.core.listener.event.TimedEvent;
import it.units.malelab.jgea.core.operator.GeneticOperator;
import it.units.malelab.jgea.core.ranker.Ranker;
import it.units.malelab.jgea.core.ranker.selector.Selector;
import it.units.malelab.jgea.core.util.Misc;
import it.units.malelab.jgea.distance.Distance;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
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

    private final S originalSolution;
    private final B originalSemantics;
    private final F originalFitness;
    private B semantics;
    private final List<EnhancedIndividual> all;

    @SuppressWarnings("LeakingThisInConstructor")
    public EnhancedIndividual(G genotype, S solution, F fitness, B semantics, int birthIteration, Map<String, Object> info) {
      super(genotype, solution, fitness, birthIteration, Collections.EMPTY_LIST, info);
      this.semantics = semantics;
      originalSolution = solution;
      originalSemantics = semantics;
      originalFitness = fitness;
      all = new ArrayList<>();
      all.add(this);
    }

    public List<EnhancedIndividual> getAll() {
      return all;
    }

    public B getOriginalSemantics() {
      return originalSemantics;
    }

    public S getOriginalSolution() {
      return originalSolution;
    }

    public F getOriginalFitness() {
      return originalFitness;
    }

    public B getSemantics() {
      return semantics;
    }

    public void setSemantics(B semantics) {
      this.semantics = semantics;
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
    List<Callable<EnhancedIndividual>> tasks = new ArrayList<>();
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
    for (G genotype : genotypeBuilder.build(populationSize, random)) {
      tasks.add(birthCallable(genotype, generations, mapper, semanticsFunction, fitnessFunction, random, listener));
    }
    List<EnhancedIndividual> population = Misc.getAll(executor.invokeAll(tasks));
    births = births + populationSize;
    fitnessEvaluations = fitnessEvaluations + populationSize;
    //iterate
    while (true) {
      generations = generations + 1;
      //rank by fitness
      List<Collection<EnhancedIndividual>> rankedPopulation = ranker.rank(population, random);
      //associate individuals
      for (Collection<EnhancedIndividual> rank : rankedPopulation) {
        for (EnhancedIndividual individual : rank) {
          //sort other by distance to this
          List<EnhancedIndividual> others = rank.stream()
                  .filter(i -> i.getAll().size() == 1)
                  .sorted((i1, i2) -> {
                    double d1 = distance.apply(i1.getSemantics(), individual.getSemantics());
                    double d2 = distance.apply(i2.getSemantics(), individual.getSemantics());
                    return -Double.compare(d1, d2);
                  })
                  .collect(Collectors.toList());
          //iterate on others
          for (EnhancedIndividual other : others) {
            fitnessEvaluations = fitnessEvaluations + 1;
            S compositeSolution = solutionReducer.apply(individual.getSolution(), other.getSolution());
            B compositeSemantics = semanticsReducer.apply(individual.getSemantics(), other.getSemantics());
            F compositeFitness;
            if (semanticsReducer != null) {
              compositeFitness = fitnessFunction.apply(compositeSemantics, listener);
            } else {
              compositeFitness = fitnessFunction.apply(semanticsFunction.apply(compositeSolution, listener), listener);
            }
            //compare composed vs original
            if (ranker.compare(
                    new EnhancedIndividual(
                            individual.getGenotype(),
                            individual.getSolution(),
                            individual.getFitness(),
                            individual.getSemantics(),
                            individual.getBirthIteration(),
                            individual.getInfo()
                    ),
                    new EnhancedIndividual(
                            individual.getGenotype(),
                            compositeSolution,
                            compositeFitness,
                            compositeSemantics,
                            individual.getBirthIteration(),
                            individual.getInfo()
                    ),
                    random) == 1) {
              //update this individuals
              individual.setSolution(compositeSolution);
              individual.setSemantics(compositeSemantics);
              individual.setFitness(compositeFitness);
              individual.getAll().forEach((associate) -> {
                associate.getAll().add(other);
              });
              individual.getAll().add(other);
              for (EnhancedIndividual associate : individual.getAll()) {
                associate.setSolution(compositeSolution);
                associate.setSemantics(compositeSemantics);
                associate.setFitness(compositeFitness);
              }
            }
          }
        }
      }
      //rank by fitness
      rankedPopulation = ranker.rank(population, random);
      //build offsprings
      int i = 0;
      tasks.clear();
      while (i < offspringSize) {
        GeneticOperator<G> operator = Misc.pickRandomly(operators, random);
        List<EnhancedIndividual> parents = new ArrayList<>(operator.arity());
        List<G> parentGenotypes = new ArrayList<>(operator.arity());
        for (int j = 0; j < operator.arity(); j++) {
          EnhancedIndividual parent = parentSelector.select(rankedPopulation, random);
          parents.add(parent);
          parentGenotypes.add(parent.getGenotype());
        }
        try {
          List<G> childGenotypes = operator.apply(parentGenotypes, random, listener);
          for (G childGenotype : childGenotypes) {
            tasks.add(birthCallable(childGenotype, generations, mapper, semanticsFunction, fitnessFunction, random, listener));
          }
          births = births + childGenotypes.size();
          fitnessEvaluations = fitnessEvaluations + childGenotypes.size();
          i = i + childGenotypes.size();
        } catch (FunctionException ex) {
          //just ignore: will not be added to the population
        }
      }
      //update population
      List<EnhancedIndividual> newPopulation = Misc.getAll(executor.invokeAll(tasks));
      population = updatePopulation(population, newPopulation, rankedPopulation, random);
      //select survivals
      while (population.size() > populationSize) {
        //re-rank
        rankedPopulation = ranker.rank(population, random);
        EnhancedIndividual individual = unsurvivalSelector.select(rankedPopulation, random);
        population.remove(individual);
      }
      EvolutionEvent event = new EvolutionEvent(
              generations,
              births,
              fitnessEvaluations,
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
    List<Collection<EnhancedIndividual>> rankedPopulation = ranker.rank(population, random);
    Collection<S> solutions = new ArrayList<>();
    for (EnhancedIndividual individual : rankedPopulation.get(0)) {
      solutions.add(individual.getSolution());
    }
    return solutions;
  }

  private Callable<EnhancedIndividual> birthCallable(G genotype, int birthIteration, NonDeterministicFunction<G, S> solutionFunction, Function<S, B> semanticsFunction, Function<? super B, ? extends F> fitnessFunction, Random random, Listener listener) {
    return () -> {
      Stopwatch stopwatch = Stopwatch.createUnstarted();
      Capturer capturer = new Capturer();
      Map<String, Object> info = new LinkedHashMap<>();
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
      info.putAll(solutionInfo);
      listener.listen(new TimedEvent(elapsed, TimeUnit.NANOSECONDS, new FunctionEvent(genotype, solution, solutionInfo)));
      capturer.clear();
      //solution -> semantics and semantics -> fitness
      stopwatch.reset().start();
      B semantics = null;
      F fitness = null;
      Map<String, Object> fitnessInfo;
      if (solution != null) {
        semantics = semanticsFunction.apply(solution, random, capturer);
        info.putAll(Misc.fromInfoEvents(capturer.getEvents(), "semantics."));
        capturer.clear();
        fitness = fitnessFunction.apply(semantics, random, capturer);
        fitnessInfo = Misc.fromInfoEvents(capturer.getEvents(), "fitness.");
        info.putAll(fitnessInfo);
      } else {
        if (fitnessFunction instanceof Bounded) {
          fitness = ((Bounded<F>) fitness).worstValue();
        }
        fitnessInfo = Collections.EMPTY_MAP;
      }
      elapsed = stopwatch.stop().elapsed(TimeUnit.NANOSECONDS);
      listener.listen(new TimedEvent(elapsed, TimeUnit.NANOSECONDS, new FunctionEvent(genotype, solution, fitnessInfo)));
      //merge info
      return new EnhancedIndividual(genotype, solution, fitness, semantics, birthIteration, info);
    };
  }

}
