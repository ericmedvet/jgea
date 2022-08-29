package it.units.malelab.core.solver;

import it.units.malelab.core.Problem;
import it.units.malelab.core.listener.Listener;
import it.units.malelab.core.util.Copyable;

import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
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
    listener.done();
    return extractSolutions(problem, random, executor, state);
  }

  default <P2 extends Problem<S>> IterativeSolver<T, P2, S> with(Function<P2, P> problemTransformer) {
    IterativeSolver<T, P, S> thisIterativeSolver = this;
    return new IterativeSolver<>() {
      @Override
      public Collection<S> extractSolutions(
          P2 problem, RandomGenerator random, ExecutorService executor, T state
      ) throws SolverException {
        return thisIterativeSolver.extractSolutions(problemTransformer.apply(problem), random, executor, state);
      }

      @Override
      public T init(P2 problem, RandomGenerator random, ExecutorService executor) throws SolverException {
        return thisIterativeSolver.init(problemTransformer.apply(problem), random, executor);
      }

      @Override
      public boolean terminate(
          P2 problem, RandomGenerator random, ExecutorService executor, T state
      ) throws SolverException {
        return thisIterativeSolver.terminate(problemTransformer.apply(problem), random, executor, state);
      }

      @Override
      public void update(P2 problem, RandomGenerator random, ExecutorService executor, T state) throws SolverException {
        thisIterativeSolver.update(problemTransformer.apply(problem), random, executor, state);
      }
    };
  }
}
