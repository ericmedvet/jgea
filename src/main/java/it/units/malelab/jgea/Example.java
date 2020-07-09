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

package it.units.malelab.jgea;

import com.google.common.collect.Range;
import it.units.malelab.jgea.core.Individual;
import it.units.malelab.jgea.core.Problem;
import it.units.malelab.jgea.core.evolver.*;
import it.units.malelab.jgea.core.evolver.stopcondition.Iterations;
import it.units.malelab.jgea.core.evolver.stopcondition.TargetFitness;
import it.units.malelab.jgea.core.fitness.SymbolicRegressionFitness;
import it.units.malelab.jgea.core.listener.Listener;
import it.units.malelab.jgea.core.listener.PrintStreamListener;
import it.units.malelab.jgea.core.listener.collector.*;
import it.units.malelab.jgea.core.order.ParetoDominance;
import it.units.malelab.jgea.core.order.PartialComparator;
import it.units.malelab.jgea.core.selector.Tournament;
import it.units.malelab.jgea.core.selector.Worst;
import it.units.malelab.jgea.core.util.Misc;
import it.units.malelab.jgea.problem.booleanfunction.EvenParity;
import it.units.malelab.jgea.problem.symbolicregression.AbstractRegressionProblemProblemWithValidation;
import it.units.malelab.jgea.problem.symbolicregression.FormulaMapper;
import it.units.malelab.jgea.problem.symbolicregression.MathUtils;
import it.units.malelab.jgea.problem.symbolicregression.Nguyen7;
import it.units.malelab.jgea.problem.symbolicregression.element.Element;
import it.units.malelab.jgea.problem.synthetic.LinearPoints;
import it.units.malelab.jgea.problem.synthetic.OneMax;
import it.units.malelab.jgea.representation.grammar.GrammarBasedProblem;
import it.units.malelab.jgea.representation.grammar.cfggp.RampedHalfAndHalf;
import it.units.malelab.jgea.representation.grammar.cfggp.StandardTreeCrossover;
import it.units.malelab.jgea.representation.grammar.cfggp.StandardTreeMutation;
import it.units.malelab.jgea.representation.sequence.Sequence;
import it.units.malelab.jgea.representation.sequence.UniformCrossover;
import it.units.malelab.jgea.representation.sequence.bit.BitFlipMutation;
import it.units.malelab.jgea.representation.sequence.bit.BitString;
import it.units.malelab.jgea.representation.sequence.bit.BitStringFactory;
import it.units.malelab.jgea.representation.sequence.numeric.GaussianMutation;
import it.units.malelab.jgea.representation.sequence.numeric.GeometricCrossover;
import it.units.malelab.jgea.representation.sequence.numeric.UniformDoubleSequenceFactory;
import it.units.malelab.jgea.representation.tree.Node;

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
    //runGrammarBasedSymbolicRegression();
    //runGrammarBasedSymbolicRegressionMO();
    runGrammarBasedParity();
  }

  public void runLinearPoints() {
    Random r = new Random(1);
    Problem<Sequence<Double>, Double> p = new LinearPoints();
    List<Evolver<Sequence<Double>, Sequence<Double>, Double>> evolvers = List.of(
        new RandomSearch<>(
            Function.identity(),
            new UniformDoubleSequenceFactory(0, 1, 10),
            PartialComparator.from(Double.class).on(Individual::getFitness)
        ),
        new RandomWalk<>(
            Function.identity(),
            new UniformDoubleSequenceFactory(0, 1, 10),
            PartialComparator.from(Double.class).on(Individual::getFitness),
            new GaussianMutation(0.01d)
        ),
        new StandardEvolver<>(
            Function.identity(),
            new UniformDoubleSequenceFactory(0, 1, 10),
            PartialComparator.from(Double.class).on(Individual::getFitness),
            100,
            Map.of(new GeometricCrossover(Range.open(-1d, 2d)).andThen(new GaussianMutation(0.01)), 1d),
            new Tournament(5),
            new Worst(),
            100,
            true
        )
    );
    for (Evolver<Sequence<Double>, Sequence<Double>, Double> evolver : evolvers) {
      System.out.println(evolver.getClass().getSimpleName());
      try {
        Collection<Sequence<Double>> solutions = evolver.solve(
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
            PartialComparator.from(Double.class).on(Individual::getFitness)
        ),
        new RandomWalk<>(
            Function.identity(),
            new BitStringFactory(100),
            PartialComparator.from(Double.class).on(Individual::getFitness),
            new BitFlipMutation(0.01d)
        ),
        new StandardEvolver<>(
            Function.identity(),
            new BitStringFactory(100),
            PartialComparator.from(Double.class).on(Individual::getFitness),
            100,
            Map.of(
                new UniformCrossover<>(Boolean.class), 0.8d,
                new BitFlipMutation(0.01d), 0.2d
            ),
            new Tournament(5),
            new Worst(),
            100,
            true
        ),
        new StandardWithEnforcedDiversity<>(
            Function.identity(),
            new BitStringFactory(100),
            PartialComparator.from(Double.class).on(Individual::getFitness),
            100,
            Map.of(
                new UniformCrossover<>(Boolean.class), 0.8d,
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

  public void runGrammarBasedSymbolicRegression() {
    Random r = new Random(1);
    AbstractRegressionProblemProblemWithValidation p;
    try {
      p = new Nguyen7(1);
    } catch (IOException e) {
      System.err.println(String.format("Cannot load problem due to %s", e));
      return;
    }
    List<Evolver<Node<String>, Node<Element>, Double>> evolvers = List.of(
        new StandardEvolver<>(
            new FormulaMapper().andThen(MathUtils.linearScaler((SymbolicRegressionFitness) p.getFitnessFunction())),
            new RampedHalfAndHalf<>(3, 12, p.getGrammar()),
            PartialComparator.from(Double.class).on(Individual::getFitness),
            100,
            Map.of(
                new StandardTreeCrossover<>(12), 0.8d,
                new StandardTreeMutation<>(12, p.getGrammar()), 0.2d
            ),
            new Tournament(5),
            new Worst(),
            100,
            true
        ),
        new StandardWithEnforcedDiversity<>(
            new FormulaMapper().andThen(MathUtils.linearScaler((SymbolicRegressionFitness) p.getFitnessFunction())),
            new RampedHalfAndHalf<>(3, 12, p.getGrammar()).withOptimisticUniqueness(100),
            PartialComparator.from(Double.class).on(Individual::getFitness),
            100,
            Map.of(
                new StandardTreeCrossover<>(12), 0.8d,
                new StandardTreeMutation<>(12, p.getGrammar()), 0.2d
            ),
            new Tournament(5),
            new Worst(),
            100,
            true,
            1000
        )
    );
    for (Evolver<Node<String>, Node<Element>, Double> evolver : evolvers) {
      System.out.println(evolver.getClass().getSimpleName());
      try {
        Collection<Node<Element>> solutions = evolver.solve(
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

  public void runGrammarBasedSymbolicRegressionMO() {
    Random r = new Random(1);
    AbstractRegressionProblemProblemWithValidation p;
    try {
      p = new Nguyen7(1);
    } catch (IOException e) {
      System.err.println(String.format("Cannot load problem due to %s", e));
      return;
    }
    List<Evolver<Node<String>, Node<Element>, List<Double>>> evolvers = List.of(
        /*new StandardEvolver<>(
            new FormulaMapper(),
            new RampedHalfAndHalf<>(3, 12, p.getGrammar()),
            new ParetoDominance<>(Double.class).on(i -> i.getFitness()),
            100,
            Map.of(
                new StandardTreeCrossover<>(12), 0.8d,
                new StandardTreeMutation<>(12, p.getGrammar()), 0.2d
            ),
            new Tournament(5),
            new Worst(),
            100,
            true
        ),*/
        new StandardWithEnforcedDiversity<>(
            new FormulaMapper().andThen(MathUtils.linearScaler((SymbolicRegressionFitness) p.getFitnessFunction())),
            new RampedHalfAndHalf<>(3, 12, p.getGrammar()).withOptimisticUniqueness(100),
            new ParetoDominance<>(Double.class).on(Individual::getFitness),
            100,
            Map.of(
                new StandardTreeCrossover<>(12), 0.8d,
                new StandardTreeMutation<>(12, p.getGrammar()), 0.2d
            ),
            new Tournament(5),
            new Worst(),
            100,
            true,
            1000
        )
    );
    for (Evolver<Node<String>, Node<Element>, List<Double>> evolver : evolvers) {
      System.out.println(evolver.getClass().getSimpleName());
      try {
        Collection<Node<Element>> solutions = evolver.solve(
            n -> List.of(
                p.getFitnessFunction().apply(n),
                (double) n.size()
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
    GrammarBasedProblem<String, List<Node<it.units.malelab.jgea.problem.booleanfunction.element.Element>>, Double> p;
    try {
      p = new EvenParity(8);
    } catch (IOException e) {
      System.err.println(String.format("Cannot load problem due to %s", e));
      return;
    }
    Evolver<Node<String>, List<Node<it.units.malelab.jgea.problem.booleanfunction.element.Element>>, Double> evolver = new StandardEvolver<>(
        new it.units.malelab.jgea.problem.booleanfunction.FormulaMapper(),
        new RampedHalfAndHalf<>(3, 12, p.getGrammar()),
        PartialComparator.from(Double.class).on(Individual::getFitness),
        100,
        Map.of(
            new StandardTreeCrossover<>(12), 0.8d,
            new StandardTreeMutation<>(12, p.getGrammar()), 0.2d
        ),
        new Tournament(3),
        new Worst(),
        100,
        true
    );
    try {
      Collection<List<Node<it.units.malelab.jgea.problem.booleanfunction.element.Element>>> solutions = evolver.solve(
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

}
