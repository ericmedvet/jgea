package io.github.ericmedvet.jgea.core.representation.sequence.numeric;

import com.google.common.collect.Range;
import io.github.ericmedvet.jgea.core.operator.Crossover;

import java.util.List;
import java.util.random.RandomGenerator;
import java.util.stream.IntStream;

public class SegmentGeometricCrossover implements Crossover<List<Double>> {
  private final Range<Double> range;

  public SegmentGeometricCrossover(Range<Double> range) {
    this.range = range;
  }

  public SegmentGeometricCrossover() {
    this(Range.openClosed(0d, 1d));
  }

  @Override
  public List<Double> recombine(List<Double> g1, List<Double> g2, RandomGenerator random) {
    if (g1.size() != g2.size()) {
      throw new IllegalArgumentException("Parent genotype sizes are different: %d vs. %d".formatted(
          g1.size(),
          g2.size()
      ));
    }
    double alpha = random.nextDouble() * (range.upperEndpoint() - range.lowerEndpoint()) + range.lowerEndpoint();
    return IntStream.range(0, g1.size()).mapToObj(i -> g1.get(i) + (g2.get(i) - g1.get(i)) * alpha).toList();
  }
}
