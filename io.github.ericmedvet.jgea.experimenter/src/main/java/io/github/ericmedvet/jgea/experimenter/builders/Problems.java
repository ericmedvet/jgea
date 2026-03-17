/*-
 * ========================LICENSE_START=================================
 * jgea-experimenter
 * %%
 * Copyright (C) 2018 - 2026 Eric Medvet
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

import io.github.ericmedvet.jgea.core.InvertibleMapper;
import io.github.ericmedvet.jgea.core.problem.BBTOProblem;
import io.github.ericmedvet.jgea.core.problem.CBTOProblem;
import io.github.ericmedvet.jgea.core.problem.MultiObjectiveProblem;
import io.github.ericmedvet.jgea.core.problem.MultiObjectiveProblem.Objective;
import io.github.ericmedvet.jgea.core.problem.MultiTargetProblem;
import io.github.ericmedvet.jgea.core.problem.MultifidelityQualityBasedProblem.MultifidelityFunction;
import io.github.ericmedvet.jgea.core.problem.Problem;
import io.github.ericmedvet.jgea.core.problem.QualityBasedProblem;
import io.github.ericmedvet.jgea.core.problem.SimpleBBMOProblem;
import io.github.ericmedvet.jgea.core.problem.SimpleCBMOProblem;
import io.github.ericmedvet.jgea.core.problem.SimpleMFBBMOProblem;
import io.github.ericmedvet.jgea.core.problem.SimpleMOProblem;
import io.github.ericmedvet.jgea.core.problem.TotalOrderQualityBasedBiProblem;
import io.github.ericmedvet.jgea.core.problem.TotalOrderQualityBasedProblem;
import io.github.ericmedvet.jgea.core.util.IndexedProvider;
import io.github.ericmedvet.jnb.core.Cacheable;
import io.github.ericmedvet.jnb.core.Discoverable;
import io.github.ericmedvet.jnb.core.Param;
import io.github.ericmedvet.jnb.datastructure.DoubleRange;
import io.github.ericmedvet.jnb.datastructure.NamedFunction;
import io.github.ericmedvet.jnb.datastructure.Pair;
import io.github.ericmedvet.jnb.datastructure.Utils;
import io.github.ericmedvet.jsdynsym.control.BiSimulation;
import io.github.ericmedvet.jsdynsym.control.HomogeneousBiSimulation;
import io.github.ericmedvet.jsdynsym.control.Simulation;
import io.github.ericmedvet.jsdynsym.control.SingleAgentTask;
import io.github.ericmedvet.jsdynsym.control.SingleRLAgentTask;
import io.github.ericmedvet.jsdynsym.core.rl.ReinforcementLearningAgent;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.SequencedMap;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.Stream;

@Discoverable(prefixTemplate = "ea.problem|p")
public class Problems {

  private Problems() {
  }

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
    return TotalOrderQualityBasedBiProblem.of(
        (s1, s2) -> simulation.simulate(s1, s2, dT, tRange),
        qFunction1,
        qFunction2,
        type.equals(OptimizationType.MAXIMIZE) ? Comparator.comparing(comparableFunction)
            .reversed() : Comparator.comparing(comparableFunction),
        simulation.homogeneousExample().orElse(null),
        "%s[%s]".formatted(
            name,
            String.join(";", NamedFunction.name(qFunction1), NamedFunction.name(qFunction2))
        )
    );
  }

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
    return TotalOrderQualityBasedProblem.of(
        s -> qFunction.apply(simulation.simulate(s, trainingOpponent.get(), dT, tRange)),
        s -> qFunction.apply(simulation.simulate(s, trainingOpponent.get(), dT, tRange)),
        type.equals(OptimizationType.MAXIMIZE) ? Comparator.comparing(comparableFunction)
            .reversed() : Comparator.comparing(comparableFunction),
        simulation.homogeneousExample().orElse(null),
        "%s[%s]".formatted(name, NamedFunction.name(qFunction))
    );
  }

  private static <Q, O extends Comparable<O>> SequencedMap<String, Objective<Q, O>> buildObjectives(
      List<Function<Q, O>> toMinObjectives,
      List<Function<Q, O>> toMaxObjectives
  ) {
    SequencedMap<String, Objective<Q, O>> objectives = Stream.concat(
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
            Utils.toSequencedMap(
                o -> NamedFunction.name(o.function()),
                o -> o
            )
        );
    if (objectives.size() != (toMinObjectives.size() + toMaxObjectives.size())) {
      Logger.getLogger(Problems.class.getName())
          .warning(
              "Objectives have conflicting names: to minimize is %s, to maximize is %s".formatted(
                  toMinObjectives.stream().map(NamedFunction::name).toList(),
                  toMaxObjectives.stream().map(NamedFunction::name).toList()
              )
          );
    }
    return objectives;
  }

  @Cacheable
  public static <S, Q, C extends Comparable<C>> TotalOrderQualityBasedProblem<S, Q> functionToTo(
      @Param(value = "name", iS = "{qFunction.name}") String name,
      @Param("qFunction") Function<S, Q> qualityFunction,
      @Param(value = "cFunction", dNPM = "f.identity()") Function<Q, C> comparableFunction,
      @Param(value = "type", dS = "minimize") OptimizationType type,
      @Param(value = "example", dNPM = "misc.nullValue()") S example
  ) {
    return TotalOrderQualityBasedProblem.of(
        qualityFunction,
        null,
        type.equals(OptimizationType.MAXIMIZE) ? Comparator.comparing(comparableFunction)
            .reversed() : Comparator.comparing(comparableFunction),
        example,
        name
    );
  }

  @Cacheable
  public static <S, CQ, O extends Comparable<O>> SimpleCBMOProblem<S, Function<S, CQ>, CQ, O> functionsToScbmo(
      @Param(value = "name", iS = "cases") String name,
      @Param("cases") List<Function<S, CQ>> cases,
      @Param("validationCases") List<Function<S, CQ>> validationCases,
      @Param("toMinObjectives") List<Function<List<CQ>, O>> toMinObjectives,
      @Param("toMaxObjectives") List<Function<List<CQ>, O>> toMaxObjectives,
      @Param(value = "example", dNPM = "misc.nullValue()") S example
  ) {
    return SimpleCBMOProblem.of(
        buildObjectives(
            toMinObjectives,
            toMaxObjectives
        ),
        (s, caseF) -> caseF.apply(s),
        IndexedProvider.from(cases),
        IndexedProvider.from(validationCases),
        example,
        name
    );
  }

  @Cacheable
  public static <S, PQ, AQ> CBTOProblem<S, QualityBasedProblem<S, PQ>, PQ, AQ> manyToCbto(
      @Param(value = "name", iS = "{problems}") String name,
      @Param("problems") List<QualityBasedProblem<S, PQ>> problems,
      @Param("validationProblems") List<QualityBasedProblem<S, PQ>> validationProblems,
      @Param(value = "aggregator", dNPM = "f.identity()") Function<List<PQ>, AQ> aggregator,
      @Param("comparator") Comparator<AQ> comparator
  ) {
    return CBTOProblem.of(
        aggregator,
        (s, p) -> p.apply(s),
        IndexedProvider.from(problems),
        IndexedProvider.from(validationProblems),
        comparator,
        problems.getFirst().example().orElse(null),
        name
    );
  }

  @Cacheable
  public static <S, Q, O> TotalOrderQualityBasedProblem<S, Q> moToSo(
      @Param(value = "name", iS = "{moProblem.name}[{objective}]") String name,
      @Param("objective") String objective,
      @Param("moProblem") MultiObjectiveProblem<S, Q, O> moProblem
  ) {
    return moProblem.toTotalOrderQualityBasedProblem(objective);
  }

  @Cacheable
  public static <S> SimpleMOProblem<S, Double> mtToMo(
      @Param(value = "name", iS = "mt2mo[{mtProblem.name}]") String name,
      @Param("mtProblem") MultiTargetProblem<S> mtProblem
  ) {
    return mtProblem.toMHOProblem();
  }

  @Cacheable
  public static <S, T> Problem<T> preMapped(
      @Param(value = "name", iS = "{problem.name}") String name,
      @Param(value = "mapper", dNPM = "ea.m.identity()") InvertibleMapper<T, S> mapper,
      @Param("problem") Problem<S> problem
  ) {
    S exampleS = problem.example().orElse(null);
    Function<T, S> f = mapper.mapperFor(exampleS);
    T exampleT = mapper.exampleFor(exampleS);
    return problem.on(f, exampleT);
  }

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
    return (SimpleMFBBMOProblem<S, B, O>) SimpleBBMOProblem.of(
        buildObjectives(
            toMinObjectives,
            toMaxObjectives
        ),
        (MultifidelityFunction<S, B>) ((s, fidelity) -> simulation.simulate(
            s,
            dT,
            new DoubleRange(initT, finalTRange.denormalize(fidelity))
        )),
        simulation.example().orElse(null),
        name
    );
  }

  @Cacheable
  public static <S, B extends Simulation.Outcome<BS>, BS, O extends Comparable<O>> SimpleMFBBMOProblem<S, B, O> simToResolutionSmfbbmo(
      @Param(value = "name", iS = "{simulation.name}[dT={dTRange.min}--{dTRange.max}]") String name,
      @Param("simulation") Simulation<S, BS, B> simulation,
      @Param("dTRange") DoubleRange dTRange,
      @Param("tRange") DoubleRange tRange,
      @Param("toMinObjectives") List<Function<B, O>> toMinObjectives,
      @Param("toMaxObjectives") List<Function<B, O>> toMaxObjectives
  ) {
    return (SimpleMFBBMOProblem<S, B, O>) SimpleBBMOProblem.of(
        buildObjectives(
            toMinObjectives,
            toMaxObjectives
        ),
        (MultifidelityFunction<S, B>) ((s, fidelity) -> simulation.simulate(s, dTRange.denormalize(fidelity), tRange)),
        simulation.example().orElse(null),
        name
    );
  }

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
    return SimpleBBMOProblem.of(
        behaviorObjectives,
        s -> simulation.simulate(s, dT, tRange),
        simulation.example().orElse(null),
        "%s[%s]".formatted(name, String.join(";", behaviorObjectives.keySet()))
    );
  }

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
            Utils.toSequencedMap(
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
            Utils.toSequencedMap(
                p -> NamedFunction.name(p.first()),
                Pair::second
            )
        );
    return SimpleMOProblem.of(
        comparators,
        simulationFunction.andThen(objectivesFunction),
        simulationFunction.andThen(objectivesFunction),
        simulation.example().orElse(null),
        "%s[%s]".formatted(name, String.join(";", comparators.keySet()))
    );
  }

  @Cacheable
  public static <S, B extends Simulation.Outcome<BS>, BS, Q> TotalOrderQualityBasedProblem<S, Q> simToTo(
      @Param(value = "name", iS = "{simulation.name}->{qFunction}") String name,
      @Param("simulation") Simulation<S, BS, B> simulation,
      @Param("dT") double dT,
      @Param("tRange") DoubleRange tRange,
      @Param("comparator") Comparator<Q> comparator,
      @Param("qFunction") Function<B, Q> qFunction
  ) {
    Function<S, Q> f = qFunction.compose(s -> simulation.simulate(s, dT, tRange));
    return TotalOrderQualityBasedProblem.of(
        f,
        f,
        comparator,
        simulation.example().orElse(null),
        name
    );
  }

  @Cacheable
  public static <S, B extends Simulation.Outcome<BS>, BS, Q> TotalOrderQualityBasedProblem<S, Q> simsToTo(
      @Param(value = "name", iS = "sims->{qFunction}->{aggregator}") String name,
      @Param("simulations") List<Simulation<S, BS, B>> simulations,
      @Param("dT") double dT,
      @Param("tRange") DoubleRange tRange,
      @Param("comparator") Comparator<Q> comparator,
      @Param("aggregator") Function<List<Q>, Q> aggregator,
      @Param("qFunction") Function<B, Q> qFunction
  ) {
    Function<S, Q> f = s -> aggregator.apply(
        simulations.stream()
            .map(sim -> qFunction.apply(sim.simulate(s, dT, tRange)))
            .toList()
    );
    return TotalOrderQualityBasedProblem.of(
        f,
        f,
        comparator,
        simulations.getFirst().example().orElse(null),
        name
    );
  }


  @Cacheable
  public static <S, O> SimpleMOProblem<S, O> smoToSubsettedSmo(
      @Param(value = "name", iS = "{smoProblem.name}") String name,
      @Param("objectives") List<String> objectives,
      @Param("smoProblem") SimpleMOProblem<S, O> smoProblem
  ) {
    return smoProblem.toReducedSimpleMOProblem(new HashSet<>(objectives));
  }

  @Cacheable
  public static <C extends ReinforcementLearningAgent<O, A, CS>, O, A, CS, TS> BBTOProblem<C, Simulation.Outcome<SingleAgentTask.Step<ReinforcementLearningAgent.RewardedInput<O>, A, TS>>, Double> srlatToBbto(
      @Param(value = "name", iS = "{task.name}") String name,
      @Param("task") SingleRLAgentTask<C, O, A, CS, TS> task,
      @Param("dT") double dT,
      @Param("tRange") DoubleRange tRange
  ) {
    return BBTOProblem.of(
        ((Comparator<Double>) Double::compareTo).reversed(),
        c -> task.simulate(c, dT, tRange),
        o -> o.snapshots()
            .values()
            .stream()
            .mapToDouble(s -> s.observation().reward())
            .sum(),
        task.example().orElse(null),
        name
    );
  }

  @Cacheable
  public static <C extends ReinforcementLearningAgent<O, A, CS>, O, A, CS> TotalOrderQualityBasedProblem<C, Double> srlatToTo(
      @Param(value = "name", iS = "{task.name}") String name,
      @Param("task") SingleRLAgentTask<C, O, A, CS, ?> task,
      @Param("dT") double dT,
      @Param("tRange") DoubleRange tRange
  ) {
    Function<C, Double> qualityFunction = c -> task.simulate(c, dT, tRange)
        .snapshots()
        .values()
        .stream()
        .mapToDouble(s -> s.observation().reward())
        .sum();
    return TotalOrderQualityBasedProblem.of(
        qualityFunction,
        qualityFunction,
        ((Comparator<Double>) Double::compareTo).reversed(),
        task.example().orElse(null),
        name
    );
  }

  public enum OptimizationType {
    MINIMIZE, MAXIMIZE
  }
}