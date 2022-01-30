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

import com.google.common.base.Stopwatch;
import com.google.common.collect.Sets;
import it.units.malelab.jgea.Worker;
import it.units.malelab.jgea.core.QualityBasedProblem;
import it.units.malelab.jgea.core.listener.CSVPrinter;
import it.units.malelab.jgea.core.listener.Factory;
import it.units.malelab.jgea.core.listener.NamedFunction;
import it.units.malelab.jgea.core.operator.Crossover;
import it.units.malelab.jgea.core.operator.Mutation;
import it.units.malelab.jgea.core.order.LexicoGraphical;
import it.units.malelab.jgea.core.selector.Last;
import it.units.malelab.jgea.core.selector.Tournament;
import it.units.malelab.jgea.core.solver.*;
import it.units.malelab.jgea.core.solver.speciation.LazySpeciator;
import it.units.malelab.jgea.core.solver.speciation.SpeciatedEvolver;
import it.units.malelab.jgea.core.solver.state.POSetPopulationState;
import it.units.malelab.jgea.core.util.Misc;
import it.units.malelab.jgea.core.util.Pair;
import it.units.malelab.jgea.distance.Jaccard;
import it.units.malelab.jgea.problem.extraction.ExtractionFitness;
import it.units.malelab.jgea.problem.extraction.Extractor;
import it.units.malelab.jgea.problem.extraction.string.RegexBasedExtractor;
import it.units.malelab.jgea.problem.extraction.string.RegexExtractionProblem;
import it.units.malelab.jgea.problem.extraction.string.RegexGrammar;
import it.units.malelab.jgea.representation.grammar.cfggp.GrammarBasedSubtreeMutation;
import it.units.malelab.jgea.representation.grammar.cfggp.GrammarRampedHalfAndHalf;
import it.units.malelab.jgea.representation.graph.*;
import it.units.malelab.jgea.representation.graph.finiteautomata.DeterministicFiniteAutomaton;
import it.units.malelab.jgea.representation.graph.finiteautomata.ShallowDFAFactory;
import it.units.malelab.jgea.representation.tree.SameRootSubtreeCrossover;
import it.units.malelab.jgea.representation.tree.Tree;

import java.io.File;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static it.units.malelab.jgea.core.listener.NamedFunctions.*;
import static it.units.malelab.jgea.core.util.Args.i;
import static it.units.malelab.jgea.core.util.Args.ri;

/**
 * @author eric
 */
public class ExtractionComparison extends Worker {

  public ExtractionComparison(String[] args) {
    super(args);
  }

  public static void main(String[] args) {
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
    double graphArcAdditionRate = 3d;
    double graphArcMutationRate = 1d;
    double graphArcRemovalRate = 0d;
    double graphNodeAdditionRate = 1d;
    double graphCrossoverRate = 1d;
    Set<RegexGrammar.Option> options = Set.of(RegexGrammar.Option.NON_CAPTURING_GROUP,
        RegexGrammar.Option.ANY,
        RegexGrammar.Option.OR,
        RegexGrammar.Option.ENHANCED_CONCATENATION
    );
    ExtractionFitness.Metric[] metrics = new ExtractionFitness.Metric[]{ExtractionFitness.Metric.SYMBOL_WEIGHTED_ERROR};
    Map<String, RegexExtractionProblem> problems = Map.of("synthetic-2-5",
        RegexExtractionProblem.varAlphabet(2, 5, 1, metrics),
        "synthetic-3-5",
        RegexExtractionProblem.varAlphabet(3, 5, 1, metrics),
        "synthetic-4-8",
        RegexExtractionProblem.varAlphabet(4, 8, 1, metrics),
        "synthetic-4-10",
        RegexExtractionProblem.varAlphabet(4, 10, 1, metrics)
    ).entrySet().stream().map(e -> Pair.of(e.getKey(), e.getValue())).collect(Collectors.toMap(Pair::first,
        Pair::second
    ));

    // new LexicoGraphical<>(Double.class, IntStream.range(0, metrics.length).toArray()).comparing(
    //                      Evolver.Individual::fitness),


    //consumers
    Map<String, Object> keys = new HashMap<>();
    List<NamedFunction<? super POSetPopulationState<?, ? extends Extractor<Character>, ? extends List<Double>>, ?>> functions = List.of(
        /*constant("seed", "%2d", keys),
        constant("problem", "%20.20s", keys),
        constant("evolver", "%20.20s", keys),*/
        // TODO restore attributes
        iterations(),
        births(),
        elapsedSeconds(),
        size().of(all()),
        size().of(firsts()),
        size().of(lasts()),
        uniqueness().of(each(genotype())).of(all()),
        uniqueness().of(each(solution())).of(all()),
        uniqueness().of(each(fitness())).of(all()),
        size().of(genotype()).of(best()),
        size().of(solution()).of(best()),
        nth(0).reformat("%5.3f").of(fitness()).of(best()),
        fitnessMappingIteration().of(best()),
        // TODO put validation, num of extractions, hist of fitnesses
        solution().reformat("%30.30s").of(best())
    );
    Factory<POSetPopulationState<?, ? extends Extractor<Character>, ? extends List<Double>>, Map<String, Object>> listenerFactory = Factory.deaf();/*new TabularPrinter<>(
        functions);*/  // TODO add functions
    if (a("file", null) != null) {
      listenerFactory = Factory.all(List.of(listenerFactory,
          new CSVPrinter<>(functions, List.of(), new File(a("file", null)))
          // TODO add functions
      ));
    }
    //evolvers
    Map<String, Function<RegexExtractionProblem, IterativeSolver<? extends POSetPopulationState<?,
        Extractor<Character>, List<Double>>, QualityBasedProblem<Extractor<Character>, List<Double>>,
        Extractor<Character>>>> solvers = new TreeMap<>();
    solvers.put("cfgtree-ga", p -> {
      RegexGrammar g = new RegexGrammar(p.qualityFunction(), options);
      return new StandardEvolver<>(t -> new RegexBasedExtractor(t.leaves()
          .stream()
          .map(Tree::content)
          .collect(Collectors.joining())),
          new GrammarRampedHalfAndHalf<>(6, maxHeight + 4, g),
          nPop,
          StopConditions.nOfIterations(nIterations),
          Map.of(new SameRootSubtreeCrossover<>(maxHeight + 4),
              0.8d,
              new GrammarBasedSubtreeMutation<>(maxHeight + 4, g),
              0.2d
          ),
          new Tournament(nTournament),
          new Last(),
          nPop,
          true,
          false,
          (ep, r) -> new POSetPopulationState<>()
      );
    });
    solvers.put("cfgtree-gadiv", p -> {
      RegexGrammar g = new RegexGrammar(p.qualityFunction(), options);
      return new StandardWithEnforcedDiversityEvolver<>(tree -> new RegexBasedExtractor(tree.leaves()
          .stream()
          .map(Tree::content)
          .collect(Collectors.joining())),
          new GrammarRampedHalfAndHalf<>(6, maxHeight + 4, g),
          nPop,
          StopConditions.nOfIterations(nIterations),
          Map.of(new SameRootSubtreeCrossover<>(maxHeight + 4),
              0.8d,
              new GrammarBasedSubtreeMutation<>(maxHeight + 4, g),
              0.2d
          ),
          new Tournament(nTournament),
          new Last(),
          nPop,
          true,
          false,
          (ep, r) -> new POSetPopulationState<>(),
          diversityMaxAttempts
      );
    });
    solvers.put("dfa-hash+-speciated", p -> {
      Function<Graph<IndexedNode<DeterministicFiniteAutomaton.State>, Set<Character>>,
          Graph<DeterministicFiniteAutomaton.State, Set<Character>>> graphMapper =
          GraphUtils.mapper(IndexedNode::content,
          sets -> sets.stream().reduce(Sets::union).orElse(Set.of())
      );
      Set<Character> positiveChars = p.qualityFunction()
          .getDesiredExtractions()
          .stream()
          .map(r -> (Set<Character>) new HashSet<>(p.qualityFunction()
              .getSequence()
              .subList(r.lowerEndpoint(), r.upperEndpoint())))
          .reduce(Sets::union)
          .orElse(Set.of());
      Predicate<Graph<DeterministicFiniteAutomaton.State, Set<Character>>> checker =
          DeterministicFiniteAutomaton.checker();
      return new SpeciatedEvolver<>(graphMapper.andThen(DeterministicFiniteAutomaton.builder()),
          new ShallowDFAFactory<>(2, positiveChars).then(GraphUtils.mapper(IndexedNode.incrementerMapper(
              DeterministicFiniteAutomaton.State.class), Misc::first)),
          nPop,
          StopConditions.nOfIterations(nIterations),
          Map.of(new IndexedNodeAddition<DeterministicFiniteAutomaton.State, DeterministicFiniteAutomaton.State,
                  Set<Character>>(
                  DeterministicFiniteAutomaton.sequentialStateFactory(2, 0.5),
                  Node::getIndex,
                  2,
                  Mutation.copy(),
                  Mutation.copy()
              ).withChecker(g -> checker.test(graphMapper.apply(g))),
              graphNodeAdditionRate,
              new ArcModification<IndexedNode<DeterministicFiniteAutomaton.State>, Set<Character>>((cs, r) -> {
                if (cs.size() == positiveChars.size()) {
                  return Sets.difference(cs, Set.of(Misc.pickRandomly(cs, r)));
                }
                if (cs.size() <= 1) {
                  return r.nextBoolean() ? Sets.union(cs,
                      Sets.difference(positiveChars, cs)
                  ) : Set.of(Misc.pickRandomly(positiveChars, r));
                }
                return r.nextBoolean() ? Sets.union(cs, Sets.difference(positiveChars, cs)) : Sets.difference(cs,
                    Set.of(Misc.pickRandomly(cs, r))
                );
              }, 1d).withChecker(g -> checker.test(graphMapper.apply(g))),
              graphArcMutationRate,
              new ArcAddition<IndexedNode<DeterministicFiniteAutomaton.State>, Set<Character>>(r -> Set.of(Misc.pickRandomly(
                  positiveChars,
                  r
              )), true).withChecker(g -> checker.test(graphMapper.apply(g))),
              graphArcAdditionRate,
              new ArcRemoval<IndexedNode<DeterministicFiniteAutomaton.State>, Set<Character>>(s -> s.content()
                  .getIndex() == 0).withChecker(g -> checker.test(graphMapper.apply(g))),
              graphArcRemovalRate,
              new AlignedCrossover<IndexedNode<DeterministicFiniteAutomaton.State>, Set<Character>>(Crossover.randomCopy(),
                  s -> s.content().getIndex() == 0,
                  false
              ).withChecker(g -> checker.test(graphMapper.apply(g))),
              graphCrossoverRate
          ),
          false,
          5,
          new LazySpeciator<>((new Jaccard()).on(i -> i.genotype().nodes()), 0.25),
          0.75
      );
    });
    solvers.put("dfa-seq-speciated", p -> {
      Set<Character> positiveChars = p.qualityFunction()
          .getDesiredExtractions()
          .stream()
          .map(r -> (Set<Character>) new HashSet<>(p.qualityFunction()
              .getSequence()
              .subList(r.lowerEndpoint(), r.upperEndpoint())))
          .reduce(Sets::union)
          .orElse(Set.of());
      Predicate<Graph<DeterministicFiniteAutomaton.State, Set<Character>>> checker =
          DeterministicFiniteAutomaton.checker();
      return new SpeciatedEvolver<>(DeterministicFiniteAutomaton.builder(),
          new ShallowDFAFactory<>(2, positiveChars),
          nPop,
          StopConditions.nOfIterations(nIterations),
          Map.of(new NodeAddition<DeterministicFiniteAutomaton.State, Set<Character>>(DeterministicFiniteAutomaton.sequentialStateFactory(
                  2,
                  0.5
              ), Mutation.copy(), Mutation.copy()).withChecker(checker),
              graphNodeAdditionRate,
              new ArcModification<DeterministicFiniteAutomaton.State, Set<Character>>((cs, r) -> {
                if (cs.size() == positiveChars.size()) {
                  return Sets.difference(cs, Set.of(Misc.pickRandomly(cs, r)));
                }
                if (cs.size() <= 1) {
                  return r.nextBoolean() ? Sets.union(cs,
                      Sets.difference(positiveChars, cs)
                  ) : Set.of(Misc.pickRandomly(positiveChars, r));
                }
                return r.nextBoolean() ? Sets.union(cs, Sets.difference(positiveChars, cs)) : Sets.difference(cs,
                    Set.of(Misc.pickRandomly(cs, r))
                );
              }, 1d).withChecker(checker),
              graphArcMutationRate,
              new ArcAddition<DeterministicFiniteAutomaton.State, Set<Character>>(r -> Set.of(Misc.pickRandomly(
                  positiveChars,
                  r
              )), true).withChecker(checker),
              graphArcAdditionRate,
              new ArcRemoval<DeterministicFiniteAutomaton.State, Set<Character>>(s -> s.getIndex() == 0).withChecker(
                  checker),
              graphArcRemovalRate,
              new AlignedCrossover<DeterministicFiniteAutomaton.State, Set<Character>>(Crossover.randomCopy(),
                  s -> s.getIndex() == 0,
                  false
              ).withChecker(checker),
              graphCrossoverRate
          ),
          false,
          5,
          new LazySpeciator<>((new Jaccard()).on(i -> i.genotype().nodes()), 0.25),
          0.75
      );
    });
    solvers.put("dfa-seq-speciated-noxover", p -> {
      Set<Character> positiveChars = p.qualityFunction()
          .getDesiredExtractions()
          .stream()
          .map(r -> (Set<Character>) new HashSet<>(p.qualityFunction()
              .getSequence()
              .subList(r.lowerEndpoint(), r.upperEndpoint())))
          .reduce(Sets::union)
          .orElse(Set.of());
      Predicate<Graph<DeterministicFiniteAutomaton.State, Set<Character>>> checker =
          DeterministicFiniteAutomaton.checker();
      return new SpeciatedEvolver<>(DeterministicFiniteAutomaton.builder(),
          new ShallowDFAFactory<>(2, positiveChars),
          nPop,
          StopConditions.nOfIterations(nIterations),
          Map.of(new NodeAddition<DeterministicFiniteAutomaton.State, Set<Character>>(DeterministicFiniteAutomaton.sequentialStateFactory(
                  2,
                  0.5
              ), Mutation.copy(), Mutation.copy()).withChecker(checker),
              graphNodeAdditionRate,
              new ArcModification<DeterministicFiniteAutomaton.State, Set<Character>>((cs, r) -> {
                if (cs.size() == positiveChars.size()) {
                  return Sets.difference(cs, Set.of(Misc.pickRandomly(cs, r)));
                }
                if (cs.size() <= 1) {
                  return r.nextBoolean() ? Sets.union(cs,
                      Sets.difference(positiveChars, cs)
                  ) : Set.of(Misc.pickRandomly(positiveChars, r));
                }
                return r.nextBoolean() ? Sets.union(cs, Sets.difference(positiveChars, cs)) : Sets.difference(cs,
                    Set.of(Misc.pickRandomly(cs, r))
                );
              }, 1d).withChecker(checker),
              graphArcMutationRate,
              new ArcAddition<DeterministicFiniteAutomaton.State, Set<Character>>(r -> Set.of(Misc.pickRandomly(
                  positiveChars,
                  r
              )), true).withChecker(checker),
              graphArcAdditionRate,
              new ArcRemoval<DeterministicFiniteAutomaton.State, Set<Character>>(s -> s.getIndex() == 0).withChecker(
                  checker),
              graphArcRemovalRate
          ),
          false,
          5,
          new LazySpeciator<>((new Jaccard()).on(i -> i.genotype().nodes()), 0.25),
          0.75
      );
    });
    solvers.put("dfa-hash+-ga", p -> {
      Function<Graph<IndexedNode<DeterministicFiniteAutomaton.State>, Set<Character>>,
          Graph<DeterministicFiniteAutomaton.State, Set<Character>>> graphMapper =
          GraphUtils.mapper(IndexedNode::content,
          sets -> sets.stream().reduce(Sets::union).orElse(Set.of())
      );
      Set<Character> positiveChars = p.qualityFunction()
          .getDesiredExtractions()
          .stream()
          .map(r -> (Set<Character>) new HashSet<>(p.qualityFunction()
              .getSequence()
              .subList(r.lowerEndpoint(), r.upperEndpoint())))
          .reduce(Sets::union)
          .orElse(Set.of());
      Predicate<Graph<DeterministicFiniteAutomaton.State, Set<Character>>> checker =
          DeterministicFiniteAutomaton.checker();
      return new StandardEvolver<>(graphMapper.andThen(DeterministicFiniteAutomaton.builder()),
          new ShallowDFAFactory<>(2, positiveChars).then(GraphUtils.mapper(IndexedNode.incrementerMapper(
              DeterministicFiniteAutomaton.State.class), Misc::first)),
          nPop,
          StopConditions.nOfIterations(nIterations),
          Map.of(new IndexedNodeAddition<DeterministicFiniteAutomaton.State, DeterministicFiniteAutomaton.State,
                  Set<Character>>(
                  DeterministicFiniteAutomaton.sequentialStateFactory(2, 0.5),
                  Node::getIndex,
                  2,
                  Mutation.copy(),
                  Mutation.copy()
              ).withChecker(g -> checker.test(graphMapper.apply(g))),
              graphNodeAdditionRate,
              new ArcModification<IndexedNode<DeterministicFiniteAutomaton.State>, Set<Character>>((cs, r) -> {
                if (cs.size() == positiveChars.size()) {
                  return Sets.difference(cs, Set.of(Misc.pickRandomly(cs, r)));
                }
                if (cs.size() <= 1) {
                  return r.nextBoolean() ? Sets.union(cs,
                      Sets.difference(positiveChars, cs)
                  ) : Set.of(Misc.pickRandomly(positiveChars, r));
                }
                return r.nextBoolean() ? Sets.union(cs, Sets.difference(positiveChars, cs)) : Sets.difference(cs,
                    Set.of(Misc.pickRandomly(cs, r))
                );
              }, 1d).withChecker(g -> checker.test(graphMapper.apply(g))),
              graphArcMutationRate,
              new ArcAddition<IndexedNode<DeterministicFiniteAutomaton.State>, Set<Character>>(r -> Set.of(Misc.pickRandomly(
                  positiveChars,
                  r
              )), true).withChecker(g -> checker.test(graphMapper.apply(g))),
              graphArcAdditionRate,
              new ArcRemoval<IndexedNode<DeterministicFiniteAutomaton.State>, Set<Character>>(s -> s.content()
                  .getIndex() == 0).withChecker(g -> checker.test(graphMapper.apply(g))),
              graphArcRemovalRate,
              new AlignedCrossover<IndexedNode<DeterministicFiniteAutomaton.State>, Set<Character>>(Crossover.randomCopy(),
                  s -> s.content().getIndex() == 0,
                  false
              ).withChecker(g -> checker.test(graphMapper.apply(g))),
              graphCrossoverRate
          ),
          new Tournament(nTournament),
          new Last(),
          nPop,
          true,
          false,
          (ep, r) -> new POSetPopulationState<>()
      );
    });
    solvers.put("dfa-hash+-speciated-noxover", p -> {
      Function<Graph<IndexedNode<DeterministicFiniteAutomaton.State>, Set<Character>>,
          Graph<DeterministicFiniteAutomaton.State, Set<Character>>> graphMapper =
          GraphUtils.mapper(IndexedNode::content,
          sets -> sets.stream().reduce(Sets::union).orElse(Set.of())
      );
      Set<Character> positiveChars = p.qualityFunction()
          .getDesiredExtractions()
          .stream()
          .map(r -> (Set<Character>) new HashSet<>(p.qualityFunction()
              .getSequence()
              .subList(r.lowerEndpoint(), r.upperEndpoint())))
          .reduce(Sets::union)
          .orElse(Set.of());
      Predicate<Graph<DeterministicFiniteAutomaton.State, Set<Character>>> checker =
          DeterministicFiniteAutomaton.checker();
      return new SpeciatedEvolver<>(graphMapper.andThen(DeterministicFiniteAutomaton.builder()),
          new ShallowDFAFactory<>(2, positiveChars).then(GraphUtils.mapper(IndexedNode.incrementerMapper(
              DeterministicFiniteAutomaton.State.class), Misc::first)),
          nPop,
          StopConditions.nOfIterations(nIterations),
          Map.of(new IndexedNodeAddition<DeterministicFiniteAutomaton.State, DeterministicFiniteAutomaton.State,
                  Set<Character>>(
                  DeterministicFiniteAutomaton.sequentialStateFactory(2, 0.5),
                  Node::getIndex,
                  2,
                  Mutation.copy(),
                  Mutation.copy()
              ).withChecker(g -> checker.test(graphMapper.apply(g))),
              graphNodeAdditionRate,
              new ArcModification<IndexedNode<DeterministicFiniteAutomaton.State>, Set<Character>>((cs, r) -> {
                if (cs.size() == positiveChars.size()) {
                  return Sets.difference(cs, Set.of(Misc.pickRandomly(cs, r)));
                }
                if (cs.size() <= 1) {
                  return r.nextBoolean() ? Sets.union(cs,
                      Sets.difference(positiveChars, cs)
                  ) : Set.of(Misc.pickRandomly(positiveChars, r));
                }
                return r.nextBoolean() ? Sets.union(cs, Sets.difference(positiveChars, cs)) : Sets.difference(cs,
                    Set.of(Misc.pickRandomly(cs, r))
                );
              }, 1d).withChecker(g -> checker.test(graphMapper.apply(g))),
              graphArcMutationRate,
              new ArcAddition<IndexedNode<DeterministicFiniteAutomaton.State>, Set<Character>>(r -> Set.of(Misc.pickRandomly(
                  positiveChars,
                  r
              )), true).withChecker(g -> checker.test(graphMapper.apply(g))),
              graphArcAdditionRate,
              new ArcRemoval<IndexedNode<DeterministicFiniteAutomaton.State>, Set<Character>>(s -> s.content()
                  .getIndex() == 0).withChecker(g -> checker.test(graphMapper.apply(g))),
              graphArcRemovalRate
          ),
          false,
          5,
          new LazySpeciator<>((new Jaccard()).on(i -> i.genotype().nodes()), 0.25),
          0.75
      );
    });
    //filter evolvers
    solvers =
        solvers.entrySet().stream().filter(e -> e.getKey().matches(evolverNamePattern)).collect(Collectors.toMap(Map.Entry::getKey,
        Map.Entry::getValue
    ));
    L.info(String.format("Going to test with %d evolvers: %s%n", solvers.size(), solvers.keySet()));
    //run
    for (int seed : seeds) {
      for (Map.Entry<String, RegexExtractionProblem> problemEntry : problems.entrySet()) {
        for (Map.Entry<String, Function<RegexExtractionProblem, IterativeSolver<? extends POSetPopulationState<?,
            Extractor<Character>, List<Double>>, QualityBasedProblem<Extractor<Character>, List<Double>>,
            Extractor<Character>>>> solverEntry : solvers.entrySet()) {
          keys.putAll(Map.of("seed", seed, "problem", problemEntry.getKey(), "evolver", solverEntry.getKey()));
          try {
            RegexExtractionProblem p = problemEntry.getValue();
            Stopwatch stopwatch = Stopwatch.createStarted();
            IterativeSolver<? extends POSetPopulationState<?, Extractor<Character>, List<Double>>,
                QualityBasedProblem<Extractor<Character>, List<Double>>, Extractor<Character>> solver =
                solverEntry.getValue()
                .apply(p);
            L.info(String.format("Starting %s", keys));
            Collection<Extractor<Character>> solutions =
                solver.solve(p.withComparator(new LexicoGraphical<>(Double.class,
                    IntStream.range(0, metrics.length).toArray()
                )),
                new Random(seed),
                executorService,
                listenerFactory.build(keys).deferred(executorService)
            );
            L.info(String.format("Done %s: %d solutions in %4.1fs",
                keys,
                solutions.size(),
                (double) stopwatch.elapsed(TimeUnit.MILLISECONDS) / 1000d
            ));
          } catch (SolverException e) {
            L.severe(String.format("Cannot complete %s due to %s", keys, e));
            e.printStackTrace();
          }
        }
      }
    }
    listenerFactory.shutdown();
  }
}
