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
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

@Discoverable(prefixTemplate = "ea.problem|p")
public class Problems {

  private Problems() {}

  public enum OptimizationType {
    @SuppressWarnings("unused")
    MINIMIZE,
    MAXIMIZE
  }

  private interface SimulationBasedProblemWithExample<S, B, O extends Simulation.Outcome<B>, Q>
      extends SimulationBasedProblem<S, B, O, Q>, ProblemWithExampleSolution<S> {
    static <S, B, O extends Simulation.Outcome<B>, Q> SimulationBasedProblemWithExample<S, B, O, Q> from(
        Function<O, Q> behaviorQualityFunction,
        Simulation<S, B, O> simulation,
        PartialComparator<QualityOutcome<B, O, Q>> qualityComparator,
        S example) {
      return new SimulationBasedProblemWithExample<>() {
        @Override
        public S example() {
          return example;
        }

        @Override
        public Function<O, Q> outcomeQualityFunction() {
          return behaviorQualityFunction;
        }

        @Override
        public Simulation<S, B, O> simulation() {
          return simulation;
        }

        @Override
        public PartialComparator<QualityOutcome<B, O, Q>> qualityComparator() {
          return qualityComparator;
        }
      };
    }
  }

  private interface SimulationBasedTotalOrderProblemWithExample<
          S, B, O extends Simulation.Outcome<B>, Q extends Comparable<Q>>
      extends SimulationBasedTotalOrderProblem<S, B, O, Q>, ProblemWithExampleSolution<S> {
    static <S, B, O extends Simulation.Outcome<B>, Q extends Comparable<Q>>
        SimulationBasedTotalOrderProblemWithExample<S, B, O, Q> from(
            Function<O, Q> behaviorQualityFunction,
            Simulation<S, B, O> simulation,
            S example,
            OptimizationType type) {
      return new SimulationBasedTotalOrderProblemWithExample<>() {
        @Override
        public S example() {
          return example;
        }

        @Override
        public Function<O, Q> outcomeQualityFunction() {
          return behaviorQualityFunction;
        }

        @Override
        public Simulation<S, B, O> simulation() {
          return simulation;
        }

        @Override
        public Comparator<QualityOutcome<B, O, Q>> totalOrderComparator() {
          return switch (type) {
            case MINIMIZE -> Comparator.comparing(QualityOutcome::quality);
            case MAXIMIZE -> ((o1, o2) -> o2.quality().compareTo(o1.quality()));
          };
        }
      };
    }
  }

  @SuppressWarnings("unused")
  public static <B, Q extends Comparable<Q>>
      SimulationBasedTotalOrderProblem<
              NumericalDynamicalSystem<?>,
              SingleAgentTask.Step<double[], double[], B>,
              Simulation.Outcome<SingleAgentTask.Step<double[], double[], B>>,
              Q>
          numEnvTo(
              @Param(value = "name", iS = "{environment.name}") String name,
              @Param(value = "dT", dD = 0.1) double dT,
              @Param(value = "initialT", dD = 0) double initialT,
              @Param(value = "finalT", dD = 100) double finalT,
              @Param("environment") Environment<double[], double[], B> environment,
              @Param(value = "stopCondition", dNPM = "predicate.not(condition = predicate.always())")
                  Predicate<B> stopCondition,
              @Param("f")
                  Function<Simulation.Outcome<SingleAgentTask.Step<double[], double[], B>>, Q>
                      outcomeQualityFunction,
              @Param(value = "type", dS = "minimize") OptimizationType type,
              @Param(value = "", injection = Param.Injection.BUILDER) NamedBuilder<?> nb,
              @Param(value = "", injection = Param.Injection.MAP) ParamMap map) {
    int nOfOutputs = environment.defaultAgentAction().length;
    int nOfInputs = environment.step(0, environment.defaultAgentAction()).length;
    @SuppressWarnings("unchecked")
    Supplier<Environment<double[], double[], B>> envSupplier = () -> (Environment<double[], double[], B>)
        nb.build((NamedParamMap) map.value("environment", ParamMap.Type.NAMED_PARAM_MAP));
    return SimulationBasedTotalOrderProblemWithExample.from(
        outcomeQualityFunction,
        SingleAgentTask.fromEnvironment(envSupplier, stopCondition, new DoubleRange(initialT, finalT), dT),
        NumericalStatelessSystem.from(nOfInputs, nOfOutputs, (t, in) -> new double[nOfOutputs]),
        type);
  }

  @SuppressWarnings("unused")
  public static <S, B, O extends Simulation.Outcome<B>, Q extends Comparable<Q>>
      SimulationBasedTotalOrderProblem<S, B, O, Q> simTo(
          @Param("simulation") Simulation<S, B, O> simulation,
          @Param("f") Function<O, Q> outcomeQualityFunction,
          @Param(value = "type", dS = "minimize") OptimizationType type) {
    Comparator<SimulationBasedProblem.QualityOutcome<B, O, Q>> comparator =
        switch (type) {
          case MINIMIZE -> Comparator.comparing(
              (SimulationBasedProblem.QualityOutcome<B, O, Q> qo) -> qo.quality());
          case MAXIMIZE -> (qo1, qo2) -> qo2.quality().compareTo(qo1.quality());
        };
    return new SimulationBasedTotalOrderProblem<>() {
      @Override
      public Function<O, Q> outcomeQualityFunction() {
        return outcomeQualityFunction;
      }

      @Override
      public Simulation<S, B, O> simulation() {
        return simulation;
      }

      @Override
      public Comparator<QualityOutcome<B, O, Q>> totalOrderComparator() {
        return comparator;
      }
    };
  }

  @SuppressWarnings("unused")
  public static <S> MultiHomogeneousObjectiveProblem<S, Double> toMho(
      @Param(value = "name", iS = "mt2mo({mtProblem.name})") String name,
      @Param("mtProblem") MultiTargetProblem<S> mtProblem) {
    return mtProblem.toMHOProblem();
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
