
package io.github.ericmedvet.jgea.core.representation.graph.numeric;

import io.github.ericmedvet.jgea.core.representation.graph.Node;

import java.util.Objects;
public class Constant extends Node {

  private final double value;

  public Constant(int index, double value) {
    super(index);
    this.value = value;
  }

  public double getValue() {
    return value;
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), value);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    if (!super.equals(o))
      return false;
    Constant constant = (Constant) o;
    return Double.compare(constant.value, value) == 0;
  }

  @Override
  public String toString() {
    return String.format("c%d[%.3f]", getIndex(), value);
  }
}
