/*-
 * ========================LICENSE_START=================================
 * jgea-core
 * %%
 * Copyright (C) 2018 - 2026 Eric Medvet
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */
package io.github.ericmedvet.jgea.core.solver.mapelites;

import io.github.ericmedvet.jgea.core.Factory;
import io.github.ericmedvet.jgea.core.distance.LNorm;
import io.github.ericmedvet.jgea.core.operator.Mutation;
import io.github.ericmedvet.jgea.core.order.PartialComparator;
import io.github.ericmedvet.jgea.core.order.PartialComparator.PartialComparatorOutcome;
import io.github.ericmedvet.jgea.core.order.PartiallyOrderedCollection;
import io.github.ericmedvet.jgea.core.problem.QualityBasedProblem;
import io.github.ericmedvet.jgea.core.solver.AbstractPopulationBasedIterativeSolver;
import io.github.ericmedvet.jgea.core.solver.Individual;
import io.github.ericmedvet.jgea.core.solver.SolverException;
import io.github.ericmedvet.jgea.core.solver.mapelites.archive.NumericalKeyArchive;
import io.github.ericmedvet.jgea.core.solver.mapelites.strategy.CoMEStrategy;
import io.github.ericmedvet.jgea.core.util.Misc;
import io.github.ericmedvet.jnb.datastructure.Pair;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.random.RandomGenerator;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class CoMapElites<G1, G2, S1, S2, S, Q> extends AbstractPopulationBasedIterativeSolver<CoMEPopulationState<G1, G2, S1, S2, S, Q, QualityBasedProblem<S, Q>>, QualityBasedProblem<S, Q>, CoMEIndividual<G1, G2, S1, S2, S, Q>, Pair<G1, G2>, S, Q> {

  private final Factory<? extends G1> genotypeFactory1;
  private final Factory<? extends G2> genotypeFactory2;
  private final Mutation<G1> mutation1;
  private final Mutation<G2> mutation2;
  private final Function<? super G1, ? extends S1> solutionMapper1;
  private final Function<? super G2, ? extends S2> solutionMapper2;
  private final BiFunction<? super S1, ? super S2, ? extends S> solutionMerger;
  private final List<Function<Individual<G1, S1, Q>, Number>> descriptors1;
  private final List<Function<Individual<G2, S2, Q>, Number>> descriptors2;
  private final NumericalKeyArchive.Provider archiveProvider1;
  private final NumericalKeyArchive.Provider archiveProvider2;
  private final int populationSize;
  private final int nOfOffspring;
  private final Supplier<CoMEStrategy> strategySupplier;
  private final double normalizedNeighborRadius;
  private final int maxNOfNeighbors;

  public CoMapElites(
      Predicate<? super CoMEPopulationState<G1, G2, S1, S2, S, Q, QualityBasedProblem<S, Q>>> stopCondition,
      Factory<? extends G1> genotypeFactory1,
      Factory<? extends G2> genotypeFactory2,
      Function<? super G1, ? extends S1> solutionMapper1,
      Function<? super G2, ? extends S2> solutionMapper2,
      BiFunction<? super S1, ? super S2, ? extends S> solutionMerger,
      List<Function<Individual<G1, S1, Q>, Number>> descriptors1,
      List<Function<Individual<G2, S2, Q>, Number>> descriptors2,
      NumericalKeyArchive.Provider archiveProvider1,
      NumericalKeyArchive.Provider archiveProvider2,
      Mutation<G1> mutation1,
      Mutation<G2> mutation2,
      int populationSize,
      int nOfOffspring,
      Supplier<CoMEStrategy> strategySupplier,
      double normalizedNeighborRadius,
      int maxNOfNeighbors,
      List<PartialComparator<? super CoMEIndividual<G1, G2, S1, S2, S, Q>>> additionalIndividualComparators
  ) {
    super(null, null, stopCondition, false, additionalIndividualComparators);
    this.genotypeFactory1 = genotypeFactory1;
    this.genotypeFactory2 = genotypeFactory2;
    this.solutionMapper1 = solutionMapper1;
    this.solutionMapper2 = solutionMapper2;
    this.solutionMerger = solutionMerger;
    this.descriptors1 = descriptors1;
    this.descriptors2 = descriptors2;
    this.archiveProvider1 = archiveProvider1;
    this.archiveProvider2 = archiveProvider2;
    this.mutation1 = mutation1;
    this.mutation2 = mutation2;
    this.populationSize = populationSize;
    this.nOfOffspring = nOfOffspring;
    this.strategySupplier = strategySupplier;
    this.normalizedNeighborRadius = normalizedNeighborRadius;
    this.maxNOfNeighbors = maxNOfNeighbors;
    if (descriptors1.size() != descriptors2.size()) {
      throw new IllegalArgumentException(
          "Unexpected different sizes of descriptors: %d vs. %d"
              .formatted(descriptors1.size(), descriptors2.size())
      );
    }
  }

  private static <X> Collection<? extends X> findNeighbors(
      List<Double> coords,
      NumericalKeyArchive<? extends X, ? extends X> archive,
      double neighborRadius
  ) {
    return archive.valuedKeys()
        .stream()
        .filter(k -> LNorm.EUCLIDEAN.apply(k, coords) < neighborRadius)
        .map(k -> archive.get(k).orElseThrow())
        .toList();
  }

  private static List<Double> getClosestCoordinate(
      List<Double> coords,
      NumericalKeyArchive<?, ?> archive
  ) {
    return archive.valuedKeys()
        .stream()
        .min(Comparator.comparingDouble(c -> LNorm.EUCLIDEAN.apply(c, coords)))
        .orElseThrow();
  }

  private static double radius(NumericalKeyArchive<?, ?> archive, double normalizedRadius) {
    if (archive.valuedKeys().isEmpty()) {
      return 0;
    }
    int d = archive.valuedKeys().stream().limit(1).toList().getFirst().size();
    final double[] mins = new double[d];
    final double[] maxs = new double[d];
    Arrays.fill(mins, Double.MAX_VALUE);
    Arrays.fill(maxs, Double.MIN_VALUE);
    archive.valuedKeys()
        .forEach(k -> IntStream.range(0, d).forEach(i -> {
          mins[i] = Math.min(mins[i], k.get(i));
          maxs[i] = Math.max(maxs[i], k.get(i));
        }));
    double volume = IntStream.range(0, d).mapToDouble(i -> maxs[i] - mins[i]).reduce(1d, (v1, v2) -> (v1 * v2));
    return Math.pow(volume, 1d / d) * normalizedRadius;
  }

  private Callable<CoMEIndividual<G1, G2, S1, S2, S, Q>> coMapCallable(
      ChildGenotype<G1> cg1,
      ChildGenotype<G2> cg2,
      CoMEPopulationState<G1, G2, S1, S2, S, Q, QualityBasedProblem<S, Q>> state,
      AtomicLong counter
  ) {
    return () -> {
      S1 s1 = solutionMapper1.apply(cg1.genotype());
      S2 s2 = solutionMapper2.apply(cg2.genotype());
      S s = solutionMerger.apply(s1, s2);
      Q q = state.problem().qualityFunction().apply(s);
      return CoMEIndividual.of(
          counter.getAndIncrement(),
          s,
          q,
          state.nOfIterations(),
          state.nOfIterations(),
          List.of(),
          MEIndividual.from(
              Individual.from(cg1, solutionMapper1, ss1 -> q, state.nOfIterations()),
              descriptors1
          ),
          MEIndividual.from(
              Individual.from(cg2, solutionMapper2, ss2 -> q, state.nOfIterations()),
              descriptors2
          )
      );
    };
  }

  private static <GT, GO, ST, SO, S, Q> Callable<Pair<CoMEPartialIndividual<GT, ST, GT, GO, ST, SO, S, Q>, List<CoMEIndividual<GT, GO, ST, SO, S, Q>>>> reproduceCallable(
      NumericalKeyArchive<? extends MEIndividual<GT, ST, Q>, ? extends MEIndividual<GT, ST, Q>> thisArchive,
      NumericalKeyArchive<? extends MEIndividual<GO, SO, Q>, ? extends MEIndividual<GO, SO, Q>> otherArchive,
      Mutation<GT> mutation,
      Function<? super GT, ? extends ST> thisSolutionMapper,
      Function<? super GO, ? extends SO> otherSolutionMapper,
      BiFunction<? super ST, ? super SO, ? extends S> solutionMerger,
      List<Function<Individual<GT, ST, Q>, Number>> thisDescriptors,
      List<Function<Individual<GO, SO, Q>, Number>> otherDescriptors,
      CoMEStrategy strategy,
      double otherNeighborRadius,
      int maxNOfNeighbors,
      QualityBasedProblem<S, Q> problem,
      RandomGenerator random,
      long iteration,
      AtomicLong counter
  ) {
    return () -> {
      MEIndividual<GT, ST, Q> parentT = Misc.pickRandomly(thisArchive.contents(), random);
      ChildGenotype<GT> childGenotypeT = new ChildGenotype<>(
          counter.getAndIncrement(),
          mutation.mutate(parentT.genotype(), random),
          List.of(parentT.id())
      );
      CoMEPartialIndividual<GT, ST, GT, GO, ST, SO, S, Q> iT = CoMEPartialIndividual.from(
          Individual.from(childGenotypeT, thisSolutionMapper, sT -> null, iteration),
          thisDescriptors
      );
      List<? extends MEIndividual<GO, SO, Q>> neighbors = new ArrayList<>(
          findNeighbors(
              strategy.getOtherCoords(iT.descriptorValues()),
              otherArchive,
              otherNeighborRadius
          )
      );
      Collections.shuffle(neighbors, random);
      List<CoMEIndividual<GT, GO, ST, SO, S, Q>> localCompositeIndividuals = neighbors.stream()
          .limit(maxNOfNeighbors)
          .map(iO -> {
            S s = solutionMerger.apply(iT.solution(), otherSolutionMapper.apply(iO.genotype()));
            return CoMEIndividual.of(
                counter.getAndIncrement(),
                s,
                problem.qualityFunction().apply(s),
                iteration,
                iteration,
                List.of(),
                iT,
                iO
            );
          })
          .toList();
      CoMEIndividual<GT, GO, ST, SO, S, Q> bestCompleteIndividual = PartiallyOrderedCollection.from(
          localCompositeIndividuals,
          problem.qualityComparator().comparing(CoMEIndividual::quality)
      )
          .firsts()
          .stream()
          .findAny()
          .orElseThrow();
      return new Pair<>(
          iT.updateWithCompleteIndividual(bestCompleteIndividual),
          localCompositeIndividuals
      );
    };
  }

  @Override
  public CoMEPopulationState<G1, G2, S1, S2, S, Q, QualityBasedProblem<S, Q>> init(
      QualityBasedProblem<S, Q> problem,
      RandomGenerator random,
      Executor executor
  ) throws SolverException {
    NumericalKeyArchive<CoMEPartialIndividual<G1, S1, G1, G2, S1, S2, S, Q>, CoMEPartialIndividual<G1, S1, G1, G2, S1, S2, S, Q>> archive1 = archiveProvider1
        .provide(
            descriptors1.size()
        );
    NumericalKeyArchive<CoMEPartialIndividual<G2, S2, G1, G2, S1, S2, S, Q>, CoMEPartialIndividual<G2, S2, G1, G2, S1, S2, S, Q>> archive2 = archiveProvider2
        .provide(
            descriptors2.size()
        );
    if (archive1.arity() != descriptors1.size()) {
      throw new SolverException(
          "Archive 1 and respective descriptor sizes do not matches: %d vs. %d".formatted(
              archive1.arity(),
              descriptors1.size()
          )
      );
    }
    if (archive2.arity() != descriptors2.size()) {
      throw new SolverException(
          "Archive 2 and respective descriptor sizes do not matches: %d vs. %d".formatted(
              archive2.arity(),
              descriptors2.size()
          )
      );
    }
    CoMEStrategy strategy1 = strategySupplier.get();
    CoMEStrategy strategy2 = strategySupplier.get();
    CoMEPopulationState<G1, G2, S1, S2, S, Q, QualityBasedProblem<S, Q>> newState = CoMEPopulationState.empty(
        problem,
        stopCondition(),
        archive1,
        archive2,
        strategy1,
        strategy2
    );
    AtomicLong counter = new AtomicLong(0);
    List<ChildGenotype<G1>> childGenotypes1 = genotypeFactory1.build(populationSize, random)
        .stream()
        .map(g -> new ChildGenotype<G1>(counter.getAndIncrement(), g, List.of()))
        .toList();
    List<ChildGenotype<G2>> childGenotypes2 = genotypeFactory2.build(populationSize, random)
        .stream()
        .map(g -> new ChildGenotype<G2>(counter.getAndIncrement(), g, List.of()))
        .toList();
    Collection<CoMEIndividual<G1, G2, S1, S2, S, Q>> coMEIndividuals = parallelCall(
        IntStream.range(0, populationSize)
            .mapToObj(
                i -> coMapCallable(
                    childGenotypes1.get(i),
                    childGenotypes2.get(i),
                    newState,
                    counter
                )
            )
            .toList(),
        executor
    );
    // update strategies
    updateStrategies(newState, coMEIndividuals);
    // return state with updated archives
    return newState.updatedWithIteration(
        populationSize,
        populationSize,
        archive1.withAll(
            coMEIndividuals.stream().map(CoMEPartialIndividual::from1).toList(),
            MEIndividual::descriptorValues,
            partialComparator(problem)
                .comparing(
                    (CoMEPartialIndividual<G1, S1, G1, G2, S1, S2, S, Q> i1) -> i1.completeIndividual()
                )
                .secondIs(PartialComparatorOutcome.BEFORE)
                .negate()
        ),
        archive2.withAll(
            coMEIndividuals.stream().map(CoMEPartialIndividual::from2).toList(),
            MEIndividual::descriptorValues,
            partialComparator(problem)
                .comparing(
                    (CoMEPartialIndividual<G2, S2, G1, G2, S1, S2, S, Q> i2) -> i2.completeIndividual()
                )
                .secondIs(PartialComparatorOutcome.BEFORE)
                .negate()
        ),
        strategy1,
        strategy2
    );
  }

  @Override
  public CoMEPopulationState<G1, G2, S1, S2, S, Q, QualityBasedProblem<S, Q>> update(
      RandomGenerator random,
      Executor executor,
      CoMEPopulationState<G1, G2, S1, S2, S, Q, QualityBasedProblem<S, Q>> state
  ) throws SolverException {
    AtomicLong counter = new AtomicLong(state.nOfBirths());
    // reproduction 1
    Collection<Pair<CoMEPartialIndividual<G1, S1, G1, G2, S1, S2, S, Q>, List<CoMEIndividual<G1, G2, S1, S2, S, Q>>>> reproduction1 = parallelCall(
        IntStream.range(0, nOfOffspring / 2)
            .mapToObj(
                i -> reproduceCallable(
                    state.archive1(),
                    state.archive2(),
                    mutation1,
                    solutionMapper1,
                    solutionMapper2,
                    solutionMerger,
                    descriptors1,
                    descriptors2,
                    state.strategy1(),
                    radius(state.archive2(), normalizedNeighborRadius),
                    maxNOfNeighbors,
                    state.problem(),
                    random,
                    state.nOfIterations(),
                    counter
                )
            )
            .toList(),
        executor
    );
    // reproduction 2
    Collection<Pair<CoMEPartialIndividual<G2, S2, G2, G1, S2, S1, S, Q>, List<CoMEIndividual<G2, G1, S2, S1, S, Q>>>> reproduction2 = parallelCall(
        IntStream.range(0, nOfOffspring / 2)
            .mapToObj(
                i -> reproduceCallable(
                    state.archive2(),
                    state.archive1(),
                    mutation2,
                    solutionMapper2,
                    solutionMapper1,
                    (s2, s1) -> solutionMerger.apply(s1, s2),
                    descriptors2,
                    descriptors1,
                    state.strategy2(),
                    radius(state.archive1(), normalizedNeighborRadius),
                    maxNOfNeighbors,
                    state.problem(),
                    random,
                    state.nOfIterations(),
                    counter
                )
            )
            .toList(),
        executor
    );
    List<CoMEIndividual<G1, G2, S1, S2, S, Q>> coMEIndividuals1 = reproduction1.stream()
        .flatMap(p1 -> p1.second().stream())
        .toList();
    List<CoMEIndividual<G1, G2, S1, S2, S, Q>> coMEIndividuals2 = reproduction2.stream()
        .flatMap(p2 -> p2.second().stream().map(CoMEIndividual::swapped))
        .toList();
    List<CoMEIndividual<G1, G2, S1, S2, S, Q>> offspring = Stream.of(
        coMEIndividuals1,
        coMEIndividuals2
    )
        .flatMap(List::stream)
        .toList();
    // update strategies
    updateStrategies(state, offspring);
    // return state with updated archives
    return state.updatedWithIteration(
        nOfOffspring,
        coMEIndividuals1.size() + coMEIndividuals2.size(),
        state.archive1()
            .withAll(
                offspring.stream().map(CoMEPartialIndividual::from1).toList(),
                MEIndividual::descriptorValues,
                partialComparator(state.problem())
                    .comparing(
                        (CoMEPartialIndividual<G1, S1, G1, G2, S1, S2, S, Q> i1) -> i1.completeIndividual()
                    )
                    .secondIs(PartialComparatorOutcome.BEFORE)
                    .negate()
            ),
        state.archive2()
            .withAll(
                offspring.stream().map(CoMEPartialIndividual::from2).toList(),
                MEIndividual::descriptorValues,
                partialComparator(state.problem())
                    .comparing(
                        (CoMEPartialIndividual<G2, S2, G1, G2, S1, S2, S, Q> i2) -> i2.completeIndividual()
                    )
                    .secondIs(PartialComparatorOutcome.BEFORE)
                    .negate()
            ),
        state.strategy1(),
        state.strategy2()
    );
  }

  private void updateStrategies(
      CoMEPopulationState<G1, G2, S1, S2, S, Q, QualityBasedProblem<S, Q>> state,
      Collection<CoMEIndividual<G1, G2, S1, S2, S, Q>> newIndividuals
  ) {
    state.strategy1()
        .update(
            newIndividuals.stream()
                .map(
                    ci -> new CoMEStrategy.Observation<>(
                        ci.individual1().descriptorValues(),
                        ci.individual2().descriptorValues(),
                        ci.quality()
                    )
                )
                .toList(),
            state.problem().qualityComparator()
        );
    state.strategy2()
        .update(
            newIndividuals.stream()
                .map(
                    ci -> new CoMEStrategy.Observation<>(
                        ci.individual2().descriptorValues(),
                        ci.individual1().descriptorValues(),
                        ci.quality()
                    )
                )
                .toList(),
            state.problem().qualityComparator()
        );
  }
}