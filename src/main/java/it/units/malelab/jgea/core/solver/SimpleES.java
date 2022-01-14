package it.units.malelab.jgea.core.solver;

import it.units.malelab.jgea.core.Factory;
import it.units.malelab.jgea.core.Problem;
import it.units.malelab.jgea.core.order.PartialComparator;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.random.RandomGenerator;

public class SimpleES<S, F extends Comparable<F>> extends AbstractPopulationBasedSolver<AbstractPopulationBasedSolver.State<List<Double>, S, F>, List<Double>, S, F> {

  private final double parentRatio;
  private final double sigma;

  public SimpleES(
      Function<? super List<Double>, ? extends S> solutionMapper,
      Factory<? extends List<Double>> genotypeFactory,
      Predicate<? super State<List<Double>, S, F>> stopCondition,
      int populationSize,
      double parentRatio,
      double sigma
  ) {
    //noinspection unchecked
    super(
        solutionMapper,
        genotypeFactory,
        populationSize,
        PartialComparator.from(Comparable.class).comparing(Individual::fitness),
        stopCondition
    );
    this.parentRatio = parentRatio;
    this.sigma = sigma;
  }

  @Override
  protected State<List<Double>, S, F> initState(
      Problem<S, F> problem, RandomGenerator random, ExecutorService executor
  ) {
    return new State<>(LocalDateTime.now());
  }

  @Override
  public void update(
      Problem<S, F> problem, RandomGenerator random, ExecutorService executor, State<List<Double>, S, F> state
  ) throws SolverException {
    // TODO do the stuff here
    state.updateElapsedMillis();
  }
}
