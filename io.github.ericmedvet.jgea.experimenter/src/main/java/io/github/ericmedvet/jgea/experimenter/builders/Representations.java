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
import io.github.ericmedvet.jgea.core.IndependentFactory;
import io.github.ericmedvet.jgea.core.operator.Crossover;
import io.github.ericmedvet.jgea.core.operator.Mutation;
import io.github.ericmedvet.jgea.core.representation.grammar.string.StringGrammar;
import io.github.ericmedvet.jgea.core.representation.grammar.string.cfggp.GrammarBasedSubtreeMutation;
import io.github.ericmedvet.jgea.core.representation.grammar.string.cfggp.GrammarRampedHalfAndHalf;
import io.github.ericmedvet.jgea.core.representation.programsynthesis.ttpn.BackTracingNetworkFactory;
import io.github.ericmedvet.jgea.core.representation.programsynthesis.ttpn.Gate;
import io.github.ericmedvet.jgea.core.representation.programsynthesis.ttpn.GateInserterMutation;
import io.github.ericmedvet.jgea.core.representation.programsynthesis.ttpn.GateRemoverMutation;
import io.github.ericmedvet.jgea.core.representation.programsynthesis.ttpn.Network;
import io.github.ericmedvet.jgea.core.representation.programsynthesis.ttpn.NetworkCrossover;
import io.github.ericmedvet.jgea.core.representation.programsynthesis.ttpn.WireSwapperMutation;
import io.github.ericmedvet.jgea.core.representation.sequence.FixedLengthListFactory;
import io.github.ericmedvet.jgea.core.representation.sequence.UniformCrossover;
import io.github.ericmedvet.jgea.core.representation.sequence.bit.BitString;
import io.github.ericmedvet.jgea.core.representation.sequence.integer.IntString;
import io.github.ericmedvet.jgea.core.representation.tree.GrowTreeBuilder;
import io.github.ericmedvet.jgea.core.representation.tree.RampedHalfAndHalf;
import io.github.ericmedvet.jgea.core.representation.tree.SameRootSubtreeCrossover;
import io.github.ericmedvet.jgea.core.representation.tree.SubtreeCrossover;
import io.github.ericmedvet.jgea.core.representation.tree.SubtreeMutation;
import io.github.ericmedvet.jgea.core.representation.tree.numeric.ConstantsMutation;
import io.github.ericmedvet.jgea.core.representation.tree.numeric.Element;
import io.github.ericmedvet.jgea.core.representation.tree.numeric.Element.Constant;
import io.github.ericmedvet.jgea.core.representation.tree.numeric.Element.Operator;
import io.github.ericmedvet.jgea.experimenter.Representation;
import io.github.ericmedvet.jnb.core.Alias;
import io.github.ericmedvet.jnb.core.Cacheable;
import io.github.ericmedvet.jnb.core.Discoverable;
import io.github.ericmedvet.jnb.core.Param;
import io.github.ericmedvet.jnb.datastructure.DoubleRange;
import io.github.ericmedvet.jnb.datastructure.Pair;
import io.github.ericmedvet.jnb.datastructure.Tree;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.SequencedSet;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Discoverable(prefixTemplate = "ea.representation|r")
public class Representations {

  private Representations() {
  }

  @Cacheable
  public static Function<Tree<io.github.ericmedvet.jgea.core.representation.tree.bool.Element>, Representation<Tree<io.github.ericmedvet.jgea.core.representation.tree.bool.Element>>> bTree(
      @Param(
          value = "constants") List<Boolean> constantLabels,
      @Param(
          value = "operators", dSs = {"and", "or", "not"}) List<io.github.ericmedvet.jgea.core.representation.tree.bool.Element.Operator> operators,
      @Param(value = "minTreeH", dI = 4) int minTreeH,
      @Param(value = "maxTreeH", dI = 10) int maxTreeH
  ) {
    return exampleTree -> {
      List<io.github.ericmedvet.jgea.core.representation.tree.bool.Element.Variable> variables = exampleTree
          .leafLabels()
          .stream()
          .filter(
              e -> e instanceof io.github.ericmedvet.jgea.core.representation.tree.bool.Element.Variable
          )
          .map(e -> ((io.github.ericmedvet.jgea.core.representation.tree.bool.Element.Variable) e))
          .distinct()
          .toList();
      List<io.github.ericmedvet.jgea.core.representation.tree.bool.Element.Constant> constants = constantLabels.stream()
          .map(io.github.ericmedvet.jgea.core.representation.tree.bool.Element.Constant::new)
          .toList();
      List<? extends io.github.ericmedvet.jgea.core.representation.tree.bool.Element> terminals = Stream.concat(
          variables.stream(),
          constants.stream()
      ).toList();
      ToIntFunction<io.github.ericmedvet.jgea.core.representation.tree.bool.Element> arityFunction = e -> switch (e) {
        case io.github.ericmedvet.jgea.core.representation.tree.bool.Element.Operator o ->
          o.arity();
        default -> 0;
      };
      return new Representation<>(
          new RampedHalfAndHalf<>(
              minTreeH,
              maxTreeH,
              arityFunction,
              IndependentFactory.picker(operators),
              IndependentFactory.picker(terminals)
          ),
          List.of(
              new SubtreeMutation<>(
                  maxTreeH,
                  new GrowTreeBuilder<>(
                      arityFunction,
                      IndependentFactory.picker(operators),
                      IndependentFactory.picker(terminals)
                  )
              )
          ),
          List.of(new SubtreeCrossover<>(maxTreeH))
      );
    };
  }

  @Cacheable
  public static Function<BitString, Representation<BitString>> bitString(
      @Param(value = "factory", dNPM = "ea.r.f.bsUniform()") Function<BitString, Factory<BitString>> factory,
      @Param(value = "mutations", dNPMs = {"ea.r.go.bsFlipMutation()"
      }) List<Function<BitString, Mutation<BitString>>> mutations,
      @Param(value = "xovers", dNPMs = {"ea.r.go.composedXover(xover = ea.r.go.bsUniformXover(); mutation = ea.r.go" + ".bsFlipMutation())"
      }) List<Function<BitString, Crossover<BitString>>> xovers
  ) {
    return eBs -> new Representation<>(
        factory.apply(eBs),
        mutations.stream().map(m -> m.apply(eBs)).toList(),
        xovers.stream().map(m -> m.apply(eBs)).toList()
    );
  }

  @Cacheable
  public static <N> Function<Tree<N>, Representation<Tree<N>>> cfgTree(
      @Param("grammar") StringGrammar<N> grammar,
      @Param(value = "minTreeH", dI = 4) int minTreeH,
      @Param(value = "maxTreeH", dI = 16) int maxTreeH
  ) {
    return _ -> new Representation<>(
        new GrammarRampedHalfAndHalf<>(minTreeH, maxTreeH, grammar),
        List.of(new GrammarBasedSubtreeMutation<>(maxTreeH, grammar)),
        List.of(new SameRootSubtreeCrossover<>(maxTreeH))
    );
  }

  @Cacheable
  public static Function<List<Double>, Representation<List<Double>>> doubleString(
      @Param(value = "factory", dNPM = "ea.r.f.dsUniform()") Function<List<Double>, Factory<List<Double>>> factory,
      @Param(value = "mutations", dNPMs = {"ea.r.go.dsGaussianMutation()"
      }) List<Function<List<Double>, Mutation<List<Double>>>> mutations,
      @Param(value = "xovers", dNPMs = {"ea.r.go.composedXover(xover = ea.r.go.dsSegmentGeometricXover(); mutation = " + "ea.r.go.dsGaussianMutation())"
      }) List<Function<List<Double>, Crossover<List<Double>>>> xovers
  ) {
    return eDs -> new Representation<>(
        factory.apply(eDs),
        mutations.stream().map(m -> m.apply(eDs)).toList(),
        xovers.stream().map(m -> m.apply(eDs)).toList()
    );
  }

  @Cacheable
  public static Function<IntString, Representation<IntString>> intString(
      @Param(value = "factory", dNPM = "ea.r.f.isUniform()") Function<IntString, Factory<IntString>> factory,
      @Param(value = "mutations", dNPMs = {"ea.r.go.isFlipMutation()"
      }) List<Function<IntString, Mutation<IntString>>> mutations,
      @Param(value = "xovers", dNPMs = {"ea.r.go.composedXover(xover = ea.r.go.isUniformXover(); mutation = ea.r.go" + ".isFlipMutation())"
      }) List<Function<IntString, Crossover<IntString>>> xovers
  ) {
    return eIs -> new Representation<>(
        factory.apply(eIs),
        mutations.stream().map(m -> m.apply(eIs)).toList(),
        xovers.stream().map(m -> m.apply(eIs)).toList()
    );
  }

  @Alias(name = "multiBTree", value = "list(of = ea.r.bTree())")
  @Alias(name = "multiSRTree", value = "list(of = ea.r.srTree())")
  @Cacheable
  public static <G> Function<List<G>, Representation<List<G>>> list(
      @Param("of") Function<G, Representation<G>> representation,
      @Param("altMutations") List<Function<List<G>, Mutation<List<G>>>> altMutations,
      @Param("altXovers") List<Function<List<G>, Crossover<List<G>>>> altCrossovers,
      @Param(value = "uniformCrossover", dB = true) boolean uniformCrossover
  ) {
    return egs -> {
      Representation<G> innerR = representation.apply(egs.getFirst());
      List<Mutation<List<G>>> mutations;
      if (altMutations.isEmpty()) {
        mutations = innerR.mutations().stream().map(Mutation::list).toList();
      } else {
        mutations = altMutations.stream().map(m -> m.apply(egs)).toList();
      }
      List<Crossover<List<G>>> crossovers;
      if (altCrossovers.isEmpty()) {
        crossovers = innerR.crossovers().stream().map(Crossover::list).toList();
        if (uniformCrossover) {
          crossovers = Stream.concat(crossovers.stream(), Stream.of(new UniformCrossover<G>()))
              .toList();
        }
      } else {
        crossovers = altCrossovers.stream().map(c -> c.apply(egs)).toList();
      }
      return new Representation<>(
          new FixedLengthListFactory<>(egs.size(), innerR.factory().independent()),
          mutations,
          crossovers
      );
    };
  }

  @Cacheable
  public static <G1, G2> Function<Pair<G1, G2>, Representation<Pair<G1, G2>>> pair(
      @Param("first") Function<G1, Representation<G1>> r1,
      @Param("second") Function<G2, Representation<G2>> r2
  ) {
    return p -> Representation.pair(r1.apply(p.first()), r2.apply(p.second()));
  }

  @Cacheable
  public static <G> Function<G, Representation<G>> seeded(
      @Param("of") Function<G, Representation<G>> representation,
      @Param("seeds") Collection<G> seeds,
      @Param(value = "seededRate", dD = 0.01) double seededRate,
      @Param(value = "mutatedRate", dD = 0.49) double mutatedRate
  ) {
    return g -> Representation.seeded(seeds, representation.apply(g), seededRate, mutatedRate);
  }

  @Cacheable
  public static Function<Tree<Element>, Representation<Tree<Element>>> srTree(
      @Param(
          value = "constants", dDs = {0.1, 1, 10}) List<Double> constants,
      @Param(
          value = "operators", dSs = {"addition", "subtraction", "multiplication", "prot_division", "prot_log"}) List<Element.Operator> operators,
      @Param(value = "minTreeH", dI = 4) int minTreeH,
      @Param(value = "maxTreeH", dI = 10) int maxTreeH,
      @Param("ephemeral") boolean ephemeral,
      @Param(value = "ephemeralMinV", dD = -5d) double ephemeralMinV,
      @Param(value = "ephemeralMinV", dD = 5d) double ephemeralMaxV,
      @Param(value = "ephemeralSigmaMut", dD = 0.25) double ephemeralSigmaMut,
      @Param(value = "includeVarNameRegex", dS = ".*") String includeVarNameRegex,
      @Param(value = "excludeVarNameRegex", dS = "") String excludeVarNameRegex
  ) {
    return exampleTree -> {
      List<Element.Variable> variables = exampleTree.leafLabels()
          .stream()
          .filter(e -> e instanceof Element.Variable)
          .map(e -> ((Element.Variable) e).name())
          .distinct()
          .filter(n -> n.matches(includeVarNameRegex))
          .filter(n -> !n.matches(excludeVarNameRegex))
          .map(Element.Variable::new)
          .toList();
      List<Element.Constant> constantElements = constants.stream()
          .map(Element.Constant::new)
          .toList();
      DoubleRange ephemeralRange = new DoubleRange(ephemeralMinV, ephemeralMaxV);
      IndependentFactory<Element> ephemeralFactory = random -> new Constant(
          ephemeralRange.denormalize(random.nextDouble())
      );
      IndependentFactory<Element> terminalFactory = IndependentFactory.oneOf(
          IndependentFactory.picker(variables),
          ephemeral ? ephemeralFactory : IndependentFactory.picker(constantElements)
      );
      IndependentFactory<Element> nonTerminalFactory = IndependentFactory.picker(operators);
      // single tree factory
      ToIntFunction<Element> arityFunction = e -> {
        if (e instanceof Operator o) {
          return o.arity();
        }
        return 0;
      };
      Function<Integer, IndependentFactory<Tree<Element>>> treeBuilder = new GrowTreeBuilder<>(
          arityFunction,
          nonTerminalFactory,
          terminalFactory
      );
      return new Representation<>(
          new RampedHalfAndHalf<>(
              minTreeH,
              maxTreeH,
              arityFunction,
              nonTerminalFactory,
              terminalFactory
          ),
          ephemeral ? List.of(
              new SubtreeMutation<>(maxTreeH, treeBuilder),
              new ConstantsMutation(ephemeralSigmaMut)
          ) : List.of(new SubtreeMutation<>(maxTreeH, treeBuilder)),
          List.of(new SubtreeCrossover<>(maxTreeH))
      );
    };
  }

  @Cacheable
  public static Function<Network, Representation<Network>> ttpn(
      @Param(value = "maxNOfGates", dI = 32) int maxNOfGates,
      @Param(value = "maxNOfAttempts", dI = 10) int maxNOfAttempts,
      @Param(value = "subnetSizeRate", dD = 0.33) double subnetSizeRate,
      @Param(value = "gates", dNPMs = {"ea.ttpn.gate.bAnd()", "ea.ttpn.gate.bNot()", "ea.ttpn.gate.bOr()", "ea.ttpn" + ".gate.bXor()", "ea.ttpn.gate.concat()", "ea.ttpn.gate.equal()", "ea.ttpn.gate.iTh()", "ea.ttpn" + ".gate.length" + "()", "ea.ttpn.gate.noop()", "ea.ttpn.gate.pairer()", "ea.ttpn.gate.queuer()", "ea.ttpn" + ".gate.select()", "ea" + ".ttpn.gate.sequencer()", "ea.ttpn.gate.sink()", "ea.ttpn.gate" + ".splitter()", "ea" + ".ttpn.gate.unpairer()", "ea" + ".ttpn.gate.iBefore()", "ea.ttpn.gate.iPMathOperator" + "(operator = addition)", "ea.ttpn.gate.iPMathOperator" + "(operator = " + "subtraction)", "ea.ttpn.gate" + ".iPMathOperator(operator = " + "multiplication)", "ea.ttpn.gate" + ".iPMathOperator(operator = division)", "ea.ttpn.gate.iRange()", "ea" + ".ttpn.gate.iSMult()", "ea.ttpn" + ".gate" + ".iSPMult()", "ea.ttpn.gate.iSPSum" + "()", "ea.ttpn.gate.iSSum()", "ea" + ".ttpn.gate.iToR()", "ea.ttpn.gate.rBefore" + "()", "ea.ttpn.gate" + ".repeater()", "ea.ttpn.gate.rPMathOperator" + "(operator " + "=" + " addition)", "ea.ttpn.gate" + ".rPMathOperator" + "(operator = subtraction)", "ea.ttpn.gate" + ".rPMathOperator(operator = multiplication)", "ea" + ".ttpn" + ".gate.rPMathOperator(operator = division)", "ea" + ".ttpn.gate.rSMult()", "ea.ttpn.gate.rSPMult()", "ea" + ".ttpn.gate.rSPSum()", "ea.ttpn.gate.rSSum()", "ea" + ".ttpn.gate.rToI()", "ea.ttpn.gate.sBefore()", "ea.ttpn" + ".gate.sConcat()", "ea.ttpn.gate" + ".sSplitter()", "ea.ttpn.gate.bConst(value = true)", "ea.ttpn" + ".gate.iConst" + "(value = 0)", "ea.ttpn" + ".gate.iConst(value = 1)", "ea.ttpn.gate.iConst(value = 2)", "ea" + ".ttpn.gate.iConst" + "(value = 5)", "ea.ttpn.gate.rConst(value = 0)", "ea.ttpn.gate.rConst(value = 0.1)", "ea.ttpn.gate.rConst" + "(value = 0" + ".2)", "ea.ttpn.gate.rConst(value =" + " 0.5)", "ea.ttpn.gate" + ".sPSequencer()"}) List<Gate> gates,
      @Param("forbiddenGates") List<Gate> forbiddenGates,
      @Param(value = "avoidDeadGates", dB = true) boolean avoidDeadGates
  ) {
    SequencedSet<Gate> actualGates = gates.stream()
        .filter(g -> !forbiddenGates.contains(g))
        .collect(Collectors.toCollection(LinkedHashSet::new));
    return ttpn -> new Representation<>(
        new BackTracingNetworkFactory(
            ttpn.inputGates().values().stream().toList(),
            ttpn.outputGates().values().stream().toList(),
            actualGates,
            maxNOfGates,
            100 * maxNOfAttempts
        ),
        List.of(
            new GateInserterMutation(actualGates, maxNOfGates, maxNOfAttempts, avoidDeadGates),
            new GateRemoverMutation(maxNOfAttempts, avoidDeadGates),
            new WireSwapperMutation(maxNOfAttempts, avoidDeadGates)
        ),
        List.of(new NetworkCrossover(maxNOfGates, subnetSizeRate, maxNOfAttempts, avoidDeadGates))
    );
  }

}