/*-
 * ========================LICENSE_START=================================
 * jgea-core
 * %%
 * Copyright (C) 2018 - 2023 Eric Medvet
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

package io.github.ericmedvet.jgea.core.listener;

import io.github.ericmedvet.jgea.core.problem.MultiTargetProblem;
import io.github.ericmedvet.jgea.core.solver.Individual;
import io.github.ericmedvet.jgea.core.solver.POCPopulationState;
import io.github.ericmedvet.jgea.core.solver.State;
import io.github.ericmedvet.jgea.core.util.Misc;
import io.github.ericmedvet.jgea.core.util.Pair;
import io.github.ericmedvet.jgea.core.util.Sized;
import io.github.ericmedvet.jgea.core.util.TextPlotter;
import io.github.ericmedvet.jsdynsym.core.DoubleRange;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

public class NamedFunctions {

  private static final String DEFAULT_FORMAT = "%s";

  private NamedFunctions() {}

  public static <I extends Individual<G, S, Q>, G, S, Q>
      NamedFunction<POCPopulationState<I, G, S, Q>, Collection<I>> all() {
    return f("all", e -> e.pocPopulation().all());
  }

  @SuppressWarnings("unchecked")
  public static <T> NamedFunction<Object, T> as(Class<T> clazz) {
    return f("as[" + clazz.getSimpleName() + "]", o -> (T) o);
  }

  public static <V> NamedFunction<Map<String, V>, V> attribute(String name) {
    return f(name, "%" + name.length() + "s", map -> map.get(name));
  }

  public static <V> List<NamedFunction<Map<String, V>, V>> attributes(String... names) {
    List<NamedFunction<Map<String, V>, V>> functions = new ArrayList<>();
    for (String name : names) {
      functions.add(attribute(name));
    }
    return functions;
  }

  @SuppressWarnings("unused")
  public static NamedFunction<Number, TextPlotter.Miniplot> bar(int l) {
    return f("bar", NamedFunction.format(l), value -> TextPlotter.horizontalBar(value.doubleValue(), 0, 1, l));
  }

  public static <I extends Individual<G, S, Q>, G, S, Q> NamedFunction<POCPopulationState<I, G, S, Q>, I> best() {
    return f("best", e -> Misc.first(e.pocPopulation().firsts()));
  }

  public static <T> NamedFunction<State, T> constant(String name, String format, T value) {
    return f(name, format, e -> value);
  }

  public static <T> NamedFunction<State, T> constant(String name, T value) {
    return constant(name, NamedFunction.format(value.toString().length()), value);
  }

  public static <F, T> NamedFunction<Collection<? extends F>, Collection<T>> each(NamedFunction<F, T> mapper) {
    return f(
        "each[" + mapper.getName() + "]",
        individuals -> individuals.stream().map(mapper).collect(java.util.stream.Collectors.toList()));
  }

  public static NamedFunction<State, Float> elapsedSeconds() {
    return f("elapsed.seconds", "%5.1f", e -> e.elapsedMillis() / 1000f);
  }

  public static <F, T> NamedFunction<F, T> f(String name, Function<F, T> function) {
    return f(name, DEFAULT_FORMAT, function);
  }

  public static <F, T> NamedFunction<F, T> f(String name, String format, Function<F, T> function) {
    return NamedFunction.build(name, format, function);
  }

  public static <I extends Individual<G, S, Q>, G, S, Q>
      NamedFunction<POCPopulationState<I, G, S, Q>, Collection<I>> firsts() {
    return f("firsts", e -> e.pocPopulation().firsts());
  }

  public static <I extends Individual<G, S, Q>, G, S, Q>
      NamedFunction<POCPopulationState<I, G, S, Q>, Long> fitnessEvaluations() {
    return f("fitness.evaluations", "%5d", POCPopulationState::nOfFitnessEvaluations);
  }

  public static <I extends Individual<G, S, Q>, G, S, Q> NamedFunction<I, Long> fitnessMappingIteration() {
    return f("birth.iteration", "%4d", Individual::qualityMappingIteration);
  }

  public static <I extends Individual<G, S, Q>, G, S, Q> NamedFunction<I, G> genotype() {
    return f("genotype", Individual::genotype);
  }

  public static <I extends Individual<G, S, Q>, G, S, Q> NamedFunction<I, Long> genotypeBirthIteration() {
    return f("genotype.birth.iteration", "%4d", Individual::genotypeBirthIteration);
  }

  public static NamedFunction<Collection<? extends Number>, TextPlotter.Miniplot> hist(int bins) {
    return f(
        "hist",
        NamedFunction.format(bins),
        values -> TextPlotter.histogram(
            values instanceof List ? (List<? extends Number>) values : new ArrayList<>(values), bins));
  }

  public static NamedFunction<Collection<List<Double>>, Double> hypervolume2D(List<DoubleRange> ranges) {
    return f("hypervolume", "%5.3f", ps -> {
      record Point(double x, double y) {}
      List<Point> points = ps.stream()
          .map(p -> new Point(p.get(0), p.get(1)))
          .sorted(Comparator.comparingDouble(Point::x))
          .toList();
      points = Stream.of(List.of(new Point(ranges.get(0).min(), points.get(0).y)), points)
          .flatMap(List::stream)
          .toList();
      double v = 0;
      for (int i = 1; i < points.size(); i = i + 1) {
        v = v + (points.get(i).x - points.get(i - 1).x) * (points.get(i).y + points.get(i - 1).y) / 2d;
      }
      return v;
    });
  }

  public static <I extends Individual<G, S, Q>, G, S, Q>
      NamedFunction<POCPopulationState<I, G, S, Q>, Collection<I>> lasts() {
    return f("lasts", e -> e.pocPopulation().lasts());
  }

  public static <T> NamedFunction<Collection<? extends T>, T> max(Comparator<T> comparator) {
    return f("max", ts -> ts.stream().max(comparator).orElse(null));
  }

  public static <T> NamedFunction<Collection<? extends T>, T> median(Comparator<T> comparator) {
    return f("median", ts -> Misc.median(ts, comparator));
  }

  public static <T> NamedFunction<Collection<? extends T>, T> min(Comparator<T> comparator) {
    return f("min", ts -> ts.stream().min(comparator).orElse(null));
  }

  public static <I extends Individual<G, S, Q>, G, S, Q>
      NamedFunction<POCPopulationState<I, G, S, Q>, Long> nOfBirths() {
    return f("births", "%5d", POCPopulationState::nOfBirths);
  }

  public static NamedFunction<State, Long> nOfIterations() {
    return f("iterations", "%4d", State::nOfIterations);
  }

  public static <T> NamedFunction<List<? extends T>, T> nth(int index) {
    return f("[" + index + "]", l -> l.get(index));
  }

  public static <T> NamedFunction<Collection<? extends T>, T> one() {
    return f("one", Misc::first);
  }

  public static <S> NamedFunction<Collection<S>, Double> overallTargetDistance(MultiTargetProblem<S> problem) {
    return f(
        "overall.target.distance",
        "%5.3f",
        ss -> problem.targets().stream()
                .mapToDouble(ts -> ss.stream()
                    .mapToDouble(s -> problem.distance().apply(s, ts))
                    .min()
                    .orElseThrow())
                .sum()
            / (double) problem.targets().size());
  }

  public static NamedFunction<State, Double> progress() {
    return f("progress", "%4.2f", s -> s.progress().rate());
  }

  public static <I extends Individual<?, ?, Q>, Q> NamedFunction<I, Q> quality() {
    return f("quality", Individual::quality);
  }

  public static NamedFunction<Object, Number> size() {
    return f("size", "%3d", NamedFunctions::size);
  }

  public static Integer size(Object o) {
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
    if (o instanceof Pair<?, ?> p) {
      Integer firstSize = size(p.first());
      Integer secondSize = size(p.second());
      if ((firstSize != null) && (secondSize != null)) {
        return firstSize + secondSize;
      }
    }
    return null;
  }

  public static <I extends Individual<G, S, Q>, G, S, Q> NamedFunction<I, S> solution() {
    return f("solution", Individual::solution);
  }

  public static NamedFunction<Collection<?>, Double> uniqueness() {
    return f("uniqueness", "%4.2f", ts -> (double) ts.stream().distinct().count() / (double) ts.size());
  }
}
