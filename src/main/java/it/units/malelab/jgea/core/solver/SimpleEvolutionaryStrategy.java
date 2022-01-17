package it.units.malelab.jgea.core.solver;

import it.units.malelab.jgea.core.Factory;
import it.units.malelab.jgea.core.order.DAGPartiallyOrderedCollection;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.random.RandomGenerator;
import java.util.stream.IntStream;

public class SimpleEvolutionaryStrategy<S, Q> extends AbstractPopulationIterativeBasedSolver<SimpleEvolutionaryStrategy.State<S, Q>, TotalOrderQualityBasedProblem<S, Q>, List<Double>, S, Q> {

  private final int nOfParents;
  private final double sigma;

  public SimpleEvolutionaryStrategy(
      Function<? super List<Double>, ? extends S> solutionMapper,
      Factory<? extends List<Double>> genotypeFactory,
      int populationSize,
      Predicate<? super State<S, Q>> stopCondition,
      int nOfParents,
      double sigma
  ) {
    super(solutionMapper, genotypeFactory, populationSize, stopCondition);
    this.nOfParents = nOfParents;
    this.sigma = sigma;
  }

  public static class State<S, Q> extends AbstractPopulationIterativeBasedSolver.State<List<Double>, S, Q> {
    private double[] means;

    public State(LocalDateTime startingDate) {
      super(startingDate);
    }

    public double[] getMeans() {
      return means;
    }

    public void setMeans(double[] means) {
      this.means = means;
    }
  }

  @Override
  protected SimpleEvolutionaryStrategy.State<S, Q> initState(
      TotalOrderQualityBasedProblem<S, Q> problem, RandomGenerator random, ExecutorService executor
  ) {
    return new SimpleEvolutionaryStrategy.State<>(LocalDateTime.now());
  }

  @Override
  public void update(
      TotalOrderQualityBasedProblem<S, Q> problem,
      RandomGenerator random,
      ExecutorService executor,
      SimpleEvolutionaryStrategy.State<S, Q> state
  ) throws SolverException {
    //select parents
    List<Individual<List<Double>, S, Q>> parents = state.getAllIndividuals()
        .stream()
        .sorted((i1, i2) -> problem.totalOrderComparator().compare(i1.fitness(), i2.fitness()))
        .limit(Math.round(nOfParents))
        .toList();
    //compute mean
    if (parents.stream().map(i -> i.genotype().size()).distinct().count() > 1) {
      throw new IllegalStateException(String.format(
          "Genotype size should be the same for all parents: found different sizes %s",
          parents.stream().map(i -> i.genotype().size()).distinct().toList()
      ));
    }
    int l = parents.get(0).genotype().size();
    final double[] sums = new double[l];
    parents.forEach(i -> IntStream.range(0, l).forEach(j -> sums[j] = sums[j] + i.genotype().get(j)));
    double[] means = Arrays.stream(sums).map(v -> v / (double) parents.size()).toArray();
    state.setMeans(means);
    //generate offspring
    List<List<Double>> offspringGenotypes = new ArrayList<>();
    while (offspringGenotypes.size() < populationSize) {
      offspringGenotypes.add(Arrays.stream(means).map(m -> m + random.nextGaussian() * sigma).boxed().toList());
    }
    List<Individual<List<Double>, S, Q>> offspring = map(
        offspringGenotypes,
        List.of(),
        solutionMapper,
        problem.qualityMapper(),
        executor,
        state
    );
    //update state
    state.setPopulation(new DAGPartiallyOrderedCollection<>(
        offspring,
        (i1, i2) -> compare(i1, i2, problem)
    ));
    state.incNOfIterations();
    state.updateElapsedMillis();
  }

}
