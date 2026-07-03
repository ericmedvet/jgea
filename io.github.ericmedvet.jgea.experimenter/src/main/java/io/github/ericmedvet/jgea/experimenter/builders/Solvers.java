/*-
 * ========================LICENSE_START=================================
 * jgea-experimenter
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

package io.github.ericmedvet.jgea.experimenter.builders;

import io.github.ericmedvet.jgea.core.Factory;
import io.github.ericmedvet.jgea.core.InvertibleMapper;
import io.github.ericmedvet.jgea.core.distance.Jaccard;
import io.github.ericmedvet.jgea.core.operator.GeneticOperator;
import io.github.ericmedvet.jgea.core.operator.Mutation;
import io.github.ericmedvet.jgea.core.order.PartialComparator;
import io.github.ericmedvet.jgea.core.problem.*;
import io.github.ericmedvet.jgea.core.representation.graph.*;
import io.github.ericmedvet.jgea.core.representation.graph.numeric.Constant;
import io.github.ericmedvet.jgea.core.representation.graph.numeric.Input;
import io.github.ericmedvet.jgea.core.representation.graph.numeric.Output;
import io.github.ericmedvet.jgea.core.representation.graph.numeric.operatorgraph.BaseOperator;
import io.github.ericmedvet.jgea.core.representation.graph.numeric.operatorgraph.OperatorGraph;
import io.github.ericmedvet.jgea.core.representation.graph.numeric.operatorgraph.OperatorNode;
import io.github.ericmedvet.jgea.core.representation.graph.numeric.operatorgraph.ShallowFactory;
import io.github.ericmedvet.jgea.core.selector.Last;
import io.github.ericmedvet.jgea.core.selector.Tournament;
import io.github.ericmedvet.jgea.core.solver.*;
import io.github.ericmedvet.jgea.core.solver.ElitistGeneticAlgorithm;
import io.github.ericmedvet.jgea.core.solver.bi.AbstractBiEvolver;
import io.github.ericmedvet.jgea.core.solver.bi.StandardBiEvolver;
import io.github.ericmedvet.jgea.core.solver.bi.mapelites.MapElitesBiEvolver;
import io.github.ericmedvet.jgea.core.solver.cabea.CellularAutomataBasedSolver;
import io.github.ericmedvet.jgea.core.solver.cabea.GridPopulationState;
import io.github.ericmedvet.jgea.core.solver.cabea.SubstrateFiller;
import io.github.ericmedvet.jgea.core.solver.es.*;
import io.github.ericmedvet.jgea.core.solver.mapelites.*;
import io.github.ericmedvet.jgea.core.solver.mapelites.archive.NumericalKeyArchive;
import io.github.ericmedvet.jgea.core.solver.mapelites.strategy.CoMEStrategy;
import io.github.ericmedvet.jgea.core.solver.multifidelity.ScheduledFidelityStandardEvolver;
import io.github.ericmedvet.jgea.core.solver.pso.PSOState;
import io.github.ericmedvet.jgea.core.solver.pso.ParticleSwarmOptimization;
import io.github.ericmedvet.jgea.core.solver.speciation.LazySpeciator;
import io.github.ericmedvet.jgea.core.solver.speciation.SpeciatedEvolver;
import io.github.ericmedvet.jgea.core.solver.speciation.SpeciatedPOCPopulationState;
import io.github.ericmedvet.jgea.experimenter.Representation;
import io.github.ericmedvet.jnb.core.Alias;
import io.github.ericmedvet.jnb.core.Cacheable;
import io.github.ericmedvet.jnb.core.Discoverable;
import io.github.ericmedvet.jnb.core.Param;
import io.github.ericmedvet.jnb.datastructure.Grid;
import io.github.ericmedvet.jnb.datastructure.Pair;
import java.util.List;
import java.util.Map;
import java.util.function.*;
import java.util.stream.DoubleStream;

@Discoverable(prefixTemplate = "ea.solver|s")
public class Solvers {

  private Solvers() {
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <G, S, Q> Function<S, AsynchronousScheduledMFMapElites<G, S, Q>> asyncScheduledMfMapElites(
      @Param(value = "name", iS = "asyncScheduledMfMapElites[{schedule.name};rr={recomputationRatio:%0.2f}]") String name,
      @Param("representation") Function<G, Representation<G>> representation,
      @Param(value = "mapper", dNPM = "ea.m.identity()") InvertibleMapper<G, S> mapper,
      @Param(value = "nOfBirthsForIteration", dI = 100) int nOfBirthsForIteration,
      @Param(value = "stop", dNPM = "ea.sc.nOfQualityEvaluations()") ProgressBasedStopCondition<? super MultiFidelityMEPopulationState<G, S, Q, MultifidelityQualityBasedProblem<S, Q>>> stopCondition,
      @Param("iComparators") List<PartialComparator<? super MEIndividual<G, S, Q>>> additionalIndividualComparators,
      @Param("descriptors") List<Function<Individual<G, S, Q>, Number>> descriptors,
      @Param("archive") NumericalKeyArchive.Provider archiProvider,
      @Param(value = "recomputationRatio", dD = 0.5) double recomputationRatio,
      @Param(value = "schedule", dNPM = "ea.schedule.flat()") DoubleUnaryOperator schedule
  ) {
    return exampleS -> {
      Representation<G> r = representation.apply(mapper.exampleFor(exampleS));
      return new AsynchronousScheduledMFMapElites<>(
          mapper.mapperFor(exampleS),
          r.factory(),
          stopCondition,
          additionalIndividualComparators,
          r.mutations().getFirst(),
          descriptors,
          archiProvider,
          schedule,
          recomputationRatio,
          nOfBirthsForIteration
      );
    };
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <G, S, Q, O> Function<S, StandardBiEvolver<G, S, Q, O>> biGa(
      @Param(value = "name", dS = "biGa") String name,
      @Param("representation") Function<G, Representation<G>> representation,
      @Param(value = "mapper", dNPM = "ea.m.identity()") InvertibleMapper<G, S> mapper,
      @Param(value = "crossoverP", dD = 0.8d) double crossoverP,
      @Param(value = "tournamentRate", dD = 0.05d) double tournamentRate,
      @Param(value = "minNTournament", dI = 3) int minNTournament,
      @Param(value = "nPop", dI = 100) int nPop,
      @Param(value = "stop", dNPM = "ea.sc.nOfQualityEvaluations()") Predicate<? super POCPopulationState<Individual<G, S, Q>, G, S, Q, QualityBasedBiProblem<S, O, Q>>> stopCondition,
      @Param(value = "maxUniquenessAttempts", dI = 100) int maxUniquenessAttempts,
      @Param("fitnessReducer") BinaryOperator<Q> fitnessReducer,
      @Param("additionalIndividualComparators") List<PartialComparator<? super Individual<G, S, Q>>> additionalIndividualComparators,
      @Param("opponentsSelector") AbstractBiEvolver.OpponentsSelector<Individual<G, S, Q>, S, Q, O> opponentsSelector,
      @Param("fitnessAggregator") Function<List<Q>, Q> fitnessAggregator
  ) {
    return exampleS -> {
      Representation<G> r = representation.apply(mapper.exampleFor(exampleS));
      return new StandardBiEvolver<>(
          mapper.mapperFor(exampleS),
          r.factory(),
          nPop,
          stopCondition,
          r.geneticOperators(crossoverP),
          new Tournament(Math.max(minNTournament, (int) Math.ceil((double) nPop * tournamentRate))),
          new Last(),
          nPop,
          true,
          maxUniquenessAttempts,
          fitnessReducer,
          additionalIndividualComparators,
          opponentsSelector,
          fitnessAggregator
      );
    };
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <G, S, Q, O> Function<S, MapElitesBiEvolver<G, S, Q, O>> biMapElites(
      @Param(value = "name", dS = "biMe") String name,
      @Param("representation") Function<G, Representation<G>> representation,
      @Param(value = "mapper", dNPM = "ea.m.identity()") InvertibleMapper<G, S> mapper,
      @Param(value = "nPop", dI = 100) int nPop,
      @Param(value = "stop", dNPM = "ea.sc.nOfQualityEvaluations()") Predicate<? super MEPopulationState<G, S, Q, QualityBasedBiProblem<S, O, Q>>> stopCondition,
      @Param("descriptors") List<Function<Individual<G, S, Q>, Number>> descriptors,
      @Param("archive") NumericalKeyArchive.Provider archiProvider,
      @Param("fitnessReducer") BinaryOperator<Q> fitnessReducer,
      @Param("emptyArchive") boolean emptyArchive,
      @Param("additionalIndividualComparators") List<PartialComparator<? super MEIndividual<G, S, Q>>> additionalIndividualComparators,
      @Param("opponentsSelector") AbstractBiEvolver.OpponentsSelector<MEIndividual<G, S, Q>, S, Q, O> opponentsSelector,
      @Param("fitnessAggregator") Function<List<Q>, Q> fitnessAggregator
  ) {
    return exampleS -> {
      Representation<G> r = representation.apply(mapper.exampleFor(exampleS));
      return new MapElitesBiEvolver<>(
          mapper.mapperFor(exampleS),
          r.factory(),
          stopCondition,
          r.mutations().getFirst(),
          nPop,
          descriptors,
          archiProvider,
          fitnessReducer,
          emptyArchive,
          additionalIndividualComparators,
          opponentsSelector,
          fitnessAggregator
      );
    };
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <G, S, Q> Function<S, CellularAutomataBasedSolver<G, S, Q>> cabea(
      @Param(value = "name", dS = "cabea") String name,
      @Param("representation") Function<G, Representation<G>> representation,
      @Param(value = "mapper", dNPM = "ea.m.identity()") InvertibleMapper<G, S> mapper,
      @Param(value = "keepProbability", dD = 0.00d) double keepProbability,
      @Param(value = "crossoverP", dD = 0.8d) double crossoverP,
      @Param(value = "nTour", dI = 1) int nTour,
      @Param(value = "stop", dNPM = "ea.sc.nOfQualityEvaluations()") Predicate<? super GridPopulationState<G, S, Q, QualityBasedProblem<S, Q>>> stopCondition,
      @Param(value = "toroidal", dB = true) boolean toroidal,
      @Param(value = "mooreRadius", dI = 1) int mooreRadius,
      @Param(value = "gridSize", dI = 11) int gridSize,
      @Param(value = "substrate", dS = "empty") SubstrateFiller.Predefined substrate,
      @Param("iComparators") List<PartialComparator<? super Individual<G, S, Q>>> additionalIndividualComparators
  ) {
    return exampleS -> {
      Representation<G> r = representation.apply(mapper.exampleFor(exampleS));
      return new CellularAutomataBasedSolver<>(
          mapper.mapperFor(exampleS),
          r.factory(),
          stopCondition,
          substrate.apply(Grid.create(gridSize, gridSize, true)),
          new CellularAutomataBasedSolver.MooreNeighborhood(mooreRadius, toroidal),
          keepProbability,
          r.geneticOperators(crossoverP),
          new Tournament(nTour),
          additionalIndividualComparators
      );
    };
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <S, Q> Function<S, CMAEvolutionaryStrategy<S, Q>> cmaEs(
      @Param(value = "name", dS = "cmaEs") String name,
      @Param(value = "mapper", dNPM = "ea.m.identity()") InvertibleMapper<List<Double>, S> mapper,
      @Param(value = "factory", dNPM = "ea.r.f.dsUniform()") Function<List<Double>, Factory<List<Double>>> factory,
      @Param(value = "stop", dNPM = "ea.sc.nOfQualityEvaluations()") Predicate<? super CMAESState<S, Q>> stopCondition
  ) {
    return exampleS -> new CMAEvolutionaryStrategy<>(
        mapper.mapperFor(exampleS),
        factory.apply(mapper.exampleFor(exampleS)),
        stopCondition
    );
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <G1, G2, S1, S2, S, Q> Function<S, CoMapElites<G1, G2, S1, S2, S, Q>> coMapElites(
      @Param(value = "name", iS = "coMe-{strategy}-{neighborRadius}-{maxNOfNeighbors}") String name,
      @Param("representation1") Function<G1, Representation<G1>> representation1,
      @Param("representation2") Function<G2, Representation<G2>> representation2,
      @Param(value = "mapper1", dNPM = "ea.m.identity()") InvertibleMapper<G1, S1> mapper1,
      @Param(value = "mapper2", dNPM = "ea.m.identity()") InvertibleMapper<G2, S2> mapper2,
      @Param("merger") InvertibleMapper<Pair<S1, S2>, S> invertibleMapperMerger,
      @Param("descriptors1") List<Function<Individual<G1, S1, Q>, Number>> descriptors1,
      @Param("descriptors1") List<Function<Individual<G2, S2, Q>, Number>> descriptors2,
      @Param("archive1") NumericalKeyArchive.Provider archiProvider1,
      @Param("archive2") NumericalKeyArchive.Provider archiProvider2,
      @Param(value = "stop", dNPM = "ea.sc.nOfQualityEvaluations()") Predicate<? super CoMEPopulationState<G1, G2, S1, S2, S, Q, QualityBasedProblem<S, Q>>> stopCondition,
      @Param(value = "populationSize", dI = 100) int populationSize,
      @Param(value = "nOfOffspring", dI = 50) int nOfOffspring,
      @Param(value = "strategy", dS = "identity") CoMEStrategy.Prepared strategy,
      @Param(value = "normalizedNeighborRadius", dD = 0.1) double normalizedNeighborRadius,
      @Param(value = "maxNOfNeighbors", dI = 2) int maxNOfNeighbors,
      @Param("iComparators") List<PartialComparator<? super CoMEIndividual<G1, G2, S1, S2, S, Q>>> additionalIndividualComparators
  ) {
    return exampleS -> {
      Pair<S1, S2> splitExample = invertibleMapperMerger.exampleFor(exampleS);
      Representation<G1> r1 = representation1.apply(mapper1.exampleFor(splitExample.first()));
      Representation<G2> r2 = representation2.apply(mapper2.exampleFor(splitExample.second()));
      BiFunction<S1, S2, S> merger = (s1, s2) -> invertibleMapperMerger.mapperFor(exampleS)
          .apply(new Pair<>(s1, s2));
      if (descriptors1.isEmpty() || descriptors2.isEmpty()) {
        throw new IllegalArgumentException("Descriptors for representations must be initialized.");
      }
      return new CoMapElites<>(
          stopCondition,
          r1.factory(),
          r2.factory(),
          mapper1.mapperFor(splitExample.first()),
          mapper2.mapperFor(splitExample.second()),
          merger,
          descriptors1,
          descriptors2,
          archiProvider1,
          archiProvider2,
          r1.mutations().getFirst(),
          r2.mutations().getFirst(),
          populationSize,
          nOfOffspring,
          strategy,
          normalizedNeighborRadius,
          maxNOfNeighbors,
          additionalIndividualComparators
      );
    };
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <S, Q> Function<S, DifferentialEvolution<S, Q>> differentialEvolution(
      @Param(value = "name", dS = "de") String name,
      @Param(value = "mapper", dNPM = "ea.m.identity()") InvertibleMapper<List<Double>, S> mapper,
      @Param(value = "factory", dNPM = "ea.r.f.dsUniform()") Function<List<Double>, Factory<List<Double>>> factory,
      @Param(value = "populationSize", dI = 15) int populationSize,
      @Param(value = "stop", dNPM = "ea.sc.nOfQualityEvaluations()") Predicate<? super ListPopulationState<Individual<List<Double>, S, Q>, List<Double>, S, Q, TotalOrderQualityBasedProblem<S, Q>>> stopCondition,
      @Param(value = "differentialWeight", dD = 0.5) double differentialWeight,
      @Param(value = "crossoverP", dD = 0.8) double crossoverP,
      @Param(value = "remap") boolean remap
  ) {
    return exampleS -> new DifferentialEvolution<>(
        mapper.mapperFor(exampleS),
        factory.apply(mapper.exampleFor(exampleS)),
        populationSize,
        stopCondition,
        differentialWeight,
        crossoverP,
        remap
    );
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <G, S, Q> Function<S, StandardEvolver<G, S, Q>> elitistGa(
      @Param(value = "name", dS = "elitistGa") String name,
      @Param("representation") Function<G, Representation<G>> representation,
      @Param(value = "mapper", dNPM = "ea.m.identity()") InvertibleMapper<G, S> mapper,
      @Param(value = "crossoverP", dD = 0.8d) double crossoverP,
      @Param(value = "tournamentRate", dD = 0.05d) double tournamentRate,
      @Param(value = "eliteRate", dD = 0.1d) double eliteRate,
      @Param(value = "minNTournament", dI = 3) int minNTournament,
      @Param(value = "nPop", dI = 100) int nPop,
      @Param(value = "stop", dNPM = "ea.sc.nOfQualityEvaluations()") Predicate<? super POCPopulationState<Individual<G, S, Q>, G, S, Q, QualityBasedProblem<S, Q>>> stopCondition,
      @Param(value = "maxUniquenessAttempts", dI = 100) int maxUniquenessAttempts,
      @Param(value = "remap") boolean remap,
      @Param("iComparators") List<PartialComparator<? super Individual<G, S, Q>>> additionalIndividualComparators
  ) {
    return exampleS -> {
      Representation<G> r = representation.apply(mapper.exampleFor(exampleS));
      return new ElitistGeneticAlgorithm<>(
          mapper.mapperFor(exampleS),
          r.factory(),
          nPop,
          stopCondition,
          r.geneticOperators(crossoverP),
          new Tournament(Math.max(minNTournament, (int) Math.ceil((double) nPop * tournamentRate))),
          eliteRate,
          maxUniquenessAttempts,
          remap,
          additionalIndividualComparators
      );
    };
  }

  @Alias(
      name = "ttpnGp", value = // spotless:off
      """
          ga(name = "ttpnGp"; representation = ea.r.ttpn(); mapper = ea.m.ttpnToProgram(); crossoverP = 0.5)
          """) // spotless:on
  @Alias(
      name = "srGp", value = // spotless:off
      """
          ga(name = "srGp"; representation = ea.r.srTree(); mapper = ea.m.srTreeToNurf())
          """) // spotless:on
  @SuppressWarnings("unused")
  @Cacheable
  public static <G, S, Q> Function<S, StandardEvolver<G, S, Q>> ga(
      @Param(value = "name", dS = "ga") String name,
      @Param("representation") Function<G, Representation<G>> representation,
      @Param(value = "mapper", dNPM = "ea.m.identity()") InvertibleMapper<G, S> mapper,
      @Param(value = "crossoverP", dD = 0.8d) double crossoverP,
      @Param(value = "tournamentRate", dD = 0.05d) double tournamentRate,
      @Param(value = "minNTournament", dI = 3) int minNTournament,
      @Param(value = "nPop", dI = 100) int nPop,
      @Param(value = "stop", dNPM = "ea.sc.nOfQualityEvaluations()") Predicate<? super POCPopulationState<Individual<G, S, Q>, G, S, Q, QualityBasedProblem<S, Q>>> stopCondition,
      @Param(value = "maxUniquenessAttempts", dI = 100) int maxUniquenessAttempts,
      @Param(value = "remap") boolean remap,
      @Param("iComparators") List<PartialComparator<? super Individual<G, S, Q>>> additionalIndividualComparators
  ) {
    return exampleS -> {
      Representation<G> r = representation.apply(mapper.exampleFor(exampleS));
      return new StandardEvolver<>(
          mapper.mapperFor(exampleS),
          r.factory(),
          nPop,
          stopCondition,
          r.geneticOperators(crossoverP),
          new Tournament(Math.max(minNTournament, (int) Math.ceil((double) nPop * tournamentRate))),
          new Last(),
          nPop,
          true,
          maxUniquenessAttempts,
          remap,
          additionalIndividualComparators
      );
    };
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <G, S, Q> Function<S, MultiArchiveMapElites<G, S, Q>> maMapElites2(
      @Param(value = "name", dS = "maMe2") String name,
      @Param("representation") Function<G, Representation<G>> representation,
      @Param(value = "mapper", dNPM = "ea.m.identity()") InvertibleMapper<G, S> mapper,
      @Param(value = "nPop", dI = 100) int nPop,
      @Param(value = "stop", dNPM = "ea.sc.nOfQualityEvaluations()") Predicate<? super MAMEPopulationState<G, S, Q, QualityBasedProblem<S, Q>>> stopCondition,
      @Param("descriptors") List<Function<Individual<G, S, Q>, Number>> descriptors1,
      @Param("descriptors") List<Function<Individual<G, S, Q>, Number>> descriptors2,
      @Param("archive") NumericalKeyArchive.Provider archiProvider,
      @Param("iComparators") List<PartialComparator<? super Individual<G, S, Q>>> additionalIndividualComparators
  ) {
    return exampleS -> {
      Representation<G> r = representation.apply(mapper.exampleFor(exampleS));
      return new MultiArchiveMapElites<>(
          mapper.mapperFor(exampleS),
          r.factory(),
          stopCondition,
          r.mutations().getFirst(),
          nPop,
          List.of(descriptors1, descriptors2),
          archiProvider,
          additionalIndividualComparators
      );
    };
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <G, S, Q> Function<S, MapElites<G, S, Q>> mapElites(
      @Param(value = "name", dS = "me") String name,
      @Param("representation") Function<G, Representation<G>> representation,
      @Param(value = "mapper", dNPM = "ea.m.identity()") InvertibleMapper<G, S> mapper,
      @Param(value = "nPop", dI = 100) int nPop,
      @Param(value = "stop", dNPM = "ea.sc.nOfQualityEvaluations()") Predicate<? super MEPopulationState<G, S, Q, QualityBasedProblem<S, Q>>> stopCondition,
      @Param("descriptors") List<Function<Individual<G, S, Q>, Number>> descriptors,
      @Param("archive") NumericalKeyArchive.Provider archiProvider,
      @Param("iComparators") List<PartialComparator<? super MEIndividual<G, S, Q>>> additionalIndividualComparators
  ) {
    return exampleS -> {
      Representation<G> r = representation.apply(mapper.exampleFor(exampleS));
      return new MapElites<>(
          mapper.mapperFor(exampleS),
          r.factory(),
          stopCondition,
          r.mutations().getFirst(),
          nPop,
          descriptors,
          archiProvider,
          additionalIndividualComparators
      );
    };
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <G, S, Q> Function<S, NsgaII<G, S, Q>> nsga2(
      @Param(value = "name", dS = "nsga2") String name,
      @Param("representation") Function<G, Representation<G>> representation,
      @Param(value = "mapper", dNPM = "ea.m.identity()") InvertibleMapper<G, S> mapper,
      @Param(value = "crossoverP", dD = 0.8d) double crossoverP,
      @Param(value = "nPop", dI = 100) int nPop,
      @Param(value = "stop", dNPM = "ea.sc.nOfQualityEvaluations()") Predicate<? super POCPopulationState<Individual<G, S, Q>, G, S, Q, MultiObjectiveProblem<S, Q, Double>>> stopCondition,
      @Param(value = "maxUniquenessAttempts", dI = 100) int maxUniquenessAttempts,
      @Param(value = "remap") boolean remap,
      @Param("iComparators") List<PartialComparator<? super Individual<G, S, Q>>> additionalIndividualComparators
  ) {
    return exampleS -> {
      Representation<G> r = representation.apply(mapper.exampleFor(exampleS));
      return new NsgaII<>(
          mapper.mapperFor(exampleS),
          r.factory(),
          nPop,
          stopCondition,
          r.geneticOperators(crossoverP),
          maxUniquenessAttempts,
          remap,
          additionalIndividualComparators
      );
    };
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <S, Q> Function<S, SpeciatedEvolver<Graph<Node, OperatorGraph.NonValuedArc>, S, Q>> oGraphea(
      @Param(value = "name", dS = "oGraphea") String name,
      @Param(value = "mapper", dNPM = "ea.m.identity()") InvertibleMapper<Graph<Node, OperatorGraph.NonValuedArc>, S> mapper,
      @Param(value = "minConst", dD = 0d) double minConst,
      @Param(value = "maxConst", dD = 5d) double maxConst,
      @Param(value = "nConst", dI = 10) int nConst,
      @Param(
          value = "operators", dSs = {"addition", "subtraction", "multiplication", "prot_division", "prot_log"}) List<BaseOperator> operators,
      @Param(value = "nPop", dI = 100) int nPop,
      @Param(value = "stop", dNPM = "ea.sc.nOfQualityEvaluations()") Predicate<? super SpeciatedPOCPopulationState<Graph<Node, OperatorGraph.NonValuedArc>, S, Q, QualityBasedProblem<S, Q>>> stopCondition,
      @Param(value = "arcAdditionRate", dD = 3d) double arcAdditionRate,
      @Param(value = "arcRemovalRate", dD = 0.1d) double arcRemovalRate,
      @Param(value = "nodeAdditionRate", dD = 1d) double nodeAdditionRate,
      @Param(value = "nPop", dI = 5) int minSpeciesSizeForElitism,
      @Param(value = "rankBase", dD = 0.75d) double rankBase,
      @Param(value = "remap") boolean remap,
      @Param("iComparators") List<PartialComparator<? super Individual<Graph<Node, OperatorGraph.NonValuedArc>, S, Q>>> additionalIndividualComparators
  ) {
    return exampleS -> {
      Map<GeneticOperator<Graph<Node, OperatorGraph.NonValuedArc>>, Double> geneticOperators = Map.ofEntries(
          Map.entry(
              new NodeAddition<Node, OperatorGraph.NonValuedArc>(
                  OperatorNode.sequentialIndexFactory(operators.toArray(BaseOperator[]::new)),
                  Mutation.copy(),
                  Mutation.copy()
              )
                  .withChecker(OperatorGraph.checker()),
              nodeAdditionRate
          ),
          Map.entry(
              new ArcAddition<Node, OperatorGraph.NonValuedArc>(
                  r -> OperatorGraph.NON_VALUED_ARC,
                  false
              )
                  .withChecker(OperatorGraph.checker()),
              arcAdditionRate
          ),
          Map.entry(
              new ArcRemoval<Node, OperatorGraph.NonValuedArc>(
                  node -> (node instanceof Input) || (node instanceof Constant) || (node instanceof Output)
              )
                  .withChecker(OperatorGraph.checker()),
              arcRemovalRate
          )
      );
      Graph<Node, OperatorGraph.NonValuedArc> graph = mapper.exampleFor(exampleS);
      double constStep = (maxConst - minConst) / nConst;
      List<Double> constants = DoubleStream.iterate(minConst, d -> d + constStep)
          .limit(nConst)
          .boxed()
          .toList();
      return new SpeciatedEvolver<>(
          mapper.mapperFor(exampleS),
          new ShallowFactory(
              graph.nodes()
                  .stream()
                  .filter(n -> n instanceof Input)
                  .map(n -> ((Input) n).getName())
                  .distinct()
                  .toList(),
              graph.nodes()
                  .stream()
                  .filter(n -> n instanceof Output)
                  .map(n -> ((Output) n).getName())
                  .distinct()
                  .toList(),
              constants
          ),
          stopCondition,
          geneticOperators,
          nPop,
          remap,
          additionalIndividualComparators,
          minSpeciesSizeForElitism,
          new LazySpeciator<>(
              (new Jaccard<Node>()).on(i -> i.genotype().nodes()),
              0.25
          ),
          rankBase
      );
    };
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <S, Q> Function<S, OpenAIEvolutionaryStrategy<S, Q>> openAiEs(
      @Param(value = "name", dS = "openAiEs") String name,
      @Param(value = "mapper", dNPM = "ea.m.identity()") InvertibleMapper<List<Double>, S> mapper,
      @Param(value = "factory", dNPM = "ea.r.f.dsUniform()") Function<List<Double>, Factory<List<Double>>> factory,
      @Param(value = "sigma", dD = 0.02d) double sigma,
      @Param(value = "batchSize", dI = 30) int batchSize,
      @Param(value = "stepSize", dD = 0.02d) double stepSize,
      @Param(value = "beta1", dD = 0.9d) double beta1,
      @Param(value = "beta2", dD = 0.999d) double beta2,
      @Param(value = "epsilon", dD = 1e-8) double epsilon,
      @Param(value = "stop", dNPM = "ea.sc.nOfQualityEvaluations()") Predicate<? super OpenAIESState<S, Q>> stopCondition
  ) {
    return exampleS -> new OpenAIEvolutionaryStrategy<>(
        mapper.mapperFor(exampleS),
        factory.apply(mapper.exampleFor(exampleS)),
        stopCondition,
        batchSize,
        sigma,
        stepSize,
        beta1,
        beta2,
        epsilon
    );
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <S, Q> Function<S, ParticleSwarmOptimization<S, Q>> pso(
      @Param(value = "name", dS = "pso") String name,
      @Param(value = "mapper", dNPM = "ea.m.identity()") InvertibleMapper<List<Double>, S> mapper,
      @Param(value = "factory", dNPM = "ea.r.f.dsUniform()") Function<List<Double>, Factory<List<Double>>> factory,
      @Param(value = "stop", dNPM = "ea.sc.nOfQualityEvaluations()") Predicate<? super PSOState<S, Q>> stopCondition,
      @Param(value = "nPop", dI = 100) int nPop,
      @Param(value = "w", dD = 0.8d) double w,
      @Param(value = "phiParticle", dD = 1.5d) double phiParticle,
      @Param(value = "phiGlobal", dD = 1.5d) double phiGlobal
  ) {
    return exampleS -> new ParticleSwarmOptimization<>(
        mapper.mapperFor(exampleS),
        factory.apply(mapper.exampleFor(exampleS)),
        stopCondition,
        nPop,
        w,
        phiParticle,
        phiGlobal
    );
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <G, S, Q> Function<S, RandomSearch<G, S, Q>> randomSearch(
      @Param(value = "name", dS = "rs") String name,
      @Param("representation") Function<G, Representation<G>> representation,
      @Param(value = "mapper", dNPM = "ea.m.identity()") InvertibleMapper<G, S> mapper,
      @Param(value = "stop", dNPM = "ea.sc.nOfQualityEvaluations()") Predicate<? super POCPopulationState<Individual<G, S, Q>, G, S, Q, QualityBasedProblem<S, Q>>> stopCondition,
      @Param("iComparators") List<PartialComparator<? super Individual<G, S, Q>>> additionalIndividualComparators
  ) {
    return exampleS -> {
      Representation<G> r = representation.apply(mapper.exampleFor(exampleS));
      return new RandomSearch<>(
          mapper.mapperFor(exampleS),
          r.factory(),
          stopCondition,
          additionalIndividualComparators
      );
    };
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <G, S, Q> Function<S, RandomWalk<G, S, Q>> randomWalk(
      @Param(value = "name", dS = "rw") String name,
      @Param("representation") Function<G, Representation<G>> representation,
      @Param(value = "mapper", dNPM = "ea.m.identity()") InvertibleMapper<G, S> mapper,
      @Param(value = "stop", dNPM = "ea.sc.nOfQualityEvaluations()") Predicate<? super POCPopulationState<Individual<G, S, Q>, G, S, Q, QualityBasedProblem<S, Q>>> stopCondition,
      @Param("iComparators") List<PartialComparator<? super Individual<G, S, Q>>> additionalIndividualComparators
  ) {
    return exampleS -> {
      Representation<G> r = representation.apply(mapper.exampleFor(exampleS));
      return new RandomWalk<>(
          mapper.mapperFor(exampleS),
          r.factory(),
          stopCondition,
          r.mutations().getFirst(),
          additionalIndividualComparators
      );
    };
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <G, S, Q> Function<S, ScheduledFidelityStandardEvolver<G, S, Q>> scheduledMfGa(
      @Param(value = "name", iS = "scheduledMfGa[{schedule.name}]") String name,
      @Param("representation") Function<G, Representation<G>> representation,
      @Param(value = "mapper", dNPM = "ea.m.identity()") InvertibleMapper<G, S> mapper,
      @Param(value = "crossoverP", dD = 0.8d) double crossoverP,
      @Param(value = "tournamentRate", dD = 0.05d) double tournamentRate,
      @Param(value = "minNTournament", dI = 3) int minNTournament,
      @Param(value = "nPop", dI = 100) int nPop,
      @Param(value = "stop", dNPM = "ea.sc.nOfQualityEvaluations()") ProgressBasedStopCondition<? super MultiFidelityPOCPopulationState<Individual<G, S, Q>, G, S, Q, MultifidelityQualityBasedProblem<S, Q>>> stopCondition,
      @Param(value = "maxUniquenessAttempts", dI = 100) int maxUniquenessAttempts,
      @Param("iComparators") List<PartialComparator<? super Individual<G, S, Q>>> additionalIndividualComparators,
      @Param(value = "schedule", dNPM = "ea.schedule.flat()") DoubleUnaryOperator schedule
  ) {
    return exampleS -> {
      Representation<G> r = representation.apply(mapper.exampleFor(exampleS));
      return new ScheduledFidelityStandardEvolver<>(
          mapper.mapperFor(exampleS),
          r.factory(),
          nPop,
          stopCondition,
          r.geneticOperators(crossoverP),
          new Tournament(Math.max(minNTournament, (int) Math.ceil((double) nPop * tournamentRate))),
          new Last(),
          nPop,
          true,
          maxUniquenessAttempts,
          additionalIndividualComparators,
          schedule
      );
    };
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <S, Q> Function<S, SimpleEvolutionaryStrategy<S, Q>> simpleEs(
      @Param(value = "name", dS = "es") String name,
      @Param(value = "mapper", dNPM = "ea.m.identity()") InvertibleMapper<List<Double>, S> mapper,
      @Param(value = "factory", dNPM = "ea.r.f.dsUniform()") Function<List<Double>, Factory<List<Double>>> factory,
      @Param(value = "sigma", dD = 0.35d) double sigma,
      @Param(value = "parentsRate", dD = 0.33d) double parentsRate,
      @Param(value = "nOfElites", dI = 1) int nOfElites,
      @Param(value = "nPop", dI = 30) int nPop,
      @Param(value = "stop", dNPM = "ea.sc.nOfQualityEvaluations()") Predicate<? super ListPopulationState<Individual<List<Double>, S, Q>, List<Double>, S, Q, TotalOrderQualityBasedProblem<S, Q>>> stopCondition,
      @Param(value = "remap") boolean remap
  ) {
    return exampleS -> new SimpleEvolutionaryStrategy<>(
        mapper.mapperFor(exampleS),
        factory.apply(mapper.exampleFor(exampleS)),
        nPop,
        stopCondition,
        nOfElites,
        (int) Math.round(nPop * parentsRate),
        sigma,
        remap
    );
  }
}