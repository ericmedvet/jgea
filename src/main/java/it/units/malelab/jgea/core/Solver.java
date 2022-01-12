package it.units.malelab.jgea.core;

import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.random.RandomGenerator;

public interface Solver<S, F> {
  Collection<S> solve(
      Problem<S, F> problem,
      RandomGenerator random,
      ExecutorService executor
  ) throws InterruptedException, ExecutionException;
}
