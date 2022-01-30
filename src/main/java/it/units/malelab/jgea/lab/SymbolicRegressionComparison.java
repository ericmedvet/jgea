/*
 * Copyright 2020 Eric Medvet <eric.medvet@gmail.com> (as eric)
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

package it.units.malelab.jgea.lab;

import com.google.common.base.Stopwatch;
import it.units.malelab.jgea.Worker;
import it.units.malelab.jgea.core.IndependentFactory;
import it.units.malelab.jgea.core.listener.CSVPrinter;
import it.units.malelab.jgea.core.listener.Factory;
import it.units.malelab.jgea.core.listener.NamedFunction;
import it.units.malelab.jgea.core.listener.TabularPrinter;
import it.units.malelab.jgea.core.operator.Crossover;
import it.units.malelab.jgea.core.operator.Mutation;
import it.units.malelab.jgea.core.selector.Last;
import it.units.malelab.jgea.core.selector.Tournament;
import it.units.malelab.jgea.core.solver.*;
import it.units.malelab.jgea.core.solver.speciation.KMeansSpeciator;
import it.units.malelab.jgea.core.solver.speciation.LazySpeciator;
import it.units.malelab.jgea.core.solver.speciation.SpeciatedEvolver;
import it.units.malelab.jgea.core.solver.state.POSetPopulationState;
import it.units.malelab.jgea.core.util.Misc;
import it.units.malelab.jgea.distance.Jaccard;
import it.units.malelab.jgea.problem.symbolicregression.*;
import it.units.malelab.jgea.representation.grammar.cfggp.GrammarBasedSubtreeMutation;
import it.units.malelab.jgea.representation.grammar.cfggp.GrammarRampedHalfAndHalf;
import it.units.malelab.jgea.representation.graph.*;
import it.units.malelab.jgea.representation.graph.numeric.Input;
import it.units.malelab.jgea.representation.graph.numeric.Output;
import it.units.malelab.jgea.representation.graph.numeric.functiongraph.BaseFunction;
import it.units.malelab.jgea.representation.graph.numeric.functiongraph.FunctionGraph;
import it.units.malelab.jgea.representation.graph.numeric.functiongraph.FunctionNode;
import it.units.malelab.jgea.representation.graph.numeric.functiongraph.ShallowSparseFactory;
import it.units.malelab.jgea.representation.graph.numeric.operatorgraph.BaseOperator;
import it.units.malelab.jgea.representation.graph.numeric.operatorgraph.OperatorGraph;
import it.units.malelab.jgea.representation.graph.numeric.operatorgraph.OperatorNode;
import it.units.malelab.jgea.representation.graph.numeric.operatorgraph.ShallowFactory;
import it.units.malelab.jgea.representation.tree.*;

import java.io.File;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.random.RandomGenerator;
import java.util.stream.Collectors;

import static it.units.malelab.jgea.core.listener.NamedFunctions.*;
import static it.units.malelab.jgea.core.util.Args.i;
import static it.units.malelab.jgea.core.util.Args.ri;

/**
 * @author eric
 */

// /usr/lib/jvm/jdk-14.0.1/bin/java -cp ~/IdeaProjects/jgea/out/artifacts/jgea_jar/jgea.jar it.units.malelab.jgea.lab
// .SymbolicRegressionComparison seed=0:10 file=results-%s.txt
public class SymbolicRegressionComparison extends Worker {

  public SymbolicRegressionComparison(String[] args) {
    super(args);
  }

  public static void main(String[] args) {
    new SymbolicRegressionComparison(args);
  }

  private static String[] vars(int n) {
    if (n == 1) {
      return new String[]{"x"};
    }
    String[] vars = new String[n];
    for (int i = 0; i < n; i++) {
      vars[i] = "x" + i;
    }
    return vars;
  }

  @Override
  public void run() {
    int nPop = i(a("nPop", "100"));
    int maxHeight = i(a("maxHeight", "10"));
    int maxNodes = i(a("maxNodes", "20"));
    int nTournament = 5;
    int diversityMaxAttempts = 100;
    int nIterations = i(a("nIterations", "100"));
    String evolverNamePattern = a("evolver", "ograph-hash\\+-speciated");
    int[] seeds = ri(a("seed", "0:1"));
    double graphArcAdditionRate = 3d;
    double graphArcMutationRate = 1d;
    double graphArcRemovalRate = 0d;
    double graphNodeAdditionRate = 1d;
    double graphCrossoverRate = 1d;
    SymbolicRegressionFitness.Metric metric = SymbolicRegressionFitness.Metric.MSE;
    Element.Operator[] operators = new Element.Operator[]{Element.Operator.ADDITION, Element.Operator.SUBTRACTION,
        Element.Operator.MULTIPLICATION, Element.Operator.PROT_DIVISION, Element.Operator.PROT_LOG};
    BaseOperator[] baseOperators = new BaseOperator[]{BaseOperator.ADDITION, BaseOperator.SUBTRACTION,
        BaseOperator.MULTIPLICATION, BaseOperator.PROT_DIVISION, BaseOperator.PROT_LOG};
    BaseFunction[] baseFunctions = new BaseFunction[]{BaseFunction.RE_LU, BaseFunction.GAUSSIAN,
        BaseFunction.PROT_INVERSE, BaseFunction.SQ};
    double[] constants = new double[]{0.1, 1d, 10d};
    List<SymbolicRegressionProblem> problems = List.of(
        new Nguyen7(metric, 1),
        new Keijzer6(metric),
        new Polynomial4(metric)
        //,
        // new Pagie1(metric)
    );
    //consumers
    List<NamedFunction<? super POSetPopulationState<?, ?, ? extends Double>, ?>> functions = List.of(
        iterations(),
        births(),
        elapsedSeconds(),
        size().of(all()),
        size().of(firsts()),
        size().of(lasts()),
        uniqueness().of(each(genotype())).of(all()),
        uniqueness().of(each(solution())).of(all()),
        uniqueness().of(each(fitness())).of(all()),
        size().of(genotype()).of(best()),
        size().of(solution()).of(best()),
        fitnessMappingIteration().of(best()),
        fitness().reformat("%5.3f").of(best()),
        hist(8).of(each(fitness())).of(all()),
        solution().reformat("%30.30s").of(best())
    );
    List<NamedFunction<? super Map<String, Object>, ?>> kFunctions = List.of(
        attribute("seed").reformat("%2d"),
        attribute("problem").reformat(NamedFunction.formatOfLongest(
            problems.stream().map(p -> p.getClass().getSimpleName())
                .toList())),
        attribute("evolver").reformat("%20.20s")
    );
    Factory<POSetPopulationState<?, ?, ? extends Double>, Map<String, Object>> listenerFactory = new TabularPrinter<>(
        functions,
        kFunctions
    );
    if (a("file", null) != null) {
      listenerFactory = Factory.all(List.of(
          listenerFactory,
          new CSVPrinter<>(functions, kFunctions, new File(a("file", null)))
      ));
    }
    //evolvers
    Map<String, Function<SymbolicRegressionProblem, IterativeSolver<? extends POSetPopulationState<?, RealFunction,
        Double>, SymbolicRegressionProblem, RealFunction>>> solvers = new TreeMap<>();
    solvers.put("tree-ga", p -> {
      IndependentFactory<Element> terminalFactory = IndependentFactory.oneOf(
          IndependentFactory.picker(Arrays.stream(
                  vars(p.qualityFunction().arity()))
              .sequential()
              .map(Element.Variable::new)
              .toArray(Element.Variable[]::new)),
          IndependentFactory.picker(Arrays.stream(constants)
              .mapToObj(Element.Constant::new)
              .toArray(Element.Constant[]::new))
      );
      return new StandardEvolver<>(
          ((Function<Tree<Element>, RealFunction>) t -> new TreeBasedRealFunction(
              t,
              vars(p.qualityFunction().arity())
          )).andThen(MathUtils.linearScaler(p.qualityFunction())),
          new RampedHalfAndHalf<>(
              4,
              maxHeight,
              Element.Operator.arityFunction(),
              IndependentFactory.picker(operators),
              terminalFactory
          ),
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
                      terminalFactory
                  )
              ),
              0.2d
          ),
          new Tournament(nTournament),
          new Last(),
          nPop,
          true,
          false,
          (srp, r) -> new POSetPopulationState<>()
      );
    });
    solvers.put("tree-ga-noxover", p -> {
      IndependentFactory<Element> terminalFactory = IndependentFactory.oneOf(
          IndependentFactory.picker(Arrays.stream(
                  vars(p.qualityFunction().arity()))
              .sequential()
              .map(Element.Variable::new)
              .toArray(Element.Variable[]::new)),
          IndependentFactory.picker(Arrays.stream(constants)
              .mapToObj(Element.Constant::new)
              .toArray(Element.Constant[]::new))
      );
      return new StandardEvolver<>(
          ((Function<Tree<Element>, RealFunction>) t -> new TreeBasedRealFunction(
              t,
              vars(p.qualityFunction().arity())
          )).andThen(MathUtils.linearScaler(p.qualityFunction())),
          new RampedHalfAndHalf<>(
              4,
              maxHeight,
              Element.Operator.arityFunction(),
              IndependentFactory.picker(operators),
              terminalFactory
          ),
          nPop,
          StopConditions.nOfIterations(nIterations),
          Map.of(new SubtreeMutation<>(
              maxHeight,
              new GrowTreeBuilder<>(
                  Element.Operator.arityFunction(),
                  IndependentFactory.picker(operators),
                  terminalFactory
              )
          ), 0.2d),
          new Tournament(nTournament),
          new Last(),
          nPop,
          true,
          false,
          (srp, r) -> new POSetPopulationState<>()
      );
    });
    solvers.put("tree-gadiv", p -> {
      IndependentFactory<Element> terminalFactory = IndependentFactory.oneOf(
          IndependentFactory.picker(Arrays.stream(
                  vars(p.qualityFunction().arity()))
              .sequential()
              .map(Element.Variable::new)
              .toArray(Element.Variable[]::new)),
          IndependentFactory.picker(Arrays.stream(constants)
              .mapToObj(Element.Constant::new)
              .toArray(Element.Constant[]::new))
      );
      return new StandardWithEnforcedDiversityEvolver<>(
          ((Function<Tree<Element>, RealFunction>) t -> new TreeBasedRealFunction(
              t,
              vars(p.qualityFunction().arity())
          )).andThen(MathUtils.linearScaler(p.qualityFunction())),
          new RampedHalfAndHalf<>(
              4,
              maxHeight,
              Element.Operator.arityFunction(),
              IndependentFactory.picker(operators),
              terminalFactory
          ),
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
                      terminalFactory
                  )
              ),
              0.2d
          ),
          new Tournament(nTournament),
          new Last(),
          nPop,
          true,
          false,
          (srp, r) -> new POSetPopulationState<>(),
          diversityMaxAttempts
      );
    });
    solvers.put("cfgtree-ga", p -> {
      SymbolicRegressionGrammar g = new SymbolicRegressionGrammar(
          List.of(operators),
          List.of(vars(p.qualityFunction().arity())),
          Arrays.stream(constants).boxed().toList()
      );
      return new StandardEvolver<>(
          new FormulaMapper().andThen(n -> TreeBasedRealFunction.from(
              n,
              vars(p.qualityFunction().arity())
          )).andThen(MathUtils.linearScaler(p.qualityFunction())),
          new GrammarRampedHalfAndHalf<>(6, maxHeight + 4, g),
          nPop,
          StopConditions.nOfIterations(nIterations),
          Map.of(
              new SameRootSubtreeCrossover<>(maxHeight + 4),
              0.8d,
              new GrammarBasedSubtreeMutation<>(maxHeight + 4, g),
              0.2d
          ),
          new Tournament(nTournament),
          new Last(),
          nPop,
          true,
          false,
          (srp, r) -> new POSetPopulationState<>()
      );
    });
    solvers.put("cfgtree-ga-noxover", p -> {
      SymbolicRegressionGrammar g = new SymbolicRegressionGrammar(
          List.of(operators),
          List.of(vars(p.qualityFunction().arity())),
          Arrays.stream(constants).boxed().toList()
      );
      return new StandardEvolver<>(
          new FormulaMapper().andThen(n -> TreeBasedRealFunction.from(
              n,
              vars(p.qualityFunction().arity())
          )).andThen(MathUtils.linearScaler(p.qualityFunction())),
          new GrammarRampedHalfAndHalf<>(6, maxHeight + 4, g),
          nPop,
          StopConditions.nOfIterations(nIterations),
          Map.of(new GrammarBasedSubtreeMutation<>(maxHeight + 4, g), 0.2d),
          new Tournament(nTournament),
          new Last(),
          nPop,
          true,
          false,
          (srp, r) -> new POSetPopulationState<>()
      );
    });
    solvers.put("cfgtree-gadiv", p -> {
      SymbolicRegressionGrammar g = new SymbolicRegressionGrammar(
          List.of(operators),
          List.of(vars(p.qualityFunction().arity())),
          Arrays.stream(constants).boxed().toList()
      );
      return new StandardWithEnforcedDiversityEvolver<>(
          new FormulaMapper().andThen(n -> TreeBasedRealFunction.from(
              n,
              vars(p.qualityFunction().arity())
          )).andThen(MathUtils.linearScaler(p.qualityFunction())),
          new GrammarRampedHalfAndHalf<>(6, maxHeight + 4, g),
          nPop,
          StopConditions.nOfIterations(nIterations),
          Map.of(
              new SameRootSubtreeCrossover<>(maxHeight + 4),
              0.8d,
              new GrammarBasedSubtreeMutation<>(maxHeight + 4, g),
              0.2d
          ),
          new Tournament(nTournament),
          new Last(),
          nPop,
          true,
          false,
          (srp, r) -> new POSetPopulationState<>(),
          diversityMaxAttempts
      );
    });
    solvers.put("fgraph-lim-ga", p -> new StandardEvolver<>(
        FunctionGraph.builder()
            .andThen(MathUtils.fromMultivariateBuilder())
            .andThen(MathUtils.linearScaler(p.qualityFunction())),
        new ShallowSparseFactory(0d, 0d, 1d, p.qualityFunction().arity(), 1),
        nPop,
        StopConditions.nOfIterations(nIterations),
        Map.of(
            new NodeAddition<Node, Double>(
                FunctionNode.limitedIndexFactory(maxNodes, baseFunctions),
                (w, r) -> w,
                (w, r) -> r.nextGaussian()
            ).withChecker(FunctionGraph.checker()),
            graphNodeAdditionRate,
            new ArcModification<Node, Double>((w, r) -> w + r.nextGaussian(), 1d).withChecker(FunctionGraph.checker()),
            graphArcMutationRate,
            new ArcAddition<Node, Double>(RandomGenerator::nextGaussian, false).withChecker(FunctionGraph.checker()),
            graphArcAdditionRate,
            new ArcRemoval<Node, Double>(node -> (node instanceof Input) || (node instanceof it.units.malelab.jgea.representation.graph.numeric.Constant) || (node instanceof Output)).withChecker(
                FunctionGraph.checker()),
            graphArcRemovalRate,
            new AlignedCrossover<Node, Double>(
                (w1, w2, r) -> w1 + (w2 - w1) * (r.nextDouble() * 3d - 1d),
                node -> (node instanceof Input) || (node instanceof it.units.malelab.jgea.representation.graph.numeric.Constant) || (node instanceof Output),
                false
            ).withChecker(FunctionGraph.checker()),
            graphCrossoverRate
        ),
        new Tournament(nTournament),
        new Last(),
        nPop,
        true,
        false,
        (srp, r) -> new POSetPopulationState<>()
    ));
    solvers.put("fgraph-lim-ga-noxover", p -> new StandardEvolver<>(
        FunctionGraph.builder()
            .andThen(MathUtils.fromMultivariateBuilder())
            .andThen(MathUtils.linearScaler(p.qualityFunction())),
        new ShallowSparseFactory(0d, 0d, 1d, p.qualityFunction().arity(), 1),
        nPop,
        StopConditions.nOfIterations(nIterations),
        Map.of(
            new NodeAddition<Node, Double>(
                FunctionNode.limitedIndexFactory(maxNodes, baseFunctions),
                (w, r) -> w,
                (w, r) -> r.nextGaussian()
            ).withChecker(FunctionGraph.checker()),
            graphNodeAdditionRate,
            new ArcModification<Node, Double>((w, r) -> w + r.nextGaussian(), 1d).withChecker(FunctionGraph.checker()),
            graphArcMutationRate,
            new ArcAddition<Node, Double>(RandomGenerator::nextGaussian, false).withChecker(FunctionGraph.checker()),
            graphArcAdditionRate,
            new ArcRemoval<Node, Double>(node -> (node instanceof Input) || (node instanceof it.units.malelab.jgea.representation.graph.numeric.Constant) || (node instanceof Output)).withChecker(
                FunctionGraph.checker()),
            graphArcRemovalRate
        ),
        new Tournament(nTournament),
        new Last(),
        nPop,
        true,
        false,
        (srp, r) -> new POSetPopulationState<>()
    ));
    solvers.put("fgraph-lim-speciated-noxover-kmeans", p -> new SpeciatedEvolver<>(
        FunctionGraph.builder()
            .andThen(MathUtils.fromMultivariateBuilder())
            .andThen(MathUtils.linearScaler(p.qualityFunction())),
        new ShallowSparseFactory(0d, 0d, 1d, p.qualityFunction().arity(), 1),
        nPop,
        StopConditions.nOfIterations(nIterations),
        Map.of(
            new NodeAddition<Node, Double>(
                FunctionNode.limitedIndexFactory(maxNodes, baseFunctions),
                (w, r) -> w,
                (w, r) -> r.nextGaussian()
            ).withChecker(FunctionGraph.checker()),
            graphNodeAdditionRate,
            new ArcModification<Node, Double>((w, r) -> w + r.nextGaussian(), 1d).withChecker(FunctionGraph.checker()),
            graphArcMutationRate,
            new ArcAddition<Node, Double>(RandomGenerator::nextGaussian, false).withChecker(FunctionGraph.checker()),
            graphArcAdditionRate,
            new ArcRemoval<Node, Double>(node -> (node instanceof Input) || (node instanceof it.units.malelab.jgea.representation.graph.numeric.Constant) || (node instanceof Output)).withChecker(
                FunctionGraph.checker()),
            graphArcRemovalRate
        ),
        false,
        5,
        new KMeansSpeciator<>(
            5,
            300,
            (x, y) -> (new Jaccard()).on(a -> new HashSet<>(Collections.singletonList(a))).apply(x, y),
            i -> i.genotype().nodes().stream().mapToDouble(Node::getIndex).toArray()
        ),
        0.75
    ));
    solvers.put("fgraph-lim-speciated-noxover", p -> new SpeciatedEvolver<>(
        FunctionGraph.builder()
            .andThen(MathUtils.fromMultivariateBuilder())
            .andThen(MathUtils.linearScaler(p.qualityFunction())),
        new ShallowSparseFactory(0d, 0d, 1d, p.qualityFunction().arity(), 1),
        nPop,
        StopConditions.nOfIterations(nIterations),
        Map.of(
            new NodeAddition<Node, Double>(
                FunctionNode.limitedIndexFactory(maxNodes, baseFunctions),
                (w, r) -> w,
                (w, r) -> r.nextGaussian()
            ).withChecker(FunctionGraph.checker()),
            graphNodeAdditionRate,
            new ArcModification<Node, Double>((w, r) -> w + r.nextGaussian(), 1d).withChecker(FunctionGraph.checker()),
            graphArcMutationRate,
            new ArcAddition<Node, Double>(RandomGenerator::nextGaussian, false).withChecker(FunctionGraph.checker()),
            graphArcAdditionRate,
            new ArcRemoval<Node, Double>(node -> (node instanceof Input) || (node instanceof it.units.malelab.jgea.representation.graph.numeric.Constant) || (node instanceof Output)).withChecker(
                FunctionGraph.checker()),
            graphArcRemovalRate
        ),
        false,
        5,
        new LazySpeciator<>((new Jaccard()).on(i -> i.genotype().nodes()), 0.25),
        0.75
    ));
    solvers.put("fgraph-seq-speciated-noxover", p -> new SpeciatedEvolver<>(
        FunctionGraph.builder()
            .andThen(MathUtils.fromMultivariateBuilder())
            .andThen(MathUtils.linearScaler(p.qualityFunction())),
        new ShallowSparseFactory(0d, 0d, 1d, p.qualityFunction().arity(), 1),
        nPop,
        StopConditions.nOfIterations(nIterations),
        Map.of(
            new NodeAddition<Node, Double>(
                FunctionNode.sequentialIndexFactory(baseFunctions),
                (w, r) -> w,
                (w, r) -> r.nextGaussian()
            ).withChecker(FunctionGraph.checker()),
            graphNodeAdditionRate,
            new ArcModification<Node, Double>((w, r) -> w + r.nextGaussian(), 1d).withChecker(FunctionGraph.checker()),
            graphArcMutationRate,
            new ArcAddition<Node, Double>(RandomGenerator::nextGaussian, false).withChecker(FunctionGraph.checker()),
            graphArcAdditionRate,
            new ArcRemoval<Node, Double>(node -> (node instanceof Input) || (node instanceof it.units.malelab.jgea.representation.graph.numeric.Constant) || (node instanceof Output)).withChecker(
                FunctionGraph.checker()),
            graphArcRemovalRate
        ),
        false,
        5,
        new LazySpeciator<>((new Jaccard()).on(i -> i.genotype().nodes()), 0.25),
        0.75
    ));
    solvers.put("fgraph-lim-gadiv", p -> new StandardWithEnforcedDiversityEvolver<>(
        FunctionGraph.builder()
            .andThen(MathUtils.fromMultivariateBuilder())
            .andThen(MathUtils.linearScaler(p.qualityFunction())),
        new ShallowSparseFactory(0d, 0d, 1d, p.qualityFunction().arity(), 1),
        nPop,
        StopConditions.nOfIterations(nIterations),
        Map.of(
            new NodeAddition<Node, Double>(
                FunctionNode.limitedIndexFactory(maxNodes, baseFunctions),
                (w, r) -> w,
                (w, r) -> r.nextGaussian()
            ).withChecker(FunctionGraph.checker()),
            graphNodeAdditionRate,
            new ArcModification<Node, Double>((w, r) -> w + r.nextGaussian(), 1d).withChecker(FunctionGraph.checker()),
            graphArcMutationRate,
            new ArcAddition<Node, Double>(RandomGenerator::nextGaussian, false).withChecker(FunctionGraph.checker()),
            graphArcAdditionRate,
            new ArcRemoval<>(node -> (node instanceof Input) || (node instanceof it.units.malelab.jgea.representation.graph.numeric.Constant) || (node instanceof Output)),
            graphArcRemovalRate,
            new AlignedCrossover<Node, Double>(
                (w1, w2, r) -> w1 + (w2 - w1) * (r.nextDouble() * 3d - 1d),
                node -> (node instanceof Input) || (node instanceof it.units.malelab.jgea.representation.graph.numeric.Constant) || (node instanceof Output),
                false
            ).withChecker(FunctionGraph.checker()),
            graphCrossoverRate
        ),
        new Tournament(nTournament),
        new Last(),
        nPop,
        true,
        false,
        (srp, r) -> new POSetPopulationState<>(),
        diversityMaxAttempts
    ));
    solvers.put("fgraph-lim-speciated", p -> new SpeciatedEvolver<>(
        FunctionGraph.builder()
            .andThen(MathUtils.fromMultivariateBuilder())
            .andThen(MathUtils.linearScaler(p.qualityFunction())),
        new ShallowSparseFactory(0d, 0d, 1d, p.qualityFunction().arity(), 1),
        nPop,
        StopConditions.nOfIterations(nIterations),
        Map.of(
            new NodeAddition<Node, Double>(
                FunctionNode.limitedIndexFactory(maxNodes, baseFunctions),
                (w, r) -> w,
                (w, r) -> r.nextGaussian()
            ).withChecker(FunctionGraph.checker()),
            graphNodeAdditionRate,
            new ArcModification<Node, Double>((w, r) -> w + r.nextGaussian(), 1d).withChecker(FunctionGraph.checker()),
            graphArcMutationRate,
            new ArcAddition<Node, Double>(RandomGenerator::nextGaussian, false).withChecker(FunctionGraph.checker()),
            graphArcAdditionRate,
            new ArcRemoval<>(node -> (node instanceof Input) || (node instanceof it.units.malelab.jgea.representation.graph.numeric.Constant) || (node instanceof Output)),
            graphArcRemovalRate,
            new AlignedCrossover<Node, Double>(
                (w1, w2, r) -> w1 + (w2 - w1) * (r.nextDouble() * 3d - 1d),
                node -> (node instanceof Input) || (node instanceof it.units.malelab.jgea.representation.graph.numeric.Constant) || (node instanceof Output),
                false
            ).withChecker(FunctionGraph.checker()),
            graphCrossoverRate
        ),
        false,
        5,
        new LazySpeciator<>((new Jaccard()).on(i -> i.genotype().nodes()), 0.25),
        0.75
    ));
    solvers.put("fgraph-seq-speciated", p -> new SpeciatedEvolver<>(
        FunctionGraph.builder()
            .andThen(MathUtils.fromMultivariateBuilder())
            .andThen(MathUtils.linearScaler(p.qualityFunction())),
        new ShallowSparseFactory(0d, 0d, 1d, p.qualityFunction().arity(), 1),
        nPop,
        StopConditions.nOfIterations(nIterations),
        Map.of(
            new NodeAddition<Node, Double>(
                FunctionNode.sequentialIndexFactory(baseFunctions),
                (w, r) -> w,
                (w, r) -> r.nextGaussian()
            ).withChecker(FunctionGraph.checker()),
            graphNodeAdditionRate,
            new ArcModification<Node, Double>((w, r) -> w + r.nextGaussian(), 1d).withChecker(FunctionGraph.checker()),
            graphArcMutationRate,
            new ArcAddition<Node, Double>(RandomGenerator::nextGaussian, false).withChecker(FunctionGraph.checker()),
            graphArcAdditionRate,
            new ArcRemoval<Node, Double>(node -> (node instanceof Input) || (node instanceof it.units.malelab.jgea.representation.graph.numeric.Constant) || (node instanceof Output)).withChecker(
                FunctionGraph.checker()),
            graphArcRemovalRate,
            new AlignedCrossover<Node, Double>(
                (w1, w2, r) -> w1 + (w2 - w1) * (r.nextDouble() * 3d - 1d),
                node -> (node instanceof Input) || (node instanceof it.units.malelab.jgea.representation.graph.numeric.Constant) || (node instanceof Output),
                false
            ).withChecker(FunctionGraph.checker()),
            graphCrossoverRate
        ),
        false,
        5,
        new LazySpeciator<>((new Jaccard()).on(i -> i.genotype().nodes()), 0.25),
        0.75
    ));
    solvers.put("fgraph-seq-ga", p -> new StandardEvolver<>(
        FunctionGraph.builder()
            .andThen(MathUtils.fromMultivariateBuilder())
            .andThen(MathUtils.linearScaler(p.qualityFunction())),
        new ShallowSparseFactory(0d, 0d, 1d, p.qualityFunction().arity(), 1),
        nPop,
        StopConditions.nOfIterations(nIterations),
        Map.of(
            new NodeAddition<Node, Double>(
                FunctionNode.sequentialIndexFactory(baseFunctions),
                (w, r) -> w,
                (w, r) -> r.nextGaussian()
            ).withChecker(FunctionGraph.checker()),
            graphNodeAdditionRate,
            new ArcModification<Node, Double>((w, r) -> w + r.nextGaussian(), 1d).withChecker(FunctionGraph.checker()),
            graphArcMutationRate,
            new ArcAddition<Node, Double>(RandomGenerator::nextGaussian, false).withChecker(FunctionGraph.checker()),
            graphArcAdditionRate,
            new ArcRemoval<Node, Double>(node -> (node instanceof Input) || (node instanceof it.units.malelab.jgea.representation.graph.numeric.Constant) || (node instanceof Output)).withChecker(
                FunctionGraph.checker()),
            graphArcRemovalRate,
            new AlignedCrossover<Node, Double>(
                (w1, w2, r) -> w1 + (w2 - w1) * (r.nextDouble() * 3d - 1d),
                node -> (node instanceof Input) || (node instanceof it.units.malelab.jgea.representation.graph.numeric.Constant) || (node instanceof Output),
                false
            ).withChecker(FunctionGraph.checker()),
            graphCrossoverRate
        ),
        new Tournament(nTournament),
        new Last(),
        nPop,
        true,
        false,
        (srp, r) -> new POSetPopulationState<>()
    ));
    solvers.put("ograph-seq-ga", p -> new StandardEvolver<>(
        OperatorGraph.builder()
            .andThen(MathUtils.fromMultivariateBuilder())
            .andThen(MathUtils.linearScaler(p.qualityFunction())),
        new ShallowFactory(p.qualityFunction().arity(), 1, constants),
        nPop,
        StopConditions.nOfIterations(nIterations),
        Map.of(
            new NodeAddition<Node, OperatorGraph.NonValuedArc>(
                OperatorNode.sequentialIndexFactory(baseOperators),
                Mutation.copy(),
                Mutation.copy()
            ).withChecker(OperatorGraph.checker()),
            graphNodeAdditionRate,
            new ArcAddition<Node, OperatorGraph.NonValuedArc>(r -> OperatorGraph.NON_VALUED_ARC, false).withChecker(
                OperatorGraph.checker()),
            graphArcAdditionRate,
            new ArcRemoval<Node, OperatorGraph.NonValuedArc>(node -> (node instanceof Input) || (node instanceof it.units.malelab.jgea.representation.graph.numeric.Constant) || (node instanceof Output)).withChecker(
                OperatorGraph.checker()),
            graphArcRemovalRate,
            new AlignedCrossover<Node, OperatorGraph.NonValuedArc>(
                Crossover.randomCopy(),
                node -> (node instanceof Input) || (node instanceof it.units.malelab.jgea.representation.graph.numeric.Constant) || (node instanceof Output),
                false
            ).withChecker(OperatorGraph.checker()),
            graphCrossoverRate
        ),
        new Tournament(nTournament),
        new Last(),
        nPop,
        true,
        false,
        (srp, r) -> new POSetPopulationState<>()
    ));
    solvers.put("ograph-seq-speciated-noxover", p -> new SpeciatedEvolver<>(
        OperatorGraph.builder()
            .andThen(MathUtils.fromMultivariateBuilder())
            .andThen(MathUtils.linearScaler(p.qualityFunction())),
        new ShallowFactory(p.qualityFunction().arity(), 1, constants),
        nPop,
        StopConditions.nOfIterations(nIterations),
        Map.of(
            new NodeAddition<Node, OperatorGraph.NonValuedArc>(
                OperatorNode.sequentialIndexFactory(baseOperators),
                Mutation.copy(),
                Mutation.copy()
            ).withChecker(OperatorGraph.checker()),
            graphNodeAdditionRate,
            new ArcAddition<Node, OperatorGraph.NonValuedArc>(r -> OperatorGraph.NON_VALUED_ARC, false).withChecker(
                OperatorGraph.checker()),
            graphArcAdditionRate,
            new ArcRemoval<Node, OperatorGraph.NonValuedArc>(node -> (node instanceof Input) || (node instanceof it.units.malelab.jgea.representation.graph.numeric.Constant) || (node instanceof Output)).withChecker(
                OperatorGraph.checker()),
            graphArcRemovalRate
        ),
        false,
        5,
        new LazySpeciator<>((new Jaccard()).on(i -> i.genotype().nodes()), 0.25),
        0.75
    ));
    solvers.put("fgraph-hash-ga", p -> {
      Function<Graph<IndexedNode<Node>, Double>, Graph<Node, Double>> graphMapper =
          GraphUtils.mapper(
              IndexedNode::content,
              Misc::first
          );
      Predicate<Graph<Node, Double>> checker = FunctionGraph.checker();
      return new StandardEvolver<>(
          graphMapper.andThen(FunctionGraph.builder())
              .andThen(MathUtils.fromMultivariateBuilder())
              .andThen(MathUtils.linearScaler(p.qualityFunction())),
          new ShallowSparseFactory(
              0d,
              0d,
              1d,
              p.qualityFunction().arity(),
              1
          ).then(GraphUtils.mapper(IndexedNode.incrementerMapper(Node.class), Misc::first)),
          nPop,
          StopConditions.nOfIterations(nIterations),
          Map.of(
              new NodeAddition<IndexedNode<Node>, Double>(
                  FunctionNode.sequentialIndexFactory(baseFunctions)
                      .then(IndexedNode.hashMapper(Node.class)),
                  (w, r) -> w,
                  (w, r) -> r.nextGaussian()
              ).withChecker(g -> checker.test(graphMapper.apply(g))),
              graphNodeAdditionRate,
              new ArcModification<IndexedNode<Node>, Double>(
                  (w, r) -> w + r.nextGaussian(),
                  1d
              ).withChecker(g -> checker.test(graphMapper.apply(g))),
              graphArcMutationRate,
              new ArcAddition<IndexedNode<Node>, Double>(
                  RandomGenerator::nextGaussian,
                  false
              ).withChecker(g -> checker.test(graphMapper.apply(g))),
              graphArcAdditionRate,
              new ArcRemoval<IndexedNode<Node>, Double>(node -> node.content() instanceof Output).withChecker(g -> checker.test(
                  graphMapper.apply(g))),
              graphArcRemovalRate,
              new AlignedCrossover<IndexedNode<Node>, Double>(
                  (w1, w2, r) -> w1 + (w2 - w1) * (r.nextDouble() * 3d - 1d),
                  node -> node.content() instanceof Output,
                  false
              ).withChecker(g -> checker.test(graphMapper.apply(g))),
              graphCrossoverRate
          ),
          new Tournament(nTournament),
          new Last(),
          nPop,
          true,
          false,
          (srp, r) -> new POSetPopulationState<>()
      );
    });
    solvers.put("fgraph-hash-speciated", p -> {
      Function<Graph<IndexedNode<Node>, Double>, Graph<Node, Double>> graphMapper =
          GraphUtils.mapper(
              IndexedNode::content,
              Misc::first
          );
      Predicate<Graph<Node, Double>> checker = FunctionGraph.checker();
      return new SpeciatedEvolver<>(
          graphMapper.andThen(FunctionGraph.builder())
              .andThen(MathUtils.fromMultivariateBuilder())
              .andThen(MathUtils.linearScaler(p.qualityFunction())),
          new ShallowSparseFactory(
              0d,
              0d,
              1d,
              p.qualityFunction().arity(),
              1
          ).then(GraphUtils.mapper(IndexedNode.incrementerMapper(Node.class), Misc::first)),
          nPop,
          StopConditions.nOfIterations(nIterations),
          Map.of(
              new NodeAddition<IndexedNode<Node>, Double>(
                  FunctionNode.sequentialIndexFactory(baseFunctions)
                      .then(IndexedNode.hashMapper(Node.class)),
                  (w, r) -> w,
                  (w, r) -> r.nextGaussian()
              ).withChecker(g -> checker.test(graphMapper.apply(g))),
              graphNodeAdditionRate,
              new ArcModification<IndexedNode<Node>, Double>(
                  (w, r) -> w + r.nextGaussian(),
                  1d
              ).withChecker(g -> checker.test(graphMapper.apply(g))),
              graphArcMutationRate,
              new ArcAddition<IndexedNode<Node>, Double>(
                  RandomGenerator::nextGaussian,
                  false
              ).withChecker(g -> checker.test(graphMapper.apply(g))),
              graphArcAdditionRate,
              new ArcRemoval<IndexedNode<Node>, Double>(node -> node.content() instanceof Output).withChecker(g -> checker.test(
                  graphMapper.apply(g))),
              graphArcRemovalRate,
              new AlignedCrossover<IndexedNode<Node>, Double>(
                  (w1, w2, r) -> w1 + (w2 - w1) * (r.nextDouble() * 3d - 1d),
                  node -> node.content() instanceof Output,
                  false
              ).withChecker(g -> checker.test(graphMapper.apply(g))),
              graphCrossoverRate
          ),
          false,
          5,
          new LazySpeciator<>((new Jaccard()).on(i -> i.genotype().nodes()), 0.25),
          0.75
      );
    });
    solvers.put("fgraph-hash+-speciated", p -> {
      Function<Graph<IndexedNode<Node>, Double>, Graph<Node, Double>> graphMapper =
          GraphUtils.mapper(
              IndexedNode::content,
              Misc::first
          );
      Predicate<Graph<Node, Double>> checker = FunctionGraph.checker();
      return new SpeciatedEvolver<>(
          GraphUtils.mapper(
                  (Function<IndexedNode<Node>, Node>) IndexedNode::content,
                  (Function<Collection<Double>, Double>) Misc::first
              )
              .andThen(FunctionGraph.builder())
              .andThen(MathUtils.fromMultivariateBuilder())
              .andThen(MathUtils.linearScaler(p.qualityFunction())),
          new ShallowSparseFactory(
              0d,
              0d,
              1d,
              p.qualityFunction().arity(),
              1
          ).then(GraphUtils.mapper(IndexedNode.incrementerMapper(Node.class), Misc::first)),
          nPop,
          StopConditions.nOfIterations(nIterations),
          Map.of(
              new IndexedNodeAddition<FunctionNode, Node, Double>(
                  FunctionNode.sequentialIndexFactory(baseFunctions),
                  n -> (n instanceof FunctionNode) ? ((FunctionNode) n).getFunction().hashCode() : 0,
                  p.qualityFunction().arity() + 1 + 1,
                  (w, r) -> w,
                  (w, r) -> r.nextGaussian()
              ).withChecker(g -> checker.test(graphMapper.apply(g))),
              graphNodeAdditionRate,
              new ArcModification<IndexedNode<Node>, Double>(
                  (w, r) -> w + r.nextGaussian(),
                  1d
              ).withChecker(g -> checker.test(graphMapper.apply(g))),
              graphArcMutationRate,
              new ArcAddition<IndexedNode<Node>, Double>(
                  RandomGenerator::nextGaussian,
                  false
              ).withChecker(g -> checker.test(graphMapper.apply(g))),
              graphArcAdditionRate,
              new ArcRemoval<IndexedNode<Node>, Double>(node -> node.content() instanceof Output).withChecker(g -> checker.test(
                  graphMapper.apply(g))),
              graphArcRemovalRate,
              new AlignedCrossover<IndexedNode<Node>, Double>(
                  (w1, w2, r) -> w1 + (w2 - w1) * (r.nextDouble() * 3d - 1d),
                  node -> node.content() instanceof Output,
                  false
              ).withChecker(g -> checker.test(graphMapper.apply(g))),
              graphCrossoverRate
          ),
          false,
          5,
          new LazySpeciator<>((new Jaccard()).on(i -> i.genotype().nodes()), 0.25),
          0.75
      );
    });
    solvers.put("ograph-hash+-speciated", p -> {
      Function<Graph<IndexedNode<Node>, OperatorGraph.NonValuedArc>, Graph<Node, OperatorGraph.NonValuedArc>> graphMapper = GraphUtils.mapper(
          IndexedNode::content,
          Misc::first
      );
      Predicate<Graph<Node, OperatorGraph.NonValuedArc>> checker = OperatorGraph.checker();
      return new SpeciatedEvolver<>(
          graphMapper.andThen(OperatorGraph.builder())
              .andThen(MathUtils.fromMultivariateBuilder())
              .andThen(MathUtils.linearScaler(p.qualityFunction())),
          new ShallowFactory(p.qualityFunction().arity(), 1, constants).then(GraphUtils.mapper(
              IndexedNode.incrementerMapper(Node.class),
              Misc::first
          )),
          nPop,
          StopConditions.nOfIterations(nIterations),
          Map.of(
              new IndexedNodeAddition<OperatorNode, Node, OperatorGraph.NonValuedArc>(
                  OperatorNode.sequentialIndexFactory(
                      baseOperators),
                  n -> (n instanceof OperatorNode) ? ((OperatorNode) n).getOperator().hashCode() : 0,
                  p.qualityFunction().arity() + 1 + constants.length,
                  Mutation.copy(),
                  Mutation.copy()
              ).withChecker(g -> checker.test(graphMapper.apply(g))),
              graphNodeAdditionRate,
              new ArcAddition<IndexedNode<Node>, OperatorGraph.NonValuedArc>(
                  r -> OperatorGraph.NON_VALUED_ARC,
                  false
              ).withChecker(g -> checker.test(graphMapper.apply(g))),
              graphArcAdditionRate,
              new ArcRemoval<IndexedNode<Node>, OperatorGraph.NonValuedArc>(node -> node.content() instanceof Output).withChecker(
                  g -> checker.test(graphMapper.apply(g))),
              graphArcRemovalRate,
              new AlignedCrossover<IndexedNode<Node>, OperatorGraph.NonValuedArc>(
                  Crossover.randomCopy(),
                  node -> node.content() instanceof Output,
                  false
              ).withChecker(g -> checker.test(graphMapper.apply(g))),
              graphCrossoverRate
          ),
          false,
          5,
          new LazySpeciator<>((new Jaccard()).on(i -> i.genotype().nodes()), 0.25),
          0.75
      );
    });
    solvers.put("fgraph-hash+-speciated-noxover", p -> {
      Function<Graph<IndexedNode<Node>, Double>, Graph<Node, Double>> graphMapper =
          GraphUtils.mapper(
              IndexedNode::content,
              Misc::first
          );
      Predicate<Graph<Node, Double>> checker = FunctionGraph.checker();
      return new SpeciatedEvolver<>(
          graphMapper.andThen(FunctionGraph.builder())
              .andThen(MathUtils.fromMultivariateBuilder())
              .andThen(MathUtils.linearScaler(p.qualityFunction())),
          new ShallowSparseFactory(0d, 0d, 1d, p.qualityFunction().arity(), 1).then(GraphUtils.mapper(
              IndexedNode.incrementerMapper(Node.class),
              Misc::first
          )),
          nPop,
          StopConditions.nOfIterations(nIterations),
          Map.of(
              new IndexedNodeAddition<FunctionNode, Node, Double>(
                  FunctionNode.sequentialIndexFactory(baseFunctions),
                  n -> (n instanceof FunctionNode) ? ((FunctionNode) n).getFunction().hashCode() : 0,
                  p.qualityFunction().arity() + 1 + 1,
                  (w, r) -> w,
                  (w, r) -> r.nextGaussian()
              ).withChecker(g -> checker.test(graphMapper.apply(g))),
              graphNodeAdditionRate,
              new ArcModification<IndexedNode<Node>, Double>(
                  (w, r) -> w + r.nextGaussian(),
                  1d
              ).withChecker(g -> checker.test(graphMapper.apply(g))),
              graphArcMutationRate,
              new ArcAddition<IndexedNode<Node>, Double>(
                  RandomGenerator::nextGaussian,
                  false
              ).withChecker(g -> checker.test(graphMapper.apply(g))),
              graphArcAdditionRate,
              new ArcRemoval<IndexedNode<Node>, Double>(node -> node.content() instanceof Output).withChecker(g -> checker.test(
                  graphMapper.apply(g))),
              graphArcRemovalRate
          ),
          false,
          5,
          new LazySpeciator<>((new Jaccard()).on(i -> i.genotype().nodes()), 0.25),
          0.75
      );
    });
    solvers.put("fgraph-seq-ga-noxover", p -> new StandardEvolver<>(
        FunctionGraph.builder()
            .andThen(MathUtils.fromMultivariateBuilder())
            .andThen(MathUtils.linearScaler(p.qualityFunction())),
        new ShallowSparseFactory(0d, 0d, 1d, p.qualityFunction().arity(), 1),
        nPop,
        StopConditions.nOfIterations(nIterations),
        Map.of(
            new NodeAddition<Node, Double>(
                FunctionNode.sequentialIndexFactory(baseFunctions),
                (w, r) -> w,
                (w, r) -> r.nextGaussian()
            ).withChecker(FunctionGraph.checker()),
            graphNodeAdditionRate,
            new ArcModification<Node, Double>((w, r) -> w + r.nextGaussian(), 1d).withChecker(FunctionGraph.checker()),
            graphArcMutationRate,
            new ArcAddition<Node, Double>(RandomGenerator::nextGaussian, false).withChecker(FunctionGraph.checker()),
            graphArcAdditionRate,
            new ArcRemoval<Node, Double>(node -> (node instanceof Input) || (node instanceof it.units.malelab.jgea.representation.graph.numeric.Constant) || (node instanceof Output)).withChecker(
                FunctionGraph.checker()),
            graphArcRemovalRate
        ),
        new Tournament(nTournament),
        new Last(),
        nPop,
        true,
        false,
        (srp, r) -> new POSetPopulationState<>()
    ));
    solvers.put("fgraph-seq-gadiv", p -> new StandardWithEnforcedDiversityEvolver<>(
        FunctionGraph.builder()
            .andThen(MathUtils.fromMultivariateBuilder())
            .andThen(MathUtils.linearScaler(p.qualityFunction())),
        new ShallowSparseFactory(0d, 0d, 1d, p.qualityFunction().arity(), 1),
        nPop,
        StopConditions.nOfIterations(nIterations),
        Map.of(
            new NodeAddition<Node, Double>(
                FunctionNode.sequentialIndexFactory(baseFunctions),
                (w, r) -> w,
                (w, r) -> r.nextGaussian()
            ).withChecker(FunctionGraph.checker()),
            graphNodeAdditionRate,
            new ArcModification<Node, Double>((w, r) -> w + r.nextGaussian(), 1d).withChecker(FunctionGraph.checker()),
            graphArcMutationRate,
            new ArcAddition<Node, Double>(RandomGenerator::nextGaussian, false).withChecker(FunctionGraph.checker()),
            graphArcAdditionRate,
            new ArcRemoval<Node, Double>(node -> (node instanceof Input) || (node instanceof it.units.malelab.jgea.representation.graph.numeric.Constant) || (node instanceof Output)).withChecker(
                FunctionGraph.checker()),
            graphArcRemovalRate,
            new AlignedCrossover<Node, Double>(
                (w1, w2, r) -> w1 + (w2 - w1) * (r.nextDouble() * 3d - 1d),
                node -> (node instanceof Input) || (node instanceof it.units.malelab.jgea.representation.graph.numeric.Constant) || (node instanceof Output),
                false
            ).withChecker(FunctionGraph.checker()),
            graphCrossoverRate
        ),
        new Tournament(nTournament),
        new Last(),
        nPop,
        true,
        false,
        (srp, r) -> new POSetPopulationState<>(),
        diversityMaxAttempts
    ));

    //filter evolvers
    solvers = solvers.entrySet().stream().filter(e -> e.getKey().matches(evolverNamePattern)).collect(Collectors.toMap(
        Map.Entry::getKey,
        Map.Entry::getValue
    ));
    L.info(String.format("Going to test with %d evolvers: %s%n", solvers.size(), solvers.keySet()));
    //run
    for (int seed : seeds) {
      for (SymbolicRegressionProblem problem : problems) {
        for (Map.Entry<String, Function<SymbolicRegressionProblem, IterativeSolver<? extends POSetPopulationState<?,
            RealFunction, Double>, SymbolicRegressionProblem, RealFunction>>> solverEntry : solvers.entrySet()) {
          Map<String, Object> keys = Map.ofEntries(
              Map.entry("seed", seed),
              Map.entry("problem", problem.getClass().getSimpleName().toLowerCase()),
              Map.entry("evolver", solverEntry.getKey())
          );
          try {
            Stopwatch stopwatch = Stopwatch.createStarted();
            IterativeSolver<? extends POSetPopulationState<?, RealFunction, Double>, SymbolicRegressionProblem,
                RealFunction> solver = solverEntry.getValue()
                .apply(problem);
            L.info(String.format("Starting %s", keys));
            Collection<RealFunction> solutions = solver.solve(
                problem,
                new Random(seed),
                executorService,
                listenerFactory.build(keys).deferred(executorService)
            );
            L.info(String.format(
                "Done %s: %d solutions in %4.1fs",
                keys,
                solutions.size(),
                (double) stopwatch.elapsed(TimeUnit.MILLISECONDS) / 1000d
            ));
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
