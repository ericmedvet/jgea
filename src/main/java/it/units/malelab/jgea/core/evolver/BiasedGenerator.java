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
import it.units.malelab.jgea.grammarbased.Grammar;
import it.units.malelab.jgea.grammarbased.cfggp.RampedHalfAndHalf;
import it.units.malelab.jgea.problem.synthetic.Text;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author eric
 */
public class BiasedGenerator<T, F extends Comparable<F>> implements Evolver<Node<T>, Node<T>, F> {

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
    Map<Node<T>, List<F>> fitnessSamples = new LinkedHashMap<>();
    //main loop
    while (true) {
      //generate individual
      Node<T> tree = null;
      //
      iterations = iterations+1;
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
    System.out.println(t);
    t.prettyPrint(System.out);
    Node<String> n = t
            .getChildren().get(0)
            .getChildren().get(0)
            .getChildren().get(1)
            .getChildren().get(0);
    System.out.println(n.getContent());
    getHKSuperTree(n, 0, 1).prettyPrint(System.out);
  }

}
