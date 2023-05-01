/*
 * Copyright 2022 eric
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

package io.github.ericmedvet.jgea.sample.lab;

import io.github.ericmedvet.jgea.core.listener.NamedFunction;
import io.github.ericmedvet.jgea.core.listener.XYPlotTableBuilder;
import io.github.ericmedvet.jgea.core.representation.grammar.Grammar;
import io.github.ericmedvet.jgea.core.representation.grammar.cfggp.GrammarBasedSubtreeMutation;
import io.github.ericmedvet.jgea.core.representation.grammar.cfggp.GrammarRampedHalfAndHalf;
import io.github.ericmedvet.jgea.core.representation.tree.SameRootSubtreeCrossover;
import io.github.ericmedvet.jgea.core.selector.Last;
import io.github.ericmedvet.jgea.core.selector.Tournament;
import io.github.ericmedvet.jgea.core.solver.*;
import io.github.ericmedvet.jgea.core.solver.state.POSetPopulationState;
import io.github.ericmedvet.jgea.core.util.Misc;
import io.github.ericmedvet.jgea.problem.regression.FormulaMapper;
import io.github.ericmedvet.jgea.problem.regression.MathUtils;
import io.github.ericmedvet.jgea.problem.regression.symbolic.TreeBasedUnivariateRealFunction;
import io.github.ericmedvet.jgea.problem.regression.univariate.UnivariateRegressionFitness;
import io.github.ericmedvet.jgea.problem.regression.univariate.synthetic.Nguyen7;
import io.github.ericmedvet.jgea.problem.regression.univariate.synthetic.SyntheticUnivariateRegressionProblem;
import io.github.ericmedvet.jgea.tui.TerminalMonitor;
import io.github.ericmedvet.jsdynsym.core.numerical.UnivariateRealFunction;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import static io.github.ericmedvet.jgea.core.listener.NamedFunctions.*;

/**
 * @author "Eric Medvet" on 2022/09/03 for jgea
 */
public class TuiExample implements Runnable {

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

  private final static Logger L = Logger.getLogger(TuiExample.class.getName());

  private final ExecutorService executorService;

  public TuiExample() {
    executorService = Executors.newFixedThreadPool(1);
  }

  public static void main(String[] args) {
    new TuiExample().run();
  }

  @Override
  public void run() {
    TerminalMonitor<? super POSetPopulationState<?, ?, ? extends Double>, Map<String, Object>> tm =
        new TerminalMonitor<>(
            Misc.concat(List.of(BASIC_FUNCTIONS, DOUBLE_FUNCTIONS)),
            List.of(),
            List.of(
                new XYPlotTableBuilder<>(
                    progress(),
                    List.of(fitness().reformat("%5.3f").of(best()).as(Number.class)),
                    1,
                    1,
                    Double.NaN,
                    1,
                    Double.NaN,
                    Double.NaN,
                    true,
                    false
                ),
                new XYPlotTableBuilder<>(
                    progress(),
                    List.of(uniqueness().of(each(genotype())).of(all())),
                    1,
                    1,
                    Double.NaN,
                    1,
                    Double.NaN,
                    Double.NaN,
                    true,
                    false
                )
            )
        );
    List<Integer> seeds = List.of(1, 2, 3, 4, 5);
    SyntheticUnivariateRegressionProblem p = new Nguyen7(UnivariateRegressionFitness.Metric.MSE, 1);
    Grammar<String> srGrammar;
    try {
      srGrammar = Grammar.fromFile(new File("grammars/symbolic" + "-regression-nguyen7" + ".bnf"));
    } catch (IOException e) {
      L.severe(String.format("Cannot load grammar: %s", e));
      return;
    }
    List<IterativeSolver<? extends POSetPopulationState<?, UnivariateRealFunction, Double>,
        SyntheticUnivariateRegressionProblem,
        UnivariateRealFunction>> solvers = new ArrayList<>();
    solvers.add(new StandardEvolver<>(
        new FormulaMapper().andThen(n -> new TreeBasedUnivariateRealFunction(n, List.of("x")))
            .andThen(MathUtils.linearScaler(p.qualityFunction())),
        new GrammarRampedHalfAndHalf<>(3, 12, srGrammar),
        100,
        StopConditions.nOfIterations(500),
        Map.of(new SameRootSubtreeCrossover<>(12), 0.8d, new GrammarBasedSubtreeMutation<>(12, srGrammar), 0.2d),
        new Tournament(5),
        new Last(),
        100,
        true,
        false,
        (srp, rnd) -> new POSetPopulationState<>()
    ));
    solvers.add(new StandardWithEnforcedDiversityEvolver<>(
        new FormulaMapper().andThen(n -> new TreeBasedUnivariateRealFunction(n, List.of("x")))
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
    int counter = 0;
    for (int seed : seeds) {
      Random r = new Random(1);
      for (IterativeSolver<? extends POSetPopulationState<?, UnivariateRealFunction, Double>,
          SyntheticUnivariateRegressionProblem, UnivariateRealFunction> solver : solvers) {
        Map<String, Object> keys = Map.ofEntries(
            Map.entry("seed", seed),
            Map.entry("solver", solver.getClass().getSimpleName())
        );
        tm.notify(counter, seeds.size() * solvers.size(), "Starting " + keys);
        try {
          Collection<UnivariateRealFunction> solutions = solver.solve(
              p,
              r,
              executorService,
              tm.build(keys).deferred(executorService)
          );
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
