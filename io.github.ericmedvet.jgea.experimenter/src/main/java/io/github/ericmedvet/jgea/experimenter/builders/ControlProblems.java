/*-
 * ========================LICENSE_START=================================
 * jgea-experimenter
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
package io.github.ericmedvet.jgea.experimenter.builders;

import io.github.ericmedvet.jgea.problem.control.navigation.Arena;
import io.github.ericmedvet.jgea.problem.control.navigation.Navigation;
import io.github.ericmedvet.jnb.core.Discoverable;
import io.github.ericmedvet.jnb.core.Param;
import io.github.ericmedvet.jsdynsym.core.DoubleRange;
import java.util.Random;

/**
 * @author "Eric Medvet" on 2024/02/23 for jgea
 */
@Discoverable(prefixTemplate = "ea.problem|p.control|c")
public class ControlProblems {
  private ControlProblems() {}

  @SuppressWarnings("unused")
  public static Navigation<?> navigation(
      @Param(value = "name", iS = "nav-{arena}") String name,
      @Param(value = "initialRobotXRange", dNPM = "ds.range(min=0.45;max=0.55)") DoubleRange initialRobotXRange,
      @Param(value = "initialRobotYRange", dNPM = "ds.range(min=0.8;max=0.85)") DoubleRange initialRobotYRange,
      @Param(value = "initialRobotDirection", dNPM = "ds.range(min=0;max=0)") DoubleRange initialRobotDirection,
      @Param(value = "targetXRange", dNPM = "ds.range(min=0.5;max=0.5)") DoubleRange targetXRange,
      @Param(value = "targetYRange", dNPM = "ds.range(min=0.15;max=0.15)") DoubleRange targetYRange,
      @Param(value = "robotRadius", dD = 0.05) double robotRadius,
      @Param(value = "robotMaxV", dD = 0.01) double robotMaxV,
      @Param(value = "sensorsAngleRange", dNPM = "ds.range(min=-1.57;max=1.57)") DoubleRange sensorsAngleRange,
      @Param(value = "nOfSensors", dI = 5) int nOfSensors,
      @Param(value = "sensorRange", dD = 0.5) double sensorRange,
      @Param(value = "senseTarget", dB = true) boolean senseTarget,
      @Param(value = "dT", dD = 0.1) double dT,
      @Param(value = "finalT", dD = 100) double finalT,
      @Param(value = "arena", dS = "empty") Arena.Prepared arena,
      @Param(value = "metric", dS = "avg_distance") Navigation.Metric metric,
      @Param(value = "seed", dI = 1) int randomSeed) {
    return new Navigation<>(
        initialRobotXRange,
        initialRobotYRange,
        initialRobotDirection,
        targetXRange,
        targetYRange,
        robotRadius,
        robotMaxV,
        sensorsAngleRange,
        nOfSensors,
        sensorRange,
        senseTarget,
        dT,
        finalT,
        arena.arena(),
        metric,
        new Random(randomSeed));
  }
}
