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
import it.units.malelab.jgea.core.function.CachedBoundedFunction;
import it.units.malelab.jgea.core.function.CachedNonDeterministicFunction;
import it.units.malelab.jgea.core.function.ComposedFunction;
import it.units.malelab.jgea.core.function.Function;
import it.units.malelab.jgea.core.function.FunctionException;
import it.units.malelab.jgea.core.function.NonDeterministicFunction;
import it.units.malelab.jgea.core.function.Reducer;
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
import it.units.malelab.jgea.core.util.Pair;
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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 *
 * @author eric
 */
public class FitnessSharingDivideAndConquerEvolver<G, S, F, B> extends StandardEvolver<G, S, F> {

  private class EnhancedIndividual extends Individual<G, S, F> {

    private final B semantics;
    private S groupSolution;
    private B groupSemantics;
    private F groupFitness;
    private final List<EnhancedIndividual> group;

    @SuppressWarnings("LeakingThisInConstructor")
    public EnhancedIndividual(G genotype, S solution, B semantics, F fitness, int birthIteration, List<Individual<G, S, F>> parents, Map<String, Object> info) {
      super(genotype, solution, fitness, birthIteration, parents, info);
      this.semantics = semantics;
      group = new ArrayList<>();
      reset();
    }

    public void reset() {
      group.clear();
      group.add(this);
      groupSolution = super.getSolution();
      groupSemantics = semantics;
      groupFitness = super.getFitness();
    }

    public List<EnhancedIndividual> getGroup() {
      return group;
    }

    @Override
    public F getFitness() {
      return groupFitness;
    }

    public B getSemantics() {
      return groupSemantics;
    }

    @Override
    public S getSolution() {
      return groupSolution;
    }

    public void setGroupSolution(S groupSolution) {
      this.groupSolution = groupSolution;
    }

    public void setGroupSemantics(B groupSemantics) {
      this.groupSemantics = groupSemantics;
    }

    public void setGroupFitness(F groupFitness) {
      this.groupFitness = groupFitness;
    }

  }

  private final Reducer<Pair<S, B>> reducer;
  private final Distance<B> distance;

  public FitnessSharingDivideAndConquerEvolver(Reducer<Pair<S, B>> reducer, Distance<B> semanticsDistance, int populationSize, Factory<G> genotypeBuilder, Ranker<Individual<G, S, F>> ranker, NonDeterministicFunction<G, S> mapper, Map<GeneticOperator<G>, Double> operators, Selector<Individual<G, S, F>> parentSelector, Selector<Individual<G, S, F>> unsurvivalSelector, int offspringSize, boolean overlapping, List<StopCondition> stoppingConditions, long cacheSize) {
    super(populationSize, genotypeBuilder, ranker, mapper, operators, parentSelector, unsurvivalSelector, offspringSize, overlapping, stoppingConditions, cacheSize, false);
    this.reducer = reducer;
    this.distance = semanticsDistance;
  }

  @Override
  public Collection<S> solve(final Problem<S, F> problem, Random random, ExecutorService executor, Listener listener) throws InterruptedException, ExecutionException {
    List<Callable<EnhancedIndividual>> tasks = new ArrayList<>();
    int generations = 0;
    AtomicInteger births = new AtomicInteger();
    AtomicInteger fitnessEvaluations = new AtomicInteger();
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
      tasks.add(birthCallable(genotype, Collections.EMPTY_LIST, generations, mapper, semanticsFunction, fitnessFunction, random, listener));
    }
    List<Individual<G, S, F>> population = (List) Misc.getAll(executor.invokeAll(tasks));
    births.addAndGet(populationSize);
    fitnessEvaluations.addAndGet(populationSize);
    //iterate
    while (true) {
      tasks.clear();
      generations = generations + 1;
      groupIndividuals(population, fitnessFunction, random, fitnessEvaluations);
      //build offsprings      
      List<Collection<Individual<G, S, F>>> rankedPopulation = ranker.rank(population, random);
      int i = 0;
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
            tasks.add(birthCallable(childGenotype, parents, generations, mapper, semanticsFunction, fitnessFunction, random, listener));
          }
          fitnessEvaluations.addAndGet(childGenotypes.size());
          births.addAndGet(childGenotypes.size());
          i = i + childGenotypes.size();
        } catch (FunctionException ex) {
          //just ignore: will not be added to the population
        }
      }
      List<Individual<G, S, F>> newPopulation = (List) Misc.getAll(executor.invokeAll(tasks));
      //update population
      updatePopulation(population, newPopulation, random);
      //reduce population
      groupIndividuals(population, fitnessFunction, random, fitnessEvaluations);
      reducePopulation(population, random);
      //send event
      rankedPopulation = ranker.rank(population, random);
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

  private Callable<EnhancedIndividual> birthCallable(
          final G genotype,
          final List<Individual<G, S, F>> parents,
          final int birthIteration,
          final NonDeterministicFunction<G, S> solutionFunction,
          final Function<S, B> semanticsFunction,
          final Function<? super B, ? extends F> fitnessFunction,
          final Random random,
          final Listener listener) {
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
      return new EnhancedIndividual(genotype, solution, semantics, fitness, birthIteration, parents, info);
    };
  }

  private void groupIndividuals(
          final List<Individual<G, S, F>> population,
          final Function<? super B, ? extends F> fitnessFunction,
          final Random random,
          final AtomicInteger fitnessEvaluations) {
    population.stream().forEach(individual -> ((EnhancedIndividual) individual).reset());
    //initial rank by fitness
    List<Collection<Individual<G, S, F>>> rankedPopulation = ranker.rank(population, random);
    List<Individual<G, S, F>> sortedPopulation = new ArrayList<>();
    rankedPopulation.stream().forEach(rank -> sortedPopulation.addAll(rank));
    //associate individuals
    for (int h = 0; h<sortedPopulation.size(); h++) {
      EnhancedIndividual individual = (EnhancedIndividual) sortedPopulation.get(h);
      if (individual.getGroup().size()>1) {
        continue;
      }
      while (true) {
        //sort other by distance to this
        List<Individual<G, S, F>> others = sortedPopulation.stream()
                .skip(h)
                .filter(i -> individual.getGroup().size()==1)
                .sorted((i1, i2) -> {
                  double d1 = distance.apply(((EnhancedIndividual) i1).getSemantics(), individual.getSemantics());
                  double d2 = distance.apply(((EnhancedIndividual) i2).getSemantics(), individual.getSemantics());
                  return -Double.compare(d1, d2);
                })
                .collect(Collectors.toList());
        //iterate on others
        boolean found = false;
        for (Individual<G, S, F> baseOther : others) {
          EnhancedIndividual other = (EnhancedIndividual) baseOther;
          fitnessEvaluations.incrementAndGet();
          Pair<S, B> groupedPair = reducer.apply(
                  Pair.build(individual.getSolution(), individual.getSemantics()),
                  Pair.build(other.getSolution(), other.getSemantics())
          );
          F groupedFitness = fitnessFunction.apply(groupedPair.second());
          //compare composed vs original
          EnhancedIndividual groupedIndividual = new EnhancedIndividual(
                  individual.getGenotype(),
                  groupedPair.first(),
                  groupedPair.second(),
                  groupedFitness,
                  individual.getBirthIteration(),
                  individual.getParents(),
                  individual.getInfo()
          );
          if (ranker.compare(groupedIndividual, individual, random) == -1) {
            //update this individuals
            individual.setGroupSolution(groupedPair.first());
            individual.setGroupSemantics(groupedPair.second());
            individual.setGroupFitness(groupedFitness);
            individual.getGroup().forEach(groupIndividual -> {
              if (groupIndividual != individual) {
                groupIndividual.getGroup().add(other);
              }
            });
            individual.getGroup().add(other);
            other.getGroup().add(individual);
            for (EnhancedIndividual groupIndividual : individual.getGroup()) {
              groupIndividual.setGroupSolution(groupedPair.first());
              groupIndividual.setGroupSemantics(groupedPair.second());
              groupIndividual.setGroupFitness(groupedFitness);
            }
            found = true;
            break;
          }
        }
        if (!found) {
          break;
        }
      }
    }
  }

}
