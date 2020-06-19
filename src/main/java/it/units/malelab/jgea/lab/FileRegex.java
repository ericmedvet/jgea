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
import it.units.malelab.jgea.core.evolver.stopcondition.Iterations;
import it.units.malelab.jgea.core.listener.collector.*;
import it.units.malelab.jgea.core.order.FitnessComparator;
import it.units.malelab.jgea.core.order.ParetoDominance;
import it.units.malelab.jgea.representation.tree.Node;
import it.units.malelab.jgea.core.evolver.Evolver;
import it.units.malelab.jgea.core.evolver.StandardEvolver;
import it.units.malelab.jgea.core.fitness.ClassificationFitness;
import it.units.malelab.jgea.core.listener.Listener;
import it.units.malelab.jgea.core.operator.GeneticOperator;
import it.units.malelab.jgea.core.selector.Tournament;
import it.units.malelab.jgea.core.selector.Worst;
import it.units.malelab.jgea.representation.grammar.GrammarBasedProblem;
import it.units.malelab.jgea.representation.grammar.cfggp.RampedHalfAndHalf;
import it.units.malelab.jgea.representation.grammar.cfggp.StandardTreeCrossover;
import it.units.malelab.jgea.representation.grammar.cfggp.StandardTreeMutation;
import it.units.malelab.jgea.problem.classification.FileRegexClassification;
import it.units.malelab.jgea.representation.grammar.RegexGrammar;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

import static it.units.malelab.jgea.core.util.Args.*;

/**
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
              p.getSolutionMapper(),
              new RampedHalfAndHalf<>(3, maxDepth, p.getGrammar()),
              new FitnessComparator<>(new ParetoDominance<>()),
              popSize,
              operators,
              new Tournament(3),
              new Worst(),
              popSize,
              true
          );
        } else {
          System.out.println("Unknown evolver: " + ea);
          continue;
        }
        Map<String, Object> constants = new TreeMap<>();
        constants.put("run", run);
        constants.put("ea", ea);
        System.out.printf("Starting evolution: %s%n", constants);
        Collection<String> solutions = evolver.solve(p, new Iterations(100), random, executor, Listener.onExecutor(listener(
            new Static(constants),
            new Basic(),
            new Population(),
            new BestInfo(),
            new BestPrinter(Set.of(BestPrinter.Part.SOLUTION))
        ), executor));
        System.out.printf("Found %d solutions: one is %s.%n", solutions.size(), solutions.stream().findFirst().orElse(""));
      }
    }
  }

}
