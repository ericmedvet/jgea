package io.github.ericmedvet.jgea.core.solver;

import io.github.ericmedvet.jgea.core.Factory;
import io.github.ericmedvet.jgea.core.operator.GeneticOperator;
import io.github.ericmedvet.jgea.core.order.DAGPartiallyOrderedCollection;
import io.github.ericmedvet.jgea.core.order.PartialComparator;
import io.github.ericmedvet.jgea.core.order.PartiallyOrderedCollection;
import io.github.ericmedvet.jgea.core.problem.MultiHomogeneousObjectiveProblem;
import io.github.ericmedvet.jgea.core.solver.state.POSetPopulationState;
import io.github.ericmedvet.jgea.core.util.Misc;
import io.github.ericmedvet.jgea.core.util.Progress;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.random.RandomGenerator;
import java.util.stream.IntStream;

// source -> https://doi.org/10.1109/4235.996017

public class NsgaII<P extends MultiHomogeneousObjectiveProblem<S, Double>, G, S>
    extends AbstractPopulationBasedIterativeSolver<NsgaII.State<G, S>, P, G, S, List<Double>> {

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

  public static class RankedIndividual<G, S> implements Comparable<RankedIndividual<G, S>> {

    protected final Individual<G, S, List<Double>> individual;
    protected final int rank;

    public RankedIndividual(Individual<G, S, List<Double>> individual, int rank) {
      this.individual = individual;
      this.rank = rank;
    }

    @Override
    public int compareTo(RankedIndividual<G, S> o) {
      Comparator<RankedIndividual<G, S>> rankComparator = Comparator.comparing(i -> i.rank);
      return rankComparator.compare(this, o);
    }

  }

  public static class RankedWithDistanceIndividual<G, S> extends RankedIndividual<G, S> implements Comparable<RankedIndividual<G, S>> {
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
    public int compareTo(RankedIndividual<G, S> o) {
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
        Progress progress,
        long nOfBirths,
        long nOfFitnessEvaluations,
        PartiallyOrderedCollection<Individual<G, S, List<Double>>> population,
        List<RankedWithDistanceIndividual<G, S>> rankedWithDistanceIndividuals
    ) {
      super(startingDateTime, elapsedMillis, nOfIterations, progress, nOfBirths, nOfFitnessEvaluations, population);
      this.rankedWithDistanceIndividuals = rankedWithDistanceIndividuals;
    }

    @Override
    public State<G, S> immutableCopy() {
      return new State<>(
          startingDateTime,
          elapsedMillis,
          nOfIterations,
          progress,
          nOfBirths,
          nOfFitnessEvaluations,
          population.immutableCopy(),
          new ArrayList<>(rankedWithDistanceIndividuals)
      );
    }

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
        PartialComparator.PartialComparatorOutcome comparatorOutcome = comparator.compare(
            orderedIndividuals.get(p),
            orderedIndividuals.get(q)
        );
        if (comparatorOutcome == PartialComparator.PartialComparatorOutcome.BEFORE) {
          pDominatedIndividuals.add(orderedIndividuals.get(q));
        } else if (comparatorOutcome == PartialComparator.PartialComparatorOutcome.AFTER) {
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

  // TODO maybe remove from here and field with tournament selector (requires a DAG on rankedWithDistanceIndividuals)
  private Individual<G, S, List<Double>> selectParentWithBinaryTournament(State<G, S> state, RandomGenerator random) {
    RankedWithDistanceIndividual<G, S> c1 = Misc.pickRandomly(state.rankedWithDistanceIndividuals, random);
    RankedWithDistanceIndividual<G, S> c2 = Misc.pickRandomly(state.rankedWithDistanceIndividuals, random);

    return c1.compareTo(c2) <= 0 ? c1.individual : c2.individual;
  }

  private List<RankedWithDistanceIndividual<G, S>> trimPopulation(Collection<Individual<G, S, List<Double>>> offspring, P problem) {
    Collection<RankedIndividual<G, S>> rankedIndividuals = fastNonDominatedSort(offspring, comparator(problem));
    Collection<RankedWithDistanceIndividual<G, S>> rankedWithDistanceIndividuals = assignCrowdingDistance(rankedIndividuals, problem.comparators());
    return rankedWithDistanceIndividuals.stream().sorted().limit(populationSize).toList();
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
    Collection<Individual<G, S, List<Double>>> population = state.rankedWithDistanceIndividuals.stream().map(i -> i.individual).toList();

    //update state
    state.setPopulation(new DAGPartiallyOrderedCollection<>(population, comparator(problem)));
    state.incNOfIterations();
    state.updateElapsedMillis();
  }

}
