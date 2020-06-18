/*
 * Copyright (C) 2019 Eric Medvet <eric.medvet@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.units.malelab.jgea.lab;

import com.google.common.collect.Lists;
import it.units.malelab.jgea.Worker;
import it.units.malelab.jgea.core.Factory;
import it.units.malelab.jgea.core.evolver.StandardEvolver;
import it.units.malelab.jgea.core.evolver.stopcondition.FitnessEvaluations;
import it.units.malelab.jgea.core.function.Function;
import it.units.malelab.jgea.core.function.FunctionException;
import it.units.malelab.jgea.representation.sequence.bit.BitStringFactory;
import it.units.malelab.jgea.core.listener.Listener;
import it.units.malelab.jgea.core.listener.MultiFileListenerFactory;
import it.units.malelab.jgea.core.listener.collector.Basic;
import it.units.malelab.jgea.core.listener.collector.BestInfo;
import it.units.malelab.jgea.core.listener.collector.Diversity;
import it.units.malelab.jgea.core.listener.collector.FunctionOfBest;
import it.units.malelab.jgea.core.listener.collector.FunctionOfEvent;
import it.units.malelab.jgea.core.listener.collector.Population;
import it.units.malelab.jgea.core.listener.collector.Static;
import it.units.malelab.jgea.representation.sequence.bit.BitFlipMutation;
import it.units.malelab.jgea.core.operator.GeneticOperator;
import it.units.malelab.jgea.representation.sequence.LengthPreservingTwoPointCrossover;
import it.units.malelab.jgea.core.ranker.ComparableRanker;
import it.units.malelab.jgea.core.ranker.FitnessComparator;
import it.units.malelab.jgea.core.selector.Tournament;
import it.units.malelab.jgea.core.selector.Worst;
import it.units.malelab.jgea.core.util.Pair;
import it.units.malelab.jgea.distance.Distance;
import it.units.malelab.jgea.distance.Edit;
import it.units.malelab.jgea.distance.Hamming;
import it.units.malelab.jgea.distance.TreeLeaves;
import it.units.malelab.jgea.representation.grammar.GrammarBasedProblem;
import it.units.malelab.jgea.representation.grammar.cfggp.RampedHalfAndHalf;
import it.units.malelab.jgea.representation.grammar.cfggp.StandardTreeCrossover;
import it.units.malelab.jgea.representation.grammar.cfggp.StandardTreeMutation;
import it.units.malelab.jgea.lab.surrogate.ControlledPrecisionProblem;
import it.units.malelab.jgea.lab.surrogate.TunablePrecisionProblem;
import it.units.malelab.jgea.problem.synthetic.OneMax;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;

import static it.units.malelab.jgea.core.util.Args.*;

/**
 *
 * @author eric
 */
public class SurrogatePrecisionControl extends Worker {

  public final static void main(String[] args) throws FileNotFoundException {
    new SurrogatePrecisionControl(args);
  }

  public SurrogatePrecisionControl(String[] args) throws FileNotFoundException {
    super(args);
  }

  public static class MinOfPrecisions<S> extends PrecisionController<S> {

    private final List<PrecisionController<S>> precisionControllers;

    public MinOfPrecisions(int historySize, PrecisionController<S>... precisionControllers) {
      super(historySize);
      this.precisionControllers = Lists.newArrayList(precisionControllers);
    }

    @Override
    public Double apply(S solution, Collection<Pair<S, Double>> history, Listener listener) throws FunctionException {
      double min = precisionControllers.stream().mapToDouble(pc -> pc.apply(solution)).min().orElse(0d);
      return min;
    }

  }

  public static class StaticController<S> extends PrecisionController<S> {

    private final double precision;

    public StaticController(double precision) {
      super(1);
      this.precision = precision;
    }

    @Override
    public Double apply(S solution, Collection<Pair<S, Double>> history, Listener listener) throws FunctionException {
      return precision;
    }

  }

  public static class LinearController<S> extends PrecisionController<S> {

    private final double startPrecision;
    private final double endPrecision;
    private final int endIteration;

    public LinearController(double startPrecision, double endPrecision, int endIteration) {
      super(1);
      this.startPrecision = startPrecision;
      this.endPrecision = endPrecision;
      this.endIteration = endIteration;
    }

    @Override
    public Double apply(S s, Collection<Pair<S, Double>> history, Listener listener) throws FunctionException {
      double precision = startPrecision + (endPrecision - startPrecision) * Math.min(1d, (double) getCalls() / (double) endIteration);
      return precision;
    }

  }

  public static class PerSolutionLinearController<S> extends PrecisionController<S> {

    private final double startPrecision;
    private final double endPrecision;
    private final int maxCount;

    public PerSolutionLinearController(double startPrecision, double endPrecision, int maxCount, int historySize) {
      super(historySize);
      this.startPrecision = startPrecision;
      this.endPrecision = endPrecision;
      this.maxCount = maxCount;
    }

    @Override
    public Double apply(S s, Collection<Pair<S, Double>> history, Listener listener) throws FunctionException {
      double count = history.stream().map(Pair::first).filter(otherS -> otherS.equals(s)).count();
      double precision = startPrecision + (endPrecision - startPrecision) * Math.min(1d, (double) count / (double) maxCount);
      return precision;
    }

  }

  public static class CrowdingController<S> extends PrecisionController<S> {

    private final double minPrecision;
    private final double radius;
    private final Distance<S> distance;

    public CrowdingController(double minPrecision, double radius, Distance<S> distance, int historySize) {
      super(historySize);
      this.minPrecision = minPrecision;
      this.radius = radius;
      this.distance = distance;
    }

    @Override
    public Double apply(S solution, Collection<Pair<S, Double>> history, Listener listener) throws FunctionException {
      if (history.size() < getHistorySize()) {
        return minPrecision;
      }
      double count = history.stream().mapToDouble(p -> distance.apply(solution, p.first())).filter(d -> d < radius).count();
      return minPrecision * (1d - count / (double) getHistorySize());
    }

  }

  public static class ReducerController<S> extends PrecisionController<S> {

    public enum AggregateType {
      MIN, AVG
    };

    private final double rate;
    private final double epsilon;
    private final Distance<S> distance;
    private final AggregateType aggregateType;

    private double currentPrecision;

    public ReducerController(double startingPrecision, double rate, double epsilon, Distance<S> distance, AggregateType aggregateType, int historySize) {
      super(historySize);
      this.rate = rate;
      this.epsilon = epsilon;
      this.distance = distance;
      this.aggregateType = aggregateType;
      currentPrecision = startingPrecision;
    }

    @Override
    public Double apply(S solution, Collection<Pair<S, Double>> history, Listener listener) throws FunctionException {
      double[] distances = history.stream().mapToDouble(p -> distance.apply(solution, p.first())).toArray();
      double distance = 0d;
      if (aggregateType.equals(AggregateType.AVG)) {
        distance = Arrays.stream(distances).average().orElse(0d);
      } else if (aggregateType.equals(AggregateType.MIN)) {
        distance = Arrays.stream(distances).min().orElse(0d);
      }
      if (distance < epsilon) {
        currentPrecision = currentPrecision * rate;
      }
      return currentPrecision;
    }

  }

  public static class HistoricAvgDistanceRatio<S> extends PrecisionController<S> {

    private final double minPrecision;
    private final SortedSet<Double> longestDistances;
    private final SortedSet<Double> shortestDistances;
    private final int nOfDistances;
    private final Distance<S> distance;

    public HistoricAvgDistanceRatio(double minPrecision, int nOfDistances, Distance<S> distance, int historySize) {
      super(historySize);
      this.minPrecision = minPrecision;
      this.nOfDistances = nOfDistances;
      this.distance = distance;
      longestDistances = new TreeSet<>();
      shortestDistances = new TreeSet<>();
    }

    @Override
    public Double apply(S solution, Collection<Pair<S, Double>> history, Listener listener) throws FunctionException {
      if (history.isEmpty()) {
        return minPrecision;
      }
      //compute average distance to history
      double avgD = history.stream().mapToDouble(p -> distance.apply(solution, p.first())).average().orElse(Double.POSITIVE_INFINITY);
      //update shortest
      if (shortestDistances.isEmpty() || (shortestDistances.last() > avgD)) {
        shortestDistances.add(avgD);
        if (shortestDistances.size() > nOfDistances) {
          shortestDistances.remove(shortestDistances.last());
        }
      }
      //update longest
      if (longestDistances.isEmpty() || (longestDistances.first() < avgD)) {
        longestDistances.add(avgD);
        if (longestDistances.size() > nOfDistances) {
          longestDistances.remove(longestDistances.first());
        }
      }
      //compute current avgD position
      double p = (avgD - shortestDistances.last()) / (longestDistances.first() - shortestDistances.last());
      if (Double.isNaN(p)) {
        return minPrecision;
      }
      p = Math.max(0d, Math.min(p, 1d));
      return minPrecision * p;
    }

  }

  @Override
  public void run() {
    //prepare parameters
    int[] runs = ri(a("runs", "0:10"));
    int evaluations = i(a("nev", "1000"));
    int population = i(a("npop", "100"));
    List<Boolean> overlappings = b(l(a("overlapping", "false")));
    Map<String, TunablePrecisionProblem> problems = new LinkedHashMap<>();
    problems.put("OneMax", new OneMax());
    /*try {
      problems.put("SR-Pagie1", new Pagie1());
      problems.put("SR-Nguyen7", new Nguyen7(1));
    } catch (IOException ex) {
      Logger.getLogger(SurrogatePrecisionControl.class.getName()).log(Level.SEVERE, "Cannot set problem!", ex);
    }*/
    //prepare controllers
    Set<String> controllerNames = new LinkedHashSet<>();
    controllerNames.add("static-0");
    controllerNames.add("static-0.25");
    controllerNames.add("static-0.50");
    controllerNames.add("linear-0.5-0-1.0");
    controllerNames.add("solutionLinear-0.5-0-0.1-100");
    controllerNames.add("solutionLinear-0.5-0-0.05-100");
    controllerNames.add("reducer-0.5-0.95-1-min-100");
    controllerNames.add("reducer-0.5-0.95-5-avg-100");
    controllerNames.add("crowding-0.5-5-100");

    controllerNames.clear();

    controllerNames.add("static-0");
    //controllerNames.add("static-0.50");
    controllerNames.add("linear-0.95-0-1.0");
    //controllerNames.add("crowding-0.5-10-100");
    controllerNames.add("avgDistRatio-0.95-5-100");
    controllerNames.add("min");
    //prepare things
    MultiFileListenerFactory listenerFactory = new MultiFileListenerFactory(a("dir", "."), a("file", null));
    //iterate
    for (int run : runs) {
      for (Map.Entry<String, TunablePrecisionProblem> problemEntry : problems.entrySet()) {
        TunablePrecisionProblem problem = problemEntry.getValue();
        //prepare problem-dependent components
        Factory genotypeFactory = null;
        Function mapper = Function.identity();
        Map<GeneticOperator, Double> operators = new LinkedHashMap<>();
        Distance solutionDistance = null;
        if (problemEntry.getKey().startsWith("OneMax")) {
          genotypeFactory = new BitStringFactory(100);
          operators.put(new BitFlipMutation(0.01d), 0.2d);
          operators.put(new LengthPreservingTwoPointCrossover(), 0.8d);
          solutionDistance = new Hamming().cached(10000);
        } else if (problemEntry.getKey().startsWith("SR-")) {
          genotypeFactory = new RampedHalfAndHalf<>(3, 12, ((GrammarBasedProblem) problem).getGrammar());
          mapper = ((GrammarBasedProblem) problem).getSolutionMapper();
          operators.put(new StandardTreeMutation<>(12, ((GrammarBasedProblem) problem).getGrammar()), 0.2d);
          operators.put(new StandardTreeCrossover(12), 0.2d);
          solutionDistance = new TreeLeaves(new Edit()).cached(10000);
        }
        for (boolean overlapping : overlappings) {
          for (String controllerName : controllerNames) {
            PrecisionController controller = createController(controllerName, evaluations, solutionDistance);
            ControlledPrecisionProblem p = new ControlledPrecisionProblem<>(problem, controller);
            //prepare evolver
            StandardEvolver evolver = new StandardEvolver(
                    population,
                    genotypeFactory,
                    new ComparableRanker(new FitnessComparator<>(Function.identity())),
                    mapper,
                    operators,
                    new Tournament<>(3),
                    new Worst<>(),
                    population,
                    overlapping,
                    Lists.newArrayList(new FitnessEvaluations(evaluations)),
                    0,
                    false
            );
            //prepare keys
            Map<String, String> keys = new LinkedHashMap<>();
            keys.put("run", Integer.toString(run));
            keys.put("problem", problemEntry.getKey());
            keys.put("max.evaluations", Integer.toString(evaluations));
            keys.put("controller", controllerName);
            keys.put("overlapping", Boolean.toString(overlapping));
            System.out.println(keys);
            //run evolver
            Random r = new Random(run);
            try {
              evolver.solve(p, r, executorService, Listener.onExecutor(listenerFactory.build(
                      new Static(keys),
                      new Basic(),
                      new Population(),
                      new Diversity(),
                      new BestInfo<>("%6.4f"),
                      new FunctionOfBest<>("actual.fitness", (Function) p.getInnerProblem().getFitnessFunction().cached(0), "%6.4f"),
                      new FunctionOfEvent<>("sum.precisions", (e, l) -> p.getController().getSumOfPrecisions(), "%8.4f"),
                      new FunctionOfEvent<>("calls", (e, l) -> p.getController().getCalls(), "%7d"),
                      new FunctionOfEvent("history.avg.precision", (e, l) -> p.getController().getHistory().stream().mapToDouble((o) -> ((Double) ((Pair) o).second()).doubleValue()).average().orElse(Double.NaN), "%5.3f")
              ), executorService));
            } catch (InterruptedException | ExecutionException ex) {
              L.log(Level.SEVERE, String.format("Cannot solve problem: %s", ex), ex);

              ex.printStackTrace();
              System.exit(0);
            }
          }
        }
      }
    }
  }

  public PrecisionController createController(String controllerName, int evaluations, Distance solutionDistance) {
    //prepare controlled problem
    PrecisionController controller = null;
    if (controllerName.startsWith("static")) {
      controller = new StaticController<>(
              d(p(controllerName, 1))
      );
    } else if (controllerName.startsWith("linear")) {
      controller = new LinearController<>(
              d(p(controllerName, 1)),
              d(p(controllerName, 2)),
              (int) Math.round((double) evaluations / d(p(controllerName, 3)))
      );
    } else if (controllerName.startsWith("solutionLinear")) {
      controller = new PerSolutionLinearController<>(
              d(p(controllerName, 1)),
              d(p(controllerName, 2)),
              (int) Math.round((double) evaluations / d(p(controllerName, 3))),
              i(p(controllerName, 4))
      );
    } else if (controllerName.startsWith("reducer")) {
      controller = new ReducerController<>(
              d(p(controllerName, 1)),
              d(p(controllerName, 2)),
              d(p(controllerName, 3)),
              solutionDistance,
              ReducerController.AggregateType.valueOf(p(controllerName, 4).toUpperCase()),
              i(p(controllerName, 5))
      );
    } else if (controllerName.startsWith("crowding")) {
      controller = new CrowdingController<>(
              d(p(controllerName, 1)),
              d(p(controllerName, 2)),
              solutionDistance,
              i(p(controllerName, 3))
      );
    } else if (controllerName.startsWith("avgDistRatio")) {
      controller = new HistoricAvgDistanceRatio<>(
              d(p(controllerName, 1)),
              i(p(controllerName, 2)),
              solutionDistance,
              i(p(controllerName, 3))
      );
    } else if (controllerName.startsWith("min")) {
      controller = new SurrogatePrecisionControl.MinOfPrecisions<>(
              100,
              new SurrogatePrecisionControl.LinearController<>(0.95d, 0d, evaluations),
              new SurrogatePrecisionControl.HistoricAvgDistanceRatio<>(0.95d, 5, solutionDistance, 100)
      );
    }
    return controller;
  }

}
