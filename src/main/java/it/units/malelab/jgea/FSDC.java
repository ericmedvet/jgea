/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea;

import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import com.google.common.collect.Sets;
import it.units.malelab.jgea.core.Individual;
import it.units.malelab.jgea.core.Node;
import it.units.malelab.jgea.core.ProblemWithValidation;
import it.units.malelab.jgea.core.Sequence;
import it.units.malelab.jgea.core.evolver.DeterministicCrowdingEvolver;
import it.units.malelab.jgea.core.evolver.Evolver;
import it.units.malelab.jgea.core.evolver.FitnessSharingDivideAndConquerEvolver;
import it.units.malelab.jgea.core.evolver.StandardEvolver;
import it.units.malelab.jgea.core.evolver.stopcondition.ElapsedTime;
import it.units.malelab.jgea.core.evolver.stopcondition.PerfectFitness;
import it.units.malelab.jgea.core.fitness.ClassificationFitness;
import it.units.malelab.jgea.core.function.Function;
import it.units.malelab.jgea.core.function.Reducer;
import it.units.malelab.jgea.core.listener.Listener;
import it.units.malelab.jgea.core.listener.collector.Basic;
import it.units.malelab.jgea.core.listener.collector.BestInfo;
import it.units.malelab.jgea.core.listener.collector.DataCollector;
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
import it.units.malelab.jgea.distance.Edit;
import it.units.malelab.jgea.grammarbased.GrammarBasedProblem;
import it.units.malelab.jgea.grammarbased.cfggp.RampedHalfAndHalf;
import it.units.malelab.jgea.grammarbased.cfggp.StandardTreeCrossover;
import it.units.malelab.jgea.grammarbased.cfggp.StandardTreeMutation;
import it.units.malelab.jgea.problem.classification.BinaryRegexClassification;
import it.units.malelab.jgea.problem.classification.RegexClassification;
import it.units.malelab.jgea.problem.extraction.BinaryRegexExtraction;
import it.units.malelab.jgea.problem.extraction.ExtractionFitness;
import it.units.malelab.jgea.grammarbased.RegexGrammar;
import it.units.malelab.jgea.problem.extraction.ExtractionSetDistance;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author eric
 */
public class FSDC extends Worker {

  private final static Logger L = Logger.getLogger(FSDC.class.getName());

  public FSDC(String[] args) throws FileNotFoundException {
    super(args);
  }

  public final static void main(String[] args) throws FileNotFoundException {
    new FSDC(args);
  }

  @Override
  public void run() {
    int maxDepth = i(a("d", "15"));
    int cacheSize = i(a("cache", "10000"));
    int maxSeconds = i(a("t", "30"));
    int popSize = i(a("pop", "500"));
    List<Integer> runs = i(l(a("runs", "0")));
    List<String> eas = l(a("ea", "standard,dc,fsdc"));
    List<String> problems = l(a("problems", "binRegexClass-100-500,binRegexExtr-20"));

    eas = Collections.singletonList("fsdc");
    problems = l("binRegexClass-100-500,binRegexExtr-20");
//    problems = l("binRegexExtr-20");

    for (int run : runs) {
      for (String p : problems) {
        for (String ea : eas) {
          Map<String, Object> staticInfo = new LinkedHashMap<>();
          staticInfo.put("run", run);
          staticInfo.put("problem", p);
          staticInfo.put("ea", ea);
          L.info(String.format("Preparing %s.", staticInfo));
          //prepare problem
          GrammarBasedProblem problem;
          List<DataCollector> dataCollectors = Lists.newArrayList(
                  new Static(staticInfo),
                  new Basic(),
                  new Population(),
                  new Diversity()
          );
          Function learningAssessmentFunction;
          Function validationAssessmentFunction;
          Distance dcDistance = null;
          Distance semanticsDistance = null;
          Reducer reducer = null;
          if (p(p, 0).equals("binRegexClass")) {
            List<RegexGrammar.Option> options = Lists.newArrayList(RegexGrammar.Option.ANY, RegexGrammar.Option.ENHANCED_CONCATENATION, RegexGrammar.Option.OR);
            if (ea.equals("fsdc")) {
              options.remove(RegexGrammar.Option.OR);
            }
            problem = new BinaryRegexClassification(
                    i(p(p, 1)), i(p(p, 2)), 1,
                    5, 0,
                    ClassificationFitness.Metric.BALANCED_ERROR_RATE, ClassificationFitness.Metric.CLASS_ERROR_RATE,
                    options.toArray(new RegexGrammar.Option[0])
            );
            learningAssessmentFunction = ((ClassificationFitness) problem.getFitnessFunction()).changeMetric(ClassificationFitness.Metric.CLASS_ERROR_RATE);
            validationAssessmentFunction = ((ClassificationFitness) ((ProblemWithValidation) problem).getFitnessFunction()).changeMetric(ClassificationFitness.Metric.CLASS_ERROR_RATE);
          } else if (p(p, 0).equals("binRegexExtr")) {
            List<RegexGrammar.Option> options = Lists.newArrayList(RegexGrammar.Option.ANY, RegexGrammar.Option.ENHANCED_CONCATENATION, RegexGrammar.Option.OR);
            if (ea.equals("fsdc")) {
              options.remove(RegexGrammar.Option.OR);
            }
            problem = new BinaryRegexExtraction(
                    i(p(p, 1)), 1,
                    new HashSet<>(options),
                    5, 0,
                    ExtractionFitness.Metric.ONE_MINUS_FM);
            learningAssessmentFunction = ((ExtractionFitness) problem.getFitnessFunction()).changeMetrics(ExtractionFitness.Metric.ONE_MINUS_FM, ExtractionFitness.Metric.ONE_MINUS_PREC, ExtractionFitness.Metric.ONE_MINUS_REC, ExtractionFitness.Metric.CHAR_ERROR);
            validationAssessmentFunction = ((ExtractionFitness) ((ProblemWithValidation) problem).getValidationFunction()).changeMetrics(ExtractionFitness.Metric.ONE_MINUS_FM, ExtractionFitness.Metric.ONE_MINUS_PREC, ExtractionFitness.Metric.ONE_MINUS_REC, ExtractionFitness.Metric.CHAR_ERROR);
          } else {
            continue;
          }
          //prepare collectors
          if (p(p, 0).equals("binRegexClass") || p(p, 0).equals("binRegexExtr")) {
            dataCollectors.addAll(Arrays.asList(
                    new BestInfo((Function) problem.getFitnessFunction(), "%5.3f"),
                    new FunctionOfBest("best.learning", learningAssessmentFunction, cacheSize, "%5.3f"),
                    new FunctionOfBest("best.validation", validationAssessmentFunction, cacheSize, "%5.3f")
            ));
          }
          //prepare distances and reducer
          if (p(p, 0).equals("binRegexClass") || p(p, 0).equals("binRegexExtr")) {
            Distance<Sequence<Character>> edit = new Edit<>();
            Distance<String> editString = (s1, s2, l) -> (edit.apply(
                    Sequence.from(s1.chars().mapToObj(c -> (char) c).toArray(Character[]::new)),
                    Sequence.from(s2.chars().mapToObj(c -> (char) c).toArray(Character[]::new))
            ));
            Distance<Individual<Node<String>, String, List<Double>>> localDistance = (Individual<Node<String>, String, List<Double>> i1, Individual<Node<String>, String, List<Double>> i2, Listener l) -> (editString.apply(
                    i1.getSolution(),
                    i2.getSolution()
            ));
            dcDistance = localDistance;
          }
          if (p(p, 0).equals("binRegexClass")) {
            Distance<List<RegexClassification.Label>> localSemanticsDistance = (l1, l2, listener) -> {
              double count = 0;
              for (int i = 0; i < Math.min(l1.size(), l2.size()); i++) {
                if (!l1.get(i).equals(l2.get(i))) {
                  count = count + 1d;
                }
              }
              return count / (double) Math.min(l1.size(), l2.size());
            };
            Reducer<Pair<String, List<RegexClassification.Label>>> localReducer = (p0, p1, listener) -> {
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
            semanticsDistance = localSemanticsDistance;
            reducer = localReducer;
          }
          if (p(p, 0).equals("binRegexExtr")) {
            Distance<Set<Range<Integer>>> localSemanticsDistance = new ExtractionSetDistance(
                    ((ExtractionFitness)problem.getFitnessFunction()).getText().length(), 10
            );
            Reducer<Pair<String, Set<Range<Integer>>>> localReducer = (p0, p1, listener) -> Pair.build(
                    p0.first() + "|" + p1.first(),
                    Sets.union(p0.second(), p1.second())
            );
            semanticsDistance = localSemanticsDistance;
            reducer = localReducer;
          }
          //prepare evolver
          Map<GeneticOperator<Node<String>>, Double> operators = new LinkedHashMap<>();
          operators.put(new StandardTreeMutation<>(maxDepth, problem.getGrammar()), 0.2d);
          operators.put(new StandardTreeCrossover<>(maxDepth), 0.8d);
          Random random = new Random(run);
          Evolver evolver;
          if (ea.equals("standard")) {
            evolver = new StandardEvolver<>(
                    popSize,
                    new RampedHalfAndHalf<>(3, maxDepth, problem.getGrammar()),
                    new ParetoRanker<>(),
                    problem.getSolutionMapper(),
                    operators,
                    new Tournament<>(3),
                    new Worst(),
                    popSize,
                    true,
                    Lists.newArrayList(new ElapsedTime(maxSeconds, TimeUnit.SECONDS), new PerfectFitness<>(problem.getFitnessFunction())),
                    cacheSize,
                    false
            );
          } else if (ea.equals("dc")) {
            evolver = new DeterministicCrowdingEvolver<>(
                    dcDistance.cached(cacheSize),
                    popSize,
                    new RampedHalfAndHalf<>(3, maxDepth, problem.getGrammar()),
                    new ParetoRanker<>(),
                    problem.getSolutionMapper(),
                    operators,
                    new Tournament<>(3),
                    new Worst(),
                    Lists.newArrayList(new ElapsedTime(maxSeconds, TimeUnit.SECONDS), new PerfectFitness<>(problem.getFitnessFunction())),
                    cacheSize,
                    false
            );
          } else if (ea.equals("fsdc")) {
            evolver = new FitnessSharingDivideAndConquerEvolver<>(
                    reducer,
                    semanticsDistance,
                    popSize,
                    new RampedHalfAndHalf<>(3, maxDepth, problem.getGrammar()),
                    new ParetoRanker<>(),
                    problem.getSolutionMapper(),
                    operators,
                    new Tournament<>(3),
                    new Worst(),
                    popSize,
                    true,
                    Lists.newArrayList(new ElapsedTime(maxSeconds, TimeUnit.SECONDS), new PerfectFitness<>(problem.getFitnessFunction())),
                    cacheSize
            );
          } else {
            continue;
          }
          try {
            L.info(String.format("Starting %s.", staticInfo));
            Collection solutions = evolver.solve(problem, random, executorService, listener(dataCollectors.toArray(new DataCollector[0])));
            L.info(String.format("Found %d solutions: one is %s.", solutions.size(), solutions.stream().findFirst().orElse("")));
          } catch (InterruptedException | ExecutionException ex) {
            L.log(Level.SEVERE, "Error while evolving!", ex);
          }
        }
      }
    }
  }

}
