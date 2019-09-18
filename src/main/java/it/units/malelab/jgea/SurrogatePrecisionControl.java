/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea;

import com.google.common.collect.Lists;
import it.units.malelab.jgea.core.PrecisionController;
import it.units.malelab.jgea.core.evolver.StandardEvolver;
import it.units.malelab.jgea.core.evolver.stopcondition.FitnessEvaluations;
import it.units.malelab.jgea.core.function.Function;
import it.units.malelab.jgea.core.function.FunctionException;
import it.units.malelab.jgea.core.genotype.BitString;
import it.units.malelab.jgea.core.genotype.BitStringFactory;
import it.units.malelab.jgea.core.listener.Listener;
import it.units.malelab.jgea.core.listener.MultiFileListenerFactory;
import it.units.malelab.jgea.core.listener.collector.Basic;
import it.units.malelab.jgea.core.listener.collector.BestInfo;
import it.units.malelab.jgea.core.listener.collector.Diversity;
import it.units.malelab.jgea.core.listener.collector.FunctionOfBest;
import it.units.malelab.jgea.core.listener.collector.FunctionOfEvent;
import it.units.malelab.jgea.core.listener.collector.Population;
import it.units.malelab.jgea.core.listener.collector.Static;
import it.units.malelab.jgea.core.operator.BitFlipMutation;
import it.units.malelab.jgea.core.operator.GeneticOperator;
import it.units.malelab.jgea.core.operator.LenghtPreservingTwoPointCrossover;
import it.units.malelab.jgea.core.ranker.ComparableRanker;
import it.units.malelab.jgea.core.ranker.FitnessComparator;
import it.units.malelab.jgea.core.ranker.selector.Tournament;
import it.units.malelab.jgea.core.ranker.selector.Worst;
import it.units.malelab.jgea.core.util.Pair;
import it.units.malelab.jgea.distance.Distance;
import it.units.malelab.jgea.distance.Hamming;
import it.units.malelab.jgea.problem.surrogate.ControlledPrecisionProblem;
import it.units.malelab.jgea.problem.surrogate.TunablePrecisionProblem;
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
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author eric
 */
public class SurrogatePrecisionControl extends Worker {

  private final static Logger L = Logger.getLogger(SurrogatePrecisionControl.class.getName());

  public final static void main(String[] args) throws FileNotFoundException {
    new SurrogatePrecisionControl(args);
  }

  public SurrogatePrecisionControl(String[] args) throws FileNotFoundException {
    super(args);
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

  @Override
  public void run() {
    //prepare parameters
    int[] runs = ri(a("runs", "0:10"));
    int evaluations = i(a("nev", "1000"));
    int solutionSize = i(a("ssize", "100"));
    int population = i(a("npop", "100"));
    List<Boolean> overlappings = b(l(a("overlapping", "false,true")));
    Map<GeneticOperator<BitString>, Double> operators = new LinkedHashMap<>();
    operators.put(new BitFlipMutation(0.01d), 0.2d);
    operators.put(new LenghtPreservingTwoPointCrossover(), 0.8d);
    //prepare controllers
    Set<String> controllerNames = new LinkedHashSet<>();
    controllerNames.add("static-0");
    controllerNames.add("static-0.25");
    controllerNames.add("static-0.50");
    controllerNames.add("linear-0.5-0-1.0");
    controllerNames.add("linear-0.5-0-0.5");
    controllerNames.add("solutionLinear-0.5-0-0.1-100");
    controllerNames.add("solutionLinear-0.5-0-0.05-100");
    controllerNames.add("reducer-0.5-0.95-1-min-100");
    controllerNames.add("reducer-0.5-0.95-5-avg-100");
    controllerNames.add("crowding-0.5-5-100");
    controllerNames.add("crowding-0.5-10-100");
    //prepare things
    MultiFileListenerFactory listenerFactory = new MultiFileListenerFactory(a("dir", "."), a("file", null));
    //prepare problem
    TunablePrecisionProblem<BitString, Double> ip = new OneMax();
    //iterate
    for (int run : runs) {
      for (boolean overlapping : overlappings) {
        for (String controllerName : controllerNames) {
          //prepare controlled problem
          PrecisionController<BitString> controller = null;
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
                    new Hamming(),
                    ReducerController.AggregateType.valueOf(p(controllerName, 4).toUpperCase()),
                    i(p(controllerName, 5))
            );
          } else if (controllerName.startsWith("crowding")) {
            controller = new CrowdingController<>(
                    d(p(controllerName, 1)),
                    d(p(controllerName, 2)),
                    new Hamming(),
                    i(p(controllerName, 3))
            );
          }
          ControlledPrecisionProblem<BitString, Double> p = new ControlledPrecisionProblem<>(ip, controller);
          //prepare evolver
          StandardEvolver<BitString, BitString, Double> evolver = new StandardEvolver<>(
                  population,
                  new BitStringFactory(solutionSize),
                  new ComparableRanker(new FitnessComparator<>(Function.identity())),
                  Function.identity(),
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
          keys.put("solution.size", Integer.toString(solutionSize));
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
                    new FunctionOfBest<>("actual.fitness", (Function) p.getInnerProblem().getFitnessFunction(), 0, "%6.4f"),
                    new FunctionOfEvent<>("sum.precisions", (e, l) -> p.getController().getSumOfPrecisions(), "%8.4f"),
                    new FunctionOfEvent<>("calls", (e, l) -> p.getController().getCalls(), "%7d"),
                    new FunctionOfEvent<>("history.avg.precision", (e, l) -> p.getController().getHistory().stream().mapToDouble(Pair::second).average().orElse(Double.NaN), "%7d")
            ), executorService));
          } catch (InterruptedException | ExecutionException ex) {
            L.log(Level.SEVERE, String.format("Cannot solve problem: %s", ex), ex);
          }
        }
      }
    }
  }

}
