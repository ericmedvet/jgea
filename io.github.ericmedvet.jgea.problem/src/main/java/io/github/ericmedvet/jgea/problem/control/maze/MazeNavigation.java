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
package io.github.ericmedvet.jgea.problem.control.maze;

import io.github.ericmedvet.jgea.problem.control.ComparableQualityControlProblem;
import io.github.ericmedvet.jsdynsym.core.DoubleRange;
import io.github.ericmedvet.jsdynsym.core.StatelessSystem;
import io.github.ericmedvet.jsdynsym.core.numerical.NumericalDynamicalSystem;
import io.github.ericmedvet.jsdynsym.core.numerical.NumericalStatelessSystem;
import java.util.*;
import java.util.function.Function;
import java.util.random.RandomGenerator;
import java.util.stream.Stream;

public class MazeNavigation<SC>
    implements ComparableQualityControlProblem<
        NumericalDynamicalSystem<SC>, double[], double[], MazeNavigation.Snapshot, Double> {

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
  private final RandomGenerator randomGenerator;

  public record Snapshot(
      Arena arena,
      Point targetPosition,
      Point robotPosition,
      double robotDirection,
      double robotRadius,
      int nOfCollisions) {}

  public MazeNavigation(
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
    this.randomGenerator = randomGenerator;
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
    List<Segment> segments = Stream.concat(
            Stream.of(
                new Segment(new Point(0, 0), new Point(arena.xExtent(), 0)),
                new Segment(new Point(0, 0), new Point(0, arena.yExtent())),
                new Segment(new Point(arena.xExtent(), arena.yExtent()), new Point(arena.xExtent(), 0)),
                new Segment(
                    new Point(arena.xExtent(), arena.yExtent()), new Point(arena.xExtent(), 0))),
            arena.obstacles().stream())
        .toList();
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
            return Math.min(d, sensorRange) / sensorRange;
          })
          .toArray();
      double[] inputs = senseTarget ? new double[nOfSensors + 2] : sInputs;
      if (senseTarget) {
        System.arraycopy(sInputs, 0, inputs, 2, sInputs.length);
        inputs[0] = Math.min(robotP.distance(targetP), sensorRange) / sensorRange;
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
      System.out.printf("%4.1fs: %s (%3.0f) (%d)%n", t, robotP, Math.toDegrees(robotA), nOfCollisions);
      t = t + dT;
    }
    // return
    return new TreeMap<>(snapshots);
  }

  @Override
  public Function<SortedMap<Double, Snapshot>, Double> behaviorToQualityFunction() {
    return snapshots -> {
      Snapshot lastSnapshot = snapshots.get(snapshots.lastKey());
      return lastSnapshot.robotPosition.distance(lastSnapshot.targetPosition);
    };
  }

  public static void main(String[] args) {
    Arena arena = new Arena(1, 1, List.of());
    MazeNavigation<StatelessSystem.State> mn = new MazeNavigation<>(
        new DoubleRange(0.5, 0.5),
        new DoubleRange(0.5, 0.5),
        new DoubleRange(0, 0),
        new DoubleRange(0.5, 0.5),
        new DoubleRange(0.5, 0.5),
        0.05,
        0.01,
        new DoubleRange(-Math.PI / 2d, Math.PI / 2d),
        5,
        0.25,
        true,
        0.1,
        20,
        arena,
        new Random(1));
    NumericalStatelessSystem c = NumericalStatelessSystem.from(7, 2, (t, inputs) -> new double[] {1d, 1d});
    mn.simulate(c);
  }
}
