/*-
 * ========================LICENSE_START=================================
 * jgea-problem
 * %%
 * Copyright (C) 2018 - 2024 Eric Medvet
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
package io.github.ericmedvet.jgea.problem.control;

import io.github.ericmedvet.jsdynsym.core.DoubleRange;
import io.github.ericmedvet.jsdynsym.core.numerical.NumericalDynamicalSystem;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.random.RandomGenerator;

public class CartAndPole
    implements ComparableQualityControlProblem<
        NumericalDynamicalSystem<?>, double[], double[], CartAndPole.Snapshot, Double> {

  private final double dT;
  private final double finalT;
  private final double poleLength;
  private final double poleMass;
  private final double chartMass;
  private final double poleFriction;
  private final DoubleRange initialAngleRange;
  private final RandomGenerator randomGenerator;

  public CartAndPole(
      double dT,
      double finalT,
      double poleLength,
      double poleMass,
      double chartMass,
      double poleFriction,
      DoubleRange initialAngleRange,
      RandomGenerator randomGenerator) {
    this.dT = dT;
    this.finalT = finalT;
    this.poleLength = poleLength;
    this.poleMass = poleMass;
    this.chartMass = chartMass;
    this.poleFriction = poleFriction;
    this.initialAngleRange = initialAngleRange;
    this.randomGenerator = randomGenerator;
  }

  public record Snapshot(double a, double da, double x, double dx) {}

  @Override
  public Function<SortedMap<Double, Snapshot>, Double> behaviorToQualityFunction() {
    return b -> b.values().stream().mapToDouble(s -> s.a * s.a).average().orElseThrow();
  }

  @Override
  public SortedMap<Double, Snapshot> simulate(NumericalDynamicalSystem<?> controller) {
    SortedMap<Double, Snapshot> map = new TreeMap<>();
    // init state
    Snapshot s =
        new Snapshot(randomGenerator.nextDouble(initialAngleRange.min(), initialAngleRange.max()), 0d, 0d, 0d);
    double t = 0d;
    // iterate over time
    while (t < finalT) {
      // State transition functions
      t = t + dT;
    }
    // return
    return map;
  }
}
