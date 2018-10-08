/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core.evolver.biased;

import com.google.common.base.Stopwatch;
import it.units.malelab.jgea.core.Individual;
import it.units.malelab.jgea.core.Node;
import it.units.malelab.jgea.core.Problem;
import it.units.malelab.jgea.core.evolver.StandardEvolver;
import it.units.malelab.jgea.core.evolver.stopcondition.StopCondition;
import it.units.malelab.jgea.core.function.Bounded;
import it.units.malelab.jgea.core.function.CachedBoundedNonDeterministicFunction;
import it.units.malelab.jgea.core.function.CachedNonDeterministicFunction;
import it.units.malelab.jgea.core.function.Function;
import it.units.malelab.jgea.core.function.FunctionException;
import it.units.malelab.jgea.core.function.NonDeterministicFunction;
import it.units.malelab.jgea.core.listener.Listener;
import it.units.malelab.jgea.core.listener.event.Capturer;
import it.units.malelab.jgea.core.listener.event.EvolutionEndEvent;
import it.units.malelab.jgea.core.listener.event.EvolutionEvent;
import it.units.malelab.jgea.core.listener.event.FunctionEvent;
import it.units.malelab.jgea.core.listener.event.TimedEvent;
import it.units.malelab.jgea.core.ranker.ComparableRanker;
import it.units.malelab.jgea.core.ranker.FitnessComparator;
import it.units.malelab.jgea.core.util.Misc;
import it.units.malelab.jgea.core.util.Pair;
import it.units.malelab.jgea.grammarbased.Grammar;
import it.units.malelab.jgea.grammarbased.GrammarBasedProblem;
import it.units.malelab.jgea.grammarbased.GrammarUtil;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 *
 * @author eric
 */
public class BiasedGenerator<T, S, F extends Comparable<F>> extends StandardEvolver<Node<T>, S, F> {

  private static class FlaggedContent<K> {

    private final K content;
    private boolean flag;

    public FlaggedContent(K content, boolean flag) {
      this.content = content;
      this.flag = flag;
    }

    public K getContent() {
      return content;
    }

    public boolean getFlag() {
      return flag;
    }

    public void setFlag(boolean flag) {
      this.flag = flag;
    }

    @Override
    public int hashCode() {
      int hash = 3;
      hash = 13 * hash + Objects.hashCode(this.content);
      hash = 13 * hash + (this.flag ? 1 : 0);
      return hash;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (getClass() != obj.getClass()) {
        return false;
      }
      final FlaggedContent<?> other = (FlaggedContent<?>) obj;
      if (this.flag != other.flag) {
        return false;
      }
      if (!Objects.equals(this.content, other.content)) {
        return false;
      }
      return true;
    }

    @Override
    public String toString() {
      return content.toString() + (flag ? "*" : "");
    }

  }

  public static class FlaggedNode<K> extends Node<FlaggedContent<K>> {

    public FlaggedNode(K content, boolean flag) {
      super(new FlaggedContent(content, flag));
    }

    public FlaggedNode(Node<K> tree, Node<K> nodeToFlag) {
      this(tree.getContent(), nodeToFlag == tree);
      for (Node<K> child : tree.getChildren()) {
        getChildren().add(new FlaggedNode<>(child, nodeToFlag));
      }
      propagateParentship();
    }

    public void pruneUnflagged() {
      if (!getContent().getFlag() & getParent() == null) {
        throw new RuntimeException("Cannot prune root, which is unflagged.");
      }
      Set<Node<FlaggedContent<K>>> toRemoveChildren = new HashSet<>();
      for (Node<FlaggedContent<K>> child : getChildren()) {
        if (child.getContent().getFlag()) {
          ((FlaggedNode) child).pruneUnflagged();
        } else {
          toRemoveChildren.add(child);
        }
      }
      getChildren().removeAll(toRemoveChildren);
      propagateParentship();
    }

    public Node<K> getDeflagged() {
      Node<K> node = new Node<>(getContent().getContent());
      for (Node<FlaggedContent<K>> child : getChildren()) {
        node.getChildren().add(((FlaggedNode) child).getDeflagged());
      }
      propagateParentship();
      return node;
    }

    public List<FlaggedNode<K>> getFlaggedNodes() {
      List<FlaggedNode<K>> nodes = new ArrayList<>();
      if (getContent().getFlag()) {
        nodes.add(this);
      }
      for (Node<FlaggedContent<K>> child : getChildren()) {
        nodes.addAll(((FlaggedNode) child).getFlaggedNodes());
      }
      return nodes;
    }

    public void flag() {
      getContent().setFlag(true);
    }

    public void unflag() {
      getContent().setFlag(false);
    }

    public void flagSubtree(int n) {
      flag();
      if (n > 0) {
        for (Node<FlaggedContent<K>> child : getChildren()) {
          ((FlaggedNode) child).flagSubtree(n - 1);
        }
      }
    }

  }

  private final NonDeterministicFunction<List<List<F>>, Integer> policy;
  private final int h;
  private final int k;
  private final int maxHeight;

  public BiasedGenerator(NonDeterministicFunction<List<List<F>>, Integer> policy, int h, int k, int populationSize, int offspringSize, int maxHeight, List<StopCondition> stoppingConditions, long cacheSize) {
    super(populationSize, null, new ComparableRanker(new FitnessComparator<>(Function.identity())), null, null, null, null, offspringSize, true, stoppingConditions, cacheSize, false);
    this.policy = policy;
    this.h = h;
    this.k = k;
    this.maxHeight = maxHeight;
  }

  @Override
  public Collection<S> solve(Problem<S, F> problem, Random random, ExecutorService executor, Listener listener) throws InterruptedException, ExecutionException {
    if (!(problem instanceof GrammarBasedProblem)) {
      throw new IllegalArgumentException("Input problem is not a GrammarBasedProblem");
    }
    Function<Node<T>, S> solutionMapper = (Function<Node<T>, S>) ((GrammarBasedProblem) problem).getSolutionMapper();
    Grammar<T> grammar = ((GrammarBasedProblem) problem).getGrammar();
    Map<T, List<Integer>> shortestOptionIndexesMap = GrammarUtil.computeShortestOptionIndexesMap(grammar);
    Set<Node<T>> trees = new LinkedHashSet<>();
    //init
    int iterations = 0;
    AtomicInteger births = new AtomicInteger();
    AtomicInteger fitnessEvaluations = new AtomicInteger();
    Stopwatch stopwatch = Stopwatch.createStarted();
    NonDeterministicFunction<S, F> fitnessFunction = problem.getFitnessFunction();
    if (cacheSize > 0) {
      if (fitnessFunction instanceof Bounded) {
        fitnessFunction = new CachedBoundedNonDeterministicFunction<>(fitnessFunction, cacheSize);
      } else {
        fitnessFunction = fitnessFunction.cached(cacheSize);
      }
    }
    Map<Node<FlaggedContent<T>>, List<List<F>>> fitnessSamples = new LinkedHashMap<>();
    List<Individual<Node<T>, S, F>> population = new ArrayList<>();
    //main loop
    while (true) {
      //generate trees
      List<Callable<Pair<Individual<Node<T>, S, F>, List<Pair<Node<FlaggedContent<T>>, Integer>>>>> tasks = new ArrayList<>();
      for (int i = 0; i < offspringSize; i++) {
        tasks.add(buildingCallable(
                iterations,
                solutionMapper,
                fitnessFunction,
                shortestOptionIndexesMap,
                fitnessSamples,
                grammar,
                random,
                listener));
      }
      fitnessEvaluations.addAndGet(offspringSize);
      births.addAndGet(offspringSize);      
      for (Pair<Individual<Node<T>, S, F>, List<Pair<Node<FlaggedContent<T>>, Integer>>> pair : Misc.getAll(executor.invokeAll(tasks))) {
        if (!trees.contains(pair.first().getGenotype())) {
          trees.add(pair.first().getGenotype());
          //add to population
          population.add(pair.first());
          //update fitnessSamples
          for (Pair<Node<FlaggedContent<T>>, Integer> context : pair.second()) {
            List<List<F>> samples = fitnessSamples.get(context.first());
            if (samples == null) {
              samples = new ArrayList<>();
              fitnessSamples.put(context.first(), samples);
            }
            while (samples.size() <= context.second()) {
              samples.add(new ArrayList<>());
            }
            samples.get(context.second()).add(pair.first().getFitness());
          }
        }
      }
      //rank population and trim
      List<Collection<Individual<Node<T>, S, F>>> rankedPopulation = ranker.rank(population, random);
      int toRemoveCount = population.size() - populationSize;
      while (toRemoveCount > 0) {
        Individual<Node<T>, S, F> toRemove = Misc.pickRandomly(rankedPopulation.get(rankedPopulation.size() - 1), random);
        rankedPopulation.get(rankedPopulation.size() - 1).remove(toRemove);
        if (rankedPopulation.get(rankedPopulation.size() - 1).isEmpty()) {
          rankedPopulation.remove(rankedPopulation.size() - 1);
        }
        population.remove(toRemove);
        toRemoveCount = toRemoveCount - 1;
      }

      if (false) {
        for (Map.Entry<Node<FlaggedContent<T>>, List<List<F>>> entry : fitnessSamples.entrySet()) {
          List<String> averages = entry.getValue().stream()
                  .map(fs -> String.format(
                  "mu=%.6f,n=%d", fs.stream().mapToDouble(f -> ((Number) f).doubleValue()).average().orElse(Double.NaN),
                  fs.size()))
                  .collect(Collectors.toList());
          System.out.printf("%s -> %s%n", entry.getKey(), averages);
        }
      }

      //cast event to listener
      iterations = iterations + 1;
      EvolutionEvent event = new EvolutionEvent(
              iterations,
              births.get(),
              (fitnessFunction instanceof CachedNonDeterministicFunction) ? ((CachedNonDeterministicFunction) fitnessFunction).getActualCount() : fitnessEvaluations.get(),
              (List) rankedPopulation,
              stopwatch.elapsed(TimeUnit.MILLISECONDS)
      );
      listener.listen(event);
      //check stopping conditions
      StopCondition stopCondition = checkStopConditions(event);
      if (stopCondition != null) {
        listener.listen(new EvolutionEndEvent(
                stopCondition,
                iterations,
                births.get(),
                (fitnessFunction instanceof CachedNonDeterministicFunction) ? ((CachedNonDeterministicFunction) fitnessFunction).getActualCount() : fitnessEvaluations.get(),
                rankedPopulation,
                stopwatch.elapsed(TimeUnit.MILLISECONDS))
        );
        break;
      }

    }
    //take out solutions
    List<Collection<Individual<Node<T>, S, F>>> rankedPopulation = ranker.rank(population, random);
    Collection<S> solutions = new ArrayList<>();
    for (Individual<Node<T>, S, F> individual : rankedPopulation.get(0)) {
      solutions.add(individual.getSolution());
    }
    return solutions;
  }

  protected Callable<Pair<Individual<Node<T>, S, F>, List<Pair<Node<FlaggedContent<T>>, Integer>>>> buildingCallable(
          final int birthIteration,
          final NonDeterministicFunction<Node<T>, S> solutionFunction,
          final NonDeterministicFunction<S, F> fitnessFunction,
          final Map<T, List<Integer>> shortestOptionIndexesMap,
          final Map<Node<FlaggedContent<T>>, List<List<F>>> fitnessSamples,
          final Grammar<T> grammar,
          final Random random,
          final Listener listener) {
    return () -> {
      Stopwatch stopwatch = Stopwatch.createUnstarted();
      Capturer capturer = new Capturer();
      long elapsed;
      stopwatch.start();
      //generate tree
      Node<T> tree = new Node<>(grammar.getStartingSymbol());
      List<Pair<Node<FlaggedContent<T>>, Integer>> contexts = new ArrayList<>();
      while (true) {
        boolean done = true;
        for (Node<T> leaf : tree.leafNodes()) {
          List<List<T>> options = grammar.getRules().get(leaf.getContent());
          if (options != null) {
            done = false;
            int optionIndex;
            FlaggedNode<T> flaggedTree = new FlaggedNode<>(leaf.getRoot(), leaf);
            Node<FlaggedContent<T>> context = getHKSuperTree(flaggedTree.getFlaggedNodes().get(0), h, k);
            //check depth
            if (tree.height() > maxHeight) {
              optionIndex = shortestOptionIndexesMap.get(leaf.getContent()).get(0); //TODO mitigate bias here
            } else {
              List<List<F>> fitnessSample = new ArrayList<>(fitnessSamples.getOrDefault(context, new ArrayList<>()));
              while (fitnessSample.size() < options.size()) {
                fitnessSample.add(Collections.EMPTY_LIST);
              }
              optionIndex = policy.apply(fitnessSample, random);
            }
            contexts.add(Pair.build(context, optionIndex));
            //expand
            for (T symbol : options.get(optionIndex)) {
              leaf.getChildren().add(new Node<>(symbol));
            }
          }
        }
        tree.propagateParentship();
        if (done) {
          break;
        }
      }
      elapsed = stopwatch.stop().elapsed(TimeUnit.NANOSECONDS);
      listener.listen(new TimedEvent(elapsed, TimeUnit.NANOSECONDS, new FunctionEvent(grammar.getStartingSymbol(), tree, Collections.EMPTY_MAP)));
      //tree -> solution
      stopwatch.reset().start();
      S solution = null;
      try {
        solution = solutionFunction.apply(tree, random, capturer);
      } catch (FunctionException ex) {
        //invalid solution
        //TODO log to listener
      }
      elapsed = stopwatch.stop().elapsed(TimeUnit.NANOSECONDS);
      Map<String, Object> solutionInfo = Misc.fromInfoEvents(capturer.getEvents(), "solution.");
      listener.listen(new TimedEvent(elapsed, TimeUnit.NANOSECONDS, new FunctionEvent(tree, solution, solutionInfo)));
      capturer.clear();
      //solution -> fitness
      stopwatch.reset().start();
      F fitness = null;
      if (solution != null) {
        fitness = fitnessFunction.apply(solution, random, capturer);
      } else {
        if (fitnessFunction instanceof Bounded) {
          fitness = ((Bounded<F>) fitnessFunction).worstValue();
        }
      }
      elapsed = stopwatch.stop().elapsed(TimeUnit.NANOSECONDS);
      Map<String, Object> fitnessInfo = Misc.fromInfoEvents(capturer.getEvents(), "fitness.");
      listener.listen(new TimedEvent(elapsed, TimeUnit.NANOSECONDS, new FunctionEvent(tree, solution, fitnessInfo)));
      //merge info and return
      return Pair.build(
              new Individual<>(tree, solution, fitness, birthIteration, null, Misc.merge(solutionInfo, fitnessInfo)),
              contexts
      );
    };
  }

  private static <K> Node<FlaggedContent<K>> getHKSuperTree(FlaggedNode<K> node, int h, int k) {
    FlaggedNode<FlaggedContent<K>> flagged = new FlaggedNode<>(node.getRoot(), node);
    FlaggedNode<FlaggedContent<K>> startingNode = flagged.getFlaggedNodes().get(0);
    int i = h;
    FlaggedNode<FlaggedContent<K>> current = startingNode;
    while (i > 0) {
      i = i - 1;
      current.flag();
      if (current.getParent() != null) {
        current = (FlaggedNode<FlaggedContent<K>>) current.getParent();
      } else {
        break;
      }
    }
    current.flagSubtree(Math.min(k, h - i));
    current.pruneUnflagged();
    return current.getDeflagged();
  }

}
