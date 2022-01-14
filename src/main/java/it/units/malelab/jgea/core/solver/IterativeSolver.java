package it.units.malelab.jgea.core.solver;

import it.units.malelab.jgea.core.Problem;
import it.units.malelab.jgea.core.listener.Listener;

import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.random.RandomGenerator;

public interface IterativeSolver<T, S, F> extends Solver<S, F> {

  Collection<S> extractSolutions(
      Problem<S, F> problem,
      RandomGenerator random,
      ExecutorService executor,
      T state
  ) throws SolverException;

  T init(Problem<S, F> problem, RandomGenerator random, ExecutorService executor) throws SolverException;

  boolean terminate(
      Problem<S, F> problem,
      RandomGenerator random,
      ExecutorService executor,
      T state
  ) throws SolverException;

  void update(Problem<S, F> problem, RandomGenerator random, ExecutorService executor, T state) throws SolverException;

  @Override
  default Collection<S> solve(
      Problem<S, F> problem, RandomGenerator random, ExecutorService executor
  ) throws SolverException {
    return solve(problem, random, executor, Listener.deaf());
  }

  default Collection<S> solve(
      Problem<S, F> problem, RandomGenerator random, ExecutorService executor, Listener<? super T> listener
  ) throws SolverException {
    T state = init(problem, random, executor);
    listener.listen(state);
    while (!terminate(problem, random, executor, state)) {
      update(problem, random, executor, state);
      listener.listen(state);
    }
    return extractSolutions(problem, random, executor, state);
  }
}
