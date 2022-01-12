package it.units.malelab.jgea.core;

import it.units.malelab.jgea.core.listener.Listener;

import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.random.RandomGenerator;

public interface IterativeSolver<T extends IterativeSolver.State<S, F>, S, F> extends Solver<S, F> {

  class State<S, F> {}

  Collection<S> extractSolutions(T state);

  T init();

  boolean terminate(Problem<S, F> problem, T state);

  void update(T state);

  @Override
  default Collection<S> solve(
      Problem<S, F> problem, RandomGenerator random, ExecutorService executor
  ) throws InterruptedException, ExecutionException {
    return solve(problem, random, executor, Listener.deaf());
  }

  default Collection<S> solve(
      Problem<S, F> problem, RandomGenerator random, ExecutorService executor, Listener<? extends T> listener
  ) throws InterruptedException, ExecutionException {
    T state = init();
    while (!terminate(problem, state)) {
      update(state);
    }
    return extractSolutions(state);
  }
}
