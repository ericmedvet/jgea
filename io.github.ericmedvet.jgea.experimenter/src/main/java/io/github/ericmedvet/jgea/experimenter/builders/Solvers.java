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
import io.github.ericmedvet.jgea.core.QualityBasedProblem;
import io.github.ericmedvet.jgea.core.operator.Crossover;
import io.github.ericmedvet.jgea.core.operator.GeneticOperator;
import io.github.ericmedvet.jgea.core.operator.Mutation;
import io.github.ericmedvet.jgea.core.representation.sequence.FixedLengthListFactory;
import io.github.ericmedvet.jgea.core.representation.sequence.UniformCrossover;
import io.github.ericmedvet.jgea.core.representation.sequence.numeric.GaussianMutation;
import io.github.ericmedvet.jgea.core.representation.sequence.numeric.UniformDoubleFactory;
import io.github.ericmedvet.jgea.core.representation.tree.*;
import io.github.ericmedvet.jgea.core.representation.tree.numeric.Element;
import io.github.ericmedvet.jgea.core.selector.Last;
import io.github.ericmedvet.jgea.core.selector.Tournament;
import io.github.ericmedvet.jgea.core.solver.*;
import io.github.ericmedvet.jgea.core.solver.state.POSetPopulationState;
import io.github.ericmedvet.jgea.experimenter.InvertibleMapper;
import io.github.ericmedvet.jnb.core.Param;

import java.util.List;
import java.util.Map;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

/**
 * @author "Eric Medvet" on 2022/11/21 for 2d-robot-evolution
 */
public class Solvers {

  private Solvers() {
  }

  @SuppressWarnings("unused")
  public static <S, Q> StandardEvolver<POSetPopulationState<List<Tree<Element>>, S, Q>, QualityBasedProblem<S, Q>,
      List<Tree<Element>>, S, Q> multiSRTreeGP(
      @Param(value = "mapper") InvertibleMapper<List<Tree<Element>>, S> mapper,
      @Param(value = "minConst", dD = 0d) double minConst,
      @Param(value = "maxConst", dD = 5d) double maxConst,
      @Param(value = "nConst", dI = 10) int nConst,
      @Param(value = "operators", dSs = {
          "addition",
          "subtraction",
          "multiplication",
          "prot_division"
      }) List<Element.Operator> operators,
      @Param(value = "minTreeH", dI = 3) int minTreeH,
      @Param(value = "maxTreeH", dI = 8) int maxTreeH,
      @Param(value = "crossoverP", dD = 0.8d) double crossoverP,
      @Param(value = "tournamentRate", dD = 0.05d) double tournamentRate,
      @Param(value = "minNTournament", dI = 3) int minNTournament,
      @Param(value = "nPop", dI = 100) int nPop,
      @Param(value = "nEval") int nEval,
      @Param(value = "diversity", dB = true) boolean diversity,
      @Param(value = "nAttemptsDiversity", dI = 100) int nAttemptsDiversity,
      @Param(value = "remap") boolean remap
  ) {
    List<Element.Variable> variables = mapper.exampleInput().stream()
        .map(t -> t.visitDepth().stream()
            .filter(e -> e instanceof Element.Variable)
            .map(e -> ((Element.Variable) e).name())
            .toList()
        )
        .flatMap(List::stream)
        .distinct()
        .map(Element.Variable::new)
        .toList();
    double constStep = (maxConst - minConst) / nConst;
    List<Element.Constant> constants = DoubleStream.iterate(minConst, d -> d + constStep)
        .limit(nConst)
        .mapToObj(Element.Constant::new)
        .toList();
    IndependentFactory<Element> terminalFactory = IndependentFactory.oneOf(
        IndependentFactory.picker(variables),
        IndependentFactory.picker(constants)
    );
    IndependentFactory<Element> nonTerminalFactory = IndependentFactory.picker(operators);
    IndependentFactory<List<Tree<Element>>> treeListFactory = new FixedLengthListFactory<>(
        mapper.exampleInput().size(),
        new RampedHalfAndHalf<>(minTreeH, maxTreeH, x -> 2, nonTerminalFactory, terminalFactory).independent()
    );
    // single tree factory
    TreeBuilder<Element> treeBuilder = new GrowTreeBuilder<>(x -> 2, nonTerminalFactory, terminalFactory);
    // subtree between same position trees
    SubtreeCrossover<Element> subtreeCrossover = new SubtreeCrossover<>(maxTreeH);
    Crossover<List<Tree<Element>>> pairWiseSubtreeCrossover = (list1, list2, rnd) ->
        IntStream.range(0, list1.size())
            .mapToObj(i -> subtreeCrossover.recombine(list1.get(i), list2.get(i), rnd))
            .toList();
    // swap trees
    Crossover<List<Tree<Element>>> uniformCrossover = (list1, list2, rnd) -> IntStream.range(0, list1.size())
        .mapToObj(i -> rnd.nextDouble() < 0.5 ? list1.get(i) : list2.get(i)).toList();
    // subtree mutation
    SubtreeMutation<Element> subtreeMutation = new SubtreeMutation<>(maxTreeH, treeBuilder);
    Mutation<List<Tree<Element>>> allSubtreeMutations = (list, rnd) -> list.stream()
        .map(t -> subtreeMutation.mutate(t, rnd))
        .toList();
    Map<GeneticOperator<List<Tree<Element>>>, Double> geneticOperators = Map.ofEntries(
        Map.entry(pairWiseSubtreeCrossover, crossoverP / 2d),
        Map.entry(uniformCrossover, crossoverP / 2d),
        Map.entry(allSubtreeMutations, 1d - crossoverP)
    );
    if (!diversity) {
      return new StandardEvolver<>(
          mapper,
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
        mapper,
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
  }

  @SuppressWarnings("unused")
  public static <S, Q> StandardEvolver<POSetPopulationState<List<Double>, S, Q>, QualityBasedProblem<S, Q>,
      List<Double>, S, Q> numGA(
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
    IndependentFactory<List<Double>> doublesFactory = new FixedLengthListFactory<>(
        mapper.exampleInput().size(),
        new UniformDoubleFactory(initialMinV, initialMaxV)
    );
    Map<GeneticOperator<List<Double>>, Double> geneticOperators = Map.of(
        new GaussianMutation(sigmaMut),
        1d - crossoverP,
        new UniformCrossover<>(doublesFactory).andThen(new GaussianMutation(sigmaMut)),
        crossoverP
    );
    if (!diversity) {
      return new StandardEvolver<>(
          mapper,
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
          mapper,
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
  }

  @SuppressWarnings("unused")
  public static <S, Q> OpenAIEvolutionaryStrategy<S, Q> openAIES(
      @Param(value = "mapper") InvertibleMapper<List<Double>, S> mapper,
      @Param(value = "initialMinV", dD = -1d) double initialMinV,
      @Param(value = "initialMaxV", dD = 1d) double initialMaxV,
      @Param(value = "sigma", dD = 0.35d) double sigma,
      @Param(value = "batchSize", dI = 15) int batchSize,
      @Param(value = "nEval") int nEval
  ) {
    return new OpenAIEvolutionaryStrategy<>(
        mapper,
        new FixedLengthListFactory<>(mapper.exampleInput().size(), new UniformDoubleFactory(initialMinV, initialMaxV)),
        batchSize,
        StopConditions.nOfFitnessEvaluations(nEval),
        sigma
    );
  }

  @SuppressWarnings("unused")
  public static <S, Q> SimpleEvolutionaryStrategy<S, Q> simpleES(
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
    return new SimpleEvolutionaryStrategy<>(
        mapper,
        new FixedLengthListFactory<>(mapper.exampleInput().size(), new UniformDoubleFactory(initialMinV, initialMaxV)),
        nPop,
        StopConditions.nOfFitnessEvaluations(nEval),
        nOfElites,
        (int) Math.round(nPop * parentsRate),
        sigma,
        remap
    );
  }

  public static <S, Q> StandardEvolver<POSetPopulationState<Tree<Element>, S, Q>, QualityBasedProblem<S, Q>,
      Tree<Element>, S, Q> srTreeGP(
      @Param(value = "mapper") InvertibleMapper<Tree<Element>, S> mapper,
      @Param(value = "minConst", dD = 0d) double minConst,
      @Param(value = "maxConst", dD = 5d) double maxConst,
      @Param(value = "nConst", dI = 10) int nConst,
      @Param(value = "operators", dSs = {
          "addition",
          "subtraction",
          "multiplication",
          "prot_division"
      }) List<Element.Operator> operators,
      @Param(value = "minTreeH", dI = 3) int minTreeH,
      @Param(value = "maxTreeH", dI = 8) int maxTreeH,
      @Param(value = "crossoverP", dD = 0.8d) double crossoverP,
      @Param(value = "tournamentRate", dD = 0.05d) double tournamentRate,
      @Param(value = "minNTournament", dI = 3) int minNTournament,
      @Param(value = "nPop", dI = 100) int nPop,
      @Param(value = "nEval") int nEval,
      @Param(value = "diversity", dB = true) boolean diversity,
      @Param(value = "nAttemptsDiversity", dI = 100) int nAttemptsDiversity,
      @Param(value = "remap") boolean remap
  ) {
    List<Element.Variable> variables = mapper.exampleInput().visitDepth().stream()
        .filter(e -> e instanceof Element.Variable)
        .map(e -> ((Element.Variable) e).name())
        .distinct()
        .map(Element.Variable::new)
        .toList();
    double constStep = (maxConst - minConst) / nConst;
    List<Element.Constant> constants = DoubleStream.iterate(minConst, d -> d + constStep)
        .limit(nConst)
        .mapToObj(Element.Constant::new)
        .toList();
    IndependentFactory<Element> terminalFactory = IndependentFactory.oneOf(
        IndependentFactory.picker(variables),
        IndependentFactory.picker(constants)
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
          mapper,
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
        mapper,
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
  }

}
