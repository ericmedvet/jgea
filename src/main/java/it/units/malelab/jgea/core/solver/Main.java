package it.units.malelab.jgea.core.solver;

import it.units.malelab.jgea.core.listener.Listener;
import it.units.malelab.jgea.core.listener.NamedFunction;
import it.units.malelab.jgea.core.listener.TabularPrinter;
import it.units.malelab.jgea.core.solver.state.POSetPopulationState;
import it.units.malelab.jgea.core.solver.state.State;
import it.units.malelab.jgea.core.util.Misc;
import it.units.malelab.jgea.representation.sequence.FixedLengthListFactory;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.random.RandomGenerator;

public class Main {

  public static void main(String[] args) {
    RandomGenerator r = new Random(0);
    ExecutorService executor = Executors.newSingleThreadExecutor();
    TotalOrderQualityBasedProblem<List<Double>, Double> p = new TotalOrderQualityBasedProblem<>() {
      @Override
      public Function<List<Double>, Double> qualityMapper() {
        return vs -> vs.stream().mapToDouble(v -> v * v).sum();
      }

      @Override
      public Comparator<Double> totalOrderComparator() {
        return Double::compareTo;
      }
    };
    SimpleEvolutionaryStrategy<List<Double>, Double> es = new SimpleEvolutionaryStrategy<>(
        Function.identity(),
        new FixedLengthListFactory<>(100, random -> random.nextDouble() * 2d - 1d),
        100,
        StopConditions.elapsedMillis(10000),
        20,
        1,
        0.01,
        false
    );
    IterativeSolver<? extends POSetPopulationState<List<Double>, List<Double>, Double>, TotalOrderQualityBasedProblem<List<Double>, Double>, List<Double>> solver = es;
    Listener.Factory<? super POSetPopulationState<List<Double>, List<Double>, Double>> lFactory = new TabularPrinter<>(
        List.of(
            NamedFunction.build("iterations", "%3d", State::getNOfIterations),
            NamedFunction.build("elapsed.seconds", "%5.2f", s -> s.getElapsedMillis() / 1000f),
            NamedFunction.build("best.fitness", "%5.3f", s -> Misc.first(s.getPopulation().firsts()).fitness())
        ),
        System.out,
        10,
        true,
        true,
        true
    );
    try {
      Collection<List<Double>> solutions = solver.solve(p, r, executor, lFactory.build());
      System.out.printf("Done! Found %d solutions.", solutions.size());
    } catch (SolverException e) {
      e.printStackTrace();
    }
    executor.shutdown();
  }
}
