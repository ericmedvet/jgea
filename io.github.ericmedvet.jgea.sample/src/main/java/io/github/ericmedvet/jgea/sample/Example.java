/*-
 * ========================LICENSE_START=================================
 * jgea-sample
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

package io.github.ericmedvet.jgea.sample;

import static io.github.ericmedvet.jgea.core.listener.NamedFunctions.*;

import io.github.ericmedvet.jgea.core.IndependentFactory;
import io.github.ericmedvet.jgea.core.listener.ListenerFactory;
import io.github.ericmedvet.jgea.core.listener.NamedFunction;
import io.github.ericmedvet.jgea.core.listener.NamedFunctions;
import io.github.ericmedvet.jgea.core.listener.TabularPrinter;
import io.github.ericmedvet.jgea.core.operator.GeneticOperator;
import io.github.ericmedvet.jgea.core.problem.Problem;
import io.github.ericmedvet.jgea.core.problem.QualityBasedProblem;
import io.github.ericmedvet.jgea.core.problem.TotalOrderQualityBasedProblem;
import io.github.ericmedvet.jgea.core.representation.NamedUnivariateRealFunction;
import io.github.ericmedvet.jgea.core.representation.grammar.string.StringGrammar;
import io.github.ericmedvet.jgea.core.representation.grammar.string.SymbolicRegressionGrammar;
import io.github.ericmedvet.jgea.core.representation.grammar.string.cfggp.GrammarBasedSubtreeMutation;
import io.github.ericmedvet.jgea.core.representation.grammar.string.cfggp.GrammarRampedHalfAndHalf;
import io.github.ericmedvet.jgea.core.representation.sequence.FixedLengthListFactory;
import io.github.ericmedvet.jgea.core.representation.sequence.bit.BitString;
import io.github.ericmedvet.jgea.core.representation.sequence.bit.BitStringFactory;
import io.github.ericmedvet.jgea.core.representation.sequence.bit.BitStringFlipMutation;
import io.github.ericmedvet.jgea.core.representation.sequence.bit.BitStringUniformCrossover;
import io.github.ericmedvet.jgea.core.representation.sequence.numeric.GaussianMutation;
import io.github.ericmedvet.jgea.core.representation.sequence.numeric.HypercubeGeometricCrossover;
import io.github.ericmedvet.jgea.core.representation.sequence.numeric.UniformDoubleFactory;
import io.github.ericmedvet.jgea.core.representation.tree.*;
import io.github.ericmedvet.jgea.core.representation.tree.numeric.Element;
import io.github.ericmedvet.jgea.core.representation.tree.numeric.TreeBasedUnivariateRealFunction;
import io.github.ericmedvet.jgea.core.selector.Last;
import io.github.ericmedvet.jgea.core.selector.Tournament;
import io.github.ericmedvet.jgea.core.solver.*;
import io.github.ericmedvet.jgea.core.util.Misc;
import io.github.ericmedvet.jgea.problem.regression.FormulaMapper;
import io.github.ericmedvet.jgea.problem.regression.MathUtils;
import io.github.ericmedvet.jgea.problem.regression.univariate.UnivariateRegressionFitness;
import io.github.ericmedvet.jgea.problem.regression.univariate.synthetic.Nguyen7;
import io.github.ericmedvet.jgea.problem.regression.univariate.synthetic.SyntheticUnivariateRegressionProblem;
import io.github.ericmedvet.jgea.problem.synthetic.OneMax;
import io.github.ericmedvet.jgea.problem.synthetic.numerical.Ackley;
import io.github.ericmedvet.jnb.datastructure.DoubleRange;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class Example {

  public static final List<NamedFunction<State<Problem<Object>, Object>, ?>> PLAIN_STATE_FUNCTIONS =
      List.of(nOfIterations(), elapsedSeconds());

  public static final List<
          NamedFunction<
              POCPopulationState<
                  Individual<Object, Object, Object>,
                  Object,
                  Object,
                  Object,
                  QualityBasedProblem<Object, Object>>,
              ?>>
      BASIC_FUNCTIONS = List.of(
          nOfBirths(),
          all().then(size()),
          firsts().then(size()),
          lasts().then(size()),
          all().then(each(genotype())).then(uniqueness()),
          all().then(each(solution())).then(uniqueness()),
          all().then(each(quality())).then(uniqueness()),
          best().then(genotype()).then(size()),
          best().then(solution()).then(size()),
          best().then(fitnessMappingIteration()));

  public static final List<
          NamedFunction<
              POCPopulationState<
                  Individual<Object, Object, Double>,
                  Object,
                  Object,
                  Double,
                  QualityBasedProblem<Object, Double>>,
              ?>>
      DOUBLE_FUNCTIONS = List.of(
          NamedFunctions
              .<Individual<Object, Object, Double>, Object, Object, Double,
                  QualityBasedProblem<Object, Double>>
                  best()
              .then(quality())
              .reformat("%5.3f"),
          NamedFunctions
              .<Individual<Object, Object, Double>, Object, Object, Double,
                  QualityBasedProblem<Object, Double>>
                  all()
              .then(each(quality()))
              .then(hist(8)),
          NamedFunctions
              .<Individual<Object, Object, Double>, Object, Object, Double,
                  QualityBasedProblem<Object, Double>>
                  all()
              .then(each(quality()))
              .then(max(Double::compare))
              .reformat("%5.3f"));

  public static void main(String[] args) throws SolverException, IOException {
    String problem = args.length > 0 ? args[0] : "symbolicRegression";
    if (problem.equals("oneMax")) {
      runOneMax();
    }
    if (problem.equals("symbolicRegression")) {
      runSymbolicRegression();
    }
    if (problem.equals("ackley")) {
      runAckley();
    }
  }

  public static void runAckley() throws SolverException {
    ExecutorService executor =
        Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() - 1);
    Random r = new Random(1);
    TotalOrderQualityBasedProblem<List<Double>, Double> p = new Ackley(10);
    @SuppressWarnings({"unchecked", "rawtypes"})
    ListenerFactory<POCPopulationState<?, ?, List<Double>, Double, ?>, Map<String, Object>> listenerFactory =
        new TabularPrinter<>(
            Misc.concat(
                List.of((List) PLAIN_STATE_FUNCTIONS, (List) BASIC_FUNCTIONS, (List) DOUBLE_FUNCTIONS)),
            List.of(attribute("solver")));
    List<
            IterativeSolver<
                ? extends POCPopulationState<?, List<Double>, List<Double>, Double, ?>,
                ? super TotalOrderQualityBasedProblem<List<Double>, Double>,
                List<Double>>>
        solvers = new ArrayList<>();
    @SuppressWarnings({"unchecked", "rawtypes"})
    Predicate<POCPopulationState<?, ?, ?, Double, ?>> stopCondition =
        (Predicate) StopConditions.targetFitness(0d).or(StopConditions.nOfIterations(100));
    solvers.add(new RandomSearch<>(
        Function.identity(), new FixedLengthListFactory<>(10, new UniformDoubleFactory(0, 1)), stopCondition));
    solvers.add(new RandomWalk<>(
        Function.identity(),
        new FixedLengthListFactory<>(10, new UniformDoubleFactory(0, 1)),
        stopCondition,
        new GaussianMutation(0.01d)));
    solvers.add(new StandardEvolver<>(
        Function.identity(),
        new FixedLengthListFactory<>(10, new UniformDoubleFactory(0, 1)),
        100,
        stopCondition,
        Map.of(
            new HypercubeGeometricCrossover(new DoubleRange(-1d, 2d)).andThen(new GaussianMutation(0.01)),
            1d),
        new Tournament(5),
        new Last(),
        100,
        true,
        0,
        false));
    solvers.add(new SimpleEvolutionaryStrategy<>(
        Function.identity(),
        new FixedLengthListFactory<>(10, new UniformDoubleFactory(0, 1)),
        100,
        stopCondition,
        25,
        1,
        0.1,
        false));
    for (IterativeSolver<
            ? extends POCPopulationState<?, List<Double>, List<Double>, Double, ?>,
            ? super TotalOrderQualityBasedProblem<List<Double>, Double>,
            List<Double>>
        solver : solvers) {
      System.out.println(solver.getClass().getSimpleName());
      Collection<List<Double>> solutions = solver.solve(
          p,
          r,
          executor,
          listenerFactory
              .build(Map.of("solver", solver.getClass().getSimpleName()))
              .deferred(executor));
      System.out.printf(
          "Found %d solutions with " + "%s.%n",
          solutions.size(), solver.getClass().getSimpleName());
    }
    listenerFactory.shutdown();
    executor.shutdown();
  }

  public static void runOneMax() throws SolverException {
    ExecutorService executor =
        Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() - 1);
    int size = 1000;
    Random r = new Random(1);
    QualityBasedProblem<BitString, Double> p = new OneMax(size);
    @SuppressWarnings({"unchecked", "rawtypes"})
    ListenerFactory<POCPopulationState<?, ?, BitString, Double, ?>, Map<String, Object>> listenerFactory =
        new TabularPrinter<>(
            Misc.concat(
                List.of((List) PLAIN_STATE_FUNCTIONS, (List) BASIC_FUNCTIONS, (List) DOUBLE_FUNCTIONS)),
            List.of(attribute("solver")));
    List<
            IterativeSolver<
                ? extends POCPopulationState<?, ?, BitString, Double, ?>,
                QualityBasedProblem<BitString, Double>,
                BitString>>
        solvers = new ArrayList<>();
    //noinspection unchecked
    @SuppressWarnings({"unchecked", "rawtypes"})
    Predicate<POCPopulationState<?, ?, ?, Double, ?>> stopCondition =
        (Predicate) StopConditions.targetFitness(0d).or(StopConditions.nOfIterations(100));
    solvers.add(new RandomSearch<>(Function.identity(), new BitStringFactory(size), stopCondition));
    solvers.add(new RandomWalk<>(
        Function.identity(), new BitStringFactory(size), stopCondition, new BitStringFlipMutation(0.01d)));
    solvers.add(new StandardEvolver<>(
        Function.identity(),
        new BitStringFactory(size),
        100,
        stopCondition,
        Map.of(new BitStringUniformCrossover(), 0.8d, new BitStringFlipMutation(0.01d), 0.2d),
        new Tournament(5),
        new Last(),
        100,
        true,
        0,
        false));
    for (IterativeSolver<
            ? extends POCPopulationState<?, ?, BitString, Double, ?>,
            QualityBasedProblem<BitString, Double>,
            BitString>
        solver : solvers) {
      Collection<BitString> solutions = solver.solve(
          p,
          r,
          executor,
          listenerFactory
              .build(Map.of("solver", solver.getClass().getSimpleName()))
              .deferred(executor));
      System.out.printf(
          "Found %d solutions with " + "%s.%n",
          solutions.size(), solver.getClass().getSimpleName());
    }
    listenerFactory.shutdown();
    executor.shutdown();
  }

  public static void runSymbolicRegression() throws IOException, SolverException {
    ExecutorService executor =
        Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() - 1);
    @SuppressWarnings({"unchecked", "rawtypes"})
    ListenerFactory<POCPopulationState<?, ?, NamedUnivariateRealFunction, Double, ?>, Map<String, Object>>
        listenerFactory = new TabularPrinter<>(
            Misc.concat(
                List.of((List) PLAIN_STATE_FUNCTIONS, (List) BASIC_FUNCTIONS, (List) DOUBLE_FUNCTIONS)),
            List.of(attribute("solver")));
    Random r = new Random(1);
    SyntheticUnivariateRegressionProblem p = new Nguyen7(UnivariateRegressionFitness.Metric.MSE, 1);
    StringGrammar<String> srGrammar = new SymbolicRegressionGrammar(
        List.of(
            Element.Operator.ADDITION,
            Element.Operator.MULTIPLICATION,
            Element.Operator.SUBTRACTION,
            Element.Operator.PROT_DIVISION),
        List.of("x1"),
        List.of(0.1, 1d, 5d));
    List<
            IterativeSolver<
                ? extends POCPopulationState<?, ?, NamedUnivariateRealFunction, Double, ?>,
                ? super SyntheticUnivariateRegressionProblem,
                NamedUnivariateRealFunction>>
        solvers = new ArrayList<>();
    solvers.add(new StandardEvolver<>(
        new FormulaMapper()
            .andThen(n -> new TreeBasedUnivariateRealFunction(
                n,
                p.qualityFunction().getDataset().xVarNames(),
                p.qualityFunction().getDataset().yVarNames().get(0)))
            .andThen(MathUtils.linearScaler(p.qualityFunction())),
        new GrammarRampedHalfAndHalf<>(3, 12, srGrammar),
        100,
        StopConditions.nOfIterations(100),
        Map.of(
            new SameRootSubtreeCrossover<>(12),
            0.8d,
            new GrammarBasedSubtreeMutation<>(12, srGrammar),
            0.2d),
        new Tournament(5),
        new Last(),
        100,
        true,
        0,
        false));
    solvers.add(new StandardEvolver<>(
        new FormulaMapper()
            .andThen(n -> new TreeBasedUnivariateRealFunction(
                n,
                p.qualityFunction().getDataset().xVarNames(),
                p.qualityFunction().getDataset().yVarNames().get(0))),
        new GrammarRampedHalfAndHalf<>(3, 12, srGrammar),
        100,
        StopConditions.nOfIterations(100),
        Map.of(
            new SameRootSubtreeCrossover<>(12),
            0.8d,
            new GrammarBasedSubtreeMutation<>(12, srGrammar),
            0.2d),
        new Tournament(5),
        new Last(),
        100,
        true,
        0,
        false));
    for (IterativeSolver<
            ? extends POCPopulationState<?, ?, NamedUnivariateRealFunction, Double, ?>,
            ? super SyntheticUnivariateRegressionProblem,
            NamedUnivariateRealFunction>
        solver : solvers) {
      System.out.println(solver.getClass().getSimpleName());
      Collection<NamedUnivariateRealFunction> solutions = solver.solve(
          p,
          r,
          executor,
          listenerFactory
              .build(Map.of("solver", solver.getClass().getSimpleName()))
              .deferred(executor));
      System.out.printf(
          "Found %d solutions with %s.%n",
          solutions.size(), solver.getClass().getSimpleName());
    }
    listenerFactory.shutdown();
    executor.shutdown();
  }

  public static void runNguyen7() throws SolverException {
    ExecutorService executor =
        Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() - 1);
    @SuppressWarnings({"unchecked", "rawtypes"})
    ListenerFactory<POCPopulationState<?, ?, NamedUnivariateRealFunction, Double, ?>, Void> listenerFactory =
        new TabularPrinter<>(
            (List) List.of(
                nOfIterations(),
                elapsedSeconds(),
                nOfBirths(),
                all().then(size()),
                firsts().then(size()),
                lasts().then(size()),
                all().then(each(genotype())).then(uniqueness()),
                all().then(each(solution())).then(uniqueness()),
                all().then(each(quality())).then(uniqueness()),
                best().then(genotype()).then(size()),
                best().then(solution()).then(size()),
                best().then(fitnessMappingIteration()),
                NamedFunctions
                    .<Individual<Object, Object, Double>, Object, Object, Double,
                        QualityBasedProblem<Object, Double>>
                        best()
                    .then(quality())
                    .reformat("%5.3f"),
                NamedFunctions
                    .<Individual<Object, Object, Double>, Object, Object, Double,
                        QualityBasedProblem<Object, Double>>
                        all()
                    .then(each(quality()))
                    .then(hist(8)),
                best().then(solution()).reformat("%20.20s")),
            List.of());
    Random r = new Random(1);
    SyntheticUnivariateRegressionProblem p = new Nguyen7(UnivariateRegressionFitness.Metric.MSE, 1);
    List<Element.Variable> variables = p.qualityFunction().getDataset().xVarNames().stream()
        .map(Element.Variable::new)
        .toList();
    List<Element.Constant> constantElements =
        Stream.of(0.1, 1d, 10d).map(Element.Constant::new).toList();
    IndependentFactory<Element> terminalFactory = IndependentFactory.oneOf(
        IndependentFactory.picker(variables), IndependentFactory.picker(constantElements));
    IndependentFactory<Element> nonTerminalFactory = IndependentFactory.picker(List.of(
        Element.Operator.ADDITION,
        Element.Operator.SUBTRACTION,
        Element.Operator.MULTIPLICATION,
        Element.Operator.PROT_DIVISION));
    TreeBuilder<Element> treeBuilder = new GrowTreeBuilder<>(x -> 2, nonTerminalFactory, terminalFactory);
    // operators
    Map<GeneticOperator<Tree<Element>>, Double> geneticOperators = Map.ofEntries(
        Map.entry(new SubtreeCrossover<>(10), 0.80), Map.entry(new SubtreeMutation<>(10, treeBuilder), 0.20));
    StandardEvolver<Tree<Element>, NamedUnivariateRealFunction, Double> solver = new StandardEvolver<>(
        t -> new TreeBasedUnivariateRealFunction(
            t,
            p.qualityFunction().getDataset().xVarNames(),
            p.qualityFunction().getDataset().yVarNames().get(0)),
        new RampedHalfAndHalf<>(4, 10, x -> 2, nonTerminalFactory, terminalFactory),
        100,
        StopConditions.nOfFitnessEvaluations(10000),
        geneticOperators,
        new Tournament(5),
        new Last(),
        100,
        true,
        0,
        false);
    Collection<NamedUnivariateRealFunction> solutions = solver.solve(p, r, executor, listenerFactory.build(null));
    System.out.printf("Found %d solutions%n", solutions.size());
  }
}
