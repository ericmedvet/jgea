/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core.evolver.biased;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import it.units.malelab.jgea.core.Individual;
import it.units.malelab.jgea.core.Node;
import it.units.malelab.jgea.core.Problem;
import it.units.malelab.jgea.core.evolver.StandardEvolver;
import it.units.malelab.jgea.core.evolver.stopcondition.FitnessEvaluations;
import it.units.malelab.jgea.core.evolver.stopcondition.Iterations;
import it.units.malelab.jgea.core.evolver.stopcondition.StopCondition;
import it.units.malelab.jgea.core.function.Bounded;
import it.units.malelab.jgea.core.function.CachedBoundedNonDeterministicFunction;
import it.units.malelab.jgea.core.function.CachedNonDeterministicFunction;
import it.units.malelab.jgea.core.function.Function;
import it.units.malelab.jgea.core.function.NonDeterministicFunction;
import it.units.malelab.jgea.core.listener.Listener;
import it.units.malelab.jgea.core.listener.event.EvolutionEndEvent;
import it.units.malelab.jgea.core.listener.event.EvolutionEvent;
import it.units.malelab.jgea.core.util.Pair;
import it.units.malelab.jgea.grammarbased.Grammar;
import it.units.malelab.jgea.grammarbased.GrammarBasedProblem;
import it.units.malelab.jgea.grammarbased.GrammarUtil;
import it.units.malelab.jgea.problem.synthetic.Text;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

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

  private final NonDeterministicFunction<List<? extends Collection<F>>, Integer> policy;
  private final int h;
  private final int k;
  private final int maxHeight;

  public BiasedGenerator(NonDeterministicFunction<List<? extends Collection<F>>, Integer> policy, int h, int k, int populationSize, int offspringSize, int maxHeight, List<StopCondition> stoppingConditions, long cacheSize) {
    super(populationSize, null, null, null, null, null, null, offspringSize, true, stoppingConditions, cacheSize, false);
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
    Function<Node<T>, S> solutionMapper = (Function<Node<T>,S>)((GrammarBasedProblem) problem).getSolutionMapper();
    Grammar<T> grammar = ((GrammarBasedProblem) problem).getGrammar();
    Map<T, List<Integer>> shortestOptionIndexesMap = GrammarUtil.computeShortestOptionIndexesMap(grammar);
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
    //main loop
    while (true) {
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
            if (leaf.height() > maxHeight) {
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
      //compute fitness
      S solution = solutionMapper.apply(tree); //TODO use callables+executor
      F fitness = fitnessFunction.apply(solution, random); //TODO use callables+executor
      System.out.printf("tree: h=%d size=%d with %d contexts, fitness=%s%n", tree.height(), tree.size(), contexts.size(), fitness);
      //update fitnessSamples
      for (Pair<Node<FlaggedContent<T>>, Integer> context : contexts) {
        List<List<F>> samples = fitnessSamples.get(context.first());
        if (samples==null) {
          samples = new ArrayList<>();
          fitnessSamples.put(context.first(), samples);
        }
        while (samples.size()<=context.second()) {
          samples.add(new ArrayList<>());
        }
        samples.get(context.second()).add(fitness);
      }
      System.out.println(fitnessSamples);
      //cast event to listener
      iterations = iterations + 1;
      EvolutionEvent event = new EvolutionEvent(
              iterations,
              births.get(),
              (fitnessFunction instanceof CachedNonDeterministicFunction) ? ((CachedNonDeterministicFunction) fitnessFunction).getActualCount() : fitnessEvaluations.get(),
              (List) null,
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
                null,
                stopwatch.elapsed(TimeUnit.MILLISECONDS))
        );
        break;
      }

    }
    //return result
    return null;
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

  public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
    GrammarBasedProblem<String, String, Integer> p = new Text("Hello World!");
    BiasedGenerator<String, String, Integer> bg = new BiasedGenerator<String, String, Integer>(
            new Uniform<>(),
            1, 0, 10, 10, 1,
            Lists.newArrayList(new FitnessEvaluations(100000), new Iterations(10)),
            10000);
    bg.solve(p, new Random(1), Executors.newFixedThreadPool(1), Listener.deaf());
  }

}
