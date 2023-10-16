
package io.github.ericmedvet.jgea.core.representation.graph;

import java.io.Serializable;
import java.util.Objects;
public abstract class Node implements Serializable {
  protected final int index;

  public Node(int index) {
    this.index = index;
  }

  public int getIndex() {
    return index;
  }

  @Override
  public int hashCode() {
    return Objects.hash(index);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    Node node = (Node) o;
    return index == node.index;
  }
}
