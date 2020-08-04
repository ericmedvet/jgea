/*
 * Copyright (C) 2020 Eric Medvet <eric.medvet@gmail.com> (as eric)
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.units.malelab.jgea.lab;

import it.units.malelab.jgea.Worker;
import it.units.malelab.jgea.core.Individual;
import it.units.malelab.jgea.core.order.PartialComparator;
import it.units.malelab.jgea.core.util.Misc;
import it.units.malelab.jgea.representation.tree.Tree;
import it.units.malelab.jgea.core.evolver.StandardEvolver;
import it.units.malelab.jgea.core.evolver.stopcondition.Iterations;
import it.units.malelab.jgea.representation.sequence.bit.BitString;
import it.units.malelab.jgea.representation.sequence.bit.BitStringFactory;
import it.units.malelab.jgea.core.listener.Listener;
import it.units.malelab.jgea.core.listener.MultiFileListenerFactory;
import it.units.malelab.jgea.core.listener.collector.Basic;
import it.units.malelab.jgea.core.listener.collector.BestInfo;
import it.units.malelab.jgea.core.listener.collector.Diversity;
import it.units.malelab.jgea.core.listener.collector.IntrinsicDimension;
import it.units.malelab.jgea.core.listener.collector.Population;
import it.units.malelab.jgea.core.listener.collector.Static;
import it.units.malelab.jgea.core.listener.collector.Suffix;
import it.units.malelab.jgea.representation.sequence.bit.BitFlipMutation;
import it.units.malelab.jgea.core.operator.GeneticOperator;
import it.units.malelab.jgea.representation.sequence.LengthPreservingTwoPointCrossover;
import it.units.malelab.jgea.core.selector.Tournament;
import it.units.malelab.jgea.core.selector.Worst;
import it.units.malelab.jgea.distance.BitStringHamming;
import it.units.malelab.jgea.distance.Distance;
import it.units.malelab.jgea.representation.grammar.GrammarBasedMapper;
import it.units.malelab.jgea.representation.grammar.GrammarBasedProblem;
import it.units.malelab.jgea.representation.grammar.ge.WeightedHierarchicalMapper;
import it.units.malelab.jgea.problem.booleanfunction.EvenParity;
import it.units.malelab.jgea.problem.booleanfunction.element.Element;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static it.units.malelab.jgea.core.util.Args.*;

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
          operators.put(new LengthPreservingTwoPointCrossover<>(Boolean.class), 0.8d);
          StandardEvolver<BitString, List<Tree<Element>>, Double> evolver = new StandardEvolver<>(
              mapper.andThen(problem.getSolutionMapper()),
              new BitStringFactory(genotypeSize),
              PartialComparator.from(Double.class).on(Individual::getFitness),
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
                Listener.onExecutor(listenerFactory.build(
                    new Static(keys),
                    new Basic(),
                    new Population(),
                    new BestInfo("%5.3f"),
                    new Diversity(),
                    new Suffix<>("unique", new IntrinsicDimension<>(hamming, true)),
                    new Suffix<>("all", new IntrinsicDimension<>(hamming, false))
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
