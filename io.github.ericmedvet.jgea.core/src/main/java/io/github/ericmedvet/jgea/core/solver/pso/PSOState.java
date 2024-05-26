package io.github.ericmedvet.jgea.core.solver.pso;

import io.github.ericmedvet.jgea.core.order.PartiallyOrderedCollection;
import io.github.ericmedvet.jgea.core.problem.TotalOrderQualityBasedProblem;
import io.github.ericmedvet.jgea.core.solver.ListPopulationState;
import io.github.ericmedvet.jgea.core.solver.State;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

public interface PSOState<S, Q>
    extends ListPopulationState<PSOIndividual<S, Q>, List<Double>, S, Q, TotalOrderQualityBasedProblem<S, Q>> {
  PSOIndividual<S, Q> knownBest();

  static <S, Q> PSOState<S, Q> empty(
      TotalOrderQualityBasedProblem<S, Q> problem,
      Predicate<State<?, ?>> stopCondition,
      Comparator<? super PSOIndividual<S, Q>> comparator) {
    return of(LocalDateTime.now(), 0, 0, problem, stopCondition, 0, 0, List.of(), comparator, null);
  }

  static <S, Q> PSOState<S, Q> of(
      LocalDateTime startingDateTime,
      long elapsedMillis,
      long nOfIterations,
      TotalOrderQualityBasedProblem<S, Q> problem,
      Predicate<State<?, ?>> stopCondition,
      long nOfBirths,
      long nOfQualityEvaluations,
      Collection<PSOIndividual<S, Q>> listPopulation,
      Comparator<? super PSOIndividual<S, Q>> comparator,
      PSOIndividual<S, Q> knownBest) {
    record HardState<S, Q>(
        LocalDateTime startingDateTime,
        long elapsedMillis,
        long nOfIterations,
        TotalOrderQualityBasedProblem<S, Q> problem,
        Predicate<State<?, ?>> stopCondition,
        long nOfBirths,
        long nOfQualityEvaluations,
        PartiallyOrderedCollection<PSOIndividual<S, Q>> pocPopulation,
        List<PSOIndividual<S, Q>> listPopulation,
        Comparator<? super PSOIndividual<S, Q>> comparator,
        PSOIndividual<S, Q> knownBest)
        implements PSOState<S, Q> {}
    List<PSOIndividual<S, Q>> sortedListPopulation =
        listPopulation.stream().sorted(comparator).toList();
    return new HardState<>(
        startingDateTime,
        elapsedMillis,
        nOfIterations,
        problem,
        stopCondition,
        nOfBirths,
        nOfQualityEvaluations,
        PartiallyOrderedCollection.from(sortedListPopulation, comparator),
        sortedListPopulation,
        comparator,
        knownBest);
  }

  default PSOState<S, Q> updated(
      long nOfNewBirths,
      long nOfNewQualityEvaluations,
      Collection<PSOIndividual<S, Q>> listPopulation,
      PSOIndividual<S, Q> knownBest) {
    return of(
        startingDateTime(),
        ChronoUnit.MILLIS.between(LocalDateTime.now(), startingDateTime()),
        nOfIterations() + 1,
        problem(),
        stopCondition(),
        nOfBirths() + nOfNewBirths,
        nOfQualityEvaluations() + nOfNewQualityEvaluations,
        listPopulation,
        comparator(),
        knownBest);
  }
}
