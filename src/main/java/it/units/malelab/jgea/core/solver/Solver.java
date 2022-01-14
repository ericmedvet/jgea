package it.units.malelab.jgea.core.solver;

import it.units.malelab.jgea.core.Problem;

import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.random.RandomGenerator;

public interface Solver<S, F> {
  Collection<S> solve(
      Problem<S, F> problem,
      RandomGenerator random,
      ExecutorService executor
  ) throws SolverException;
}
