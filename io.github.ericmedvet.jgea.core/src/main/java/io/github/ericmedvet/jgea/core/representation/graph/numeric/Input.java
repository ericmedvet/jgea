
package io.github.ericmedvet.jgea.core.representation.graph.numeric;

import io.github.ericmedvet.jgea.core.representation.graph.Node;
public class Input extends Node {

  private final String name;

  public Input(int index, String name) {
    super(index);
    this.name = name;
  }

  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    return name;
  }
}
