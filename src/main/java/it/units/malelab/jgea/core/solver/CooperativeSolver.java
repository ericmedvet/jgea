package it.units.malelab.jgea.core.solver;

import it.units.malelab.jgea.core.QualityBasedProblem;
import it.units.malelab.jgea.core.listener.Listener;
import it.units.malelab.jgea.core.order.PartialComparator;
import it.units.malelab.jgea.core.solver.state.POSetPopulationState;
import it.units.malelab.jgea.core.solver.state.State;
import it.units.malelab.jgea.core.util.Misc;
import it.units.malelab.jgea.core.util.Pair;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.random.RandomGenerator;

public class CooperativeSolver<T1 extends POSetPopulationState<G1, S1, Q>, T2 extends POSetPopulationState<G2, S2, Q>,
    G1, G2, S1, S2, P extends QualityBasedProblem<S, Q>, S, Q> {

  private final AbstractPopulationIterativeBasedSolver<T1, QualityBasedProblem<S1, Q>, G1, S1, Q> firstSolver;
  private final AbstractPopulationIterativeBasedSolver<T2, QualityBasedProblem<S2, Q>, G2, S2, Q> secondSolver;
  private final BiFunction<S1, S2, S> solutionAggregator;
  private final Function<T1, Individual<G1, S1, Q>> firstRepresentativeExtractor;
  private final Function<T2, Individual<G2, S2, Q>> secondRepresentativeExtractor;
  private final Predicate<? super CooperativeState<T1, T2, G1, G2, S1, S2, Q>> stopCondition;

  public CooperativeSolver(AbstractPopulationIterativeBasedSolver<T1, QualityBasedProblem<S1, Q>, G1, S1, Q> firstSolver, AbstractPopulationIterativeBasedSolver<T2, QualityBasedProblem<S2, Q>, G2, S2, Q> secondSolver, BiFunction<S1, S2, S> solutionAggregator, Function<T1, Individual<G1, S1, Q>> firstRepresentativeExtractor, Function<T2, Individual<G2, S2, Q>> secondRepresentativeExtractor, Predicate<? super CooperativeState<T1, T2, G1, G2, S1, S2, Q>> stopCondition) {
    this.firstSolver = firstSolver;
    this.secondSolver = secondSolver;
    this.solutionAggregator = solutionAggregator;
    this.firstRepresentativeExtractor = firstRepresentativeExtractor;
    this.secondRepresentativeExtractor = secondRepresentativeExtractor;
    this.stopCondition = stopCondition;
  }

  public static class CooperativeState<T1 extends POSetPopulationState<G1, S1, Q>,
      T2 extends POSetPopulationState<G2, S2, Q>, G1, G2, S1, S2, Q> extends State {
    private final T1 firstState;
    private final T2 secondState;

    public CooperativeState(LocalDateTime startingDateTime, long elapsedMillis, long nOfIterations, T1 firstState, T2 secondState) {
      super(startingDateTime, elapsedMillis, nOfIterations);
      this.firstState = firstState;
      this.secondState = secondState;
    }

    public CooperativeState(T1 firstState, T2 secondState) {
      this.firstState = firstState;
      this.secondState = secondState;
    }

    public Individual<G1, S1, Q> firstBest() {
      return Misc.first(firstState.getPopulation().firsts());
    }

    public Individual<G2, S2, Q> secondBest() {
      return Misc.first(secondState.getPopulation().firsts());
    }

    public long getNOfFitnessEvaluations() {
      return firstState.getNOfFitnessEvaluations() + secondState.getNOfFitnessEvaluations();
    }

    @Override
    public State immutableCopy() {
      return new CooperativeState<>(startingDateTime, elapsedMillis, nOfIterations, firstState.immutableCopy(), secondState.immutableCopy());
    }
  }

  @SuppressWarnings("unchecked")
  public Pair<Collection<S1>, Collection<S2>> solve(
      P problem, RandomGenerator random, ExecutorService executor, Listener<? super CooperativeState<T1, T2, G1, G2, S1, S2, Q>> listener
  ) throws SolverException {
    QualityBasedProblem<S1, Q> dummyProblem1 = QualityBasedProblem.create(s1 -> null, (q1, q2) -> PartialComparator.PartialComparatorOutcome.SAME);
    QualityBasedProblem<S2, Q> dummyProblem2 = QualityBasedProblem.create(s2 -> null, (q1, q2) -> PartialComparator.PartialComparatorOutcome.SAME);
    CooperativeState<T1, T2, G1, G2, S1, S2, Q> state = init(problem, random, executor);
    listener.listen(state);
    while (!stopCondition.test(state)) {
      update(problem, random, executor, state);
      listener.listen((CooperativeState<T1, T2, G1, G2, S1, S2, Q>) state.immutableCopy());
    }
    listener.done();
    return Pair.of(
        firstSolver.extractSolutions(dummyProblem1, random, executor, state.firstState),
        secondSolver.extractSolutions(dummyProblem2, random, executor, state.secondState)
    );
  }

  public Pair<Collection<S1>, Collection<S2>> solve(
      P problem, RandomGenerator random, ExecutorService executor
  ) throws SolverException {
    return solve(problem, random, executor, Listener.deaf());
  }

  public void update(P problem, RandomGenerator random, ExecutorService executor, CooperativeState<T1, T2, G1, G2, S1, S2, Q> state) throws SolverException {
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
    // TODO get all evaluated individuals and add them to the state
    firstSolver.update(problem1, random, executor, state.firstState);
    secondSolver.update(problem2, random, executor, state.secondState);
    state.incNOfIterations();
    state.updateElapsedMillis();
  }

  public CooperativeState<T1, T2, G1, G2, S1, S2, Q> init(P problem, RandomGenerator random, ExecutorService executor) throws SolverException {
    QualityBasedProblem<S1, Q> dummyProblem1 = QualityBasedProblem.create(s1 -> null, (q1, q2) -> PartialComparator.PartialComparatorOutcome.SAME);
    QualityBasedProblem<S2, Q> dummyProblem2 = QualityBasedProblem.create(s2 -> null, (q1, q2) -> PartialComparator.PartialComparatorOutcome.SAME);
    T1 state1 = firstSolver.init(dummyProblem1, random, executor);
    T2 state2 = secondSolver.init(dummyProblem2, random, executor);
    Individual<G1,S1,Q> representative1 = firstRepresentativeExtractor.apply(state1);
    Individual<G2,S2,Q> representative2 = secondRepresentativeExtractor.apply(state2);
    QualityBasedProblem<S1, Q> problem1 = QualityBasedProblem.create(
        s1 -> problem.qualityFunction().apply(solutionAggregator.apply(s1, representative2.solution())),
        problem.qualityComparator()
    );
    QualityBasedProblem<S2, Q> problem2 = QualityBasedProblem.create(
        s2 -> problem.qualityFunction().apply(solutionAggregator.apply(representative1.solution(), s2)),
        problem.qualityComparator()
    );
    return new CooperativeState<>(
        firstSolver.init(problem1, random, executor),
        secondSolver.init(problem2, random, executor));
  }

}
