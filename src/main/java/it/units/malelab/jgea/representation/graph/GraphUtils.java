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

package it.units.malelab.jgea.representation.graph;

import com.google.common.graph.MutableValueGraph;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Predicate;

/**
 * @author eric
 * @created 2020/07/13
 * @project jgea
 */
public class GraphUtils {

  public static <N> void removeUnconnectedNodes(MutableValueGraph<N, ?> graph, Predicate<N> unremovableNodePredicate) {
    while (true) {
      Set<N> toRemoveNodes = new LinkedHashSet<>();
      for (N node : graph.nodes()) {
        if (!unremovableNodePredicate.test(node) && (graph.inDegree(node) == 0) && (graph.outDegree(node) == 0)) {
          toRemoveNodes.add(node);
        }
      }
      if (!toRemoveNodes.isEmpty()) {
        toRemoveNodes.forEach(n -> graph.removeNode(n));
      } else {
        break;
      }
    }
  }
}
