/*-
 * ========================LICENSE_START=================================
 * jgea-sample
 * %%
 * Copyright (C) 2018 - 2023 Eric Medvet
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

package io.github.ericmedvet.jgea.sample.lab;

import static io.github.ericmedvet.jgea.core.listener.NamedFunctions.*;
import static io.github.ericmedvet.jgea.sample.Args.i;
import static io.github.ericmedvet.jgea.sample.Args.ri;

import com.google.common.base.Stopwatch;
import io.github.ericmedvet.jgea.core.IndependentFactory;
import io.github.ericmedvet.jgea.core.distance.Jaccard;
import io.github.ericmedvet.jgea.core.listener.CSVPrinter;
import io.github.ericmedvet.jgea.core.listener.ListenerFactory;
import io.github.ericmedvet.jgea.core.listener.NamedFunction;
import io.github.ericmedvet.jgea.core.listener.TabularPrinter;
import io.github.ericmedvet.jgea.core.operator.Crossover;
import io.github.ericmedvet.jgea.core.operator.Mutation;
import io.github.ericmedvet.jgea.core.representation.NamedUnivariateRealFunction;
import io.github.ericmedvet.jgea.core.representation.grammar.string.SymbolicRegressionGrammar;
import io.github.ericmedvet.jgea.core.representation.grammar.string.cfggp.GrammarBasedSubtreeMutation;
import io.github.ericmedvet.jgea.core.representation.grammar.string.cfggp.GrammarRampedHalfAndHalf;
import io.github.ericmedvet.jgea.core.representation.graph.*;
import io.github.ericmedvet.jgea.core.representation.graph.numeric.Constant;
import io.github.ericmedvet.jgea.core.representation.graph.numeric.Input;
import io.github.ericmedvet.jgea.core.representation.graph.numeric.Output;
import io.github.ericmedvet.jgea.core.representation.graph.numeric.functiongraph.BaseFunction;
import io.github.ericmedvet.jgea.core.representation.graph.numeric.functiongraph.FunctionGraph;
import io.github.ericmedvet.jgea.core.representation.graph.numeric.functiongraph.FunctionNode;
import io.github.ericmedvet.jgea.core.representation.graph.numeric.functiongraph.ShallowSparseFactory;
import io.github.ericmedvet.jgea.core.representation.graph.numeric.operatorgraph.BaseOperator;
import io.github.ericmedvet.jgea.core.representation.graph.numeric.operatorgraph.OperatorGraph;
import io.github.ericmedvet.jgea.core.representation.graph.numeric.operatorgraph.OperatorNode;
import io.github.ericmedvet.jgea.core.representation.graph.numeric.operatorgraph.ShallowFactory;
import io.github.ericmedvet.jgea.core.representation.tree.*;
import io.github.ericmedvet.jgea.core.representation.tree.numeric.Element;
import io.github.ericmedvet.jgea.core.representation.tree.numeric.TreeBasedUnivariateRealFunction;
import io.github.ericmedvet.jgea.core.selector.Last;
import io.github.ericmedvet.jgea.core.selector.Tournament;
import io.github.ericmedvet.jgea.core.solver.*;
import io.github.ericmedvet.jgea.core.solver.speciation.KMeansSpeciator;
import io.github.ericmedvet.jgea.core.solver.speciation.LazySpeciator;
import io.github.ericmedvet.jgea.core.solver.speciation.SpeciatedEvolver;
import io.github.ericmedvet.jgea.core.solver.state.POCPopulationState;
import io.github.ericmedvet.jgea.core.util.Misc;
import io.github.ericmedvet.jgea.problem.regression.FormulaMapper;
import io.github.ericmedvet.jgea.problem.regression.MathUtils;
import io.github.ericmedvet.jgea.problem.regression.univariate.UnivariateRegressionFitness;
import io.github.ericmedvet.jgea.problem.regression.univariate.synthetic.Keijzer6;
import io.github.ericmedvet.jgea.problem.regression.univariate.synthetic.Nguyen7;
import io.github.ericmedvet.jgea.problem.regression.univariate.synthetic.Polynomial4;
import io.github.ericmedvet.jgea.problem.regression.univariate.synthetic.SyntheticUnivariateRegressionProblem;
import io.github.ericmedvet.jgea.sample.Worker;
import java.io.File;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.random.RandomGenerator;
import java.util.stream.Collectors;

// /usr/lib/jvm/jdk-14.0.1/bin/java -cp ~/IdeaProjects/jgea/out/artifacts/jgea_jar/jgea.jar
// it.units.malelab.jgea
// .sample.lab
// .SymbolicRegressionComparison seed=0:10 file=results-%s.txt
public class SymbolicRegressionComparison extends Worker {

  public SymbolicRegressionComparison(String[] args) {
    super(args);
  }

  public static void main(String[] args) {
    new SymbolicRegressionComparison(args);
  }

  @Override
  public void run() {
    int nPop = i(a("nPop", "100"));
    int maxHeight = i(a("maxHeight", "10"));
    int maxNodes = i(a("maxNodes", "20"));
    int nTournament = 5;
    int diversityMaxAttempts = 100;
    int nIterations = i(a("nIterations", "100"));
    String evolverNamePattern = a("evolver", "tree-ga");
    int[] seeds = ri(a("seed", "0:1"));
    double graphArcAdditionRate = 3d;
    double graphArcMutationRate = 1d;
    double graphArcRemovalRate = 0d;
    double graphNodeAdditionRate = 1d;
    double graphCrossoverRate = 1d;
    UnivariateRegressionFitness.Metric metric = UnivariateRegressionFitness.Metric.MSE;
    Element.Operator[] operators = new Element.Operator[] {
      Element.Operator.ADDITION,
      Element.Operator.SUBTRACTION,
      Element.Operator.MULTIPLICATION,
      Element.Operator.PROT_DIVISION,
      Element.Operator.PROT_LOG
    };
    BaseOperator[] baseOperators = new BaseOperator[] {
      BaseOperator.ADDITION,
      BaseOperator.SUBTRACTION,
      BaseOperator.MULTIPLICATION,
      BaseOperator.PROT_DIVISION,
      BaseOperator.PROT_LOG
    };
    BaseFunction[] baseFunctions = new BaseFunction[] {
      BaseFunction.RE_LU, BaseFunction.GAUSSIAN, BaseFunction.PROT_INVERSE, BaseFunction.SQ
    };
    List<Double> constants = List.of(0.1d, 1d, 10d);
    List<SyntheticUnivariateRegressionProblem> problems = List.of(
        new Nguyen7(metric, 1), new Keijzer6(metric), new Polynomial4(metric)
        // ,
        // new Pagie1(metric)
        );
    // consumers
    List<NamedFunction<? super POCPopulationState<?, ?, ? extends Double>, ?>> functions = List.of(
        nOfIterations(),
        births(),
        elapsedSeconds(),
        bar(8).of(progress()),
        size().of(all()),
        size().of(firsts()),
        size().of(lasts()),
        uniqueness().of(each(genotype())).of(all()),
        uniqueness().of(each(solution())).of(all()),
        uniqueness().of(each(quality())).of(all()),
        size().of(genotype()).of(best()),
        size().of(solution()).of(best()),
        fitnessMappingIteration().of(best()),
        quality().reformat("%5.3f").of(best()),
        hist(8).of(each(quality())).of(all()),
        solution().reformat("%30.30s").of(best()));
    List<NamedFunction<? super Map<String, Object>, ?>> kFunctions = List.of(
        attribute("seed").reformat("%2d"),
        attribute("problem")
            .reformat(NamedFunction.formatOfLongest(problems.stream()
                .map(p -> p.getClass().getSimpleName())
                .toList())),
        attribute("evolver").reformat("%20.20s"));
    ListenerFactory<POCPopulationState<?, ?, ? extends Double>, Map<String, Object>> listenerFactory =
        new TabularPrinter<>(functions, kFunctions);
    if (a("file", null) != null) {
      listenerFactory = ListenerFactory.all(
          List.of(listenerFactory, new CSVPrinter<>(functions, kFunctions, new File(a("file", null)), true)));
    }
    // evolvers
    Map<
            String,
            Function<
                SyntheticUnivariateRegressionProblem,
                IterativeSolver<
                    ? extends POCPopulationState<?, NamedUnivariateRealFunction, Double>,
                    SyntheticUnivariateRegressionProblem,
                    NamedUnivariateRealFunction>>>
        solvers = new TreeMap<>();
    solvers.put("tree-ga", p -> {
      IndependentFactory<Element> terminalFactory = IndependentFactory.oneOf(
          IndependentFactory.picker(p.qualityFunction().getDataset().xVarNames().stream()
              .map(Element.Variable::new)
              .toList()),
          IndependentFactory.picker(
              constants.stream().map(Element.Constant::new).toList()));
      return new AbstractStandardEvolver<>(
          TreeBasedUnivariateRealFunction.mapper(
                  p.qualityFunction().getDataset().xVarNames(),
                  p.qualityFunction().getDataset().yVarNames().get(0))
              .andThen(MathUtils.linearScaler(p.qualityFunction())),
          new RampedHalfAndHalf<>(
              4,
              maxHeight,
              Element.Operator.arityFunction(),
              IndependentFactory.picker(operators),
              terminalFactory),
          nPop,
          StopConditions.nOfIterations(nIterations),
          Map.of(
              new SubtreeCrossover<>(maxHeight),
              0.8d,
              new SubtreeMutation<>(
                  maxHeight,
                  new GrowTreeBuilder<>(
                      Element.Operator.arityFunction(),
                      IndependentFactory.picker(operators),
                      terminalFactory)),
              0.2d),
          new Tournament(nTournament),
          new Last(),
          nPop,
          true,
          false,
          (srp, r) -> new POCPopulationState<>());
    });
    solvers.put("tree-ga-noxover", p -> {
      IndependentFactory<Element> terminalFactory = IndependentFactory.oneOf(
          IndependentFactory.picker(p.qualityFunction().getDataset().xVarNames().stream()
              .map(Element.Variable::new)
              .toList()),
          IndependentFactory.picker(
              constants.stream().map(Element.Constant::new).toList()));
      return new AbstractStandardEvolver<>(
          TreeBasedUnivariateRealFunction.mapper(
                  p.qualityFunction().getDataset().xVarNames(),
                  p.qualityFunction().getDataset().yVarNames().get(0))
              .andThen(MathUtils.linearScaler(p.qualityFunction())),
          new RampedHalfAndHalf<>(
              4,
              maxHeight,
              Element.Operator.arityFunction(),
              IndependentFactory.picker(operators),
              terminalFactory),
          nPop,
          StopConditions.nOfIterations(nIterations),
          Map.of(
              new SubtreeMutation<>(
                  maxHeight,
                  new GrowTreeBuilder<>(
                      Element.Operator.arityFunction(),
                      IndependentFactory.picker(operators),
                      terminalFactory)),
              0.2d),
          new Tournament(nTournament),
          new Last(),
          nPop,
          true,
          false,
          (srp, r) -> new POCPopulationState<>());
    });
    solvers.put("tree-gadiv", p -> {
      IndependentFactory<Element> terminalFactory = IndependentFactory.oneOf(
          IndependentFactory.picker(p.qualityFunction().getDataset().xVarNames().stream()
              .map(Element.Variable::new)
              .toList()),
          IndependentFactory.picker(
              constants.stream().map(Element.Constant::new).toList()));
      return new StandardWithEnforcedDiversityEvolver<>(
          TreeBasedUnivariateRealFunction.mapper(
                  p.qualityFunction().getDataset().xVarNames(),
                  p.qualityFunction().getDataset().yVarNames().get(0))
              .andThen(MathUtils.linearScaler(p.qualityFunction())),
          new RampedHalfAndHalf<>(
              4,
              maxHeight,
              Element.Operator.arityFunction(),
              IndependentFactory.picker(operators),
              terminalFactory),
          nPop,
          StopConditions.nOfIterations(nIterations),
          Map.of(
              new SubtreeCrossover<>(maxHeight),
              0.8d,
              new SubtreeMutation<>(
                  maxHeight,
                  new GrowTreeBuilder<>(
                      Element.Operator.arityFunction(),
                      IndependentFactory.picker(operators),
                      terminalFactory)),
              0.2d),
          new Tournament(nTournament),
          new Last(),
          nPop,
          true,
          false,
          (srp, r) -> new POCPopulationState<>(),
          diversityMaxAttempts);
    });
    solvers.put("cfgtree-ga", p -> {
      SymbolicRegressionGrammar g = new SymbolicRegressionGrammar(
          List.of(operators), p.qualityFunction().getDataset().xVarNames(), constants);
      return new AbstractStandardEvolver<>(
          new FormulaMapper()
              .andThen(TreeBasedUnivariateRealFunction.mapper(
                  p.qualityFunction().getDataset().xVarNames(),
                  p.qualityFunction().getDataset().yVarNames().get(0)))
              .andThen(MathUtils.linearScaler(p.qualityFunction())),
          new GrammarRampedHalfAndHalf<>(6, maxHeight + 4, g),
          nPop,
          StopConditions.nOfIterations(nIterations),
          Map.of(
              new SameRootSubtreeCrossover<>(maxHeight + 4),
              0.8d,
              new GrammarBasedSubtreeMutation<>(maxHeight + 4, g),
              0.2d),
          new Tournament(nTournament),
          new Last(),
          nPop,
          true,
          false,
          (srp, r) -> new POCPopulationState<>());
    });
    solvers.put("cfgtree-ga-noxover", p -> {
      SymbolicRegressionGrammar g = new SymbolicRegressionGrammar(
          List.of(operators), p.qualityFunction().getDataset().xVarNames(), constants);
      return new AbstractStandardEvolver<>(
          new FormulaMapper()
              .andThen(TreeBasedUnivariateRealFunction.mapper(
                  p.qualityFunction().getDataset().xVarNames(),
                  p.qualityFunction().getDataset().yVarNames().get(0)))
              .andThen(MathUtils.linearScaler(p.qualityFunction())),
          new GrammarRampedHalfAndHalf<>(6, maxHeight + 4, g),
          nPop,
          StopConditions.nOfIterations(nIterations),
          Map.of(new GrammarBasedSubtreeMutation<>(maxHeight + 4, g), 0.2d),
          new Tournament(nTournament),
          new Last(),
          nPop,
          true,
          false,
          (srp, r) -> new POCPopulationState<>());
    });
    solvers.put("cfgtree-gadiv", p -> {
      SymbolicRegressionGrammar g = new SymbolicRegressionGrammar(
          List.of(operators), p.qualityFunction().getDataset().xVarNames(), constants);
      return new StandardWithEnforcedDiversityEvolver<>(
          new FormulaMapper()
              .andThen(TreeBasedUnivariateRealFunction.mapper(
                  p.qualityFunction().getDataset().xVarNames(),
                  p.qualityFunction().getDataset().yVarNames().get(0)))
              .andThen(MathUtils.linearScaler(p.qualityFunction())),
          new GrammarRampedHalfAndHalf<>(6, maxHeight + 4, g),
          nPop,
          StopConditions.nOfIterations(nIterations),
          Map.of(
              new SameRootSubtreeCrossover<>(maxHeight + 4),
              0.8d,
              new GrammarBasedSubtreeMutation<>(maxHeight + 4, g),
              0.2d),
          new Tournament(nTournament),
          new Last(),
          nPop,
          true,
          false,
          (srp, r) -> new POCPopulationState<>(),
          diversityMaxAttempts);
    });
    solvers.put(
        "fgraph-lim-ga",
        p -> new AbstractStandardEvolver<>(
            FunctionGraph.mapper(
                    p.qualityFunction().getDataset().xVarNames(),
                    p.qualityFunction().getDataset().yVarNames())
                .andThen(NamedUnivariateRealFunction::from)
                .andThen(MathUtils.linearScaler(p.qualityFunction())),
            new ShallowSparseFactory(
                0d,
                0d,
                1d,
                p.qualityFunction().getDataset().xVarNames(),
                p.qualityFunction().getDataset().yVarNames()),
            nPop,
            StopConditions.nOfIterations(nIterations),
            Map.of(
                new NodeAddition<Node, Double>(
                        FunctionNode.limitedIndexFactory(maxNodes, baseFunctions),
                        (w, r) -> w,
                        (w, r) -> r.nextGaussian())
                    .withChecker(FunctionGraph.checker()),
                graphNodeAdditionRate,
                new ArcModification<Node, Double>((w, r) -> w + r.nextGaussian(), 1d)
                    .withChecker(FunctionGraph.checker()),
                graphArcMutationRate,
                new ArcAddition<Node, Double>(RandomGenerator::nextGaussian, false)
                    .withChecker(FunctionGraph.checker()),
                graphArcAdditionRate,
                new ArcRemoval<Node, Double>(node -> (node instanceof Input)
                        || (node instanceof Constant)
                        || (node instanceof Output))
                    .withChecker(FunctionGraph.checker()),
                graphArcRemovalRate,
                new AlignedCrossover<Node, Double>(
                        (w1, w2, r) -> w1 + (w2 - w1) * (r.nextDouble() * 3d - 1d),
                        node -> (node instanceof Input)
                            || (node instanceof Constant)
                            || (node instanceof Output),
                        false)
                    .withChecker(FunctionGraph.checker()),
                graphCrossoverRate),
            new Tournament(nTournament),
            new Last(),
            nPop,
            true,
            false,
            (srp, r) -> new POCPopulationState<>()));
    solvers.put(
        "fgraph-lim-ga-noxover",
        p -> new AbstractStandardEvolver<>(
            FunctionGraph.mapper(
                    p.qualityFunction().getDataset().xVarNames(),
                    p.qualityFunction().getDataset().yVarNames())
                .andThen(NamedUnivariateRealFunction::from)
                .andThen(MathUtils.linearScaler(p.qualityFunction())),
            new ShallowSparseFactory(
                0d,
                0d,
                1d,
                p.qualityFunction().getDataset().xVarNames(),
                p.qualityFunction().getDataset().yVarNames()),
            nPop,
            StopConditions.nOfIterations(nIterations),
            Map.of(
                new NodeAddition<Node, Double>(
                        FunctionNode.limitedIndexFactory(maxNodes, baseFunctions),
                        (w, r) -> w,
                        (w, r) -> r.nextGaussian())
                    .withChecker(FunctionGraph.checker()),
                graphNodeAdditionRate,
                new ArcModification<Node, Double>((w, r) -> w + r.nextGaussian(), 1d)
                    .withChecker(FunctionGraph.checker()),
                graphArcMutationRate,
                new ArcAddition<Node, Double>(RandomGenerator::nextGaussian, false)
                    .withChecker(FunctionGraph.checker()),
                graphArcAdditionRate,
                new ArcRemoval<Node, Double>(node -> (node instanceof Input)
                        || (node instanceof Constant)
                        || (node instanceof Output))
                    .withChecker(FunctionGraph.checker()),
                graphArcRemovalRate),
            new Tournament(nTournament),
            new Last(),
            nPop,
            true,
            false,
            (srp, r) -> new POCPopulationState<>()));
    solvers.put(
        "fgraph-lim-speciated-noxover-kmeans",
        p -> new SpeciatedEvolver<>(
            FunctionGraph.mapper(
                    p.qualityFunction().getDataset().xVarNames(),
                    p.qualityFunction().getDataset().yVarNames())
                .andThen(NamedUnivariateRealFunction::from)
                .andThen(MathUtils.linearScaler(p.qualityFunction())),
            new ShallowSparseFactory(
                0d,
                0d,
                1d,
                p.qualityFunction().getDataset().xVarNames(),
                p.qualityFunction().getDataset().yVarNames()),
            nPop,
            StopConditions.nOfIterations(nIterations),
            Map.of(
                new NodeAddition<Node, Double>(
                        FunctionNode.limitedIndexFactory(maxNodes, baseFunctions),
                        (w, r) -> w,
                        (w, r) -> r.nextGaussian())
                    .withChecker(FunctionGraph.checker()),
                graphNodeAdditionRate,
                new ArcModification<Node, Double>((w, r) -> w + r.nextGaussian(), 1d)
                    .withChecker(FunctionGraph.checker()),
                graphArcMutationRate,
                new ArcAddition<Node, Double>(RandomGenerator::nextGaussian, false)
                    .withChecker(FunctionGraph.checker()),
                graphArcAdditionRate,
                new ArcRemoval<Node, Double>(node -> (node instanceof Input)
                        || (node instanceof Constant)
                        || (node instanceof Output))
                    .withChecker(FunctionGraph.checker()),
                graphArcRemovalRate),
            false,
            5,
            new KMeansSpeciator<>(
                5,
                300,
                (x, y) -> (new Jaccard())
                    .on(a -> new HashSet<>(Collections.singletonList(a)))
                    .apply(x, y),
                i -> i.genotype().nodes().stream()
                    .mapToDouble(Node::getIndex)
                    .toArray()),
            0.75));
    solvers.put(
        "fgraph-lim-speciated-noxover",
        p -> new SpeciatedEvolver<>(
            FunctionGraph.mapper(
                    p.qualityFunction().getDataset().xVarNames(),
                    p.qualityFunction().getDataset().yVarNames())
                .andThen(NamedUnivariateRealFunction::from)
                .andThen(MathUtils.linearScaler(p.qualityFunction())),
            new ShallowSparseFactory(
                0d,
                0d,
                1d,
                p.qualityFunction().getDataset().xVarNames(),
                p.qualityFunction().getDataset().yVarNames()),
            nPop,
            StopConditions.nOfIterations(nIterations),
            Map.of(
                new NodeAddition<Node, Double>(
                        FunctionNode.limitedIndexFactory(maxNodes, baseFunctions),
                        (w, r) -> w,
                        (w, r) -> r.nextGaussian())
                    .withChecker(FunctionGraph.checker()),
                graphNodeAdditionRate,
                new ArcModification<Node, Double>((w, r) -> w + r.nextGaussian(), 1d)
                    .withChecker(FunctionGraph.checker()),
                graphArcMutationRate,
                new ArcAddition<Node, Double>(RandomGenerator::nextGaussian, false)
                    .withChecker(FunctionGraph.checker()),
                graphArcAdditionRate,
                new ArcRemoval<Node, Double>(node -> (node instanceof Input)
                        || (node instanceof Constant)
                        || (node instanceof Output))
                    .withChecker(FunctionGraph.checker()),
                graphArcRemovalRate),
            false,
            5,
            new LazySpeciator<>((new Jaccard()).on(i -> i.genotype().nodes()), 0.25),
            0.75));
    solvers.put(
        "fgraph-seq-speciated-noxover",
        p -> new SpeciatedEvolver<>(
            FunctionGraph.mapper(
                    p.qualityFunction().getDataset().xVarNames(),
                    p.qualityFunction().getDataset().yVarNames())
                .andThen(NamedUnivariateRealFunction::from)
                .andThen(MathUtils.linearScaler(p.qualityFunction())),
            new ShallowSparseFactory(
                0d,
                0d,
                1d,
                p.qualityFunction().getDataset().xVarNames(),
                p.qualityFunction().getDataset().yVarNames()),
            nPop,
            StopConditions.nOfIterations(nIterations),
            Map.of(
                new NodeAddition<Node, Double>(
                        FunctionNode.sequentialIndexFactory(baseFunctions),
                        (w, r) -> w,
                        (w, r) -> r.nextGaussian())
                    .withChecker(FunctionGraph.checker()),
                graphNodeAdditionRate,
                new ArcModification<Node, Double>((w, r) -> w + r.nextGaussian(), 1d)
                    .withChecker(FunctionGraph.checker()),
                graphArcMutationRate,
                new ArcAddition<Node, Double>(RandomGenerator::nextGaussian, false)
                    .withChecker(FunctionGraph.checker()),
                graphArcAdditionRate,
                new ArcRemoval<Node, Double>(node -> (node instanceof Input)
                        || (node instanceof Constant)
                        || (node instanceof Output))
                    .withChecker(FunctionGraph.checker()),
                graphArcRemovalRate),
            false,
            5,
            new LazySpeciator<>((new Jaccard()).on(i -> i.genotype().nodes()), 0.25),
            0.75));
    solvers.put(
        "fgraph-lim-gadiv",
        p -> new StandardWithEnforcedDiversityEvolver<>(
            FunctionGraph.mapper(
                    p.qualityFunction().getDataset().xVarNames(),
                    p.qualityFunction().getDataset().yVarNames())
                .andThen(NamedUnivariateRealFunction::from)
                .andThen(MathUtils.linearScaler(p.qualityFunction())),
            new ShallowSparseFactory(
                0d,
                0d,
                1d,
                p.qualityFunction().getDataset().xVarNames(),
                p.qualityFunction().getDataset().yVarNames()),
            nPop,
            StopConditions.nOfIterations(nIterations),
            Map.of(
                new NodeAddition<Node, Double>(
                        FunctionNode.limitedIndexFactory(maxNodes, baseFunctions),
                        (w, r) -> w,
                        (w, r) -> r.nextGaussian())
                    .withChecker(FunctionGraph.checker()),
                graphNodeAdditionRate,
                new ArcModification<Node, Double>((w, r) -> w + r.nextGaussian(), 1d)
                    .withChecker(FunctionGraph.checker()),
                graphArcMutationRate,
                new ArcAddition<Node, Double>(RandomGenerator::nextGaussian, false)
                    .withChecker(FunctionGraph.checker()),
                graphArcAdditionRate,
                new ArcRemoval<>(node -> (node instanceof Input)
                    || (node instanceof Constant)
                    || (node instanceof Output)),
                graphArcRemovalRate,
                new AlignedCrossover<Node, Double>(
                        (w1, w2, r) -> w1 + (w2 - w1) * (r.nextDouble() * 3d - 1d),
                        node -> (node instanceof Input)
                            || (node instanceof Constant)
                            || (node instanceof Output),
                        false)
                    .withChecker(FunctionGraph.checker()),
                graphCrossoverRate),
            new Tournament(nTournament),
            new Last(),
            nPop,
            true,
            false,
            (srp, r) -> new POCPopulationState<>(),
            diversityMaxAttempts));
    solvers.put(
        "fgraph-lim-speciated",
        p -> new SpeciatedEvolver<>(
            FunctionGraph.mapper(
                    p.qualityFunction().getDataset().xVarNames(),
                    p.qualityFunction().getDataset().yVarNames())
                .andThen(NamedUnivariateRealFunction::from)
                .andThen(MathUtils.linearScaler(p.qualityFunction())),
            new ShallowSparseFactory(
                0d,
                0d,
                1d,
                p.qualityFunction().getDataset().xVarNames(),
                p.qualityFunction().getDataset().yVarNames()),
            nPop,
            StopConditions.nOfIterations(nIterations),
            Map.of(
                new NodeAddition<Node, Double>(
                        FunctionNode.limitedIndexFactory(maxNodes, baseFunctions),
                        (w, r) -> w,
                        (w, r) -> r.nextGaussian())
                    .withChecker(FunctionGraph.checker()),
                graphNodeAdditionRate,
                new ArcModification<Node, Double>((w, r) -> w + r.nextGaussian(), 1d)
                    .withChecker(FunctionGraph.checker()),
                graphArcMutationRate,
                new ArcAddition<Node, Double>(RandomGenerator::nextGaussian, false)
                    .withChecker(FunctionGraph.checker()),
                graphArcAdditionRate,
                new ArcRemoval<>(node -> (node instanceof Input)
                    || (node instanceof Constant)
                    || (node instanceof Output)),
                graphArcRemovalRate,
                new AlignedCrossover<Node, Double>(
                        (w1, w2, r) -> w1 + (w2 - w1) * (r.nextDouble() * 3d - 1d),
                        node -> (node instanceof Input)
                            || (node instanceof Constant)
                            || (node instanceof Output),
                        false)
                    .withChecker(FunctionGraph.checker()),
                graphCrossoverRate),
            false,
            5,
            new LazySpeciator<>((new Jaccard()).on(i -> i.genotype().nodes()), 0.25),
            0.75));
    solvers.put(
        "fgraph-seq-speciated",
        p -> new SpeciatedEvolver<>(
            FunctionGraph.mapper(
                    p.qualityFunction().getDataset().xVarNames(),
                    p.qualityFunction().getDataset().yVarNames())
                .andThen(NamedUnivariateRealFunction::from)
                .andThen(MathUtils.linearScaler(p.qualityFunction())),
            new ShallowSparseFactory(
                0d,
                0d,
                1d,
                p.qualityFunction().getDataset().xVarNames(),
                p.qualityFunction().getDataset().yVarNames()),
            nPop,
            StopConditions.nOfIterations(nIterations),
            Map.of(
                new NodeAddition<Node, Double>(
                        FunctionNode.sequentialIndexFactory(baseFunctions),
                        (w, r) -> w,
                        (w, r) -> r.nextGaussian())
                    .withChecker(FunctionGraph.checker()),
                graphNodeAdditionRate,
                new ArcModification<Node, Double>((w, r) -> w + r.nextGaussian(), 1d)
                    .withChecker(FunctionGraph.checker()),
                graphArcMutationRate,
                new ArcAddition<Node, Double>(RandomGenerator::nextGaussian, false)
                    .withChecker(FunctionGraph.checker()),
                graphArcAdditionRate,
                new ArcRemoval<Node, Double>(node -> (node instanceof Input)
                        || (node instanceof Constant)
                        || (node instanceof Output))
                    .withChecker(FunctionGraph.checker()),
                graphArcRemovalRate,
                new AlignedCrossover<Node, Double>(
                        (w1, w2, r) -> w1 + (w2 - w1) * (r.nextDouble() * 3d - 1d),
                        node -> (node instanceof Input)
                            || (node instanceof Constant)
                            || (node instanceof Output),
                        false)
                    .withChecker(FunctionGraph.checker()),
                graphCrossoverRate),
            false,
            5,
            new LazySpeciator<>((new Jaccard()).on(i -> i.genotype().nodes()), 0.25),
            0.75));
    solvers.put(
        "fgraph-seq-ga",
        p -> new AbstractStandardEvolver<>(
            FunctionGraph.mapper(
                    p.qualityFunction().getDataset().xVarNames(),
                    p.qualityFunction().getDataset().yVarNames())
                .andThen(NamedUnivariateRealFunction::from)
                .andThen(MathUtils.linearScaler(p.qualityFunction())),
            new ShallowSparseFactory(
                0d,
                0d,
                1d,
                p.qualityFunction().getDataset().xVarNames(),
                p.qualityFunction().getDataset().yVarNames()),
            nPop,
            StopConditions.nOfIterations(nIterations),
            Map.of(
                new NodeAddition<Node, Double>(
                        FunctionNode.sequentialIndexFactory(baseFunctions),
                        (w, r) -> w,
                        (w, r) -> r.nextGaussian())
                    .withChecker(FunctionGraph.checker()),
                graphNodeAdditionRate,
                new ArcModification<Node, Double>((w, r) -> w + r.nextGaussian(), 1d)
                    .withChecker(FunctionGraph.checker()),
                graphArcMutationRate,
                new ArcAddition<Node, Double>(RandomGenerator::nextGaussian, false)
                    .withChecker(FunctionGraph.checker()),
                graphArcAdditionRate,
                new ArcRemoval<Node, Double>(node -> (node instanceof Input)
                        || (node instanceof Constant)
                        || (node instanceof Output))
                    .withChecker(FunctionGraph.checker()),
                graphArcRemovalRate,
                new AlignedCrossover<Node, Double>(
                        (w1, w2, r) -> w1 + (w2 - w1) * (r.nextDouble() * 3d - 1d),
                        node -> (node instanceof Input)
                            || (node instanceof Constant)
                            || (node instanceof Output),
                        false)
                    .withChecker(FunctionGraph.checker()),
                graphCrossoverRate),
            new Tournament(nTournament),
            new Last(),
            nPop,
            true,
            false,
            (srp, r) -> new POCPopulationState<>()));
    solvers.put(
        "ograph-seq-ga",
        p -> new AbstractStandardEvolver<>(
            OperatorGraph.mapper(
                    p.qualityFunction().getDataset().xVarNames(),
                    p.qualityFunction().getDataset().yVarNames())
                .andThen(NamedUnivariateRealFunction::from)
                .andThen(MathUtils.linearScaler(p.qualityFunction())),
            new ShallowFactory(
                p.qualityFunction().getDataset().xVarNames(),
                p.qualityFunction().getDataset().yVarNames(),
                constants),
            nPop,
            StopConditions.nOfIterations(nIterations),
            Map.of(
                new NodeAddition<Node, OperatorGraph.NonValuedArc>(
                        OperatorNode.sequentialIndexFactory(baseOperators),
                        Mutation.copy(),
                        Mutation.copy())
                    .withChecker(OperatorGraph.checker()),
                graphNodeAdditionRate,
                new ArcAddition<Node, OperatorGraph.NonValuedArc>(
                        r -> OperatorGraph.NON_VALUED_ARC, false)
                    .withChecker(OperatorGraph.checker()),
                graphArcAdditionRate,
                new ArcRemoval<Node, OperatorGraph.NonValuedArc>(node -> (node instanceof Input)
                        || (node instanceof Constant)
                        || (node instanceof Output))
                    .withChecker(OperatorGraph.checker()),
                graphArcRemovalRate,
                new AlignedCrossover<Node, OperatorGraph.NonValuedArc>(
                        Crossover.randomCopy(),
                        node -> (node instanceof Input)
                            || (node instanceof Constant)
                            || (node instanceof Output),
                        false)
                    .withChecker(OperatorGraph.checker()),
                graphCrossoverRate),
            new Tournament(nTournament),
            new Last(),
            nPop,
            true,
            false,
            (srp, r) -> new POCPopulationState<>()));
    solvers.put(
        "ograph-seq-speciated-noxover",
        p -> new SpeciatedEvolver<>(
            OperatorGraph.mapper(
                    p.qualityFunction().getDataset().xVarNames(),
                    p.qualityFunction().getDataset().yVarNames())
                .andThen(NamedUnivariateRealFunction::from)
                .andThen(MathUtils.linearScaler(p.qualityFunction())),
            new ShallowFactory(
                p.qualityFunction().getDataset().xVarNames(),
                p.qualityFunction().getDataset().yVarNames(),
                constants),
            nPop,
            StopConditions.nOfIterations(nIterations),
            Map.of(
                new NodeAddition<Node, OperatorGraph.NonValuedArc>(
                        OperatorNode.sequentialIndexFactory(baseOperators),
                        Mutation.copy(),
                        Mutation.copy())
                    .withChecker(OperatorGraph.checker()),
                graphNodeAdditionRate,
                new ArcAddition<Node, OperatorGraph.NonValuedArc>(
                        r -> OperatorGraph.NON_VALUED_ARC, false)
                    .withChecker(OperatorGraph.checker()),
                graphArcAdditionRate,
                new ArcRemoval<Node, OperatorGraph.NonValuedArc>(node -> (node instanceof Input)
                        || (node instanceof Constant)
                        || (node instanceof Output))
                    .withChecker(OperatorGraph.checker()),
                graphArcRemovalRate),
            false,
            5,
            new LazySpeciator<>((new Jaccard()).on(i -> i.genotype().nodes()), 0.25),
            0.75));
    solvers.put("fgraph-hash-ga", p -> {
      Function<Graph<IndexedNode<Node>, Double>, Graph<Node, Double>> graphMapper =
          GraphUtils.mapper(IndexedNode::content, Misc::first);
      Predicate<Graph<Node, Double>> checker = FunctionGraph.checker();
      return new AbstractStandardEvolver<>(
          graphMapper
              .andThen(FunctionGraph.mapper(
                  p.qualityFunction().getDataset().xVarNames(),
                  p.qualityFunction().getDataset().yVarNames()))
              .andThen(NamedUnivariateRealFunction::from)
              .andThen(MathUtils.linearScaler(p.qualityFunction())),
          new ShallowSparseFactory(
                  0d,
                  0d,
                  1d,
                  p.qualityFunction().getDataset().xVarNames(),
                  p.qualityFunction().getDataset().yVarNames())
              .then(GraphUtils.mapper(IndexedNode.incrementerMapper(Node.class), Misc::first)),
          nPop,
          StopConditions.nOfIterations(nIterations),
          Map.of(
              new NodeAddition<IndexedNode<Node>, Double>(
                      FunctionNode.sequentialIndexFactory(baseFunctions)
                          .then(IndexedNode.hashMapper(Node.class)),
                      (w, r) -> w,
                      (w, r) -> r.nextGaussian())
                  .withChecker(g -> checker.test(graphMapper.apply(g))),
              graphNodeAdditionRate,
              new ArcModification<IndexedNode<Node>, Double>((w, r) -> w + r.nextGaussian(), 1d)
                  .withChecker(g -> checker.test(graphMapper.apply(g))),
              graphArcMutationRate,
              new ArcAddition<IndexedNode<Node>, Double>(RandomGenerator::nextGaussian, false)
                  .withChecker(g -> checker.test(graphMapper.apply(g))),
              graphArcAdditionRate,
              new ArcRemoval<IndexedNode<Node>, Double>(node -> node.content() instanceof Output)
                  .withChecker(g -> checker.test(graphMapper.apply(g))),
              graphArcRemovalRate,
              new AlignedCrossover<IndexedNode<Node>, Double>(
                      (w1, w2, r) -> w1 + (w2 - w1) * (r.nextDouble() * 3d - 1d),
                      node -> node.content() instanceof Output,
                      false)
                  .withChecker(g -> checker.test(graphMapper.apply(g))),
              graphCrossoverRate),
          new Tournament(nTournament),
          new Last(),
          nPop,
          true,
          false,
          (srp, r) -> new POCPopulationState<>());
    });
    solvers.put("fgraph-hash-speciated", p -> {
      Function<Graph<IndexedNode<Node>, Double>, Graph<Node, Double>> graphMapper =
          GraphUtils.mapper(IndexedNode::content, Misc::first);
      Predicate<Graph<Node, Double>> checker = FunctionGraph.checker();
      return new SpeciatedEvolver<>(
          graphMapper
              .andThen(FunctionGraph.mapper(
                  p.qualityFunction().getDataset().xVarNames(),
                  p.qualityFunction().getDataset().yVarNames()))
              .andThen(NamedUnivariateRealFunction::from)
              .andThen(MathUtils.linearScaler(p.qualityFunction())),
          new ShallowSparseFactory(
                  0d,
                  0d,
                  1d,
                  p.qualityFunction().getDataset().xVarNames(),
                  p.qualityFunction().getDataset().yVarNames())
              .then(GraphUtils.mapper(IndexedNode.incrementerMapper(Node.class), Misc::first)),
          nPop,
          StopConditions.nOfIterations(nIterations),
          Map.of(
              new NodeAddition<IndexedNode<Node>, Double>(
                      FunctionNode.sequentialIndexFactory(baseFunctions)
                          .then(IndexedNode.hashMapper(Node.class)),
                      (w, r) -> w,
                      (w, r) -> r.nextGaussian())
                  .withChecker(g -> checker.test(graphMapper.apply(g))),
              graphNodeAdditionRate,
              new ArcModification<IndexedNode<Node>, Double>((w, r) -> w + r.nextGaussian(), 1d)
                  .withChecker(g -> checker.test(graphMapper.apply(g))),
              graphArcMutationRate,
              new ArcAddition<IndexedNode<Node>, Double>(RandomGenerator::nextGaussian, false)
                  .withChecker(g -> checker.test(graphMapper.apply(g))),
              graphArcAdditionRate,
              new ArcRemoval<IndexedNode<Node>, Double>(node -> node.content() instanceof Output)
                  .withChecker(g -> checker.test(graphMapper.apply(g))),
              graphArcRemovalRate,
              new AlignedCrossover<IndexedNode<Node>, Double>(
                      (w1, w2, r) -> w1 + (w2 - w1) * (r.nextDouble() * 3d - 1d),
                      node -> node.content() instanceof Output,
                      false)
                  .withChecker(g -> checker.test(graphMapper.apply(g))),
              graphCrossoverRate),
          false,
          5,
          new LazySpeciator<>((new Jaccard()).on(i -> i.genotype().nodes()), 0.25),
          0.75);
    });
    solvers.put("fgraph-hash+-speciated", p -> {
      Function<Graph<IndexedNode<Node>, Double>, Graph<Node, Double>> graphMapper =
          GraphUtils.mapper(IndexedNode::content, Misc::first);
      Predicate<Graph<Node, Double>> checker = FunctionGraph.checker();
      return new SpeciatedEvolver<>(
          GraphUtils.mapper(
                  (Function<IndexedNode<Node>, Node>) IndexedNode::content,
                  (Function<Collection<Double>, Double>) Misc::first)
              .andThen(FunctionGraph.mapper(
                  p.qualityFunction().getDataset().xVarNames(),
                  p.qualityFunction().getDataset().yVarNames()))
              .andThen(NamedUnivariateRealFunction::from)
              .andThen(MathUtils.linearScaler(p.qualityFunction())),
          new ShallowSparseFactory(
                  0d,
                  0d,
                  1d,
                  p.qualityFunction().getDataset().xVarNames(),
                  p.qualityFunction().getDataset().yVarNames())
              .then(GraphUtils.mapper(IndexedNode.incrementerMapper(Node.class), Misc::first)),
          nPop,
          StopConditions.nOfIterations(nIterations),
          Map.of(
              new IndexedNodeAddition<FunctionNode, Node, Double>(
                      FunctionNode.sequentialIndexFactory(baseFunctions),
                      n -> (n instanceof FunctionNode)
                          ? ((FunctionNode) n)
                              .getFunction()
                              .hashCode()
                          : 0,
                      p.qualityFunction()
                              .getDataset()
                              .xVarNames()
                              .size()
                          + 1
                          + 1,
                      (w, r) -> w,
                      (w, r) -> r.nextGaussian())
                  .withChecker(g -> checker.test(graphMapper.apply(g))),
              graphNodeAdditionRate,
              new ArcModification<IndexedNode<Node>, Double>((w, r) -> w + r.nextGaussian(), 1d)
                  .withChecker(g -> checker.test(graphMapper.apply(g))),
              graphArcMutationRate,
              new ArcAddition<IndexedNode<Node>, Double>(RandomGenerator::nextGaussian, false)
                  .withChecker(g -> checker.test(graphMapper.apply(g))),
              graphArcAdditionRate,
              new ArcRemoval<IndexedNode<Node>, Double>(node -> node.content() instanceof Output)
                  .withChecker(g -> checker.test(graphMapper.apply(g))),
              graphArcRemovalRate,
              new AlignedCrossover<IndexedNode<Node>, Double>(
                      (w1, w2, r) -> w1 + (w2 - w1) * (r.nextDouble() * 3d - 1d),
                      node -> node.content() instanceof Output,
                      false)
                  .withChecker(g -> checker.test(graphMapper.apply(g))),
              graphCrossoverRate),
          false,
          5,
          new LazySpeciator<>((new Jaccard()).on(i -> i.genotype().nodes()), 0.25),
          0.75);
    });
    solvers.put("ograph-hash+-speciated", p -> {
      Function<Graph<IndexedNode<Node>, OperatorGraph.NonValuedArc>, Graph<Node, OperatorGraph.NonValuedArc>>
          graphMapper = GraphUtils.mapper(IndexedNode::content, Misc::first);
      Predicate<Graph<Node, OperatorGraph.NonValuedArc>> checker = OperatorGraph.checker();
      return new SpeciatedEvolver<>(
          graphMapper
              .andThen(OperatorGraph.mapper(
                  p.qualityFunction().getDataset().xVarNames(),
                  p.qualityFunction().getDataset().yVarNames()))
              .andThen(NamedUnivariateRealFunction::from)
              .andThen(MathUtils.linearScaler(p.qualityFunction())),
          new ShallowFactory(
                  p.qualityFunction().getDataset().xVarNames(),
                  p.qualityFunction().getDataset().yVarNames(),
                  constants)
              .then(GraphUtils.mapper(IndexedNode.incrementerMapper(Node.class), Misc::first)),
          nPop,
          StopConditions.nOfIterations(nIterations),
          Map.of(
              new IndexedNodeAddition<OperatorNode, Node, OperatorGraph.NonValuedArc>(
                      OperatorNode.sequentialIndexFactory(baseOperators),
                      n -> (n instanceof OperatorNode)
                          ? ((OperatorNode) n)
                              .getOperator()
                              .hashCode()
                          : 0,
                      p.qualityFunction()
                              .getDataset()
                              .xVarNames()
                              .size()
                          + 1
                          + constants.size(),
                      Mutation.copy(),
                      Mutation.copy())
                  .withChecker(g -> checker.test(graphMapper.apply(g))),
              graphNodeAdditionRate,
              new ArcAddition<IndexedNode<Node>, OperatorGraph.NonValuedArc>(
                      r -> OperatorGraph.NON_VALUED_ARC, false)
                  .withChecker(g -> checker.test(graphMapper.apply(g))),
              graphArcAdditionRate,
              new ArcRemoval<IndexedNode<Node>, OperatorGraph.NonValuedArc>(
                      node -> node.content() instanceof Output)
                  .withChecker(g -> checker.test(graphMapper.apply(g))),
              graphArcRemovalRate,
              new AlignedCrossover<IndexedNode<Node>, OperatorGraph.NonValuedArc>(
                      Crossover.randomCopy(), node -> node.content() instanceof Output, false)
                  .withChecker(g -> checker.test(graphMapper.apply(g))),
              graphCrossoverRate),
          false,
          5,
          new LazySpeciator<>((new Jaccard()).on(i -> i.genotype().nodes()), 0.25),
          0.75);
    });
    solvers.put("fgraph-hash+-speciated-noxover", p -> {
      Function<Graph<IndexedNode<Node>, Double>, Graph<Node, Double>> graphMapper =
          GraphUtils.mapper(IndexedNode::content, Misc::first);
      Predicate<Graph<Node, Double>> checker = FunctionGraph.checker();
      return new SpeciatedEvolver<>(
          graphMapper
              .andThen(FunctionGraph.mapper(
                  p.qualityFunction().getDataset().xVarNames(),
                  p.qualityFunction().getDataset().yVarNames()))
              .andThen(NamedUnivariateRealFunction::from)
              .andThen(MathUtils.linearScaler(p.qualityFunction())),
          new ShallowSparseFactory(
                  0d,
                  0d,
                  1d,
                  p.qualityFunction().getDataset().xVarNames(),
                  p.qualityFunction().getDataset().yVarNames())
              .then(GraphUtils.mapper(IndexedNode.incrementerMapper(Node.class), Misc::first)),
          nPop,
          StopConditions.nOfIterations(nIterations),
          Map.of(
              new IndexedNodeAddition<FunctionNode, Node, Double>(
                      FunctionNode.sequentialIndexFactory(baseFunctions),
                      n -> (n instanceof FunctionNode)
                          ? ((FunctionNode) n)
                              .getFunction()
                              .hashCode()
                          : 0,
                      p.qualityFunction()
                              .getDataset()
                              .xVarNames()
                              .size()
                          + 1
                          + 1,
                      (w, r) -> w,
                      (w, r) -> r.nextGaussian())
                  .withChecker(g -> checker.test(graphMapper.apply(g))),
              graphNodeAdditionRate,
              new ArcModification<IndexedNode<Node>, Double>((w, r) -> w + r.nextGaussian(), 1d)
                  .withChecker(g -> checker.test(graphMapper.apply(g))),
              graphArcMutationRate,
              new ArcAddition<IndexedNode<Node>, Double>(RandomGenerator::nextGaussian, false)
                  .withChecker(g -> checker.test(graphMapper.apply(g))),
              graphArcAdditionRate,
              new ArcRemoval<IndexedNode<Node>, Double>(node -> node.content() instanceof Output)
                  .withChecker(g -> checker.test(graphMapper.apply(g))),
              graphArcRemovalRate),
          false,
          5,
          new LazySpeciator<>((new Jaccard()).on(i -> i.genotype().nodes()), 0.25),
          0.75);
    });
    solvers.put(
        "fgraph-seq-ga-noxover",
        p -> new AbstractStandardEvolver<>(
            FunctionGraph.mapper(
                    p.qualityFunction().getDataset().xVarNames(),
                    p.qualityFunction().getDataset().yVarNames())
                .andThen(NamedUnivariateRealFunction::from)
                .andThen(MathUtils.linearScaler(p.qualityFunction())),
            new ShallowSparseFactory(
                0d,
                0d,
                1d,
                p.qualityFunction().getDataset().xVarNames(),
                p.qualityFunction().getDataset().yVarNames()),
            nPop,
            StopConditions.nOfIterations(nIterations),
            Map.of(
                new NodeAddition<Node, Double>(
                        FunctionNode.sequentialIndexFactory(baseFunctions),
                        (w, r) -> w,
                        (w, r) -> r.nextGaussian())
                    .withChecker(FunctionGraph.checker()),
                graphNodeAdditionRate,
                new ArcModification<Node, Double>((w, r) -> w + r.nextGaussian(), 1d)
                    .withChecker(FunctionGraph.checker()),
                graphArcMutationRate,
                new ArcAddition<Node, Double>(RandomGenerator::nextGaussian, false)
                    .withChecker(FunctionGraph.checker()),
                graphArcAdditionRate,
                new ArcRemoval<Node, Double>(node -> (node instanceof Input)
                        || (node instanceof Constant)
                        || (node instanceof Output))
                    .withChecker(FunctionGraph.checker()),
                graphArcRemovalRate),
            new Tournament(nTournament),
            new Last(),
            nPop,
            true,
            false,
            (srp, r) -> new POCPopulationState<>()));
    solvers.put(
        "fgraph-seq-gadiv",
        p -> new StandardWithEnforcedDiversityEvolver<>(
            FunctionGraph.mapper(
                    p.qualityFunction().getDataset().xVarNames(),
                    p.qualityFunction().getDataset().yVarNames())
                .andThen(NamedUnivariateRealFunction::from)
                .andThen(MathUtils.linearScaler(p.qualityFunction())),
            new ShallowSparseFactory(
                0d,
                0d,
                1d,
                p.qualityFunction().getDataset().xVarNames(),
                p.qualityFunction().getDataset().yVarNames()),
            nPop,
            StopConditions.nOfIterations(nIterations),
            Map.of(
                new NodeAddition<Node, Double>(
                        FunctionNode.sequentialIndexFactory(baseFunctions),
                        (w, r) -> w,
                        (w, r) -> r.nextGaussian())
                    .withChecker(FunctionGraph.checker()),
                graphNodeAdditionRate,
                new ArcModification<Node, Double>((w, r) -> w + r.nextGaussian(), 1d)
                    .withChecker(FunctionGraph.checker()),
                graphArcMutationRate,
                new ArcAddition<Node, Double>(RandomGenerator::nextGaussian, false)
                    .withChecker(FunctionGraph.checker()),
                graphArcAdditionRate,
                new ArcRemoval<Node, Double>(node -> (node instanceof Input)
                        || (node instanceof Constant)
                        || (node instanceof Output))
                    .withChecker(FunctionGraph.checker()),
                graphArcRemovalRate,
                new AlignedCrossover<Node, Double>(
                        (w1, w2, r) -> w1 + (w2 - w1) * (r.nextDouble() * 3d - 1d),
                        node -> (node instanceof Input)
                            || (node instanceof Constant)
                            || (node instanceof Output),
                        false)
                    .withChecker(FunctionGraph.checker()),
                graphCrossoverRate),
            new Tournament(nTournament),
            new Last(),
            nPop,
            true,
            false,
            (srp, r) -> new POCPopulationState<>(),
            diversityMaxAttempts));

    // filter evolvers
    solvers = solvers.entrySet().stream()
        .filter(e -> e.getKey().matches(evolverNamePattern))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    L.info(String.format("Going to test with %d evolvers: %s%n", solvers.size(), solvers.keySet()));
    // run
    for (int seed : seeds) {
      for (SyntheticUnivariateRegressionProblem problem : problems) {
        for (Map.Entry<
                String,
                Function<
                    SyntheticUnivariateRegressionProblem,
                    IterativeSolver<
                        ? extends POCPopulationState<?, NamedUnivariateRealFunction, Double>,
                        SyntheticUnivariateRegressionProblem,
                        NamedUnivariateRealFunction>>>
            solverEntry : solvers.entrySet()) {
          Map<String, Object> keys = Map.ofEntries(
              Map.entry("seed", seed),
              Map.entry(
                  "problem",
                  problem.getClass().getSimpleName().toLowerCase()),
              Map.entry("evolver", solverEntry.getKey()));
          try {
            Stopwatch stopwatch = Stopwatch.createStarted();
            IterativeSolver<
                    ? extends POCPopulationState<?, NamedUnivariateRealFunction, Double>,
                    SyntheticUnivariateRegressionProblem,
                    NamedUnivariateRealFunction>
                solver = solverEntry.getValue().apply(problem);
            L.info(String.format("Starting %s", keys));
            Collection<NamedUnivariateRealFunction> solutions = solver.solve(
                problem,
                new Random(seed),
                executorService,
                listenerFactory.build(keys).deferred(executorService));
            L.info(String.format(
                "Done %s: %d solutions in %4.1fs",
                keys, solutions.size(), (double) stopwatch.elapsed(TimeUnit.MILLISECONDS) / 1000d));
          } catch (SolverException e) {
            L.severe(String.format("Cannot complete %s due to %s", keys, e));
            e.printStackTrace();
          }
        }
      }
    }
    listenerFactory.shutdown();
  }
}
