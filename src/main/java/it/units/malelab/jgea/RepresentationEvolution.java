/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea;

import com.google.common.collect.Lists;
import it.units.malelab.jgea.core.Individual;
import it.units.malelab.jgea.core.Node;
import it.units.malelab.jgea.core.evolver.DeterministicCrowdingEvolver;
import it.units.malelab.jgea.core.evolver.StandardEvolver;
import it.units.malelab.jgea.core.evolver.stopcondition.Iterations;
import it.units.malelab.jgea.core.evolver.stopcondition.PerfectFitness;
import it.units.malelab.jgea.core.fitness.Linearization;
import it.units.malelab.jgea.core.function.Function;
import it.units.malelab.jgea.core.genotype.BitString;
import it.units.malelab.jgea.core.genotype.BitStringFactory;
import it.units.malelab.jgea.core.listener.Listener;
import it.units.malelab.jgea.core.listener.MultiFileListenerFactory;
import it.units.malelab.jgea.core.listener.collector.Basic;
import it.units.malelab.jgea.core.listener.collector.BestInfo;
import it.units.malelab.jgea.core.listener.collector.BestPrinter;
import it.units.malelab.jgea.core.listener.collector.Diversity;
import it.units.malelab.jgea.core.listener.collector.FunctionOfBest;
import it.units.malelab.jgea.core.listener.collector.Population;
import it.units.malelab.jgea.core.listener.collector.Static;
import it.units.malelab.jgea.core.operator.BitFlipMutation;
import it.units.malelab.jgea.core.operator.GeneticOperator;
import it.units.malelab.jgea.core.operator.LenghtPreservingTwoPointCrossover;
import it.units.malelab.jgea.core.ranker.ComparableRanker;
import it.units.malelab.jgea.core.ranker.FitnessComparator;
import it.units.malelab.jgea.core.ranker.selector.Tournament;
import it.units.malelab.jgea.core.ranker.selector.Worst;
import it.units.malelab.jgea.core.util.Misc;
import it.units.malelab.jgea.core.util.Pair;
import it.units.malelab.jgea.distance.Distance;
import it.units.malelab.jgea.distance.Edit;
import it.units.malelab.jgea.distance.Pairwise;
import it.units.malelab.jgea.distance.StringSequence;
import it.units.malelab.jgea.distance.TreeLeaves;
import it.units.malelab.jgea.grammarbased.GrammarBasedProblem;
import it.units.malelab.jgea.grammarbased.cfggp.RampedHalfAndHalf;
import it.units.malelab.jgea.grammarbased.cfggp.StandardTreeCrossover;
import it.units.malelab.jgea.grammarbased.cfggp.StandardTreeMutation;
import it.units.malelab.jgea.problem.booleanfunction.EvenParity;
import it.units.malelab.jgea.problem.booleanfunction.MultipleOutputParallelMultiplier;
import it.units.malelab.jgea.problem.mapper.EnhancedProblem;
import it.units.malelab.jgea.problem.mapper.FitnessFunction;
import it.units.malelab.jgea.problem.mapper.MapperGeneration;
import it.units.malelab.jgea.problem.mapper.MapperUtils;
import it.units.malelab.jgea.problem.mapper.RecursiveMapper;
import it.units.malelab.jgea.problem.mapper.element.Element;
import it.units.malelab.jgea.problem.symbolicregression.Keijzer6;
import it.units.malelab.jgea.problem.symbolicregression.Nguyen7;
import it.units.malelab.jgea.problem.symbolicregression.Pagie1;
import it.units.malelab.jgea.problem.synthetic.KLandscapes;
import it.units.malelab.jgea.problem.synthetic.Text;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static it.units.malelab.jgea.core.util.Args.*;

/**
 *
 * @author eric
 */
public class RepresentationEvolution extends Worker {

  private final static Logger L = Logger.getLogger(RepresentationEvolution.class.getName());
  private final static long CACHE_SIZE = 10000;

  public final static void main(String[] args) throws FileNotFoundException {
    new RepresentationEvolution(args);
  }

  public RepresentationEvolution(String[] args) throws FileNotFoundException {
    super(args);
  }

  @Override
  public void run() {
    //prepare parameters
    boolean onlyExistingRepresentations = b(a("onlyExisting", "true"));
    List<Integer> learningRuns = i(l(a("runs", "0")));
    int nProperties = i(a("nProps", "1"));
    Map<String, EnhancedProblem> baseProblems = new LinkedHashMap<>();
    Map<String, EnhancedProblem> validationOnlyProblems = new LinkedHashMap<>();
    try {
      baseProblems.put("Parity-3", new EnhancedProblem<>(new EvenParity(3), (Distance) (new Pairwise<>(new TreeLeaves<>(new Edit<>()))).cached(CACHE_SIZE)));
      baseProblems.put("Pagie1", new EnhancedProblem<>(new Pagie1(), (Distance) (new TreeLeaves<>(new Edit<>()).cached(CACHE_SIZE))));
      baseProblems.put("KLandscapes-5", new EnhancedProblem<>(new KLandscapes(5), (Distance) (new TreeLeaves<>(new Edit<>()).cached(CACHE_SIZE))));
      baseProblems.put("Text", new EnhancedProblem<>(new Text("Hello World!"), new StringSequence(new Edit<>()).cached(CACHE_SIZE)));
      validationOnlyProblems.putAll(baseProblems);
      validationOnlyProblems.put("MOPM-3", new EnhancedProblem<>(new MultipleOutputParallelMultiplier(3), (Distance) (new Pairwise<>(new TreeLeaves<>(new Edit<>()))).cached(CACHE_SIZE)));
      validationOnlyProblems.put("Nguyen7", new EnhancedProblem<>(new Nguyen7(1), (Distance) (new TreeLeaves<>(new Edit<>()).cached(CACHE_SIZE))));
      validationOnlyProblems.put("Keijzer6", new EnhancedProblem<>(new Keijzer6(), (Distance) (new TreeLeaves<>(new Edit<>()).cached(CACHE_SIZE))));
      validationOnlyProblems.put("KLandscapes-7", new EnhancedProblem<>(new KLandscapes(7), (Distance) (new TreeLeaves<>(new Edit<>()).cached(CACHE_SIZE))));
    } catch (IOException ex) {
      L.log(Level.SEVERE, "Cannot instantiate problems", ex);
      System.exit(-1);
    }
    int learningGenotypeSize = 64;
    int learningN = 100;
    int learningMaxMappingDepth = 9;
    int learningIterations = 50;
    int learningPopulation = 500;
    int learningDepth = 14;
    int validationGenotypeSize = 256;
    int validationN = 400;
    int validationMaxMappingDepth = 9;
    int validationRuns = 5;
    int validationIterations = 50;
    int validationPopulation = 500;
    List<FitnessFunction.Property> properties = Arrays.asList(
            FitnessFunction.Property.DEGENERACY,
            FitnessFunction.Property.NON_LOCALITY,
            FitnessFunction.Property.NON_UNIFORMITY);
    //prepare things
    List<FitnessFunction.Property> localProperties = properties.subList(0, nProperties);
    MultiFileListenerFactory learningListenerFactory = new MultiFileListenerFactory(a("dir", "."), a("lFile", null));
    MultiFileListenerFactory validationListenerFactory = new MultiFileListenerFactory(a("dir", "."), a("vFile", null));
    if (!onlyExistingRepresentations) {
      //iterate
      for (Map.Entry<String, EnhancedProblem> problemEntry : baseProblems.entrySet()) {
        List<EnhancedProblem> learningProblems = new ArrayList<>(baseProblems.values());
        learningProblems.remove(problemEntry.getValue());
        List<EnhancedProblem> validationProblems = Collections.singletonList(problemEntry.getValue());
        for (int learningRun : learningRuns) {
          try {
            //prepare problem
            MapperGeneration mapperGeneration = new MapperGeneration(
                    learningProblems, learningGenotypeSize, learningN, learningMaxMappingDepth, localProperties,
                    validationProblems, validationGenotypeSize, validationN, validationMaxMappingDepth, properties,
                    learningRun);
            Map<GeneticOperator<Node<String>>, Double> operators = new LinkedHashMap<>();
            operators.put(new StandardTreeMutation<>(learningDepth, mapperGeneration.getGrammar()), 0.2d);
            operators.put(new StandardTreeCrossover<>(learningDepth), 0.8d);
            Distance<Node<Element>> innerDistance = new TreeLeaves<>(new Edit<>());
            Distance<Individual<Node<String>, Pair<Node<Element>, Node<Element>>, List<Double>>> distance = (i1, i2, l) -> {
              double dFirst = innerDistance.apply(i1.getSolution().first(), i2.getSolution().first());
              double dSecond = innerDistance.apply(i1.getSolution().second(), i2.getSolution().second());
              return dFirst + dSecond;
            };
            double[] weights = new double[localProperties.size()];
            //prepare evolver
            Arrays.fill(weights, 1d / (double) localProperties.size());
            DeterministicCrowdingEvolver<Node<String>, Pair<Node<Element>, Node<Element>>, List<Double>> evolver = new DeterministicCrowdingEvolver<>(
                    distance,
                    learningPopulation,
                    new RampedHalfAndHalf<>(3, learningDepth, mapperGeneration.getGrammar()),
                    new ComparableRanker(new FitnessComparator<>(new Linearization(weights))),
                    mapperGeneration.getSolutionMapper(),
                    operators,
                    new Tournament<>(3),
                    new Worst<>(),
                    Lists.newArrayList(new Iterations(learningIterations)),
                    CACHE_SIZE,
                    false
            );
            //evolve
            Map<String, String> keys = new LinkedHashMap<>();
            List<String> learningProblemNames = new ArrayList<>(baseProblems.keySet());
            learningProblemNames.remove(problemEntry.getKey());
            keys.put("learning.problems", learningProblemNames.stream().collect(Collectors.joining("|")));
            keys.put("learning.run", Integer.toString(learningRun));
            keys.put("learning.fitness", localProperties.stream().map(p -> p.toString().toLowerCase()).collect(Collectors.joining("|")));
            Random random = new Random(learningRun);
            System.out.printf("%s%n", keys);
            try {
              Collection<Pair<Node<Element>, Node<Element>>> mapperPairs = evolver.solve(mapperGeneration, random, executorService,
                      Listener.onExecutor(learningListenerFactory.build(
                              new Static(keys),
                              new Basic(),
                              new Population(),
                              new BestInfo<>((FitnessFunction) mapperGeneration.getFitnessFunction(), "%5.3f"),
                              new FunctionOfBest("best.validation", (FitnessFunction) mapperGeneration.getValidationFunction(), 10000, "%5.3f"),
                              new Diversity(),
                              new BestPrinter()
                      ), executorService
                      ));
              Pair<Node<Element>, Node<Element>> mapperPair = Misc.first(mapperPairs);
              for (Map.Entry<String, EnhancedProblem> innerProblemEntry : validationOnlyProblems.entrySet()) {
                doValidation(mapperPair, innerProblemEntry, validationRuns, validationMaxMappingDepth, validationPopulation, validationGenotypeSize, validationIterations, keys, validationListenerFactory);
              }
            } catch (InterruptedException | ExecutionException ex) {
              L.log(Level.SEVERE, String.format("Cannot solve learning run: %s", ex), ex);
            }
          } catch (IOException ex) {
            L.log(Level.SEVERE, String.format("Cannot instantiate MapperGeneration problem: %s", ex), ex);
          }
        }
      }
    } else {
      List<String> mappers = Arrays.asList("ge", "ge-opt", "hge", "whge");
      for (String mapper : mappers) {
        for (Map.Entry<String, EnhancedProblem> problemEntry : baseProblems.entrySet()) {
          Pair<Node<Element>, Node<Element>> mapperPair = getMapper(mapper, problemEntry.getValue().getProblem());
          FitnessFunction fitnessFunction = new FitnessFunction(
                  Collections.singletonList(problemEntry.getValue()),
                  validationGenotypeSize,
                  validationN,
                  validationMaxMappingDepth,
                  properties,
                  1);
          List<Double> values = fitnessFunction.apply(mapperPair);
          System.out.printf("%s;%s;%f;%f;%f%n",
                  mapper,
                  problemEntry.getKey(),
                  values.get(0),
                  values.get(1),
                  values.get(2)
          );
        }
        for (Map.Entry<String, EnhancedProblem> innerProblemEntry : validationOnlyProblems.entrySet()) {
          Pair<Node<Element>, Node<Element>> mapperPair = getMapper(mapper, innerProblemEntry.getValue().getProblem());
          Map<String, String> keys = new LinkedHashMap<>();
          keys.put("mapper", mapper);
          try {
            doValidation(mapperPair, innerProblemEntry, validationRuns, validationMaxMappingDepth, validationPopulation, validationGenotypeSize, validationIterations, keys, validationListenerFactory);
          } catch (ExecutionException | InterruptedException ex) {
            L.log(Level.SEVERE, String.format("Cannot solve validation run: %s", ex), ex);
          }
        }
      }
    }
  }

  private Pair<Node<Element>, Node<Element>> getMapper(String mapper, GrammarBasedProblem problem) {
    Node<String> rawMappingTree = null;
    if (mapper.equals("ge")) {
      rawMappingTree = MapperUtils.getGERawTree(8);
    } else if (mapper.equals("ge-opt")) {
      rawMappingTree = MapperUtils.getGERawTree((int) Math.ceil(Math.log(
              (double) problem.getGrammar().getRules().values().stream().mapToInt((l -> ((List) l).size())).max().getAsInt()
      ) / Math.log(2d))
      );
    } else if (mapper.equals("hge")) {
      rawMappingTree = MapperUtils.getHGERawTree();
    } else if (mapper.equals("whge")) {
      rawMappingTree = MapperUtils.getWHGERawTree();
    } else {
      return null;
    }
    Node<Element> optionChooser = MapperUtils.transform(rawMappingTree.getChildren().get(0));
    Node<Element> genoAssigner = MapperUtils.transform(rawMappingTree.getChildren().get(1));
    optionChooser.propagateParentship();
    genoAssigner.propagateParentship();
    return Pair.build(optionChooser, genoAssigner);
  }

  private void doValidation(
          Pair<Node<Element>, Node<Element>> mapperPair,
          Map.Entry<String, EnhancedProblem> innerProblemEntry,
          int validationRuns,
          int validationMaxMappingDepth,
          int validationPopulation,
          int validationGenotypeSize,
          int validationIterations,
          Map<String, String> keys,
          MultiFileListenerFactory validationListenerFactory) throws ExecutionException, InterruptedException {
    //iterate on problems
    Map<GeneticOperator<BitString>, Double> innerOperators = new LinkedHashMap<>();
    innerOperators.put(new BitFlipMutation(0.01d), 0.2d);
    innerOperators.put(new LenghtPreservingTwoPointCrossover(), 0.8d);
    for (int validationRun = 0; validationRun < validationRuns; validationRun++) {
      //prepare mapper
      RecursiveMapper recursiveMapper = new RecursiveMapper<>(
              mapperPair.first(),
              mapperPair.second(),
              validationMaxMappingDepth,
              2,
              innerProblemEntry.getValue().getProblem().getGrammar());
      //prepare evolver
      StandardEvolver innerEvolver = new StandardEvolver(
              validationPopulation,
              new BitStringFactory(validationGenotypeSize),
              new ComparableRanker(new FitnessComparator<>(Function.identity())),
              recursiveMapper.andThen(innerProblemEntry.getValue().getProblem().getSolutionMapper()),
              innerOperators,
              new Tournament<>(3),
              new Worst<>(),
              validationPopulation,
              true,
              Lists.newArrayList(
                      new Iterations(validationIterations),
                      new PerfectFitness<>(innerProblemEntry.getValue().getProblem().getFitnessFunction())),
              CACHE_SIZE,
              false
      );
      //solve validation
      Map<String, String> innerKeys = new LinkedHashMap<>();
      innerKeys.put("validation.problem", innerProblemEntry.getKey());
      innerKeys.put("validation.run", Integer.toString(validationRun));
      System.out.printf("\t%s%n", innerKeys);
      innerKeys.putAll(keys);
      Random innerRandom = new Random(validationRun);
      innerEvolver.solve(innerProblemEntry.getValue().getProblem(), innerRandom, executorService, Listener.onExecutor(validationListenerFactory.build(
              new Static(innerKeys),
              new Basic(),
              new Population(),
              new BestInfo("%5.3f"),
              new Diversity()
      ), executorService
      ));
    }
  }

}
