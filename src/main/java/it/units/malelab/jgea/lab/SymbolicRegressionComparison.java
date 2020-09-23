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
    int nIterations = i(a("nIterations", "100"));
    String evolverNamePattern = a("evolver", "ograph-hash\\+-speciated");
    int[] seeds = ri(a("seed", "0:1"));
    double graphArcAdditionRate = 3d;
    double graphArcMutationRate = 1d;
    double graphArcRemovalRate = 0d;
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
              PartialComparator.from(Double.class).comparing(Individual::getFitness),
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
              PartialComparator.from(Double.class).comparing(Individual::getFitness),
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
              PartialComparator.from(Double.class).comparing(Individual::getFitness),
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
              PartialComparator.from(Double.class).comparing(Individual::getFitness),
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
              PartialComparator.from(Double.class).comparing(Individual::getFitness),
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
              PartialComparator.from(Double.class).comparing(Individual::getFitness),
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
        Map.entry("fgraph-lim-ga", p -> new StandardEvolver<Graph<Node, Double>, RealFunction, Double>(
            FunctionGraph.builder()
                .andThen(MathUtils.fromMultivariateBuilder())
                .andThen(MathUtils.linearScaler((SymbolicRegressionFitness) p.getFitnessFunction())),
            new ShallowSparseFactory(0d, 0d, 1d, p.arity(), 1),
            PartialComparator.from(Double.class).comparing(Individual::getFitness),
            nPop,
            Map.of(
                new NodeAddition<Node, Double>(
                    FunctionNode.limitedIndexFactory(maxNodes, baseFunctions),
                    (w, r) -> w,
                    (w, r) -> r.nextGaussian()
                ).withChecker(FunctionGraph.checker()), graphNodeAdditionRate,
                new ArcModification<Node, Double>((w, r) -> w + r.nextGaussian(), 1d).withChecker(FunctionGraph.checker()), graphArcMutationRate,
                new ArcAddition<Node, Double>(Random::nextGaussian, false).withChecker(FunctionGraph.checker()), graphArcAdditionRate,
                new ArcRemoval<Node, Double>(
                    node -> (node instanceof Input) || (node instanceof it.units.malelab.jgea.representation.graph.numeric.Constant) || (node instanceof Output)
                ).withChecker(FunctionGraph.checker()), graphArcRemovalRate,
                new AlignedCrossover<Node, Double>(
                    (w1, w2, r) -> w1 + (w2 - w1) * (r.nextDouble() * 3d - 1d),
                    node -> (node instanceof Input) || (node instanceof it.units.malelab.jgea.representation.graph.numeric.Constant) || (node instanceof Output),
                    false
                ).withChecker(FunctionGraph.checker()), graphCrossoverRate
            ),
            new Tournament(nTournament),
            new Worst(),
            nPop,
            true
        )),
        Map.entry("fgraph-lim-ga-noxover", p -> new StandardEvolver<Graph<Node, Double>, RealFunction, Double>(
            FunctionGraph.builder()
                .andThen(MathUtils.fromMultivariateBuilder())
                .andThen(MathUtils.linearScaler((SymbolicRegressionFitness) p.getFitnessFunction())),
            new ShallowSparseFactory(0d, 0d, 1d, p.arity(), 1),
            PartialComparator.from(Double.class).comparing(Individual::getFitness),
            nPop,
            Map.of(
                new NodeAddition<Node, Double>(
                    FunctionNode.limitedIndexFactory(maxNodes, baseFunctions),
                    (w, r) -> w,
                    (w, r) -> r.nextGaussian()
                ).withChecker(FunctionGraph.checker()), graphNodeAdditionRate,
                new ArcModification<Node, Double>((w, r) -> w + r.nextGaussian(), 1d).withChecker(FunctionGraph.checker()), graphArcMutationRate,
                new ArcAddition<Node, Double>(Random::nextGaussian, false).withChecker(FunctionGraph.checker()), graphArcAdditionRate,
                new ArcRemoval<Node, Double>(
                    node -> (node instanceof Input) || (node instanceof it.units.malelab.jgea.representation.graph.numeric.Constant) || (node instanceof Output)
                ).withChecker(FunctionGraph.checker()), graphArcRemovalRate
            ),
            new Tournament(nTournament),
            new Worst(),
            nPop,
            true
        )),
        Map.entry("fgraph-lim-speciated-noxover", p -> new SpeciatedEvolver<Graph<Node, Double>, RealFunction, Double>(
            FunctionGraph.builder()
                .andThen(MathUtils.fromMultivariateBuilder())
                .andThen(MathUtils.linearScaler((SymbolicRegressionFitness) p.getFitnessFunction())),
            new ShallowSparseFactory(0d, 0d, 1d, p.arity(), 1),
            PartialComparator.from(Double.class).comparing(Individual::getFitness),
            nPop,
            Map.of(
                new NodeAddition<Node, Double>(
                    FunctionNode.limitedIndexFactory(maxNodes, baseFunctions),
                    (w, r) -> w,
                    (w, r) -> r.nextGaussian()
                ).withChecker(FunctionGraph.checker()), graphNodeAdditionRate,
                new ArcModification<Node, Double>((w, r) -> w + r.nextGaussian(), 1d).withChecker(FunctionGraph.checker()), graphArcMutationRate,
                new ArcAddition<Node, Double>(Random::nextGaussian, false).withChecker(FunctionGraph.checker()), graphArcAdditionRate,
                new ArcRemoval<Node, Double>(
                    node -> (node instanceof Input) || (node instanceof it.units.malelab.jgea.representation.graph.numeric.Constant) || (node instanceof Output)
                ).withChecker(FunctionGraph.checker()), graphArcRemovalRate
            ),
            5,
            (new Jaccard()).on(i -> i.getGenotype().nodes()),
            0.25,
            individuals -> {
              double[] fitnesses = individuals.stream().mapToDouble(i -> i.getFitness()).toArray();
              Individual<Graph<Node, Double>, RealFunction, Double> r = Misc.first(individuals);
              return new Individual<>(
                  r.getGenotype(),
                  r.getSolution(),
                  Misc.median(fitnesses),
                  r.getBirthIteration()
              );
            },
            0.75
        )),
        Map.entry("fgraph-seq-speciated-noxover", p -> new SpeciatedEvolver<Graph<Node, Double>, RealFunction, Double>(
            FunctionGraph.builder()
                .andThen(MathUtils.fromMultivariateBuilder())
                .andThen(MathUtils.linearScaler((SymbolicRegressionFitness) p.getFitnessFunction())),
            new ShallowSparseFactory(0d, 0d, 1d, p.arity(), 1),
            PartialComparator.from(Double.class).comparing(Individual::getFitness),
            nPop,
            Map.of(
                new NodeAddition<Node, Double>(
                    FunctionNode.sequentialIndexFactory(baseFunctions),
                    (w, r) -> w,
                    (w, r) -> r.nextGaussian()
                ).withChecker(FunctionGraph.checker()), graphNodeAdditionRate,
                new ArcModification<Node, Double>((w, r) -> w + r.nextGaussian(), 1d).withChecker(FunctionGraph.checker()), graphArcMutationRate,
                new ArcAddition<Node, Double>(Random::nextGaussian, false).withChecker(FunctionGraph.checker()), graphArcAdditionRate,
                new ArcRemoval<Node, Double>(
                    node -> (node instanceof Input) || (node instanceof it.units.malelab.jgea.representation.graph.numeric.Constant) || (node instanceof Output)
                ).withChecker(FunctionGraph.checker()), graphArcRemovalRate
            ),
            5,
            (new Jaccard()).on(i -> i.getGenotype().nodes()),
            0.25,
            individuals -> {
              double[] fitnesses = individuals.stream().mapToDouble(i -> i.getFitness()).toArray();
              Individual<Graph<Node, Double>, RealFunction, Double> r = Misc.first(individuals);
              return new Individual<>(
                  r.getGenotype(),
                  r.getSolution(),
                  Misc.median(fitnesses),
                  r.getBirthIteration()
              );
            },
            0.75
        )),
        Map.entry("fgraph-lim-gadiv", p -> new StandardWithEnforcedDiversityEvolver<Graph<Node, Double>, RealFunction, Double>(
            FunctionGraph.builder()
                .andThen(MathUtils.fromMultivariateBuilder())
                .andThen(MathUtils.linearScaler((SymbolicRegressionFitness) p.getFitnessFunction())),
            new ShallowSparseFactory(0d, 0d, 1d, p.arity(), 1),
            PartialComparator.from(Double.class).comparing(Individual::getFitness),
            nPop,
            Map.of(
                new NodeAddition<Node, Double>(
                    FunctionNode.limitedIndexFactory(maxNodes, baseFunctions),
                    (w, r) -> w,
                    (w, r) -> r.nextGaussian()
                ).withChecker(FunctionGraph.checker()), graphNodeAdditionRate,
                new ArcModification<Node, Double>((w, r) -> w + r.nextGaussian(), 1d).withChecker(FunctionGraph.checker()), graphArcMutationRate,
                new ArcAddition<Node, Double>(Random::nextGaussian, false).withChecker(FunctionGraph.checker()), graphArcAdditionRate,
                new ArcRemoval<Node, Double>(
                    node -> (node instanceof Input) || (node instanceof it.units.malelab.jgea.representation.graph.numeric.Constant) || (node instanceof Output)
                ), graphArcRemovalRate,
                new AlignedCrossover<Node, Double>(
                    (w1, w2, r) -> w1 + (w2 - w1) * (r.nextDouble() * 3d - 1d),
                    node -> (node instanceof Input) || (node instanceof it.units.malelab.jgea.representation.graph.numeric.Constant) || (node instanceof Output),
                    false
                ).withChecker(FunctionGraph.checker()), graphCrossoverRate
            ),
            new Tournament(nTournament),
            new Worst(),
            nPop,
            true,
            diversityMaxAttempts
        )),
        Map.entry("fgraph-lim-speciated", p -> new SpeciatedEvolver<Graph<Node, Double>, RealFunction, Double>(
            FunctionGraph.builder()
                .andThen(MathUtils.fromMultivariateBuilder())
                .andThen(MathUtils.linearScaler((SymbolicRegressionFitness) p.getFitnessFunction())),
            new ShallowSparseFactory(0d, 0d, 1d, p.arity(), 1),
            PartialComparator.from(Double.class).comparing(Individual::getFitness),
            nPop,
            Map.of(
                new NodeAddition<Node, Double>(
                    FunctionNode.limitedIndexFactory(maxNodes, baseFunctions),
                    (w, r) -> w,
                    (w, r) -> r.nextGaussian()
                ).withChecker(FunctionGraph.checker()), graphNodeAdditionRate,
                new ArcModification<Node, Double>((w, r) -> w + r.nextGaussian(), 1d).withChecker(FunctionGraph.checker()), graphArcMutationRate,
                new ArcAddition<Node, Double>(Random::nextGaussian, false).withChecker(FunctionGraph.checker()), graphArcAdditionRate,
                new ArcRemoval<Node, Double>(
                    node -> (node instanceof Input) || (node instanceof it.units.malelab.jgea.representation.graph.numeric.Constant) || (node instanceof Output)
                ), graphArcRemovalRate,
                new AlignedCrossover<Node, Double>(
                    (w1, w2, r) -> w1 + (w2 - w1) * (r.nextDouble() * 3d - 1d),
                    node -> (node instanceof Input) || (node instanceof it.units.malelab.jgea.representation.graph.numeric.Constant) || (node instanceof Output),
                    false
                ).withChecker(FunctionGraph.checker()), graphCrossoverRate
            ),
            5,
            (new Jaccard()).on(i -> i.getGenotype().nodes()),
            0.25,
            individuals -> {
              double[] fitnesses = individuals.stream().mapToDouble(i -> i.getFitness()).toArray();
              Individual<Graph<Node, Double>, RealFunction, Double> r = Misc.first(individuals);
              return new Individual<>(
                  r.getGenotype(),
                  r.getSolution(),
                  Misc.median(fitnesses),
                  r.getBirthIteration()
              );
            },
            0.75
        )),
        Map.entry("fgraph-seq-speciated", p -> new SpeciatedEvolver<Graph<Node, Double>, RealFunction, Double>(
            FunctionGraph.builder()
                .andThen(MathUtils.fromMultivariateBuilder())
                .andThen(MathUtils.linearScaler((SymbolicRegressionFitness) p.getFitnessFunction())),
            new ShallowSparseFactory(0d, 0d, 1d, p.arity(), 1),
            PartialComparator.from(Double.class).comparing(Individual::getFitness),
            nPop,
            Map.of(
                new NodeAddition<Node, Double>(
                    FunctionNode.sequentialIndexFactory(baseFunctions),
                    (w, r) -> w,
                    (w, r) -> r.nextGaussian()
                ).withChecker(FunctionGraph.checker()), graphNodeAdditionRate,
                new ArcModification<Node, Double>((w, r) -> w + r.nextGaussian(), 1d).withChecker(FunctionGraph.checker()), graphArcMutationRate,
                new ArcAddition<Node, Double>(Random::nextGaussian, false).withChecker(FunctionGraph.checker()), graphArcAdditionRate,
                new ArcRemoval<Node, Double>(
                    node -> (node instanceof Input) || (node instanceof it.units.malelab.jgea.representation.graph.numeric.Constant) || (node instanceof Output)
                ).withChecker(FunctionGraph.checker()), graphArcRemovalRate,
                new AlignedCrossover<Node, Double>(
                    (w1, w2, r) -> w1 + (w2 - w1) * (r.nextDouble() * 3d - 1d),
                    node -> (node instanceof Input) || (node instanceof it.units.malelab.jgea.representation.graph.numeric.Constant) || (node instanceof Output),
                    false
                ).withChecker(FunctionGraph.checker()), graphCrossoverRate
            ),
            5,
            (new Jaccard()).on(i -> i.getGenotype().nodes()),
            0.25,
            individuals -> {
              double[] fitnesses = individuals.stream().mapToDouble(i -> i.getFitness()).toArray();
              Individual<Graph<Node, Double>, RealFunction, Double> r = Misc.first(individuals);
              return new Individual<>(
                  r.getGenotype(),
                  r.getSolution(),
                  Misc.median(fitnesses),
                  r.getBirthIteration()
              );
            },
            0.75
        )),
        Map.entry("fgraph-seq-ga", p -> new StandardEvolver<Graph<Node, Double>, RealFunction, Double>(
            FunctionGraph.builder()
                .andThen(MathUtils.fromMultivariateBuilder())
                .andThen(MathUtils.linearScaler((SymbolicRegressionFitness) p.getFitnessFunction())),
            new ShallowSparseFactory(0d, 0d, 1d, p.arity(), 1),
            PartialComparator.from(Double.class).comparing(Individual::getFitness),
            nPop,
            Map.of(
                new NodeAddition<Node, Double>(
                    FunctionNode.sequentialIndexFactory(baseFunctions),
                    (w, r) -> w,
                    (w, r) -> r.nextGaussian()
                ).withChecker(FunctionGraph.checker()), graphNodeAdditionRate,
                new ArcModification<Node, Double>((w, r) -> w + r.nextGaussian(), 1d).withChecker(FunctionGraph.checker()), graphArcMutationRate,
                new ArcAddition<Node, Double>(Random::nextGaussian, false).withChecker(FunctionGraph.checker()), graphArcAdditionRate,
                new ArcRemoval<Node, Double>(
                    node -> (node instanceof Input) || (node instanceof it.units.malelab.jgea.representation.graph.numeric.Constant) || (node instanceof Output)
                ).withChecker(FunctionGraph.checker()), graphArcRemovalRate,
                new AlignedCrossover<Node, Double>(
                    (w1, w2, r) -> w1 + (w2 - w1) * (r.nextDouble() * 3d - 1d),
                    node -> (node instanceof Input) || (node instanceof it.units.malelab.jgea.representation.graph.numeric.Constant) || (node instanceof Output),
                    false
                ).withChecker(FunctionGraph.checker()), graphCrossoverRate
            ),
            new Tournament(nTournament),
            new Worst(),
            nPop,
            true
        )),
        Map.entry("ograph-seq-ga", p -> new StandardEvolver<Graph<Node, OperatorGraph.NonValuedArc>, RealFunction, Double>(
            OperatorGraph.builder()
                .andThen(MathUtils.fromMultivariateBuilder())
                .andThen(MathUtils.linearScaler((SymbolicRegressionFitness) p.getFitnessFunction())),
            new ShallowFactory(p.arity(), 1, constants),
            PartialComparator.from(Double.class).comparing(Individual::getFitness),
            nPop,
            Map.of(
                new NodeAddition<Node, OperatorGraph.NonValuedArc>(
                    OperatorNode.sequentialIndexFactory(baseOperators),
                    Mutation.copy(),
                    Mutation.copy()
                ).withChecker(OperatorGraph.checker()), graphNodeAdditionRate,
                new ArcAddition<Node, OperatorGraph.NonValuedArc>(r -> OperatorGraph.NON_VALUED_ARC, false).withChecker(OperatorGraph.checker()), graphArcAdditionRate,
                new ArcRemoval<Node, OperatorGraph.NonValuedArc>(
                    node -> (node instanceof Input) || (node instanceof it.units.malelab.jgea.representation.graph.numeric.Constant) || (node instanceof Output)
                ).withChecker(OperatorGraph.checker()), graphArcRemovalRate,
                new AlignedCrossover<Node, OperatorGraph.NonValuedArc>(
                    Crossover.randomCopy(),
                    node -> (node instanceof Input) || (node instanceof it.units.malelab.jgea.representation.graph.numeric.Constant) || (node instanceof Output),
                    false
                ).withChecker(OperatorGraph.checker()), graphCrossoverRate
            ),
            new Tournament(nTournament),
            new Worst(),
            nPop,
            true
        )),
        Map.entry("ograph-seq-speciated-noxover", p -> new SpeciatedEvolver<Graph<Node, OperatorGraph.NonValuedArc>, RealFunction, Double>(
            OperatorGraph.builder()
                .andThen(MathUtils.fromMultivariateBuilder())
                .andThen(MathUtils.linearScaler((SymbolicRegressionFitness) p.getFitnessFunction())),
            new ShallowFactory(p.arity(), 1, constants),
            PartialComparator.from(Double.class).comparing(Individual::getFitness),
            nPop,
            Map.of(
                new NodeAddition<Node, OperatorGraph.NonValuedArc>(
                    OperatorNode.sequentialIndexFactory(baseOperators),
                    Mutation.copy(),
                    Mutation.copy()
                ).withChecker(OperatorGraph.checker()), graphNodeAdditionRate,
                new ArcAddition<Node, OperatorGraph.NonValuedArc>(r -> OperatorGraph.NON_VALUED_ARC, false).withChecker(OperatorGraph.checker()), graphArcAdditionRate,
                new ArcRemoval<Node, OperatorGraph.NonValuedArc>(
                    node -> (node instanceof Input) || (node instanceof it.units.malelab.jgea.representation.graph.numeric.Constant) || (node instanceof Output)
                ).withChecker(OperatorGraph.checker()), graphArcRemovalRate
            ),
            5,
            (new Jaccard()).on(i -> i.getGenotype().nodes()),
            0.25,
            individuals -> {
              double[] fitnesses = individuals.stream().mapToDouble(i -> i.getFitness()).toArray();
              Individual<Graph<Node, OperatorGraph.NonValuedArc>, RealFunction, Double> r = Misc.first(individuals);
              return new Individual<>(
                  r.getGenotype(),
                  r.getSolution(),
                  Misc.median(fitnesses),
                  r.getBirthIteration()
              );
            },
            0.75
        )),
        Map.entry("fgraph-hash-ga", p -> {
          Function<Graph<IndexedNode<Node>, Double>, Graph<Node, Double>> graphMapper = GraphUtils.mapper(
              IndexedNode::content,
              Misc::first
          );
          Predicate<Graph<Node, Double>> checker = FunctionGraph.checker();
          return new StandardEvolver<Graph<IndexedNode<Node>, Double>, RealFunction, Double>(
              graphMapper
                  .andThen(FunctionGraph.builder())
                  .andThen(MathUtils.fromMultivariateBuilder())
                  .andThen(MathUtils.linearScaler((SymbolicRegressionFitness) p.getFitnessFunction())),
              new ShallowSparseFactory(0d, 0d, 1d, p.arity(), 1)
                  .then(GraphUtils.mapper(IndexedNode.incrementerMapper(Node.class), Misc::first)),
              PartialComparator.from(Double.class).comparing(Individual::getFitness),
              nPop,
              Map.of(
                  new NodeAddition<IndexedNode<Node>, Double>(
                      FunctionNode.sequentialIndexFactory(baseFunctions)
                          .then(IndexedNode.hashMapper(Node.class)),
                      (w, r) -> w,
                      (w, r) -> r.nextGaussian()
                  ).withChecker(g -> checker.test(graphMapper.apply(g))), graphNodeAdditionRate,
                  new ArcModification<IndexedNode<Node>, Double>((w, r) -> w + r.nextGaussian(), 1d).withChecker(g -> checker.test(graphMapper.apply(g))), graphArcMutationRate,
                  new ArcAddition<IndexedNode<Node>, Double>(Random::nextGaussian, false).withChecker(g -> checker.test(graphMapper.apply(g))), graphArcAdditionRate,
                  new ArcRemoval<IndexedNode<Node>, Double>(node -> node.content() instanceof Output).withChecker(g -> checker.test(graphMapper.apply(g))), graphArcRemovalRate,
                  new AlignedCrossover<IndexedNode<Node>, Double>(
                      (w1, w2, r) -> w1 + (w2 - w1) * (r.nextDouble() * 3d - 1d),
                      node -> node.content() instanceof Output,
                      false
                  ).withChecker(g -> checker.test(graphMapper.apply(g))), graphCrossoverRate
              ),
              new Tournament(nTournament),
              new Worst(),
              nPop,
              true
          );
        }),
        Map.entry("fgraph-hash-speciated", p -> {
          Function<Graph<IndexedNode<Node>, Double>, Graph<Node, Double>> graphMapper = GraphUtils.mapper(
              IndexedNode::content,
              Misc::first
          );
          Predicate<Graph<Node, Double>> checker = FunctionGraph.checker();
          return new SpeciatedEvolver<Graph<IndexedNode<Node>, Double>, RealFunction, Double>(
              graphMapper
                  .andThen(FunctionGraph.builder())
                  .andThen(MathUtils.fromMultivariateBuilder())
                  .andThen(MathUtils.linearScaler((SymbolicRegressionFitness) p.getFitnessFunction())),
              new ShallowSparseFactory(0d, 0d, 1d, p.arity(), 1)
                  .then(GraphUtils.mapper(IndexedNode.incrementerMapper(Node.class), Misc::first)),
              PartialComparator.from(Double.class).comparing(Individual::getFitness),
              nPop,
              Map.of(
                  new NodeAddition<IndexedNode<Node>, Double>(
                      FunctionNode.sequentialIndexFactory(baseFunctions)
                          .then(IndexedNode.hashMapper(Node.class)),
                      (w, r) -> w,
                      (w, r) -> r.nextGaussian()
                  ).withChecker(g -> checker.test(graphMapper.apply(g))), graphNodeAdditionRate,
                  new ArcModification<IndexedNode<Node>, Double>((w, r) -> w + r.nextGaussian(), 1d).withChecker(g -> checker.test(graphMapper.apply(g))), graphArcMutationRate,
                  new ArcAddition<IndexedNode<Node>, Double>(Random::nextGaussian, false).withChecker(g -> checker.test(graphMapper.apply(g))), graphArcAdditionRate,
                  new ArcRemoval<IndexedNode<Node>, Double>(node -> node.content() instanceof Output).withChecker(g -> checker.test(graphMapper.apply(g))), graphArcRemovalRate,
                  new AlignedCrossover<IndexedNode<Node>, Double>(
                      (w1, w2, r) -> w1 + (w2 - w1) * (r.nextDouble() * 3d - 1d),
                      node -> node.content() instanceof Output,
                      false
                  ).withChecker(g -> checker.test(graphMapper.apply(g))), graphCrossoverRate
              ),
              5,
              (new Jaccard()).on(i -> i.getGenotype().nodes()),
              0.25,
              individuals -> {
                double[] fitnesses = individuals.stream().mapToDouble(Individual::getFitness).toArray();
                Individual<Graph<IndexedNode<Node>, Double>, RealFunction, Double> r = Misc.first(individuals);
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
        Map.entry("fgraph-hash+-speciated", p -> {
          Function<Graph<IndexedNode<Node>, Double>, Graph<Node, Double>> graphMapper = GraphUtils.mapper(
              IndexedNode::content,
              Misc::first
          );
          Predicate<Graph<Node, Double>> checker = FunctionGraph.checker();
          return new SpeciatedEvolver<>(
              GraphUtils.mapper((Function<IndexedNode<Node>, Node>) IndexedNode::content, (Function<Collection<Double>, Double>) Misc::first)
                  .andThen(FunctionGraph.builder())
                  .andThen(MathUtils.fromMultivariateBuilder())
                  .andThen(MathUtils.linearScaler((SymbolicRegressionFitness) p.getFitnessFunction())),
              new ShallowSparseFactory(0d, 0d, 1d, p.arity(), 1)
                  .then(GraphUtils.mapper(IndexedNode.incrementerMapper(Node.class), Misc::first)),
              PartialComparator.from(Double.class).comparing(Individual::getFitness),
              nPop,
              Map.of(
                  new IndexedNodeAddition<FunctionNode, Node, Double>(
                      FunctionNode.sequentialIndexFactory(baseFunctions),
                      n -> (n instanceof FunctionNode) ? ((FunctionNode) n).getFunction().hashCode() : 0,
                      p.arity() + 1 + 1,
                      (w, r) -> w,
                      (w, r) -> r.nextGaussian()
                  ).withChecker(g -> checker.test(graphMapper.apply(g))), graphNodeAdditionRate,
                  new ArcModification<IndexedNode<Node>, Double>((w, r) -> w + r.nextGaussian(), 1d).withChecker(g -> checker.test(graphMapper.apply(g))), graphArcMutationRate,
                  new ArcAddition<IndexedNode<Node>, Double>(Random::nextGaussian, false).withChecker(g -> checker.test(graphMapper.apply(g))), graphArcAdditionRate,
                  new ArcRemoval<IndexedNode<Node>, Double>(node -> node.content() instanceof Output).withChecker(g -> checker.test(graphMapper.apply(g))), graphArcRemovalRate,
                  new AlignedCrossover<IndexedNode<Node>, Double>(
                      (w1, w2, r) -> w1 + (w2 - w1) * (r.nextDouble() * 3d - 1d),
                      node -> node.content() instanceof Output,
                      false
                  ).withChecker(g -> checker.test(graphMapper.apply(g))), graphCrossoverRate
              ),
              5,
              (new Jaccard()).on(i -> i.getGenotype().nodes()),
              0.25,
              individuals -> {
                double[] fitnesses = individuals.stream().mapToDouble(Individual::getFitness).toArray();
                Individual<Graph<IndexedNode<Node>, Double>, RealFunction, Double> r = Misc.first(individuals);
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
        Map.entry("ograph-hash+-speciated", p -> {
          Function<Graph<IndexedNode<Node>, OperatorGraph.NonValuedArc>, Graph<Node, OperatorGraph.NonValuedArc>> graphMapper = GraphUtils.mapper(
              IndexedNode::content,
              Misc::first
          );
          Predicate<Graph<Node, OperatorGraph.NonValuedArc>> checker = OperatorGraph.checker();
          return new SpeciatedEvolver<>(
              graphMapper
                  .andThen(OperatorGraph.builder())
                  .andThen(MathUtils.fromMultivariateBuilder())
                  .andThen(MathUtils.linearScaler((SymbolicRegressionFitness) p.getFitnessFunction())),
              new ShallowFactory(p.arity(), 1, constants)
                  .then(GraphUtils.mapper(IndexedNode.incrementerMapper(Node.class), Misc::first)),
              PartialComparator.from(Double.class).comparing(Individual::getFitness),
              nPop,
              Map.of(
                  new IndexedNodeAddition<OperatorNode, Node, OperatorGraph.NonValuedArc>(
                      OperatorNode.sequentialIndexFactory(baseOperators),
                      n -> (n instanceof OperatorNode) ? ((OperatorNode) n).getOperator().hashCode() : 0,
                      p.arity() + 1 + constants.length,
                      Mutation.copy(),
                      Mutation.copy()
                  ).withChecker(g -> checker.test(graphMapper.apply(g))), graphNodeAdditionRate,
                  new ArcAddition<IndexedNode<Node>, OperatorGraph.NonValuedArc>(r -> OperatorGraph.NON_VALUED_ARC, false)
                      .withChecker(g -> checker.test(graphMapper.apply(g))), graphArcAdditionRate,
                  new ArcRemoval<IndexedNode<Node>, OperatorGraph.NonValuedArc>(node -> node.content() instanceof Output)
                      .withChecker(g -> checker.test(graphMapper.apply(g))), graphArcRemovalRate,
                  new AlignedCrossover<IndexedNode<Node>, OperatorGraph.NonValuedArc>(
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
                Individual<Graph<IndexedNode<Node>, OperatorGraph.NonValuedArc>, RealFunction, Double> r = Misc.first(individuals);
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
        Map.entry("fgraph-hash+-speciated-noxover", p -> {
          Function<Graph<IndexedNode<Node>, Double>, Graph<Node, Double>> graphMapper = GraphUtils.mapper(
              IndexedNode::content,
              Misc::first
          );
          Predicate<Graph<Node, Double>> checker = FunctionGraph.checker();
          return new SpeciatedEvolver<>(
              graphMapper
                  .andThen(FunctionGraph.builder())
                  .andThen(MathUtils.fromMultivariateBuilder())
                  .andThen(MathUtils.linearScaler((SymbolicRegressionFitness) p.getFitnessFunction())),
              new ShallowSparseFactory(0d, 0d, 1d, p.arity(), 1)
                  .then(GraphUtils.mapper(IndexedNode.incrementerMapper(Node.class), Misc::first)),
              PartialComparator.from(Double.class).comparing(Individual::getFitness),
              nPop,
              Map.of(
                  new IndexedNodeAddition<FunctionNode, Node, Double>(
                      FunctionNode.sequentialIndexFactory(baseFunctions),
                      n -> (n instanceof FunctionNode) ? ((FunctionNode) n).getFunction().hashCode() : 0,
                      p.arity() + 1 + 1,
                      (w, r) -> w,
                      (w, r) -> r.nextGaussian()
                  ).withChecker(g -> checker.test(graphMapper.apply(g))), graphNodeAdditionRate,
                  new ArcModification<IndexedNode<Node>, Double>((w, r) -> w + r.nextGaussian(), 1d).withChecker(g -> checker.test(graphMapper.apply(g))), graphArcMutationRate,
                  new ArcAddition<IndexedNode<Node>, Double>(Random::nextGaussian, false).withChecker(g -> checker.test(graphMapper.apply(g))), graphArcAdditionRate,
                  new ArcRemoval<IndexedNode<Node>, Double>(node -> node.content() instanceof Output).withChecker(g -> checker.test(graphMapper.apply(g))), graphArcRemovalRate
              ),
              5,
              (new Jaccard()).on(i -> i.getGenotype().nodes()),
              0.25,
              individuals -> {
                double[] fitnesses = individuals.stream().mapToDouble(Individual::getFitness).toArray();
                Individual<Graph<IndexedNode<Node>, Double>, RealFunction, Double> r = Misc.first(individuals);
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
        Map.entry("fgraph-seq-ga-noxover", p -> new StandardEvolver<Graph<Node, Double>, RealFunction, Double>(
            FunctionGraph.builder()
                .andThen(MathUtils.fromMultivariateBuilder())
                .andThen(MathUtils.linearScaler((SymbolicRegressionFitness) p.getFitnessFunction())),
            new ShallowSparseFactory(0d, 0d, 1d, p.arity(), 1),
            PartialComparator.from(Double.class).comparing(Individual::getFitness),
            nPop,
            Map.of(
                new NodeAddition<Node, Double>(
                    FunctionNode.sequentialIndexFactory(baseFunctions),
                    (w, r) -> w,
                    (w, r) -> r.nextGaussian()
                ).withChecker(FunctionGraph.checker()), graphNodeAdditionRate,
                new ArcModification<Node, Double>((w, r) -> w + r.nextGaussian(), 1d).withChecker(FunctionGraph.checker()), graphArcMutationRate,
                new ArcAddition<Node, Double>(Random::nextGaussian, false).withChecker(FunctionGraph.checker()), graphArcAdditionRate,
                new ArcRemoval<Node, Double>(
                    node -> (node instanceof Input) || (node instanceof it.units.malelab.jgea.representation.graph.numeric.Constant) || (node instanceof Output)
                ).withChecker(FunctionGraph.checker()), graphArcRemovalRate
            ),
            new Tournament(nTournament),
            new Worst(),
            nPop,
            true
        )),
        Map.entry("fgraph-seq-gadiv", p -> new StandardWithEnforcedDiversityEvolver<Graph<Node, Double>, RealFunction, Double>(
            FunctionGraph.builder()
                .andThen(MathUtils.fromMultivariateBuilder())
                .andThen(MathUtils.linearScaler((SymbolicRegressionFitness) p.getFitnessFunction())),
            new ShallowSparseFactory(0d, 0d, 1d, p.arity(), 1),
            PartialComparator.from(Double.class).comparing(Individual::getFitness),
            nPop,
            Map.of(
                new NodeAddition<Node, Double>(
                    FunctionNode.sequentialIndexFactory(baseFunctions),
                    (w, r) -> w,
                    (w, r) -> r.nextGaussian()
                ).withChecker(FunctionGraph.checker()), graphNodeAdditionRate,
                new ArcModification<Node, Double>((w, r) -> w + r.nextGaussian(), 1d).withChecker(FunctionGraph.checker()), graphArcMutationRate,
                new ArcAddition<Node, Double>(Random::nextGaussian, false).withChecker(FunctionGraph.checker()), graphArcAdditionRate,
                new ArcRemoval<Node, Double>(
                    node -> (node instanceof Input) || (node instanceof it.units.malelab.jgea.representation.graph.numeric.Constant) || (node instanceof Output)
                ).withChecker(FunctionGraph.checker()), graphArcRemovalRate,
                new AlignedCrossover<Node, Double>(
                    (w1, w2, r) -> w1 + (w2 - w1) * (r.nextDouble() * 3d - 1d),
                    node -> (node instanceof Input) || (node instanceof it.units.malelab.jgea.representation.graph.numeric.Constant) || (node instanceof Output),
                    false
                ).withChecker(FunctionGraph.checker()), graphCrossoverRate
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
