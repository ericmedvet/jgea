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
import it.units.malelab.jgea.core.Individual;
import it.units.malelab.jgea.core.Problem;
import it.units.malelab.jgea.core.evolver.*;
import it.units.malelab.jgea.core.evolver.stopcondition.Iterations;
import it.units.malelab.jgea.core.evolver.stopcondition.TargetFitness;
import it.units.malelab.jgea.core.listener.Listener;
import it.units.malelab.jgea.core.listener.PrintStreamListener;
import it.units.malelab.jgea.core.listener.collector.*;
import it.units.malelab.jgea.core.order.ParetoDominance;
import it.units.malelab.jgea.core.order.PartialComparator;
import it.units.malelab.jgea.core.selector.Tournament;
import it.units.malelab.jgea.core.selector.Worst;
import it.units.malelab.jgea.core.util.Misc;
import it.units.malelab.jgea.core.util.Sized;
import it.units.malelab.jgea.problem.booleanfunction.EvenParity;
import it.units.malelab.jgea.problem.booleanfunction.element.Element;
import it.units.malelab.jgea.problem.symbolicregression.*;
import it.units.malelab.jgea.problem.synthetic.LinearPoints;
import it.units.malelab.jgea.problem.synthetic.OneMax;
import it.units.malelab.jgea.problem.synthetic.Rastrigin;
import it.units.malelab.jgea.problem.synthetic.Sphere;
import it.units.malelab.jgea.representation.grammar.Grammar;
import it.units.malelab.jgea.representation.grammar.GrammarBasedProblem;
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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author eric
 */
public class Example extends Worker {

  public Example(String[] args) throws FileNotFoundException {
    super(args);
  }

  public static void main(String[] args) throws FileNotFoundException {
    new Example(args);
  }

  @Override
  public void run() {
    //runOneMax();
    //runLinearPoints();
    //runSymbolicRegression();
    //runSymbolicRegressionMO();
    //runGrammarBasedParity();
    runSphere();
    //runRastrigin();
  }

  public void runLinearPoints() {
    Random r = new Random(1);
    Problem<List<Double>, Double> p = new LinearPoints();
    List<Evolver<List<Double>, List<Double>, Double>> evolvers = List.of(
        new RandomSearch<>(
            Function.identity(),
            new FixedLengthListFactory<>(10, new UniformDoubleFactory(0, 1)),
            PartialComparator.from(Double.class).comparing(Individual::getFitness)
        ),
        new RandomWalk<>(
            Function.identity(),
            new FixedLengthListFactory<>(10, new UniformDoubleFactory(0, 1)),
            PartialComparator.from(Double.class).comparing(Individual::getFitness),
            new GaussianMutation(0.01d)
        ),
        new StandardEvolver<>(
            Function.identity(),
            new FixedLengthListFactory<>(10, new UniformDoubleFactory(0, 1)),
            PartialComparator.from(Double.class).comparing(Individual::getFitness),
            100,
            Map.of(new GeometricCrossover(Range.open(-1d, 2d)).andThen(new GaussianMutation(0.01)), 1d),
            new Tournament(5),
            new Worst(),
            100,
            true
        )
    );
    for (Evolver<List<Double>, List<Double>, Double> evolver : evolvers) {
      System.out.println(evolver.getClass().getSimpleName());
      try {
        Collection<List<Double>> solutions = evolver.solve(
            p.getFitnessFunction(),
            new TargetFitness<>(0d).or(new Iterations(100)),
            r,
            executorService,
            listener(
                new Basic(),
                new Population(),
                new Diversity(),
                new BestInfo("%5.3f"),
                new FunctionOfOneBest<>(i -> List.of(new Item(
                    "solution",
                    i.getSolution().stream().map(d -> String.format("%5.2f", d)).collect(Collectors.joining(",")),
                    "%s")))
            ));
        System.out.printf("Found %d solutions with %s.%n", solutions.size(), evolver.getClass().getSimpleName());
      } catch (InterruptedException | ExecutionException e) {
        e.printStackTrace();
      }
    }
  }

  public void runOneMax() {
    Random r = new Random(1);
    Problem<BitString, Double> p = new OneMax();
    List<Evolver<BitString, BitString, Double>> evolvers = List.of(
        new RandomSearch<>(
            Function.identity(),
            new BitStringFactory(100),
            PartialComparator.from(Double.class).comparing(Individual::getFitness)
        ),
        new RandomWalk<>(
            Function.identity(),
            new BitStringFactory(100),
            PartialComparator.from(Double.class).comparing(Individual::getFitness),
            new BitFlipMutation(0.01d)
        ),
        new StandardEvolver<>(
            Function.identity(),
            new BitStringFactory(100),
            PartialComparator.from(Double.class).comparing(Individual::getFitness),
            100,
            Map.of(
                new UniformCrossover<>(new BitStringFactory(100)), 0.8d,
                new BitFlipMutation(0.01d), 0.2d
            ),
            new Tournament(5),
            new Worst(),
            100,
            true
        ),
        new StandardWithEnforcedDiversityEvolver<>(
            Function.identity(),
            new BitStringFactory(100),
            PartialComparator.from(Double.class).comparing(Individual::getFitness),
            100,
            Map.of(
                new UniformCrossover<>(new BitStringFactory(100)), 0.8d,
                new BitFlipMutation(0.01d), 0.2d
            ),
            new Tournament(5),
            new Worst(),
            100,
            true,
            100
        )
    );
    for (Evolver<BitString, BitString, Double> evolver : evolvers) {
      System.out.println(evolver.getClass().getSimpleName());
      try {
        Collection<BitString> solutions = evolver.solve(
            Misc.cached(p.getFitnessFunction(), 10000),
            new TargetFitness<>(0d).or(new Iterations(1000)),
            r,
            executorService,
            listener(
                new Basic(),
                new Population(),
                new BestInfo("%5.3f"),
                new BestPrinter(BestPrinter.Part.GENOTYPE)
            ));
        System.out.printf("Found %d solutions with %s.%n", solutions.size(), evolver.getClass().getSimpleName());
      } catch (InterruptedException | ExecutionException e) {
        e.printStackTrace();
      }
    }
  }

  public void runSymbolicRegression() {
    Random r = new Random(1);
    SymbolicRegressionProblem p = new Nguyen7(SymbolicRegressionFitness.Metric.MSE, 1);
    Grammar<String> srGrammar;
    try {
      srGrammar = Grammar.fromFile(new File("grammars/symbolic-regression-nguyen7.bnf"));
    } catch (IOException e) {
      e.printStackTrace();
      return;
    }
    List<Evolver<Tree<String>, RealFunction, Double>> evolvers = List.of(
        new StandardEvolver<>(
            new FormulaMapper()
                .andThen(n -> TreeBasedRealFunction.from(n, "x"))
                .andThen(MathUtils.linearScaler((SymbolicRegressionFitness) p.getFitnessFunction())),
            new GrammarRampedHalfAndHalf<>(3, 12, srGrammar),
            PartialComparator.from(Double.class).comparing(Individual::getFitness),
            100,
            Map.of(
                new SameRootSubtreeCrossover<>(12), 0.8d,
                new GrammarBasedSubtreeMutation<>(12, srGrammar), 0.2d
            ),
            new Tournament(5),
            new Worst(),
            100,
            true
        ),
        new StandardWithEnforcedDiversityEvolver<>(
            new FormulaMapper()
                .andThen(n -> TreeBasedRealFunction.from(n, "x"))
                .andThen(MathUtils.linearScaler((SymbolicRegressionFitness) p.getFitnessFunction())),
            new GrammarRampedHalfAndHalf<>(3, 12, srGrammar).withOptimisticUniqueness(100),
            PartialComparator.from(Double.class).comparing(Individual::getFitness),
            100,
            Map.of(
                new SameRootSubtreeCrossover<>(12), 0.8d,
                new GrammarBasedSubtreeMutation<>(12, srGrammar), 0.2d
            ),
            new Tournament(5),
            new Worst(),
            100,
            true,
            1000
        )
    );
    for (Evolver<Tree<String>, RealFunction, Double> evolver : evolvers) {
      System.out.println(evolver.getClass().getSimpleName());
      try {
        Collection<RealFunction> solutions = evolver.solve(
            Misc.cached(p.getFitnessFunction(), 10000),
            new TargetFitness<>(0d).or(new Iterations(100)),
            r,
            executorService,
            listener(
                new Basic(),
                new Population(),
                new Diversity(),
                new BestInfo("%5.3f"),
                new FunctionOfOneBest<>(i -> List.of(new Item(
                    "validation.fitness",
                    p.getValidationFunction().apply(i.getSolution()),
                    "%5.3f"
                ))),
                new BestPrinter(BestPrinter.Part.SOLUTION)
            ));
        System.out.printf("Found %d solutions with %s.%n", solutions.size(), evolver.getClass().getSimpleName());
      } catch (InterruptedException | ExecutionException e) {
        e.printStackTrace();
      }
    }
  }

  public void runSymbolicRegressionMO() {
    Random r = new Random(1);
    SymbolicRegressionProblem p = new Nguyen7(SymbolicRegressionFitness.Metric.MSE, 1);
    Grammar<String> srGrammar;
    try {
      srGrammar = Grammar.fromFile(new File("grammars/symbolic-regression-nguyen7.bnf"));
    } catch (IOException e) {
      e.printStackTrace();
      return;
    }
    List<Evolver<Tree<String>, RealFunction, List<Double>>> evolvers = List.of(
        new StandardEvolver<>(
            new FormulaMapper()
                .andThen(n -> TreeBasedRealFunction.from(n, "x"))
                .andThen(MathUtils.linearScaler((SymbolicRegressionFitness) p.getFitnessFunction())),
            new GrammarRampedHalfAndHalf<>(3, 12, srGrammar),
            new ParetoDominance<>(Double.class).comparing(Individual::getFitness),
            100,
            Map.of(
                new SameRootSubtreeCrossover<>(12), 0.8d,
                new GrammarBasedSubtreeMutation<>(12, srGrammar), 0.2d
            ),
            new Tournament(5),
            new Worst(),
            100,
            true
        ),
        new StandardWithEnforcedDiversityEvolver<>(
            new FormulaMapper()
                .andThen(n -> TreeBasedRealFunction.from(n, "x"))
                .andThen(MathUtils.linearScaler((SymbolicRegressionFitness) p.getFitnessFunction())),
            new GrammarRampedHalfAndHalf<>(3, 12, srGrammar).withOptimisticUniqueness(100),
            new ParetoDominance<>(Double.class).comparing(Individual::getFitness),
            100,
            Map.of(
                new SameRootSubtreeCrossover<>(12), 0.8d,
                new GrammarBasedSubtreeMutation<>(12, srGrammar), 0.2d
            ),
            new Tournament(5),
            new Worst(),
            100,
            true,
            1000
        )
    );
    for (Evolver<Tree<String>, RealFunction, List<Double>> evolver : evolvers) {
      System.out.println(evolver.getClass().getSimpleName());
      try {
        Collection<RealFunction> solutions = evolver.solve(
            f -> List.of(
                p.getFitnessFunction().apply(f),
                (f instanceof Sized) ? ((Sized) f).size() : Double.POSITIVE_INFINITY
            ),
            new Iterations(3),
            r,
            executorService,
            listener(
                new Basic(),
                new Population(),
                new Diversity(),
                new BestInfo("%5.3f", "%2.0f"),
                new FunctionOfFirsts<>(bests -> List.of(new Item("firsts.fitnesses", bests.stream().map(i -> i.getFitness().toString()).collect(Collectors.joining(", ")), "%s"))),
                new FunctionOfOneBest<>(i -> List.of(new Item(
                    "validation.fitness",
                    p.getValidationFunction().apply(i.getSolution()),
                    "%5.3f"
                ))),
                new BestPrinter(BestPrinter.Part.SOLUTION)
            ));
        System.out.printf("Found %d solutions with %s.%n", solutions.size(), evolver.getClass().getSimpleName());
      } catch (InterruptedException | ExecutionException e) {
        e.printStackTrace();
      }
    }
  }

  public void runGrammarBasedParity() {
    Random r = new Random(1);
    GrammarBasedProblem<String, List<Tree<Element>>, Double> p;
    try {
      p = new EvenParity(8);
    } catch (IOException e) {
      System.err.printf("Cannot load problem due to %s%n", e);
      return;
    }
    Evolver<Tree<String>, List<Tree<Element>>, Double> evolver = new StandardEvolver<>(
        new it.units.malelab.jgea.problem.booleanfunction.FormulaMapper(),
        new GrammarRampedHalfAndHalf<>(3, 12, p.getGrammar()),
        PartialComparator.from(Double.class).comparing(Individual::getFitness),
        100,
        Map.of(
            new SameRootSubtreeCrossover<>(12), 0.8d,
            new GrammarBasedSubtreeMutation<>(12, p.getGrammar()), 0.2d
        ),
        new Tournament(3),
        new Worst(),
        100,
        true
    );
    try {
      Collection<List<Tree<Element>>> solutions = evolver.solve(
          Misc.cached(p.getFitnessFunction(), 10000),
          new Iterations(100),
          r,
          executorService,
          Listener.onExecutor(new PrintStreamListener<>(
              System.out, true, 10, " ", "|",
              new Basic(),
              new Population(),
              new Diversity(),
              new BestInfo("%5.3f")
          ), executorService)
      );
      System.out.printf("Found %d solutions with %s.%n", solutions.size(), evolver.getClass().getSimpleName());
    } catch (InterruptedException | ExecutionException e) {
      e.printStackTrace();
    }
  }

  public void runSphere() {
    Random r = new Random(1);
    Problem<List<Double>, Double> p = new Sphere();
    List<Evolver<List<Double>, List<Double>, Double>> evolvers = List.of(
        new StandardEvolver<>(
            Function.identity(),
            new FixedLengthListFactory<>(10, new UniformDoubleFactory(-10, 10)),
            PartialComparator.from(Double.class).comparing(Individual::getFitness),
            100,
            Map.of(new GeometricCrossover(Range.open(-1d, 2d)).andThen(new GaussianMutation(0.01)), 1d),
            new Tournament(5),
            new Worst(),
            100,
            true
        ),
        new CMAESEvolver<>(
            Function.identity(),
            new FixedLengthListFactory<>(10, new UniformDoubleFactory(-10, 10)),
            PartialComparator.from(Double.class).comparing(Individual::getFitness)
        )
    );
    for (Evolver<List<Double>, List<Double>, Double> evolver : evolvers) {
      System.out.println(evolver.getClass().getSimpleName());
      try {
        Collection<List<Double>> solutions = evolver.solve(
            p.getFitnessFunction(),
            new TargetFitness<>(0d).or(new Iterations(100)),
            r,
            executorService,
            listener(
                new Basic(),
                new Population(),
                new Diversity(),
                new BestInfo("%5.3f"),
                new FunctionOfOneBest<>(i -> List.of(new Item(
                    "solution",
                    i.getSolution().stream().map(d -> String.format("%5.2f", d)).collect(Collectors.joining(",")),
                    "%s")))
            ));
        System.out.printf("Found %d solutions with %s.%n", solutions.size(), evolver.getClass().getSimpleName());
      } catch (InterruptedException | ExecutionException e) {
        e.printStackTrace();
      }
    }
  }

  public void runRastrigin() {
    Random r = new Random(1);
    Problem<List<Double>, Double> p = new Rastrigin();
    List<Evolver<List<Double>, List<Double>, Double>> evolvers = List.of(
        new StandardEvolver<>(
            Function.identity(),
            new FixedLengthListFactory<>(10, new UniformDoubleFactory(-5.12, 5.12)),
            PartialComparator.from(Double.class).comparing(Individual::getFitness),
            100,
            Map.of(new GeometricCrossover(Range.open(-1d, 2d)).andThen(new GaussianMutation(0.01)), 1d),
            new Tournament(5),
            new Worst(),
            100,
            true
        ),
        new CMAESEvolver<>(
            Function.identity(),
            new FixedLengthListFactory<>(10, new UniformDoubleFactory(-5.12, 5.12)),
            PartialComparator.from(Double.class).comparing(Individual::getFitness)
        )
    );
    for (Evolver<List<Double>, List<Double>, Double> evolver : evolvers) {
      System.out.println(evolver.getClass().getSimpleName());
      try {
        Collection<List<Double>> solutions = evolver.solve(
            p.getFitnessFunction(),
            new TargetFitness<>(0d).or(new Iterations(100)),
            r,
            executorService,
            listener(
                new Basic(),
                new Population(),
                new Diversity(),
                new BestInfo("%5.3f"),
                new FunctionOfOneBest<>(i -> List.of(new Item(
                    "solution",
                    i.getSolution().stream().map(d -> String.format("%5.2f", d)).collect(Collectors.joining(",")),
                    "%s")))
            ));
        System.out.printf("Found %d solutions with %s.%n", solutions.size(), evolver.getClass().getSimpleName());
      } catch (InterruptedException | ExecutionException e) {
        e.printStackTrace();
      }
    }
  }
}
