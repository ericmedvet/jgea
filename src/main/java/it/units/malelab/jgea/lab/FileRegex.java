/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.lab;

import com.google.common.collect.Lists;
import it.units.malelab.jgea.Worker;
import it.units.malelab.jgea.core.Individual;
import it.units.malelab.jgea.representation.tree.Node;
import it.units.malelab.jgea.core.ProblemWithValidation;
import it.units.malelab.jgea.representation.sequence.Sequence;
import it.units.malelab.jgea.core.evolver.DeterministicCrowdingEvolver;
import it.units.malelab.jgea.core.evolver.Evolver;
import it.units.malelab.jgea.core.evolver.StandardEvolver;
import it.units.malelab.jgea.core.evolver.stopcondition.ElapsedTime;
import it.units.malelab.jgea.core.evolver.stopcondition.TargetFitness;
import it.units.malelab.jgea.core.fitness.ClassificationFitness;
import it.units.malelab.jgea.core.function.Reducer;
import it.units.malelab.jgea.core.listener.Listener;
import it.units.malelab.jgea.core.listener.collector.Basic;
import it.units.malelab.jgea.core.listener.collector.BestInfo;
import it.units.malelab.jgea.core.listener.collector.FunctionOfBest;
import it.units.malelab.jgea.core.listener.collector.Diversity;
import it.units.malelab.jgea.core.listener.collector.Population;
import it.units.malelab.jgea.core.listener.collector.Static;
import it.units.malelab.jgea.core.operator.GeneticOperator;
import it.units.malelab.jgea.core.ranker.ParetoRanker;
import it.units.malelab.jgea.core.ranker.selector.Tournament;
import it.units.malelab.jgea.core.ranker.selector.Worst;
import it.units.malelab.jgea.core.util.Pair;
import it.units.malelab.jgea.distance.Distance;
import it.units.malelab.jgea.representation.sequence.Edit;
import it.units.malelab.jgea.representation.grammar.GrammarBasedProblem;
import it.units.malelab.jgea.representation.grammar.cfggp.RampedHalfAndHalf;
import it.units.malelab.jgea.representation.grammar.cfggp.StandardTreeCrossover;
import it.units.malelab.jgea.representation.grammar.cfggp.StandardTreeMutation;
import it.units.malelab.jgea.problem.classification.FileRegexClassification;
import it.units.malelab.jgea.problem.classification.RegexClassification;
import it.units.malelab.jgea.representation.grammar.RegexGrammar;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import static it.units.malelab.jgea.core.util.Args.*;

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
    int maxDepth = i(a("d", "15"));
    int cacheSize = i(a("cache", "10000"));
    int maxMinutes = i(a("t", "10"));
    int folds = i(a("folds", "5"));
    int fold = i(a("fold", "0"));
    int popSize = i(a("pop", "500"));
    List<Integer> runs = i(l(a("runs", "0")));
    List<String> eas = l(a("ea", "standard,dc,fsdc"));
    GrammarBasedProblem<String, String, List<Double>> pOr = new FileRegexClassification(
            a("pFile", ""),
            a("nFile", ""),
            folds, fold,
            ClassificationFitness.Metric.BALANCED_ERROR_RATE, ClassificationFitness.Metric.CLASS_ERROR_RATE,
            RegexGrammar.Option.ANY, RegexGrammar.Option.OR, RegexGrammar.Option.CHAR_CLASS, RegexGrammar.Option.ENHANCED_CONCATENATION
    );
    GrammarBasedProblem<String, String, List<Double>> pNoOr = new FileRegexClassification(
            a("pFile", ""),
            a("nFile", ""),
            folds, fold,
            ClassificationFitness.Metric.BALANCED_ERROR_RATE, ClassificationFitness.Metric.CLASS_ERROR_RATE,
            RegexGrammar.Option.ANY, RegexGrammar.Option.OR, RegexGrammar.Option.CHAR_CLASS, RegexGrammar.Option.ENHANCED_CONCATENATION
    );
    GrammarBasedProblem<String, String, List<Double>> p;
    for (int run : runs) {
      for (String ea : eas) {
        Random random = new Random(run);
        Evolver<Node<String>, String, List<Double>> evolver;
        if (ea.equals("standard")) {
          p = pOr;
          Map<GeneticOperator<Node<String>>, Double> operators = new LinkedHashMap<>();
          operators.put(new StandardTreeMutation<>(maxDepth, p.getGrammar()), 0.2d);
          operators.put(new StandardTreeCrossover<>(maxDepth), 0.8d);
          evolver = new StandardEvolver<>(
                  popSize,
                  new RampedHalfAndHalf<>(3, maxDepth, p.getGrammar()),
                  new ParetoRanker<>(false),
                  p.getSolutionMapper(),
                  operators,
                  new Tournament<>(3),
                  new Worst(),
                  popSize,
                  true,
                  Lists.newArrayList(new ElapsedTime(maxMinutes, TimeUnit.MINUTES), new TargetFitness<>(p.getFitnessFunction())),
                  cacheSize,
                  false
          );
        } else if (ea.equals("dc")) {
          p = pOr;
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
          evolver = new DeterministicCrowdingEvolver<>(
                  distance.cached(cacheSize),
                  popSize,
                  new RampedHalfAndHalf<>(3, maxDepth, p.getGrammar()),
                  new ParetoRanker<>(false),
                  p.getSolutionMapper(),
                  operators,
                  new Tournament<>(3),
                  new Worst(),
                  Lists.newArrayList(new ElapsedTime(maxMinutes, TimeUnit.MINUTES), new TargetFitness<>(p.getFitnessFunction())),
                  cacheSize,
                  false
          );
        } else if (ea.equals("fsdc")) {
          p = pNoOr;
          Map<GeneticOperator<Node<String>>, Double> operators = new LinkedHashMap<>();
          operators.put(new StandardTreeMutation<>(maxDepth, p.getGrammar()), 0.2d);
          operators.put(new StandardTreeCrossover<>(maxDepth), 0.8d);
          Reducer<Pair<String, List<RegexClassification.Label>>> reducer = (p0, p1, listener) -> {
            String s = p0.first() + "|" + p1.first();
            List<RegexClassification.Label> ored = new ArrayList<>(Math.min(p0.second().size(), p1.second().size()));
            for (int i = 0; i < Math.min(p0.second().size(), p1.second().size()); i++) {
              if (p0.second().get(i).equals(RegexClassification.Label.FOUND) || p1.second().get(i).equals(RegexClassification.Label.FOUND)) {
                ored.add(RegexClassification.Label.FOUND);
              } else {
                ored.add(RegexClassification.Label.NOT_FOUND);
              }
            }
            return Pair.build(s, ored);
          };
          Distance<List<RegexClassification.Label>> semanticsDistance = (l1, l2, listener) -> {
            double count = 0;
            for (int i = 0; i < Math.min(l1.size(), l2.size()); i++) {
              if (!l1.get(i).equals(l2.get(i))) {
                count = count + 1d;
              }
            }
            return count / (double) Math.min(l1.size(), l2.size());
          };
          evolver = new FitnessSharingDivideAndConquerEvolver<>(
                  reducer,
                  semanticsDistance,
                  popSize,
                  new RampedHalfAndHalf<>(3, maxDepth, p.getGrammar()),
                  new ParetoRanker<>(false),
                  p.getSolutionMapper(),
                  operators,
                  new Tournament<>(3),
                  new Worst(),
                  popSize,
                  true,
                  Lists.newArrayList(new ElapsedTime(maxMinutes, TimeUnit.MINUTES), new TargetFitness<>(p.getFitnessFunction())),
                  cacheSize
          );
        } else {
          continue;
        }
        Map<String, Object> constants = new TreeMap<>();
        constants.put("run", run);
        constants.put("ea", ea);
        System.out.printf("Starting evolution: %s%n", constants);
        Collection<String> solutions = evolver.solve(p, random, executor,
                Listener.onExecutor(listener(new Static(constants),
                                new Basic(),
                                new Population(),
                                new BestInfo<>((ClassificationFitness)p.getFitnessFunction(), "%5.3f"),
                                new FunctionOfBest<>("best.learning", ((ClassificationFitness)p.getFitnessFunction()).changeMetric(ClassificationFitness.Metric.CLASS_ERROR_RATE).cached(10000), "%5.3f"),
                                new FunctionOfBest<>("best.validation", ((ClassificationFitness)((ProblemWithValidation)p).getValidationFunction()).changeMetric(ClassificationFitness.Metric.CLASS_ERROR_RATE).cached(10000), "%5.3f"),
                                new Diversity()
                        ), executor
                )
        );
        System.out.printf("Found %d solutions: one is %s.%n", solutions.size(), solutions.stream().findFirst().orElse(""));
      }
    }
  }

}
