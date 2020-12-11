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

import it.units.malelab.jgea.core.operator.Mutation;
import it.units.malelab.jgea.core.util.Misc;

import java.util.Random;
import java.util.function.Predicate;

/**
 * @author eric
 */
public class ArcRemoval<N, A> implements Mutation<Graph<N, A>> {
  private final Predicate<N> unremovableNodePredicate;

  public ArcRemoval(Predicate<N> unremovableNodePredicate) {
    this.unremovableNodePredicate = unremovableNodePredicate;
  }

  @Override
  public Graph<N, A> mutate(Graph<N, A> parent, Random random) {
    Graph<N, A> child = LinkedHashGraph.copyOf(parent);
    if (!child.arcs().isEmpty()) {
      Graph.Arc<N> arc = Misc.pickRandomly(child.arcs(), random);
      child.removeArc(arc);
    }
    GraphUtils.removeUnconnectedNodes(child, unremovableNodePredicate);
    return child;
  }
}
