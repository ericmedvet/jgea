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

import io.github.ericmedvet.jgea.problem.control.SingleAgentComparableQualityControlProblem;
import io.github.ericmedvet.jnb.core.*;
import io.github.ericmedvet.jnb.datastructure.DoubleRange;
import io.github.ericmedvet.jsdynsym.control.Environment;
import io.github.ericmedvet.jsdynsym.control.SingleAgentTask;
import io.github.ericmedvet.jsdynsym.core.numerical.NumericalDynamicalSystem;
import io.github.ericmedvet.jsdynsym.core.numerical.NumericalStatelessSystem;

import java.util.SortedMap;
import java.util.function.Function;

/**
 * @author "Eric Medvet" on 2024/02/23 for jgea
 */
@Discoverable(prefixTemplate = "ea.problem|p.control|c")
public class ControlProblems {
  private ControlProblems() {
  }

  @SuppressWarnings("unused")
  public static <S, Q extends Comparable<Q>>
  SingleAgentComparableQualityControlProblem<NumericalDynamicalSystem<?>, double[], double[], S, Q>
  numericalTotalOrder(
      @Param(value = "name", iS = "{environment.name}") String name,
      @Param(value = "dT", dD = 0.1) double dT,
      @Param(value = "initialT", dD = 0) double initialT,
      @Param(value = "finalT", dD = 100) double finalT,
      @Param("environment") Environment<double[], double[], S> environment,
      @Param("f")
      Function<SortedMap<Double, SingleAgentTask.Step<double[], double[], S>>, Q>
          behaviorQualityFunction,
      @Param(value = "", injection = Param.Injection.BUILDER) NamedBuilder<?> nb,
      @Param(value = "", injection = Param.Injection.MAP) ParamMap map
  ) {
    int nOfOutputs = environment.defaultAgentAction().length;
    int nOfInputs = environment.step(0, environment.defaultAgentAction()).length;
    //noinspection unchecked
    return SingleAgentComparableQualityControlProblem.fromEnvironment(
        () -> (Environment<double[], double[], S>) nb.build((NamedParamMap) map.value(
            "environment",
            ParamMap.Type.NAMED_PARAM_MAP
        )),
        NumericalStatelessSystem.from(nOfInputs, nOfOutputs, (t, in) -> new double[nOfOutputs]),
        behaviorQualityFunction,
        new DoubleRange(initialT, finalT),
        dT
    );
  }
}
