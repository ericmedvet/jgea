package io.github.ericmedvet.jgea.core.solver.es;

import io.github.ericmedvet.jgea.core.order.PartiallyOrderedCollection;
import io.github.ericmedvet.jgea.core.problem.TotalOrderQualityBasedProblem;
import io.github.ericmedvet.jgea.core.solver.Individual;
import io.github.ericmedvet.jgea.core.solver.ListPopulationState;
import io.github.ericmedvet.jgea.core.solver.State;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

/**
 * @author "Eric Medvet" on 2024/05/27 for jgea
 */
public interface OpenAIESState<S, Q>
    extends ListPopulationState<
        Individual<List<Double>, S, Q>, List<Double>, S, Q, TotalOrderQualityBasedProblem<S, Q>> {

  double[] center();

  double[] m();

  double[] v();

  static <S, Q> OpenAIESState<S, Q> of(
      LocalDateTime startingDateTime,
      long elapsedMillis,
      long nOfIterations,
      TotalOrderQualityBasedProblem<S, Q> problem,
      Predicate<State<?, ?>> stopCondition,
      long nOfBirths,
      long nOfQualityEvaluations,
      Collection<Individual<List<Double>, S, Q>> listPopulation,
      double[] center,
      double[] m,
      double[] v) {
    record HardState<S, Q>(
        LocalDateTime startingDateTime,
        long elapsedMillis,
        long nOfIterations,
        TotalOrderQualityBasedProblem<S, Q> problem,
        Predicate<State<?, ?>> stopCondition,
        long nOfBirths,
        long nOfQualityEvaluations,
        PartiallyOrderedCollection<Individual<List<Double>, S, Q>> pocPopulation,
        List<Individual<List<Double>, S, Q>> listPopulation,
        double[] center,
        double[] m,
        double[] v)
        implements OpenAIESState<S, Q> {}
    Comparator<Individual<List<Double>, S, Q>> comparator =
        (i1, i2) -> problem.totalOrderComparator().compare(i1.quality(), i2.quality());
    List<Individual<List<Double>, S, Q>> sortedListPopulation =
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
        center,
        m,
        v);
  }

  static <S, Q> OpenAIESState<S, Q> empty(
      TotalOrderQualityBasedProblem<S, Q> problem, Predicate<State<?, ?>> stopCondition, double[] center) {
    return of(
        LocalDateTime.now(),
        0,
        0,
        problem,
        stopCondition,
        0,
        0,
        List.of(),
        center,
        new double[center.length],
        new double[center.length]);
  }

  default OpenAIESState<S, Q> updatedWithIteration(
      Collection<Individual<List<Double>, S, Q>> listPopulation, double[] center, double[] m, double[] v) {
    return of(
        startingDateTime(),
        ChronoUnit.MILLIS.between(LocalDateTime.now(), startingDateTime()),
        nOfIterations() + 1,
        problem(),
        stopCondition(),
        nOfBirths() + listPopulation.size(),
        nOfQualityEvaluations() + listPopulation.size(),
        listPopulation,
        center,
        m,
        v);
  }
}
