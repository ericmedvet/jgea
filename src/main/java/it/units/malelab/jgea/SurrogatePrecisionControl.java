/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea;

import com.google.common.collect.Lists;
import it.units.malelab.jgea.core.Individual;
import it.units.malelab.jgea.core.Problem;
import it.units.malelab.jgea.core.evolver.StandardEvolver;
import it.units.malelab.jgea.core.evolver.stopcondition.FitnessEvaluations;
import it.units.malelab.jgea.core.evolver.stopcondition.PerfectFitness;
import it.units.malelab.jgea.core.function.Function;
import it.units.malelab.jgea.core.genotype.BitString;
import it.units.malelab.jgea.core.genotype.BitStringFactory;
import it.units.malelab.jgea.core.listener.Listener;
import it.units.malelab.jgea.core.listener.collector.Basic;
import it.units.malelab.jgea.core.listener.collector.BestInfo;
import it.units.malelab.jgea.core.listener.collector.BestPrinter;
import it.units.malelab.jgea.core.listener.collector.Diversity;
import it.units.malelab.jgea.core.listener.collector.FunctionOfBest;
import it.units.malelab.jgea.core.listener.collector.FunctionOfEvent;
import it.units.malelab.jgea.core.listener.collector.Population;
import it.units.malelab.jgea.core.operator.BitFlipMutation;
import it.units.malelab.jgea.core.operator.GeneticOperator;
import it.units.malelab.jgea.core.operator.LenghtPreservingTwoPointCrossover;
import it.units.malelab.jgea.core.ranker.ComparableRanker;
import it.units.malelab.jgea.core.ranker.FitnessComparator;
import it.units.malelab.jgea.core.ranker.selector.Tournament;
import it.units.malelab.jgea.core.ranker.selector.Worst;
import it.units.malelab.jgea.core.util.Pair;
import it.units.malelab.jgea.problem.surrogate.ControlledPrecisionProblem;
import it.units.malelab.jgea.problem.synthetic.OneMax;
import java.io.FileNotFoundException;
import java.util.Comparator;
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

  @Override
  public void run() {
    //prepare parameters
    List<Integer> runs = i(l(a("runs", "0")));
    int evaluations = i(a("nev", "10000"));
    int solutionSize = i(a("ssize", "1000"));
    int population = i(a("npop", "100"));
    //prepare problems
    Problem<BitString, Double> ip = new OneMax();
    ControlledPrecisionProblem<BitString, Double> p = new ControlledPrecisionProblem<>(
            new OneMax(),
            (BitString b, List<Pair<BitString, Double>> history, Listener listener) -> 0.00001d
    );
    Map<GeneticOperator<BitString>, Double> operators = new LinkedHashMap<>();
    operators.put(new BitFlipMutation(0.01d), 0.2d);
    operators.put(new LenghtPreservingTwoPointCrossover(), 0.8d);
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
    Random r = new Random(1);
    try {
      evolver.solve(p, r, executorService, Listener.onExecutor(listener(
              new Basic(),
              new Population(),
              new Diversity(),
              new BestInfo<>("%6.4f"),
              new FunctionOfBest<>("actual.fitness", (Function)p.getInnerProblem().getFitnessFunction(), 0, "%6.4f"),
              new FunctionOfEvent<>("precision", (e, l) -> p.overallCost(), "%6.4f")
      ), executorService));
    } catch (InterruptedException | ExecutionException ex) {
      L.log(Level.SEVERE, String.format("Cannot solve problem: %s", ex), ex);
    }
  }
  
}
