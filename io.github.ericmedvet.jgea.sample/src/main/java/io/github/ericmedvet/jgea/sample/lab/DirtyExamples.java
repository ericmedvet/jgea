/*
 * Copyright 2020 Eric Medvet <eric
 * .medvet@gmail.com> (as eric)
 *
 * Licensed under the Apache License,
 *  Version 2.0 (the "License");
 * you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the
 * License at
 *
 *     http://www.apache
 * .org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law
 * or agreed to in writing, software
 * distributed under the License is
 * distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or
 * implied.
 * See the License for the specific
 * language governing permissions and
 * limitations under the License.
 */

package io.github.ericmedvet.jgea.sample.lab;

import com.google.common.collect.Range;
import io.github.ericmedvet.jgea.core.QualityBasedProblem;
import io.github.ericmedvet.jgea.core.TotalOrderQualityBasedProblem;
import io.github.ericmedvet.jgea.core.listener.Listener;
import io.github.ericmedvet.jgea.core.listener.ListenerFactory;
import io.github.ericmedvet.jgea.core.listener.NamedFunction;
import io.github.ericmedvet.jgea.core.listener.TabularPrinter;
import io.github.ericmedvet.jgea.core.representation.grammar.Grammar;
import io.github.ericmedvet.jgea.core.representation.grammar.cfggp.GrammarBasedSubtreeMutation;
import io.github.ericmedvet.jgea.core.representation.grammar.cfggp.GrammarRampedHalfAndHalf;
import io.github.ericmedvet.jgea.core.representation.sequence.FixedLengthListFactory;
import io.github.ericmedvet.jgea.core.representation.sequence.UniformCrossover;
import io.github.ericmedvet.jgea.core.representation.sequence.bit.BitFlipMutation;
import io.github.ericmedvet.jgea.core.representation.sequence.bit.BitString;
import io.github.ericmedvet.jgea.core.representation.sequence.bit.BitStringFactory;
import io.github.ericmedvet.jgea.core.representation.sequence.numeric.GaussianMutation;
import io.github.ericmedvet.jgea.core.representation.sequence.numeric.HypercubeGeometricCrossover;
import io.github.ericmedvet.jgea.core.representation.sequence.numeric.UniformDoubleFactory;
import io.github.ericmedvet.jgea.core.representation.tree.SameRootSubtreeCrossover;
import io.github.ericmedvet.jgea.core.representation.tree.Tree;
import io.github.ericmedvet.jgea.core.selector.Last;
import io.github.ericmedvet.jgea.core.selector.Tournament;
import io.github.ericmedvet.jgea.core.solver.*;
import io.github.ericmedvet.jgea.core.solver.state.POSetPopulationState;
import io.github.ericmedvet.jgea.core.util.Misc;
import io.github.ericmedvet.jgea.problem.booleanfunction.Element;
import io.github.ericmedvet.jgea.problem.booleanfunction.EvenParity;
import io.github.ericmedvet.jgea.problem.regression.FormulaMapper;
import io.github.ericmedvet.jgea.problem.regression.MathUtils;
import io.github.ericmedvet.jgea.problem.regression.symbolic.TreeBasedRealFunction;
import io.github.ericmedvet.jgea.problem.regression.univariate.SyntheticUnivariateRegressionProblem;
import io.github.ericmedvet.jgea.problem.regression.univariate.UnivariateRegressionFitness;
import io.github.ericmedvet.jgea.problem.regression.univariate.synthetic.Nguyen7;
import io.github.ericmedvet.jgea.problem.synthetic.LinearPoints;
import io.github.ericmedvet.jgea.problem.synthetic.OneMax;
import io.github.ericmedvet.jgea.sample.Worker;
import io.github.ericmedvet.jsdynsym.core.numerical.UnivariateRealFunction;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

import static io.github.ericmedvet.jgea.core.listener.NamedFunctions.*;

/**
 * @author eric
 */
public class DirtyExamples extends Worker {

  public final static List<NamedFunction<? super POSetPopulationState<?, ?, ?>, ?>> BASIC_FUNCTIONS =
      List.of(
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
          fitnessMappingIteration().of(best())
      );

  public final static List<NamedFunction<? super POSetPopulationState<?, ?, ? extends Double>, ?>> DOUBLE_FUNCTIONS =
      List.of(
          fitness().reformat("%5.3f").of(best()),
          hist(8).of(each(fitness())).of(all()),
          max(Double::compare).reformat("%5.3f").of(each(fitness())).of(all())
      );

  public DirtyExamples(String[] args) throws FileNotFoundException {
    super(args);
  }

  public static void main(String[] args) throws FileNotFoundException {
    new DirtyExamples(args);
  }

  @Override
  public void run() {
    //runCones();
    //runLinearPoints();
    runOneMax();
    //runSymbolicRegression();
    //runSymbolicRegressionMO();
    //runGrammarBasedParity();
    //runSphere();
    //runRastrigin();
    //runCooperativeOneMax();
  }

  public void runCooperativeOneMax() {
    int size = 1000;
    AbstractPopulationBasedIterativeSolver<POSetPopulationState<BitString, BitString, Double>,
        QualityBasedProblem<BitString, Double>, BitString, BitString, Double> solver =
        new StandardEvolver<POSetPopulationState<BitString, BitString, Double>, QualityBasedProblem<BitString
            , Double>, BitString, BitString, Double>(
            Function.identity(),
            new BitStringFactory(size / 2),
            50,
            StopConditions.targetFitness(0d).or(StopConditions.nOfIterations(100)),
            Map.of(new UniformCrossover<>(new BitStringFactory(5)), 0.8d, new BitFlipMutation(0.01d), 0.2d),
            new Tournament(5),
            new Last(),
            100,
            true,
            true,
            (problem, random) -> new POSetPopulationState<>()
        );
    Random r = new Random(1);
    BiFunction<BitString, BitString, BitString> aggregator = BitString::append;
    CooperativeSolver.Selector<Individual<BitString, BitString, Double>> selector = (p, rnd) -> p.firsts();
    Function<Collection<Double>, Double> qualitiesAggregator = l -> l.stream().findFirst().get();
    CooperativeSolver<
        POSetPopulationState<BitString, BitString, Double>, POSetPopulationState<BitString, BitString, Double>,
        BitString, BitString, BitString, BitString, QualityBasedProblem<BitString, Double>, BitString, Double> cooperativeSolver = new CooperativeSolver<>(
        solver,
        solver,
        aggregator,
        selector,
        selector,
        qualitiesAggregator,
        StopConditions.nOfIterations(100)
    );
    QualityBasedProblem<BitString, Double> problem = new OneMax();

    Listener<CooperativeSolver.State<POSetPopulationState<BitString, BitString, Double>,
        POSetPopulationState<BitString, BitString, Double>,
        BitString, BitString, BitString, BitString, BitString, Double>> stateListener = state ->
        System.out.printf(
            "%d\t%d\t%1.3f\t%1.3f\n",
            state.getNOfIterations(),
            state.getNOfFitnessEvaluations(),
            state.best1().fitness(),
            state.best2().fitness()
        );

    try {
      cooperativeSolver.solve(problem, r, executorService, stateListener);
    } catch (SolverException e) {
      e.printStackTrace();
    }
  }

  public void runGrammarBasedParity() {
    ListenerFactory<POSetPopulationState<?, ?, ? extends Double>, Void> listenerFactory =
        new TabularPrinter<>(Misc.concat(List.of(
            BASIC_FUNCTIONS,
            DOUBLE_FUNCTIONS
        )), List.of());
    Random r = new Random(1);
    EvenParity p;
    try {
      p = new EvenParity(8);
    } catch (IOException e) {
      System.err.printf("Cannot load problem due to" + " %s%n", e);
      return;
    }
    IterativeSolver<POSetPopulationState<Tree<String>,
        List<Tree<Element>>, Double>, EvenParity,
        List<Tree<Element>>> solver = new StandardEvolver<>(
        new io.github.ericmedvet.jgea.problem.booleanfunction.FormulaMapper(),
        new GrammarRampedHalfAndHalf<>(3, 12, p.getGrammar()),
        100,
        StopConditions.nOfIterations(100),
        Map.of(new SameRootSubtreeCrossover<>(12), 0.8d, new GrammarBasedSubtreeMutation<>(12, p.getGrammar()), 0.2d),
        new Tournament(3),
        new Last(),
        100,
        true,
        false,
        (ep, tr) -> new POSetPopulationState<>()
    );
    try {
      Collection<List<Tree<Element>>> solutions = solver.solve(
          p,
          r,
          executorService,
          listenerFactory.build(null).deferred(executorService)
      );
      System.out.printf("Found %d solutions with %s" + ".%n", solutions.size(), solver.getClass().getSimpleName());
    } catch (SolverException e) {
      e.printStackTrace();
    }
  }

  /*public void runCones() {
    Factory<POSetPopulationState<?, ?, List<Double>>, Map<String, Object>> listenerFactory =
        new TabularPrinter<POSetPopulationState<?, ?, List<Double>>, Map<String, Object>>(
            Misc.concat(List.of(BASIC_FUNCTIONS)),
            List.of()
        );
    MultiHomogeneousObjectiveProblem<List<Double>, Double> p = new Cones();
    NsgaII<MultiHomogeneousObjectiveProblem<List<Double>, Double>, List<Double>, List<Double>> solver = new NsgaII<>(
        Function.identity(),
        new FixedLengthListFactory<>(3, new UniformDoubleFactory(5, 10)),
        25,
        StopConditions.nOfIterations(100),
        Map.of(new GeometricCrossover(Range.open(-1d, 2d)).andThen(new GaussianMutation(0.01)), 1d),
        false
    );

    System.out.println(solver.getClass().getSimpleName());
    try {
      Collection<List<Double>> solutions = solver.solve(
          p,
          RandomGenerator.getDefault(),
          executorService,
          listenerFactory.build(Map.of()).deferred(executorService)
      );
      System.out.printf("Found %d solutions with " + "%s.%n", solutions.size(), solver.getClass().getSimpleName());
    } catch (SolverException e) {
      e.printStackTrace();
    }

  }*/

  public void runLinearPoints() {
    ListenerFactory<POSetPopulationState<?, ?, ? extends Double>, Map<String, Object>> listenerFactory =
        new TabularPrinter<>(
            Misc.concat(List.of(BASIC_FUNCTIONS, DOUBLE_FUNCTIONS)),
            List.of()
        );
    Random r = new Random(1);
    TotalOrderQualityBasedProblem<List<Double>, Double> p = new LinearPoints();
    List<IterativeSolver<? extends POSetPopulationState<List<Double>, List<Double>, Double>,
        TotalOrderQualityBasedProblem<List<Double>, Double>, List<Double>>> solvers = new ArrayList<>();
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
    solvers.add(new StandardEvolver<POSetPopulationState<List<Double>, List<Double>, Double>,
        TotalOrderQualityBasedProblem<List<Double>, Double>, List<Double>, List<Double>, Double>(
        Function.identity(),
        new FixedLengthListFactory<>(10, new UniformDoubleFactory(0, 1)),
        100,
        StopConditions.targetFitness(0d).or(StopConditions.nOfIterations(100)),
        Map.of(new HypercubeGeometricCrossover(Range.open(-1d, 2d)).andThen(new GaussianMutation(0.01)), 1d),
        new Tournament(5),
        new Last(),
        100,
        true,
        false,
        (problem, random) -> new POSetPopulationState<>()
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
    for (IterativeSolver<? extends POSetPopulationState<List<Double>, List<Double>, Double>,
        TotalOrderQualityBasedProblem<List<Double>, Double>, List<Double>> solver : solvers) {
      System.out.println(solver.getClass().getSimpleName());
      try {
        Collection<List<Double>> solutions = solver.solve(
            p,
            r,
            executorService,
            listenerFactory.build(Map.of()).deferred(executorService)
        );
        System.out.printf("Found %d solutions with " + "%s.%n", solutions.size(), solver.getClass().getSimpleName());
      } catch (SolverException e) {
        e.printStackTrace();
      }
    }
  }

  public void runOneMax() {
    int size = 1000;
    Random r = new Random(1);
    QualityBasedProblem<BitString, Double> p = new OneMax();
    List<NamedFunction<? super POSetPopulationState<?, ?, ?>, ?>> keysFunctions = List.of();
    ListenerFactory<POSetPopulationState<?, ?, ? extends Double>, Map<String, Object>> listenerFactory =
        ListenerFactory.all(List.of(new TabularPrinter<>(
            Misc.concat(List.of(keysFunctions, BASIC_FUNCTIONS, DOUBLE_FUNCTIONS)),
            List.of(attribute("solver"))
        )));
    List<IterativeSolver<? extends POSetPopulationState<?, BitString, Double>, QualityBasedProblem<BitString, Double>
        , BitString>> solvers = new ArrayList<>();
    solvers.add(new RandomSearch<>(
        Function.identity(),
        new BitStringFactory(size),
        StopConditions.targetFitness(0d).or(StopConditions.nOfIterations(100))
    ));
    solvers.add(new RandomWalk<>(
        Function.identity(),
        new BitStringFactory(size),
        StopConditions.targetFitness(0d).or(StopConditions.nOfIterations(100)),
        new BitFlipMutation(0.01d)
    ));
    solvers.add(new StandardEvolver<POSetPopulationState<BitString, BitString, Double>, QualityBasedProblem<BitString
        , Double>, BitString, BitString, Double>(
        Function.identity(),
        new BitStringFactory(size),
        100,
        StopConditions.targetFitness(0d).or(StopConditions.nOfIterations(100)),
        Map.of(new UniformCrossover<>(new BitStringFactory(size)), 0.8d, new BitFlipMutation(0.01d), 0.2d),
        new Tournament(5),
        new Last(),
        100,
        true,
        false,
        (problem, random) -> new POSetPopulationState<>()
    ));
    solvers.add(new StandardWithEnforcedDiversityEvolver<POSetPopulationState<BitString, BitString, Double>,
        QualityBasedProblem<BitString, Double>, BitString, BitString, Double>(
        Function.identity(),
        new BitStringFactory(size),
        100,
        StopConditions.targetFitness(0d).or(StopConditions.nOfIterations(100)),
        Map.of(new UniformCrossover<>(new BitStringFactory(size)), 0.8d, new BitFlipMutation(0.01d), 0.2d),
        new Tournament(5),
        new Last(),
        100,
        true,
        false,
        (problem, random) -> new POSetPopulationState<>(),
        100
    ));
    for (IterativeSolver<? extends POSetPopulationState<?, BitString, Double>, QualityBasedProblem<BitString, Double>
        , BitString> evolver : solvers) {
      try {
        Collection<BitString> solutions = evolver.solve(
            p,
            r,
            executorService,
            listenerFactory.build(Map.of("solver", evolver.getClass().getSimpleName())).deferred(executorService)
        );
        System.out.printf("Found %d solutions with " + "%s.%n", solutions.size(), evolver.getClass().getSimpleName());
      } catch (SolverException e) {
        e.printStackTrace();
      }
    }
    listenerFactory.shutdown();
  }

  public void runSymbolicRegression() {
    ListenerFactory<? super POSetPopulationState<?, ?, ? extends Double>, Void> listenerFactory =
        new TabularPrinter<>(Misc.concat(
            List.of(BASIC_FUNCTIONS, DOUBLE_FUNCTIONS)), List.of());
    Random r = new Random(1);
    SyntheticUnivariateRegressionProblem p = new Nguyen7(UnivariateRegressionFitness.Metric.MSE, 1);
    Grammar<String> srGrammar;
    try {
      srGrammar = Grammar.fromFile(new File("grammars/symbolic" + "-regression-nguyen7" + ".bnf"));
    } catch (IOException e) {
      e.printStackTrace();
      return;
    }
    List<IterativeSolver<? extends POSetPopulationState<?, UnivariateRealFunction, Double>, SyntheticUnivariateRegressionProblem,
        UnivariateRealFunction>> solvers = new ArrayList<>();
    solvers.add(new StandardEvolver<>(
        new FormulaMapper().andThen(n -> TreeBasedRealFunction.from(
                n,
                "x"
            ))
            .andThen(MathUtils.linearScaler(p.qualityFunction())),
        new GrammarRampedHalfAndHalf<>(3, 12, srGrammar),
        100,
        StopConditions.nOfIterations(100),
        Map.of(new SameRootSubtreeCrossover<>(12), 0.8d, new GrammarBasedSubtreeMutation<>(12, srGrammar), 0.2d),
        new Tournament(5),
        new Last(),
        100,
        true,
        false,
        (srp, rnd) -> new POSetPopulationState<>()
    ));
    solvers.add(new StandardWithEnforcedDiversityEvolver<>(
        new FormulaMapper().andThen(n -> TreeBasedRealFunction.from(
                n,
                "x"
            ))
            .andThen(MathUtils.linearScaler(p.qualityFunction())),
        new GrammarRampedHalfAndHalf<>(3, 12, srGrammar),
        100,
        StopConditions.nOfIterations(100),
        Map.of(new SameRootSubtreeCrossover<>(12), 0.8d, new GrammarBasedSubtreeMutation<>(12, srGrammar), 0.2d),
        new Tournament(5),
        new Last(),
        100,
        true,
        false,
        (srp, rnd) -> new POSetPopulationState<>(),
        100
    ));
    for (IterativeSolver<? extends POSetPopulationState<?, UnivariateRealFunction, Double>, SyntheticUnivariateRegressionProblem,
        UnivariateRealFunction> solver : solvers) {
      System.out.println(solver.getClass().getSimpleName());
      try {
        Collection<UnivariateRealFunction> solutions = solver.solve(
            p,
            r,
            executorService,
            listenerFactory.build(null).deferred(executorService)
        );
        System.out.printf("Found %d solutions with %s.%n", solutions.size(), solver.getClass().getSimpleName());
      } catch (SolverException e) {
        e.printStackTrace();
      }
    }
  }

}
