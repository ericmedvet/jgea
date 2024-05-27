package io.github.ericmedvet.jgea.core.solver.es;

import io.github.ericmedvet.jgea.core.order.PartiallyOrderedCollection;
import io.github.ericmedvet.jgea.core.problem.TotalOrderQualityBasedProblem;
import io.github.ericmedvet.jgea.core.solver.ListPopulationState;
import io.github.ericmedvet.jgea.core.solver.State;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

public interface CMAESState<S, Q> extends ListPopulationState<
    CMAESIndividual<S, Q>, List<Double>, S, Q, TotalOrderQualityBasedProblem<S, Q>> {

  RealMatrix B();

  RealMatrix C();

  RealMatrix D();

  double[] cEvolutionPath();

  long lastEigenUpdateIteration();

  double[] means();

  double[] sEvolutionPath();

  double sigma();

  static <S, Q> CMAESState<S, Q> empty(
      TotalOrderQualityBasedProblem<S, Q> problem,
      Predicate<State<?, ?>> stopCondition,
      double[] means
  ) {
    return of(
        LocalDateTime.now(), 0, 0, problem, stopCondition, 0, 0, List.of(),
        means,
        0.5,
        new double[means.length],
        new double[means.length],
        MatrixUtils.createRealIdentityMatrix(means.length),
        MatrixUtils.createRealIdentityMatrix(means.length),
        MatrixUtils.createRealIdentityMatrix(means.length),
        0
    );
  }

  static <S, Q> CMAESState<S, Q> of(
      LocalDateTime startingDateTime,
      long elapsedMillis,
      long nOfIterations,
      TotalOrderQualityBasedProblem<S, Q> problem,
      Predicate<State<?, ?>> stopCondition,
      long nOfBirths,
      long nOfQualityEvaluations,
      Collection<CMAESIndividual<S, Q>> listPopulation,
      double[] means,
      double sigma,
      double[] sEvolutionPath,
      double[] cEvolutionPath,
      RealMatrix B,
      RealMatrix C,
      RealMatrix D,
      long lastEigenUpdateIteration
  ) {
    record HardState<S, Q>(
        LocalDateTime startingDateTime,
        long elapsedMillis,
        long nOfIterations,
        TotalOrderQualityBasedProblem<S, Q> problem,
        Predicate<State<?, ?>> stopCondition,
        long nOfBirths,
        long nOfQualityEvaluations,
        PartiallyOrderedCollection<CMAESIndividual<S, Q>> pocPopulation,
        List<CMAESIndividual<S, Q>> listPopulation,
        double[] means,
        double sigma,
        double[] sEvolutionPath,
        double[] cEvolutionPath,
        RealMatrix B,
        RealMatrix C,
        RealMatrix D,
        long lastEigenUpdateIteration
    ) implements CMAESState<S, Q> {}
    Comparator<CMAESIndividual<S, Q>> comparator = (i1, i2) -> problem.totalOrderComparator().compare(
        i1.quality(),
        i2.quality()
    );
    List<CMAESIndividual<S, Q>> sortedListPopulation =
        listPopulation.stream().sorted(comparator).toList();
    return new HardState<>(
        startingDateTime,
        elapsedMillis,
        nOfIterations,
        problem,
        stopCondition,
        nOfBirths,
        nOfQualityEvaluations,
        PartiallyOrderedCollection.from(listPopulation, comparator),
        sortedListPopulation,
        means,
        sigma,
        sEvolutionPath,
        cEvolutionPath,
        B,
        C,
        D,
        lastEigenUpdateIteration
    );
  }

  default CMAESState<S,Q> updatedWithIteration(
      Collection<CMAESIndividual<S, Q>> listPopulation
  ) {
    return of(
        startingDateTime(),
        ChronoUnit.MILLIS.between(LocalDateTime.now(), startingDateTime()),
        nOfIterations()+1,
        problem(),
        stopCondition(),
        nOfBirths()+listPopulation.size(),
        nOfQualityEvaluations()+listPopulation.size(),
        listPopulation,
        means(),
        sigma(),
        sEvolutionPath(),
        cEvolutionPath(),
        B(),
        C(),
        D(),
        lastEigenUpdateIteration()
    );
  }

  default CMAESState<S, Q> updatedWithMatrices(
      RealMatrix B, RealMatrix D
  ) {
    return of(
        startingDateTime(),
        elapsedMillis(),
        nOfIterations(),
        problem(),
        stopCondition(),
        nOfBirths(),
        nOfQualityEvaluations(),
        listPopulation(),
        means(),
        sigma(),
        sEvolutionPath(),
        cEvolutionPath(),
        B,
        C(),
        D,
        nOfIterations()
    );
  }

  default CMAESState<S, Q> updatedWithPaths(
      double[] means,
      double sigma,
      double[] sEvolutionPath,
      double[] cEvolutionPath,
      RealMatrix C
  ) {
    return of(
        startingDateTime(),
        elapsedMillis(),
        nOfIterations(),
        problem(),
        stopCondition(),
        nOfBirths(),
        nOfQualityEvaluations(),
        listPopulation(),
        means,
        sigma,
        sEvolutionPath,
        cEvolutionPath,
        B(),
        C,
        D(),
        lastEigenUpdateIteration()
    );
  }
}
