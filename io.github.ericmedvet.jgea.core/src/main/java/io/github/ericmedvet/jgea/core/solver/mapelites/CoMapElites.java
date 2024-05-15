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
import io.github.ericmedvet.jgea.core.util.Pair;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ExecutorService;
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
        CoMEPopulationState.CompositeIndividual<G1, G2, S1, S2, S, Q>,
        Pair<G1, G2>,
        S,
        Q> {

  private static final Distance<List<Integer>> COORD_DISTANCE = (c1, c2) -> {
    if (c1.size() != c2.size()) {
      throw new IllegalArgumentException("Lists have different sizes: %d vs. %d".formatted(c1.size(), c2.size()));
    }
    double sum = 0;
    for (int i = 0; i < c1.size(); i++) {
      sum = sum + (c1.get(i) - c2.get(i)) * (c1.get(i) * c2.get(i));
    }
    return Math.sqrt(sum);
  };

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

  public enum Strategy {
    IDENTITY,
    BEST,
    CENTRAL
  }

  private record State<G1, G2, S1, S2, S, Q>(
      LocalDateTime startingDateTime,
      long elapsedMillis,
      long nOfIterations,
      QualityBasedProblem<S, Q> problem,
      Predicate<io.github.ericmedvet.jgea.core.solver.State<?, ?>> stopCondition,
      long nOfBirths,
      long nOfQualityEvaluations,
      PartiallyOrderedCollection<CompositeIndividual<G1, G2, S1, S2, S, Q>> pocPopulation,
      Map<List<Integer>, Individual<G1, S1, Q>> mapOfElites1,
      Map<List<Integer>, Individual<G2, S2, Q>> mapOfElites2,
      List<MapElites.Descriptor<G1, S1, Q>> descriptors1,
      List<MapElites.Descriptor<G2, S2, Q>> descriptors2)
      implements CoMEPopulationState<G1, G2, S1, S2, S, Q, QualityBasedProblem<S, Q>>,
          io.github.ericmedvet.jgea.core.solver.State.WithComputedProgress<QualityBasedProblem<S, Q>, S> {

    // from -> for the init a state
    public static <G1, G2, S1, S2, S, Q> State<G1, G2, S1, S2, S, Q> from(
        QualityBasedProblem<S, Q> problem,
        long nOfBirths,
        long nOfQualityEvaluations,
        Map<List<Integer>, Individual<G1, S1, Q>> mapOfElites1,
        Map<List<Integer>, Individual<G2, S2, Q>> mapOfElites2,
        List<CoMEPopulationState.CompositeIndividual<G1, G2, S1, S2, S, Q>> compositeIndividuals,
        List<MapElites.Descriptor<G1, S1, Q>> descriptors1,
        List<MapElites.Descriptor<G2, S2, Q>> descriptors2,
        Predicate<io.github.ericmedvet.jgea.core.solver.State<?, ?>> stopCondition) {
      return new State<>(
          LocalDateTime.now(),
          0,
          0,
          problem,
          stopCondition,
          nOfBirths,
          nOfQualityEvaluations,
          PartiallyOrderedCollection.from(compositeIndividuals, partialComparator(problem)),
          mapOfElites1,
          mapOfElites2,
          descriptors1,
          descriptors2);
    }

    // from -> for update a state
    public static <G1, G2, S1, S2, S, Q> State<G1, G2, S1, S2, S, Q> from(
        State<G1, G2, S1, S2, S, Q> state,
        long nOfBirths,
        long nOfFitnessEvaluations,
        Map<List<Integer>, Individual<G1, S1, Q>> mapOfElites1,
        Map<List<Integer>, Individual<G2, S2, Q>> mapOfElites2,
        List<CoMEPopulationState.CompositeIndividual<G1, G2, S1, S2, S, Q>> compositeIndividuals) {
      return new State<>(
          state.startingDateTime,
          ChronoUnit.MILLIS.between(state.startingDateTime, LocalDateTime.now()),
          state.nOfIterations + 1,
          state.problem,
          state.stopCondition,
          state.nOfBirths + nOfBirths,
          state.nOfQualityEvaluations + nOfFitnessEvaluations,
          PartiallyOrderedCollection.from(compositeIndividuals, partialComparator(state.problem)),
          mapOfElites1,
          mapOfElites2,
          state.descriptors1,
          state.descriptors2);
    }
  } // record state end

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

  @Override
  protected CoMEPopulationState.CompositeIndividual<G1, G2, S1, S2, S, Q> newIndividual(
      Pair<G1, G2> genotype,
      CoMEPopulationState<G1, G2, S1, S2, S, Q, QualityBasedProblem<S, Q>> state,
      QualityBasedProblem<S, Q> problem) {
    throw new UnsupportedOperationException();
  }

  @Override
  protected CoMEPopulationState.CompositeIndividual<G1, G2, S1, S2, S, Q> updateIndividual(
      CoMEPopulationState.CompositeIndividual<G1, G2, S1, S2, S, Q> individual,
      CoMEPopulationState<G1, G2, S1, S2, S, Q, QualityBasedProblem<S, Q>> state,
      QualityBasedProblem<S, Q> problem) {
    throw new UnsupportedOperationException();
  }

  @Override
  public CoMEPopulationState<G1, G2, S1, S2, S, Q, QualityBasedProblem<S, Q>> init(
      QualityBasedProblem<S, Q> problem, RandomGenerator random, ExecutorService executor)
      throws SolverException {
    List<Individual<G1, S1, Q>> individualList1 = new ArrayList<>();
    List<Individual<G2, S2, Q>> individualList2 = new ArrayList<>();
    List<CoMEPopulationState.CompositeIndividual<G1, G2, S1, S2, S, Q>> compositeIndividuals = new ArrayList<>();
    List<? extends G1> g1s = genotypeFactory1.build(populationSize, random);
    List<? extends G2> g2s = genotypeFactory2.build(populationSize, random);
    for (int i = 0; i < populationSize; i++) {
      S1 s1 = solutionMapper1.apply(g1s.get(i));
      S2 s2 = solutionMapper2.apply(g2s.get(i));
      S s = solutionMerger.apply(s1, s2);
      Q q = problem.qualityFunction().apply(s);
      individualList1.add(Individual.of(g1s.get(i), s1, q, 0, 0));
      individualList2.add(Individual.of(g2s.get(i), s2, q, 0, 0));
      compositeIndividuals.add(
          new CoMEPopulationState.CompositeIndividual<>(g1s.get(i), g2s.get(i), s1, s2, s, q, 0, 0));
    }
    return State.from(
        problem,
        populationSize * 2L,
        populationSize,
        MapElites.mapOfElites(individualList1, descriptors1, partialComparator(problem)),
        MapElites.mapOfElites(individualList2, descriptors2, partialComparator(problem)),
        compositeIndividuals,
        descriptors1,
        descriptors2,
        stopCondition());
  }

  private static <GT, GO, ST, SO, S, Q>
      Pair<
              Map<List<Integer>, Individual<GT, ST, Q>>,
              List<CoMEPopulationState.CompositeIndividual<GT, GO, ST, SO, S, Q>>>
          reproduce(
              Map<List<Integer>, Individual<GT, ST, Q>> thisArchive,
              Map<List<Integer>, Individual<GO, SO, Q>> otherArchive,
              Mutation<GT> mutation,
              Function<? super GT, ? extends ST> solutionMapper,
              BiFunction<? super ST, ? super SO, ? extends S> solutionMerger,
              List<MapElites.Descriptor<GT, ST, Q>> thisDescriptors,
              List<MapElites.Descriptor<GO, SO, Q>> otherDescriptors,
              Strategy strategy,
              int nOfOffspring,
              QualityBasedProblem<S, Q> problem,
              RandomGenerator random,
              long iteration) {
    Pair<
            Map<List<Integer>, Individual<GT, ST, Q>>,
            List<CoMEPopulationState.CompositeIndividual<GT, GO, ST, SO, S, Q>>>
        result = new Pair<>(new LinkedHashMap<>(), new ArrayList<>());
    for (int i = 0; i < nOfOffspring; i++) {
      GT gt = mutation.mutate(
          Misc.pickRandomly(thisArchive.values(), random).genotype(), random);
      ST st = solutionMapper.apply(gt);
      List<Integer> thisCoords = thisDescriptors.stream()
          .map(d -> d.binOf(Individual.of(gt, st, null, 0, 0)))
          .toList();
      List<Integer> otherCoords = getClosestCoordinate(
          getOtherCoords(thisCoords, otherArchive, problem, thisDescriptors, otherDescriptors, strategy),
          otherArchive); // collaborator choice
      Collection<Individual<GO, SO, Q>> neighbors =
          findNeighbors(otherCoords, otherArchive, CoMapElites::euclideanDistance, 2);
      List<CoMEPopulationState.CompositeIndividual<GT, GO, ST, SO, S, Q>> localCompositeIndividuals =
          neighbors.stream()
              .map(io -> {
                S s = solutionMerger.apply(st, io.solution());
                return new CoMEPopulationState.CompositeIndividual<>(
                    gt,
                    io.genotype(),
                    st,
                    io.solution(),
                    s,
                    problem.qualityFunction().apply(s),
                    iteration,
                    iteration);
              })
              .toList();
      Q q = PartiallyOrderedCollection.from(
              localCompositeIndividuals,
              problem.qualityComparator().comparing(CoMEPopulationState.CompositeIndividual::quality))
          .firsts()
          .stream()
          .findAny()
          .map(CoMEPopulationState.CompositeIndividual::quality)
          .orElseThrow();
      result.first()
          .put(
              thisCoords,
              MapElites.chooseBest(
                  Individual.of(gt, st, q, iteration, iteration),
                  result.first().get(thisCoords),
                  problem.qualityComparator().comparing(Individual::quality)));
      result.second().addAll(localCompositeIndividuals);
    }
    return result;
  }

  @Override
  public CoMEPopulationState<G1, G2, S1, S2, S, Q, QualityBasedProblem<S, Q>> update(
      QualityBasedProblem<S, Q> problem,
      RandomGenerator random,
      ExecutorService executor,
      CoMEPopulationState<G1, G2, S1, S2, S, Q, QualityBasedProblem<S, Q>> state)
      throws SolverException {
    List<Individual<G2, S2, Q>> individualList2 = new ArrayList<>();
    List<CoMEPopulationState.CompositeIndividual<G1, G2, S1, S2, S, Q>> compositeIndividuals = new ArrayList<>();
    // reproduction
    Pair<
            Map<List<Integer>, Individual<G1, S1, Q>>,
            List<CoMEPopulationState.CompositeIndividual<G1, G2, S1, S2, S, Q>>>
        reproduction1 = reproduce(
            state.mapOfElites1(),
            state.mapOfElites2(),
            mutation1,
            solutionMapper1,
            solutionMerger,
            descriptors1,
            descriptors2,
            strategy,
            nOfOffspring,
            problem,
            random,
            state.nOfIterations());
    Pair<
            Map<List<Integer>, Individual<G2, S2, Q>>,
            List<CoMEPopulationState.CompositeIndividual<G2, G1, S2, S1, S, Q>>>
        reproduction2 = reproduce(
            state.mapOfElites2(),
            state.mapOfElites1(),
            mutation2,
            solutionMapper2,
            (s2, s1) -> solutionMerger.apply(s1, s2),
            descriptors2,
            descriptors1,
            strategy,
            nOfOffspring,
            problem,
            random,
            state.nOfIterations());
    // replace
    Map<List<Integer>, Individual<G1, S1, Q>> newArchive1 = new LinkedHashMap<>(state.mapOfElites1());
    reproduction1
        .first()
        .forEach((coords, i1) -> newArchive1.put(
            coords,
            MapElites.chooseBest(
                i1,
                newArchive1.get(coords),
                problem.qualityComparator().comparing(Individual::quality))));
    Map<List<Integer>, Individual<G2, S2, Q>> newArchive2 = new LinkedHashMap<>(state.mapOfElites2());
    reproduction2
        .first()
        .forEach((coords, i2) -> newArchive2.put(
            coords,
            MapElites.chooseBest(
                i2,
                newArchive2.get(coords),
                problem.qualityComparator().comparing(Individual::quality))));
    List<CoMEPopulationState.CompositeIndividual<G1, G2, S1, S2, S, Q>> swappedCompositeIndividuals2 =
        reproduction2.second().stream()
            .map(ci -> new CoMEPopulationState.CompositeIndividual<>(
                ci.genotype2(),
                ci.genotype1(),
                ci.solution2(),
                ci.solution1(),
                ci.solution(),
                ci.quality(),
                ci.genotypeBirthIteration(),
                ci.qualityMappingIteration()))
            .toList();
    // return state
    return State.from(
        (State<G1, G2, S1, S2, S, Q>) state,
        2L * nOfOffspring,
        reproduction1.second().size() + reproduction2.second().size(),
        reproduction1.first(),
        reproduction2.first(),
        Stream.concat(reproduction1.second().stream(), swappedCompositeIndividuals2.stream())
            .toList());
  }

  public static <X> Collection<X> findNeighbors(
      List<Integer> coords,
      Map<List<Integer>, X> mapOfElites,
      Distance<List<Integer>> distance,
      double threshold) {
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

  private static <GT, GO, ST, SO, S, Q> List<Integer> getOtherCoords(
      List<Integer> thisCoords,
      Map<List<Integer>, Individual<GO, SO, Q>> otherMapOfElites,
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
                .count()
            > 0) {
          throw new IllegalArgumentException("Descriptors are not compatible: different number of bins");
        }
        yield thisCoords;
      }
    };
  }

  private List<Integer> getClosest(List<Integer> coords, Map<List<Integer>, Individual<?, ?, ?>> mapOfElites) {
    return mapOfElites.keySet().stream()
        .min(Comparator.comparingDouble(c -> COORD_DISTANCE.apply(c, coords)))
        .orElseThrow();
  }

  private List<List<Integer>> getNeighborhood(
      List<Integer> coords, Map<List<Integer>, Individual<?, ?, ?>> mapOfElites, double radius) {
    return mapOfElites.keySet().stream()
        .filter(c -> COORD_DISTANCE.apply(c, coords) <= radius)
        .toList();
  }
}
