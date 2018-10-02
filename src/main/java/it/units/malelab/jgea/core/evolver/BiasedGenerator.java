/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core.evolver;

import com.google.common.base.Stopwatch;
import it.units.malelab.jgea.core.Factory;
import it.units.malelab.jgea.core.Node;
import it.units.malelab.jgea.core.Problem;
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
import it.units.malelab.jgea.grammarbased.cfggp.RampedHalfAndHalf;
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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author eric
 */
public class BiasedGenerator<T, F extends Comparable<F>> implements Evolver<Node<T>, Node<T>, F> {

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

  public static class Case<K> {

    private final FlaggedNode<K> tree;
    private final int optionIndex;

    public Case(FlaggedNode<K> tree, int optionIndex) {
      this.tree = tree;
      this.optionIndex = optionIndex;
    }

    public FlaggedNode<K> getTree() {
      return tree;
    }

    public int getOptionIndex() {
      return optionIndex;
    }

    @Override
    public int hashCode() {
      int hash = 3;
      hash = 83 * hash + Objects.hashCode(this.tree);
      hash = 83 * hash + this.optionIndex;
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
      final Case<?> other = (Case<?>) obj;
      if (this.optionIndex != other.optionIndex) {
        return false;
      }
      if (!Objects.equals(this.tree, other.tree)) {
        return false;
      }
      return true;
    }

  }

  private final Grammar<T> grammar;
  private final Function<List<? extends Collection<F>>, Integer> policy;
  private final int h;
  private final int k;
  private final int nToUpdate;
  private final int maxDepth;
  private final List<StopCondition> stopConditions;
  private final long cacheSize;

  public BiasedGenerator(Grammar<T> grammar, Function<List<? extends Collection<F>>, Integer> policy, int h, int k, int nToUpdate, int maxDepth, List<StopCondition> stopConditions, long cacheSize) {
    this.grammar = grammar;
    this.policy = policy;
    this.h = h;
    this.k = k;
    this.nToUpdate = nToUpdate;
    this.maxDepth = maxDepth;
    this.stopConditions = stopConditions;
    this.cacheSize = cacheSize;
  }

  @Override
  public Collection<Node<T>> solve(Problem<Node<T>, F> problem, Random random, ExecutorService executor, Listener listener) throws InterruptedException, ExecutionException {
    //init
    int iterations = 0;
    AtomicInteger births = new AtomicInteger();
    AtomicInteger fitnessEvaluations = new AtomicInteger();
    Stopwatch stopwatch = Stopwatch.createStarted();
    NonDeterministicFunction<Node<T>, F> fitnessFunction = problem.getFitnessFunction();
    if (cacheSize > 0) {
      if (fitnessFunction instanceof Bounded) {
        fitnessFunction = new CachedBoundedNonDeterministicFunction<>(fitnessFunction, cacheSize);
      } else {
        fitnessFunction = fitnessFunction.cached(cacheSize);
      }
    }
    Map<Case, List<F>> fitnessSamples = new LinkedHashMap<>();
    //main loop
    while (true) {
      //generate individual
      Node<T> tree = null;
      //
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

  private Node<T> build(Node<T> root, Map<Node<T>, List<F>> fitnessSamples) {
    List<List<T>> options = grammar.getRules().get(root.getContent());
    return null;
  }

  private StopCondition checkStopConditions(EvolutionEvent event) {
    for (StopCondition stopCondition : stopConditions) {
      if (stopCondition.shouldStop(event)) {
        return stopCondition;
      }
    }
    return null;
  }

  private static <K> Node<K> getHKSuperTree(Node<K> node, int h, int k) {
    if (h == 0) {
      return new Node<>(node.getContent());
    }
    List<Node<K>> ancestors = node.getAncestors();
    ancestors = new ArrayList<>(ancestors.subList(0, Math.min(ancestors.size(), h)));
    List<Integer> indexes = node.ancestorIndexes().subList(0, ancestors.size());
    Node<K> newRoot = ancestors.get(ancestors.size() - 1);
    newRoot = newRoot.prunedSubTree(Math.min(k, ancestors.size()));
    Collections.reverse(ancestors);
    Collections.reverse(indexes);
    ancestors.add(new Node<>(node.getContent()));
    ancestors = ancestors.subList(1, ancestors.size());
    newRoot = addLineage(newRoot, ancestors, indexes);
    return newRoot;
  }

  private static <K> FlaggedNode<K> getHKSuperTree(FlaggedNode<K> node, int h, int k) {
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
    current.getDeflagged().prettyPrint(System.out);
    return null;
  }

  private static <K> Node<K> addLineage(Node<K> node, List<Node<K>> descendants, List<Integer> indexes) {
    if (descendants.isEmpty()) {
      //do nothing
      return node;
    }
    int index = indexes.get(0);
    Node<K> descendant = descendants.get(0);
    Node<K> newNode = new Node<>(descendant.getContent());
    if (!node.getChildren().isEmpty()) {
      //just replace the content of the n-th node
      newNode.getChildren().addAll(node.getChildren().get(index).getChildren());
      node.getChildren().set(index, addLineage(
              newNode,
              descendants.subList(1, descendants.size()),
              indexes.subList(1, indexes.size())
      ));
    } else {
      node.getChildren().add(addLineage(
              newNode,
              descendants.subList(1, descendants.size()),
              indexes.subList(1, indexes.size())
      ));
    }
    return node;
  }

  public static void main(String[] args) throws IOException {
    Grammar<String> g = new Text("Hello World!").getGrammar();
    Factory<Node<String>> factory = new RampedHalfAndHalf<>(3, 12, g);
    List<Node<String>> ts = factory.build(100, new Random(1));
    for (int i = 0; i < ts.size(); i++) {
      //System.out.printf("%d\t%s%n", i, ts.get(i));
    }
    Node<String> t = ts.get(26);
    t.prettyPrint(System.out);
    Node<String> n = t
            .getChildren().get(0)
            .getChildren().get(0)
            .getChildren().get(1)
            .getChildren().get(0);
    System.out.println(n.getContent());
    FlaggedNode<String> at = new FlaggedNode<>(t, n);
    //at.prettyPrint(System.out);
    //System.out.println(at.getFlaggedNodes());
    //at.unflag().prettyPrint(System.out);
    getHKSuperTree(at.getFlaggedNodes().get(0), 2, 1).prettyPrint(System.out);
  }

}
