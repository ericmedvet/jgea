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

package io.github.ericmedvet.jgea.sample.lab;

import static io.github.ericmedvet.jgea.core.listener.NamedFunctions.*;

import io.github.ericmedvet.jgea.core.listener.NamedFunction;
import io.github.ericmedvet.jgea.core.listener.XYPlotTableBuilder;
import io.github.ericmedvet.jgea.core.representation.NamedUnivariateRealFunction;
import io.github.ericmedvet.jgea.core.representation.grammar.string.StringGrammar;
import io.github.ericmedvet.jgea.core.representation.grammar.string.cfggp.GrammarBasedSubtreeMutation;
import io.github.ericmedvet.jgea.core.representation.grammar.string.cfggp.GrammarRampedHalfAndHalf;
import io.github.ericmedvet.jgea.core.representation.tree.SameRootSubtreeCrossover;
import io.github.ericmedvet.jgea.core.representation.tree.numeric.TreeBasedUnivariateRealFunction;
import io.github.ericmedvet.jgea.core.selector.Last;
import io.github.ericmedvet.jgea.core.selector.Tournament;
import io.github.ericmedvet.jgea.core.solver.*;
import io.github.ericmedvet.jgea.core.solver.POCPopulationState;
import io.github.ericmedvet.jgea.core.util.Misc;
import io.github.ericmedvet.jgea.problem.regression.FormulaMapper;
import io.github.ericmedvet.jgea.problem.regression.MathUtils;
import io.github.ericmedvet.jgea.problem.regression.univariate.UnivariateRegressionFitness;
import io.github.ericmedvet.jgea.problem.regression.univariate.synthetic.Nguyen7;
import io.github.ericmedvet.jgea.problem.regression.univariate.synthetic.SyntheticUnivariateRegressionProblem;
import io.github.ericmedvet.jgea.tui.TerminalMonitor;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class TuiExample implements Runnable {

  public static final List<NamedFunction<? super POCPopulationState<?, ?, ?>, ?>> BASIC_FUNCTIONS = List.of(
      nOfIterations(),
      births(),
      elapsedSeconds(),
      size().of(all()),
      size().of(firsts()),
      size().of(lasts()),
      uniqueness().of(each(genotype())).of(all()),
      uniqueness().of(each(solution())).of(all()),
      uniqueness().of(each(quality())).of(all()),
      size().of(genotype()).of(best()),
      size().of(solution()).of(best()),
      fitnessMappingIteration().of(best()));

  public static final List<NamedFunction<? super POCPopulationState<?, ?, ? extends Double>, ?>> DOUBLE_FUNCTIONS =
      List.of(
          quality().reformat("%5.3f").of(best()),
          hist(8).of(each(quality())).of(all()),
          max(Double::compare).reformat("%5.3f").of(each(quality())).of(all()));

  private static final Logger L = Logger.getLogger(TuiExample.class.getName());

  private final ExecutorService executorService;

  public TuiExample() {
    executorService = Executors.newFixedThreadPool(1);
  }

  public static void main(String[] args) {
    new TuiExample().run();
  }

  @Override
  public void run() {
    TerminalMonitor<? super POCPopulationState<?, ?, ? extends Double>, Map<String, Object>> tm =
        new TerminalMonitor<>(
            Misc.concat(List.of(BASIC_FUNCTIONS, DOUBLE_FUNCTIONS)),
            List.of(),
            List.of(
                new XYPlotTableBuilder<>(
                    progress(),
                    List.of(quality()
                        .reformat("%5.3f")
                        .of(best())
                        .as(Number.class)),
                    1,
                    1,
                    Double.NaN,
                    1,
                    Double.NaN,
                    Double.NaN,
                    true,
                    false),
                new XYPlotTableBuilder<>(
                    progress(),
                    List.of(uniqueness()
                        .of(each(genotype()))
                        .of(all())),
                    1,
                    1,
                    Double.NaN,
                    1,
                    Double.NaN,
                    Double.NaN,
                    true,
                    false)));
    List<Integer> seeds = List.of(1, 2, 3, 4, 5);
    SyntheticUnivariateRegressionProblem p = new Nguyen7(UnivariateRegressionFitness.Metric.MSE, 1);
    StringGrammar<String> srGrammar;
    try {
      srGrammar = StringGrammar.load(
          StringGrammar.class.getResourceAsStream("/grammars/1d/symbolic-regression-nguyen7.bnf"));
    } catch (IOException e) {
      L.severe(String.format("Cannot load grammar: %s", e));
      return;
    }
    List<
            IterativeSolver<
                ? extends POCPopulationState<?, NamedUnivariateRealFunction, Double>,
                SyntheticUnivariateRegressionProblem,
                NamedUnivariateRealFunction>>
        solvers = new ArrayList<>();
    solvers.add(new AbstractStandardEvolver<>(
        new FormulaMapper()
            .andThen(n -> new TreeBasedUnivariateRealFunction(
                n,
                p.qualityFunction().getDataset().xVarNames(),
                p.qualityFunction().getDataset().yVarNames().get(0)))
            .andThen(MathUtils.linearScaler(p.qualityFunction())),
        new GrammarRampedHalfAndHalf<>(3, 12, srGrammar),
        100,
        StopConditions.nOfIterations(500),
        Map.of(
            new SameRootSubtreeCrossover<>(12),
            0.8d,
            new GrammarBasedSubtreeMutation<>(12, srGrammar),
            0.2d),
        new Tournament(5),
        new Last(),
        100,
        true,
        false,
        (srp, rnd) -> new POCPopulationState<>()));
    solvers.add(new StandardWithEnforcedDiversityEvolver<>(
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
        false,
        (srp, rnd) -> new POCPopulationState<>(),
        100));
    int counter = 0;
    for (int seed : seeds) {
      Random r = new Random(1);
      for (IterativeSolver<
              ? extends POCPopulationState<?, NamedUnivariateRealFunction, Double>,
              SyntheticUnivariateRegressionProblem,
              NamedUnivariateRealFunction>
          solver : solvers) {
        Map<String, Object> keys = Map.ofEntries(
            Map.entry("seed", seed),
            Map.entry("solver", solver.getClass().getSimpleName()));
        tm.notify(counter, seeds.size() * solvers.size(), "Starting " + keys);
        try {
          Collection<NamedUnivariateRealFunction> solutions =
              solver.solve(p, r, executorService, tm.build(keys).deferred(executorService));
          counter = counter + 1;
          tm.notify(counter, seeds.size() * solvers.size(), "Starting " + keys);
          L.info(String.format("Found %d solutions with %s", solutions.size(), keys));
        } catch (SolverException e) {
          L.severe(String.format("Exception while doing %s: %s", e, keys));
        }
      }
    }
    tm.shutdown();
  }
}
