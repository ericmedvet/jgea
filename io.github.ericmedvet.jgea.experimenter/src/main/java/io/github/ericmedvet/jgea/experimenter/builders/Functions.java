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

import io.github.ericmedvet.jgea.core.problem.MultiTargetProblem;
import io.github.ericmedvet.jgea.core.problem.Problem;
import io.github.ericmedvet.jgea.core.representation.sequence.bit.BitString;
import io.github.ericmedvet.jgea.core.representation.sequence.integer.IntString;
import io.github.ericmedvet.jgea.core.solver.Individual;
import io.github.ericmedvet.jgea.core.solver.POCPopulationState;
import io.github.ericmedvet.jgea.core.solver.State;
import io.github.ericmedvet.jgea.core.util.*;
import io.github.ericmedvet.jgea.core.util.Misc;
import io.github.ericmedvet.jgea.experimenter.Run;
import io.github.ericmedvet.jgea.experimenter.Utils;
import io.github.ericmedvet.jnb.core.Discoverable;
import io.github.ericmedvet.jnb.core.Param;
import io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction;
import io.github.ericmedvet.jnb.datastructure.NamedFunction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Logger;

@Discoverable(prefixTemplate = "ea.function|f")
public class Functions {

  private static final Logger L = Logger.getLogger(Functions.class.getName());
  private static final String DEFAULT_FORMAT = "%.0s";

  public static <X, I extends Individual<G, S, Q>, G, S, Q, Y> NamedFunction<X, Y> all(
      @Param(value = "beforeF", dNPM = "f.identity()") Function<X, POCPopulationState<I, G, S, Q, ?>> beforeF,
      @Param(value = "afterF", dNPM = "f.identity()") Function<Collection<I>, Y> afterF) {
    Function<POCPopulationState<I, G, S, Q, ?>, Collection<I>> f =
        state -> state.pocPopulation().all();
    return NamedFunction.from(beforeF, f, afterF, "all");
  }

  public static <X, I extends Individual<G, S, Q>, G, S, Q, Y> NamedFunction<X, Y> best(
      @Param(value = "beforeF", dNPM = "f.identity()") Function<X, POCPopulationState<I, G, S, Q, ?>> beforeF,
      @Param(value = "afterF", dNPM = "f.identity()") Function<I, Y> afterF) {
    Function<POCPopulationState<I, G, S, Q, ?>, I> f =
        state -> state.pocPopulation().all().iterator().next();
    return NamedFunction.from(beforeF, f, afterF, "best");
  }

  public static <X, Z, Y> NamedFunction<X, Y> toDoubleString(
      @Param(value = "beforeF", dNPM = "f.identity()") Function<X, Z> beforeF,
      @Param(value = "afterF", dNPM = "f.identity()") Function<List<Double>, Y> afterF) {
    Function<Z, List<Double>> f = z -> {
      if (z instanceof IntString is) {
        return is.asDoubleString();
      }
      if (z instanceof BitString bs) {
        return bs.asDoubleString();
      }
      if (z instanceof List<?> list) {
        return list.stream()
            .map(i -> {
              if (i instanceof Number n) {
                return n.doubleValue();
              }
              throw new IllegalArgumentException("Cannot convert %s to double"
                  .formatted(i.getClass().getSimpleName()));
            })
            .toList();
      }
      throw new IllegalArgumentException(
          "Cannot convert %s to double string".formatted(z.getClass().getSimpleName()));
    };
    return NamedFunction.from(beforeF, f, afterF, "to.double.string");
  }

  public static <X, Y> FormattedNamedFunction<X, Y> elapsedSecs(
      @Param(value = "beforeF", dNPM = "f.identity()") Function<X, State<?, ?>> beforeF,
      @Param(value = "afterF", dNPM = "f.identity()") Function<Double, Y> afterF,
      @Param(value = "format", dS = "%6.1f") String format) {
    Function<State<?, ?>, Double> f = s -> s.elapsedMillis() / 1000d;
    return FormattedNamedFunction.from(beforeF, f, afterF, format, "elapsed.secs");
  }

  public static <X, I extends Individual<G, S, Q>, G, S, Q, Y> NamedFunction<X, Y> firsts(
      @Param(value = "beforeF", dNPM = "f.identity()") Function<X, POCPopulationState<I, G, S, Q, ?>> beforeF,
      @Param(value = "afterF", dNPM = "f.identity()") Function<Collection<I>, Y> afterF) {
    Function<POCPopulationState<I, G, S, Q, ?>, Collection<I>> f =
        state -> state.pocPopulation().firsts();
    return NamedFunction.from(beforeF, f, afterF, "firsts");
  }

  public static <X, I extends Individual<G, S, Q>, G, S, Q, Y> NamedFunction<X, Y> mids(
      @Param(value = "beforeF", dNPM = "f.identity()") Function<X, POCPopulationState<I, G, S, Q, ?>> beforeF,
      @Param(value = "afterF", dNPM = "f.identity()") Function<Collection<I>, Y> afterF) {
    Function<POCPopulationState<I, G, S, Q, ?>, Collection<I>> f = state -> {
      Collection<I> all = new ArrayList<>(state.pocPopulation().all());
      all.removeAll(state.pocPopulation().firsts());
      all.removeAll(state.pocPopulation().lasts());
      return all;
    };
    return NamedFunction.from(beforeF, f, afterF, "mids");
  }

  public static <X, G, Y> FormattedNamedFunction<X, Y> genotype(
      @Param(value = "beforeF", dNPM = "f.identity()") Function<X, Individual<G, ?, ?>> beforeF,
      @Param(value = "afterF", dNPM = "f.identity()") Function<G, Y> afterF,
      @Param(value = "format", dS = DEFAULT_FORMAT) String format) {
    Function<Individual<G, ?, ?>, G> f = Individual::genotype;
    return FormattedNamedFunction.from(beforeF, f, afterF, format, "genotype");
  }

  public static <X, Y> FormattedNamedFunction<X, Y> hist(
      @Param(value = "nOfBins", dI = 8) int nOfBins,
      @Param(value = "beforeF", dNPM = "f.identity()") Function<X, Collection<Number>> beforeF,
      @Param(value = "afterF", dNPM = "f.identity()") Function<TextPlotter.Miniplot, Y> afterF) {
    Function<Collection<Number>, TextPlotter.Miniplot> f =
        vs -> TextPlotter.histogram(vs.stream().toList(), nOfBins);
    return FormattedNamedFunction.from(beforeF, f, afterF, "%" + nOfBins + "s", "hits");
  }

  public static <X, I extends Individual<G, S, Q>, G, S, Q, Y> NamedFunction<X, Y> lasts(
      @Param(value = "beforeF", dNPM = "f.identity()") Function<X, POCPopulationState<I, G, S, Q, ?>> beforeF,
      @Param(value = "afterF", dNPM = "f.identity()") Function<Collection<I>, Y> afterF) {
    Function<POCPopulationState<I, G, S, Q, ?>, Collection<I>> f =
        state -> state.pocPopulation().lasts();
    return NamedFunction.from(beforeF, f, afterF, "lasts");
  }

  public static <X, Y> FormattedNamedFunction<X, Y> nOfBirths(
      @Param(value = "beforeF", dNPM = "f.identity()") Function<X, POCPopulationState<?, ?, ?, ?, ?>> beforeF,
      @Param(value = "afterF", dNPM = "f.identity()") Function<Long, Y> afterF,
      @Param(value = "format", dS = "%5d") String format) {
    Function<POCPopulationState<?, ?, ?, ?, ?>, Long> f = POCPopulationState::nOfBirths;
    return FormattedNamedFunction.from(beforeF, f, afterF, format, "n.births");
  }

  public static <X, Y> FormattedNamedFunction<X, Y> nOfIterations(
      @Param(value = "beforeF", dNPM = "f.identity()") Function<X, State<?, ?>> beforeF,
      @Param(value = "afterF", dNPM = "f.identity()") Function<Long, Y> afterF,
      @Param(value = "format", dS = "%4d") String format) {
    Function<State<?, ?>, Long> f = State::nOfIterations;
    return FormattedNamedFunction.from(beforeF, f, afterF, format, "n.iterations");
  }

  public static <X, P extends Problem<S>, S, Y> NamedFunction<X, Y> problem(
      @Param(value = "beforeF", dNPM = "f.identity()") Function<X, State<P, S>> beforeF,
      @Param(value = "afterF", dNPM = "f.identity()") Function<P, Y> afterF) {
    Function<State<P, S>, P> f = State::problem;
    return NamedFunction.from(beforeF, f, afterF, "problem");
  }

  public static <X, Y> NamedFunction<X, Y> progress(
      @Param(value = "beforeF", dNPM = "f.identity()") Function<X, State<?, ?>> beforeF,
      @Param(value = "afterF", dNPM = "f.identity()") Function<Progress, Y> afterF) {
    Function<State<?, ?>, Progress> f = State::progress;
    return NamedFunction.from(beforeF, f, afterF, "progress");
  }

  public static <X, Q, Y> FormattedNamedFunction<X, Y> quality(
      @Param(value = "beforeF", dNPM = "f.identity()") Function<X, Individual<?, ?, Q>> beforeF,
      @Param(value = "afterF", dNPM = "f.identity()") Function<Q, Y> afterF,
      @Param(value = "format", dS = DEFAULT_FORMAT) String format) {
    Function<Individual<?, ?, Q>, Q> f = Individual::quality;
    return FormattedNamedFunction.from(beforeF, f, afterF, format, "quality");
  }

  public static <X, S, Y> FormattedNamedFunction<X, Y> solution(
      @Param(value = "beforeF", dNPM = "f.identity()") Function<X, Individual<?, S, ?>> beforeF,
      @Param(value = "afterF", dNPM = "f.identity()") Function<S, Y> afterF,
      @Param(value = "format", dS = DEFAULT_FORMAT) String format) {
    Function<Individual<?, S, ?>, S> f = Individual::solution;
    return FormattedNamedFunction.from(beforeF, f, afterF, format, "solution");
  }

  public static <X, Y> FormattedNamedFunction<X, Y> hypervolume2D(
      @Param("minReference") List<Double> minReference,
      @Param("maxReference") List<Double> maxReference,
      @Param(value = "beforeF", dNPM = "f.identity()") Function<X, Collection<List<Double>>> beforeF,
      @Param(value = "afterF", dNPM = "f.identity()") Function<Double, Y> afterF,
      @Param(value = "format", dS = "%.2f") String format) {
    Function<Collection<List<Double>>, Double> f = ps -> Misc.hypervolume2D(ps, minReference, maxReference);
    return FormattedNamedFunction.from(beforeF, f, afterF, format, "hv");
  }

  public static <X, P extends MultiTargetProblem<S>, S, Y> FormattedNamedFunction<X, Y> overallTargetDistance(
      @Param(value = "beforeF", dNPM = "f.identity()") Function<X, POCPopulationState<?, ?, S, ?, P>> beforeF,
      @Param(value = "afterF", dNPM = "f.identity()") Function<Double, Y> afterF,
      @Param(value = "format", dS = "%.2f") String format) {
    Function<POCPopulationState<?, ?, S, ?, P>, Double> f = state -> state.problem().targets().stream()
        .mapToDouble(ts -> state.pocPopulation().all().stream()
            .mapToDouble(s -> state.problem().distance().apply(s.solution(), ts))
            .min()
            .orElseThrow())
        .average()
        .orElseThrow();
    return FormattedNamedFunction.from(beforeF, f, afterF, format, "overall.target.distance");
  }

  public static <X, Y> FormattedNamedFunction<X, Y> size(
      @Param(value = "beforeF", dNPM = "f.identity()") Function<X, Object> beforeF,
      @Param(value = "afterF", dNPM = "f.identity()") Function<Integer, Y> afterF,
      @Param(value = "format", dS = "%d") String format) {
    Function<Object, Integer> f = o -> {
      if (o instanceof Sized s) {
        return s.size();
      }
      if (o instanceof Collection<?> c) {
        if (Misc.first(c) instanceof Sized s) {
          return c.stream().mapToInt(i -> s.size()).sum();
        }
        return c.size();
      }
      if (o instanceof String s) {
        return s.length();
      }
      throw new IllegalArgumentException(
          "Cannot compute size of %s".formatted(o.getClass().getSimpleName()));
    };
    return FormattedNamedFunction.from(beforeF, f, afterF, format, "size");
  }

  public static <X, Y> FormattedNamedFunction<X, Y> uniqueness(
      @Param(value = "beforeF", dNPM = "f.identity()") Function<X, Collection<?>> beforeF,
      @Param(value = "afterF", dNPM = "f.identity()") Function<Double, Y> afterF,
      @Param(value = "format", dS = "%5.3f") String format) {
    Function<Collection<?>, Double> f =
        ts -> (double) ts.stream().distinct().count() / (double) ts.size();
    return FormattedNamedFunction.from(beforeF, f, afterF, format, "uniqueness");
  }

  public static <X, Y> FormattedNamedFunction<X, Y> runKey(
      @Param("runKey") Map.Entry<String, String> runKey,
      @Param(value = "beforeF", dNPM = "f.identity()") Function<X, Run<?, ?, ?, ?>> beforeF,
      @Param(value = "afterF", dNPM = "f.identity()") Function<String, Y> afterF,
      @Param(value = "format", dS = DEFAULT_FORMAT) String format) {
    Function<Run<?, ?, ?, ?>, String> f = run -> Utils.interpolate(runKey.getValue(), run);
    return FormattedNamedFunction.from(beforeF, f, afterF, format, runKey.getValue());
  }
}
