/*-
 * ========================LICENSE_START=================================
 * jgea-problem
 * %%
 * Copyright (C) 2018 - 2023 Eric Medvet
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */
package io.github.ericmedvet.jgea.problem.synthetic;

import static io.github.ericmedvet.jgea.core.util.VectorUtils.*;

import java.util.List;
import java.util.Random;
import java.util.random.RandomGenerator;
import java.util.stream.IntStream;

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
        .mapToObj(i -> sum(mult(randomUnitVector(p, random), radius), center))
        .toList();
  }
}
