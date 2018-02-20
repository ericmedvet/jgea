/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea;

import com.google.common.collect.Lists;
import it.units.malelab.jgea.core.Individual;
import it.units.malelab.jgea.core.Node;
import it.units.malelab.jgea.core.ProblemWithValidation;
import it.units.malelab.jgea.core.Sequence;
import it.units.malelab.jgea.core.evolver.DeterministicCrowdingEvolver;
import it.units.malelab.jgea.core.evolver.StandardEvolver;
import it.units.malelab.jgea.core.evolver.stopcondition.FitnessEvaluations;
import it.units.malelab.jgea.core.evolver.stopcondition.PerfectFitness;
import it.units.malelab.jgea.core.fitness.Classification;
import it.units.malelab.jgea.core.function.CachedNonDeterministicFunction;
import it.units.malelab.jgea.core.function.Function;
import it.units.malelab.jgea.core.listener.Listener;
import it.units.malelab.jgea.core.listener.PrintStreamListener;
import it.units.malelab.jgea.core.listener.collector.Basic;
import it.units.malelab.jgea.core.listener.collector.BestInfo;
import it.units.malelab.jgea.core.listener.collector.BestPrinter;
import it.units.malelab.jgea.core.listener.collector.FunctionOfBest;
import it.units.malelab.jgea.core.listener.collector.Collector;
import it.units.malelab.jgea.core.listener.collector.Diversity;
import it.units.malelab.jgea.core.listener.collector.Population;
import it.units.malelab.jgea.core.operator.GeneticOperator;
import it.units.malelab.jgea.core.ranker.ComparableRanker;
import it.units.malelab.jgea.core.ranker.FitnessComparator;
import it.units.malelab.jgea.core.ranker.ParetoRanker;
import it.units.malelab.jgea.core.ranker.selector.Tournament;
import it.units.malelab.jgea.core.ranker.selector.Worst;
import it.units.malelab.jgea.core.util.WithNames;
import it.units.malelab.jgea.distance.Distance;
import it.units.malelab.jgea.distance.Edit;
import it.units.malelab.jgea.grammarbased.GrammarBasedProblem;
import it.units.malelab.jgea.grammarbased.cfggp.RampedHalfAndHalf;
import it.units.malelab.jgea.grammarbased.cfggp.StandardTreeCrossover;
import it.units.malelab.jgea.grammarbased.cfggp.StandardTreeMutation;
import it.units.malelab.jgea.problem.booleanfunction.EvenParity;
import it.units.malelab.jgea.problem.booleanfunction.element.Element;
import it.units.malelab.jgea.problem.classification.AbstractClassificationProblem;
import it.units.malelab.jgea.problem.classification.BinaryRegexClassification;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
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

  public static void main(String[] args) throws InterruptedException, ExecutionException, IOException {

    //read command lines
    String baseResultDirName = a(args, "dir", ".");
    String baseResultFileName = a(args, "file", "results");
    ExecutorService executor = Executors.newFixedThreadPool(4);
    //parity(executor);
    binaryRegex(executor);
    executor.shutdown();
  }

  private static void parity(ExecutorService executor) throws IOException, InterruptedException, ExecutionException {
    final GrammarBasedProblem<String, List<Node<Element>>, Double> p = new EvenParity(5);
    Map<GeneticOperator<Node<String>>, Double> operators = new LinkedHashMap<>();
    operators.put(new StandardTreeMutation<>(12, p.getGrammar()), 0.2d);
    operators.put(new StandardTreeCrossover<>(12), 0.8d);
    StandardEvolver<Node<String>, List<Node<Element>>, Double> evolver = new StandardEvolver<>(
            500,
            new RampedHalfAndHalf<>(3, 12, p.getGrammar()),
            new ComparableRanker(new FitnessComparator<>()),
            p.getSolutionMapper(),
            operators,
            new Tournament<>(3),
            new Worst<>(),
            500,
            true,
            Lists.newArrayList(new FitnessEvaluations(100000), new PerfectFitness<>(p.getFitnessFunction())),
            10000,
            false
    );
    Random r = new Random(1);
    evolver.solve(p, r, executor,
            Listener.onExecutor(new PrintStreamListener(System.out, true, 10, " ", " | ",
                            new Basic(),
                            new Population(),
                            new BestInfo<>(
                                    (f, l) -> Collections.singletonMap("", f),
                                    (n, l) -> "%6.4f"),
                            new Diversity(),
                            new BestPrinter(null, "%s")
                    ), executor)
    );
  }

  private static void binaryRegex(ExecutorService executor) throws IOException, InterruptedException, ExecutionException {
    GrammarBasedProblem<String, String, List<Double>> p = new BinaryRegexClassification(
            50, 100, 5, 0, 1,
            Classification.ErrorMetric.BALANCED_ERROR_RATE, Classification.ErrorMetric.CLASS_ERROR_RATE
    );
    Map<GeneticOperator<Node<String>>, Double> operators = new LinkedHashMap<>();
    operators.put(new StandardTreeMutation<>(15, p.getGrammar()), 0.2d);
    operators.put(new StandardTreeCrossover<>(15), 0.8d);
    Distance<Sequence<Character>> edit = new Edit<>();
    Distance<String> editString = (s1, s2, l) -> (edit.apply(
            Sequence.from(s1.chars().mapToObj(c -> (char) c).toArray(Character[]::new)),
            Sequence.from(s2.chars().mapToObj(c -> (char) c).toArray(Character[]::new))
    ));
    Distance<Individual<Node<String>, String, List<Double>>> distance = (Individual<Node<String>, String, List<Double>> i1, Individual<Node<String>, String, List<Double>> i2, Listener l) -> (editString.apply(
            i1.getSolution(),
            i2.getSolution()
    ));
    StandardEvolver<Node<String>, String, List<Double>> evolver = new DeterministicCrowdingEvolver<>(
            distance.cached(10000),
            500,
            new RampedHalfAndHalf<>(3, 15, p.getGrammar()),
            new ParetoRanker<>(),
            p.getSolutionMapper(),
            operators,
            new Tournament<>(3),
            new Worst(),
            Lists.newArrayList(new FitnessEvaluations(100000), new PerfectFitness<>(p.getFitnessFunction())),
            10000,
            false
    );
    Random r = new Random(1);
    evolver.solve(p, r, executor,
            Listener.onExecutor(new PrintStreamListener(System.out, true, 10, " ", " | ",
                            new Basic(),
                            new Population(),
                            new BestInfo<>(
                                    BestInfo.fromNames((WithNames) p.getFitnessFunction()),
                                    (n, l) -> "%5.3f"),
                            new FunctionOfBest<>(
                                    "learning",
                                    new Classification<>(
                                            ((AbstractClassificationProblem) p).getLearningData(),
                                            (AbstractClassificationProblem) p,
                                            Classification.ErrorMetric.CLASS_ERROR_RATE
                                    ).cached(10000),
                                    BestInfo.fromNames((WithNames) ((ProblemWithValidation<String, List<Double>>) p).getValidationFunction()),
                                    (n, l) -> "%5.3f"),
                            new FunctionOfBest<>(
                                    "validation",
                                    ((ProblemWithValidation<String, List<Double>>) p).getValidationFunction().cached(10000),
                                    BestInfo.fromNames((WithNames) ((ProblemWithValidation<String, List<Double>>) p).getValidationFunction()),
                                    (n, l) -> "%5.3f"),
                            new Diversity(),
                            new BestPrinter(null, "%s")
                    ), executor
            )
    );
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
