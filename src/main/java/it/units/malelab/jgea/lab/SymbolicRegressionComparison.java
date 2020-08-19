/*
 * Copyright (C) 2020 Eric Medvet <eric.medvet@gmail.com> (as eric)
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.units.malelab.jgea.lab;

import com.google.common.base.Stopwatch;
import com.google.common.graph.ValueGraph;
import it.units.malelab.jgea.Worker;
import it.units.malelab.jgea.core.IndependentFactory;
import it.units.malelab.jgea.core.Individual;
import it.units.malelab.jgea.core.evolver.Evolver;
import it.units.malelab.jgea.core.evolver.SpeciatedEvolver;
import it.units.malelab.jgea.core.evolver.StandardEvolver;
import it.units.malelab.jgea.core.evolver.StandardWithEnforcedDiversityEvolver;
import it.units.malelab.jgea.core.evolver.stopcondition.Iterations;
import it.units.malelab.jgea.core.listener.Listener;
import it.units.malelab.jgea.core.listener.MultiFileListenerFactory;
import it.units.malelab.jgea.core.listener.collector.*;
import it.units.malelab.jgea.core.operator.Crossover;
import it.units.malelab.jgea.core.operator.Mutation;
import it.units.malelab.jgea.core.order.PartialComparator;
import it.units.malelab.jgea.core.selector.Tournament;
import it.units.malelab.jgea.core.selector.Worst;
import it.units.malelab.jgea.core.util.Misc;
import it.units.malelab.jgea.distance.Jaccard;
import it.units.malelab.jgea.problem.symbolicregression.*;
import it.units.malelab.jgea.problem.symbolicregression.element.Constant;
import it.units.malelab.jgea.problem.symbolicregression.element.Element;
import it.units.malelab.jgea.problem.symbolicregression.element.Operator;
import it.units.malelab.jgea.problem.symbolicregression.element.Variable;
import it.units.malelab.jgea.representation.graph.IndexedNodeAddition;
import it.units.malelab.jgea.representation.grammar.cfggp.GrammarBasedSubtreeMutation;
import it.units.malelab.jgea.representation.grammar.cfggp.GrammarRampedHalfAndHalf;
import it.units.malelab.jgea.representation.graph.*;
import it.units.malelab.jgea.representation.graph.numeric.functiongraph.*;
import it.units.malelab.jgea.representation.graph.numeric.Node;
import it.units.malelab.jgea.representation.graph.numeric.Output;
import it.units.malelab.jgea.representation.graph.numeric.operatorgraph.BaseOperator;
import it.units.malelab.jgea.representation.graph.numeric.operatorgraph.OperatorGraph;
import it.units.malelab.jgea.representation.graph.numeric.operatorgraph.OperatorNode;
import it.units.malelab.jgea.representation.graph.numeric.operatorgraph.ShallowFactory;
import it.units.malelab.jgea.representation.tree.*;

import java.io.FileNotFoundException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static it.units.malelab.jgea.core.util.Args.i;
import static it.units.malelab.jgea.core.util.Args.ri;

/**
 * @author eric
 * @created 2020/08/05
 * @project jgea
 */

// /usr/lib/jvm/jdk-14.0.1/bin/java -cp ~/IdeaProjects/jgea/out/artifacts/jgea_jar/jgea.jar it.units.malelab.jgea.lab.SymbolicRegressionComparison seed=0:10 file=results-%s.txt
public class SymbolicRegressionComparison extends Worker {

  public SymbolicRegressionComparison(String[] args) throws FileNotFoundException {
    super(args);
  }

  public static void main(String[] args) throws FileNotFoundException {
    new SymbolicRegressionComparison(args);
  }

  @Override
  public void run() {
    int nPop = i(a("nPop", "100"));
    int maxHeight = i(a("maxHeight", "10"));
    int maxNodes = i(a("maxNodes", "20"));
    int nTournament = 5;
    int diversityMaxAttempts = 100;
    int nIterations = i(a("nIterations", "50"));
    String evolverNamePattern = a("evolver", "ograph-.*");
    int[] seeds = ri(a("seed", "0:1"));
    double graphEdgeAdditionRate = 3d;
    double graphEdgeMutationRate = 1d;
    double graphEdgeRemovalRate = 0d;
    double graphNodeAdditionRate = 1d;
    double graphCrossoverRate = 1d;
    SymbolicRegressionFitness.Metric metric = SymbolicRegressionFitness.Metric.MSE;
    Operator[] operators = new Operator[]{Operator.ADDITION, Operator.SUBTRACTION, Operator.MULTIPLICATION, Operator.PROT_DIVISION, Operator.PROT_LOG};
    BaseOperator[] baseOperators = new BaseOperator[]{BaseOperator.ADDITION, BaseOperator.SUBTRACTION, BaseOperator.MULTIPLICATION, BaseOperator.PROT_DIVISION, BaseOperator.PROT_LOG};
    BaseFunction[] baseFunctions = new BaseFunction[]{BaseFunction.RE_LU, BaseFunction.GAUSSIAN, BaseFunction.PROT_INVERSE, BaseFunction.SQ};
    double[] constants = new double[]{0.1, 1d, 10d};
    List<SymbolicRegressionProblem> problems = List.of(
        new Nguyen7(metric, 1),
        new Keijzer6(metric),
        new Polynomial4(metric),
        new Pagie1(metric)
    );
    MultiFileListenerFactory<Object, RealFunction, Double> listenerFactory = new MultiFileListenerFactory<>(
        a("dir", "."),
        a("file", null)
    );
    Map<String, Function<SymbolicRegressionProblem, Evolver<?, RealFunction, Double>>> evolvers = new TreeMap<>(Map.ofEntries(
        Map.entry("tree-ga", p -> {
          IndependentFactory<Element> terminalFactory = IndependentFactory.oneOf(
              IndependentFactory.picker(Arrays.stream(vars(p.arity())).sequential().map(Variable::new).toArray(Variable[]::new)),
              IndependentFactory.picker(Arrays.stream(constants).mapToObj(Constant::new).toArray(Constant[]::new))
          );
          return new StandardEvolver<Tree<Element>, RealFunction, Double>(
              ((Function<Tree<Element>, RealFunction>) t -> new TreeBasedRealFunction(t, vars(p.arity())))
                  .andThen(MathUtils.linearScaler((SymbolicRegressionFitness) p.getFitnessFunction())),
              new RampedHalfAndHalf<>(
                  4, maxHeight,
                  Operator.arityFunction(),
                  IndependentFactory.picker(operators),
                  terminalFactory
              ),
              PartialComparator.from(Double.class).on(Individual::getFitness),
              nPop,
              Map.of(
                  new SubtreeCrossover<>(maxHeight), 0.8d,
                  new SubtreeMutation<>(maxHeight, new GrowTreeBuilder<>(
                      Operator.arityFunction(),
                      IndependentFactory.picker(operators),
                      terminalFactory
                  )), 0.2d
              ),
              new Tournament(nTournament),
              new Worst(),
              nPop,
              true
          );
        }),
        Map.entry("tree-ga-noxover", p -> {
          IndependentFactory<Element> terminalFactory = IndependentFactory.oneOf(
              IndependentFactory.picker(Arrays.stream(vars(p.arity())).sequential().map(Variable::new).toArray(Variable[]::new)),
              IndependentFactory.picker(Arrays.stream(constants).mapToObj(Constant::new).toArray(Constant[]::new))
          );
          return new StandardEvolver<Tree<Element>, RealFunction, Double>(
              ((Function<Tree<Element>, RealFunction>) t -> new TreeBasedRealFunction(t, vars(p.arity())))
                  .andThen(MathUtils.linearScaler((SymbolicRegressionFitness) p.getFitnessFunction())),
              new RampedHalfAndHalf<>(
                  4, maxHeight,
                  Operator.arityFunction(),
                  IndependentFactory.picker(operators),
                  terminalFactory
              ),
              PartialComparator.from(Double.class).on(Individual::getFitness),
              nPop,
              Map.of(
                  new SubtreeMutation<>(maxHeight, new GrowTreeBuilder<>(
                      Operator.arityFunction(),
                      IndependentFactory.picker(operators),
                      terminalFactory
                  )), 0.2d
              ),
              new Tournament(nTournament),
              new Worst(),
              nPop,
              true
          );
        }),
        Map.entry("tree-gadiv", p -> {
          IndependentFactory<Element> terminalFactory = IndependentFactory.oneOf(
              IndependentFactory.picker(Arrays.stream(vars(p.arity())).sequential().map(Variable::new).toArray(Variable[]::new)),
              IndependentFactory.picker(Arrays.stream(constants).mapToObj(Constant::new).toArray(Constant[]::new))
          );
          return new StandardWithEnforcedDiversityEvolver<Tree<Element>, RealFunction, Double>(
              ((Function<Tree<Element>, RealFunction>) t -> new TreeBasedRealFunction(t, vars(p.arity())))
                  .andThen(MathUtils.linearScaler((SymbolicRegressionFitness) p.getFitnessFunction())),
              new RampedHalfAndHalf<>(
                  4, maxHeight,
                  Operator.arityFunction(),
                  IndependentFactory.picker(operators),
                  terminalFactory
              ),
              PartialComparator.from(Double.class).on(Individual::getFitness),
              nPop,
              Map.of(
                  new SubtreeCrossover<>(maxHeight), 0.8d,
                  new SubtreeMutation<>(maxHeight, new GrowTreeBuilder<>(
                      Operator.arityFunction(),
                      IndependentFactory.picker(operators),
                      terminalFactory
                  )), 0.2d
              ),
              new Tournament(nTournament),
              new Worst(),
              nPop,
              true,
              diversityMaxAttempts
          );
        }),
        Map.entry("cfgtree-ga", p -> {
          SymbolicRegressionGrammar g = new SymbolicRegressionGrammar(
              List.of(operators),
              List.of(vars(p.arity())),
              Arrays.stream(constants).mapToObj(d -> (Double) d).collect(Collectors.toList())
          );
          return new StandardEvolver<Tree<String>, RealFunction, Double>(
              new FormulaMapper()
                  .andThen(n -> TreeBasedRealFunction.from(n, vars(p.arity())))
                  .andThen(MathUtils.linearScaler((SymbolicRegressionFitness) p.getFitnessFunction())),
              new GrammarRampedHalfAndHalf<>(6, maxHeight + 4, g),
              PartialComparator.from(Double.class).on(Individual::getFitness),
              nPop,
              Map.of(
                  new SameRootSubtreeCrossover<>(maxHeight + 4), 0.8d,
                  new GrammarBasedSubtreeMutation<>(maxHeight + 4, g), 0.2d
              ),
              new Tournament(nTournament),
              new Worst(),
              nPop,
              true
          );
        }),
        Map.entry("cfgtree-ga-noxover", p -> {
          SymbolicRegressionGrammar g = new SymbolicRegressionGrammar(
              List.of(operators),
              List.of(vars(p.arity())),
              Arrays.stream(constants).mapToObj(d -> (Double) d).collect(Collectors.toList())
          );
          return new StandardEvolver<Tree<String>, RealFunction, Double>(
              new FormulaMapper()
                  .andThen(n -> TreeBasedRealFunction.from(n, vars(p.arity())))
                  .andThen(MathUtils.linearScaler((SymbolicRegressionFitness) p.getFitnessFunction())),
              new GrammarRampedHalfAndHalf<>(6, maxHeight + 4, g),
              PartialComparator.from(Double.class).on(Individual::getFitness),
              nPop,
              Map.of(
                  new GrammarBasedSubtreeMutation<>(maxHeight + 4, g), 0.2d
              ),
              new Tournament(nTournament),
              new Worst(),
              nPop,
              true
          );
        }),
        Map.entry("cfgtree-gadiv", p -> {
          SymbolicRegressionGrammar g = new SymbolicRegressionGrammar(
              List.of(operators),
              List.of(vars(p.arity())),
              Arrays.stream(constants).mapToObj(d -> (Double) d).collect(Collectors.toList())
          );
          return new StandardWithEnforcedDiversityEvolver<Tree<String>, RealFunction, Double>(
              new FormulaMapper()
                  .andThen(n -> TreeBasedRealFunction.from(n, vars(p.arity())))
                  .andThen(MathUtils.linearScaler((SymbolicRegressionFitness) p.getFitnessFunction())),
              new GrammarRampedHalfAndHalf<>(6, maxHeight + 4, g),
              PartialComparator.from(Double.class).on(Individual::getFitness),
              nPop,
              Map.of(
                  new SameRootSubtreeCrossover<>(maxHeight + 4), 0.8d,
                  new GrammarBasedSubtreeMutation<>(maxHeight + 4, g), 0.2d
              ),
              new Tournament(nTournament),
              new Worst(),
              nPop,
              true,
              diversityMaxAttempts
          );
        }),
        Map.entry("fgraph-lim-ga", p -> new StandardEvolver<ValueGraph<Node, Double>, RealFunction, Double>(
            FunctionGraph.builder()
                .andThen(MathUtils.fromMultivariateBuilder())
                .andThen(MathUtils.linearScaler((SymbolicRegressionFitness) p.getFitnessFunction())),
            new ShallowSparseFactory(0d, 0d, 1d, p.arity(), 1),
            PartialComparator.from(Double.class).on(Individual::getFitness),
            nPop,
            Map.of(
                new NodeAddition<>(
                    FunctionNode.limitedIndexFactory(maxNodes, baseFunctions),
                    (w, r) -> w,
                    (w, r) -> r.nextGaussian()
                ), graphNodeAdditionRate,
                new EdgeModification<>((w, r) -> w + r.nextGaussian(), 1d), graphEdgeMutationRate,
                new EdgeAddition<>(Random::nextGaussian, false), graphEdgeAdditionRate,
                new EdgeRemoval<>(node -> node instanceof Output), graphEdgeRemovalRate,
                new AlignedCrossover<>(
                    (w1, w2, r) -> w1 + (w2 - w1) * (r.nextDouble() * 3d - 1d),
                    node -> node instanceof Output,
                    false
                ), graphCrossoverRate
            ),
            new Tournament(nTournament),
            new Worst(),
            nPop,
            true
        )),
        Map.entry("fgraph-lim-ga-noxover", p -> new StandardEvolver<ValueGraph<Node, Double>, RealFunction, Double>(
            FunctionGraph.builder()
                .andThen(MathUtils.fromMultivariateBuilder())
                .andThen(MathUtils.linearScaler((SymbolicRegressionFitness) p.getFitnessFunction())),
            new ShallowSparseFactory(0d, 0d, 1d, p.arity(), 1),
            PartialComparator.from(Double.class).on(Individual::getFitness),
            nPop,
            Map.of(
                new NodeAddition<>(
                    FunctionNode.limitedIndexFactory(maxNodes, baseFunctions),
                    (w, r) -> w,
                    (w, r) -> r.nextGaussian()
                ), graphNodeAdditionRate,
                new EdgeModification<>((w, r) -> w + r.nextGaussian(), 1d), graphEdgeMutationRate,
                new EdgeAddition<>(Random::nextGaussian, false), graphEdgeAdditionRate,
                new EdgeRemoval<>(node -> node instanceof Output), graphEdgeRemovalRate
            ),
            new Tournament(nTournament),
            new Worst(),
            nPop,
            true
        )),
        Map.entry("fgraph-lim-gadiv", p -> new StandardWithEnforcedDiversityEvolver<ValueGraph<Node, Double>, RealFunction, Double>(
            FunctionGraph.builder()
                .andThen(MathUtils.fromMultivariateBuilder())
                .andThen(MathUtils.linearScaler((SymbolicRegressionFitness) p.getFitnessFunction())),
            new ShallowSparseFactory(0d, 0d, 1d, p.arity(), 1),
            PartialComparator.from(Double.class).on(Individual::getFitness),
            nPop,
            Map.of(
                new NodeAddition<>(
                    FunctionNode.limitedIndexFactory(maxNodes, baseFunctions),
                    (w, r) -> w,
                    (w, r) -> r.nextGaussian()
                ), graphNodeAdditionRate,
                new EdgeModification<>((w, r) -> w + r.nextGaussian(), 1d), graphEdgeMutationRate,
                new EdgeAddition<>(Random::nextGaussian, false), graphEdgeAdditionRate,
                new EdgeRemoval<>(node -> node instanceof Output), graphEdgeRemovalRate,
                new AlignedCrossover<>(
                    (w1, w2, r) -> w1 + (w2 - w1) * (r.nextDouble() * 3d - 1d),
                    node -> node instanceof Output,
                    false
                ), graphCrossoverRate
            ),
            new Tournament(nTournament),
            new Worst(),
            nPop,
            true,
            diversityMaxAttempts
        )),
        Map.entry("fgraph-lim-speciated", p -> new SpeciatedEvolver<ValueGraph<Node, Double>, RealFunction, Double>(
            FunctionGraph.builder()
                .andThen(MathUtils.fromMultivariateBuilder())
                .andThen(MathUtils.linearScaler((SymbolicRegressionFitness) p.getFitnessFunction())),
            new ShallowSparseFactory(0d, 0d, 1d, p.arity(), 1),
            PartialComparator.from(Double.class).on(Individual::getFitness),
            nPop,
            Map.of(
                new NodeAddition<>(
                    FunctionNode.limitedIndexFactory(maxNodes, baseFunctions),
                    (w, r) -> w,
                    (w, r) -> r.nextGaussian()
                ), graphNodeAdditionRate,
                new EdgeModification<>((w, r) -> w + r.nextGaussian(), 1d), graphEdgeMutationRate,
                new EdgeAddition<>(Random::nextGaussian, false), graphEdgeAdditionRate,
                new EdgeRemoval<>(node -> node instanceof Output), graphEdgeRemovalRate,
                new AlignedCrossover<>(
                    (w1, w2, r) -> w1 + (w2 - w1) * (r.nextDouble() * 3d - 1d),
                    node -> node instanceof Output,
                    false
                ), graphCrossoverRate
            ),
            5,
            (new Jaccard()).on(i -> i.getGenotype().nodes()),
            0.25,
            individuals -> {
              double[] fitnesses = individuals.stream().mapToDouble(i -> i.getFitness()).toArray();
              Individual<ValueGraph<Node, Double>, RealFunction, Double> r = Misc.first(individuals);
              return new Individual<>(
                  r.getGenotype(),
                  r.getSolution(),
                  Misc.median(fitnesses),
                  r.getBirthIteration()
              );
            },
            0.75
        )),
        Map.entry("fgraph-seq-speciated", p -> new SpeciatedEvolver<ValueGraph<Node, Double>, RealFunction, Double>(
            FunctionGraph.builder()
                .andThen(MathUtils.fromMultivariateBuilder())
                .andThen(MathUtils.linearScaler((SymbolicRegressionFitness) p.getFitnessFunction())),
            new ShallowSparseFactory(0d, 0d, 1d, p.arity(), 1),
            PartialComparator.from(Double.class).on(Individual::getFitness),
            nPop,
            Map.of(
                new NodeAddition<>(
                    FunctionNode.sequentialIndexFactory(baseFunctions),
                    (w, r) -> w,
                    (w, r) -> r.nextGaussian()
                ), graphNodeAdditionRate,
                new EdgeModification<>((w, r) -> w + r.nextGaussian(), 1d), graphEdgeMutationRate,
                new EdgeAddition<>(Random::nextGaussian, false), graphEdgeAdditionRate,
                new EdgeRemoval<>(node -> node instanceof Output), graphEdgeRemovalRate,
                new AlignedCrossover<>(
                    (w1, w2, r) -> w1 + (w2 - w1) * (r.nextDouble() * 3d - 1d),
                    node -> node instanceof Output,
                    false
                ), graphCrossoverRate
            ),
            5,
            (new Jaccard()).on(i -> i.getGenotype().nodes()),
            0.25,
            individuals -> {
              double[] fitnesses = individuals.stream().mapToDouble(i -> i.getFitness()).toArray();
              Individual<ValueGraph<Node, Double>, RealFunction, Double> r = Misc.first(individuals);
              return new Individual<>(
                  r.getGenotype(),
                  r.getSolution(),
                  Misc.median(fitnesses),
                  r.getBirthIteration()
              );
            },
            0.75
        )),
        Map.entry("fgraph-seq-ga", p -> new StandardEvolver<ValueGraph<Node, Double>, RealFunction, Double>(
            FunctionGraph.builder()
                .andThen(MathUtils.fromMultivariateBuilder())
                .andThen(MathUtils.linearScaler((SymbolicRegressionFitness) p.getFitnessFunction())),
            new ShallowSparseFactory(0d, 0d, 1d, p.arity(), 1),
            PartialComparator.from(Double.class).on(Individual::getFitness),
            nPop,
            Map.of(
                new NodeAddition<>(
                    FunctionNode.sequentialIndexFactory(baseFunctions),
                    (w, r) -> w,
                    (w, r) -> r.nextGaussian()
                ), graphNodeAdditionRate,
                new EdgeModification<>((w, r) -> w + r.nextGaussian(), 1d), graphEdgeMutationRate,
                new EdgeAddition<>(Random::nextGaussian, false), graphEdgeAdditionRate,
                new EdgeRemoval<>(node -> node instanceof Output), graphEdgeRemovalRate,
                new AlignedCrossover<>(
                    (w1, w2, r) -> w1 + (w2 - w1) * (r.nextDouble() * 3d - 1d),
                    node -> node instanceof Output,
                    false
                ), graphCrossoverRate
            ),
            new Tournament(nTournament),
            new Worst(),
            nPop,
            true
        )),
        Map.entry("ograph-seq-ga", p -> new StandardEvolver<ValueGraph<Node, OperatorGraph.Edge>, RealFunction, Double>(
            OperatorGraph.builder()
                .andThen(MathUtils.fromMultivariateBuilder())
                .andThen(MathUtils.linearScaler((SymbolicRegressionFitness) p.getFitnessFunction())),
            new ShallowFactory(p.arity(), 1, constants),
            PartialComparator.from(Double.class).on(Individual::getFitness),
            nPop,
            Map.of(
                new NodeAddition<Node, OperatorGraph.Edge>(
                    OperatorNode.sequentialIndexFactory(baseOperators),
                    Mutation.copy(),
                    Mutation.copy()
                ).withChecker(OperatorGraph.checker()), graphNodeAdditionRate,
                new EdgeAddition<Node, OperatorGraph.Edge>(r -> OperatorGraph.EDGE, false).withChecker(OperatorGraph.checker()), graphEdgeAdditionRate,
                new EdgeRemoval<Node, OperatorGraph.Edge>(node -> node instanceof Output).withChecker(OperatorGraph.checker()), graphEdgeRemovalRate,
                new AlignedCrossover<Node, OperatorGraph.Edge>(
                    Crossover.randomCopy(),
                    node -> node instanceof Output,
                    false
                ).withChecker(OperatorGraph.checker()), graphCrossoverRate
            ),
            new Tournament(nTournament),
            new Worst(),
            nPop,
            true
        )),
        Map.entry("fgraph-hash-ga", p -> new StandardEvolver<ValueGraph<IndexedNode<Node>, Double>, RealFunction, Double>(
            GraphUtils.mapper((Function<IndexedNode<Node>, Node>) IndexedNode::content, (Function<Collection<Double>, Double>) Misc::first)
                .andThen(FunctionGraph.builder())
                .andThen(MathUtils.fromMultivariateBuilder())
                .andThen(MathUtils.linearScaler((SymbolicRegressionFitness) p.getFitnessFunction())),
            new ShallowSparseFactory(0d, 0d, 1d, p.arity(), 1)
                .then(GraphUtils.mapper(IndexedNode.incrementerMapper(Node.class), Misc::first)),
            PartialComparator.from(Double.class).on(Individual::getFitness),
            nPop,
            Map.of(
                new NodeAddition<>(
                    FunctionNode.sequentialIndexFactory(baseFunctions)
                        .then(IndexedNode.hashMapper(Node.class)),
                    (w, r) -> w,
                    (w, r) -> r.nextGaussian()
                ), graphNodeAdditionRate,
                new EdgeModification<>((w, r) -> w + r.nextGaussian(), 1d), graphEdgeMutationRate,
                new EdgeAddition<>(Random::nextGaussian, false), graphEdgeAdditionRate,
                new EdgeRemoval<>(node -> node.content() instanceof Output), graphEdgeRemovalRate,
                new AlignedCrossover<>(
                    (w1, w2, r) -> w1 + (w2 - w1) * (r.nextDouble() * 3d - 1d),
                    node -> node.content() instanceof Output,
                    false
                ), graphCrossoverRate
            ),
            new Tournament(nTournament),
            new Worst(),
            nPop,
            true
        )),
        Map.entry("fgraph-hash-speciated", p -> new SpeciatedEvolver<ValueGraph<IndexedNode<Node>, Double>, RealFunction, Double>(
            GraphUtils.mapper((Function<IndexedNode<Node>, Node>) IndexedNode::content, (Function<Collection<Double>, Double>) Misc::first)
                .andThen(FunctionGraph.builder())
                .andThen(MathUtils.fromMultivariateBuilder())
                .andThen(MathUtils.linearScaler((SymbolicRegressionFitness) p.getFitnessFunction())),
            new ShallowSparseFactory(0d, 0d, 1d, p.arity(), 1)
                .then(GraphUtils.mapper(IndexedNode.incrementerMapper(Node.class), Misc::first)),
            PartialComparator.from(Double.class).on(Individual::getFitness),
            nPop,
            Map.of(
                new NodeAddition<>(
                    FunctionNode.sequentialIndexFactory(baseFunctions)
                        .then(IndexedNode.hashMapper(Node.class)),
                    (w, r) -> w,
                    (w, r) -> r.nextGaussian()
                ), graphNodeAdditionRate,
                new EdgeModification<>((w, r) -> w + r.nextGaussian(), 1d), graphEdgeMutationRate,
                new EdgeAddition<>(Random::nextGaussian, false), graphEdgeAdditionRate,
                new EdgeRemoval<>(node -> node.content() instanceof Output), graphEdgeRemovalRate,
                new AlignedCrossover<>(
                    (w1, w2, r) -> w1 + (w2 - w1) * (r.nextDouble() * 3d - 1d),
                    node -> node.content() instanceof Output,
                    false
                ), graphCrossoverRate
            ),
            5,
            (new Jaccard()).on(i -> i.getGenotype().nodes()),
            0.25,
            individuals -> {
              double[] fitnesses = individuals.stream().mapToDouble(Individual::getFitness).toArray();
              Individual<ValueGraph<IndexedNode<Node>, Double>, RealFunction, Double> r = Misc.first(individuals);
              return new Individual<>(
                  r.getGenotype(),
                  r.getSolution(),
                  Misc.median(fitnesses),
                  r.getBirthIteration()
              );
            },
            0.75
        )),
        Map.entry("fgraph-hash+-speciated", p -> new SpeciatedEvolver<>(
            GraphUtils.mapper((Function<IndexedNode<Node>, Node>) IndexedNode::content, (Function<Collection<Double>, Double>) Misc::first)
                .andThen(FunctionGraph.builder())
                .andThen(MathUtils.fromMultivariateBuilder())
                .andThen(MathUtils.linearScaler((SymbolicRegressionFitness) p.getFitnessFunction())),
            new ShallowSparseFactory(0d, 0d, 1d, p.arity(), 1)
                .then(GraphUtils.mapper(IndexedNode.incrementerMapper(Node.class), Misc::first)),
            PartialComparator.from(Double.class).on(Individual::getFitness),
            nPop,
            Map.of(
                new IndexedNodeAddition<>(
                    FunctionNode.sequentialIndexFactory(baseFunctions),
                    n -> n.getFunction().hashCode(),
                    p.arity() + 2,
                    (w, r) -> w,
                    (w, r) -> r.nextGaussian()
                ), graphNodeAdditionRate,
                new EdgeModification<>((w, r) -> w + r.nextGaussian(), 1d), graphEdgeMutationRate,
                new EdgeAddition<>(Random::nextGaussian, false), graphEdgeAdditionRate,
                new EdgeRemoval<>(node -> node.content() instanceof Output), graphEdgeRemovalRate,
                new AlignedCrossover<>(
                    (w1, w2, r) -> w1 + (w2 - w1) * (r.nextDouble() * 3d - 1d),
                    node -> node.content() instanceof Output,
                    false
                ), graphCrossoverRate
            ),
            5,
            (new Jaccard()).on(i -> i.getGenotype().nodes()),
            0.25,
            individuals -> {
              double[] fitnesses = individuals.stream().mapToDouble(Individual::getFitness).toArray();
              Individual<ValueGraph<IndexedNode<Node>, Double>, RealFunction, Double> r = Misc.first(individuals);
              return new Individual<>(
                  r.getGenotype(),
                  r.getSolution(),
                  Misc.median(fitnesses),
                  r.getBirthIteration()
              );
            },
            0.75
        )),
        Map.entry("ograph-hash+-speciated", p -> {
          Function<ValueGraph<IndexedNode<Node>, OperatorGraph.Edge>, ValueGraph<Node, OperatorGraph.Edge>> graphMapper = GraphUtils.mapper(
              IndexedNode::content,
              Misc::first
          );
          Predicate<ValueGraph<Node, OperatorGraph.Edge>> checker = OperatorGraph.checker();
          return new SpeciatedEvolver<>(
              graphMapper
                  .andThen(OperatorGraph.builder())
                  .andThen(MathUtils.fromMultivariateBuilder())
                  .andThen(MathUtils.linearScaler((SymbolicRegressionFitness) p.getFitnessFunction())),
              new ShallowFactory(p.arity(), 1, constants)
                  .then(GraphUtils.mapper(IndexedNode.incrementerMapper(Node.class), Misc::first)),
              PartialComparator.from(Double.class).on(Individual::getFitness),
              nPop,
              Map.of(
                  new IndexedNodeAddition<OperatorNode, Node, OperatorGraph.Edge>(
                      OperatorNode.sequentialIndexFactory(baseOperators),
                      n -> n.getOperator().hashCode(),
                      p.arity() + 2,
                      Mutation.copy(),
                      Mutation.copy()
                  ).withChecker(g -> checker.test(graphMapper.apply(g))), graphNodeAdditionRate,
                  new EdgeAddition<IndexedNode<Node>, OperatorGraph.Edge>(r -> OperatorGraph.EDGE, false).withChecker(g -> checker.test(graphMapper.apply(g))), graphEdgeAdditionRate,
                  new EdgeRemoval<IndexedNode<Node>, OperatorGraph.Edge>(node -> node.content() instanceof Output).withChecker(g -> checker.test(graphMapper.apply(g))), graphEdgeRemovalRate,
                  new AlignedCrossover<IndexedNode<Node>, OperatorGraph.Edge>(
                      Crossover.randomCopy(),
                      node -> node.content() instanceof Output,
                      false
                  ).withChecker(g -> checker.test(graphMapper.apply(g))), graphCrossoverRate
              ),
              5,
              (new Jaccard()).on(i -> i.getGenotype().nodes()),
              0.25,
              individuals -> {
                double[] fitnesses = individuals.stream().mapToDouble(Individual::getFitness).toArray();
                Individual<ValueGraph<IndexedNode<Node>, OperatorGraph.Edge>, RealFunction, Double> r = Misc.first(individuals);
                return new Individual<>(
                    r.getGenotype(),
                    r.getSolution(),
                    Misc.median(fitnesses),
                    r.getBirthIteration()
                );
              },
              0.75
          );
        }),
        Map.entry("fgraph-hash+-speciated-noxover", p -> new SpeciatedEvolver<>(
            GraphUtils.mapper((Function<IndexedNode<Node>, Node>) IndexedNode::content, (Function<Collection<Double>, Double>) Misc::first)
                .andThen(FunctionGraph.builder())
                .andThen(MathUtils.fromMultivariateBuilder())
                .andThen(MathUtils.linearScaler((SymbolicRegressionFitness) p.getFitnessFunction())),
            new ShallowSparseFactory(0d, 0d, 1d, p.arity(), 1)
                .then(GraphUtils.mapper(IndexedNode.incrementerMapper(Node.class), Misc::first)),
            PartialComparator.from(Double.class).on(Individual::getFitness),
            nPop,
            Map.of(
                new IndexedNodeAddition<>(
                    FunctionNode.sequentialIndexFactory(baseFunctions),
                    n -> n.getFunction().hashCode(),
                    p.arity() + 2,
                    (w, r) -> w,
                    (w, r) -> r.nextGaussian()
                ), graphNodeAdditionRate,
                new EdgeModification<>((w, r) -> w + r.nextGaussian(), 1d), graphEdgeMutationRate,
                new EdgeAddition<>(Random::nextGaussian, false), graphEdgeAdditionRate,
                new EdgeRemoval<>(node -> node.content() instanceof Output), graphEdgeRemovalRate
            ),
            5,
            (new Jaccard()).on(i -> i.getGenotype().nodes()),
            0.25,
            individuals -> {
              double[] fitnesses = individuals.stream().mapToDouble(Individual::getFitness).toArray();
              Individual<ValueGraph<IndexedNode<Node>, Double>, RealFunction, Double> r = Misc.first(individuals);
              return new Individual<>(
                  r.getGenotype(),
                  r.getSolution(),
                  Misc.median(fitnesses),
                  r.getBirthIteration()
              );
            },
            0.75
        )),
        Map.entry("fgraph-seq-ga-noxover", p -> new StandardEvolver<ValueGraph<Node, Double>, RealFunction, Double>(
            FunctionGraph.builder()
                .andThen(MathUtils.fromMultivariateBuilder())
                .andThen(MathUtils.linearScaler((SymbolicRegressionFitness) p.getFitnessFunction())),
            new ShallowSparseFactory(0d, 0d, 1d, p.arity(), 1),
            PartialComparator.from(Double.class).on(Individual::getFitness),
            nPop,
            Map.of(
                new NodeAddition<>(
                    FunctionNode.sequentialIndexFactory(baseFunctions),
                    (w, r) -> w,
                    (w, r) -> r.nextGaussian()
                ), graphNodeAdditionRate,
                new EdgeModification<>((w, r) -> w + r.nextGaussian(), 1d), graphEdgeMutationRate,
                new EdgeAddition<>(Random::nextGaussian, false), graphEdgeAdditionRate,
                new EdgeRemoval<>(node -> node instanceof Output), graphEdgeRemovalRate
            ),
            new Tournament(nTournament),
            new Worst(),
            nPop,
            true
        )),
        Map.entry("fgraph-seq-gadiv", p -> new StandardWithEnforcedDiversityEvolver<ValueGraph<Node, Double>, RealFunction, Double>(
            FunctionGraph.builder()
                .andThen(MathUtils.fromMultivariateBuilder())
                .andThen(MathUtils.linearScaler((SymbolicRegressionFitness) p.getFitnessFunction())),
            new ShallowSparseFactory(0d, 0d, 1d, p.arity(), 1),
            PartialComparator.from(Double.class).on(Individual::getFitness),
            nPop,
            Map.of(
                new NodeAddition<>(
                    FunctionNode.sequentialIndexFactory(baseFunctions),
                    (w, r) -> w,
                    (w, r) -> r.nextGaussian()
                ), graphNodeAdditionRate,
                new EdgeModification<>((w, r) -> w + r.nextGaussian(), 1d), graphEdgeMutationRate,
                new EdgeAddition<>(Random::nextGaussian, false), graphEdgeAdditionRate,
                new EdgeRemoval<>(node -> node instanceof Output), graphEdgeRemovalRate,
                new AlignedCrossover<>(
                    (w1, w2, r) -> w1 + (w2 - w1) * (r.nextDouble() * 3d - 1d),
                    node -> node instanceof Output,
                    false
                ), graphCrossoverRate
            ),
            new Tournament(nTournament),
            new Worst(),
            nPop,
            true,
            diversityMaxAttempts
        ))
    ));
    //filter evolvers
    evolvers = evolvers.entrySet().stream()
        .filter(e -> e.getKey().matches(evolverNamePattern))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    L.info(String.format("Going to test with %d evolvers: %s%n",
        evolvers.size(),
        evolvers.keySet()
    ));
    //run
    for (int seed : seeds) {
      for (SymbolicRegressionProblem problem : problems) {
        for (Map.Entry<String, Function<SymbolicRegressionProblem, Evolver<?, RealFunction, Double>>> evolverEntry : evolvers.entrySet()) {
          Map<String, String> keys = new TreeMap<>(Map.of(
              "seed", Integer.toString(seed),
              "problem", problem.getClass().getSimpleName().toLowerCase(),
              "evolver", evolverEntry.getKey()
          ));
          try {
            List<DataCollector<?, ? super RealFunction, ? super Double>> collectors = List.of(new Static(keys),
                new Basic(),
                new Population(),
                new Diversity(),
                new BestInfo("%7.5f"),
                new FunctionOfOneBest<>(i -> List.of(new Item(
                    "validation.fitness",
                    problem.getValidationFunction().apply(i.getSolution()),
                    "%7.5f"
                ))),
                new BestPrinter(BestPrinter.Part.SOLUTION, "%80.80s")
            );
            Stopwatch stopwatch = Stopwatch.createStarted();
            Evolver<?, RealFunction, Double> evolver = evolverEntry.getValue().apply(problem);
            L.info(String.format("Starting %s", keys));
            Collection<RealFunction> solutions = evolver.solve(
                Misc.cached(problem.getFitnessFunction(), 10000),
                new Iterations(nIterations),
                new Random(seed),
                executorService,
                Listener.onExecutor((listenerFactory.getBaseFileName() == null) ?
                        listener(collectors.toArray(DataCollector[]::new)) :
                        listenerFactory.build(collectors.toArray(DataCollector[]::new))
                    , executorService));
            L.info(String.format("Done %s: %d solutions in %4.1fs",
                keys,
                solutions.size(),
                (double) stopwatch.elapsed(TimeUnit.MILLISECONDS) / 1000d
            ));
          } catch (InterruptedException | ExecutionException e) {
            L.severe(String.format("Cannot complete %s due to %s",
                keys,
                e
            ));
            e.printStackTrace();
          }
        }
      }
    }
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
}
