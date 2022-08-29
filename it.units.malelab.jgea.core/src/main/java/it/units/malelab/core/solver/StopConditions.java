package it.units.malelab.core.solver;

import it.units.malelab.core.solver.state.POSetPopulationState;
import it.units.malelab.core.solver.state.State;

import java.util.function.Predicate;

public class StopConditions {

  private StopConditions() {
  }

  public static Predicate<State> elapsedMillis(final long n) {
    return s -> s.getElapsedMillis() >= n;
  }

  public static Predicate<POSetPopulationState<?, ?, ?>> nOfBirths(final long n) {
    return s -> s.getNOfBirths() >= n;
  }

  public static Predicate<POSetPopulationState<?, ?, ?>> nOfFitnessEvaluations(final long n) {
    return s -> s.getNOfFitnessEvaluations() >= n;
  }

  public static Predicate<State> nOfIterations(final long n) {
    return s -> s.getNOfIterations() >= n;
  }

  public static <F extends Comparable<F>> Predicate<POSetPopulationState<?, ?, ? extends F>> targetFitness(final F targetF) {
    return s -> s.getPopulation().firsts().stream().map(Individual::fitness).anyMatch(f -> f.compareTo(targetF) <= 0);
  }
}
