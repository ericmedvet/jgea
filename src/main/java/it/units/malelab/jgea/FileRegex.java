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
import it.units.malelab.jgea.core.evolver.stopcondition.ElapsedTime;
import it.units.malelab.jgea.core.evolver.stopcondition.FitnessEvaluations;
import it.units.malelab.jgea.core.evolver.stopcondition.PerfectFitness;
import it.units.malelab.jgea.core.fitness.Classification;
import it.units.malelab.jgea.core.listener.Listener;
import it.units.malelab.jgea.core.listener.PrintStreamListener;
import it.units.malelab.jgea.core.listener.collector.Basic;
import it.units.malelab.jgea.core.listener.collector.BestInfo;
import it.units.malelab.jgea.core.listener.collector.BestPrinter;
import it.units.malelab.jgea.core.listener.collector.FunctionOfBest;
import it.units.malelab.jgea.core.listener.collector.Diversity;
import it.units.malelab.jgea.core.listener.collector.Population;
import it.units.malelab.jgea.core.listener.collector.Static;
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
import it.units.malelab.jgea.problem.classification.FileRegexClassification;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author eric
 */
public class FileRegex extends Worker {

  public FileRegex(String[] args) throws FileNotFoundException {
    super(args);
  }

  public final static void main(String[] args) throws FileNotFoundException {
    new FileRegex(args);
  }

  @Override
  public void run() {
    try {
      androidRegex(executorService);
    } catch (IOException | InterruptedException | ExecutionException ex) {
      Logger.getLogger(FileRegex.class.getName()).log(Level.SEVERE, "Exception: " + ex, ex);
    }
  }

  private void androidRegex(ExecutorService executor) throws IOException, InterruptedException, ExecutionException {
    int maxDepth = i(a(args, "d", "15"));
    int cacheSize = i(a(args, "cache", "10000"));
    int maxMinutes = i(a(args, "t", "10"));
    int folds = i(a(args, "folds", "5"));
    int fold = i(a(args, "fold", "0"));
    int popSize = i(a(args, "pop", "500"));
    List<Integer> runs = i(l(a(args, "runs", "0")));
    GrammarBasedProblem<String, String, List<Double>> p = new FileRegexClassification(
            a(args, "pFile", ""),
            a(args, "nFile", ""),
            folds, fold,
            Classification.ErrorMetric.BALANCED_ERROR_RATE, Classification.ErrorMetric.CLASS_ERROR_RATE
    );
    Map<GeneticOperator<Node<String>>, Double> operators = new LinkedHashMap<>();
    operators.put(new StandardTreeMutation<>(maxDepth, p.getGrammar()), 0.2d);
    operators.put(new StandardTreeCrossover<>(maxDepth), 0.8d);
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
            distance.cached(cacheSize),
            popSize,
            new RampedHalfAndHalf<>(3, maxDepth, p.getGrammar()),
            new ParetoRanker<>(),
            p.getSolutionMapper(),
            operators,
            new Tournament<>(3),
            new Worst(),
            Lists.newArrayList(new ElapsedTime(maxMinutes, TimeUnit.MINUTES), new PerfectFitness<>(p.getFitnessFunction())),
            cacheSize,
            false
    );
    for (int run : runs) {
      Random random = new Random(run);
      evolver.solve(p, random, executor,
              Listener.onExecutor(listener(
                      new Static(Collections.singletonMap("run", run)),
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
                              ).cached(cacheSize),
                              BestInfo.fromNames((WithNames) ((ProblemWithValidation<String, List<Double>>) p).getValidationFunction()),
                              (n, l) -> "%5.3f"),
                      new FunctionOfBest<>(
                              "validation",
                              ((ProblemWithValidation<String, List<Double>>) p).getValidationFunction().cached(cacheSize),
                              BestInfo.fromNames((WithNames) ((ProblemWithValidation<String, List<Double>>) p).getValidationFunction()),
                              (n, l) -> "%5.3f"),
                      new Diversity(),
                      new BestPrinter(null, "%s")
              ), executor
              )
      );
    }
  }

}
