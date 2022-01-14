package it.units.malelab.jgea.core.solver;

import it.units.malelab.jgea.core.Factory;
import it.units.malelab.jgea.core.Problem;
import it.units.malelab.jgea.core.order.DAGPartiallyOrderedCollection;
import it.units.malelab.jgea.core.order.PartialComparator;
import it.units.malelab.jgea.core.order.PartiallyOrderedCollection;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
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

public abstract class AbstractPopulationBasedSolver<T extends AbstractPopulationBasedSolver.State<G, S, F>, G, S, F> implements IterativeSolver<T, S, F> {

  protected final Function<? super G, ? extends S> solutionMapper;
  protected final Factory<? extends G> genotypeFactory;
  protected final PartialComparator<? super Individual<G, S, F>> individualComparator;
  protected final int populationSize;
  private final Predicate<? super State<G, S, F>> stopCondition;

  public AbstractPopulationBasedSolver(
      Function<? super G, ? extends S> solutionMapper,
      Factory<? extends G> genotypeFactory,
      int populationSize,
      PartialComparator<? super Individual<G, S, F>> individualComparator,
      Predicate<? super State<G, S, F>> stopCondition
  ) {
    this.solutionMapper = solutionMapper;
    this.genotypeFactory = genotypeFactory;
    this.populationSize = populationSize;
    this.individualComparator = individualComparator;
    this.stopCondition = stopCondition;
  }

  public static final class State<G, S, F> implements BaseState, POSetPopulationState<G, S, F> {
    private final LocalDateTime startingDateTime;
    private long nOfBirths = 0;
    private long nOfFitnessEvaluations = 0;
    private long nOfIterations = 0;
    private long elapsedMillis = 0;
    private PartiallyOrderedCollection<Individual<G, S, F>> population;

    public State(LocalDateTime startingDate) {
      this.startingDateTime = startingDate;
    }

    @Override
    public long getElapsedMillis() {
      return elapsedMillis;
    }

    @Override
    public long getNOfBirths() {
      return nOfBirths;
    }

    @Override
    public long getNOfFitnessEvaluations() {
      return nOfFitnessEvaluations;
    }

    public void setNOfFitnessEvaluations(long n) {
      nOfFitnessEvaluations = nOfFitnessEvaluations + n;
    }

    @Override
    public long getNOfIterations() {
      return nOfIterations;
    }

    @Override
    public PartiallyOrderedCollection<Individual<G, S, F>> getPopulation() {
      return population;
    }

    public void setPopulation(PartiallyOrderedCollection<Individual<G, S, F>> population) {
      this.population = population;
    }

    public void incNOfBirths(long n) {
      nOfBirths = nOfBirths + n;
    }

    public void incNOfIterations(long n) {
      nOfIterations = nOfIterations + n;
    }

    public void updateElapsedMillis() {
      elapsedMillis = ChronoUnit.MILLIS.between(LocalDateTime.now(), startingDateTime);
    }
  }

  protected abstract T initState(Problem<S, F> problem, RandomGenerator random, ExecutorService executor);

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

  protected static <T extends AbstractPopulationBasedSolver.State<G, S, F>, G, S, F> List<Individual<G, S, F>> map(
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
      F fitness = fitnessFunction.apply(solution);
      return new Individual<>(
          individual.genotype(),
          solution,
          fitness,
          state.getNOfIterations(),
          individual.genotypeBirthIteration()
      );
    }).toList());
    state.incNOfBirths(genotypes.size());
    state.incNOfIterations(genotypes.size() + individuals.size());
    try {
      return getAll(executor.invokeAll(callables));
    } catch (InterruptedException e) {
      throw new SolverException(e);
    }
  }

  @Override
  public Collection<S> extractSolutions(
      Problem<S, F> problem, RandomGenerator random, ExecutorService executor, T state
  ) {
    return state.getBestIndividuals().stream().map(Individual::solution).toList();
  }

  @Override
  public T init(Problem<S, F> problem, RandomGenerator random, ExecutorService executor) throws SolverException {
    T state = initState(problem, random, executor);
    state.setPopulation(new DAGPartiallyOrderedCollection<>(
        map(genotypeFactory.build(populationSize, random), List.of(), solutionMapper, problem, executor, state),
        individualComparator
    ));
    return state;
  }

  @Override
  public boolean terminate(
      Problem<S, F> problem, RandomGenerator random, ExecutorService executor, T state
  ) {
    return stopCondition.test(state);
  }
}
