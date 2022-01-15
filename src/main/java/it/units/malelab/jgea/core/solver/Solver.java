package it.units.malelab.jgea.core.solver;

import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.random.RandomGenerator;

public interface Solver<P extends Problem<S>, S> {
  Collection<S> solve(
      P problem,
      RandomGenerator random,
      ExecutorService executor
  ) throws SolverException;
}
