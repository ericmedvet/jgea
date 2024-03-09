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
import io.github.ericmedvet.jgea.core.problem.MultiHomogeneousObjectiveProblem;
import io.github.ericmedvet.jgea.core.problem.MultiTargetProblem;
import io.github.ericmedvet.jgea.core.problem.ProblemWithExampleSolution;
import io.github.ericmedvet.jgea.core.problem.TotalOrderQualityBasedProblem;
import io.github.ericmedvet.jgea.problem.simulation.SimulationBasedProblem;
import io.github.ericmedvet.jgea.problem.simulation.SimulationBasedTotalOrderProblem;
import io.github.ericmedvet.jnb.core.*;
import io.github.ericmedvet.jnb.datastructure.DoubleRange;
import io.github.ericmedvet.jsdynsym.control.Environment;
import io.github.ericmedvet.jsdynsym.control.Simulation;
import io.github.ericmedvet.jsdynsym.control.SingleAgentTask;
import io.github.ericmedvet.jsdynsym.core.numerical.NumericalDynamicalSystem;
import io.github.ericmedvet.jsdynsym.core.numerical.NumericalStatelessSystem;
import java.util.Comparator;
import java.util.SortedMap;
import java.util.function.Function;

@Discoverable(prefixTemplate = "ea.problem|p")
public class Problems {

  private Problems() {}

  public enum OptimizationType {
    @SuppressWarnings("unused")
    MINIMIZE,
    MAXIMIZE
  }

  private interface SimulationBasedProblemWithExample<S, B, Q>
      extends SimulationBasedProblem<S, B, Q>, ProblemWithExampleSolution<S> {
    static <S, B, Q> SimulationBasedProblemWithExample<S, B, Q> from(
        Function<SortedMap<Double, B>, Q> behaviorQualityFunction,
        Simulation<S, B> simulation,
        PartialComparator<Outcome<B, Q>> qualityComparator,
        S example) {
      return new SimulationBasedProblemWithExample<>() {
        @Override
        public Function<SortedMap<Double, B>, Q> behaviorQualityFunction() {
          return behaviorQualityFunction;
        }

        @Override
        public Simulation<S, B> simulation() {
          return simulation;
        }

        @Override
        public S example() {
          return example;
        }

        @Override
        public PartialComparator<Outcome<B, Q>> qualityComparator() {
          return qualityComparator;
        }
      };
    }
  }

  private interface SimulationBasedTotalOrderProblemWithExample<S, B, Q extends Comparable<Q>>
      extends SimulationBasedTotalOrderProblem<S, B, Q>, ProblemWithExampleSolution<S> {
    static <S, B, Q extends Comparable<Q>> SimulationBasedTotalOrderProblemWithExample<S, B, Q> from(
        Function<SortedMap<Double, B>, Q> behaviorQualityFunction,
        Simulation<S, B> simulation,
        S example,
        OptimizationType type) {
      return new SimulationBasedTotalOrderProblemWithExample<>() {
        @Override
        public Function<SortedMap<Double, B>, Q> behaviorQualityFunction() {
          return behaviorQualityFunction;
        }

        @Override
        public Simulation<S, B> simulation() {
          return simulation;
        }

        @Override
        public S example() {
          return example;
        }

        @Override
        public Comparator<Outcome<B, Q>> totalOrderComparator() {
          return switch (type) {
            case MINIMIZE -> Comparator.comparing(Outcome::quality);
            case MAXIMIZE -> ((o1, o2) -> o2.quality().compareTo(o1.quality()));
          };
        }
      };
    }
  }

  @SuppressWarnings("unused")
  public static <S> MultiHomogeneousObjectiveProblem<S, Double> toMho(
      @Param(value = "name", iS = "mt2mo({mtProblem.name})") String name,
      @Param("mtProblem") MultiTargetProblem<S> mtProblem) {
    return mtProblem.toMHOProblem();
  }

  @SuppressWarnings("unused")
  public static <B, Q extends Comparable<Q>>
      SimulationBasedTotalOrderProblem<
              NumericalDynamicalSystem<?>, SingleAgentTask.Step<double[], double[], B>, Q>
          numEnvTo(
              @Param(value = "name", iS = "{environment.name}") String name,
              @Param(value = "dT", dD = 0.1) double dT,
              @Param(value = "initialT", dD = 0) double initialT,
              @Param(value = "finalT", dD = 100) double finalT,
              @Param("environment") Environment<double[], double[], B> environment,
              @Param("f")
                  Function<SortedMap<Double, SingleAgentTask.Step<double[], double[], B>>, Q>
                      behaviorQualityFunction,
              @Param(value = "type", dS = "minimize") OptimizationType type,
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
        NumericalStatelessSystem.from(nOfInputs, nOfOutputs, (t, in) -> new double[nOfOutputs]),
        type);
  }

  @SuppressWarnings("unused")
  public static <S, Q, C extends Comparable<C>> TotalOrderQualityBasedProblem<S, Q> totalOrder(
      @Param(value = "name", dS = "to") String name,
      @Param("qFunction") Function<S, Q> qualityFunction,
      @Param(value = "cFunction", dNPM = "f.identity()") Function<Q, C> comparableFunction,
      @Param(value = "type", dS = "minimize") OptimizationType type) {
    return new TotalOrderQualityBasedProblem<>() {
      @Override
      public Function<S, Q> qualityFunction() {
        return qualityFunction;
      }

      @Override
      public Comparator<Q> totalOrderComparator() {
        if (type.equals(OptimizationType.MAXIMIZE)) {
          return Comparator.comparing(comparableFunction).reversed();
        }
        return Comparator.comparing(comparableFunction);
      }
    };
  }
}
