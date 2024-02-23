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
package io.github.ericmedvet.jgea.problem.control.navigation;

import io.github.ericmedvet.jgea.core.problem.ProblemWithExampleSolution;
import io.github.ericmedvet.jgea.problem.control.ComparableQualityControlProblem;
import io.github.ericmedvet.jsdynsym.core.DoubleRange;
import io.github.ericmedvet.jsdynsym.core.numerical.NumericalDynamicalSystem;
import io.github.ericmedvet.jsdynsym.core.numerical.NumericalStatelessSystem;
import java.util.*;
import java.util.function.Function;
import java.util.random.RandomGenerator;

public class Navigation<SC>
    implements ComparableQualityControlProblem<
            NumericalDynamicalSystem<SC>, double[], double[], Navigation.Snapshot, Double>,
        ProblemWithExampleSolution<NumericalDynamicalSystem<SC>> {

  private final DoubleRange initialRobotXRange;
  private final DoubleRange initialRobotYRange;
  private final DoubleRange initialRobotDirection;
  private final DoubleRange targetXRange;
  private final DoubleRange targetYRange;
  private final double robotRadius;
  private final double robotMaxV;
  private final DoubleRange sensorsAngleRange;
  private final int nOfSensors;
  private final double sensorRange;
  private final boolean senseTarget;
  private final double dT;
  private final double finalT;
  private final Arena arena;
  private final Metric metric;
  private final RandomGenerator randomGenerator;

  public Navigation(
      DoubleRange initialRobotXRange,
      DoubleRange initialRobotYRange,
      DoubleRange initialRobotDirection,
      DoubleRange targetXRange,
      DoubleRange targetYRange,
      double robotRadius,
      double robotMaxV,
      DoubleRange sensorsAngleRange,
      int nOfSensors,
      double sensorRange,
      boolean senseTarget,
      double dT,
      double finalT,
      Arena arena,
      Metric metric,
      RandomGenerator randomGenerator) {
    this.initialRobotXRange = initialRobotXRange;
    this.initialRobotYRange = initialRobotYRange;
    this.initialRobotDirection = initialRobotDirection;
    this.targetXRange = targetXRange;
    this.targetYRange = targetYRange;
    this.robotRadius = robotRadius;
    this.robotMaxV = robotMaxV;
    this.sensorsAngleRange = sensorsAngleRange;
    this.nOfSensors = nOfSensors;
    this.sensorRange = sensorRange;
    this.senseTarget = senseTarget;
    this.dT = dT;
    this.finalT = finalT;
    this.arena = arena;
    this.metric = metric;
    this.randomGenerator = randomGenerator;
  }

  public enum Metric {
    FINAL_DISTANCE,
    MIN_DISTANCE,
    AVG_DISTANCE
  }

  public record Snapshot(
      Arena arena,
      Point targetPosition,
      Point robotPosition,
      double robotDirection,
      double robotRadius,
      int nOfCollisions) {}

  @Override
  public NumericalDynamicalSystem<SC> example() {
    //noinspection unchecked
    return (NumericalDynamicalSystem<SC>)
        NumericalStatelessSystem.from(nOfSensors + (senseTarget ? 2 : 0), 2, (t, in) -> new double[2]);
  }

  @Override
  public SortedMap<Double, Snapshot> simulate(NumericalDynamicalSystem<SC> controller) {
    // check controller consistency
    if (controller.nOfInputs() != nOfSensors + (senseTarget ? 2 : 0)) {
      throw new IllegalArgumentException("Controller has wrong number of inputs: %d found, %d expected"
          .formatted(controller.nOfInputs(), nOfSensors + (senseTarget ? 2 : 0)));
    }
    if (controller.nOfOutputs() != 2) {
      throw new IllegalArgumentException(
          "Controller has wrong number of outputs: %d found, 2 expected".formatted(controller.nOfOutputs()));
    }
    // init
    Point targetP = new Point(
        targetXRange.denormalize(randomGenerator.nextDouble()),
        targetYRange.denormalize(randomGenerator.nextDouble()));
    Point robotP = new Point(
        initialRobotXRange.denormalize(randomGenerator.nextDouble()),
        initialRobotYRange.denormalize(randomGenerator.nextDouble()));
    int nOfCollisions = 0;
    double robotA = initialRobotDirection.denormalize(randomGenerator.nextDouble());
    double t = 0;
    // prepare map and segments
    Map<Double, Snapshot> snapshots = new HashMap<>();
    List<Segment> segments = arena.segments();
    DoubleRange sensorsRange = new DoubleRange(robotRadius, sensorRange);
    // iterate
    while (t < finalT) {
      final Point finalRobotP = robotP;
      // read sensors
      double[] sInputs = sensorsAngleRange
          .delta(robotA)
          .points(nOfSensors - 1)
          .map(a -> {
            Semiline sl = new Semiline(finalRobotP, a);
            double d = segments.stream()
                .map(sl::interception)
                .filter(Optional::isPresent)
                .mapToDouble(op -> op.orElseThrow().distance(finalRobotP))
                .min()
                .orElse(Double.POSITIVE_INFINITY);
            return sensorsRange.normalize(d);
          })
          .toArray();
      double[] inputs = senseTarget ? new double[nOfSensors + 2] : sInputs;
      if (senseTarget) {
        System.arraycopy(sInputs, 0, inputs, 2, sInputs.length);
        inputs[0] = sensorsRange.normalize(robotP.distance(targetP));
        inputs[1] = targetP.diff(robotP).direction() - robotA;
        if (inputs[1] > Math.PI) {
          inputs[1] = inputs[1] - Math.PI;
        }
        if (inputs[1] < -Math.PI) {
          inputs[1] = inputs[1] + Math.PI;
        }
        inputs[1] = inputs[1] / Math.PI;
      }
      // compute output
      double[] output = controller.step(t, inputs);
      double v1 = DoubleRange.SYMMETRIC_UNIT.clip(output[0]) * robotMaxV;
      double v2 = DoubleRange.SYMMETRIC_UNIT.clip(output[1]) * robotMaxV;
      // compute new pose
      Point newRobotP = robotP.sum(new Point(robotA).scale((v1 + v2) / 2d));
      double deltaA = Math.asin((v2 - v1) / 2d / robotRadius);
      // check collision and update pose
      double minD =
          segments.stream().mapToDouble(newRobotP::distance).min().orElseThrow();
      if (minD > robotRadius) {
        robotP = newRobotP;
      } else {
        nOfCollisions = nOfCollisions + 1;
      }
      robotA = robotA + deltaA;
      // add snapshot
      snapshots.put(t, new Snapshot(arena, targetP, robotP, robotA, robotRadius, nOfCollisions));
      t = t + dT;
    }
    // return
    return new TreeMap<>(snapshots);
  }

  @Override
  public Function<SortedMap<Double, Snapshot>, Double> behaviorToQualityFunction() {
    return snapshots -> switch (metric) {
      case FINAL_DISTANCE -> snapshots
          .get(snapshots.lastKey())
          .robotPosition()
          .distance(snapshots.get(snapshots.lastKey()).targetPosition());
      case MIN_DISTANCE -> snapshots.values().stream()
          .mapToDouble(s -> s.robotPosition().distance(s.targetPosition()))
          .min()
          .orElseThrow();
      case AVG_DISTANCE -> snapshots.values().stream()
          .mapToDouble(s -> s.robotPosition().distance(s.targetPosition()))
          .average()
          .orElseThrow();
    };
  }
}
