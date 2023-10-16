
package io.github.ericmedvet.jgea.core.solver;

import io.github.ericmedvet.jgea.core.Factory;
import io.github.ericmedvet.jgea.core.order.DAGPartiallyOrderedCollection;
import io.github.ericmedvet.jgea.core.order.PartialComparator;
import io.github.ericmedvet.jgea.core.problem.QualityBasedProblem;
import io.github.ericmedvet.jgea.core.solver.state.POSetPopulationState;
import io.github.ericmedvet.jgea.core.util.Progress;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.random.RandomGenerator;

public abstract class AbstractPopulationBasedIterativeSolver<T extends POSetPopulationState<G, S, Q>,
    P extends QualityBasedProblem<S, Q>, G, S, Q> implements IterativeSolver<T, P, S> {

  protected final Function<? super G, ? extends S> solutionMapper;
  protected final Factory<? extends G> genotypeFactory;
  protected final int populationSize;
  private final Predicate<? super T> stopCondition;

  public AbstractPopulationBasedIterativeSolver(
      Function<? super G, ? extends S> solutionMapper,
      Factory<? extends G> genotypeFactory,
      int populationSize,
      Predicate<? super T> stopCondition
  ) {
    this.solutionMapper = solutionMapper;
    this.genotypeFactory = genotypeFactory;
    this.populationSize = populationSize;
    this.stopCondition = stopCondition;
  }

  protected abstract T initState(P problem, RandomGenerator random, ExecutorService executor);

  private static <T> List<T> getAll(List<Future<T>> futures) throws SolverException {
    List<T> results = new ArrayList<>();
    for (Future<T> future : futures) {
      try {
        results.add(future.get());
      } catch (InterruptedException | ExecutionException e) {
        throw new SolverException(e);
      }
    }
    return results;
  }

  protected static <T extends POSetPopulationState<G, S, F>, G, S, F> List<Individual<G, S, F>> map(
      Collection<? extends G> genotypes,
      Collection<Individual<G, S, F>> individuals,
      Function<? super G, ? extends S> solutionMapper,
      Function<? super S, ? extends F> fitnessFunction,
      ExecutorService executor,
      T state
  ) throws SolverException {
    List<Callable<Individual<G, S, F>>> callables = new ArrayList<>(genotypes.size() + individuals.size());
    callables.addAll(genotypes.stream().map(genotype -> (Callable<Individual<G, S, F>>) () -> {
      S solution = solutionMapper.apply(genotype);
      F fitness = fitnessFunction.apply(solution);
      return new Individual<G, S, F>(genotype, solution, fitness, state.getNOfIterations(), state.getNOfIterations());
    }).toList());
    callables.addAll(individuals.stream().map(individual -> (Callable<Individual<G, S, F>>) () -> {
      S solution = solutionMapper.apply(individual.genotype());
      return new Individual<>(
          individual.genotype(),
          solution,
          fitnessFunction.apply(solution),
          state.getNOfIterations(),
          individual.genotypeBirthIteration()
      );
    }).toList());
    state.incNOfBirths(genotypes.size());
    state.incNOfFitnessEvaluations(genotypes.size() + individuals.size());
    try {
      return getAll(executor.invokeAll(callables));
    } catch (InterruptedException e) {
      throw new SolverException(e);
    }
  }

  protected PartialComparator<Individual<G, S, Q>> comparator(P problem) {
    return (i1, i2) -> problem.qualityComparator().compare(i1.fitness(), i2.fitness());
  }

  @Override
  public Collection<S> extractSolutions(
      P problem, RandomGenerator random, ExecutorService executor, T state
  ) {
    return state.getPopulation().firsts().stream().map(Individual::solution).toList();
  }

  @Override
  public T init(P problem, RandomGenerator random, ExecutorService executor) throws SolverException {
    T state = initState(problem, random, executor);
    state.setPopulation(new DAGPartiallyOrderedCollection<>(map(
        genotypeFactory.build(populationSize, random),
        List.of(),
        solutionMapper,
        problem.qualityFunction(),
        executor,
        state
    ), comparator(problem)));
    return state;
  }

  @Override
  public boolean terminate(
      P problem, RandomGenerator random, ExecutorService executor, T state
  ) {
    if (stopCondition instanceof ProgressBasedStopCondition<?> progressBasedStopCondition) {
      //noinspection unchecked
      Progress progress = ((ProgressBasedStopCondition<T>) progressBasedStopCondition).progress(state);
      state.setProgress(progress);
      return progress.rate() >= 1d;
    }
    return stopCondition.test(state);
  }
}
