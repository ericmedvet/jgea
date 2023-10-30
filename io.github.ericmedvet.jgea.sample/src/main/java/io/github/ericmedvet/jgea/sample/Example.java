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

package io.github.ericmedvet.jgea.sample;

import static io.github.ericmedvet.jgea.core.listener.NamedFunctions.*;

import com.google.common.collect.Range;
import io.github.ericmedvet.jgea.core.listener.ListenerFactory;
import io.github.ericmedvet.jgea.core.listener.NamedFunction;
import io.github.ericmedvet.jgea.core.listener.TabularPrinter;
import io.github.ericmedvet.jgea.core.problem.QualityBasedProblem;
import io.github.ericmedvet.jgea.core.problem.TotalOrderQualityBasedProblem;
import io.github.ericmedvet.jgea.core.representation.NamedUnivariateRealFunction;
import io.github.ericmedvet.jgea.core.representation.grammar.string.StringGrammar;
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
import io.github.ericmedvet.jgea.core.representation.tree.SameRootSubtreeCrossover;
import io.github.ericmedvet.jgea.core.representation.tree.numeric.TreeBasedUnivariateRealFunction;
import io.github.ericmedvet.jgea.core.selector.Last;
import io.github.ericmedvet.jgea.core.selector.Tournament;
import io.github.ericmedvet.jgea.core.solver.*;
import io.github.ericmedvet.jgea.core.solver.POCPopulationState;
import io.github.ericmedvet.jgea.problem.regression.FormulaMapper;
import io.github.ericmedvet.jgea.problem.regression.MathUtils;
import io.github.ericmedvet.jgea.problem.regression.univariate.UnivariateRegressionFitness;
import io.github.ericmedvet.jgea.problem.regression.univariate.synthetic.Nguyen7;
import io.github.ericmedvet.jgea.problem.regression.univariate.synthetic.SyntheticUnivariateRegressionProblem;
import io.github.ericmedvet.jgea.problem.synthetic.Ackley;
import io.github.ericmedvet.jgea.problem.synthetic.OneMax;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

public class Example {

  public static final List<NamedFunction<? extends State, ?>> BASIC_FUNCTIONS = List.of(
      nOfIterations(),
      nOfBirths(),
      elapsedSeconds(),
      size().of(all()),
      size().of(firsts()),
      size().of(lasts()),
      uniqueness().of(each(genotype())).of(all()),
      uniqueness().of(each(solution())).of(all()),
      uniqueness().of(each(quality())).of(all()),
      size().of(genotype()).of(best()),
      size().of(solution()).of(best()),
      fitnessMappingIteration().of(best())
  );

  public static final List<NamedFunction<? super POCPopulationState<?, ?, ?, ? extends Double>, ?>> DOUBLE_FUNCTIONS =
      List.of(
          //quality().reformat("%5.3f").of(best()),
          //hist(8).of(each(quality())).of(all()),
          //max(Double::compare).reformat("%5.3f").of(each(quality())).of(all())
      );

  public static void main(String[] args) throws SolverException, IOException {
    String problem = args.length > 0 ? args[0] : "oneMax";
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
    ListenerFactory<POCPopulationState<?, ?, ?, ? extends Double>, Map<String, Object>> listenerFactory =
        ListenerFactory.deaf();
    // new TabularPrinter<>(Misc.concat(List.of(BASIC_FUNCTIONS)), List.of());
    List<
        IterativeSolver<
            ? extends POCPopulationState<?, List<Double>, List<Double>, Double>,
            ? super TotalOrderQualityBasedProblem<List<Double>, Double>,
            List<Double>>>
        solvers = new ArrayList<>();
    solvers.add(new RandomSearch<>(
        Function.identity(),
        new FixedLengthListFactory<>(10, new UniformDoubleFactory(0, 1)),
        StopConditions.targetFitness(0d).or(StopConditions.nOfIterations(100))
    ));
    solvers.add(new RandomWalk<>(
        Function.identity(),
        new FixedLengthListFactory<>(10, new UniformDoubleFactory(0, 1)),
        StopConditions.targetFitness(0d).or(StopConditions.nOfIterations(100)),
        new GaussianMutation(0.01d)
    ));
    solvers.add(new StandardEvolver<>(
        Function.identity(),
        new FixedLengthListFactory<>(10, new UniformDoubleFactory(0, 1)),
        100,
        StopConditions.targetFitness(0d).or(StopConditions.nOfIterations(100)),
        Map.of(new HypercubeGeometricCrossover(Range.open(-1d, 2d)).andThen(new GaussianMutation(0.01)), 1d),
        new Tournament(5),
        new Last(),
        100,
        true,
        false
    ));
    solvers.add(new SimpleEvolutionaryStrategy<>(
        Function.identity(),
        new FixedLengthListFactory<>(10, new UniformDoubleFactory(0, 1)),
        100,
        StopConditions.targetFitness(0d).or(StopConditions.nOfIterations(100)),
        25,
        1,
        0.1,
        false
    ));
    for (IterativeSolver<
        ? extends POCPopulationState<?, List<Double>, List<Double>, Double>,
        ? super TotalOrderQualityBasedProblem<List<Double>, Double>,
        List<Double>>
        solver : solvers) {
      System.out.println(solver.getClass().getSimpleName());
      Collection<List<Double>> solutions =
          solver.solve(p, r, executor, listenerFactory.build(Map.of()).deferred(executor));
      System.out.printf(
          "Found %d solutions with " + "%s.%n",
          solutions.size(), solver.getClass().getSimpleName()
      );
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

    ListenerFactory<POCPopulationState<?, ?, BitString, Double>, Map<String, Object>> listenerFactory =
        new TabularPrinter<>(
            List.of(nOfIterations(), elapsedSeconds(), nOfBirths()), List.of(attribute("solver")));
    // new TabularPrinter<>(Misc.concat(List.of(keysFunctions,BASIC_FUNCTIONS)), List.of(attribute("solver")));
    List<
        IterativeSolver<
            ? extends POCPopulationState<?, ?, BitString, Double>,
            QualityBasedProblem<BitString, Double>,
            BitString>>
        solvers = new ArrayList<>();
    solvers.add(new RandomSearch<>(
        Function.identity(),
        new BitStringFactory(size),
        StopConditions.targetFitness(0d).or(StopConditions.nOfIterations(100))
    ));
    solvers.add(new RandomWalk<>(
        Function.identity(),
        new BitStringFactory(size),
        StopConditions.targetFitness(0d).or(StopConditions.nOfIterations(100)),
        new BitStringFlipMutation(0.01d)
    ));
    solvers.add(new StandardEvolver<>(
        Function.identity(),
        new BitStringFactory(size),
        100,
        StopConditions.targetFitness(0d).or(StopConditions.nOfIterations(100)),
        Map.of(new BitStringUniformCrossover(), 0.8d, new BitStringFlipMutation(0.01d), 0.2d),
        new Tournament(5),
        new Last(),
        100,
        true,
        false
    ));
    solvers.add(new StandardWithEnforcedDiversityEvolver<BitString, BitString, Double>(
        Function.identity(),
        new BitStringFactory(size),
        100,
        StopConditions.targetFitness(0d).or(StopConditions.nOfIterations(100)),
        Map.of(new BitStringUniformCrossover(), 0.8d, new BitStringFlipMutation(0.01d), 0.2d),
        new Tournament(5),
        new Last(),
        100,
        true,
        false,
        100
    ));
    for (IterativeSolver<
        ? extends POCPopulationState<?, ?, BitString, Double>,
        QualityBasedProblem<BitString, Double>,
        BitString>
        evolver : solvers) {
      Collection<BitString> solutions = evolver.solve(
          p,
          r,
          executor,
          listenerFactory
              .build(Map.of("solver", evolver.getClass().getSimpleName()))
              .deferred(executor)
      );
      System.out.printf(
          "Found %d solutions with " + "%s.%n",
          solutions.size(), evolver.getClass().getSimpleName()
      );
    }
    listenerFactory.shutdown();
    executor.shutdown();
  }

  public static void runSymbolicRegression() throws IOException, SolverException {
    ExecutorService executor =
        Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() - 1);
    ListenerFactory<? super POCPopulationState<?, ?, ?, ? extends Double>, Void> listenerFactory =
        ListenerFactory.deaf();
    // new TabularPrinter<>(Misc.concat(List.of(BASIC_FUNCTIONS)), List.of());
    Random r = new Random(1);
    SyntheticUnivariateRegressionProblem p = new Nguyen7(UnivariateRegressionFitness.Metric.MSE, 1);
    StringGrammar<String> srGrammar;
    srGrammar = StringGrammar.load(
        StringGrammar.class.getResourceAsStream("/grammars/1d/symbolic-regression-nguyen7.bnf"));
    List<
        IterativeSolver<
            ? extends POCPopulationState<?, ?, NamedUnivariateRealFunction, Double>,
            ? super SyntheticUnivariateRegressionProblem,
            NamedUnivariateRealFunction>>
        solvers = new ArrayList<>();
    solvers.add(new StandardEvolver<>(
        new FormulaMapper()
            .andThen(n -> new TreeBasedUnivariateRealFunction(
                n,
                p.qualityFunction().getDataset().xVarNames(),
                p.qualityFunction().getDataset().yVarNames().get(0)
            ))
            .andThen(MathUtils.linearScaler(p.qualityFunction())),
        new GrammarRampedHalfAndHalf<>(3, 12, srGrammar),
        100,
        StopConditions.nOfIterations(100),
        Map.of(
            new SameRootSubtreeCrossover<>(12),
            0.8d,
            new GrammarBasedSubtreeMutation<>(12, srGrammar),
            0.2d
        ),
        new Tournament(5),
        new Last(),
        100,
        true,
        false
    ));
    solvers.add(new StandardWithEnforcedDiversityEvolver<>(
        new FormulaMapper()
            .andThen(n -> new TreeBasedUnivariateRealFunction(
                n,
                p.qualityFunction().getDataset().xVarNames(),
                p.qualityFunction().getDataset().yVarNames().get(0)
            ))
            .andThen(MathUtils.linearScaler(p.qualityFunction())),
        new GrammarRampedHalfAndHalf<>(3, 12, srGrammar),
        100,
        StopConditions.nOfIterations(100),
        Map.of(
            new SameRootSubtreeCrossover<>(12),
            0.8d,
            new GrammarBasedSubtreeMutation<>(12, srGrammar),
            0.2d
        ),
        new Tournament(5),
        new Last(),
        100,
        true,
        false,
        100
    ));
    for (IterativeSolver<
        ? extends POCPopulationState<?, ?, NamedUnivariateRealFunction, Double>,
        ? super SyntheticUnivariateRegressionProblem,
        NamedUnivariateRealFunction>
        solver : solvers) {
      System.out.println(solver.getClass().getSimpleName());
      Collection<NamedUnivariateRealFunction> solutions =
          solver.solve(p, r, executor, listenerFactory.build(null).deferred(executor));
      System.out.printf(
          "Found %d solutions with %s.%n",
          solutions.size(), solver.getClass().getSimpleName()
      );
    }
    listenerFactory.shutdown();
    executor.shutdown();
  }
}
