/*-
 * ========================LICENSE_START=================================
 * jgea-experimenter
 * %%
 * Copyright (C) 2018 - 2025 Eric Medvet
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

import io.github.ericmedvet.jgea.core.problem.*;
import io.github.ericmedvet.jgea.core.problem.MultiObjectiveProblem.Objective;
import io.github.ericmedvet.jgea.core.util.Misc;
import io.github.ericmedvet.jnb.core.Cacheable;
import io.github.ericmedvet.jnb.core.Discoverable;
import io.github.ericmedvet.jnb.core.Param;
import io.github.ericmedvet.jnb.datastructure.DoubleRange;
import io.github.ericmedvet.jnb.datastructure.NamedFunction;
import io.github.ericmedvet.jnb.datastructure.Pair;
import io.github.ericmedvet.jsdynsym.control.*;
import io.github.ericmedvet.jsdynsym.control.Simulation.Outcome;
import io.github.ericmedvet.jsdynsym.core.rl.ReinforcementLearningAgent;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;

@Discoverable(prefixTemplate = "ea.problem|p")
public class Problems {

  private Problems() {
  }

  public enum OptimizationType {
    @SuppressWarnings("unused") MINIMIZE, MAXIMIZE
  }

  @SuppressWarnings("unused")
  @Cacheable
  //bi simulation to homogeneous bi quality based problem
  public static <S, B extends BiSimulation.Outcome<BS>, BS, Q, C extends Comparable<C>> TotalOrderQualityBasedBiProblem<S, B, Q> biSimToBiTo(
      @Param(value = "name", iS = "{simulation.name}") String name,
      @Param("simulation") HomogeneousBiSimulation<S, BS, B> simulation,
      @Param(value = "cFunction", dNPM = "f.identity()") Function<Q, C> comparableFunction,
      @Param(value = "type", dS = "minimize") OptimizationType type,
      @Param(value = "qFunction1") Function<B, Q> qFunction1,
      @Param(value = "qFunction2") Function<B, Q> qFunction2,
      @Param("dT") double dT,
      @Param("tRange") DoubleRange tRange
  ) {
    return new TotalOrderQualityBasedBiProblem<>() {
      @Override
      public Optional<S> example() {
        return simulation.homogeneousExample();
      }

      @Override
      public BiFunction<S, S, B> outcomeFunction() {
        return (s1, s2) -> simulation.simulate(s1, s2, dT, tRange);
      }

      @Override
      public Function<B, Q> firstQualityFunction() {
        return qFunction1;
      }

      @Override
      public Function<B, Q> secondQualityFunction() {
        return qFunction1;
      }

      @Override
      public String toString() {
        return "%s[%s]".formatted(
            name,
            String.join(";", NamedFunction.name(qFunction1), NamedFunction.name(qFunction2))
        );
      }

      @Override
      public Comparator<Q> totalOrderComparator() {
        return type.equals(OptimizationType.MAXIMIZE) ? Comparator.comparing(comparableFunction)
            .reversed() : Comparator.comparing(comparableFunction);
      }
    };
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <S, B extends BiSimulation.Outcome<BS>, BS, Q, C extends Comparable<C>> TotalOrderQualityBasedProblem<S, Q> biSimToTo(
      @Param(value = "name", iS = "{simulation.name}") String name,
      @Param("simulation") HomogeneousBiSimulation<S, BS, B> simulation,
      @Param(value = "cFunction", dNPM = "f.identity()") Function<Q, C> comparableFunction,
      @Param(value = "type", dS = "minimize") OptimizationType type,
      @Param(value = "qFunction") Function<B, Q> qFunction,
      @Param(value = "trainingOpponent") Supplier<S> trainingOpponent,
      @Param("dT") double dT,
      @Param("tRange") DoubleRange tRange
  ) {
    return new TotalOrderQualityBasedProblem<>() {
      @Override
      public Optional<S> example() {
        return simulation.homogeneousExample();
      }

      @Override
      public Function<S, Q> qualityFunction() {
        return (s -> qFunction.apply(simulation.simulate(s, trainingOpponent.get(), dT, tRange)));
      }

      @Override
      public String toString() {
        return "%s[%s]".formatted(name, String.join(";", NamedFunction.name(qFunction)));
      }

      @Override
      public Comparator<Q> totalOrderComparator() {
        return type.equals(OptimizationType.MAXIMIZE) ? Comparator.comparing(comparableFunction)
            .reversed() : Comparator.comparing(comparableFunction);
      }
    };
  }

  private static <B extends Outcome<BS>, BS, O extends Comparable<O>> @NotNull SequencedMap<String, Objective<B, O>> buildObjectives(
      List<Function<B, O>> toMinObjectives,
      List<Function<B, O>> toMaxObjectives
  ) {
    SequencedMap<String, Objective<B, O>> behaviorObjectives = Stream.concat(
        toMinObjectives.stream()
            .map(f -> new Objective<>(f, Comparable::compareTo)),
        toMaxObjectives.stream()
            .map(
                f -> new Objective<>(
                    f,
                    ((Comparator<O>) Comparable::compareTo).reversed()
                )
            )
    )
        .collect(
            Misc.toSequencedMap(
                o -> NamedFunction.name(o.function()),
                o -> o
            )
        );
    if (behaviorObjectives.size() != (toMinObjectives.size() + toMaxObjectives.size())) {
      Logger.getLogger(Problems.class.getName())
          .warning(
              "Objectives have conflicting names: to minimize is %s, to maximize is %s".formatted(
                  toMinObjectives.stream().map(NamedFunction::name).toList(),
                  toMaxObjectives.stream().map(NamedFunction::name).toList()
              )
          );
    }
    return behaviorObjectives;
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <S, Q, C extends Comparable<C>> TotalOrderQualityBasedProblem<S, Q> functionToTo(
      @Param(value = "name", iS = "{qFunction.name}") String name,
      @Param("qFunction") Function<S, Q> qualityFunction,
      @Param(value = "cFunction", dNPM = "f.identity()") Function<Q, C> comparableFunction,
      @Param(value = "type", dS = "minimize") OptimizationType type,
      @Param(value = "example", dNPM = "ea.misc.nullValue()") S example
  ) {
    return TotalOrderQualityBasedProblem.from(
        qualityFunction,
        null,
        type.equals(OptimizationType.MAXIMIZE) ? Comparator.comparing(comparableFunction)
            .reversed() : Comparator.comparing(comparableFunction),
        Optional.ofNullable(example)
    );
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <S, Q, O> TotalOrderQualityBasedProblem<S, Q> moToSo(
      @Param(value = "name", iS = "{moProblem.name}[{objective}]") String name,
      @Param("objective") String objective,
      @Param("moProblem") MultiObjectiveProblem<S, Q, O> moProblem
  ) {
    return moProblem.toTotalOrderQualityBasedProblem(objective);
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <S> SimpleMOProblem<S, Double> mtToMo(
      @Param(value = "name", iS = "mt2mo[{mtProblem.name}]") String name,
      @Param("mtProblem") MultiTargetProblem<S> mtProblem
  ) {
    return mtProblem.toMHOProblem();
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <S, B extends Simulation.Outcome<BS>, BS, O extends Comparable<O>> SimpleMFBBMOProblem<S, B, O> simToDurationSmfbbmo(
      @Param(value = "name", iS = "{simulation.name}[finalT={finalTRange.min}--{finalTRange.min}]") String name,
      @Param("simulation") Simulation<S, BS, B> simulation,
      @Param("dT") double dT,
      @Param("initT") double initT,
      @Param("finalTRange") DoubleRange finalTRange,
      @Param("toMinObjectives") List<Function<B, O>> toMinObjectives,
      @Param("toMaxObjectives") List<Function<B, O>> toMaxObjectives
  ) {
    SequencedMap<String, Objective<B, O>> behaviorObjectives = buildObjectives(
        toMinObjectives,
        toMaxObjectives
    );
    return new SimpleMFBBMOProblem<>() {
      @Override
      public MultifidelityFunction<? super S, ? extends B> behaviorFunction() {
        return (s, fidelity) -> simulation.simulate(s, dT, new DoubleRange(initT, finalTRange.denormalize(fidelity)));
      }

      @Override
      public SequencedMap<String, Objective<B, O>> behaviorObjectives() {
        return behaviorObjectives;
      }

      @Override
      public Optional<S> example() {
        return simulation.example();
      }

      @Override
      public String toString() {
        return "%s[fT=%s;%s]".formatted(name, finalTRange, String.join(";", behaviorObjectives.keySet()));
      }
    };
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <S, B extends Simulation.Outcome<BS>, BS, O extends Comparable<O>> SimpleMFBBMOProblem<S, B, O> simToResolutionSmfbbmo(
      @Param(value = "name", iS = "{simulation.name}[dT={dTRange.min}--{dTRange.min}]") String name,
      @Param("simulation") Simulation<S, BS, B> simulation,
      @Param("dTRange") DoubleRange dTRange,
      @Param("tRange") DoubleRange tRange,
      @Param("toMinObjectives") List<Function<B, O>> toMinObjectives,
      @Param("toMaxObjectives") List<Function<B, O>> toMaxObjectives
  ) {
    SequencedMap<String, Objective<B, O>> behaviorObjectives = buildObjectives(
        toMinObjectives,
        toMaxObjectives
    );
    return new SimpleMFBBMOProblem<>() {
      @Override
      public MultifidelityFunction<? super S, ? extends B> behaviorFunction() {
        return (s, fidelity) -> simulation.simulate(s, dTRange.denormalize(fidelity), tRange);
      }

      @Override
      public SequencedMap<String, Objective<B, O>> behaviorObjectives() {
        return behaviorObjectives;
      }

      @Override
      public Optional<S> example() {
        return simulation.example();
      }

      @Override
      public String toString() {
        return "%s[dT=%s;%s]".formatted(name, dTRange, String.join(";", behaviorObjectives.keySet()));
      }
    };
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <S, B extends Simulation.Outcome<BS>, BS, O extends Comparable<O>> SimpleBBMOProblem<S, B, O> simToSbbmo(
      @Param(value = "name", iS = "{simulation.name}") String name,
      @Param("simulation") Simulation<S, BS, B> simulation,
      @Param("dT") double dT,
      @Param("tRange") DoubleRange tRange,
      @Param("toMinObjectives") List<Function<B, O>> toMinObjectives,
      @Param("toMaxObjectives") List<Function<B, O>> toMaxObjectives
  ) {
    SequencedMap<String, Objective<B, O>> behaviorObjectives = buildObjectives(
        toMinObjectives,
        toMaxObjectives
    );
    return new SimpleBBMOProblem<>() {
      @Override
      public Function<? super S, ? extends B> behaviorFunction() {
        return s -> simulation.simulate(s, dT, tRange);
      }

      @Override
      public SequencedMap<String, Objective<B, O>> behaviorObjectives() {
        return behaviorObjectives;
      }

      @Override
      public Optional<S> example() {
        return simulation.example();
      }

      @Override
      public String toString() {
        return "%s[%s]".formatted(name, String.join(";", behaviorObjectives.keySet()));
      }
    };
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <S, B extends Simulation.Outcome<BS>, BS, O extends Comparable<O>> SimpleMOProblem<S, O> simToSmo(
      @Param(value = "name", iS = "{simulation.name}") String name,
      @Param("simulation") Simulation<S, BS, B> simulation,
      @Param("dT") double dT,
      @Param("tRange") DoubleRange tRange,
      @Param("toMinObjectives") List<Function<B, O>> toMinObjectives,
      @Param("toMaxObjectives") List<Function<B, O>> toMaxObjectives
  ) {
    Function<S, B> simulationFunction = s -> simulation.simulate(s, dT, tRange);
    Function<B, SequencedMap<String, O>> objectivesFunction = b -> Stream.concat(
        toMinObjectives.stream(),
        toMaxObjectives.stream()
    )
        .collect(
            Misc.toSequencedMap(
                NamedFunction::name,
                f -> f.apply(b)
            )
        );
    SequencedMap<String, Comparator<O>> comparators = Stream.concat(
        toMinObjectives.stream().map(f -> new Pair<>(f, ((Comparator<O>) Comparable::compareTo))),
        toMaxObjectives.stream()
            .map(f -> new Pair<>(f, ((Comparator<O>) Comparable::compareTo).reversed()))
    )
        .collect(
            Misc.toSequencedMap(
                p -> NamedFunction.name(p.first()),
                Pair::second
            )
        );
    return new SimpleMOProblem<>() {
      @Override
      public SequencedMap<String, Comparator<O>> comparators() {
        return comparators;
      }

      @Override
      public Optional<S> example() {
        return simulation.example();
      }

      @Override
      public Function<S, SequencedMap<String, O>> qualityFunction() {
        return simulationFunction.andThen(objectivesFunction);
      }

      @Override
      public String toString() {
        return "%s[%s]".formatted(name, String.join(";", comparators.keySet()));
      }
    };
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <S, O> SimpleMOProblem<S, O> smoToSubsettedSmo(
      @Param(value = "name", iS = "{smoProblem.name}") String name,
      @Param("objectives") List<String> objectives,
      @Param("smoProblem") SimpleMOProblem<S, O> smoProblem
  ) {
    return smoProblem.toReducedSimpleMOProblem(new HashSet<>(objectives));
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <C extends ReinforcementLearningAgent<O, A, TS>, TS, O, A> BBTOProblem<C, Simulation.Outcome<SingleAgentTask.Step<ReinforcementLearningAgent.RewardedInput<O>, A, TS>>, Double> srlatToBbto(
      @Param(value = "name", iS = "{task.name}") String name,
      @Param("task") SingleRLAgentTask<C, O, A, TS> task,
      @Param("dT") double dT,
      @Param("tRange") DoubleRange tRange
  ) {
    return new BBTOProblem<>() {
      @Override
      public Function<? super C, ? extends Simulation.Outcome<SingleAgentTask.Step<ReinforcementLearningAgent.RewardedInput<O>, A, TS>>> behaviorFunction() {
        return c -> task.simulate(c, dT, tRange);
      }

      @Override
      public Function<? super Simulation.Outcome<SingleAgentTask.Step<ReinforcementLearningAgent.RewardedInput<O>, A, TS>>, ? extends Double> behaviorQualityFunction() {
        return o -> o.snapshots().values().stream().mapToDouble(s -> s.observation().reward()).sum();
      }

      @Override
      public Optional<C> example() {
        return task.example();
      }

      @Override
      public Comparator<Double> totalOrderBehaviorQualityComparator() {
        return ((Comparator<Double>) Double::compareTo).reversed();
      }
    };
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <C extends ReinforcementLearningAgent<O, A, ?>, O, A> TotalOrderQualityBasedProblem<C, Double> srlatToTo(
      @Param(value = "name", iS = "{task.name}") String name,
      @Param("task") SingleRLAgentTask<C, O, A, ?> task,
      @Param("dT") double dT,
      @Param("tRange") DoubleRange tRange
  ) {
    return new TotalOrderQualityBasedProblem<>() {
      @Override
      public Optional<C> example() {
        return task.example();
      }

      @Override
      public Function<C, Double> qualityFunction() {
        return c -> task.simulate(c, dT, tRange)
            .snapshots()
            .values()
            .stream()
            .mapToDouble(s -> s.observation().reward())
            .sum();
      }

      @Override
      public String toString() {
        return name;
      }

      @Override
      public Comparator<Double> totalOrderComparator() {
        return ((Comparator<Double>) Double::compareTo).reversed();
      }
    };
  }
}
