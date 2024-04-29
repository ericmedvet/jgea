package io.github.ericmedvet.jgea.core.solver.mapelites;

import io.github.ericmedvet.jgea.core.Factory;
import io.github.ericmedvet.jgea.core.operator.Mutation;
import io.github.ericmedvet.jgea.core.order.PartiallyOrderedCollection;
import io.github.ericmedvet.jgea.core.problem.QualityBasedProblem;
import io.github.ericmedvet.jgea.core.solver.AbstractPopulationBasedIterativeSolver;
import io.github.ericmedvet.jgea.core.solver.Individual;
import io.github.ericmedvet.jgea.core.solver.SolverException;
import io.github.ericmedvet.jgea.core.util.Pair;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.random.RandomGenerator;

public class CoMapElites<G1, G2, S1, S2, S, Q>
    extends AbstractPopulationBasedIterativeSolver<
        CoMEPopulationState<G1, G2, S1, S2, S, Q, QualityBasedProblem<S, Q>>,
        QualityBasedProblem<S, Q>,
        CoMEPopulationState.CompositeIndividual<G1, G2, S1, S2, S, Q>,
        Pair<G1, G2>,
        S,
        Q> {

  private final Factory<? extends G1> genotypeFactory1;
  private final Factory<? extends G2> genotypeFactory2;
  private final Mutation<G1> mutation1;
  private final Mutation<G2> mutation2;
  private final Function<? super G1, ? extends S1> solutionMapper1;
  private final Function<? super G2, ? extends S2> solutionMapper2;
  private final BiFunction<? super S1, ? super S2, ? extends S> solutionMerger;
  private final List<MapElites.Descriptor<G1, S1, Q>> descriptors1;
  private final List<MapElites.Descriptor<G2, S2, Q>> descriptors2;
  private final int populationSize;
  private final int nOfOffspring;

  private record State<G1, G2, S1, S2, S, Q>(
      LocalDateTime startingDateTime,
      long elapsedMillis,
      long nOfIterations,
      QualityBasedProblem<S, Q> problem,
      Predicate<io.github.ericmedvet.jgea.core.solver.State<?, ?>> stopCondition,
      long nOfBirths,
      long nOfQualityEvaluations,
      PartiallyOrderedCollection<CompositeIndividual<G1, G2, S1, S2, S, Q>> pocPopulation,
      Map<List<Integer>, Individual<G1, S1, Q>> mapOfElites1,
      Map<List<Integer>, Individual<G2, S2, Q>> mapOfElites2,
      List<MapElites.Descriptor<G1, S1, Q>> descriptors1,
      List<MapElites.Descriptor<G2, S2, Q>> descriptors2)
      implements CoMEPopulationState<G1, G2, S1, S2, S, Q, QualityBasedProblem<S, Q>>,
          io.github.ericmedvet.jgea.core.solver.State.WithComputedProgress<QualityBasedProblem<S, Q>, S> {

    // from -> for the init a state
    public static <G1, G2, S1, S2, S, Q> State<G1, G2, S1, S2, S, Q> from(
        QualityBasedProblem<S, Q> problem,
        long nOfBirths,
        long nOfQualityEvaluations,
        Map<List<Integer>, Individual<G1, S1, Q>> mapOfElites1,
        Map<List<Integer>, Individual<G2, S2, Q>> mapOfElites2,
        List<CoMEPopulationState.CompositeIndividual<G1, G2, S1, S2, S, Q>> compositeIndividuals,
        List<MapElites.Descriptor<G1, S1, Q>> descriptors1,
        List<MapElites.Descriptor<G2, S2, Q>> descriptors2,
        Predicate<io.github.ericmedvet.jgea.core.solver.State<?, ?>> stopCondition) {
      return new State<>(
          LocalDateTime.now(),
          0,
          0,
          problem,
          stopCondition,
          nOfBirths,
          nOfQualityEvaluations,
          PartiallyOrderedCollection.from(compositeIndividuals, partialComparator(problem)),
          mapOfElites1,
          mapOfElites2,
          descriptors1,
          descriptors2);
    }

    // from -> for update a state
    public static <G1, G2, S1, S2, S, Q> State<G1, G2, S1, S2, S, Q> from(
        State<G1, G2, S1, S2, S, Q> state,
        long nOfBirths,
        long nOfFitnessEvaluations,
        Map<List<Integer>, Individual<G1, S1, Q>> mapOfElites1,
        Map<List<Integer>, Individual<G2, S2, Q>> mapOfElites2,
        List<CoMEPopulationState.CompositeIndividual<G1, G2, S1, S2, S, Q>> compositeIndividuals) {
      return new State<>(
          state.startingDateTime,
          ChronoUnit.MILLIS.between(state.startingDateTime, LocalDateTime.now()),
          state.nOfIterations + 1,
          state.problem,
          state.stopCondition,
          state.nOfBirths + nOfBirths,
          state.nOfQualityEvaluations + nOfFitnessEvaluations,
          PartiallyOrderedCollection.from(compositeIndividuals, partialComparator(state.problem)),
          mapOfElites1,
          mapOfElites2,
          state.descriptors1,
          state.descriptors2);
    }
  } // record state end

  public CoMapElites(
      Predicate<? super CoMEPopulationState<G1, G2, S1, S2, S, Q, QualityBasedProblem<S, Q>>> stopCondition,
      Factory<? extends G1> genotypeFactory1,
      Factory<? extends G2> genotypeFactory2,
      Function<? super G1, ? extends S1> solutionMapper1,
      Function<? super G2, ? extends S2> solutionMapper2,
      BiFunction<? super S1, ? super S2, ? extends S> solutionMerger,
      List<MapElites.Descriptor<G1, S1, Q>> descriptors1,
      List<MapElites.Descriptor<G2, S2, Q>> descriptors2,
      Mutation<G1> mutation1,
      Mutation<G2> mutation2,
      int populationSize,
      int nOfOffspring) {
    super(null, null, stopCondition, false);
    this.genotypeFactory1 = genotypeFactory1;
    this.genotypeFactory2 = genotypeFactory2;
    this.solutionMapper1 = solutionMapper1;
    this.solutionMapper2 = solutionMapper2;
    this.solutionMerger = solutionMerger;
    this.descriptors1 = descriptors1;
    this.descriptors2 = descriptors2;
    this.mutation1 = mutation1;
    this.mutation2 = mutation2;
    this.populationSize = populationSize;
    this.nOfOffspring = nOfOffspring;
  }

  @Override
  protected CoMEPopulationState.CompositeIndividual<G1, G2, S1, S2, S, Q> newIndividual(
      Pair<G1, G2> genotype,
      CoMEPopulationState<G1, G2, S1, S2, S, Q, QualityBasedProblem<S, Q>> state,
      QualityBasedProblem<S, Q> problem) {
    S1 s1 = solutionMapper1.apply(genotype.first());
    S2 s2 = solutionMapper2.apply(genotype.second());
    S s = solutionMerger.apply(s1, s2);
    Q q = problem.qualityFunction().apply(s);
    CoMEPopulationState.CompositeIndividual<G1, G2, S1, S2, S, Q> compositeIndividual =
        new CoMEPopulationState.CompositeIndividual<>(
            genotype.first(),
            genotype.second(),
            s1,
            s2,
            s,
            q,
            state == null ? 0 : state.nOfIterations(),
            state == null ? 0 : state.nOfIterations());
    return compositeIndividual;
  }

  @Override
  protected CoMEPopulationState.CompositeIndividual<G1, G2, S1, S2, S, Q> updateIndividual(
      CoMEPopulationState.CompositeIndividual<G1, G2, S1, S2, S, Q> individual,
      CoMEPopulationState<G1, G2, S1, S2, S, Q, QualityBasedProblem<S, Q>> state,
      QualityBasedProblem<S, Q> problem) {
    S1 s1 = solutionMapper1.apply(individual.genotype1());
    S2 s2 = solutionMapper2.apply(individual.genotype2());
    S s = solutionMerger.apply(s1, s2);
    Q q = problem.qualityFunction().apply(s);
    CoMEPopulationState.CompositeIndividual compositeIndividual = new CoMEPopulationState.CompositeIndividual<>(
        individual.genotype1(),
        individual.genotype2(),
        s1,
        s2,
        s,
        q,
        individual.qualityMappingIteration(),
        state == null ? individual.qualityMappingIteration() : state.nOfIterations());
    return compositeIndividual;
  }

  @Override
  public CoMEPopulationState<G1, G2, S1, S2, S, Q, QualityBasedProblem<S, Q>> init(
      QualityBasedProblem<S, Q> problem, RandomGenerator random, ExecutorService executor)
      throws SolverException {
    List<Individual<G1, S1, Q>> individualList1 = new ArrayList<>();
    List<Individual<G2, S2, Q>> individualList2 = new ArrayList<>();
    List<CoMEPopulationState.CompositeIndividual<G1, G2, S1, S2, S, Q>> compositeIndividuals = new ArrayList<>();
    List<? extends G1> g1s = genotypeFactory1.build(populationSize, random);
    List<? extends G2> g2s = genotypeFactory2.build(populationSize, random);
    for (int i = 0; i < populationSize; i++) {
      S1 s1 = solutionMapper1.apply(g1s.get(i));
      S2 s2 = solutionMapper2.apply(g2s.get(i));
      S s = solutionMerger.apply(s1, s2);
      Q q = problem.qualityFunction().apply(s);
      individualList1.add(Individual.of(g1s.get(i), s1, q, 0, 0));
      individualList2.add(Individual.of(g2s.get(i), s2, q, 0, 0));
      compositeIndividuals.add(
          new CoMEPopulationState.CompositeIndividual<>(g1s.get(i), g2s.get(i), s1, s2, s, q, 0, 0));
    }
    return State.from(
        problem,
        populationSize * 2L,
        populationSize,
        MapElites.mapOfElites(individualList1, descriptors1, partialComparator(problem)),
        MapElites.mapOfElites(individualList2, descriptors2, partialComparator(problem)),
        compositeIndividuals,
        descriptors1,
        descriptors2,
        stopCondition());
  }

  @Override
  public CoMEPopulationState<G1, G2, S1, S2, S, Q, QualityBasedProblem<S, Q>> update(
      QualityBasedProblem<S, Q> problem,
      RandomGenerator random,
      ExecutorService executor,
      CoMEPopulationState<G1, G2, S1, S2, S, Q, QualityBasedProblem<S, Q>> state)
      throws SolverException {
    /*Collection<Individual<G1, S1, Q>> parents1 = ((State<G1, G2, S1, S2, S, Q>) state).mapOfElites1.values();
    List<G1> offspringGenotypes1 = IntStream.range(0, nOfOffspring).mapToObj(j -> MapElites.mutation.mutate(Misc.pickRandomly(parents1, random).genotype(), random)).toList();
    Collection<Individual> individuals1 = (Collection<Individual>) Stream.of(map(offspringGenotypes1, List.of(), state, problem, executor), parents1);
    Map<List<Integer>, Individual<G1, S1, Q>> elitesMap1 = MapElites.mapOfElites(individuals1, partialComparator(problem));

    Collection<Individual<G2, S2, Q>> parents2 = ((State<G1, G2, S1, S2, S, Q>) state).mapOfElites2.values();
    List<G2> offspringGenotypes2 = IntStream.range(0, nOfOffspring).mapToObj(j -> MapElites.mutation.mutate(Misc.pickRandomly(parents2, random).genotype(), random)).toList();
    Collection<Individual> individuals2 = (Collection<Individual>) Stream.of(map(offspringGenotypes2, List.of(), state, problem, executor), parents2);
    Map<List<Integer>, Individual<G2, S2, Q>> elitesMap2 = MapElites.mapOfElites(individuals2, partialComparator(problem));

    // TAKE IT FROM HERE
    */
    return null;
  }
}
