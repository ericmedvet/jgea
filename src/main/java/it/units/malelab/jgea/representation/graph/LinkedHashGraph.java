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

import java.io.Serializable;
import java.util.*;

/**
 * @author eric
 * @created 2020/08/20
 * @project jgea
 */
public class LinkedHashGraph<N, A> implements Graph<N, A>, Serializable {
  private final Set<N> nodes;
  private final Map<Arc<N>, A> arcs;

  public LinkedHashGraph() {
    nodes = new LinkedHashSet<>();
    arcs = new LinkedHashMap<>();
  }

  public static <N1, A1> Graph<N1, A1> copyOf(Graph<N1, A1> other) {
    Graph<N1, A1> g = new LinkedHashGraph<>();
    other.nodes().forEach(g::addNode);
    other.arcs().forEach(a -> g.setArcValue(a, other.getArcValue(a)));
    return g;
  }

  @Override
  public Set<N> nodes() {
    return nodes;
  }

  @Override
  public Set<Arc<N>> arcs() {
    return arcs.keySet();
  }

  @Override
  public void addNode(N node) {
    nodes.add(node);
  }

  @Override
  public boolean removeNode(N node) {
    boolean removed = nodes.remove(node);
    arcs.keySet().removeIf(a -> a.getSource().equals(node) || a.getTarget().equals(node));
    return removed;
  }

  @Override
  public void setArcValue(Arc<N> arc, A value) {
    if (!nodes.contains(arc.getSource()) || !nodes.contains(arc.getTarget())) {
      throw new IllegalArgumentException(String.format(
          "Cannot set arc value between %s and %s because at least one endpoint node is not present",
          arc.getSource(),
          arc.getTarget()
      ));
    }
    arcs.put(arc, value);
  }

  @Override
  public boolean removeArc(Arc<N> arc) {
    return arcs.remove(arc) != null;
  }

  @Override
  public A getArcValue(Arc<N> arc) {
    return arcs.get(arc);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    LinkedHashGraph<?, ?> that = (LinkedHashGraph<?, ?>) o;
    return nodes.equals(that.nodes) &&
        arcs.equals(that.arcs);
  }

  @Override
  public int hashCode() {
    return Objects.hash(nodes, arcs);
  }

  @Override
  public String toString() {
    return "{" +
        "nodes=" + nodes +
        ", arcs=" + arcs +
        '}';
  }
}
