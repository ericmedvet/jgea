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

import com.google.common.base.Stopwatch;
import it.units.malelab.jgea.Worker;
import it.units.malelab.jgea.core.Individual;
import it.units.malelab.jgea.core.evolver.Evolver;
import it.units.malelab.jgea.core.evolver.StandardEvolver;
import it.units.malelab.jgea.core.evolver.StandardWithEnforcedDiversityEvolver;
import it.units.malelab.jgea.core.evolver.stopcondition.Iterations;
import it.units.malelab.jgea.core.listener.Listener;
import it.units.malelab.jgea.core.listener.MultiFileListenerFactory;
import it.units.malelab.jgea.core.listener.collector.*;
import it.units.malelab.jgea.core.order.PartialComparator;
import it.units.malelab.jgea.core.selector.Tournament;
import it.units.malelab.jgea.core.selector.Worst;
import it.units.malelab.jgea.core.util.Misc;
import it.units.malelab.jgea.problem.extraction.ExtractionFitness;
import it.units.malelab.jgea.problem.extraction.ExtractionProblem;
import it.units.malelab.jgea.problem.extraction.Extractor;
import it.units.malelab.jgea.problem.extraction.string.RegexExtractionProblem;
import it.units.malelab.jgea.problem.extraction.string.RegexGrammar;
import it.units.malelab.jgea.problem.symbolicregression.*;
import it.units.malelab.jgea.representation.grammar.cfggp.GrammarBasedSubtreeMutation;
import it.units.malelab.jgea.representation.grammar.cfggp.GrammarRampedHalfAndHalf;
import it.units.malelab.jgea.representation.tree.SameRootSubtreeCrossover;
import it.units.malelab.jgea.representation.tree.Tree;

import java.io.FileNotFoundException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import static it.units.malelab.jgea.core.util.Args.*;

/**
 * @author eric
 * @created 2020/08/14
 * @project jgea
 */
public class ExtractionComparison extends Worker {

  public ExtractionComparison(String[] args) throws FileNotFoundException {
    super(args);
  }

  public static void main(String[] args) throws FileNotFoundException {
    new ExtractionComparison(args);
  }

  @Override
  public void run() {
    int nPop = i(a("nPop", "100"));
    int maxHeight = i(a("maxHeight", "10"));
    int maxNodes = i(a("maxNodes", "20"));
    int nTournament = 5;
    int diversityMaxAttempts = 100;
    int nIterations = i(a("nIterations", "50"));
    String evolverNamePattern = a("evolver", ".*");
    int[] seeds = ri(a("seed", "0:1"));
    double graphEdgeAdditionRate = 3d;
    double graphEdgeMutationRate = 1d;
    double graphEdgeRemovalRate = 0d;
    double graphNodeAdditionRate = 1d;
    double graphCrossoverRate = 1d;
    Set<RegexGrammar.Option> options = Set.of(RegexGrammar.Option.QUANTIFIERS, RegexGrammar.Option.NON_CAPTURING_GROUP, RegexGrammar.Option.OR, RegexGrammar.Option.ANY); // TODO find a way to make faster when extracting null string
    ExtractionFitness.Metric metric = ExtractionFitness.Metric.ONE_MINUS_FM;
    Map<String, RegexExtractionProblem> problems = Map.of(
        "ternary-5", RegexExtractionProblem.ternary(5, 1, metric)
    );
    MultiFileListenerFactory<Object, RealFunction, Double> listenerFactory = new MultiFileListenerFactory<>(
        a("dir", "."),
        a("file", null)
    );
    Map<String, Function<RegexExtractionProblem, Evolver<?, Extractor<Character>, Double>>> evolvers = new TreeMap<>(Map.ofEntries(
        Map.entry("cfgtree-ga", p -> {
          RegexGrammar g = new RegexGrammar(List.of(p.getText()), options);
          return new StandardWithEnforcedDiversityEvolver<Tree<String>, Extractor<Character>, Double>(
              (Tree<String> tree) -> RegexExtractionProblem.fromRegex(tree.leaves().stream()
                  .map(Tree::content)
                  .collect(Collectors.joining())),
              new GrammarRampedHalfAndHalf<>(6, maxHeight + 4, g),
              PartialComparator.from(Double.class).on(Individual::getFitness),
              nPop,
              Map.of(
                  new SameRootSubtreeCrossover<>(maxHeight + 4), 0.8d,
                  new GrammarBasedSubtreeMutation<>(maxHeight + 4, g), 0.2d
              ),
              new Tournament(nTournament),
              new Worst(),
              nPop,
              true,
              100
          );
        })
    ));
    //filter evolvers
    evolvers = evolvers.entrySet().stream()
        .filter(e -> e.getKey().matches(evolverNamePattern))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    L.info(String.format("Going to test with %d evolvers: %s%n",
        evolvers.size(),
        evolvers.keySet()
    ));
    //run
    for (int seed : seeds) {
      for (Map.Entry<String, RegexExtractionProblem> problemEntry : problems.entrySet()) {
        for (Map.Entry<String, Function<RegexExtractionProblem, Evolver<?, Extractor<Character>, Double>>> evolverEntry : evolvers.entrySet()) {
          Map<String, String> keys = new TreeMap<>(Map.of(
              "seed", Integer.toString(seed),
              "problem", problemEntry.getKey(),
              "evolver", evolverEntry.getKey()
          ));
          try {
            List<DataCollector<?, ? super Extractor<Character>, ? super Double>> collectors = List.of(new Static(keys),
                new Basic(),
                new Population(),
                new Diversity(),
                new BestInfo("%7.5f"),
                new FunctionOfOneBest<>(i -> List.of(new Item(
                    "validation.fitness",
                    problemEntry.getValue().getValidationFunction().apply(i.getSolution()).get(0),
                    "%7.5f"
                ))),
                new BestPrinter(BestPrinter.Part.SOLUTION, "%80.80s")
            );
            Stopwatch stopwatch = Stopwatch.createStarted();
            Evolver<?, Extractor<Character>, Double> evolver = evolverEntry.getValue().apply(problemEntry.getValue());
            L.info(String.format("Starting %s", keys));
            Collection<RealFunction> solutions = evolver.solve(
                Misc.cached(e -> problemEntry.getValue().getFitnessFunction().apply(e).get(0), 10000),
                new Iterations(nIterations),
                new Random(seed),
                executorService,
                Listener.onExecutor((listenerFactory.getBaseFileName() == null) ?
                        listener(collectors.toArray(DataCollector[]::new)) :
                        listenerFactory.build(collectors.toArray(DataCollector[]::new))
                    , executorService));
            L.info(String.format("Done %s: %d solutions in %4.1fs",
                keys,
                solutions.size(),
                (double) stopwatch.elapsed(TimeUnit.MILLISECONDS) / 1000d
            ));
          } catch (InterruptedException | ExecutionException e) {
            L.severe(String.format("Cannot complete %s due to %s",
                keys,
                e
            ));
            e.printStackTrace();
          }
        }
      }
    }
  }
}
