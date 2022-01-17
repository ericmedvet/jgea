package it.units.malelab.jgea.core.solver;

import it.units.malelab.jgea.core.listener.Listener;

import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.random.RandomGenerator;

public interface IterativeSolver<T extends Copyable, P extends Problem<S>, S> extends Solver<P, S> {

  Collection<S> extractSolutions(
      P problem, RandomGenerator random, ExecutorService executor, T state
  ) throws SolverException;

  T init(P problem, RandomGenerator random, ExecutorService executor) throws SolverException;

  boolean terminate(
      P problem, RandomGenerator random, ExecutorService executor, T state
  ) throws SolverException;

  void update(P problem, RandomGenerator random, ExecutorService executor, T state) throws SolverException;

  @Override
  default Collection<S> solve(
      P problem, RandomGenerator random, ExecutorService executor
  ) throws SolverException {
    return solve(problem, random, executor, Listener.deaf());
  }

  @SuppressWarnings("unchecked")
  default Collection<S> solve(
      P problem, RandomGenerator random, ExecutorService executor, Listener<? super T> listener
  ) throws SolverException {
    T state = init(problem, random, executor);
    listener.listen(state);
    while (!terminate(problem, random, executor, state)) {
      update(problem, random, executor, state);
      listener.listen((T) state.immutableCopy());
    }
    return extractSolutions(problem, random, executor, state);
  }
}
