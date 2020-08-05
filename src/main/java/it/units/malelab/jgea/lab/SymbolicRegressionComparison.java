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

import it.units.malelab.jgea.Example;
import it.units.malelab.jgea.Worker;
import it.units.malelab.jgea.core.IndependentFactory;
import it.units.malelab.jgea.core.Individual;
import it.units.malelab.jgea.core.evolver.Evolver;
import it.units.malelab.jgea.core.evolver.StandardEvolver;
import it.units.malelab.jgea.core.evolver.stopcondition.FitnessEvaluations;
import it.units.malelab.jgea.core.evolver.stopcondition.Iterations;
import it.units.malelab.jgea.core.evolver.stopcondition.TargetFitness;
import it.units.malelab.jgea.core.listener.collector.*;
import it.units.malelab.jgea.core.order.PartialComparator;
import it.units.malelab.jgea.core.selector.Tournament;
import it.units.malelab.jgea.core.selector.Worst;
import it.units.malelab.jgea.core.util.Misc;
import it.units.malelab.jgea.problem.symbolicregression.*;
import it.units.malelab.jgea.problem.symbolicregression.element.Constant;
import it.units.malelab.jgea.problem.symbolicregression.element.Element;
import it.units.malelab.jgea.problem.symbolicregression.element.Operator;
import it.units.malelab.jgea.problem.symbolicregression.element.Variable;
import it.units.malelab.jgea.representation.tree.*;

import java.io.FileNotFoundException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

/**
 * @author eric
 * @created 2020/08/05
 * @project jgea
 */
public class SymbolicRegressionComparison extends Worker {

  public SymbolicRegressionComparison(String[] args) throws FileNotFoundException {
    super(args);
  }

  public static void main(String[] args) throws FileNotFoundException {
    new SymbolicRegressionComparison(args);
  }

  @Override
  public void run() {
    int nPop = 100;
    int nTournament = 5;
    int[] seeds = new int[]{0};
    Element[] nonTerminals = Operator.values();
    Element[] constants = new Element[]{
        new Variable("x"),
        new Constant(0.1),
        new Constant(1),
        new Constant(10)
    };
    List<SymbolicRegressionProblem> problems = List.of(
        new Nguyen7(1),
        new Keijzer6(),
        new Polynomial4()
    );
    Map<String, Function<SymbolicRegressionProblem, Evolver<?, RealFunction, Double>>> evolvers = Map.of(
        "tree-gp", p -> new StandardEvolver<Tree<Element>, RealFunction, Double>(
            ((Function<Tree<Element>, RealFunction>) t -> new TreeBasedRealFunction(t, vars(p.getArity())))
                .andThen(MathUtils.linearScaler((SymbolicRegressionFitness) p.getFitnessFunction())),
            new RampedHalfAndHalf<>(
                4, 10,
                Operator.arityFunction(),
                IndependentFactory.picker(nonTerminals),
                IndependentFactory.oneOf(
                    IndependentFactory.picker(Arrays.stream(vars(p.getArity())).sequential().map(Variable::new).toArray(Variable[]::new)),
                    IndependentFactory.picker(constants)
                )
            ),
            PartialComparator.from(Double.class).on(Individual::getFitness),
            nPop,
            Map.of(
                new SubtreeCrossover<>(10), 0.8d,
                new SubtreeMutation<>(10, new GrowTreeBuilder<>(
                    Operator.arityFunction(),
                    IndependentFactory.picker(nonTerminals),
                    IndependentFactory.oneOf(
                        IndependentFactory.picker(Arrays.stream(vars(p.getArity())).sequential().map(Variable::new).toArray(Variable[]::new)),
                        IndependentFactory.picker(constants)
                    )
                )), 0.2d
            ),
            new Tournament(nTournament),
            new Worst(),
            nPop,
            true
        )
    );
    //run
    for (int seed : seeds) {
      for (SymbolicRegressionProblem problem : problems) {
        for (Map.Entry<String, Function<SymbolicRegressionProblem, Evolver<?, RealFunction, Double>>> evolverEntry : evolvers.entrySet()) {
          System.out.printf("Seed: %2d\tProblem: %s\tEvolver: %s%n",
              seed,
              problem.getClass().getSimpleName(),
              evolverEntry.getKey()
          );
          try {
            Evolver<?, RealFunction, Double> evolver = evolverEntry.getValue().apply(problem);
            Collection<RealFunction> solutions = evolver.solve(
                Misc.cached(problem.getFitnessFunction(), 10000),
                new TargetFitness<>(0d).or(new Iterations(100)),
                new Random(seed),
                executorService,
                listener(
                    new Basic(),
                    new Population(),
                    new Diversity(),
                    new BestInfo("%7.5f"),
                    new FunctionOfOneBest<>(i -> List.of(new Item(
                        "validation.fitness",
                        problem.getValidationFunction().apply(i.getSolution()),
                        "%7.5f"
                    ))),
                    new BestPrinter(BestPrinter.Part.SOLUTION, "%50.50s")
                ));
          } catch (InterruptedException | ExecutionException e) {
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
