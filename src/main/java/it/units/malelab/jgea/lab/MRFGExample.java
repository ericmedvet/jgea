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

import com.google.common.graph.ValueGraph;
import it.units.malelab.jgea.Worker;
import it.units.malelab.jgea.core.Individual;
import it.units.malelab.jgea.core.evolver.*;
import it.units.malelab.jgea.core.evolver.stopcondition.FitnessEvaluations;
import it.units.malelab.jgea.core.evolver.stopcondition.TargetFitness;
import it.units.malelab.jgea.core.listener.Listener;
import it.units.malelab.jgea.core.listener.collector.*;
import it.units.malelab.jgea.core.operator.Mutation;
import it.units.malelab.jgea.core.order.PartialComparator;
import it.units.malelab.jgea.core.selector.Tournament;
import it.units.malelab.jgea.core.selector.Worst;
import it.units.malelab.jgea.core.util.Misc;
import it.units.malelab.jgea.problem.symbolicregression.*;
import it.units.malelab.jgea.representation.graph.*;
import it.units.malelab.jgea.representation.graph.multivariatefunction.*;

import java.io.FileNotFoundException;
import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * @author eric
 */
public class MRFGExample extends Worker {

  public MRFGExample(String[] args) throws FileNotFoundException {
    super(args);
  }

  public static void main(String[] args) throws FileNotFoundException {
    new MRFGExample(args);
  }

  @Override
  public void run() {
    Random r = new Random(1);
    int maxNodes = 20;
    SymbolicRegressionProblem p = new Nguyen7(SymbolicRegressionFitness.Metric.MSE, 1);
    List<Evolver<ValueGraph<Node, Double>, RealFunction, Double>> evolvers = List.of(
        new RandomSearch<ValueGraph<Node, Double>, RealFunction, Double>(
            MultivariateRealFunctionGraph.builder()
                .andThen(GraphBasedRealFunction.builder())
                .andThen(MathUtils.linearScaler((SymbolicRegressionFitness) p.getFitnessFunction())),
            new ShallowGraphFactory(0.5d, 0d, 1d, p.arity(), 1),
            PartialComparator.from(Double.class).on(Individual::getFitness)
        ),
        new RandomWalk<>(
            MultivariateRealFunctionGraph.builder()
                .andThen(GraphBasedRealFunction.builder())
                .andThen(MathUtils.linearScaler((SymbolicRegressionFitness) p.getFitnessFunction())),
            new ShallowGraphFactory(0.5d, 0d, 1d, p.arity(), 1),
            PartialComparator.from(Double.class).on(Individual::getFitness),
            Mutation.oneOf(Map.of(
                new NodeAddition<>(
                    FunctionNode.limitedIndexFactory(maxNodes, BaseFunction.RE_LU, BaseFunction.GAUSSIAN, BaseFunction.SIN),
                    Random::nextGaussian
                ), 1d,
                new EdgeModification<>((w, random) -> w + random.nextGaussian()), 1d,
                new EdgeAddition<>(Random::nextGaussian, false), 1d,
                new EdgeRemoval<>(node -> node instanceof Output), 0.1d
            ))
        ),
        new StandardEvolver<>(
            MultivariateRealFunctionGraph.builder()
                .andThen(GraphBasedRealFunction.builder())
                .andThen(MathUtils.linearScaler((SymbolicRegressionFitness) p.getFitnessFunction())),
            new ShallowGraphFactory(0.5d, 0d, 1d, p.arity(), 1),
            PartialComparator.from(Double.class).on(Individual::getFitness),
            100,
            Map.of(
                new NodeAddition<>(
                    FunctionNode.limitedIndexFactory(maxNodes, BaseFunction.RE_LU, BaseFunction.GAUSSIAN, BaseFunction.SIN),
                    Random::nextGaussian
                ), 2d,
                new EdgeModification<>((w, random) -> w + random.nextGaussian()), 1d,
                new EdgeAddition<>(Random::nextGaussian, false), 1d,
                new EdgeRemoval<>(node -> node instanceof Output), 0.1d
            ),
            new Tournament(5),
            new Worst(),
            100,
            true
        ),
        new StandardEvolver<>(
            MultivariateRealFunctionGraph.builder()
                .andThen(GraphBasedRealFunction.builder())
                .andThen(MathUtils.linearScaler((SymbolicRegressionFitness) p.getFitnessFunction())),
            new ShallowGraphFactory(0.5d, 0d, 1d, p.arity(), 1),
            PartialComparator.from(Double.class).on(Individual::getFitness),
            100,
            Map.of(
                new NodeAddition<>(
                    FunctionNode.limitedIndexFactory(maxNodes, BaseFunction.RE_LU, BaseFunction.GAUSSIAN, BaseFunction.SIN),
                    Random::nextGaussian
                ), 2d,
                new EdgeModification<>((w, random) -> w + random.nextGaussian()), 1d,
                new EdgeAddition<>(Random::nextGaussian, false), 1d,
                new EdgeRemoval<>(node -> node instanceof Output), 0.1d,
                new AlignedCrossover<>(
                    (w1, w2, random) -> w1 + (w2 - w1) - random.nextDouble(),
                    node -> node instanceof Output,
                    false
                ), 1d
            ),
            new Tournament(5),
            new Worst(),
            100,
            true
        ),
        new StandardWithEnforcedDiversity<>(
            MultivariateRealFunctionGraph.builder()
                .andThen(GraphBasedRealFunction.builder())
                .andThen(MathUtils.linearScaler((SymbolicRegressionFitness) p.getFitnessFunction())),
            new ShallowGraphFactory(0.5d, 0d, 1d, p.arity(), 1),
            PartialComparator.from(Double.class).on(Individual::getFitness),
            100,
            Map.of(
                new NodeAddition<>(
                    FunctionNode.limitedIndexFactory(maxNodes, BaseFunction.RE_LU, BaseFunction.GAUSSIAN, BaseFunction.SIN),
                    Random::nextGaussian
                ), 2d,
                new EdgeModification<>((w, random) -> w + random.nextGaussian()), 1d,
                new EdgeAddition<>(Random::nextGaussian, false), 1d,
                new EdgeRemoval<>(node -> node instanceof Output), 0.1d,
                new AlignedCrossover<>(
                    (w1, w2, random) -> w1 + (w2 - w1) - random.nextDouble(),
                    node -> node instanceof Output,
                    false
                ), 1d
            ),
            new Tournament(5),
            new Worst(),
            100,
            true,
            100
        )
    );
    evolvers = evolvers.subList(2, evolvers.size()); //TODO remove
    for (Evolver<ValueGraph<Node, Double>, RealFunction, Double> evolver : evolvers) {
      System.out.println(evolver.getClass().getSimpleName());
      try {
        Collection<RealFunction> solutions = evolver.solve(
            Misc.cached(p.getFitnessFunction(), 10000),
            new TargetFitness<>(0d).or(new FitnessEvaluations(5000)),
            r,
            executorService,
            Listener.onExecutor(listener(
                new Basic(),
                new Population(),
                new Diversity(),
                new BestInfo("%7.5f"),
                new FunctionOfOneBest<>(i -> List.of(new Item(
                    "validation.fitness",
                    p.getValidationFunction().apply(i.getSolution()),
                    "%7.5f"
                )))//,
                //new BestPrinter(BestPrinter.Part.SOLUTION)
            ), executorService)
        );
        System.out.printf("Found %d solutions with %s.%n", solutions.size(), evolver.getClass().getSimpleName());
      } catch (InterruptedException | ExecutionException e) {
        e.printStackTrace();
      }
    }
  }

}
