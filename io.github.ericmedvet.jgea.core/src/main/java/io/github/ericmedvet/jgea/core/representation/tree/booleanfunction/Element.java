
package io.github.ericmedvet.jgea.core.representation.tree.booleanfunction;

import java.io.Serializable;
public interface Element {

  enum Operator implements Element, Serializable {

    AND(".and"), AND1NOT(".and1not"), OR(".or"), XOR(".xor"), NOT(".not"), IF(".if");

    private final String string;

    Operator(String string) {
      this.string = string;
    }

    @Override
    public String toString() {
      return string;
    }

  }

  record Constant(boolean value) implements Element, Serializable {

    @Override
    public String toString() {
      return Boolean.toString(value);
    }
  }

  record Decoration(String string) implements Element, Serializable {}

  record Variable(String name) implements Element, Serializable {}

  String toString();
}
