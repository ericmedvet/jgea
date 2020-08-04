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

package it.units.malelab.jgea.representation.graph.multivariatefunction;

import com.google.common.graph.ValueGraph;
import it.units.malelab.jgea.Worker;
import it.units.malelab.jgea.core.Individual;
import it.units.malelab.jgea.core.evolver.Evolver;
import it.units.malelab.jgea.core.evolver.RandomSearch;
import it.units.malelab.jgea.core.evolver.stopcondition.Iterations;
import it.units.malelab.jgea.core.evolver.stopcondition.TargetFitness;
import it.units.malelab.jgea.core.listener.collector.*;
import it.units.malelab.jgea.core.order.PartialComparator;
import it.units.malelab.jgea.core.util.Misc;
import it.units.malelab.jgea.problem.symbolicregression.*;

import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

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
    SymbolicRegressionProblem p = new Nguyen7(1);
    List<Evolver<ValueGraph<MultivariateRealFunctionGraph.Node, Double>, RealFunction, Double>> evolvers = List.of(
        new RandomSearch<ValueGraph<MultivariateRealFunctionGraph.Node, Double>, RealFunction, Double>(
            MultivariateRealFunctionGraph.builder()
                .andThen(GraphBasedRealFunction.builder())
                .andThen(MathUtils.linearScaler((SymbolicRegressionFitness) p.getFitnessFunction())),
            new ShallowGraphFactory(0.5d, 0d, 1d, p.getArity(), 1),
            PartialComparator.from(Double.class).on(Individual::getFitness)
        )
    );
    for (Evolver<ValueGraph<MultivariateRealFunctionGraph.Node, Double>, RealFunction, Double> evolver : evolvers) {
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

}
