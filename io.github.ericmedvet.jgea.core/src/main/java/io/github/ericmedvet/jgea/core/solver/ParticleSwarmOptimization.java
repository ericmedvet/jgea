package io.github.ericmedvet.jgea.core.solver;

import static io.github.ericmedvet.jgea.core.util.VectorUtils.*;

import io.github.ericmedvet.jgea.core.Factory;
import io.github.ericmedvet.jgea.core.order.PartiallyOrderedCollection;
import io.github.ericmedvet.jgea.core.problem.TotalOrderQualityBasedProblem;
import io.github.ericmedvet.jgea.core.util.Progress;
import io.github.ericmedvet.jgea.core.util.VectorUtils;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.random.RandomGenerator;

public class ParticleSwarmOptimization<S, Q> extends AbstractPopulationBasedIterativeSolver<
    ListPopulationState<ParticleSwarmOptimization.PSOIndividual<S, Q>, List<Double>, S, Q>,
    TotalOrderQualityBasedProblem<S, Q>,
    ParticleSwarmOptimization.PSOIndividual<S, Q>,
    List<Double>,
    S,
    Q> {

  public interface PSOIndividual<S, Q> extends Individual<List<Double>, S, Q> {
    List<Double> velocity();

    List<Double> bestKnownPosition();
    Q bestKnownQuality();

    default List<Double> position() {
      return genotype();
    }

    static <S1, Q1> PSOIndividual<S1, Q1> of(
        List<Double> genotype, List<Double> velocity, List<Double> bestKnownPosition, Q1 bestKnownQuality, S1 solution, Q1 quality, long genotypeBirthIteration, long qualityMappingIteration
    ) {
      record HardIndividual<S1, Q1>(List<Double> genotype, List<Double> velocity, List<Double> bestKnownPosition,
                                    Q1 bestKnownQuality, S1 solution, Q1 quality, long genotypeBirthIteration,
                                    long qualityMappingIteration) implements PSOIndividual<S1, Q1> {
      }
      return new HardIndividual<>(genotype, velocity, bestKnownPosition, bestKnownQuality, solution, quality, genotypeBirthIteration, qualityMappingIteration);
    }
  }

  private final int populationSize;
  private final double w; //dumping coefficient
  private final double phiParticle;
  private final double phiGlobal;

  protected record State<S, Q>(
      LocalDateTime startingDateTime,
      long elapsedMillis,
      long nOfIterations,
      Progress progress,
      long nOfBirths,
      long nOfFitnessEvaluations,
      PartiallyOrderedCollection<PSOIndividual<S, Q>> pocPopulation,
      List<PSOIndividual<S, Q>> listPopulation,
      PSOIndividual<S, Q> knownBest
  )
      implements ListPopulationState<PSOIndividual<S, Q>, List<Double>, S, Q> {
    public static <S, Q> State<S, Q> from(
        State<S, Q> state,
        Progress progress,
        int nOfBirths,
        int nOfFitnessEvaluations,
        Collection<PSOIndividual<S, Q>> listPopulation,
        PSOIndividual<S, Q> knownBest,
        Comparator<? super PSOIndividual<S, Q>> comparator) {
      return new State<>(
          state.startingDateTime,
          ChronoUnit.MILLIS.between(state.startingDateTime, LocalDateTime.now()),
          state.nOfIterations() + 1,
          progress,
          state.nOfBirths() + nOfBirths,
          state.nOfFitnessEvaluations() + nOfFitnessEvaluations,
          PartiallyOrderedCollection.from(listPopulation, comparator),
          listPopulation.stream().sorted(comparator).toList(),
          knownBest
      );
    }

    public static <S, Q> State<S, Q> from(
        Collection<PSOIndividual<S, Q>> listPopulation,
        Comparator<? super PSOIndividual<S, Q>> comparator) {
      List<PSOIndividual<S, Q>> list = listPopulation.stream().sorted(comparator).toList();
      return new State<>(
          LocalDateTime.now(),
          0,
          0,
          Progress.NA,
          listPopulation.size(),
          listPopulation.size(),
          PartiallyOrderedCollection.from(listPopulation, comparator),
          listPopulation.stream().sorted(comparator).toList(),
          list.get(0));
    }
  }

  public ParticleSwarmOptimization(Function<? super List<Double>, ? extends S> solutionMapper, Factory<? extends List<Double>> genotypeFactory, Predicate<? super ListPopulationState<PSOIndividual<S, Q>, List<Double>, S, Q>> stopCondition, int populationSize, double w, double phiParticle, double phiGlobal) {
    super(solutionMapper, genotypeFactory, stopCondition, false);
    this.populationSize = populationSize;
    this.w = w;
    this.phiParticle = phiParticle;
    this.phiGlobal = phiGlobal;
  }

  @Override
  protected PSOIndividual<S, Q> newIndividual(List<Double> genotype, ListPopulationState<PSOIndividual<S, Q>, List<Double>, S, Q> state, TotalOrderQualityBasedProblem<S, Q> problem) {
    throw new UnsupportedOperationException("This method should not be called");
  }

  @Override
  protected PSOIndividual<S, Q> updateIndividual(PSOIndividual<S, Q> individual, ListPopulationState<PSOIndividual<S, Q>, List<Double>, S, Q> state, TotalOrderQualityBasedProblem<S, Q> problem) {
    throw new UnsupportedOperationException("This method should not be called");
  }

  @Override
  public ListPopulationState<PSOIndividual<S, Q>, List<Double>, S, Q> init(TotalOrderQualityBasedProblem<S, Q> problem, RandomGenerator random, ExecutorService executor) throws SolverException {
    //init positions
    List<? extends List<Double>> positions = genotypeFactory.build(populationSize, random);
    double min = positions.stream().flatMap(List::stream).mapToDouble(v -> v).min().orElseThrow();
    double max = positions.stream().flatMap(List::stream).mapToDouble(v -> v).max().orElseThrow();
    try {
      Collection<PSOIndividual<S, Q>> individuals = getAll(executor.invokeAll(positions.stream()
          .map(p -> (Callable<PSOIndividual<S, Q>>) () -> {
            S s = solutionMapper.apply(p);
            Q q = problem.qualityFunction().apply(s);
            return PSOIndividual.of(
                p,
                buildList(p.size(), () -> random.nextDouble(-(max - min), max - min)),
                p,
                q,
                s,
                q,
                0,
                0
            );
          })
          .toList()));
      return State.from(individuals, comparator(problem));
    } catch (InterruptedException e) {
      throw new SolverException(e);
    }
  }

  @Override
  public ListPopulationState<PSOIndividual<S, Q>, List<Double>, S, Q> update(TotalOrderQualityBasedProblem<S, Q> problem, RandomGenerator random, ExecutorService executor, ListPopulationState<PSOIndividual<S, Q>, List<Double>, S, Q> state) throws SolverException {
    PSOIndividual<S, Q> knownBest = ((State<S, Q>) state).knownBest();
    List<Double> globalBestPosition = knownBest.position();
    try {
      Collection<PSOIndividual<S, Q>> individuals = getAll(executor.invokeAll(((State<S, Q>) state).listPopulation.stream()
          .map(i -> (Callable<PSOIndividual<S, Q>>) () -> {
            double rParticle = random.nextDouble();
            double rGlobal = random.nextDouble();
            List<Double> vVel = mult(i.velocity(), w);
            List<Double> vParticle = mult(diff(i.bestKnownPosition(), i.position()), rParticle * phiParticle);
            List<Double> vGlobal = mult(diff(globalBestPosition, i.position()), rGlobal * phiGlobal);
            List<Double> newVelocity = sum(vVel, sum(vParticle, vGlobal));// TODO maybe make a sum version with varargs
            List<Double> newPosition = sum(i.position(), newVelocity);
            S newSolution = solutionMapper.apply(newPosition);
            Q newQuality = problem.qualityFunction().apply(newSolution);
            List<Double> newBestKnownPosition = i.bestKnownPosition();
            Q newBestKnownQuality = i.bestKnownQuality();
            if (problem.totalOrderComparator().compare(newQuality,i.quality())<0) {
              newBestKnownPosition = newPosition;
              newBestKnownQuality = newQuality;
            }
            // TODO create the individual
            return null;
          })
          .toList()));
      List<PSOIndividual<S, Q>> sortedIndividuals = individuals.stream().sorted(comparator(problem)).toList();

      if (comparator(problem).compare(sortedIndividuals.get(0), knownBest) < 0) {
        knownBest = sortedIndividuals.get(0);
      }
      return State.from((State<S, Q>) state, progress(state), populationSize, populationSize, sortedIndividuals, knownBest, comparator(problem));
    } catch (InterruptedException e) {
      throw new SolverException(e);
    }
  }
}
