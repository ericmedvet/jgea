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

import io.github.ericmedvet.jgea.core.order.PartialComparator;
import io.github.ericmedvet.jgea.core.problem.ProblemWithExampleSolution;
import io.github.ericmedvet.jgea.problem.simulation.SimulationBasedProblem;
import io.github.ericmedvet.jgea.problem.simulation.SimulationBasedTotalOrderProblem;
import io.github.ericmedvet.jnb.core.*;
import io.github.ericmedvet.jnb.datastructure.DoubleRange;
import io.github.ericmedvet.jsdynsym.control.Environment;
import io.github.ericmedvet.jsdynsym.control.Simulation;
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

  private interface SimulationBasedProblemWithExample<S, B, Q>
      extends SimulationBasedProblem<S, B, Q>, ProblemWithExampleSolution<S> {
    static <S, B, Q> SimulationBasedProblemWithExample<S, B, Q> from(
        Function<SortedMap<Double, B>, Q> behaviorQualityFunction,
        Simulation<S, B> simulation,
        PartialComparator<SimulationBasedProblem.Outcome<B, Q>> qualityComparator,
        S example) {
      return new SimulationBasedProblemWithExample<>() {
        @Override
        public S example() {
          return example;
        }

        @Override
        public PartialComparator<Outcome<B, Q>> qualityComparator() {
          return qualityComparator;
        }

        @Override
        public Function<SortedMap<Double, B>, Q> behaviorQualityFunction() {
          return behaviorQualityFunction;
        }

        @Override
        public Simulation<S, B> simulation() {
          return simulation;
        }
      };
    }
  }

  private interface SimulationBasedTotalOrderProblemWithExample<S, B, Q extends Comparable<Q>>
      extends SimulationBasedTotalOrderProblem<S, B, Q>, ProblemWithExampleSolution<S> {
    static <S, B, Q extends Comparable<Q>> SimulationBasedTotalOrderProblemWithExample<S, B, Q> from(
        Function<SortedMap<Double, B>, Q> behaviorQualityFunction, Simulation<S, B> simulation, S example) {
      return new SimulationBasedTotalOrderProblemWithExample<S, B, Q>() {
        @Override
        public S example() {
          return example;
        }

        @Override
        public Function<SortedMap<Double, B>, Q> behaviorQualityFunction() {
          return behaviorQualityFunction;
        }

        @Override
        public Simulation<S, B> simulation() {
          return simulation;
        }
      };
    }
  }

  private ControlProblems() {}

  public static <B, Q extends Comparable<Q>>
      SimulationBasedTotalOrderProblem<
              NumericalDynamicalSystem<?>, SingleAgentTask.Step<double[], double[], B>, Q>
          numericalTotalOrder(
              @Param(value = "name", iS = "{environment.name}") String name,
              @Param(value = "dT", dD = 0.1) double dT,
              @Param(value = "initialT", dD = 0) double initialT,
              @Param(value = "finalT", dD = 100) double finalT,
              @Param("environment") Environment<double[], double[], B> environment,
              @Param("f")
                  Function<SortedMap<Double, SingleAgentTask.Step<double[], double[], B>>, Q>
                      behaviorQualityFunction,
              @Param(value = "", injection = Param.Injection.BUILDER) NamedBuilder<?> nb,
              @Param(value = "", injection = Param.Injection.MAP) ParamMap map) {
    int nOfOutputs = environment.defaultAgentAction().length;
    int nOfInputs = environment.step(0, environment.defaultAgentAction()).length;
    //noinspection unchecked
    return SimulationBasedTotalOrderProblemWithExample.from(
        behaviorQualityFunction,
        SingleAgentTask.fromEnvironment(
            () -> (Environment<double[], double[], B>)
                nb.build((NamedParamMap) map.value("environment", ParamMap.Type.NAMED_PARAM_MAP)),
            new DoubleRange(initialT, finalT),
            dT),
        NumericalStatelessSystem.from(nOfInputs, nOfOutputs, (t, in) -> new double[nOfOutputs]));
  }
}
