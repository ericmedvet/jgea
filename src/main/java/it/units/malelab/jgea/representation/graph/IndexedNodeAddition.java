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

package it.units.malelab.jgea.representation.graph;

import it.units.malelab.jgea.core.IndependentFactory;
import it.units.malelab.jgea.core.operator.Mutation;
import it.units.malelab.jgea.core.util.Misc;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.ToIntFunction;

/**
 * @author eric
 */
public class IndexedNodeAddition<M extends N, N, A> implements Mutation<Graph<IndexedNode<N>, A>> {

  private static class IndexKey {
    protected final int srcIndex;
    protected final int dstIndex;
    protected final int type;
    protected final int nOfSiblings;

    public IndexKey(int srcIndex, int dstIndex, int type, int nOfSiblings) {
      this.srcIndex = srcIndex;
      this.dstIndex = dstIndex;
      this.type = type;
      this.nOfSiblings = nOfSiblings;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      IndexKey indexKey = (IndexKey) o;
      return srcIndex == indexKey.srcIndex &&
          dstIndex == indexKey.dstIndex &&
          type == indexKey.type &&
          nOfSiblings == indexKey.nOfSiblings;
    }

    @Override
    public int hashCode() {
      return Objects.hash(srcIndex, dstIndex, type, nOfSiblings);
    }
  }

  private final IndependentFactory<M> nodeFactory;
  private final ToIntFunction<N> nodeTyper;
  private final Mutation<A> toNewNodeArcMutation;
  private final Mutation<A> fromNewNodeArcMutation;
  private final Mutation<A> existingArcMutation;

  private int counter;
  private final Map<IndexKey, Integer> counterMap;

  public IndexedNodeAddition(IndependentFactory<M> nodeFactory, ToIntFunction<N> nodeTyper, int counterInitialValue, Mutation<A> toNewNodeArcMutation, Mutation<A> fromNewNodeArcMutation, Mutation<A> existingArcMutation) {
    this.nodeFactory = nodeFactory;
    this.nodeTyper = nodeTyper;
    counter = counterInitialValue;
    this.toNewNodeArcMutation = toNewNodeArcMutation;
    this.fromNewNodeArcMutation = fromNewNodeArcMutation;
    this.existingArcMutation = existingArcMutation;
    counterMap = new HashMap<>();
  }

  public IndexedNodeAddition(IndependentFactory<M> nodeFactory, ToIntFunction<N> nodeTyper, int counterInitialValue, Mutation<A> toNewNodeArcMutation, Mutation<A> fromNewNodeArcMutation) {
    this(nodeFactory, nodeTyper, counterInitialValue, toNewNodeArcMutation, fromNewNodeArcMutation, null);
  }

  @Override
  public Graph<IndexedNode<N>, A> mutate(Graph<IndexedNode<N>, A> parent, Random random) {
    Graph<IndexedNode<N>, A> child = LinkedHashGraph.copyOf(parent);
    if (!child.arcs().isEmpty()) {
      M newNode = nodeFactory.build(random);
      Graph.Arc<IndexedNode<N>> arc = Misc.pickRandomly(child.arcs(), random);
      A existingArcValue = child.getArcValue(arc);
      //mutate existing edge
      if (existingArcMutation != null) {
        child.setArcValue(arc, existingArcMutation.mutate(existingArcValue, random));
      } else {
        child.removeArc(arc);
      }
      //add new edges
      A newArcValueTo = toNewNodeArcMutation.mutate(existingArcValue, random);
      A newArcValueFrom = fromNewNodeArcMutation.mutate(existingArcValue, random);
      //get new node type
      int newNodeType = nodeTyper.applyAsInt(newNode);
      //count "siblings"
      int nSiblings = (int) child.nodes().stream()
          .filter(n -> child.predecessors(n).contains(arc.getSource())
              && child.successors(n).contains(arc.getTarget())
              && (newNode.getClass().isAssignableFrom(n.getClass()))
              && nodeTyper.applyAsInt(n.content()) == newNodeType)
          .count();
      //compute index
      IndexKey key = new IndexKey(
          arc.getSource().index(),
          arc.getTarget().index(),
          newNodeType,
          nSiblings
      );
      Integer index = counterMap.get(key);
      if (index == null) {
        index = counter;
        counterMap.put(key, index);
        counter = counter + 1;
      }
      //add node
      IndexedNode<N> indexedNode = new IndexedNode<>(index, newNode);
      child.addNode(indexedNode);
      //connect edges
      child.setArcValue(arc.getSource(), indexedNode, newArcValueTo);
      child.setArcValue(indexedNode, arc.getTarget(), newArcValueFrom);
    }
    return child;
  }

  public static void main(String[] args) {
    Random r = new Random();
    IndependentFactory<Graph<String, Integer>> factory = random -> {
      Graph<String, Integer> g = new LinkedHashGraph<>();
      g.addNode("i");
      g.addNode("o");
      g.setArcValue("i", "o", 1);
      return g;
    };
    AtomicInteger atomicInteger = new AtomicInteger();
    IndependentFactory<String> mFactory = random -> "m" + atomicInteger.getAndIncrement();
    Graph<String, Integer> g = factory.build(r);
    System.out.println(g);
    NodeAddition<String, Integer> mut = new NodeAddition<>(
        mFactory,
        (a, random) -> a,
        (a, random) -> a
    );
    for (int i = 0; i < 10; i++) {
      g = mut.mutate(g, r);
      System.out.println(g);
    }


    Function<Graph<String, Integer>, Graph<IndexedNode<String>, Integer>> mapper = GraphUtils.mapper(
        c -> new IndexedNode<>(c.equals("i") ? 0 : 1, c),
        Collection::size
    );
    Function<Graph<IndexedNode<String>, Integer>, Graph<String, Integer>> inverseMapper = GraphUtils.mapper(
        IndexedNode::content,
        Collection::size
    );

    System.out.println("\nINDEXED\n");
    IndexedNodeAddition<String, String, Integer> iMut = new IndexedNodeAddition<>(
        mFactory,
        c -> 0,
        2,
        (a, random) -> a,
        (a, random) -> a
    );
    Graph<IndexedNode<String>, Integer> iG = mapper.apply(factory.build(r));
    System.out.println(iG);
    for (int i = 0; i < 10; i++) {
      iG = iMut.mutate(iG, r);
      System.out.printf("   %2d %s%n", iG.size(), iG);
      System.out.printf("-> %2d %s%n", inverseMapper.apply(iG).size(), inverseMapper.apply(iG));
    }

    Graph<IndexedNode<String>, Integer> iG0 = mapper.apply(factory.build(r));
    AlignedCrossover<IndexedNode<String>, Integer> aXOver = new AlignedCrossover<>(
        (g1, g2, random) -> g1,
        s -> false,
        true
    );
    for (int i = 0; i < 3; i++) {
      Graph<IndexedNode<String>, Integer> iG1 = iMut.mutate(iG0, r);
      Graph<IndexedNode<String>, Integer> iG2 = iMut.mutate(iG0, r);
      System.out.printf("%2d %s -> %2d %s%n", iG1.size(), iG1, inverseMapper.apply(iG1).size(), inverseMapper.apply(iG1));
      System.out.printf("%2d %s -> %2d %s%n", iG2.size(), iG2, inverseMapper.apply(iG2).size(), inverseMapper.apply(iG2));
      Graph<IndexedNode<String>, Integer> xIG = aXOver.recombine(iG1, iG2, r);
      System.out.printf("aXOver: %2d %s -> %2d %s%n", xIG.size(), xIG, inverseMapper.apply(xIG).size(), inverseMapper.apply(xIG));
    }

  }

}
