/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea;

import com.google.common.collect.Lists;
import it.units.malelab.jgea.core.Node;
import it.units.malelab.jgea.core.evolver.StandardEvolver;
import it.units.malelab.jgea.core.evolver.stopcondition.Iterations;
import it.units.malelab.jgea.core.function.Function;
import it.units.malelab.jgea.core.genotype.BitString;
import it.units.malelab.jgea.core.genotype.BitStringFactory;
import it.units.malelab.jgea.core.listener.Listener;
import it.units.malelab.jgea.core.listener.MultiFileListenerFactory;
import it.units.malelab.jgea.core.listener.collector.Basic;
import it.units.malelab.jgea.core.listener.collector.BestInfo;
import it.units.malelab.jgea.core.listener.collector.Diversity;
import it.units.malelab.jgea.core.listener.collector.IntrinsicDimension;
import it.units.malelab.jgea.core.listener.collector.Population;
import it.units.malelab.jgea.core.listener.collector.Static;
import it.units.malelab.jgea.core.listener.collector.Suffix;
import it.units.malelab.jgea.core.operator.BitFlipMutation;
import it.units.malelab.jgea.core.operator.GeneticOperator;
import it.units.malelab.jgea.core.operator.LenghtPreservingTwoPointCrossover;
import it.units.malelab.jgea.core.ranker.ComparableRanker;
import it.units.malelab.jgea.core.ranker.FitnessComparator;
import it.units.malelab.jgea.core.ranker.selector.Tournament;
import it.units.malelab.jgea.core.ranker.selector.Worst;
import it.units.malelab.jgea.distance.BitStringHamming;
import it.units.malelab.jgea.distance.Distance;
import it.units.malelab.jgea.grammarbased.GrammarBasedMapper;
import it.units.malelab.jgea.grammarbased.GrammarBasedProblem;
import it.units.malelab.jgea.grammarbased.ge.WeightedHierarchicalMapper;
import it.units.malelab.jgea.problem.booleanfunction.EvenParity;
import it.units.malelab.jgea.problem.booleanfunction.element.Element;
import it.units.malelab.jgea.problem.synthetic.KLandscapes;
import java.io.FileNotFoundException;
import java.io.IOException;
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
public class IntrinsicDimensionAssessment extends Worker {

  private final static Logger L = Logger.getLogger(IntrinsicDimensionAssessment.class.getName());
  private final static long CACHE_SIZE = 10000;

  public final static void main(String[] args) throws FileNotFoundException {
    new IntrinsicDimensionAssessment(args);
  }

  public IntrinsicDimensionAssessment(String[] args) throws FileNotFoundException {
    super(args);
  }

  @Override
  public void run() {
    //prepare parameters
    List<Integer> runs = i(l(a("runs", "0")));
    List<Integer> ks = i(l(a("ks", "3,4,5,6,7")));
    int genotypeSize = 256;
    int iterations = 50;
    int population = 500;
    //prepare things
    MultiFileListenerFactory listenerFactory = new MultiFileListenerFactory(a("dir", "."), a("file", null));
    Distance<BitString> hamming = (new BitStringHamming()).cached(CACHE_SIZE);
    //iterate
    for (int k : ks) {
      try {
        //GrammarBasedProblem<String, Node<String>, Double> problem = new KLandscapes(k);
        GrammarBasedProblem<String, List<Node<Element>>, Double> problem = new EvenParity(k);
        for (int run : runs) {
          GrammarBasedMapper<BitString, String> mapper = new WeightedHierarchicalMapper<>(2, problem.getGrammar());
          Map<GeneticOperator<BitString>, Double> operators = new LinkedHashMap<>();
          operators.put(new BitFlipMutation(0.01d), 0.2d);
          operators.put(new LenghtPreservingTwoPointCrossover(), 0.8d);
          StandardEvolver<BitString, List<Node<Element>>, Double> evolver = new StandardEvolver<>(
                  population,
                  new BitStringFactory(genotypeSize),
                  new ComparableRanker(new FitnessComparator<>(Function.identity())),
                  mapper.andThen(problem.getSolutionMapper()),
                  operators,
                  new Tournament<>(3),
                  new Worst<>(),
                  population,
                  true,
                  Lists.newArrayList(new Iterations(iterations)),
                  CACHE_SIZE,
                  false
          );
          Map<String, String> keys = new LinkedHashMap<>();
          keys.put("run", Integer.toString(run));
          keys.put("k", Integer.toString(k));
          keys.put("genotype.size", Integer.toString(genotypeSize));
          keys.put("population.size", Integer.toString(population));
          System.out.println(keys);
          Random random = new Random(run);
          try {
            evolver.solve(problem, random, executorService,
                    Listener.onExecutor(listenerFactory.build(
                            new Static(keys),
                            new Basic(),
                            new Population(),
                            new BestInfo<>("%5.3f"),
                            new Diversity(),
                            new Suffix("unique", new IntrinsicDimension(hamming, true)),
                            new Suffix("all", new IntrinsicDimension(hamming, false))
                    ), executorService
                    ));
          } catch (InterruptedException | ExecutionException ex) {
            L.log(Level.SEVERE, String.format("Cannot solve problem: %s", ex), ex);
          }
        }
      } catch (IOException ex) {
        L.log(Level.SEVERE, String.format("Cannot prepare problem: %s", ex), ex);
      }
    }
  }

}
