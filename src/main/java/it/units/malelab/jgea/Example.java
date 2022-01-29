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

package it.units.malelab.jgea;

import com.google.common.collect.Range;
import it.units.malelab.jgea.core.QualityBasedProblem;
import it.units.malelab.jgea.core.TotalOrderQualityBasedProblem;
import it.units.malelab.jgea.core.listener.Listener;
import it.units.malelab.jgea.core.listener.NamedFunction;
import it.units.malelab.jgea.core.listener.TabularPrinter;
import it.units.malelab.jgea.core.selector.Last;
import it.units.malelab.jgea.core.selector.Tournament;
import it.units.malelab.jgea.core.solver.*;
import it.units.malelab.jgea.core.solver.state.POSetPopulationState;
import it.units.malelab.jgea.core.util.Misc;
import it.units.malelab.jgea.problem.booleanfunction.Element;
import it.units.malelab.jgea.problem.booleanfunction.EvenParity;
import it.units.malelab.jgea.problem.symbolicregression.*;
import it.units.malelab.jgea.problem.synthetic.LinearPoints;
import it.units.malelab.jgea.problem.synthetic.OneMax;
import it.units.malelab.jgea.representation.grammar.Grammar;
import it.units.malelab.jgea.representation.grammar.cfggp.GrammarBasedSubtreeMutation;
import it.units.malelab.jgea.representation.grammar.cfggp.GrammarRampedHalfAndHalf;
import it.units.malelab.jgea.representation.sequence.FixedLengthListFactory;
import it.units.malelab.jgea.representation.sequence.UniformCrossover;
import it.units.malelab.jgea.representation.sequence.bit.BitFlipMutation;
import it.units.malelab.jgea.representation.sequence.bit.BitString;
import it.units.malelab.jgea.representation.sequence.bit.BitStringFactory;
import it.units.malelab.jgea.representation.sequence.numeric.GaussianMutation;
import it.units.malelab.jgea.representation.sequence.numeric.GeometricCrossover;
import it.units.malelab.jgea.representation.sequence.numeric.UniformDoubleFactory;
import it.units.malelab.jgea.representation.tree.SameRootSubtreeCrossover;
import it.units.malelab.jgea.representation.tree.Tree;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;

import static it.units.malelab.jgea.core.listener.NamedFunctions.*;

/**
 * @author eric
 */
public class Example extends Worker {

  public final static List<NamedFunction<? super POSetPopulationState<?, ?, ?>, ?>> BASIC_FUNCTIONS = List.of(
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

  public final static List<NamedFunction<? super POSetPopulationState<?, ?, ? extends Double>, ?>> DOUBLE_FUNCTIONS = List.of(
      fitness().reformat("%5.3f").of(best()),
      hist(8).of(each(fitness())).of(all()),
      max(Double::compare).reformat("%5.3f").of(each(fitness())).of(all())
  );

  public Example(String[] args) throws FileNotFoundException {
    super(args);
  }

  public static void main(String[] args) throws FileNotFoundException {
    new Example(args);
  }

  @Override
  public void run() {
    //runLinearPoints();
    //runOneMax();
    runSymbolicRegression();
    //runSymbolicRegressionMO();
    //runGrammarBasedParity();
    //runSphere();
    //runRastrigin();
  }

  public void runGrammarBasedParity() {
    Listener.Factory<POSetPopulationState<?, ?, ? extends Double>> listenerFactory = new TabularPrinter<>(Misc.concat(
        List.of(BASIC_FUNCTIONS, DOUBLE_FUNCTIONS)));
    Random r = new Random(1);
    EvenParity p;
    try {
      p = new EvenParity(8);
    } catch (IOException e) {
      System.err.printf("Cannot load problem due to %s%n", e);
      return;
    }
    IterativeSolver<POSetPopulationState<Tree<String>, List<Tree<Element>>, Double>, EvenParity, List<Tree<Element>>> solver = new StandardEvolver<>(
        new it.units.malelab.jgea.problem.booleanfunction.FormulaMapper(),
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
          listenerFactory.build().deferred(executorService)
      );
      System.out.printf("Found %d solutions with %s.%n", solutions.size(), solver.getClass().getSimpleName());
    } catch (SolverException e) {
      e.printStackTrace();
    }
  }

  public void runLinearPoints() {
    Listener.Factory<POSetPopulationState<?, ?, ? extends Double>> listenerFactory = new TabularPrinter<>(Misc.concat(
        List.of(BASIC_FUNCTIONS, DOUBLE_FUNCTIONS)));
    Random r = new Random(1);
    TotalOrderQualityBasedProblem<List<Double>, Double> p = new LinearPoints();
    List<IterativeSolver<? extends POSetPopulationState<List<Double>, List<Double>, Double>, TotalOrderQualityBasedProblem<List<Double>, Double>, List<Double>>> solvers = new ArrayList<>();
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
    solvers.add(new StandardEvolver<POSetPopulationState<List<Double>, List<Double>, Double>, TotalOrderQualityBasedProblem<List<Double>, Double>, List<Double>, List<Double>, Double>(
        Function.identity(),
        new FixedLengthListFactory<>(10, new UniformDoubleFactory(0, 1)),
        100,
        StopConditions.targetFitness(0d).or(StopConditions.nOfIterations(100)),
        Map.of(new GeometricCrossover(Range.open(-1d, 2d)).andThen(new GaussianMutation(0.01)), 1d),
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
    for (IterativeSolver<? extends POSetPopulationState<List<Double>, List<Double>, Double>, TotalOrderQualityBasedProblem<List<Double>, Double>, List<Double>> solver : solvers) {
      System.out.println(solver.getClass().getSimpleName());
      try {
        Collection<List<Double>> solutions = solver.solve(
            p,
            r,
            executorService,
            listenerFactory.build().deferred(executorService)
        );
        System.out.printf("Found %d solutions with %s.%n", solutions.size(), solver.getClass().getSimpleName());
      } catch (SolverException e) {
        e.printStackTrace();
      }
    }
  }

  public void runOneMax() {
    int size = 1000;
    Random r = new Random(1);
    QualityBasedProblem<BitString, Double> p = new OneMax();
    List<NamedFunction<? super POSetPopulationState<?, ?, ?>, ?>> keysFunctions = List.of(); // TODO restore eventAttribute("evolver", "%20.20s"));
    Listener.Factory<POSetPopulationState<?, ?, ? extends Double>> listenerFactory = Listener.Factory.all(List.of(new TabularPrinter<>(
        Misc.concat(List.of(keysFunctions, BASIC_FUNCTIONS, DOUBLE_FUNCTIONS)))));
    List<IterativeSolver<? extends POSetPopulationState<?, BitString, Double>, QualityBasedProblem<BitString, Double>, BitString>> solvers = new ArrayList<>();
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
    solvers.add(new StandardEvolver<POSetPopulationState<BitString, BitString, Double>, QualityBasedProblem<BitString, Double>, BitString, BitString, Double>(
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
    solvers.add(new StandardWithEnforcedDiversityEvolver<POSetPopulationState<BitString, BitString, Double>, QualityBasedProblem<BitString, Double>, BitString, BitString, Double>(
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
    for (IterativeSolver<? extends POSetPopulationState<?, BitString, Double>, QualityBasedProblem<BitString, Double>, BitString> evolver : solvers) {
      Listener<POSetPopulationState<?, ?, ? extends Double>> listener = Listener.all(List.of(
          //new EventAugmenter(Map.ofEntries(Map.entry("evolver", evolver.getClass().getSimpleName()))), // TODO restore
          listenerFactory.build())).deferred(executorService);
      try {
        Collection<BitString> solutions = evolver.solve(p, r, executorService, listener);
        System.out.printf("Found %d solutions with %s.%n", solutions.size(), evolver.getClass().getSimpleName());
      } catch (SolverException e) {
        e.printStackTrace();
      }
    }
    listenerFactory.shutdown();
  }

  public void runSymbolicRegression() {
    Listener.Factory<? super POSetPopulationState<?, ?, ? extends Double>> listenerFactory = new TabularPrinter<>(Misc.concat(
        List.of(
            BASIC_FUNCTIONS,
            DOUBLE_FUNCTIONS
        )));
    Random r = new Random(1);
    SymbolicRegressionProblem p = new Nguyen7(SymbolicRegressionFitness.Metric.MSE, 1);
    Grammar<String> srGrammar;
    try {
      srGrammar = Grammar.fromFile(new File("grammars/symbolic-regression-nguyen7.bnf"));
    } catch (IOException e) {
      e.printStackTrace();
      return;
    }
    List<IterativeSolver<? extends POSetPopulationState<?, RealFunction, Double>, SymbolicRegressionProblem, RealFunction>> solvers = new ArrayList<>();
    solvers.add(new StandardEvolver<>(
        new FormulaMapper().andThen(n -> TreeBasedRealFunction.from(n, "x"))
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
        new FormulaMapper().andThen(n -> TreeBasedRealFunction.from(n, "x"))
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
    for (IterativeSolver<? extends POSetPopulationState<?, RealFunction, Double>, SymbolicRegressionProblem, RealFunction> solver : solvers) {
      System.out.println(solver.getClass().getSimpleName());
      try {
        Collection<RealFunction> solutions = solver.solve(
            p,
            r,
            executorService,
            listenerFactory.build().deferred(executorService)
        );
        System.out.printf("Found %d solutions with %s.%n", solutions.size(), solver.getClass().getSimpleName());
      } catch (SolverException e) {
        e.printStackTrace();
      }
    }
  }
  
}
