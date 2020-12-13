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
import it.units.malelab.jgea.core.evolver.StandardEvolver;
import it.units.malelab.jgea.core.evolver.stopcondition.Iterations;
import it.units.malelab.jgea.core.listener.Listener;
import it.units.malelab.jgea.core.listener.MultiFileListenerFactory;
import it.units.malelab.jgea.core.listener.collector.*;
import it.units.malelab.jgea.core.operator.GeneticOperator;
import it.units.malelab.jgea.core.order.PartialComparator;
import it.units.malelab.jgea.core.selector.Tournament;
import it.units.malelab.jgea.core.selector.Worst;
import it.units.malelab.jgea.core.util.Misc;
import it.units.malelab.jgea.distance.BitStringHamming;
import it.units.malelab.jgea.distance.Distance;
import it.units.malelab.jgea.problem.booleanfunction.EvenParity;
import it.units.malelab.jgea.problem.booleanfunction.element.Element;
import it.units.malelab.jgea.representation.grammar.GrammarBasedMapper;
import it.units.malelab.jgea.representation.grammar.GrammarBasedProblem;
import it.units.malelab.jgea.representation.grammar.ge.WeightedHierarchicalMapper;
import it.units.malelab.jgea.representation.sequence.SameTwoPointsCrossover;
import it.units.malelab.jgea.representation.sequence.bit.BitFlipMutation;
import it.units.malelab.jgea.representation.sequence.bit.BitString;
import it.units.malelab.jgea.representation.sequence.bit.BitStringFactory;
import it.units.malelab.jgea.representation.tree.Tree;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static it.units.malelab.jgea.core.util.Args.i;
import static it.units.malelab.jgea.core.util.Args.l;

/**
 * @author eric
 */
public class IntrinsicDimensionAssessment extends Worker {

  private final static Logger L = Logger.getLogger(IntrinsicDimensionAssessment.class.getName());
  private final static long CACHE_SIZE = 10000;

  public static void main(String[] args) throws FileNotFoundException {
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
    MultiFileListenerFactory<BitString, Object, Object> listenerFactory = new MultiFileListenerFactory<>(a("dir", "."), a("file", null));
    Distance<BitString> hamming = Misc.cached(new BitStringHamming(), CACHE_SIZE);
    //iterate
    for (int k : ks) {
      try {
        //GrammarBasedProblem<String, Node<String>, Double> problem = new KLandscapes(k);
        GrammarBasedProblem<String, List<Tree<Element>>, Double> problem = new EvenParity(k);
        for (int run : runs) {
          GrammarBasedMapper<BitString, String> mapper = new WeightedHierarchicalMapper<>(2, problem.getGrammar());
          Map<GeneticOperator<BitString>, Double> operators = new LinkedHashMap<>();
          operators.put(new BitFlipMutation(0.01d), 0.2d);
          operators.put(new SameTwoPointsCrossover<>(new BitStringFactory(genotypeSize)), 0.8d);
          StandardEvolver<BitString, List<Tree<Element>>, Double> evolver = new StandardEvolver<>(
              mapper.andThen(problem.getSolutionMapper()),
              new BitStringFactory(genotypeSize),
              PartialComparator.from(Double.class).comparing(Individual::getFitness),
              population,
              operators,
              new Tournament(3),
              new Worst(),
              population,
              true
          );
          Map<String, String> keys = new LinkedHashMap<>();
          keys.put("run", Integer.toString(run));
          keys.put("k", Integer.toString(k));
          keys.put("genotype.size", Integer.toString(genotypeSize));
          keys.put("population.size", Integer.toString(population));
          System.out.println(keys);
          Random random = new Random(run);
          try {
            evolver.solve(problem.getFitnessFunction(), new Iterations(iterations), random, executorService,
                Listener.onExecutor(listenerFactory.build(List.of(
                    new Static(keys),
                    new Basic(),
                    new Population(),
                    new BestInfo("%5.3f"),
                    new Diversity(),
                    new Suffix<>("unique", new IntrinsicDimension<>(hamming, true)),
                    new Suffix<>("all", new IntrinsicDimension<>(hamming, false))
                    )), executorService
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
