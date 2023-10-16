/*
 * Copyright 2023 eric
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.ericmedvet.jgea.experimenter.builders;

import io.github.ericmedvet.jgea.core.Factory;
import io.github.ericmedvet.jgea.core.IndependentFactory;
import io.github.ericmedvet.jgea.core.distance.Jaccard;
import io.github.ericmedvet.jgea.core.operator.Crossover;
import io.github.ericmedvet.jgea.core.operator.GeneticOperator;
import io.github.ericmedvet.jgea.core.operator.Mutation;
import io.github.ericmedvet.jgea.core.problem.QualityBasedProblem;
import io.github.ericmedvet.jgea.core.representation.graph.*;
import io.github.ericmedvet.jgea.core.representation.graph.numeric.Constant;
import io.github.ericmedvet.jgea.core.representation.graph.numeric.Input;
import io.github.ericmedvet.jgea.core.representation.graph.numeric.Output;
import io.github.ericmedvet.jgea.core.representation.graph.numeric.operatorgraph.BaseOperator;
import io.github.ericmedvet.jgea.core.representation.graph.numeric.operatorgraph.OperatorGraph;
import io.github.ericmedvet.jgea.core.representation.graph.numeric.operatorgraph.OperatorNode;
import io.github.ericmedvet.jgea.core.representation.graph.numeric.operatorgraph.ShallowFactory;
import io.github.ericmedvet.jgea.core.representation.sequence.FixedLengthListFactory;
import io.github.ericmedvet.jgea.core.representation.sequence.bit.BitString;
import io.github.ericmedvet.jgea.core.representation.sequence.bit.BitStringFactory;
import io.github.ericmedvet.jgea.core.representation.sequence.bit.BitStringFlipMutation;
import io.github.ericmedvet.jgea.core.representation.sequence.bit.BitStringUniformCrossover;
import io.github.ericmedvet.jgea.core.representation.sequence.integer.IntString;
import io.github.ericmedvet.jgea.core.representation.sequence.integer.IntStringFlipMutation;
import io.github.ericmedvet.jgea.core.representation.sequence.integer.IntStringUniformCrossover;
import io.github.ericmedvet.jgea.core.representation.sequence.integer.UniformIntStringFactory;
import io.github.ericmedvet.jgea.core.representation.sequence.numeric.GaussianMutation;
import io.github.ericmedvet.jgea.core.representation.sequence.numeric.HypercubeGeometricCrossover;
import io.github.ericmedvet.jgea.core.representation.sequence.numeric.UniformDoubleFactory;
import io.github.ericmedvet.jgea.core.representation.tree.*;
import io.github.ericmedvet.jgea.core.representation.tree.numeric.Element;
import io.github.ericmedvet.jgea.core.selector.Last;
import io.github.ericmedvet.jgea.core.selector.Tournament;
import io.github.ericmedvet.jgea.core.solver.*;
import io.github.ericmedvet.jgea.core.solver.speciation.LazySpeciator;
import io.github.ericmedvet.jgea.core.solver.speciation.SpeciatedEvolver;
import io.github.ericmedvet.jgea.core.solver.state.POSetPopulationState;
import io.github.ericmedvet.jgea.experimenter.InvertibleMapper;
import io.github.ericmedvet.jnb.core.Param;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

/**
 * @author "Eric Medvet" on 2022/11/21 for 2d-robot-evolution
 */
public class Solvers {

  private Solvers() {
  }

  @SuppressWarnings("unused")
  public static <S, Q> Function<S, StandardEvolver<POSetPopulationState<BitString, S, Q>, QualityBasedProblem<S, Q>,
      BitString, S, Q>> bitStringGa(
      @Param(value = "mapper") InvertibleMapper<BitString, S> mapper,
      @Param(value = "crossoverP", dD = 0.8d) double crossoverP,
      @Param(value = "pMut", dD = 0.01d) double pMut,
      @Param(value = "tournamentRate", dD = 0.05d) double tournamentRate,
      @Param(value = "minNTournament", dI = 3) int minNTournament,
      @Param(value = "nPop", dI = 100) int nPop,
      @Param(value = "nEval") int nEval,
      @Param(value = "diversity", dB = true) boolean diversity,
      @Param(value = "remap") boolean remap
  ) {
    return exampleS -> {
      BitString exampleGenotype = mapper.exampleFor(exampleS);
      IndependentFactory<BitString> factory = new BitStringFactory(exampleGenotype.size());
      Map<GeneticOperator<BitString>, Double> geneticOperators = Map.ofEntries(
          Map.entry(new BitStringFlipMutation(pMut), 1d - crossoverP),
          Map.entry(new BitStringUniformCrossover().andThen(new BitStringFlipMutation(pMut)), crossoverP)
      );
      if (!diversity) {
        return new StandardEvolver<>(
            mapper.mapperFor(exampleS),
            factory,
            nPop,
            StopConditions.nOfFitnessEvaluations(nEval),
            geneticOperators,
            new Tournament(Math.max(minNTournament, (int) Math.ceil((double) nPop * tournamentRate))),
            new Last(),
            nPop,
            true,
            remap,
            (p, r) -> new POSetPopulationState<>()
        );
      } else {
        return new StandardWithEnforcedDiversityEvolver<>(
            mapper.mapperFor(exampleS),
            factory,
            nPop,
            StopConditions.nOfFitnessEvaluations(nEval),
            geneticOperators,
            new Tournament(Math.max(minNTournament, (int) Math.ceil((double) nPop * tournamentRate))),
            new Last(),
            nPop,
            true,
            remap,
            (p, r) -> new POSetPopulationState<>(),
            100
        );
      }
    };
  }

  @SuppressWarnings("unused")
  public static <S, Q> Function<S, RandomWalk<QualityBasedProblem<S, Q>, BitString, S, Q>> bitStringRandomWalk(
      @Param(value = "mapper") InvertibleMapper<BitString, S> mapper,
      @Param(value = "pMut", dD = 0.01d) double pMut,
      @Param(value = "nEval") int nEval
  ) {
    return exampleS -> {
      BitString exampleGenotype = mapper.exampleFor(exampleS);
      return new RandomWalk<>(
          mapper.mapperFor(exampleS),
          new BitStringFactory(exampleGenotype.size()),
          StopConditions.nOfFitnessEvaluations(nEval),
          new BitStringFlipMutation(pMut)
      );
    };
  }

  @SuppressWarnings("unused")
  public static <S, Q> Function<S, CMAEvolutionaryStrategy<S, Q>> cmaEs(
      @Param(value = "mapper") InvertibleMapper<List<Double>, S> mapper,
      @Param(value = "initialMinV", dD = -1d) double initialMinV,
      @Param(value = "initialMaxV", dD = 1d) double initialMaxV,
      @Param(value = "nEval") int nEval
  ) {
    return exampleS -> new CMAEvolutionaryStrategy<>(
        mapper.mapperFor(exampleS),
        new FixedLengthListFactory<>(
            mapper.exampleFor(exampleS).size(),
            new UniformDoubleFactory(initialMinV, initialMaxV)
        ),
        StopConditions.nOfFitnessEvaluations(nEval)
    );
  }

  public static <S, Q> Function<S, DifferentialEvolution<S, Q>> differentialEvolutionEs(
      @Param(value = "mapper") InvertibleMapper<List<Double>, S> mapper,
      @Param(value = "initialMinV", dD = -1d) double initialMinV,
      @Param(value = "initialMaxV", dD = 1d) double initialMaxV,
      @Param(value = "batchSize", dI = 15) int populationSize,
      @Param(value = "nEval") int nEval,
      @Param(value = "differentialWeight", dD = 0.5) double differentialWeight,
      @Param(value = "crossoverProb", dD = 0.8) double crossoverProb,
      @Param(value = "remap") boolean remap
  ) {
    return exampleS -> new DifferentialEvolution<>(
        mapper.mapperFor(exampleS),
        new FixedLengthListFactory<>(
            mapper.exampleFor(exampleS).size(),
            new UniformDoubleFactory(initialMinV, initialMaxV)
        ),
        populationSize,
        StopConditions.nOfFitnessEvaluations(nEval),
        differentialWeight,
        crossoverProb,
        remap
    );
  }

  @SuppressWarnings("unused")
  public static <S, Q> Function<S, StandardEvolver<POSetPopulationState<List<Double>, S, Q>, QualityBasedProblem<S, Q>,
      List<Double>, S, Q>> doubleStringGa(
      @Param(value = "mapper") InvertibleMapper<List<Double>, S> mapper,
      @Param(value = "initialMinV", dD = -1d) double initialMinV,
      @Param(value = "initialMaxV", dD = 1d) double initialMaxV,
      @Param(value = "crossoverP", dD = 0.8d) double crossoverP,
      @Param(value = "sigmaMut", dD = 0.35d) double sigmaMut,
      @Param(value = "tournamentRate", dD = 0.05d) double tournamentRate,
      @Param(value = "minNTournament", dI = 3) int minNTournament,
      @Param(value = "nPop", dI = 100) int nPop,
      @Param(value = "nEval") int nEval,
      @Param(value = "diversity") boolean diversity,
      @Param(value = "remap") boolean remap
  ) {
    return exampleS -> {
      IndependentFactory<List<Double>> doublesFactory = new FixedLengthListFactory<>(
          mapper.exampleFor(exampleS).size(),
          new UniformDoubleFactory(initialMinV, initialMaxV)
      );
      Crossover<List<Double>> crossover = new HypercubeGeometricCrossover();
      Map<GeneticOperator<List<Double>>, Double> geneticOperators = Map.ofEntries(
          Map.entry(new GaussianMutation(sigmaMut), 1d - crossoverP),
          Map.entry(crossover.andThen(new GaussianMutation(sigmaMut)), crossoverP)
      );
      if (!diversity) {
        return new StandardEvolver<>(
            mapper.mapperFor(exampleS),
            doublesFactory,
            nPop,
            StopConditions.nOfFitnessEvaluations(nEval),
            geneticOperators,
            new Tournament(Math.max(minNTournament, (int) Math.ceil((double) nPop * tournamentRate))),
            new Last(),
            nPop,
            true,
            remap,
            (p, r) -> new POSetPopulationState<>()
        );
      } else {
        return new StandardWithEnforcedDiversityEvolver<>(
            mapper.mapperFor(exampleS),
            doublesFactory,
            nPop,
            StopConditions.nOfFitnessEvaluations(nEval),
            geneticOperators,
            new Tournament(Math.max(minNTournament, (int) Math.ceil((double) nPop * tournamentRate))),
            new Last(),
            nPop,
            true,
            remap,
            (p, r) -> new POSetPopulationState<>(),
            100
        );
      }
    };
  }

  @SuppressWarnings("unused")
  public static <S, Q> Function<S, StandardEvolver<POSetPopulationState<IntString, S, Q>, QualityBasedProblem<S, Q>,
      IntString, S, Q>> intStringGa(
      @Param(value = "mapper") InvertibleMapper<IntString, S> mapper,
      @Param(value = "crossoverP", dD = 0.8d) double crossoverP,
      @Param(value = "pMut", dD = 0.01d) double pMut,
      @Param(value = "tournamentRate", dD = 0.05d) double tournamentRate,
      @Param(value = "minNTournament", dI = 3) int minNTournament,
      @Param(value = "nPop", dI = 100) int nPop,
      @Param(value = "nEval") int nEval,
      @Param(value = "diversity", dB = true) boolean diversity,
      @Param(value = "remap") boolean remap
  ) {
    return exampleS -> {
      IntString exampleGenotype = mapper.exampleFor(exampleS);
      IndependentFactory<IntString> factory = new UniformIntStringFactory(
          exampleGenotype.lowerBound(),
          exampleGenotype.upperBound(),
          exampleGenotype.size()
      );
      Map<GeneticOperator<IntString>, Double> geneticOperators = Map.ofEntries(
          Map.entry(new IntStringFlipMutation(pMut), 1d - crossoverP),
          Map.entry(new IntStringUniformCrossover().andThen(new IntStringFlipMutation(pMut)), crossoverP)
      );
      if (!diversity) {
        return new StandardEvolver<>(
            mapper.mapperFor(exampleS),
            factory,
            nPop,
            StopConditions.nOfFitnessEvaluations(nEval),
            geneticOperators,
            new Tournament(Math.max(minNTournament, (int) Math.ceil((double) nPop * tournamentRate))),
            new Last(),
            nPop,
            true,
            remap,
            (p, r) -> new POSetPopulationState<>()
        );
      } else {
        return new StandardWithEnforcedDiversityEvolver<>(
            mapper.mapperFor(exampleS),
            factory,
            nPop,
            StopConditions.nOfFitnessEvaluations(nEval),
            geneticOperators,
            new Tournament(Math.max(minNTournament, (int) Math.ceil((double) nPop * tournamentRate))),
            new Last(),
            nPop,
            true,
            remap,
            (p, r) -> new POSetPopulationState<>(),
            100
        );
      }
    };
  }

  @SuppressWarnings("unused")
  public static <S, Q> Function<S, StandardEvolver<POSetPopulationState<List<Tree<Element>>, S, Q>,
      QualityBasedProblem<S, Q>,
      List<Tree<Element>>, S, Q>> multiSRTreeGp(
      @Param(value = "mapper") InvertibleMapper<List<Tree<Element>>, S> mapper,
      @Param(value = "constants", dDs = {0.1, 1, 10}) List<Double> constants,
      @Param(value = "operators", dSs = {
          "addition",
          "subtraction",
          "multiplication",
          "prot_division",
          "prot_log"
      }) List<Element.Operator> operators,
      @Param(value = "minTreeH", dI = 4) int minTreeH,
      @Param(value = "maxTreeH", dI = 10) int maxTreeH,
      @Param(value = "crossoverP", dD = 0.8d) double crossoverP,
      @Param(value = "tournamentRate", dD = 0.05d) double tournamentRate,
      @Param(value = "minNTournament", dI = 3) int minNTournament,
      @Param(value = "nPop", dI = 100) int nPop,
      @Param(value = "nEval") int nEval,
      @Param(value = "diversity", dB = true) boolean diversity,
      @Param(value = "nAttemptsDiversity", dI = 100) int nAttemptsDiversity,
      @Param(value = "remap") boolean remap
  ) {
    return exampleS -> {
      List<Element.Variable> variables = mapper.exampleFor(exampleS).stream().map(t -> t.visitDepth()
          .stream()
          .filter(e -> e instanceof Element.Variable)
          .map(e -> ((Element.Variable) e).name())
          .toList()).flatMap(List::stream).distinct().map(Element.Variable::new).toList();
      List<Element.Constant> constantElements = constants.stream()
          .map(Element.Constant::new)
          .toList();
      IndependentFactory<Element> terminalFactory = IndependentFactory.oneOf(
          IndependentFactory.picker(variables),
          IndependentFactory.picker(constantElements)
      );
      IndependentFactory<Element> nonTerminalFactory = IndependentFactory.picker(operators);
      IndependentFactory<List<Tree<Element>>> treeListFactory = new FixedLengthListFactory<>(
          mapper.exampleFor(exampleS).size(),
          new TreeIndependentFactory<>(minTreeH, maxTreeH, x -> 2, nonTerminalFactory, terminalFactory, 0.5)
      );
      // single tree factory
      TreeBuilder<Element> treeBuilder = new GrowTreeBuilder<>(x -> 2, nonTerminalFactory, terminalFactory);
      // subtree between same position trees
      SubtreeCrossover<Element> subtreeCrossover = new SubtreeCrossover<>(maxTreeH);
      Crossover<List<Tree<Element>>> pairWiseSubtreeCrossover = (list1, list2, rnd) -> IntStream.range(0, list1.size())
          .mapToObj(i -> subtreeCrossover.recombine(list1.get(i), list2.get(i), rnd))
          .toList();
      // swap trees
      Crossover<List<Tree<Element>>> uniformCrossover = (list1, list2, rnd) -> IntStream.range(0, list1.size())
          .mapToObj(i -> rnd.nextDouble() < 0.5 ? list1.get(i) : list2.get(i))
          .toList();
      // subtree mutation
      SubtreeMutation<Element> subtreeMutation = new SubtreeMutation<>(maxTreeH, treeBuilder);
      Mutation<List<Tree<Element>>> allSubtreeMutations = (list, rnd) -> list.stream().map(t -> subtreeMutation.mutate(
          t,
          rnd
      )).toList();
      Map<GeneticOperator<List<Tree<Element>>>, Double> geneticOperators = Map.ofEntries(
          Map.entry(
              pairWiseSubtreeCrossover,
              crossoverP / 2d
          ),
          Map.entry(uniformCrossover, crossoverP / 2d),
          Map.entry(allSubtreeMutations, 1d - crossoverP)
      );
      if (!diversity) {
        return new StandardEvolver<>(
            mapper.mapperFor(exampleS),
            treeListFactory,
            nPop,
            StopConditions.nOfFitnessEvaluations(nEval),
            geneticOperators,
            new Tournament(Math.max(minNTournament, (int) Math.ceil((double) nPop * tournamentRate))),
            new Last(),
            nPop,
            true,
            remap,
            (p, r) -> new POSetPopulationState<>()
        );
      }
      return new StandardWithEnforcedDiversityEvolver<>(
          mapper.mapperFor(exampleS),
          treeListFactory,
          nPop,
          StopConditions.nOfFitnessEvaluations(nEval),
          geneticOperators,
          new Tournament(Math.max(minNTournament, (int) Math.ceil((double) nPop * tournamentRate))),
          new Last(),
          nPop,
          true,
          remap,
          (p, r) -> new POSetPopulationState<>(),
          nAttemptsDiversity
      );
    };
  }

  @SuppressWarnings("unused")
  public static <S, Q> Function<S, SpeciatedEvolver<QualityBasedProblem<S, Q>, Graph<Node,
      OperatorGraph.NonValuedArc>, S, Q>> oGraphea(
      @Param(value = "mapper") InvertibleMapper<Graph<Node, OperatorGraph.NonValuedArc>, S> mapper,
      @Param(value = "minConst", dD = 0d) double minConst,
      @Param(value = "maxConst", dD = 5d) double maxConst,
      @Param(value = "nConst", dI = 10) int nConst,
      @Param(value = "operators", dSs = {"addition", "subtraction", "multiplication", "prot_division", "prot_log"}) List<BaseOperator> operators,
      @Param(value = "nPop", dI = 100) int nPop,
      @Param(value = "nEval") int nEval,
      @Param(value = "arcAdditionRate", dD = 3d) double arcAdditionRate,
      @Param(value = "arcRemovalRate", dD = 0.1d) double arcRemovalRate,
      @Param(value = "nodeAdditionRate", dD = 1d) double nodeAdditionRate,
      @Param(value = "nPop", dI = 5) int minSpeciesSizeForElitism,
      @Param(value = "rankBase", dD = 0.75d) double rankBase,
      @Param(value = "remap") boolean remap
  ) {
    return exampleS -> {
      Map<GeneticOperator<Graph<Node, OperatorGraph.NonValuedArc>>, Double> geneticOperators =
          Map.ofEntries(
              Map.entry(new NodeAddition<Node, OperatorGraph.NonValuedArc>(
                  OperatorNode.sequentialIndexFactory(operators.toArray(BaseOperator[]::new)),
                  Mutation.copy(),
                  Mutation.copy()
              ).withChecker(OperatorGraph.checker()), nodeAdditionRate),
              Map.entry(new ArcAddition<Node, OperatorGraph.NonValuedArc>(
                  r -> OperatorGraph.NON_VALUED_ARC,
                  false
              ).withChecker(OperatorGraph.checker()), arcAdditionRate),
              Map.entry(new ArcRemoval<Node, OperatorGraph.NonValuedArc>(node -> (node instanceof Input) || (node instanceof Constant) || (node instanceof Output)).withChecker(
                  OperatorGraph.checker()), arcRemovalRate)
          );
      Graph<Node, OperatorGraph.NonValuedArc> graph = mapper.exampleFor(exampleS);
      double constStep = (maxConst - minConst) / nConst;
      List<Double> constants = DoubleStream.iterate(minConst, d -> d + constStep).limit(nConst).boxed().toList();
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
          nPop,
          StopConditions.nOfFitnessEvaluations(nEval),
          geneticOperators,
          remap,
          minSpeciesSizeForElitism,
          new LazySpeciator<>((new Jaccard()).on(i -> i.genotype().nodes()), 0.25),
          rankBase
      );
    };
  }

  @SuppressWarnings("unused")
  public static <S, Q> Function<S, OpenAIEvolutionaryStrategy<S, Q>> openAiEs(
      @Param(value = "mapper") InvertibleMapper<List<Double>, S> mapper,
      @Param(value = "initialMinV", dD = -1d) double initialMinV,
      @Param(value = "initialMaxV", dD = 1d) double initialMaxV,
      @Param(value = "sigma", dD = 0.35d) double sigma,
      @Param(value = "batchSize", dI = 15) int batchSize,
      @Param(value = "nEval") int nEval
  ) {
    return exampleS -> new OpenAIEvolutionaryStrategy<>(
        mapper.mapperFor(exampleS),
        new FixedLengthListFactory<>(
            mapper.exampleFor(exampleS).size(),
            new UniformDoubleFactory(initialMinV, initialMaxV)
        ),
        batchSize,
        StopConditions.nOfFitnessEvaluations(nEval),
        sigma
    );
  }

  @SuppressWarnings("unused")
  public static <S, Q> Function<S, SimpleEvolutionaryStrategy<S, Q>> simpleEs(
      @Param(value = "mapper") InvertibleMapper<List<Double>, S> mapper,
      @Param(value = "initialMinV", dD = -1d) double initialMinV,
      @Param(value = "initialMaxV", dD = 1d) double initialMaxV,
      @Param(value = "sigma", dD = 0.35d) double sigma,
      @Param(value = "parentsRate", dD = 0.33d) double parentsRate,
      @Param(value = "nOfElites", dI = 1) int nOfElites,
      @Param(value = "nPop", dI = 30) int nPop,
      @Param(value = "nEval") int nEval,
      @Param(value = "remap") boolean remap
  ) {
    return exampleS -> new SimpleEvolutionaryStrategy<>(
        mapper.mapperFor(exampleS),
        new FixedLengthListFactory<>(
            mapper.exampleFor(exampleS).size(),
            new UniformDoubleFactory(initialMinV, initialMaxV)
        ),
        nPop,
        StopConditions.nOfFitnessEvaluations(nEval),
        nOfElites,
        (int) Math.round(nPop * parentsRate),
        sigma,
        remap
    );
  }

  @SuppressWarnings("unused")
  public static <S, Q> Function<S, StandardEvolver<POSetPopulationState<Tree<Element>, S, Q>, QualityBasedProblem<S, Q>,
      Tree<Element>, S, Q>> srTreeGp(
      @Param(value = "mapper") InvertibleMapper<Tree<Element>, S> mapper,
      @Param(value = "constants", dDs = {0.1, 1, 10}) List<Double> constants,
      @Param(value = "operators", dSs = {
          "addition",
          "subtraction",
          "multiplication",
          "prot_division",
          "prot_log"
      }) List<Element.Operator> operators,
      @Param(value = "minTreeH", dI = 4) int minTreeH,
      @Param(value = "maxTreeH", dI = 10) int maxTreeH,
      @Param(value = "crossoverP", dD = 0.8d) double crossoverP,
      @Param(value = "tournamentRate", dD = 0.05d) double tournamentRate,
      @Param(value = "minNTournament", dI = 3) int minNTournament,
      @Param(value = "nPop", dI = 100) int nPop,
      @Param(value = "nEval") int nEval,
      @Param(value = "diversity", dB = true) boolean diversity,
      @Param(value = "nAttemptsDiversity", dI = 100) int nAttemptsDiversity,
      @Param(value = "remap") boolean remap
  ) {
    return exampleS -> {
      List<Element.Variable> variables = mapper.exampleFor(exampleS)
          .visitDepth()
          .stream()
          .filter(e -> e instanceof Element.Variable)
          .map(e -> ((Element.Variable) e).name())
          .distinct()
          .map(Element.Variable::new)
          .toList();
      List<Element.Constant> constantElements = constants.stream()
          .map(Element.Constant::new)
          .toList();
      IndependentFactory<Element> terminalFactory = IndependentFactory.oneOf(
          IndependentFactory.picker(variables),
          IndependentFactory.picker(constantElements)
      );
      IndependentFactory<Element> nonTerminalFactory = IndependentFactory.picker(operators);
      // single tree factory
      TreeBuilder<Element> treeBuilder = new GrowTreeBuilder<>(x -> 2, nonTerminalFactory, terminalFactory);
      Factory<Tree<Element>> treeFactory = new RampedHalfAndHalf<>(
          minTreeH,
          maxTreeH,
          x -> 2,
          nonTerminalFactory,
          terminalFactory
      );
      // operators
      Map<GeneticOperator<Tree<Element>>, Double> geneticOperators = Map.ofEntries(
          Map.entry(new SubtreeCrossover<>(maxTreeH), crossoverP),
          Map.entry(new SubtreeMutation<>(maxTreeH, treeBuilder), 1d - crossoverP)
      );
      if (!diversity) {
        return new StandardEvolver<>(
            mapper.mapperFor(exampleS),
            treeFactory,
            nPop,
            StopConditions.nOfFitnessEvaluations(nEval),
            geneticOperators,
            new Tournament(Math.max(minNTournament, (int) Math.ceil((double) nPop * tournamentRate))),
            new Last(),
            nPop,
            true,
            remap,
            (p, r) -> new POSetPopulationState<>()
        );
      }
      return new StandardWithEnforcedDiversityEvolver<>(
          mapper.mapperFor(exampleS),
          treeFactory,
          nPop,
          StopConditions.nOfFitnessEvaluations(nEval),
          geneticOperators,
          new Tournament(Math.max(minNTournament, (int) Math.ceil((double) nPop * tournamentRate))),
          new Last(),
          nPop,
          true,
          remap,
          (p, r) -> new POSetPopulationState<>(),
          nAttemptsDiversity
      );
    };
  }

  @SuppressWarnings("unused")
  public static <S, Q> Function<S, RandomWalk<QualityBasedProblem<S, Q>, Tree<Element>, S, Q>> srTreeRandomWalk(
      @Param(value = "mapper") InvertibleMapper<Tree<Element>, S> mapper,
      @Param(value = "constants", dDs = {0.1, 1, 10}) List<Double> constants,
      @Param(value = "operators", dSs = {
          "addition",
          "subtraction",
          "multiplication",
          "prot_division",
          "prot_log"
      }) List<Element.Operator> operators,
      @Param(value = "minTreeH", dI = 4) int minTreeH,
      @Param(value = "maxTreeH", dI = 10) int maxTreeH,
      @Param(value = "nEval") int nEval,
      @Param(value = "remap") boolean remap
  ) {
    return exampleS -> {
      List<Element.Variable> variables = mapper.exampleFor(exampleS)
          .visitDepth()
          .stream()
          .filter(e -> e instanceof Element.Variable)
          .map(e -> ((Element.Variable) e).name())
          .distinct()
          .map(Element.Variable::new)
          .toList();
      List<Element.Constant> constantElements = constants.stream()
          .map(Element.Constant::new)
          .toList();
      IndependentFactory<Element> terminalFactory = IndependentFactory.oneOf(
          IndependentFactory.picker(variables),
          IndependentFactory.picker(constantElements)
      );
      IndependentFactory<Element> nonTerminalFactory = IndependentFactory.picker(operators);
      // single tree factory
      TreeBuilder<Element> treeBuilder = new GrowTreeBuilder<>(x -> 2, nonTerminalFactory, terminalFactory);
      Factory<Tree<Element>> treeFactory = new TreeIndependentFactory<>(
          minTreeH,
          maxTreeH,
          x -> 2,
          nonTerminalFactory,
          terminalFactory,
          0.5d
      );
      return new RandomWalk<>(
          mapper.mapperFor(exampleS),
          treeFactory,
          StopConditions.nOfFitnessEvaluations(nEval),
          new SubtreeMutation<>(maxTreeH, treeBuilder)
      );
    };
  }

}
