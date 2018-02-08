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
import it.units.malelab.jgea.core.genotype.BitString;
import it.units.malelab.jgea.core.genotype.BitStringFactory;
import it.units.malelab.jgea.core.listener.ListenerUtils;
import it.units.malelab.jgea.core.listener.PrintStreamListener;
import it.units.malelab.jgea.core.listener.collector.Basic;
import it.units.malelab.jgea.core.listener.collector.BestPrinter;
import it.units.malelab.jgea.core.listener.collector.Diversity;
import it.units.malelab.jgea.core.listener.collector.Population;
import it.units.malelab.jgea.core.listener.collector.SingleObjectiveBest;
import it.units.malelab.jgea.core.mapper.Identity;
import it.units.malelab.jgea.core.operator.BitFlipMutation;
import it.units.malelab.jgea.core.operator.GeneticOperator;
import it.units.malelab.jgea.core.operator.LenghtPreservingTwoPointCrossover;
import it.units.malelab.jgea.core.ranker.ComparableRanker;
import it.units.malelab.jgea.core.ranker.selector.Tournament;
import it.units.malelab.jgea.core.ranker.selector.Worst;
import it.units.malelab.jgea.problem.synthetic.OneMax;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author eric
 */
public class Executor {

  public static void main(String[] args) throws InterruptedException, ExecutionException {
    //read command lines
    String baseResultDirName = a(args, "dir", ".");
    String baseResultFileName = a(args, "file", "results");

    Problem<BitString, Double> p = new OneMax();
    Map<GeneticOperator<BitString>, Double> operators = new LinkedHashMap<>();
    operators.put(new BitFlipMutation(0.01), 0.2d);
    operators.put(new LenghtPreservingTwoPointCrossover<BitString>(), 0.8d);
    StandardEvolver<BitString, BitString, Double> evolver = new StandardEvolver<>(
            100,
            new BitStringFactory(100),
            new ComparableRanker<>(new Comparator<Individual<BitString, BitString, Double>>() {
              @Override
              public int compare(Individual<BitString, BitString, Double> i1, Individual<BitString, BitString, Double> i2) {
                return Double.compare(i1.getFitness(), i2.getFitness());
              }
            }),
            new Identity<BitString>(),
            operators,
            new Tournament<Individual<BitString, BitString, Double>>(3),
            new Worst<Individual<BitString, BitString, Double>>(),
            100,
            true,
            Lists.newArrayList(new FitnessEvaluations(10000), new PerfectFitness(p.getFitnessMapper().bestValue())),
            10000,
            false
    );
    Random r = new Random(1);
    ExecutorService executor = Executors.newFixedThreadPool(2);
    evolver.solve(p, r, executor,
            ListenerUtils.onExecutor(
                    new PrintStreamListener(System.out, true, 10, " ", " | ",
                            new Basic(),
                            new Population(),
                            new SingleObjectiveBest("%4.2f", false, null),
                            new Diversity(),
                            new BestPrinter(null, "%s")
                    ), executor));
  }

  private final static String PIECES_SEP = "-";
  private final static String OPTIONS_SEP = ",";
  private final static String KEYVAL_SEP = "=";

  private static String p(String s, int n) {
    String[] pieces = s.split(PIECES_SEP);
    if (n < pieces.length) {
      return pieces[n];
    }
    return null;
  }

  private static int i(String s) {
    return Integer.parseInt(s);
  }

  private static double d(String s) {
    return Double.parseDouble(s);
  }

  private static String a(String[] args, String name, String defaultValue) {
    for (String arg : args) {
      String[] pieces = arg.split(KEYVAL_SEP);
      if (pieces[0].equals(name)) {
        return pieces[1];
      }
    }
    return defaultValue;
  }

  private static List<String> l(String s) {
    List<String> l = new ArrayList<>();
    String[] pieces = s.split(OPTIONS_SEP);
    for (String piece : pieces) {
      l.add(piece);
    }
    return l;
  }

  private static List<Integer> i(List<String> strings) {
    List<Integer> ints = new ArrayList<>();
    for (String string : strings) {
      ints.add(Integer.parseInt(string));
    }
    return ints;
  }

}
