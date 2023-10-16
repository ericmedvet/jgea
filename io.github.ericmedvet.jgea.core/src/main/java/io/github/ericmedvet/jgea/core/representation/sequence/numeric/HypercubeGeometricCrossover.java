
package io.github.ericmedvet.jgea.core.representation.sequence.numeric;

import com.google.common.collect.Range;
import io.github.ericmedvet.jgea.core.representation.sequence.ElementWiseCrossover;
public class HypercubeGeometricCrossover extends ElementWiseCrossover<Double> {

  public HypercubeGeometricCrossover(Range<Double> range) {
    super(
        (v1, v2, random) -> v1 + (v2 - v1) * (random.nextDouble() * (range.upperEndpoint() - range.lowerEndpoint()) + range.lowerEndpoint())
    );
  }

  public HypercubeGeometricCrossover() {
    this(Range.openClosed(0d, 1d));
  }
}
