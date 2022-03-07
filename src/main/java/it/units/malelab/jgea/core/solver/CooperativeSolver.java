package it.units.malelab.jgea.core.solver;

import it.units.malelab.jgea.core.QualityBasedProblem;
import it.units.malelab.jgea.core.order.DAGPartiallyOrderedCollection;
import it.units.malelab.jgea.core.order.PartialComparator;
import it.units.malelab.jgea.core.order.PartiallyOrderedCollection;
import it.units.malelab.jgea.core.solver.state.POSetPopulationState;
import it.units.malelab.jgea.core.util.Misc;
import it.units.malelab.jgea.core.util.Pair;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.random.RandomGenerator;

public class CooperativeSolver<T1 extends POSetPopulationState<G1, S1, Q>, T2 extends POSetPopulationState<G2, S2, Q>,
    G1, G2, S1, S2, P extends QualityBasedProblem<S, Q>, S, Q> extends
    AbstractPopulationIterativeBasedSolver<CooperativeSolver.State<T1, T2, G1, G2, S1, S2, S, Q>, P, Pair<G1, G2>, S, Q> {

  private final AbstractPopulationIterativeBasedSolver<T1, QualityBasedProblem<S1, Q>, G1, S1, Q> firstSolver;
  private final AbstractPopulationIterativeBasedSolver<T2, QualityBasedProblem<S2, Q>, G2, S2, Q> secondSolver;
  private final BiFunction<S1, S2, S> solutionAggregator;
  private final Function<T1, Individual<G1, S1, Q>> firstRepresentativeExtractor;
  private final Function<T2, Individual<G2, S2, Q>> secondRepresentativeExtractor;

  public CooperativeSolver(AbstractPopulationIterativeBasedSolver<T1, QualityBasedProblem<S1, Q>, G1, S1, Q> firstSolver, AbstractPopulationIterativeBasedSolver<T2, QualityBasedProblem<S2, Q>, G2, S2, Q> secondSolver, BiFunction<S1, S2, S> solutionAggregator, Function<T1, Individual<G1, S1, Q>> firstRepresentativeExtractor, Function<T2, Individual<G2, S2, Q>> secondRepresentativeExtractor, Predicate<? super State<T1, T2, G1, G2, S1, S2, S, Q>> stopCondition) {
    super(
        p -> solutionAggregator.apply(firstSolver.solutionMapper.apply(p.first()), secondSolver.solutionMapper.apply(p.second())),
        null,
        0,
        stopCondition
    );
    // TODO check null genotype factory and pop size
    this.firstSolver = firstSolver;
    this.secondSolver = secondSolver;
    this.solutionAggregator = solutionAggregator;
    this.firstRepresentativeExtractor = firstRepresentativeExtractor;
    this.secondRepresentativeExtractor = secondRepresentativeExtractor;
  }

  public static class State<T1 extends POSetPopulationState<G1, S1, Q>, T2 extends POSetPopulationState<G2, S2, Q>,
      G1, G2, S1, S2, S, Q> extends POSetPopulationState<Pair<G1, G2>, S, Q> {
    private final T1 firstState;
    private final T2 secondState;

    public State(LocalDateTime startingDateTime, long elapsedMillis, long nOfIterations, long nOfBirths,
                 long nOfFitnessEvaluations,
                 PartiallyOrderedCollection<Individual<Pair<G1, G2>, S, Q>> population,
                 T1 firstState, T2 secondState) {
      super(startingDateTime, elapsedMillis, nOfIterations, nOfBirths, nOfFitnessEvaluations, population);
      this.firstState = firstState;
      this.secondState = secondState;
    }

    public State(T1 firstState, T2 secondState) {
      this.firstState = firstState;
      this.secondState = secondState;
    }

    public Individual<G1, S1, Q> firstBest() {
      return Misc.first(firstState.getPopulation().firsts());
    }

    public Individual<G2, S2, Q> secondBest() {
      return Misc.first(secondState.getPopulation().firsts());
    }

    @Override
    public long getNOfBirths() {
      return firstState.getNOfBirths() + secondState.getNOfBirths();
    }

    @Override
    public long getNOfFitnessEvaluations() {
      return firstState.getNOfFitnessEvaluations() + secondState.getNOfFitnessEvaluations();
    }

    @Override
    @SuppressWarnings("unchecked")
    public State<T1, T2, G1, G2, S1, S2, S, Q> immutableCopy() {
      return new State<>(startingDateTime, elapsedMillis, nOfIterations,
          getNOfBirths(), getNOfFitnessEvaluations(), population,
          (T1) firstState.immutableCopy(), (T2) secondState.immutableCopy());
    }

  }

  @Override
  protected State<T1, T2, G1, G2, S1, S2, S, Q> initState(P problem, RandomGenerator random, ExecutorService executor) {
    // TODO check!
    return null;
  }

  @Override
  public State<T1, T2, G1, G2, S1, S2, S, Q> init(P problem, RandomGenerator random, ExecutorService executor) throws SolverException {
    QualityBasedProblem<S1, Q> dummyProblem1 = QualityBasedProblem.create(s1 -> null, (q1, q2) -> PartialComparator.PartialComparatorOutcome.SAME);
    QualityBasedProblem<S2, Q> dummyProblem2 = QualityBasedProblem.create(s2 -> null, (q1, q2) -> PartialComparator.PartialComparatorOutcome.SAME);
    Individual<G1, S1, Q> representative1 = firstRepresentativeExtractor.apply(firstSolver.init(dummyProblem1, random, executor));
    Individual<G2, S2, Q> representative2 = secondRepresentativeExtractor.apply(secondSolver.init(dummyProblem2, random, executor));
    QualityBasedProblem<S1, Q> problem1 = QualityBasedProblem.create(
        s1 -> problem.qualityFunction().apply(solutionAggregator.apply(s1, representative2.solution())),
        problem.qualityComparator()
    );
    QualityBasedProblem<S2, Q> problem2 = QualityBasedProblem.create(
        s2 -> problem.qualityFunction().apply(solutionAggregator.apply(representative1.solution(), s2)),
        problem.qualityComparator()
    );
    T1 state1 = firstSolver.init(problem1, random, executor);
    T2 state2 = secondSolver.init(problem2, random, executor);
    Collection<Individual<Pair<G1, G2>, S, Q>> evaluatedIndividuals = new ArrayList<>();
    state1.getPopulation().all().stream().map(i ->
        new Individual<>(Pair.of(i.genotype(), representative2.genotype()),
            solutionAggregator.apply(i.solution(), representative2.solution()),
            i.fitness(), i.fitnessMappingIteration(), i.genotypeBirthIteration())
    ).forEach(evaluatedIndividuals::add);
    state2.getPopulation().all().stream().map(i ->
        new Individual<>(Pair.of(representative1.genotype(), i.genotype()),
            solutionAggregator.apply(representative1.solution(), i.solution()),
            i.fitness(), i.fitnessMappingIteration(), i.genotypeBirthIteration())
    ).forEach(evaluatedIndividuals::add);
    State<T1, T2, G1, G2, S1, S2, S, Q> state = new State<>(state1, state2);
    state.setPopulation(new DAGPartiallyOrderedCollection<>(evaluatedIndividuals, comparator(problem)));
    return state;
  }

  @Override
  public void update(P problem, RandomGenerator random, ExecutorService executor, State<T1, T2, G1, G2, S1, S2, S, Q> state) throws SolverException {
    // TODO change to list of representatives and average or median of fitness
    Individual<G1, S1, Q> representative1 = firstRepresentativeExtractor.apply(state.firstState);
    Individual<G2, S2, Q> representative2 = secondRepresentativeExtractor.apply(state.secondState);
    QualityBasedProblem<S1, Q> problem1 = QualityBasedProblem.create(
        s1 -> problem.qualityFunction().apply(solutionAggregator.apply(s1, representative2.solution())),
        problem.qualityComparator()
    );
    QualityBasedProblem<S2, Q> problem2 = QualityBasedProblem.create(
        s2 -> problem.qualityFunction().apply(solutionAggregator.apply(representative1.solution(), s2)),
        problem.qualityComparator()
    );
    firstSolver.update(problem1, random, executor, state.firstState);
    secondSolver.update(problem2, random, executor, state.secondState);
    Collection<Individual<Pair<G1, G2>, S, Q>> evaluatedIndividuals = new ArrayList<>();
    state.firstState.getPopulation().all().stream().map(i ->
        new Individual<>(Pair.of(i.genotype(), representative2.genotype()),
            solutionAggregator.apply(i.solution(), representative2.solution()),
            i.fitness(), i.fitnessMappingIteration(), i.genotypeBirthIteration())
    ).forEach(evaluatedIndividuals::add);
    state.secondState.getPopulation().all().stream().map(i ->
        new Individual<>(Pair.of(representative1.genotype(), i.genotype()),
            solutionAggregator.apply(representative1.solution(), i.solution()),
            i.fitness(), i.fitnessMappingIteration(), i.genotypeBirthIteration())
    ).forEach(evaluatedIndividuals::add);
    state.setPopulation(new DAGPartiallyOrderedCollection<>(evaluatedIndividuals, comparator(problem)));
    state.incNOfIterations();
    state.updateElapsedMillis();
  }

}
