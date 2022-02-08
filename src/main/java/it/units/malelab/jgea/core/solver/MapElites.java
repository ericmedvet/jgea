package it.units.malelab.jgea.core.solver;

import it.units.malelab.jgea.core.Factory;
import it.units.malelab.jgea.core.QualityBasedProblem;
import it.units.malelab.jgea.core.operator.Mutation;
import it.units.malelab.jgea.core.order.DAGPartiallyOrderedCollection;
import it.units.malelab.jgea.core.order.MapOfElites;
import it.units.malelab.jgea.core.order.PartialComparator;
import it.units.malelab.jgea.core.order.PartiallyOrderedCollection;
import it.units.malelab.jgea.core.solver.state.POSetPopulationState;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.random.RandomGenerator;
import java.util.stream.IntStream;

/**
 * @author Giorgia
 */
public class MapElites<G, P extends QualityBasedProblem<S, Q>, S, Q> extends AbstractPopulationIterativeBasedSolver<MapElites.State<G, S, Q>, P, G, S, Q> {

  private final int batchSize;
  private final Mutation<G> mutation;

  private final Function<Individual<G, S, Q>, List<Double>> featuresExtractor;
  private final List<MapOfElites.Feature> features;

  public MapElites(
      Function<? super G, ? extends S> solutionMapper,
      Factory<? extends G> genotypeFactory,
      int populationSize,
      int batchSize,
      Predicate<? super MapElites.State<G, S, Q>> stopCondition,
      Mutation<G> mutation,
      Function<Individual<G, S, Q>, List<Double>> featuresExtractor,
      List<Integer> featuresSizes,
      List<Double> featuresMinValues,
      List<Double> featuresMaxValues
  ) {
    super(solutionMapper, genotypeFactory, populationSize, stopCondition);
    this.batchSize = batchSize;
    this.mutation = mutation;
    this.featuresExtractor = featuresExtractor;
    features = MapOfElites.buildFeatures(featuresSizes, featuresMinValues, featuresMaxValues);
  }

  public static class State<G, S, Q> extends POSetPopulationState<G, S, Q> {

    private final MapOfElites<Individual<G, S, Q>> mapOfElites;

    public State(List<MapOfElites.Feature> features,
                 Function<Individual<G, S, Q>, List<Double>> featuresExtractor,
                 PartialComparator<? super Individual<G, S, Q>> individualsComparator
    ) {
      mapOfElites = new MapOfElites<>(features, true, featuresExtractor, individualsComparator);
    }

    protected State(
        LocalDateTime startingDateTime,
        long elapsedMillis,
        long nOfIterations,
        long nOfBirths,
        long nOfFitnessEvaluations,
        PartiallyOrderedCollection<Individual<G, S, Q>> population,
        MapOfElites<Individual<G, S, Q>> mapOfElites
    ) {
      super(startingDateTime, elapsedMillis, nOfIterations, nOfBirths, nOfFitnessEvaluations, population);
      this.mapOfElites = mapOfElites;
    }

    @Override
    public State<G, S, Q> immutableCopy() {
      return new State<>(
          startingDateTime,
          elapsedMillis,
          nOfIterations,
          nOfBirths,
          nOfFitnessEvaluations,
          population.immutableCopy(),
          mapOfElites.copy()
      );
    }

  }

  @Override
  protected State<G, S, Q> initState(P problem, RandomGenerator random, ExecutorService executor) {
    return new State<>(features, featuresExtractor, comparator(problem));
  }

  @Override
  public State<G, S, Q> init(P problem, RandomGenerator random, ExecutorService executor) throws SolverException {
    State<G, S, Q> state = super.init(problem, random, executor);
    state.mapOfElites.addAll(state.getPopulation().all());
    return state;
  }

  @Override
  public void update(P problem, RandomGenerator random, ExecutorService executor, State<G, S, Q> state) throws SolverException {
    List<G> allGenotypes = state.getPopulation().all().stream().filter(Objects::nonNull).map(Individual::genotype).toList();
    Collection<G> offspringGenotypes = IntStream.range(0, batchSize)
        .mapToObj(i -> mutation.mutate(allGenotypes.get(random.nextInt(allGenotypes.size())), random)).toList();
    Collection<Individual<G, S, Q>> offspringIndividuals = map(offspringGenotypes, List.of(), solutionMapper, problem.qualityFunction(), executor, state);

    state.mapOfElites.addAll(offspringIndividuals);
    state.setPopulation(new DAGPartiallyOrderedCollection<>(state.mapOfElites.all(), comparator(problem)));
  }
}
