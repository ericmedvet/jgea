package io.github.ericmedvet.jgea.core.solver.cooperative;

import io.github.ericmedvet.jgea.core.order.PartialComparator;
import io.github.ericmedvet.jgea.core.order.PartiallyOrderedCollection;
import io.github.ericmedvet.jgea.core.problem.QualityBasedProblem;
import io.github.ericmedvet.jgea.core.solver.Individual;
import io.github.ericmedvet.jgea.core.solver.POCPopulationState;
import io.github.ericmedvet.jgea.core.solver.State;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

/**
 * @author "Eric Medvet" on 2024/05/27 for jgea
 */
public interface CooperativeState<
        T1 extends POCPopulationState<Individual<G1, S1, Q>, G1, S1, Q, P1>,
        T2 extends POCPopulationState<Individual<G2, S2, Q>, G2, S2, Q, P2>,
        G1,
        G2,
        S1,
        S2,
        S,
        Q,
        P1 extends QualityBasedProblem<S1, Q>,
        P2 extends QualityBasedProblem<S2, Q>,
        P extends QualityBasedProblem<S, Q>>
    extends POCPopulationState<Individual<Void, S, Q>, Void, S, Q, P> {

  T1 state1();

  T2 state2();

  static <
          T1 extends POCPopulationState<Individual<G1, S1, Q>, G1, S1, Q, P1>,
          T2 extends POCPopulationState<Individual<G2, S2, Q>, G2, S2, Q, P2>,
          G1,
          G2,
          S1,
          S2,
          S,
          Q,
          P1 extends QualityBasedProblem<S1, Q>,
          P2 extends QualityBasedProblem<S2, Q>,
          P extends QualityBasedProblem<S, Q>>
      CooperativeState<T1, T2, G1, G2, S1, S2, S, Q, P1, P2, P> of(
          LocalDateTime startingDateTime,
          long elapsedMillis,
          long nOfIterations,
          P problem,
          Predicate<State<?, ?>> stopCondition,
          long nOfBirths,
          long nOfQualityEvaluations,
          T1 state1,
          T2 state2,
          Collection<Individual<Void, S, Q>> individuals) {
    record HardState<
            T1 extends POCPopulationState<Individual<G1, S1, Q>, G1, S1, Q, P1>,
            T2 extends POCPopulationState<Individual<G2, S2, Q>, G2, S2, Q, P2>,
            G1,
            G2,
            S1,
            S2,
            S,
            Q,
            P1 extends QualityBasedProblem<S1, Q>,
            P2 extends QualityBasedProblem<S2, Q>,
            P extends QualityBasedProblem<S, Q>>(
        LocalDateTime startingDateTime,
        long elapsedMillis,
        long nOfIterations,
        P problem,
        Predicate<State<?, ?>> stopCondition,
        long nOfBirths,
        long nOfQualityEvaluations,
        PartiallyOrderedCollection<Individual<Void, S, Q>> pocPopulation,
        T1 state1,
        T2 state2)
        implements CooperativeState<T1, T2, G1, G2, S1, S2, S, Q, P1, P2, P> {}
    PartialComparator<? super Individual<Void, S, Q>> comparator =
        (i1, i2) -> problem.qualityComparator().compare(i1.quality(), i2.quality());
    return new HardState<>(
        startingDateTime,
        elapsedMillis,
        nOfIterations,
        problem,
        stopCondition,
        nOfBirths,
        nOfQualityEvaluations,
        PartiallyOrderedCollection.from(individuals, comparator),
        state1,
        state2);
  }

  static <
          T1 extends POCPopulationState<Individual<G1, S1, Q>, G1, S1, Q, P1>,
          T2 extends POCPopulationState<Individual<G2, S2, Q>, G2, S2, Q, P2>,
          G1,
          G2,
          S1,
          S2,
          S,
          Q,
          P1 extends QualityBasedProblem<S1, Q>,
          P2 extends QualityBasedProblem<S2, Q>,
          P extends QualityBasedProblem<S, Q>>
      CooperativeState<T1, T2, G1, G2, S1, S2, S, Q, P1, P2, P> empty(
          P problem, Predicate<State<?, ?>> stopCondition) {
    return of(LocalDateTime.now(), 0, 0, problem, stopCondition, 0, 0, null, null, List.of());
  }

  default CooperativeState<T1, T2, G1, G2, S1, S2, S, Q, P1, P2, P> updatedWithIteration(
      long nOfNewBirths,
      long nOfNewQualityEvaluations,
      T1 state1,
      T2 state2,
      Collection<Individual<Void, S, Q>> individuals) {
    return of(
        startingDateTime(),
        ChronoUnit.MILLIS.between(LocalDateTime.now(), startingDateTime()),
        nOfIterations() + 1,
        problem(),
        stopCondition(),
        nOfBirths() + nOfNewBirths,
        nOfQualityEvaluations() + nOfNewQualityEvaluations,
        state1,
        state2,
        individuals);
  }

  @Override
  default CooperativeState<T1, T2, G1, G2, S1, S2, S, Q, P1, P2, P> updatedWithProblem(P problem) {
    return of(
        startingDateTime(),
        elapsedMillis(),
        nOfIterations(),
        problem,
        stopCondition(),
        nOfBirths(),
        nOfQualityEvaluations(),
        state1(),
        state2(),
        pocPopulation().all());
  }
}
