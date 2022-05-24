package it.units.malelab.jgea.core.solver.coevolution;

import it.units.malelab.jgea.core.QualityBasedProblem;
import it.units.malelab.jgea.core.order.DAGPartiallyOrderedCollection;
import it.units.malelab.jgea.core.order.PartialComparator;
import it.units.malelab.jgea.core.order.PartiallyOrderedCollection;
import it.units.malelab.jgea.core.solver.AbstractPopulationBasedIterativeSolver;
import it.units.malelab.jgea.core.solver.Individual;
import it.units.malelab.jgea.core.solver.SolverException;
import it.units.malelab.jgea.core.solver.state.POSetPopulationState;
import it.units.malelab.jgea.core.util.Misc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.random.RandomGenerator;
import java.util.stream.IntStream;

public class CooperativeSolver<T1 extends POSetPopulationState<G1, S1, Q>, T2 extends POSetPopulationState<G2, S2, Q>,
    G1, G2, S1, S2, P extends QualityBasedProblem<S, Q>, S, Q> extends
    AbstractPopulationBasedIterativeSolver<CooperativeSolver.State<T1, T2, G1, G2, S1, S2, S, Q>, P, Void, S, Q> {

  private final AbstractPopulationBasedIterativeSolver<T1, QualityBasedProblem<S1, Q>, G1, S1, Q> solver1;
  private final AbstractPopulationBasedIterativeSolver<T2, QualityBasedProblem<S2, Q>, G2, S2, Q> solver2;
  private final BiFunction<S1, S2, S> solutionAggregator;
  private final CollaboratorSelector<Individual<G1, S1, Q>> extractor1;
  private final CollaboratorSelector<Individual<G2, S2, Q>> extractor2;
  private final Function<Collection<Q>, Q> qualityAggregator;

  public CooperativeSolver(AbstractPopulationBasedIterativeSolver<T1, QualityBasedProblem<S1, Q>, G1, S1, Q> solver1,
                           AbstractPopulationBasedIterativeSolver<T2, QualityBasedProblem<S2, Q>, G2, S2, Q> solver2,
                           BiFunction<S1, S2, S> solutionAggregator,
                           CollaboratorSelector<Individual<G1, S1, Q>> extractor1,
                           CollaboratorSelector<Individual<G2, S2, Q>> extractor2,
                           Function<Collection<Q>, Q> qualityAggregator,
                           Predicate<? super State<T1, T2, G1, G2, S1, S2, S, Q>> stopCondition
  ) {
    super(null, null, 0, stopCondition);
    this.solver1 = solver1;
    this.solver2 = solver2;
    this.solutionAggregator = solutionAggregator;
    this.extractor1 = extractor1;
    this.extractor2 = extractor2;
    this.qualityAggregator = qualityAggregator;
  }

  public static class State<T1 extends POSetPopulationState<G1, S1, Q>, T2 extends POSetPopulationState<G2, S2, Q>,
      G1, G2, S1, S2, S, Q> extends POSetPopulationState<Void, S, Q> {
    private final T1 state1;
    private final T2 state2;

    public State(LocalDateTime startingDateTime, long elapsedMillis, long nOfIterations, long nOfBirths,
                 long nOfFitnessEvaluations,
                 PartiallyOrderedCollection<Individual<Void, S, Q>> population,
                 T1 state1, T2 state2) {
      super(startingDateTime, elapsedMillis, nOfIterations, nOfBirths, nOfFitnessEvaluations, population);
      this.state1 = state1;
      this.state2 = state2;
    }

    public State(T1 state1, T2 state2) {
      this.state1 = state1;
      this.state2 = state2;
    }

    public Individual<G1, S1, Q> best1() {
      return Misc.first(state1.getPopulation().firsts());
    }

    public Individual<G2, S2, Q> best2() {
      return Misc.first(state2.getPopulation().firsts());
    }

    @Override
    public long getNOfBirths() {
      return state1.getNOfBirths() + state2.getNOfBirths();
    }

    @Override
    @SuppressWarnings("unchecked")
    public State<T1, T2, G1, G2, S1, S2, S, Q> immutableCopy() {
      return new State<>(startingDateTime, elapsedMillis, nOfIterations,
          getNOfBirths(), getNOfFitnessEvaluations(), population,
          (T1) state1.immutableCopy(), (T2) state2.immutableCopy());
    }

  }

  @Override
  protected State<T1, T2, G1, G2, S1, S2, S, Q> initState(P problem, RandomGenerator random, ExecutorService executor) {
    return null;
  }

  @Override
  public State<T1, T2, G1, G2, S1, S2, S, Q> init(P problem, RandomGenerator random, ExecutorService executor)
      throws SolverException {
    QualityBasedProblem<S1, Q> dummyProblem1 = QualityBasedProblem.create(
        s1 -> null,
        (q1, q2) -> PartialComparator.PartialComparatorOutcome.SAME
    );
    QualityBasedProblem<S2, Q> dummyProblem2 = QualityBasedProblem.create(
        s2 -> null,
        (q1, q2) -> PartialComparator.PartialComparatorOutcome.SAME
    );
    Collection<Individual<G1, S1, Q>> representatives1 = extractor1
        .select(solver1.init(dummyProblem1, random, executor).getPopulation(), random);
    Collection<Individual<G2, S2, Q>> representatives2 = extractor2
        .select(solver2.init(dummyProblem2, random, executor).getPopulation(), random);
    Collection<Individual<Void, S, Q>> evaluatedIndividuals = Collections.synchronizedCollection(new ArrayList<>());
    QualityBasedProblem<S1, Q> problem1 = QualityBasedProblem.create(
        s1 -> {
          List<S> solutions = representatives2.stream().map(s2 -> solutionAggregator.apply(s1, s2.solution())).toList();
          List<Q> qualities = solutions.stream().map(s -> problem.qualityFunction().apply(s)).toList();
          IntStream.range(0, solutions.size()).forEach(i ->
              evaluatedIndividuals.add(
                  new Individual<>(null, solutions.get(i), qualities.get(i), 0, 0))
          );
          return qualityAggregator.apply(qualities);
        },
        problem.qualityComparator()
    );
    QualityBasedProblem<S2, Q> problem2 = QualityBasedProblem.create(
        s2 -> {
          List<S> solutions = representatives1.stream().map(s1 -> solutionAggregator.apply(s1.solution(), s2)).toList();
          List<Q> qualities = solutions.stream().map(s -> problem.qualityFunction().apply(s)).toList();
          IntStream.range(0, solutions.size()).forEach(i ->
              evaluatedIndividuals.add(
                  new Individual<>(null, solutions.get(i), qualities.get(i), 0, 0))
          );
          return qualityAggregator.apply(qualities);
        },
        problem.qualityComparator()
    );
    T1 state1 = solver1.init(problem1, random, executor);
    T2 state2 = solver2.init(problem2, random, executor);
    State<T1, T2, G1, G2, S1, S2, S, Q> state = new State<>(state1, state2);
    state.incNOfFitnessEvaluations(evaluatedIndividuals.size());
    state.setPopulation(new DAGPartiallyOrderedCollection<>(evaluatedIndividuals, comparator(problem)));
    return state;
  }

  // TODO fix fitness mapping iteration and genotype birth iteration
  @Override
  public void update(P problem, RandomGenerator random, ExecutorService executor, State<T1, T2, G1, G2, S1, S2, S, Q> state) throws SolverException {
    Collection<Individual<G1, S1, Q>> representatives1 = extractor1.select(state.state1.getPopulation(), random);
    Collection<Individual<G2, S2, Q>> representatives2 = extractor2.select(state.state2.getPopulation(), random);
    Collection<Individual<Void, S, Q>> evaluatedIndividuals = Collections.synchronizedCollection(new ArrayList<>());
    QualityBasedProblem<S1, Q> problem1 = QualityBasedProblem.create(
        s1 -> {
          List<S> solutions = representatives2.stream()
              .map(s2 -> solutionAggregator.apply(s1, s2.solution())).toList();
          List<Q> qualities = solutions.stream()
              .map(s -> problem.qualityFunction().apply(s)).toList();
          IntStream.range(0, solutions.size()).forEach(i ->
              evaluatedIndividuals.add(
                  new Individual<>(null, solutions.get(i), qualities.get(i), 0, 0))
          );
          return qualityAggregator.apply(qualities);
        },
        problem.qualityComparator()
    );
    QualityBasedProblem<S2, Q> problem2 = QualityBasedProblem.create(
        s2 -> {
          List<S> solutions = representatives1.stream()
              .map(s1 -> solutionAggregator.apply(s1.solution(), s2)).toList();
          List<Q> qualities = solutions.stream()
              .map(s -> problem.qualityFunction().apply(s)).toList();
          IntStream.range(0, solutions.size()).forEach(i ->
              evaluatedIndividuals.add(
                  new Individual<>(null, solutions.get(i), qualities.get(i), 0, 0))
          );
          return qualityAggregator.apply(qualities);
        },
        problem.qualityComparator()
    );
    solver1.update(problem1, random, executor, state.state1);
    solver2.update(problem2, random, executor, state.state2);
    state.setPopulation(new DAGPartiallyOrderedCollection<>(evaluatedIndividuals, comparator(problem)));
    state.incNOfFitnessEvaluations(evaluatedIndividuals.size());
    state.incNOfIterations();
    state.updateElapsedMillis();
  }

}
