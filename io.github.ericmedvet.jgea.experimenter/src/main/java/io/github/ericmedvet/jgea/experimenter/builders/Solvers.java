/*-
 * ========================LICENSE_START=================================
 * jgea-experimenter
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

package io.github.ericmedvet.jgea.experimenter.builders;

import io.github.ericmedvet.jgea.core.InvertibleMapper;
import io.github.ericmedvet.jgea.core.distance.Jaccard;
import io.github.ericmedvet.jgea.core.operator.GeneticOperator;
import io.github.ericmedvet.jgea.core.operator.Mutation;
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
import io.github.ericmedvet.jgea.core.solver.cabea.CellularAutomataBasedSolver;
import io.github.ericmedvet.jgea.core.solver.cabea.SubstrateFiller;
import io.github.ericmedvet.jgea.core.solver.es.CMAEvolutionaryStrategy;
import io.github.ericmedvet.jgea.core.solver.es.OpenAIEvolutionaryStrategy;
import io.github.ericmedvet.jgea.core.solver.es.SimpleEvolutionaryStrategy;
import io.github.ericmedvet.jgea.core.solver.mapelites.MapElites;
import io.github.ericmedvet.jgea.core.solver.pso.ParticleSwarmOptimization;
import io.github.ericmedvet.jgea.core.solver.speciation.LazySpeciator;
import io.github.ericmedvet.jgea.core.solver.speciation.SpeciatedEvolver;
import io.github.ericmedvet.jgea.experimenter.Representation;
import io.github.ericmedvet.jnb.core.Discoverable;
import io.github.ericmedvet.jnb.core.Param;
import io.github.ericmedvet.jnb.datastructure.Grid;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.DoubleStream;

// import stuff for CoME
import io.github.ericmedvet.jgea.core.solver.mapelites.CoMapElites;
import java.util.function.BiFunction;
import java.util.concurrent.ExecutorService;
import java.util.random.RandomGenerator;
import io.github.ericmedvet.jnb.datastructure.Pair;

@Discoverable(prefixTemplate = "ea.solver|s")
public class Solvers {

  private Solvers() {}

  @SuppressWarnings("unused")
  public static <G, S, Q> Function<S, CellularAutomataBasedSolver<G, S, Q>> cabea(
      @Param(value = "name", dS = "cabea") String name,
      @Param("representation") Function<G, Representation<G>> representation,
      @Param(value = "mapper", dNPM = "ea.m.identity()") InvertibleMapper<G, S> mapper,
      @Param(value = "keepProbability", dD = 0.00d) double keepProbability,
      @Param(value = "crossoverP", dD = 0.8d) double crossoverP,
      @Param(value = "nTour", dI = 1) int nTour,
      @Param(value = "nEval", dI = 1000) int nEval,
      @Param(value = "toroidal", dB = true) boolean toroidal,
      @Param(value = "mooreRadius", dI = 1) int mooreRadius,
      @Param(value = "gridSize", dI = 11) int gridSize,
      @Param(value = "substrate", dS = "empty") SubstrateFiller.Predefined substrate) {
    return exampleS -> {
      Representation<G> r = representation.apply(mapper.exampleFor(exampleS));
      return new CellularAutomataBasedSolver<>(
          mapper.mapperFor(exampleS),
          r.factory(),
          StopConditions.nOfFitnessEvaluations(nEval),
          substrate.apply(Grid.create(gridSize, gridSize, true)),
          new CellularAutomataBasedSolver.MooreNeighborhood(mooreRadius, toroidal),
          keepProbability,
          r.geneticOperators(crossoverP),
          new Tournament(nTour));
    };
  }

  @SuppressWarnings("unused")
  public static <S, Q> Function<S, CMAEvolutionaryStrategy<S, Q>> cmaEs(
      @Param(value = "name", dS = "cmaEs") String name,
      @Param(value = "mapper", dNPM = "ea.m.identity()") InvertibleMapper<List<Double>, S> mapper,
      @Param(value = "initialMinV", dD = -1d) double initialMinV,
      @Param(value = "initialMaxV", dD = 1d) double initialMaxV,
      @Param(value = "nEval", dI = 1000) int nEval) {
    return exampleS -> new CMAEvolutionaryStrategy<>(
        mapper.mapperFor(exampleS),
        Representations.doubleString(initialMinV, initialMaxV, 0)
            .apply(mapper.exampleFor(exampleS))
            .factory(),
        StopConditions.nOfFitnessEvaluations(nEval));
  }

  @SuppressWarnings("unused")
  public static <S, Q> Function<S, DifferentialEvolution<S, Q>> differentialEvolution(
      @Param(value = "name", dS = "de") String name,
      @Param(value = "mapper", dNPM = "ea.m.identity()") InvertibleMapper<List<Double>, S> mapper,
      @Param(value = "initialMinV", dD = -1d) double initialMinV,
      @Param(value = "initialMaxV", dD = 1d) double initialMaxV,
      @Param(value = "populationSize", dI = 15) int populationSize,
      @Param(value = "nEval", dI = 1000) int nEval,
      @Param(value = "differentialWeight", dD = 0.5) double differentialWeight,
      @Param(value = "crossoverP", dD = 0.8) double crossoverP,
      @Param(value = "remap") boolean remap) {
    return exampleS -> new DifferentialEvolution<>(
        mapper.mapperFor(exampleS),
        Representations.doubleString(initialMinV, initialMaxV, 0)
            .apply(mapper.exampleFor(exampleS))
            .factory(),
        populationSize,
        StopConditions.nOfFitnessEvaluations(nEval),
        differentialWeight,
        crossoverP,
        remap);
  }

  @SuppressWarnings("unused")
  public static <G, S, Q> Function<S, StandardEvolver<G, S, Q>> ga(
      @Param(value = "name", dS = "ga") String name,
      @Param("representation") Function<G, Representation<G>> representation,
      @Param(value = "mapper", dNPM = "ea.m.identity()") InvertibleMapper<G, S> mapper,
      @Param(value = "crossoverP", dD = 0.8d) double crossoverP,
      @Param(value = "tournamentRate", dD = 0.05d) double tournamentRate,
      @Param(value = "minNTournament", dI = 3) int minNTournament,
      @Param(value = "nPop", dI = 100) int nPop,
      @Param(value = "nEval", dI = 1000) int nEval,
      @Param(value = "maxUniquenessAttempts", dI = 100) int maxUniquenessAttempts,
      @Param(value = "remap") boolean remap) {
    return exampleS -> {
      Representation<G> r = representation.apply(mapper.exampleFor(exampleS));
      return new StandardEvolver<>(
          mapper.mapperFor(exampleS),
          r.factory(),
          nPop,
          StopConditions.nOfFitnessEvaluations(nEval),
          r.geneticOperators(crossoverP),
          new Tournament(Math.max(minNTournament, (int) Math.ceil((double) nPop * tournamentRate))),
          new Last(),
          nPop,
          true,
          maxUniquenessAttempts,
          remap);
    };
  }

  @SuppressWarnings("unused")
  public static <G, S, Q> Function<S, MapElites<G, S, Q>> mapElites(
      @Param(value = "name", dS = "me") String name,
      @Param("representation") Function<G, Representation<G>> representation,
      @Param(value = "mapper", dNPM = "ea.m.identity()") InvertibleMapper<G, S> mapper,
      @Param(value = "nPop", dI = 100) int nPop,
      @Param(value = "nEval", dI = 1000) int nEval,
      @Param("descriptors") List<MapElites.Descriptor<G, S, Q>> descriptors) {
    return exampleS -> {
      Representation<G> r = representation.apply(mapper.exampleFor(exampleS));
      return new MapElites<>(
          mapper.mapperFor(exampleS),
          r.factory(),
          StopConditions.nOfFitnessEvaluations(nEval),
          r.mutations().get(0),
          nPop,
          descriptors);
    };
  }

  @SuppressWarnings("unused")
  public static <G, S> Function<S, NsgaII<G, S>> nsga2(
      @Param(value = "name", dS = "nsga2") String name,
      @Param("representation") Function<G, Representation<G>> representation,
      @Param(value = "mapper", dNPM = "ea.m.identity()") InvertibleMapper<G, S> mapper,
      @Param(value = "crossoverP", dD = 0.8d) double crossoverP,
      @Param(value = "nPop", dI = 100) int nPop,
      @Param(value = "nEval", dI = 1000) int nEval,
      @Param(value = "maxUniquenessAttempts", dI = 100) int maxUniquenessAttempts,
      @Param(value = "remap") boolean remap) {
    return exampleS -> {
      Representation<G> r = representation.apply(mapper.exampleFor(exampleS));
      return new NsgaII<>(
          mapper.mapperFor(exampleS),
          r.factory(),
          nPop,
          StopConditions.nOfFitnessEvaluations(nEval),
          r.geneticOperators(crossoverP),
          maxUniquenessAttempts,
          remap);
    };
  }

  @SuppressWarnings("unused")
  public static <S, Q> Function<S, SpeciatedEvolver<Graph<Node, OperatorGraph.NonValuedArc>, S, Q>> oGraphea(
      @Param(value = "name", dS = "oGraphea") String name,
      @Param(value = "mapper", dNPM = "ea.m.identity()")
          InvertibleMapper<Graph<Node, OperatorGraph.NonValuedArc>, S> mapper,
      @Param(value = "minConst", dD = 0d) double minConst,
      @Param(value = "maxConst", dD = 5d) double maxConst,
      @Param(value = "nConst", dI = 10) int nConst,
      @Param(
              value = "operators",
              dSs = {"addition", "subtraction", "multiplication", "prot_division", "prot_log"})
          List<BaseOperator> operators,
      @Param(value = "nPop", dI = 100) int nPop,
      @Param(value = "nEval", dI = 1000) int nEval,
      @Param(value = "arcAdditionRate", dD = 3d) double arcAdditionRate,
      @Param(value = "arcRemovalRate", dD = 0.1d) double arcRemovalRate,
      @Param(value = "nodeAdditionRate", dD = 1d) double nodeAdditionRate,
      @Param(value = "nPop", dI = 5) int minSpeciesSizeForElitism,
      @Param(value = "rankBase", dD = 0.75d) double rankBase,
      @Param(value = "remap") boolean remap) {
    return exampleS -> {
      Map<GeneticOperator<Graph<Node, OperatorGraph.NonValuedArc>>, Double> geneticOperators = Map.ofEntries(
          Map.entry(
              new NodeAddition<Node, OperatorGraph.NonValuedArc>(
                      OperatorNode.sequentialIndexFactory(operators.toArray(BaseOperator[]::new)),
                      Mutation.copy(),
                      Mutation.copy())
                  .withChecker(OperatorGraph.checker()),
              nodeAdditionRate),
          Map.entry(
              new ArcAddition<Node, OperatorGraph.NonValuedArc>(r -> OperatorGraph.NON_VALUED_ARC, false)
                  .withChecker(OperatorGraph.checker()),
              arcAdditionRate),
          Map.entry(
              new ArcRemoval<Node, OperatorGraph.NonValuedArc>(node -> (node instanceof Input)
                      || (node instanceof Constant)
                      || (node instanceof Output))
                  .withChecker(OperatorGraph.checker()),
              arcRemovalRate));
      Graph<Node, OperatorGraph.NonValuedArc> graph = mapper.exampleFor(exampleS);
      double constStep = (maxConst - minConst) / nConst;
      List<Double> constants = DoubleStream.iterate(minConst, d -> d + constStep)
          .limit(nConst)
          .boxed()
          .toList();
      return new SpeciatedEvolver<>(
          mapper.mapperFor(exampleS),
          new ShallowFactory(
              graph.nodes().stream()
                  .filter(n -> n instanceof Input)
                  .map(n -> ((Input) n).getName())
                  .distinct()
                  .toList(),
              graph.nodes().stream()
                  .filter(n -> n instanceof Output)
                  .map(n -> ((Output) n).getName())
                  .distinct()
                  .toList(),
              constants),
          StopConditions.nOfFitnessEvaluations(nEval),
          geneticOperators,
          nPop,
          remap,
          minSpeciesSizeForElitism,
          new LazySpeciator<>(
              (new Jaccard<Node>()).on(i -> i.genotype().nodes()), 0.25),
          rankBase);
    };
  }

  @SuppressWarnings("unused")
  public static <S, Q> Function<S, OpenAIEvolutionaryStrategy<S, Q>> openAiEs(
      @Param(value = "name", dS = "openAiEs") String name,
      @Param(value = "mapper", dNPM = "ea.m.identity()") InvertibleMapper<List<Double>, S> mapper,
      @Param(value = "initialMinV", dD = -1d) double initialMinV,
      @Param(value = "initialMaxV", dD = 1d) double initialMaxV,
      @Param(value = "sigma", dD = 0.02d) double sigma,
      @Param(value = "batchSize", dI = 30) int batchSize,
      @Param(value = "stepSize", dD = 0.02d) double stepSize,
      @Param(value = "beta1", dD = 0.9d) double beta1,
      @Param(value = "beta2", dD = 0.999d) double beta2,
      @Param(value = "epsilon", dD = 1e-8) double epsilon,
      @Param(value = "nEval", dI = 1000) int nEval) {
    return exampleS -> new OpenAIEvolutionaryStrategy<>(
        mapper.mapperFor(exampleS),
        Representations.doubleString(initialMinV, initialMaxV, 0)
            .apply(mapper.exampleFor(exampleS))
            .factory(),
        StopConditions.nOfFitnessEvaluations(nEval),
        batchSize,
        sigma,
        stepSize,
        beta1,
        beta2,
        epsilon);
  }

  @SuppressWarnings("unused")
  public static <S, Q> Function<S, ParticleSwarmOptimization<S, Q>> pso(
      @Param(value = "name", dS = "pso") String name,
      @Param(value = "mapper", dNPM = "ea.m.identity()") InvertibleMapper<List<Double>, S> mapper,
      @Param(value = "initialMinV", dD = -1d) double initialMinV,
      @Param(value = "initialMaxV", dD = 1d) double initialMaxV,
      @Param(value = "nEval", dI = 1000) int nEval,
      @Param(value = "nPop", dI = 100) int nPop,
      @Param(value = "w", dD = 0.8d) double w,
      @Param(value = "phiParticle", dD = 1.5d) double phiParticle,
      @Param(value = "phiGlobal", dD = 1.5d) double phiGlobal) {
    return exampleS -> new ParticleSwarmOptimization<>(
        mapper.mapperFor(exampleS),
        Representations.doubleString(initialMinV, initialMaxV, 0)
            .apply(mapper.exampleFor(exampleS))
            .factory(),
        StopConditions.nOfFitnessEvaluations(nEval),
        nPop,
        w,
        phiParticle,
        phiGlobal);
  }

  @SuppressWarnings("unused")
  public static <G, S, Q> Function<S, RandomSearch<G, S, Q>> randomSearch(
      @Param(value = "name", dS = "rs") String name,
      @Param("representation") Function<G, Representation<G>> representation,
      @Param(value = "mapper", dNPM = "ea.m.identity()") InvertibleMapper<G, S> mapper,
      @Param(value = "nEval", dI = 1000) int nEval) {
    return exampleS -> {
      Representation<G> r = representation.apply(mapper.exampleFor(exampleS));
      return new RandomSearch<>(
          mapper.mapperFor(exampleS), r.factory(), StopConditions.nOfFitnessEvaluations(nEval));
    };
  }

  @SuppressWarnings("unused")
  public static <G, S, Q> Function<S, RandomWalk<G, S, Q>> randomWalk(
      @Param(value = "name", dS = "rw") String name,
      @Param("representation") Function<G, Representation<G>> representation,
      @Param(value = "mapper", dNPM = "ea.m.identity()") InvertibleMapper<G, S> mapper,
      @Param(value = "nEval", dI = 1000) int nEval) {
    return exampleS -> {
      Representation<G> r = representation.apply(mapper.exampleFor(exampleS));
      return new RandomWalk<>(
          mapper.mapperFor(exampleS),
          r.factory(),
          StopConditions.nOfFitnessEvaluations(nEval),
          r.mutations().get(0));
    };
  }




  @SuppressWarnings("unused")
  public static <S, Q> Function<S, SimpleEvolutionaryStrategy<S, Q>> simpleEs(
      @Param(value = "name", dS = "es") String name,
      @Param(value = "mapper", dNPM = "ea.m.identity()") InvertibleMapper<List<Double>, S> mapper,
      @Param(value = "initialMinV", dD = -1d) double initialMinV,
      @Param(value = "initialMaxV", dD = 1d) double initialMaxV,
      @Param(value = "sigma", dD = 0.35d) double sigma,
      @Param(value = "parentsRate", dD = 0.33d) double parentsRate,
      @Param(value = "nOfElites", dI = 1) int nOfElites,
      @Param(value = "nPop", dI = 30) int nPop,
      @Param(value = "nEval", dI = 1000) int nEval,
      @Param(value = "remap") boolean remap) {
    return exampleS -> new SimpleEvolutionaryStrategy<>(
        mapper.mapperFor(exampleS),
        Representations.doubleString(initialMinV, initialMaxV, 0)
            .apply(mapper.exampleFor(exampleS))
            .factory(),
        nPop,
        StopConditions.nOfFitnessEvaluations(nEval),
        nOfElites,
        (int) Math.round(nPop * parentsRate),
        sigma,
        remap);
  }




  @SuppressWarnings("unused")
  public static <G1, G2, S1, S2, S, Q> Function<S, CoMapElites<G1, G2, S1, S2, S, Q>> coMapElites(
      @Param(value = "name", dS = "CoMapElites") String name,
      @Param("representation1") Function<G1, Representation<G1>> representation1,
      @Param("representation2") Function<G2, Representation<G2>> representation2,
      @Param(value = "mapper1", dNPM = "ea.m.identity()") InvertibleMapper<G1, S1> mapper1,
      @Param(value = "mapper2", dNPM = "ea.m.identity()") InvertibleMapper<G2, S2> mapper2,
      @Param("merger") BiFunction<S1, S2, S> BiFunctionMerger, //attention here
      @Param("inverseMerger") InvertibleMapper<Pair<S1, S2>, S> InvertibleMapperMerger, //attention here
      @Param(value = "descriptors1") List<MapElites.Descriptor<G1, S1, Q>> descriptors1,
      @Param(value = "descriptors2") List<MapElites.Descriptor<G2, S2, Q>> descriptors2,
      @Param(value = "nEval", dI = 1000) int nEval,
      @Param(value = "populationSize", dI = 100) int populationSize,
      @Param(value = "nOfOffspring", dI = 50) int nOfOffspring,
      @Param(value = "strategy", dS = "IDENTITY") CoMapElites.Strategy strategy,
      @Param("executor") ExecutorService executor,
      @Param("randomGenerator") RandomGenerator random) {

    return exampleS -> {
      // Create representations based on the inverse mapper and solution merger
      Pair<S1, S2> splitExample = InvertibleMapperMerger.exampleFor(exampleS); // attention here
      Representation<G1> r1 = representation1.apply(mapper1.exampleFor(splitExample.first())); //attention here
      Representation<G2> r2 = representation2.apply(mapper2.exampleFor(splitExample.second())); //attention here

      if (descriptors1.isEmpty() || descriptors2.isEmpty()) {
        throw new IllegalArgumentException("Descriptors for representations must be initialized.");
      }

      return new CoMapElites<>(
          StopConditions.nOfFitnessEvaluations(nEval),  // Stop condition
          r1.factory(),                                 // Factory for G1 (attention here)
          r2.factory(),                                 // Factory for G2 (attention here)
          mapper1.mapperFor(splitExample.first()),      // Solution mapper for G1 -> S1
          mapper2.mapperFor(splitExample.second()),     // Solution mapper for G2 -> S2
          BiFunctionMerger,
          descriptors1,                                 // Descriptors for G1
          descriptors2,                                 // Descriptors for G2
          r1.mutations().get(0),                        // Mutation for G1
          r2.mutations().get(0),                        // Mutation for G2
          populationSize,                               // Population size
          nOfOffspring,                                 // Number of offspring
          strategy                                      // Strategy (it is an enum)
      );
    };
  }








}
