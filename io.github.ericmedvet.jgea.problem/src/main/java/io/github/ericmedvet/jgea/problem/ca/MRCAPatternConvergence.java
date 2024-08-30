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
import io.github.ericmedvet.jgea.core.problem.ProblemWithExampleSolution;
import io.github.ericmedvet.jgea.core.representation.NamedMultivariateRealFunction;
import io.github.ericmedvet.jgea.core.util.IntRange;
import io.github.ericmedvet.jnb.datastructure.DoubleRange;
import io.github.ericmedvet.jnb.datastructure.Grid;
import io.github.ericmedvet.jsdynsym.core.numerical.MultivariateRealFunction;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class MRCAPatternConvergence
    implements ComparableQualityBasedProblem<MultivariateRealGridCellularAutomaton, Double>,
        ProblemWithExampleSolution<MultivariateRealGridCellularAutomaton> {

  private static final DoubleRange STATE_RANGE = DoubleRange.SYMMETRIC_UNIT;

  private final Grid<double[]> targetGrid;
  private final IntRange convergenceRange;
  private final Distance<double[]> distance;
  private final DoubleRange caStateRange;

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
      DoubleRange caStateRange,
      DoubleRange targetRange) {
    this.targetGrid = targetGrid.map(
        vs -> Arrays.stream(vs).map(targetRange::normalize).toArray());
    this.convergenceRange = convergenceRange;
    this.distance = distance;
    this.caStateRange = caStateRange;
  }

  public MRCAPatternConvergence(
      Grid<double[]> targetGrid,
      IntRange convergenceRange,
      StateDistance stateDistance,
      DoubleRange caStateRange,
      DoubleRange targetRange) {
    this(targetGrid, convergenceRange, stateDistance.get(), caStateRange, targetRange);
  }

  @Override
  public Function<MultivariateRealGridCellularAutomaton, Double> qualityFunction() {
    return ca -> {
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
                .mapToDouble(e -> distance.apply(
                    Arrays.stream(e.value())
                        .map(caStateRange::normalize)
                        .toArray(),
                    targetGrid.get(e.key())))
                .average()
                .orElseThrow();
          })
          .average()
          .orElseThrow();
    };
  }

  @Override
  public MultivariateRealGridCellularAutomaton example() {
    int stateSize = targetGrid.get(0, 0).length;
    return new MultivariateRealGridCellularAutomaton(
        Grid.create(targetGrid.w(), targetGrid.h(), new double[stateSize]),
        DoubleRange.SYMMETRIC_UNIT,
        MultivariateRealGridCellularAutomaton.Kernel.SUM.get(),
        NamedMultivariateRealFunction.from(
            MultivariateRealFunction.from(vs -> new double[stateSize], stateSize, stateSize),
            MultivariateRealFunction.varNames("c", stateSize),
            MultivariateRealFunction.varNames("c", stateSize)),
        1,
        0d,
        true);
  }
}
