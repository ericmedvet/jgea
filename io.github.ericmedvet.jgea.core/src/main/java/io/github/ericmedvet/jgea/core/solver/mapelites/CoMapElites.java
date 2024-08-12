/*-
 * ========================LICENSE_START=================================
 * jgea-core
 * %%
 * Copyright (C) 2018 - 2024 Eric Medvet
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
import io.github.ericmedvet.jgea.core.distance.Distance;
import io.github.ericmedvet.jgea.core.operator.Mutation;
import io.github.ericmedvet.jgea.core.order.PartiallyOrderedCollection;
import io.github.ericmedvet.jgea.core.problem.QualityBasedProblem;
import io.github.ericmedvet.jgea.core.solver.AbstractPopulationBasedIterativeSolver;
import io.github.ericmedvet.jgea.core.solver.Individual;
import io.github.ericmedvet.jgea.core.solver.SolverException;
import io.github.ericmedvet.jgea.core.util.Misc;
import io.github.ericmedvet.jnb.datastructure.Pair;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.random.RandomGenerator;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class CoMapElites<G1, G2, S1, S2, S, Q>
    extends AbstractPopulationBasedIterativeSolver<
        CoMEPopulationState<G1, G2, S1, S2, S, Q, QualityBasedProblem<S, Q>>,
        QualityBasedProblem<S, Q>,
        CoMEIndividual<G1, G2, S1, S2, S, Q>,
        Pair<G1, G2>,
        S,
        Q> {

  private final Factory<? extends G1> genotypeFactory1;
  private final Factory<? extends G2> genotypeFactory2;
  private final Mutation<G1> mutation1;
  private final Mutation<G2> mutation2;
  private final Function<? super G1, ? extends S1> solutionMapper1;
  private final Function<? super G2, ? extends S2> solutionMapper2;
  private final BiFunction<? super S1, ? super S2, ? extends S> solutionMerger;
  private final List<MapElites.Descriptor<G1, S1, Q>> descriptors1;
  private final List<MapElites.Descriptor<G2, S2, Q>> descriptors2;
  private final int populationSize;
  private final int nOfOffspring;
  private final Strategy strategy;

  public CoMapElites(
      Predicate<? super CoMEPopulationState<G1, G2, S1, S2, S, Q, QualityBasedProblem<S, Q>>> stopCondition,
      Factory<? extends G1> genotypeFactory1,
      Factory<? extends G2> genotypeFactory2,
      Function<? super G1, ? extends S1> solutionMapper1,
      Function<? super G2, ? extends S2> solutionMapper2,
      BiFunction<? super S1, ? super S2, ? extends S> solutionMerger,
      List<MapElites.Descriptor<G1, S1, Q>> descriptors1,
      List<MapElites.Descriptor<G2, S2, Q>> descriptors2,
      Mutation<G1> mutation1,
      Mutation<G2> mutation2,
      int populationSize,
      int nOfOffspring,
      Strategy strategy) {
    super(null, null, stopCondition, false);
    this.genotypeFactory1 = genotypeFactory1;
    this.genotypeFactory2 = genotypeFactory2;
    this.solutionMapper1 = solutionMapper1;
    this.solutionMapper2 = solutionMapper2;
    this.solutionMerger = solutionMerger;
    this.descriptors1 = descriptors1;
    this.descriptors2 = descriptors2;
    this.mutation1 = mutation1;
    this.mutation2 = mutation2;
    this.populationSize = populationSize;
    this.nOfOffspring = nOfOffspring;
    this.strategy = strategy;
  }

  public enum Strategy {
    IDENTITY,
    BEST,
    CENTRAL
  }

  private static double euclideanDistance(List<Integer> coords1, List<Integer> coords2) {
    if (coords1.size() != coords2.size()) {
      throw new IllegalArgumentException("Coordinates must have the same dimensions.");
    }
    double sum = 0.0;
    for (int i = 0; i < coords1.size(); i++) {
      double diff = coords1.get(i) - coords2.get(i);
      sum += diff * diff;
    }
    return Math.sqrt(sum);
  }

  private static <X> Collection<X> findNeighbors(
      List<Integer> coords,
      Map<List<Integer>, X> mapOfElites,
      Distance<List<Integer>> distance,
      double threshold) { // problem here
    return mapOfElites.entrySet().stream()
        .filter(e -> distance.apply(e.getKey(), coords) < threshold)
        .map(Map.Entry::getValue)
        .toList();
  }

  private static List<Integer> getClosestCoordinate(List<Integer> coords, Map<List<Integer>, ?> mapOfElites) {
    return mapOfElites.keySet().stream()
        .min(Comparator.comparingDouble(c -> euclideanDistance(c, coords)))
        .orElseThrow();
  }

  private static <GT, GO, ST, SO, S, Q> List<Integer> getOtherCoords(
      List<Integer> thisCoords,
      Map<List<Integer>, MEIndividual<GO, SO, Q>> otherMapOfElites,
      QualityBasedProblem<S, Q> problem,
      List<MapElites.Descriptor<GT, ST, Q>> thisDescriptors,
      List<MapElites.Descriptor<GO, SO, Q>> otherDescriptors,
      Strategy strategy) {
    return switch (strategy) {
      case BEST -> PartiallyOrderedCollection.from(
              otherMapOfElites.entrySet(),
              problem.qualityComparator()
                  .comparing(e -> e.getValue().quality()))
          .firsts()
          .stream()
          .findFirst()
          .map(Map.Entry::getKey)
          .orElseThrow();
      case CENTRAL -> otherDescriptors.stream().map(d -> d.nOfBins() / 2).toList();
      case IDENTITY -> {
        if (thisDescriptors.size() != otherDescriptors.size()) {
          throw new IllegalArgumentException(
              "Coordinates must have the same dimensions: %d in this archive, %d in other archive"
                  .formatted(thisDescriptors.size(), otherDescriptors.size()));
        }
        if (IntStream.range(0, thisDescriptors.size())
                .filter(i -> thisDescriptors.get(i).nOfBins()
                    != otherDescriptors.get(i).nOfBins())
                .count() // problem here
            > 0) {
          throw new IllegalArgumentException("Descriptors are not compatible: different number of bins");
        }
        yield thisCoords;
      }
    };
  }

  private static <GT, GO, ST, SO, S, Q>
      Callable<Pair<MEIndividual<GT, ST, Q>, List<CoMEIndividual<GT, GO, ST, SO, S, Q>>>> reproduceCallable(
          Archive<MEIndividual<GT, ST, Q>> thisArchive,
          Archive<MEIndividual<GO, SO, Q>> otherArchive,
          Mutation<GT> mutation,
          Function<? super GT, ? extends ST> solutionMapper,
          BiFunction<? super ST, ? super SO, ? extends S> solutionMerger,
          List<MapElites.Descriptor<GT, ST, Q>> thisDescriptors,
          List<MapElites.Descriptor<GO, SO, Q>> otherDescriptors,
          Strategy strategy,
          QualityBasedProblem<S, Q> problem,
          RandomGenerator random,
          long iteration,
          AtomicLong counter) {
    return () -> {
      MEIndividual<GT, ST, Q> parentT =
          Misc.pickRandomly(thisArchive.asMap().values(), random);
      ChildGenotype<GT> childGenotypeT = new ChildGenotype<>(
          counter.getAndIncrement(), mutation.mutate(parentT.genotype(), random), List.of(parentT.id()));
      MEIndividual<GT, ST, Q> iT = MEIndividual.from(
          Individual.from(childGenotypeT, solutionMapper, sT -> null, iteration), thisDescriptors);
      List<Integer> thisCoords = iT.coordinates().stream()
          .map(MapElites.Descriptor.Coordinate::bin)
          .toList();
      List<Integer> otherCoords = getClosestCoordinate(
          getOtherCoords(
              thisCoords, otherArchive.asMap(), problem, thisDescriptors, otherDescriptors, strategy),
          otherArchive.asMap()); // collaborator choice
      Collection<MEIndividual<GO, SO, Q>> neighbors =
          findNeighbors(otherCoords, otherArchive.asMap(), CoMapElites::euclideanDistance, 2);
      List<CoMEIndividual<GT, GO, ST, SO, S, Q>> localCompositeIndividuals = neighbors.stream()
          .map(iO -> {
            S s = solutionMerger.apply(iT.solution(), iO.solution());
            return CoMEIndividual.of(
                counter.getAndIncrement(),
                s,
                problem.qualityFunction().apply(s),
                iteration,
                iteration,
                List.of(),
                iT,
                iO);
          })
          .toList();
      Q q = PartiallyOrderedCollection.from(
              localCompositeIndividuals,
              problem.qualityComparator().comparing(CoMEIndividual::quality))
          .firsts()
          .stream()
          .findAny()
          .map(CoMEIndividual::quality)
          .orElseThrow();
      return new Pair<>(iT.updatedWithQuality(q), localCompositeIndividuals);
    };
  }

  private Callable<CoMEIndividual<G1, G2, S1, S2, S, Q>> coMapCallable(
      ChildGenotype<G1> cg1,
      ChildGenotype<G2> cg2,
      CoMEPopulationState<G1, G2, S1, S2, S, Q, QualityBasedProblem<S, Q>> state,
      AtomicLong counter) {
    S1 s1 = solutionMapper1.apply(cg1.genotype());
    S2 s2 = solutionMapper2.apply(cg2.genotype());
    S s = solutionMerger.apply(s1, s2);
    Q q = state.problem().qualityFunction().apply(s);
    return () -> CoMEIndividual.of(
        counter.getAndIncrement(),
        s,
        q,
        state.nOfIterations(),
        state.nOfIterations(),
        List.of(),
        MEIndividual.from(Individual.from(cg1, solutionMapper1, ss1 -> q, state.nOfIterations()), descriptors1),
        MEIndividual.from(
            Individual.from(cg2, solutionMapper2, ss2 -> q, state.nOfIterations()), descriptors2));
  }

  @Override
  public CoMEPopulationState<G1, G2, S1, S2, S, Q, QualityBasedProblem<S, Q>> init(
      QualityBasedProblem<S, Q> problem, RandomGenerator random, ExecutorService executor)
      throws SolverException {
    CoMEPopulationState<G1, G2, S1, S2, S, Q, QualityBasedProblem<S, Q>> newState =
        CoMEPopulationState.empty(problem, stopCondition(), descriptors1, descriptors2);
    AtomicLong counter = new AtomicLong(0);
    List<ChildGenotype<G1>> childGenotypes1 = genotypeFactory1.build(populationSize, random).stream()
        .map(g -> new ChildGenotype<G1>(counter.getAndIncrement(), g, List.of()))
        .toList();
    List<ChildGenotype<G2>> childGenotypes2 = genotypeFactory2.build(populationSize, random).stream()
        .map(g -> new ChildGenotype<G2>(counter.getAndIncrement(), g, List.of()))
        .toList();
    Collection<CoMEIndividual<G1, G2, S1, S2, S, Q>> coMEIndividuals = getAll(
        IntStream.range(0, populationSize)
            .mapToObj(i -> coMapCallable(childGenotypes1.get(i), childGenotypes2.get(i), newState, counter))
            .toList(),
        executor);
    return newState.updatedWithIteration(
        populationSize,
        populationSize,
        coMEIndividuals,
        newState.mapOfElites1()
            .updated(
                coMEIndividuals.stream()
                    .map(CoMEIndividual::individual1)
                    .toList(),
                MEIndividual::bins,
                partialComparator(problem)),
        newState.mapOfElites2()
            .updated(
                coMEIndividuals.stream()
                    .map(CoMEIndividual::individual2)
                    .toList(),
                MEIndividual::bins,
                partialComparator(problem)));
  }

  @Override
  public CoMEPopulationState<G1, G2, S1, S2, S, Q, QualityBasedProblem<S, Q>> update(
      RandomGenerator random,
      ExecutorService executor,
      CoMEPopulationState<G1, G2, S1, S2, S, Q, QualityBasedProblem<S, Q>> state)
      throws SolverException {
    AtomicLong counter = new AtomicLong(state.nOfBirths());
    // reproduction
    Collection<Pair<MEIndividual<G1, S1, Q>, List<CoMEIndividual<G1, G2, S1, S2, S, Q>>>> reproduction1 = getAll(
        IntStream.range(0, populationSize)
            .mapToObj(i -> reproduceCallable(
                state.mapOfElites1(),
                state.mapOfElites2(),
                mutation1,
                solutionMapper1,
                solutionMerger,
                descriptors1,
                descriptors2,
                strategy,
                state.problem(),
                random,
                state.nOfIterations(),
                counter))
            .toList(),
        executor);
    Collection<Pair<MEIndividual<G2, S2, Q>, List<CoMEIndividual<G2, G1, S2, S1, S, Q>>>> reproduction2 = getAll(
        IntStream.range(0, populationSize)
            .mapToObj(i -> reproduceCallable(
                state.mapOfElites2(),
                state.mapOfElites1(),
                mutation2,
                solutionMapper2,
                (s2, s1) -> solutionMerger.apply(s1, s2),
                descriptors2,
                descriptors1,
                strategy,
                state.problem(),
                random,
                state.nOfIterations(),
                counter))
            .toList(),
        executor);
    List<CoMEIndividual<G1, G2, S1, S2, S, Q>> coMEIndividuals1 =
        reproduction1.stream().flatMap(p1 -> p1.second().stream()).toList();
    List<CoMEIndividual<G1, G2, S1, S2, S, Q>> coMEIndividuals2 = reproduction2.stream()
        .flatMap(p2 -> p2.second().stream().map(CoMEIndividual::swapped))
        .toList();
    // return state
    return state.updatedWithIteration(
        nOfOffspring * 2L,
        coMEIndividuals1.size() + coMEIndividuals2.size(),
        Stream.concat(coMEIndividuals1.stream(), coMEIndividuals2.stream())
            .toList(),
        state.mapOfElites1()
            .updated(
                reproduction1.stream().map(Pair::first).toList(),
                MEIndividual::bins,
                partialComparator(state.problem())),
        state.mapOfElites2()
            .updated(
                reproduction2.stream().map(Pair::first).toList(),
                MEIndividual::bins,
                partialComparator(state.problem())));
  }
}
