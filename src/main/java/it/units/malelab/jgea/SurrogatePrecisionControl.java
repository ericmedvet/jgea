/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea;

import com.google.common.collect.Lists;
import it.units.malelab.jgea.core.evolver.StandardEvolver;
import it.units.malelab.jgea.core.evolver.stopcondition.FitnessEvaluations;
import it.units.malelab.jgea.core.evolver.stopcondition.PerfectFitness;
import it.units.malelab.jgea.core.function.BiFunction;
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
import it.units.malelab.jgea.problem.surrogate.ControlledPrecisionProblem;
import it.units.malelab.jgea.problem.surrogate.TunablePrecisionProblem;
import it.units.malelab.jgea.problem.synthetic.OneMax;
import java.io.FileNotFoundException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
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

  public static class StaticController<S> implements BiFunction<S, List<Pair<S, Double>>, Double> {

    private final double precision;

    public StaticController(double precision) {
      this.precision = precision;
    }

    @Override
    public Double apply(S s, List<Pair<S, Double>> history, Listener listener) throws FunctionException {
      return precision;
    }

  }

  public static class LinearController<S> implements BiFunction<S, List<Pair<S, Double>>, Double> {

    private final double min;
    private final double max;
    private final int span;

    public LinearController(double min, double max, int span) {
      this.min = min;
      this.max = max;
      this.span = span;
    }

    @Override
    public Double apply(S s, List<Pair<S, Double>> history, Listener listener) throws FunctionException {
      return max - (max - min) * Math.min((double) (history.size() / span), 1d);
    }

  }

  @Override
  public void run() {
    //prepare parameters
    List<Integer> runs = i(l(a("runs", "0,1,2,3,4,5,6,7,8,9")));
    int evaluations = i(a("nev", "1000"));
    int solutionSize = i(a("ssize", "100"));
    int population = i(a("npop", "100"));
    Map<GeneticOperator<BitString>, Double> operators = new LinkedHashMap<>();
    operators.put(new BitFlipMutation(0.01d), 0.2d);
    operators.put(new LenghtPreservingTwoPointCrossover(), 0.8d);
    //prepare controllers
    Map<String, BiFunction<BitString, List<Pair<BitString, Double>>, Double>> controllers = new LinkedHashMap<>();
    controllers.put("static-0", new StaticController<>(0d));
    controllers.put("static-0.25", new StaticController<>(0.25d));
    controllers.put("static-0.50", new StaticController<>(0.5d));
    controllers.put("static-0.75", new StaticController<>(0.75d));
    controllers.put("static-0.90", new StaticController<>(0.9d));
    controllers.put("linear-0,1,1.0", new LinearController<>(0d, 1d, evaluations));
    controllers.put("linear-0,1,0.5", new LinearController<>(0d, 1d, evaluations / 2));
    //prepare things
    MultiFileListenerFactory listenerFactory = new MultiFileListenerFactory(a("dir", "."), a("file", null));
    //prepare problem
    TunablePrecisionProblem<BitString, Double> ip = new OneMax();
    //iterate
    for (int run : runs) {
      for (Map.Entry<String, BiFunction<BitString, List<Pair<BitString, Double>>, Double>> namedController : controllers.entrySet()) {
        //prepare controlled problem
        ControlledPrecisionProblem<BitString, Double> p = new ControlledPrecisionProblem<>(ip, namedController.getValue());
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
                true,
                Lists.newArrayList(new FitnessEvaluations(evaluations), new PerfectFitness<>(p.getFitnessFunction())),
                0,
                false
        );
        //prepare keys
        Map<String, String> keys = new LinkedHashMap<>();
        keys.put("run", Integer.toString(run));
        keys.put("solution.size", Integer.toString(solutionSize));
        keys.put("max.evaluations", Integer.toString(evaluations));
        keys.put("controller", namedController.getKey());
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
                  new FunctionOfEvent<>("overall.cost", (e, l) -> p.overallCost(), "%6.4f")
          ), executorService));
        } catch (InterruptedException | ExecutionException ex) {
          L.log(Level.SEVERE, String.format("Cannot solve problem: %s", ex), ex);
        }
      }
    }
  }

}
