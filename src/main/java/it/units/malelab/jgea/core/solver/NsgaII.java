/*
 * Copyright 2022 Giorgia Nadizar <giorgia.nadizar@gmail.com> (as giorgia)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core.solver;

import it.units.malelab.jgea.core.Factory;
import it.units.malelab.jgea.core.MultiHomogeneousObjectiveProblem;
import it.units.malelab.jgea.core.operator.GeneticOperator;
import it.units.malelab.jgea.core.order.DAGPartiallyOrderedCollection;
import it.units.malelab.jgea.core.order.PartialComparator;
import it.units.malelab.jgea.core.order.PartialComparator.PartialComparatorOutcome;
import it.units.malelab.jgea.core.order.PartiallyOrderedCollection;
import it.units.malelab.jgea.core.solver.state.POSetPopulationState;
import it.units.malelab.jgea.core.util.Misc;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.random.RandomGenerator;
import java.util.stream.IntStream;

/**
 * @author giorgia
 */

// source -> https://doi.org/10.1109/4235.996017

public class NsgaII<P extends MultiHomogeneousObjectiveProblem<S, Double>, G, S>
    extends AbstractPopulationIterativeBasedSolver<NsgaII.State<G, S>, P, G, S, List<Double>> {

  protected final Map<GeneticOperator<G>, Double> operators;
  private final boolean remap;

  public NsgaII(
      Function<? super G, ? extends S> solutionMapper,
      Factory<? extends G> genotypeFactory, int populationSize,
      Predicate<? super State<G, S>> stopCondition,
      Map<GeneticOperator<G>, Double> operators,
      boolean remap) {
    super(solutionMapper, genotypeFactory, populationSize, stopCondition);
    this.operators = operators;
    this.remap = remap;
  }

  private static class RankedIndividual<G, S> implements Comparable<RankedIndividual<G, S>> {

    protected final Individual<G, S, List<Double>> individual;
    protected final int rank;

    public RankedIndividual(Individual<G, S, List<Double>> individual, int rank) {
      this.individual = individual;
      this.rank = rank;
    }

    @Override
    public int compareTo(@NotNull RankedIndividual<G, S> o) {
      Comparator<RankedIndividual<G, S>> rankComparator = Comparator.comparing(i -> i.rank);
      return rankComparator.compare(this, o);
    }

  }

  private static class RankedWithDistanceIndividual<G, S> extends RankedIndividual<G, S> implements Comparable<RankedIndividual<G, S>> {
    private double crowdingDistance;

    public RankedWithDistanceIndividual(Individual<G, S, List<Double>> individual, int rank, double crowdingDistance) {
      super(individual, rank);
      this.crowdingDistance = crowdingDistance;
    }

    public RankedWithDistanceIndividual(RankedIndividual<G, S> rankedIndividual, double crowdingDistance) {
      this(rankedIndividual.individual, rankedIndividual.rank, crowdingDistance);
    }

    public RankedWithDistanceIndividual(RankedIndividual<G, S> rankedIndividual) {
      this(rankedIndividual, 0);
    }

    @Override
    public int compareTo(@NotNull RankedIndividual<G, S> o) {
      RankedWithDistanceIndividual<G, S> that = (RankedWithDistanceIndividual<G, S>) o;
      Comparator<RankedWithDistanceIndividual<G, S>> rankComparator = Comparator.comparing(i -> i.rank);
      Comparator<RankedWithDistanceIndividual<G, S>> distanceComparator = Comparator.comparing(i -> i.crowdingDistance);
      return rankComparator.thenComparing(distanceComparator).compare(this, that);
    }

  }

  public static class State<G, S> extends POSetPopulationState<G, S, List<Double>> {

    private List<RankedWithDistanceIndividual<G, S>> rankedWithDistanceIndividuals;

    public State() {
    }

    protected State(
        LocalDateTime startingDateTime,
        long elapsedMillis,
        long nOfIterations,
        long nOfBirths,
        long nOfFitnessEvaluations,
        PartiallyOrderedCollection<Individual<G, S, List<Double>>> population,
        List<RankedWithDistanceIndividual<G, S>> rankedWithDistanceIndividuals
    ) {
      super(startingDateTime, elapsedMillis, nOfIterations, nOfBirths, nOfFitnessEvaluations, population);
      this.rankedWithDistanceIndividuals = rankedWithDistanceIndividuals;
    }

    @Override
    public State<G, S> immutableCopy() {
      return new State<>(
          startingDateTime,
          elapsedMillis,
          nOfIterations,
          nOfBirths,
          nOfFitnessEvaluations,
          population.immutableCopy(),
          new ArrayList<>(rankedWithDistanceIndividuals)
      );
    }

  }

  @Override
  protected State<G, S> initState(P problem, RandomGenerator random, ExecutorService executor) {
    return new State<>();
  }

  @Override
  public State<G, S> init(P problem, RandomGenerator random, ExecutorService executor) throws SolverException {
    State<G, S> state = super.init(problem, random, executor);
    state.rankedWithDistanceIndividuals = trimPopulation(state.getPopulation().all(), problem);
    return state;
  }

  @Override
  public void update(P problem, RandomGenerator random, ExecutorService executor, State<G, S> state) throws SolverException {
    Collection<Individual<G, S, List<Double>>> offspring = buildOffspring(state, problem, random, executor);
    if (remap) {
      offspring.addAll(map(
          List.of(),
          state.getPopulation().all(),
          solutionMapper,
          problem.qualityFunction(),
          executor,
          state
      ));
    } else {
      offspring.addAll(state.getPopulation().all());
    }
    state.rankedWithDistanceIndividuals = trimPopulation(offspring, problem);

    //update state
    state.setPopulation(new DAGPartiallyOrderedCollection<>(offspring, comparator(problem)));
    state.incNOfIterations();
    state.updateElapsedMillis();
  }

  private Collection<RankedIndividual<G, S>> fastNonDominatedSort(Collection<Individual<G, S, List<Double>>> individuals, PartialComparator<Individual<G, S, List<Double>>> comparator) {
    Collection<RankedIndividual<G, S>> rankedIndividuals = new ArrayList<>();

    int rank = 0;
    List<Individual<G, S, List<Double>>> orderedIndividuals = new ArrayList<>(individuals);
    double[] dominantIndividuals = new double[individuals.size()];
    List<Set<Individual<G, S, List<Double>>>> dominatedIndividuals = new ArrayList<>();
    List<Individual<G, S, List<Double>>> currentFront = new ArrayList<>();
    for (int p = 0; p < individuals.size(); p++) {
      Set<Individual<G, S, List<Double>>> pDominatedIndividuals = new HashSet<>();
      dominatedIndividuals.add(pDominatedIndividuals);
      for (int q = 0; q < individuals.size(); q++) {
        PartialComparatorOutcome comparatorOutcome = comparator.compare(orderedIndividuals.get(p), orderedIndividuals.get(q));
        if (comparatorOutcome == PartialComparatorOutcome.BEFORE) {
          pDominatedIndividuals.add(orderedIndividuals.get(q));
        } else if (comparatorOutcome == PartialComparatorOutcome.AFTER) {
          dominantIndividuals[p]++;
        }
      }
      if (dominantIndividuals[p] == 0) {
        rankedIndividuals.add(new RankedIndividual<>(orderedIndividuals.get(p), rank));
        currentFront.add(orderedIndividuals.get(p));
      }
    }

    List<Individual<G, S, List<Double>>> newCurrentFront;
    while (!currentFront.isEmpty()) {
      rank++;
      newCurrentFront = new ArrayList<>();
      for (Individual<G, S, List<Double>> individualP : currentFront) {
        int p = orderedIndividuals.indexOf(individualP);
        for (Individual<G, S, List<Double>> individualQ : dominatedIndividuals.get(p)) {
          int q = orderedIndividuals.indexOf(individualQ);
          dominantIndividuals[q]--;
          if (dominantIndividuals[q] == 0) {
            rankedIndividuals.add(new RankedIndividual<>(individualQ, rank));
            newCurrentFront.add(individualQ);
          }
        }
      }
      currentFront = newCurrentFront;
    }

    return rankedIndividuals;
  }

  private Collection<RankedWithDistanceIndividual<G, S>> assignCrowdingDistance(Collection<RankedIndividual<G, S>> rankedIndividuals, List<Comparator<Double>> comparators) {
    int maxRank = rankedIndividuals.stream().mapToInt(i -> i.rank).max().orElse(0);
    Collection<RankedWithDistanceIndividual<G, S>> rankedWithDistanceIndividuals = new ArrayList<>();
    IntStream.range(0, maxRank + 1).forEach(rank -> {
      Collection<RankedIndividual<G, S>> currentRankIndividuals = rankedIndividuals.stream().filter(i -> i.rank == rank).toList();
      rankedWithDistanceIndividuals.addAll(assignCrowdingDistanceWithinRank(currentRankIndividuals, comparators));
    });
    return rankedWithDistanceIndividuals;
  }

  private Collection<RankedWithDistanceIndividual<G, S>> assignCrowdingDistanceWithinRank(Collection<RankedIndividual<G, S>> rankedIndividuals, List<Comparator<Double>> comparators) {
    int l = rankedIndividuals.size();
    List<RankedWithDistanceIndividual<G, S>> rankedWithDistanceIndividuals = rankedIndividuals.stream().map(RankedWithDistanceIndividual::new).toList();
    IntStream.range(0, comparators.size()).forEach(m -> {
      Comparator<RankedIndividual<G, S>> mObjectiveComparator = Comparator.comparing(i -> i.individual.fitness().get(m), comparators.get(m));
      List<RankedWithDistanceIndividual<G, S>> mSortedIndividuals = rankedWithDistanceIndividuals.stream().sorted(mObjectiveComparator).toList();
      mSortedIndividuals.get(0).crowdingDistance = Double.POSITIVE_INFINITY;
      mSortedIndividuals.get(l - 1).crowdingDistance = Double.POSITIVE_INFINITY;
      double mNormalization = mSortedIndividuals.get(l - 1).individual.fitness().get(m) - mSortedIndividuals.get(0).individual.fitness().get(m);
      IntStream.range(1, l - 2).forEach(i -> {
        double ithDistanceIncrement = (mSortedIndividuals.get(i + 1).individual.fitness().get(m) - mSortedIndividuals.get(i - 1).individual.fitness().get(m)) / mNormalization;
        mSortedIndividuals.get(i).crowdingDistance += ithDistanceIncrement;
      });
    });

    return rankedWithDistanceIndividuals;
  }

  private List<RankedWithDistanceIndividual<G, S>> trimPopulation(Collection<Individual<G, S, List<Double>>> offspring, P problem) {
    Collection<RankedIndividual<G, S>> rankedIndividuals = fastNonDominatedSort(offspring, comparator(problem));
    Collection<RankedWithDistanceIndividual<G, S>> rankedWithDistanceIndividuals = assignCrowdingDistance(rankedIndividuals, problem.comparators());
    return rankedWithDistanceIndividuals.stream().sorted().limit(populationSize).toList();
  }

  private Collection<Individual<G, S, List<Double>>> buildOffspring(
      State<G, S> state, P problem, RandomGenerator random, ExecutorService executor
  ) throws SolverException {
    Collection<G> offspringGenotypes = new ArrayList<>();
    while (offspringGenotypes.size() < populationSize) {
      GeneticOperator<G> operator = Misc.pickRandomly(operators, random);
      List<G> parentGenotypes = new ArrayList<>(operator.arity());
      for (int j = 0; j < operator.arity(); j++) {
        Individual<G, S, List<Double>> parent = selectParentWithBinaryTournament(state, random);
        parentGenotypes.add(parent.genotype());
      }
      offspringGenotypes.addAll(operator.apply(parentGenotypes, random));
    }
    return map(offspringGenotypes, List.of(), solutionMapper, problem.qualityFunction(), executor, state);
  }


  // TODO maybe remove from here and field with tournament selector (requires a DAG on rankedWithDistanceIndividuals)
  private Individual<G, S, List<Double>> selectParentWithBinaryTournament(State<G, S> state, RandomGenerator random) {
    RankedWithDistanceIndividual<G, S> c1 = Misc.pickRandomly(state.rankedWithDistanceIndividuals, random);
    RankedWithDistanceIndividual<G, S> c2 = Misc.pickRandomly(state.rankedWithDistanceIndividuals, random);

    return c1.compareTo(c2) <= 0 ? c1.individual : c2.individual;
  }

}
