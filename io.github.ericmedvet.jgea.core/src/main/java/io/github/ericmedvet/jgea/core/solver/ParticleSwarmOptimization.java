package io.github.ericmedvet.jgea.core.solver;

import io.github.ericmedvet.jgea.core.Factory;
import io.github.ericmedvet.jgea.core.problem.TotalOrderQualityBasedProblem;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.random.RandomGenerator;

public class ParticleSwarmOptimization<S, Q> extends AbstractPopulationBasedIterativeSolver<
    ListPopulationState<Individual<List<Double>, S, Q>, List<Double>, S, Q>,
    TotalOrderQualityBasedProblem<S, Q>,
    Individual<List<Double>, S, Q>,
    List<Double>,
    S,
    Q> {

  private final int populationSize;
  private final double w; //dumping coefficient
  private final double phiParticle;
  private final double phiGlobal;


  public ParticleSwarmOptimization(Function<? super List<Double>, ? extends S> solutionMapper, Factory<? extends List<Double>> genotypeFactory, Predicate<? super ListPopulationState<Individual<List<Double>, S, Q>, List<Double>, S, Q>> stopCondition, int populationSize, double w, double phiParticle, double phiGlobal) {
    super(solutionMapper, genotypeFactory, stopCondition, false);
    this.populationSize = populationSize;
    this.w = w;
    this.phiParticle = phiParticle;
    this.phiGlobal = phiGlobal;
  }

  @Override
  protected Individual<List<Double>, S, Q> newIndividual(List<Double> genotype, ListPopulationState<Individual<List<Double>, S, Q>, List<Double>, S, Q> state, TotalOrderQualityBasedProblem<S, Q> problem) {
    return null;
  }

  @Override
  protected Individual<List<Double>, S, Q> updateIndividual(Individual<List<Double>, S, Q> individual, ListPopulationState<Individual<List<Double>, S, Q>, List<Double>, S, Q> state, TotalOrderQualityBasedProblem<S, Q> problem) {
    return null;
  }

  @Override
  public ListPopulationState<Individual<List<Double>, S, Q>, List<Double>, S, Q> init(TotalOrderQualityBasedProblem<S, Q> problem, RandomGenerator random, ExecutorService executor) throws SolverException {
    return null;
  }

  @Override
  public ListPopulationState<Individual<List<Double>, S, Q>, List<Double>, S, Q> update(TotalOrderQualityBasedProblem<S, Q> problem, RandomGenerator random, ExecutorService executor, ListPopulationState<Individual<List<Double>, S, Q>, List<Double>, S, Q> state) throws SolverException {
    return null;
  }
}
