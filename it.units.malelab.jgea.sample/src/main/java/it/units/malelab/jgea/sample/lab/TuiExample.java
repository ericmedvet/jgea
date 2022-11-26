package it.units.malelab.jgea.sample.lab;

import it.units.malelab.jgea.core.listener.NamedFunction;
import it.units.malelab.jgea.core.listener.XYPlotTableBuilder;
import it.units.malelab.jgea.core.representation.grammar.Grammar;
import it.units.malelab.jgea.core.representation.grammar.cfggp.GrammarBasedSubtreeMutation;
import it.units.malelab.jgea.core.representation.grammar.cfggp.GrammarRampedHalfAndHalf;
import it.units.malelab.jgea.core.representation.graph.numeric.RealFunction;
import it.units.malelab.jgea.core.representation.tree.SameRootSubtreeCrossover;
import it.units.malelab.jgea.core.selector.Last;
import it.units.malelab.jgea.core.selector.Tournament;
import it.units.malelab.jgea.core.solver.*;
import it.units.malelab.jgea.core.solver.state.POSetPopulationState;
import it.units.malelab.jgea.core.util.Misc;
import it.units.malelab.jgea.problem.symbolicregression.*;
import it.units.malelab.jgea.tui.TerminalMonitor;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import static it.units.malelab.jgea.core.listener.NamedFunctions.*;

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
    executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() - 1);
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
                    iterations(),
                    List.of(fitness().reformat("%5.3f").of(best()).as(Number.class))
                ),
                new XYPlotTableBuilder<>(iterations(), List.of(uniqueness().of(each(genotype())).of(all())))
            )
        );
    List<Integer> seeds = List.of(1, 2, 3, 4, 5);
    SyntheticSymbolicRegressionProblem p = new Nguyen7(SymbolicRegressionFitness.Metric.MSE, 1);
    Grammar<String> srGrammar;
    try {
      srGrammar = Grammar.fromFile(new File("grammars/symbolic" + "-regression-nguyen7" + ".bnf"));
    } catch (IOException e) {
      L.severe(String.format("Cannot load grammar: %s", e));
      return;
    }
    List<IterativeSolver<? extends POSetPopulationState<?, RealFunction, Double>, SyntheticSymbolicRegressionProblem,
        RealFunction>> solvers = new ArrayList<>();
    solvers.add(new StandardEvolver<>(
        new FormulaMapper().andThen(n -> TreeBasedRealFunction.from(n, "x"))
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
        new FormulaMapper().andThen(n -> TreeBasedRealFunction.from(
            n,
            "x"
        )).andThen(MathUtils.linearScaler(p.qualityFunction())),
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
      for (IterativeSolver<? extends POSetPopulationState<?, RealFunction, Double>, SyntheticSymbolicRegressionProblem,
          RealFunction> solver : solvers) {
        Map<String, Object> keys = Map.ofEntries(
            Map.entry("seed", seed),
            Map.entry("solver", solver.getClass().getSimpleName())
        );
        tm.notify((double) counter / (double) (seeds.size() * solvers.size()), "Starting " + keys);
        try {
          Collection<RealFunction> solutions = solver.solve(
              p,
              r,
              executorService,
              tm.build(keys).deferred(executorService)
          );
          counter = counter + 1;
          tm.notify((double) counter / (double) (seeds.size() * solvers.size()), "Starting " + keys);
          L.info(String.format("Found %d solutions with %s", solutions.size(), keys));
        } catch (SolverException e) {
          L.severe(String.format("Exception while doing %s: %s", e, keys));
        }
      }
    }
    tm.shutdown();
  }

}
