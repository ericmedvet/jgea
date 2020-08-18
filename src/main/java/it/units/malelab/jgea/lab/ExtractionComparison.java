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
import com.google.common.collect.Sets;
import com.google.common.graph.ValueGraph;
import it.units.malelab.jgea.Worker;
import it.units.malelab.jgea.core.Individual;
import it.units.malelab.jgea.core.evolver.Evolver;
import it.units.malelab.jgea.core.evolver.SpeciatedEvolver;
import it.units.malelab.jgea.core.evolver.StandardEvolver;
import it.units.malelab.jgea.core.evolver.stopcondition.Iterations;
import it.units.malelab.jgea.core.listener.Listener;
import it.units.malelab.jgea.core.listener.MultiFileListenerFactory;
import it.units.malelab.jgea.core.listener.collector.*;
import it.units.malelab.jgea.core.operator.Crossover;
import it.units.malelab.jgea.core.operator.Mutation;
import it.units.malelab.jgea.core.order.LexicoGraphical;
import it.units.malelab.jgea.core.order.ParetoDominance;
import it.units.malelab.jgea.core.selector.Tournament;
import it.units.malelab.jgea.core.selector.Worst;
import it.units.malelab.jgea.core.util.Misc;
import it.units.malelab.jgea.distance.Jaccard;
import it.units.malelab.jgea.problem.extraction.ExtractionFitness;
import it.units.malelab.jgea.problem.extraction.Extractor;
import it.units.malelab.jgea.problem.extraction.RegexBasedExtractor;
import it.units.malelab.jgea.problem.extraction.string.RegexExtractionProblem;
import it.units.malelab.jgea.problem.extraction.string.RegexGrammar;
import it.units.malelab.jgea.problem.symbolicregression.RealFunction;
import it.units.malelab.jgea.representation.grammar.cfggp.GrammarBasedSubtreeMutation;
import it.units.malelab.jgea.representation.grammar.cfggp.GrammarRampedHalfAndHalf;
import it.units.malelab.jgea.representation.graph.*;
import it.units.malelab.jgea.representation.graph.finiteautomata.DeterministicFiniteAutomaton;
import it.units.malelab.jgea.representation.graph.finiteautomata.ShallowDFAFactory;
import it.units.malelab.jgea.representation.tree.SameRootSubtreeCrossover;
import it.units.malelab.jgea.representation.tree.Tree;

import java.io.FileNotFoundException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static it.units.malelab.jgea.core.util.Args.i;
import static it.units.malelab.jgea.core.util.Args.ri;

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
    int maxHeight = i(a("maxHeight", "13"));
    int nTournament = 5;
    int diversityMaxAttempts = 100;
    int nIterations = i(a("nIterations", "50"));
    String evolverNamePattern = a("evolver", "dfa.*");
    int[] seeds = ri(a("seed", "0:1"));
    double graphEdgeAdditionRate = 3d;
    double graphEdgeMutationRate = 1d;
    double graphEdgeRemovalRate = 0d;
    double graphNodeAdditionRate = 1d;
    double graphCrossoverRate = 1d;
    Set<RegexGrammar.Option> options = Set.of(RegexGrammar.Option.NON_CAPTURING_GROUP, RegexGrammar.Option.ANY, RegexGrammar.Option.OR, RegexGrammar.Option.ENHANCED_CONCATENATION);
    ExtractionFitness.Metric[] metrics = new ExtractionFitness.Metric[]{ExtractionFitness.Metric.SYMBOL_WEIGHTED_ERROR};
    Map<String, RegexExtractionProblem> problems = Map.of(
        "synthetic-2-5", RegexExtractionProblem.varAlphabet(2, 5, 1, metrics),
        "synthetic-3-5", RegexExtractionProblem.varAlphabet(3, 5, 1, metrics),
        "synthetic-4-10", RegexExtractionProblem.varAlphabet(4, 10, 1, metrics)
    );
    MultiFileListenerFactory<Object, RealFunction, List<Double>> listenerFactory = new MultiFileListenerFactory<>(
        a("dir", "."),
        a("file", null)
    );
    Map<String, Function<RegexExtractionProblem, Evolver<?, Extractor<Character>, List<Double>>>> evolvers = new TreeMap<>(Map.ofEntries(
        Map.entry("cfgtree-ga", p -> {
          RegexGrammar g = new RegexGrammar(p.getFitnessFunction(), options);
          return new StandardEvolver<Tree<String>, Extractor<Character>, List<Double>>(
              (Tree<String> tree) -> new RegexBasedExtractor(tree.leaves().stream()
                  .map(Tree::content)
                  .collect(Collectors.joining())),
              new GrammarRampedHalfAndHalf<>(6, maxHeight + 4, g),
              new LexicoGraphical(IntStream.range(0, metrics.length).toArray()).on(Individual::getFitness),
              nPop,
              Map.of(
                  new SameRootSubtreeCrossover<>(maxHeight + 4), 0.8d,
                  new GrammarBasedSubtreeMutation<>(maxHeight + 4, g), 0.2d
              ),
              new Tournament(nTournament),
              new Worst(),
              nPop,
              true
          );
        }),
        Map.entry("dfa-hash+-speciated", p -> {
          Function<ValueGraph<IndexedNode<DeterministicFiniteAutomaton.State>, Set<Character>>, ValueGraph<DeterministicFiniteAutomaton.State, Set<Character>>> graphMapper = GraphUtils.mapper(
              IndexedNode::content,
              sets -> sets.stream().reduce(Sets::union).orElse(Set.of())
          );
          Set<Character> positiveChars = p.getFitnessFunction().getDesiredExtractions().stream()
              .map(r -> p.getFitnessFunction().getSequence().subList(r.lowerEndpoint(), r.upperEndpoint()).stream().collect(Collectors.toSet()))
              .reduce(Sets::union)
              .orElse(Set.of());
          Predicate<ValueGraph<DeterministicFiniteAutomaton.State, Set<Character>>> checker = DeterministicFiniteAutomaton.checker();
          return new SpeciatedEvolver<ValueGraph<IndexedNode<DeterministicFiniteAutomaton.State>, Set<Character>>, Extractor<Character>, List<Double>>(
              graphMapper
                  .andThen(DeterministicFiniteAutomaton.builder()),
              new ShallowDFAFactory<Character>(2, positiveChars)
                  .then(GraphUtils.mapper(IndexedNode.incrementerMapper(DeterministicFiniteAutomaton.State.class), Misc::first)),
              new LexicoGraphical(IntStream.range(0, metrics.length).toArray()).on(Individual::getFitness),
              nPop,
              Map.of(
                  new IndexedNodeAddition<DeterministicFiniteAutomaton.State, DeterministicFiniteAutomaton.State, Set<Character>>(
                      DeterministicFiniteAutomaton.sequentialStateFactory(2, 0.5),
                      s -> s.getIndex(),
                      2,
                      Mutation.copy(),
                      Mutation.copy()
                  ).withChecker(g -> checker.test(graphMapper.apply(g))), graphNodeAdditionRate,
                  new EdgeModification<IndexedNode<DeterministicFiniteAutomaton.State>, Set<Character>>(
                      (cs, r) -> {
                        if (cs.size() == positiveChars.size()) {
                          return Sets.difference(cs, Set.of(Misc.pickRandomly(cs, r)));
                        }
                        if (cs.size() <= 1) {
                          return r.nextBoolean() ? Sets.union(cs, Sets.difference(positiveChars, cs)) : Set.of(Misc.pickRandomly(positiveChars, r));
                        }
                        return r.nextBoolean() ? Sets.union(cs, Sets.difference(positiveChars, cs)) : Sets.difference(cs, Set.of(Misc.pickRandomly(cs, r)));
                      },
                      1d
                  ).withChecker(g -> checker.test(graphMapper.apply(g))), graphEdgeMutationRate,
                  new EdgeAddition<IndexedNode<DeterministicFiniteAutomaton.State>, Set<Character>>(
                      r -> Set.of(Misc.pickRandomly(positiveChars, r)),
                      true
                  ).withChecker(g -> checker.test(graphMapper.apply(g))), graphEdgeAdditionRate,
                  new EdgeRemoval<IndexedNode<DeterministicFiniteAutomaton.State>, Set<Character>>(
                      s -> s.content().getIndex() == 0
                  ).withChecker(g -> checker.test(graphMapper.apply(g))), graphEdgeRemovalRate,
                  new AlignedCrossover<IndexedNode<DeterministicFiniteAutomaton.State>, Set<Character>>(
                      Crossover.randomCopy(),
                      s -> s.content().getIndex() == 0,
                      false
                  ).withChecker(g -> checker.test(graphMapper.apply(g))), graphCrossoverRate
              ),
              5,
              (new Jaccard()).on(i -> i.getGenotype().nodes()),
              0.25,
              individuals -> {
                Individual<ValueGraph<IndexedNode<DeterministicFiniteAutomaton.State>, Set<Character>>, Extractor<Character>, List<Double>> r = Misc.first(individuals);
                return new Individual<>(
                    r.getGenotype(),
                    r.getSolution(),
                    Misc.median(
                        individuals.stream().map(Individual::getFitness).collect(Collectors.toList()),
                        new LexicoGraphical(IntStream.range(0, metrics.length).toArray()).comparator()
                    ),
                    r.getBirthIteration()
                );
              },
              0.75
          );
        }),
        Map.entry("dfa-seq-speciated", p -> {
          Set<Character> positiveChars = p.getFitnessFunction().getDesiredExtractions().stream()
              .map(r -> p.getFitnessFunction().getSequence().subList(r.lowerEndpoint(), r.upperEndpoint()).stream().collect(Collectors.toSet()))
              .reduce(Sets::union)
              .orElse(Set.of());
          Predicate<ValueGraph<DeterministicFiniteAutomaton.State, Set<Character>>> checker = DeterministicFiniteAutomaton.checker();
          return new SpeciatedEvolver<ValueGraph<DeterministicFiniteAutomaton.State, Set<Character>>, Extractor<Character>, List<Double>>(
              DeterministicFiniteAutomaton.builder(),
              new ShallowDFAFactory<Character>(2, positiveChars),
              new LexicoGraphical(IntStream.range(0, metrics.length).toArray()).on(Individual::getFitness),
              nPop,
              Map.of(
                  new NodeAddition<DeterministicFiniteAutomaton.State, Set<Character>>(
                      DeterministicFiniteAutomaton.sequentialStateFactory(2, 0.5),
                      Mutation.copy(),
                      Mutation.copy()
                  ).withChecker(checker), graphNodeAdditionRate,
                  new EdgeModification<DeterministicFiniteAutomaton.State, Set<Character>>(
                      (cs, r) -> {
                        if (cs.size() == positiveChars.size()) {
                          return Sets.difference(cs, Set.of(Misc.pickRandomly(cs, r)));
                        }
                        if (cs.size() <= 1) {
                          return r.nextBoolean() ? Sets.union(cs, Sets.difference(positiveChars, cs)) : Set.of(Misc.pickRandomly(positiveChars, r));
                        }
                        return r.nextBoolean() ? Sets.union(cs, Sets.difference(positiveChars, cs)) : Sets.difference(cs, Set.of(Misc.pickRandomly(cs, r)));
                      },
                      1d
                  ).withChecker(checker), graphEdgeMutationRate,
                  new EdgeAddition<DeterministicFiniteAutomaton.State, Set<Character>>(
                      r -> Set.of(Misc.pickRandomly(positiveChars, r)),
                      true
                  ).withChecker(checker), graphEdgeAdditionRate,
                  new EdgeRemoval<DeterministicFiniteAutomaton.State, Set<Character>>(
                      s -> s.getIndex() == 0
                  ).withChecker(checker), graphEdgeRemovalRate,
                  new AlignedCrossover<DeterministicFiniteAutomaton.State, Set<Character>>(
                      Crossover.randomCopy(),
                      s -> s.getIndex() == 0,
                      false
                  ).withChecker(checker), graphCrossoverRate
              ),
              5,
              (new Jaccard()).on(i -> i.getGenotype().nodes()),
              0.25,
              individuals -> {
                Individual<ValueGraph<DeterministicFiniteAutomaton.State, Set<Character>>, Extractor<Character>, List<Double>> r = Misc.first(individuals);
                return new Individual<>(
                    r.getGenotype(),
                    r.getSolution(),
                    Misc.median(
                        individuals.stream().map(Individual::getFitness).collect(Collectors.toList()),
                        new LexicoGraphical(IntStream.range(0, metrics.length).toArray()).comparator()
                    ),
                    r.getBirthIteration()
                );
              },
              0.75
          );
        }),
        Map.entry("dfa-hash+-ga", p -> {
          Function<ValueGraph<IndexedNode<DeterministicFiniteAutomaton.State>, Set<Character>>, ValueGraph<DeterministicFiniteAutomaton.State, Set<Character>>> graphMapper = GraphUtils.mapper(
              IndexedNode::content,
              sets -> sets.stream().reduce(Sets::union).orElse(Set.of())
          );
          Set<Character> positiveChars = p.getFitnessFunction().getDesiredExtractions().stream()
              .map(r -> p.getFitnessFunction().getSequence().subList(r.lowerEndpoint(), r.upperEndpoint()).stream().collect(Collectors.toSet()))
              .reduce(Sets::union)
              .orElse(Set.of());
          Predicate<ValueGraph<DeterministicFiniteAutomaton.State, Set<Character>>> checker = DeterministicFiniteAutomaton.checker();
          return new StandardEvolver<ValueGraph<IndexedNode<DeterministicFiniteAutomaton.State>, Set<Character>>, Extractor<Character>, List<Double>>(
              graphMapper
                  .andThen(DeterministicFiniteAutomaton.builder()),
              new ShallowDFAFactory<Character>(2, positiveChars)
                  .then(GraphUtils.mapper(IndexedNode.incrementerMapper(DeterministicFiniteAutomaton.State.class), Misc::first)),
              new LexicoGraphical(IntStream.range(0, metrics.length).toArray()).on(Individual::getFitness),
              nPop,
              Map.of(
                  new IndexedNodeAddition<DeterministicFiniteAutomaton.State, DeterministicFiniteAutomaton.State, Set<Character>>(
                      DeterministicFiniteAutomaton.sequentialStateFactory(2, 0.5),
                      s -> s.getIndex(),
                      2,
                      Mutation.copy(),
                      Mutation.copy()
                  ).withChecker(g -> checker.test(graphMapper.apply(g))), graphNodeAdditionRate,
                  new EdgeModification<IndexedNode<DeterministicFiniteAutomaton.State>, Set<Character>>(
                      (cs, r) -> {
                        if (cs.size() == positiveChars.size()) {
                          return Sets.difference(cs, Set.of(Misc.pickRandomly(cs, r)));
                        }
                        if (cs.size() <= 1) {
                          return r.nextBoolean() ? Sets.union(cs, Sets.difference(positiveChars, cs)) : Set.of(Misc.pickRandomly(positiveChars, r));
                        }
                        return r.nextBoolean() ? Sets.union(cs, Sets.difference(positiveChars, cs)) : Sets.difference(cs, Set.of(Misc.pickRandomly(cs, r)));
                      },
                      1d
                  ).withChecker(g -> checker.test(graphMapper.apply(g))), graphEdgeMutationRate,
                  new EdgeAddition<IndexedNode<DeterministicFiniteAutomaton.State>, Set<Character>>(
                      r -> Set.of(Misc.pickRandomly(positiveChars, r)),
                      true
                  ).withChecker(g -> checker.test(graphMapper.apply(g))), graphEdgeAdditionRate,
                  new EdgeRemoval<IndexedNode<DeterministicFiniteAutomaton.State>, Set<Character>>(
                      s -> s.content().getIndex() == 0
                  ).withChecker(g -> checker.test(graphMapper.apply(g))), graphEdgeRemovalRate,
                  new AlignedCrossover<IndexedNode<DeterministicFiniteAutomaton.State>, Set<Character>>(
                      Crossover.randomCopy(),
                      s -> s.content().getIndex() == 0,
                      false
                  ).withChecker(g -> checker.test(graphMapper.apply(g))), graphCrossoverRate
              ),
              new Tournament(nTournament),
              new Worst(),
              nPop,
              true
          );
        }),
        Map.entry("dfa-hash+-speciated-noxover", p -> {
          Function<ValueGraph<IndexedNode<DeterministicFiniteAutomaton.State>, Set<Character>>, ValueGraph<DeterministicFiniteAutomaton.State, Set<Character>>> graphMapper = GraphUtils.mapper(
              IndexedNode::content,
              sets -> sets.stream().reduce(Sets::union).orElse(Set.of())
          );
          Set<Character> positiveChars = p.getFitnessFunction().getDesiredExtractions().stream()
              .map(r -> p.getFitnessFunction().getSequence().subList(r.lowerEndpoint(), r.upperEndpoint()).stream().collect(Collectors.toSet()))
              .reduce(Sets::union)
              .orElse(Set.of());
          Predicate<ValueGraph<DeterministicFiniteAutomaton.State, Set<Character>>> checker = DeterministicFiniteAutomaton.checker();
          return new SpeciatedEvolver<ValueGraph<IndexedNode<DeterministicFiniteAutomaton.State>, Set<Character>>, Extractor<Character>, List<Double>>(
              graphMapper
                  .andThen(DeterministicFiniteAutomaton.builder()),
              new ShallowDFAFactory<Character>(2, positiveChars)
                  .then(GraphUtils.mapper(IndexedNode.incrementerMapper(DeterministicFiniteAutomaton.State.class), Misc::first)),
              new LexicoGraphical(IntStream.range(0, metrics.length).toArray()).on(Individual::getFitness),
              nPop,
              Map.of(
                  new IndexedNodeAddition<DeterministicFiniteAutomaton.State, DeterministicFiniteAutomaton.State, Set<Character>>(
                      DeterministicFiniteAutomaton.sequentialStateFactory(2, 0.5),
                      s -> s.getIndex(),
                      2,
                      Mutation.copy(),
                      Mutation.copy()
                  ).withChecker(g -> checker.test(graphMapper.apply(g))), graphNodeAdditionRate,
                  new EdgeModification<IndexedNode<DeterministicFiniteAutomaton.State>, Set<Character>>(
                      (cs, r) -> {
                        if (cs.size() == positiveChars.size()) {
                          return Sets.difference(cs, Set.of(Misc.pickRandomly(cs, r)));
                        }
                        if (cs.size() <= 1) {
                          return r.nextBoolean() ? Sets.union(cs, Sets.difference(positiveChars, cs)) : Set.of(Misc.pickRandomly(positiveChars, r));
                        }
                        return r.nextBoolean() ? Sets.union(cs, Sets.difference(positiveChars, cs)) : Sets.difference(cs, Set.of(Misc.pickRandomly(cs, r)));
                      },
                      1d
                  ).withChecker(g -> checker.test(graphMapper.apply(g))), graphEdgeMutationRate,
                  new EdgeAddition<IndexedNode<DeterministicFiniteAutomaton.State>, Set<Character>>(
                      r -> Set.of(Misc.pickRandomly(positiveChars, r)),
                      true
                  ).withChecker(g -> checker.test(graphMapper.apply(g))), graphEdgeAdditionRate,
                  new EdgeRemoval<IndexedNode<DeterministicFiniteAutomaton.State>, Set<Character>>(
                      s -> s.content().getIndex() == 0
                  ).withChecker(g -> checker.test(graphMapper.apply(g))), graphEdgeRemovalRate
              ),
              5,
              (new Jaccard()).on(i -> i.getGenotype().nodes()),
              0.25,
              individuals -> {
                Individual<ValueGraph<IndexedNode<DeterministicFiniteAutomaton.State>, Set<Character>>, Extractor<Character>, List<Double>> r = Misc.first(individuals);
                return new Individual<>(
                    r.getGenotype(),
                    r.getSolution(),
                    Misc.median(
                        individuals.stream().map(Individual::getFitness).collect(Collectors.toList()),
                        new LexicoGraphical(IntStream.range(0, metrics.length).toArray()).comparator()
                    ),
                    r.getBirthIteration()
                );
              },
              0.75
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
        for (Map.Entry<String, Function<RegexExtractionProblem, Evolver<?, Extractor<Character>, List<Double>>>> evolverEntry : evolvers.entrySet()) {
          Map<String, String> keys = new TreeMap<>(Map.of(
              "seed", Integer.toString(seed),
              "problem", problemEntry.getKey(),
              "evolver", evolverEntry.getKey()
          ));
          try {
            RegexExtractionProblem p = problemEntry.getValue();
            List<DataCollector<?, ? super Extractor<Character>, ? super Double>> collectors = List.of(new Static(keys),
                new Basic(),
                new Population(),
                new Diversity(),
                new BestInfo("%7.5f"),
                new FunctionOfOneBest<>(i -> {
                  List<Double> f = p.getValidationFunction().apply(i.getSolution());
                  return IntStream.range(0, f.size())
                      .mapToObj(n -> new Item(
                          "validation.fitness.obj." + n,
                          f.get(n),
                          "%7.5f"
                      )).collect(Collectors.toList());
                }),
                new FunctionOfOneBest<>(i ->
                    List.of(new Item(
                        "num.extractions",
                        i.getSolution().extractNonOverlapping(p.getFitnessFunction().getSequence()).size(),
                        "%4d"
                    ))
                ),
                new BestPrinter(BestPrinter.Part.SOLUTION, "%60.60s")
            );
            Stopwatch stopwatch = Stopwatch.createStarted();
            Evolver<?, Extractor<Character>, List<Double>> evolver = evolverEntry.getValue().apply(p);
            L.info(String.format("Starting %s", keys));
            Collection<RealFunction> solutions = evolver.solve(
                Misc.cached(p.getFitnessFunction(), 10000),
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
