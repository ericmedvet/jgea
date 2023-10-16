
package io.github.ericmedvet.jgea.core.representation.sequence;
public class UniformCrossover<E> extends ElementWiseCrossover<E> {

  public UniformCrossover() {
    super((e1, e2, random) -> random.nextBoolean()?e1:e2);
  }

}
