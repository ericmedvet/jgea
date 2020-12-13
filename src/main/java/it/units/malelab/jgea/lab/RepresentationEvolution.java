/*
 * Copyright 2020 Eric Medvet <eric.medvet@gmail.com> (as eric)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it.units.malelab.jgea.lab;

import it.units.malelab.jgea.Worker;
import it.units.malelab.jgea.core.Individual;
import it.units.malelab.jgea.core.evolver.Evolver;
import it.units.malelab.jgea.core.evolver.StandardEvolver;
import it.units.malelab.jgea.core.evolver.StandardWithEnforcedDiversityEvolver;
import it.units.malelab.jgea.core.evolver.stopcondition.Iterations;
import it.units.malelab.jgea.core.fitness.Linearization;
import it.units.malelab.jgea.core.listener.Listener;
import it.units.malelab.jgea.core.listener.MultiFileListenerFactory;
import it.units.malelab.jgea.core.listener.collector.*;
import it.units.malelab.jgea.core.operator.GeneticOperator;
import it.units.malelab.jgea.core.order.PartialComparator;
import it.units.malelab.jgea.core.selector.Tournament;
import it.units.malelab.jgea.core.selector.Worst;
import it.units.malelab.jgea.core.util.Misc;
import it.units.malelab.jgea.core.util.Pair;
import it.units.malelab.jgea.distance.*;
import it.units.malelab.jgea.problem.booleanfunction.EvenParity;
import it.units.malelab.jgea.problem.booleanfunction.MultipleOutputParallelMultiplier;
import it.units.malelab.jgea.problem.mapper.*;
import it.units.malelab.jgea.problem.mapper.element.Element;
import it.units.malelab.jgea.problem.symbolicregression.*;
import it.units.malelab.jgea.problem.synthetic.KLandscapes;
import it.units.malelab.jgea.problem.synthetic.Text;
import it.units.malelab.jgea.representation.grammar.Grammar;
import it.units.malelab.jgea.representation.grammar.GrammarBasedProblem;
import it.units.malelab.jgea.representation.grammar.cfggp.GrammarBasedSubtreeMutation;
import it.units.malelab.jgea.representation.grammar.cfggp.GrammarRampedHalfAndHalf;
import it.units.malelab.jgea.representation.sequence.SameTwoPointsCrossover;
import it.units.malelab.jgea.representation.sequence.bit.BitFlipMutation;
import it.units.malelab.jgea.representation.sequence.bit.BitString;
import it.units.malelab.jgea.representation.sequence.bit.BitStringFactory;
import it.units.malelab.jgea.representation.tree.SameRootSubtreeCrossover;
import it.units.malelab.jgea.representation.tree.Tree;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static it.units.malelab.jgea.core.util.Args.*;

/**
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
    Map<String, EnhancedProblem<?, ?, ?>> baseProblems = new LinkedHashMap<>();
    Map<String, EnhancedProblem<?, ?, ?>> validationOnlyProblems = new LinkedHashMap<>();
    try {
      baseProblems.put("Parity-3", new EnhancedProblem<>(new EvenParity(3), Misc.cached(new Pairwise<>(new TreeLeaves<>(new Edit<>())), CACHE_SIZE)));
      baseProblems.put("Pagie1", from(new Pagie1(SymbolicRegressionFitness.Metric.MSE), Grammar.fromFile(new File("grammars/symbolic-regression-pagie1.bnf")), "x", "y"));
      baseProblems.put("KLandscapes-5", new EnhancedProblem<>(new KLandscapes(5), Misc.cached(new TreeLeaves<>(new Edit<>()), CACHE_SIZE)));
      baseProblems.put("Text", new EnhancedProblem<>(new Text("Hello World!"), Misc.cached(new StringSequence(new Edit<>()), CACHE_SIZE)));
      validationOnlyProblems.putAll(baseProblems);
      validationOnlyProblems.put("MOPM-3", new EnhancedProblem<>(new MultipleOutputParallelMultiplier(3), Misc.cached((new Pairwise<>(new TreeLeaves<>(new Edit<>()))), CACHE_SIZE)));
      baseProblems.put("Nguyen7", from(new Nguyen7(SymbolicRegressionFitness.Metric.MSE, 1), Grammar.fromFile(new File("grammars/symbolic-regression-nguyen7.bnf")), "x"));
      baseProblems.put("Keijzer6", from(new Keijzer6(SymbolicRegressionFitness.Metric.MSE), Grammar.fromFile(new File("grammars/symbolic-regression-keijzer6.bnf")), "x"));
      validationOnlyProblems.put("KLandscapes-7", new EnhancedProblem<>(new KLandscapes(7), Misc.cached(new TreeLeaves<>(new Edit<>()), CACHE_SIZE)));
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
    MultiFileListenerFactory<Object, Object, Object> learningListenerFactory = new MultiFileListenerFactory<>(a("dir", "."), a("lFile", null));
    MultiFileListenerFactory<Object, Object, Object> validationListenerFactory = new MultiFileListenerFactory<>(a("dir", "."), a("vFile", null));
    if (!onlyExistingRepresentations) {
      //iterate
      for (Map.Entry<String, EnhancedProblem<?, ?, ?>> problemEntry : baseProblems.entrySet()) {
        List<EnhancedProblem<?, ?, ?>> learningProblems = new ArrayList<>(baseProblems.values());
        learningProblems.remove(problemEntry.getValue());
        List<EnhancedProblem<?, ?, ?>> validationProblems = Collections.singletonList(problemEntry.getValue());
        for (int learningRun : learningRuns) {
          try {
            //prepare problem
            MapperGeneration mapperGeneration = new MapperGeneration(
                (List) learningProblems, learningGenotypeSize, learningN, learningMaxMappingDepth, localProperties,
                (List) validationProblems, validationGenotypeSize, validationN, validationMaxMappingDepth, properties,
                learningRun);
            Map<GeneticOperator<Tree<String>>, Double> operators = new LinkedHashMap<>();
            operators.put(new GrammarBasedSubtreeMutation<>(learningDepth, mapperGeneration.getGrammar()), 0.2d);
            operators.put(new SameRootSubtreeCrossover<>(learningDepth), 0.8d);
            Distance<Tree<Element>> innerDistance = new TreeLeaves<>(new Edit<>());
            Distance<Individual<Tree<String>, Pair<Tree<Element>, Tree<Element>>, List<Double>>> distance = (i1, i2) -> {
              double dFirst = innerDistance.apply(i1.getSolution().first(), i2.getSolution().first());
              double dSecond = innerDistance.apply(i1.getSolution().second(), i2.getSolution().second());
              return dFirst + dSecond;
            };
            double[] weights = new double[localProperties.size()];
            //prepare evolver
            Arrays.fill(weights, 1d / (double) localProperties.size());
            Evolver<Tree<String>, Pair<Tree<Element>, Tree<Element>>, List<Double>> evolver = new StandardWithEnforcedDiversityEvolver<>(
                mapperGeneration.getSolutionMapper(),
                new GrammarRampedHalfAndHalf<>(3, learningDepth, mapperGeneration.getGrammar()),
                PartialComparator.from(Double.class).comparing(new Linearization(weights).compose(Individual::getFitness)),
                learningPopulation,
                operators,
                new Tournament(3),
                new Worst(),
                learningPopulation,
                true,
                learningPopulation //max attempts
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
              Listener<Object, Object, Object> listener = learningListenerFactory.build(List.of(
                  new Static(keys),
                  new Basic(),
                  new Population(),
                  new BestInfo("%5.3f"),
                  //new FunctionOfOneBest<>("best.validation", (FitnessFunction) mapperGeneration.getValidationFunction().cached(10000), "%5.3f"),
                  new Diversity(),
                  new BestPrinter(BestPrinter.Part.GENOTYPE)
              ));
              Collection<Pair<Tree<Element>, Tree<Element>>> mapperPairs = evolver.solve(
                  mapperGeneration.getFitnessFunction(), new Iterations(learningIterations), random, executorService,
                  Listener.onExecutor(listener, executorService)
              );
              Pair<Tree<Element>, Tree<Element>> mapperPair = Misc.first(mapperPairs);
              for (Map.Entry<String, EnhancedProblem<?, ?, ?>> innerProblemEntry : validationOnlyProblems.entrySet()) {
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
        for (Map.Entry<String, EnhancedProblem<?, ?, ?>> problemEntry : baseProblems.entrySet()) {
          Pair<Tree<Element>, Tree<Element>> mapperPair = getMapper(mapper, problemEntry.getValue().getProblem());
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
        for (Map.Entry<String, EnhancedProblem<?, ?, ?>> innerProblemEntry : validationOnlyProblems.entrySet()) {
          Pair<Tree<Element>, Tree<Element>> mapperPair = getMapper(mapper, innerProblemEntry.getValue().getProblem());
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

  private Pair<Tree<Element>, Tree<Element>> getMapper(String mapper, GrammarBasedProblem problem) {
    Tree<String> rawMappingTree = null;
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
    Tree<Element> optionChooser = MapperUtils.transform(rawMappingTree.child(0));
    Tree<Element> genoAssigner = MapperUtils.transform(rawMappingTree.child(1));
    return Pair.of(optionChooser, genoAssigner);
  }

  private void doValidation(
      Pair<Tree<Element>, Tree<Element>> mapperPair,
      Map.Entry<String, EnhancedProblem<?, ?, ?>> innerProblemEntry,
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
    innerOperators.put(new SameTwoPointsCrossover(new BitStringFactory(1)), 0.8d);
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
          recursiveMapper.andThen(innerProblemEntry.getValue().getProblem().getSolutionMapper()),
          new BitStringFactory(validationGenotypeSize),
          PartialComparator.from(Double.class).comparing((Function<Individual<?, ?, Double>, Double>) Individual::getFitness),
          validationPopulation,
          innerOperators,
          new Tournament(3),
          new Worst(),
          validationPopulation,
          true
      );
      //solve validation
      Map<String, String> innerKeys = new LinkedHashMap<>();
      innerKeys.put("validation.problem", innerProblemEntry.getKey());
      innerKeys.put("validation.run", Integer.toString(validationRun));
      System.out.printf("\t%s%n", innerKeys);
      innerKeys.putAll(keys);
      Random innerRandom = new Random(validationRun);
      innerEvolver.solve(
          innerProblemEntry.getValue().getProblem().getFitnessFunction(), new Iterations(validationIterations),
          innerRandom, executorService, Listener.onExecutor(validationListenerFactory.build(List.of(
              new Static(innerKeys),
              new Basic(),
              new Population(),
              new BestInfo("%5.3f"),
              new Diversity()
              )), executorService
          ));
    }
  }

  private static EnhancedProblem<String, RealFunction, Double> from(SymbolicRegressionProblem p, Grammar<String> g, String... vars) {
    Distance<Tree<it.units.malelab.jgea.problem.symbolicregression.element.Element>> nodeDistance = Misc.cached(new TreeLeaves<>(new Edit<>()), CACHE_SIZE);
    Distance<RealFunction> d = (f1, f2) -> nodeDistance.apply(
        ((TreeBasedRealFunction) f1).getNode(),
        ((TreeBasedRealFunction) f2).getNode()
    );
    return new EnhancedProblem<>(
        new GrammarBasedProblem<String, RealFunction, Double>() {
          @Override
          public Grammar<String> getGrammar() {
            return g;
          }

          @Override
          public Function<Tree<String>, RealFunction> getSolutionMapper() {
            return new FormulaMapper().andThen(n -> TreeBasedRealFunction.from(n, vars));
          }

          @Override
          public Function<RealFunction, Double> getFitnessFunction() {
            return p.getFitnessFunction();
          }
        },
        d
    );
  }

}
