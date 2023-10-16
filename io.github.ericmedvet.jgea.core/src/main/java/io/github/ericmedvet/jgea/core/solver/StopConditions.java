
package io.github.ericmedvet.jgea.core.solver;

import io.github.ericmedvet.jgea.core.solver.state.POSetPopulationState;
import io.github.ericmedvet.jgea.core.solver.state.State;
import io.github.ericmedvet.jgea.core.util.Progress;

import java.util.function.Predicate;

public class StopConditions {

  private StopConditions() {
  }

  @SuppressWarnings("unused")
  public static ProgressBasedStopCondition<State> elapsedMillis(final long n) {
    return s -> new Progress(0, n, s.getElapsedMillis());
  }

  @SuppressWarnings("unused")
  public static ProgressBasedStopCondition<POSetPopulationState<?, ?, ?>> nOfBirths(final long n) {
    return s -> new Progress(0, n, s.getNOfBirths());
  }

  @SuppressWarnings("unused")
  public static ProgressBasedStopCondition<POSetPopulationState<?, ?, ?>> nOfFitnessEvaluations(final long n) {
    return s -> new Progress(0, n, s.getNOfFitnessEvaluations());
  }

  @SuppressWarnings("unused")
  public static ProgressBasedStopCondition<State> nOfIterations(final long n) {
    return s -> new Progress(0, n, s.getNOfIterations());
  }

  @SuppressWarnings("unused")
  public static <F extends Comparable<F>> Predicate<POSetPopulationState<?, ?, ? extends F>> targetFitness(final F targetF) {
    return s -> s.getPopulation().firsts().stream().map(Individual::fitness).anyMatch(f -> f.compareTo(targetF) <= 0);
  }
}
