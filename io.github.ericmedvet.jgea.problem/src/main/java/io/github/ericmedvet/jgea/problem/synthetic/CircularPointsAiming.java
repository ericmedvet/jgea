package io.github.ericmedvet.jgea.problem.synthetic;

import java.util.List;
import java.util.Random;
import java.util.random.RandomGenerator;
import java.util.stream.IntStream;

import static io.github.ericmedvet.jgea.core.util.VectorUtils.*;

/**
 * @author "Eric Medvet" on 2023/11/02 for jgea
 */
public class CircularPointsAiming extends PointsAiming {
  public CircularPointsAiming(int p, int n, double radius, double center, int seed) {
    super(targets(p, n, radius, center, seed));
  }


  private static List<Double> randomUnitVector(int p, RandomGenerator randomGenerator) {
    List<Double> v = buildList(p, randomGenerator::nextGaussian);
    return mult(v, 1d / norm(v, 2d));
  }

  private static List<List<Double>> targets(int p, int n, double radius, double center, int seed) {
    RandomGenerator random = new Random(seed);
    return IntStream.range(0, n)
        .mapToObj(i -> sum(mult(randomUnitVector(p, random), radius), center)
        )
        .toList();
  }
}
