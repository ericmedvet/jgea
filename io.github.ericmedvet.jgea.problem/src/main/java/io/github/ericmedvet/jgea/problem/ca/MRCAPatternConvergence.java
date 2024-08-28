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
package io.github.ericmedvet.jgea.problem.ca;

import io.github.ericmedvet.jgea.core.distance.Distance;
import io.github.ericmedvet.jgea.core.problem.ComparableQualityBasedProblem;
import io.github.ericmedvet.jgea.core.util.IntRange;
import io.github.ericmedvet.jnb.datastructure.DoubleRange;
import io.github.ericmedvet.jnb.datastructure.Grid;
import io.github.ericmedvet.jsdynsym.core.numerical.MultivariateRealFunction;
import java.util.Arrays;
import java.util.List;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.random.RandomGenerator;

public class MRCAPatternConvergence implements ComparableQualityBasedProblem<MultivariateRealFunction, Double> {

  private static final DoubleRange STATE_RANGE = DoubleRange.SYMMETRIC_UNIT;

  private final Grid<double[]> targetGrid;
  private final IntRange convergenceRange;
  private final Distance<double[]> distance;
  private final double noiseSigma;
  private final RandomGenerator randomGenerator;
  private final Grid<double[]> initialStates;
  private final List<Grid<Double>> convolutionKernels;
  private final boolean toroidal;

  public enum Kernel implements Supplier<List<Grid<Double>>> {
    SUM(List.of(Grid.create(3, 3, 1d))),
    SOBEL_EDGES(List.of(
        Grid.create(3, 3, List.of(-1d, 0d, +1d, -2d, 0d, +2d, -1d, 0d, +1d)),
        Grid.create(3, 3, List.of(-1d, -2d, -1d, 0d, 0d, 0d, +1d, +2d, +1d)),
        Grid.create(3, 3, List.of(0d, 0d, 0d, 0d, 1d, 0d, 0d, 0d, 0d))));
    private final List<Grid<Double>> kernels;

    Kernel(List<Grid<Double>> kernels) {
      this.kernels = kernels;
    }

    @Override
    public List<Grid<Double>> get() {
      return kernels;
    }
  }

  public enum StateDistance implements Supplier<Distance<double[]>> {
    L1_1((vs1, vs2) -> Math.abs(vs1[0] - vs2[0])),
    L1_3((vs1, vs2) -> Math.abs(vs1[0] - vs2[0]) + Math.abs(vs1[1] - vs2[1]) + Math.abs(vs1[2] - vs2[2])),
    L2_3((vs1, vs2) -> Math.sqrt((vs1[0] - vs2[0]) * (vs1[0] - vs2[0])
        + (vs1[1] - vs2[1]) * (vs1[1] - vs2[1])
        + (vs1[2] - vs2[2]) * (vs1[2] - vs2[2])));
    private final Distance<double[]> distance;

    StateDistance(Distance<double[]> distance) {
      this.distance = distance;
    }

    @Override
    public Distance<double[]> get() {
      return distance;
    }
  }

  public MRCAPatternConvergence(
      Grid<double[]> targetGrid,
      IntRange convergenceRange,
      Distance<double[]> distance,
      double noiseSigma,
      RandomGenerator randomGenerator,
      Grid<double[]> initialStates,
      List<Grid<Double>> convolutionKernels,
      boolean toroidal) {
    this.targetGrid = targetGrid;
    this.convergenceRange = convergenceRange;
    this.distance = distance;
    this.noiseSigma = noiseSigma;
    this.randomGenerator = randomGenerator;
    this.initialStates = initialStates;
    this.convolutionKernels = convolutionKernels;
    this.toroidal = toroidal;
    if (initialStates.w() != targetGrid.w() || initialStates.h() != targetGrid.h()) {
      throw new IllegalArgumentException(
          "Unexpected different sizes for the grid: initial is %dx%d, target is %dx%d"
              .formatted(
                  initialStates.w(), initialStates.h(),
                  targetGrid.w(), targetGrid.h()));
    }
  }

  public MRCAPatternConvergence(
      Grid<double[]> targetGrid,
      IntRange convergenceRange,
      StateDistance stateDistance,
      double noiseSigma,
      RandomGenerator randomGenerator,
      int nOfChannels,
      Kernel kernel,
      boolean toroidal) {
    this(
        targetGrid,
        convergenceRange,
        stateDistance.get(),
        noiseSigma,
        randomGenerator,
        Grid.create(targetGrid.w(), targetGrid.h(), (x, y) -> {
          double[] vs = new double[nOfChannels];
          Arrays.fill(vs, STATE_RANGE.min());
          if (x != targetGrid.w() / 2 && y != targetGrid.h() / 2) {
            vs[0] = STATE_RANGE.max();
          }
          return vs;
        }),
        kernel.get(),
        toroidal);
  }

  @Override
  public Function<MultivariateRealFunction, Double> qualityFunction() {
    final DoubleUnaryOperator postOp;
    if (noiseSigma > 0) {
      postOp = v -> STATE_RANGE.normalize(v) + randomGenerator.nextGaussian() * noiseSigma;
    } else {
      postOp = STATE_RANGE::normalize;
    }
    return mrf -> {
      // create the CA
      MultivariateRealGridCellularAutomaton ca = new MultivariateRealGridCellularAutomaton(
          initialStates, convolutionKernels, mrf.andThen(postOp), toroidal);
      // evolve the CA
      List<Grid<double[]>> states = ca.evolve(convergenceRange.max());
      // compute avg distance
      return states.subList(convergenceRange.min(), convergenceRange.max()).stream()
          .mapToDouble(g -> {
            if (g.w() != targetGrid.w() || g.h() != targetGrid.h()) {
              throw new IllegalArgumentException(
                  "Unexpected different sizes for the grid: evaluated is %dx%d, target is %dx%d"
                      .formatted(
                          g.w(), g.h(),
                          targetGrid.w(), targetGrid.h()));
            }
            return g.entries().stream()
                .mapToDouble(e -> distance.apply(e.value(), targetGrid.get(e.key())))
                .average()
                .orElseThrow();
          })
          .average()
          .orElseThrow();
    };
  }
}
