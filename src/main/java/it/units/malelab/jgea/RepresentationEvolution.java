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
import it.units.malelab.jgea.core.evolver.stopcondition.FitnessEvaluations;
import it.units.malelab.jgea.core.evolver.stopcondition.PerfectFitness;
import it.units.malelab.jgea.core.function.FunctionException;
import it.units.malelab.jgea.core.listener.Listener;
import it.units.malelab.jgea.core.operator.GeneticOperator;
import it.units.malelab.jgea.core.ranker.ComparableRanker;
import it.units.malelab.jgea.core.ranker.FitnessComparator;
import it.units.malelab.jgea.core.ranker.selector.Tournament;
import it.units.malelab.jgea.core.ranker.selector.Worst;
import it.units.malelab.jgea.core.util.Pair;
import it.units.malelab.jgea.distance.Distance;
import it.units.malelab.jgea.distance.Edit;
import it.units.malelab.jgea.distance.Pairwise;
import it.units.malelab.jgea.distance.StringSequence;
import it.units.malelab.jgea.distance.TreeLeaves;
import it.units.malelab.jgea.grammarbased.cfggp.RampedHalfAndHalf;
import it.units.malelab.jgea.grammarbased.cfggp.StandardTreeCrossover;
import it.units.malelab.jgea.grammarbased.cfggp.StandardTreeMutation;
import it.units.malelab.jgea.problem.booleanfunction.EvenParity;
import it.units.malelab.jgea.problem.mapper.EnhancedProblem;
import it.units.malelab.jgea.problem.mapper.FitnessFunction;
import it.units.malelab.jgea.problem.mapper.MapperGeneration;
import it.units.malelab.jgea.problem.mapper.element.Element;
import it.units.malelab.jgea.problem.symbolicregression.Pagie1;
import it.units.malelab.jgea.problem.synthetic.KLandscapes;
import it.units.malelab.jgea.problem.synthetic.Text;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author eric
 */
public class RepresentationEvolution extends Worker {

  public RepresentationEvolution(String[] args) throws FileNotFoundException {
    super(args);
  }

  @Override
  public void run() {
    List<EnhancedProblem> problems = new ArrayList<>();
    try {
      problems.add(new EnhancedProblem(new EvenParity(5), new Pairwise<>(new TreeLeaves<>(new Edit<>())), null));
      problems.add(new EnhancedProblem(new Pagie1(), new Pairwise<>(new TreeLeaves<>(new Edit<>())), null));
      problems.add(new EnhancedProblem(new KLandscapes(5), new Pairwise<>(new TreeLeaves<>(new Edit<>())), null));
      problems.add(new EnhancedProblem(new Text("Hello World!"), new StringSequence(new Edit<>()), null));
    } catch (IOException ex) {
      Logger.getLogger(RepresentationEvolution.class.getName()).log(Level.SEVERE, "Cannot instantiate problems", ex);
      System.exit(-1);
    }
    int learningGenotypeSize = 128;
    int learningN = 100;
    int learningMaxMappingDepth = 9;
    int learningRuns = 1;
    int learningGenerations = 50;
    int learningPopulation = 500;
    int learningDepth = 12;
    int validationGenotypeSize = 128;
    int validationN = 400;
    int validationMaxMappingDepth = 12;
    int validationRuns = 2;
    int validationGenerations = 30;
    int validationPopulation = 250;
    List<FitnessFunction.Property> properties = Arrays.asList(
            FitnessFunction.Property.DEGENERACY,
            FitnessFunction.Property.NON_LOCALITY,
            FitnessFunction.Property.NON_UNIFORMITY);
    //iterate
    for (EnhancedProblem problem : problems) {
      List<EnhancedProblem> learningProblems = new ArrayList<>(problems);
      learningProblems.remove(problem);
      List<EnhancedProblem> validationProblems = Collections.singletonList(problem);
      for (int learningRun = 0; learningRun < learningRuns; learningRun++) {
        for (int p = 1; p <= properties.size(); p++) {
          List<FitnessFunction.Property> localProperties = properties.subList(0, p);
          try {
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
              return dFirst+dSecond;
            };
            DeterministicCrowdingEvolver<Node<String>, Pair<Node<Element>, Node<Element>>, List<Double>> evolver = new DeterministicCrowdingEvolver<>(
                    distance,
                    learningPopulation,
                    new RampedHalfAndHalf<>(3, learningDepth, mapperGeneration.getGrammar()),
                    new ComparableRanker(new FitnessComparator<>()),
                    mapperGeneration.getSolutionMapper(),
                    operators,
                    new Tournament<>(3),
                    new Worst<>(),
                    Lists.newArrayList(new FitnessEvaluations(10000), new PerfectFitness<>(0d)),
                    10000,
                    false
            );
            //I AM HERE
          } catch (IOException ex) {
            Logger.getLogger(RepresentationEvolution.class.getName()).log(Level.SEVERE, "Cannot instantiate MapperGeneration problem.", ex);
          }
        }
      }
    }
  }

}
