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

import io.github.ericmedvet.jgea.core.solver.Individual;
import io.github.ericmedvet.jgea.core.solver.state.POSetPopulationStateC;
import io.github.ericmedvet.jgea.core.solver.state.StateC;
import io.github.ericmedvet.jgea.core.util.Misc;
import io.github.ericmedvet.jgea.core.util.Pair;
import io.github.ericmedvet.jgea.core.util.Sized;
import io.github.ericmedvet.jgea.core.util.TextPlotter;
import java.util.*;
import java.util.function.Function;

public class NamedFunctions {

  private static final String DEFAULT_FORMAT = "%s";
  private static final long CACHE_SIZE = 100;

  private NamedFunctions() {}

  public static <G, S, F>
      NamedFunction<
              POSetPopulationStateC<? extends G, ? extends S, ? extends F>,
              Collection<? extends Individual<? extends G, ? extends S, ? extends F>>>
          all() {
    return f("all", e -> e.getPopulation().all());
  }

  @SuppressWarnings("unchecked")
  public static <T> NamedFunction<Object, T> as(Class<T> clazz) {
    return f("as[" + clazz.getSimpleName() + "]", o -> (T) o);
  }

  public static <V> NamedFunction<Map<String, V>, V> attribute(String name) {
    return f(name, "%" + name.length() + "s", map -> map.get(name));
  }

  public static <V> List<NamedFunction<? super Map<String, V>, V>> attributes(String... names) {
    List<NamedFunction<? super Map<String, V>, V>> functions = new ArrayList<>();
    for (String name : names) {
      functions.add(attribute(name));
    }
    return functions;
  }

  @SuppressWarnings("unused")
  public static NamedFunction<Number, String> bar(int l) {
    return f(
        "bar",
        NamedFunction.format(l),
        value -> TextPlotter.horizontalBar(value.doubleValue(), 0, 1, l));
  }

  public static <G, S, F>
      NamedFunction<
              POSetPopulationStateC<? extends G, ? extends S, ? extends F>,
              Individual<? extends G, ? extends S, ? extends F>>
          best() {
    return f("best", e -> Misc.first(e.getPopulation().firsts()));
  }

  public static <G, S, F>
      NamedFunction<POSetPopulationStateC<? extends G, ? extends S, ? extends F>, Long> births() {
    return f("births", "%5d", POSetPopulationStateC::getNOfBirths);
  }

  public static <F, T> NamedFunction<F, T> cachedF(String name, Function<F, T> function) {
    return f(name, Misc.cached(function, CACHE_SIZE));
  }

  public static <F, T> NamedFunction<F, T> cachedF(
      String name, String format, Function<F, T> function) {
    return f(name, format, Misc.cached(function, CACHE_SIZE));
  }

  public static <F, T> NamedFunction<F, T> cachedF(
      String name, String format, Function<F, T> function, long size) {
    return f(name, format, Misc.cached(function, size));
  }

  public static <T> NamedFunction<StateC, T> constant(String name, String format, T value) {
    return f(name, format, e -> value);
  }

  public static <T> NamedFunction<StateC, T> constant(String name, T value) {
    return constant(name, NamedFunction.format(value.toString().length()), value);
  }

  public static <F, T> NamedFunction<Collection<? extends F>, Collection<T>> each(
      NamedFunction<F, T> mapper) {
    return f(
        "each[" + mapper.getName() + "]",
        individuals ->
            individuals.stream().map(mapper).collect(java.util.stream.Collectors.toList()));
  }

  public static NamedFunction<StateC, Float> elapsedSeconds() {
    return f("elapsed.seconds", "%5.1f", e -> e.getElapsedMillis() / 1000f);
  }

  public static <F, T> NamedFunction<F, T> f(String name, Function<F, T> function) {
    return f(name, DEFAULT_FORMAT, function);
  }

  public static <F, T> NamedFunction<F, T> f(String name, String format, Function<F, T> function) {
    return NamedFunction.build(name, format, function);
  }

  public static <G, S, F>
      NamedFunction<
              POSetPopulationStateC<? extends G, ? extends S, ? extends F>,
              Collection<? extends Individual<? extends G, ? extends S, ? extends F>>>
          firsts() {
    return f("firsts", e -> e.getPopulation().firsts());
  }

  public static <F> NamedFunction<Individual<?, ?, ? extends F>, F> fitness() {
    return f("fitness", Individual::fitness);
  }

  public static <G, S, F>
      NamedFunction<POSetPopulationStateC<? extends G, ? extends S, ? extends F>, Long>
          fitnessEvaluations() {
    return f("fitness.evaluations", "%5d", POSetPopulationStateC::getNOfFitnessEvaluations);
  }

  public static <G, S, F>
      NamedFunction<Individual<? extends G, ? extends S, ? extends F>, Long>
          fitnessMappingIteration() {
    return f("birth.iteration", "%4d", Individual::fitnessMappingIteration);
  }

  public static <G> NamedFunction<Individual<? extends G, ?, ?>, G> genotype() {
    return f("genotype", Individual::genotype);
  }

  public static <G, S, F>
      NamedFunction<Individual<? extends G, ? extends S, ? extends F>, Long>
          genotypeBirthIteration() {
    return f("genotype.birth.iteration", "%4d", Individual::genotypeBirthIteration);
  }

  @SuppressWarnings("unchecked")
  public static NamedFunction<Collection<? extends Number>, String> hist(int bins) {
    return f(
        "hist",
        NamedFunction.format(bins),
        values ->
            TextPlotter.histogram(
                values instanceof List ? (List<? extends Number>) values : new ArrayList<>(values),
                bins));
  }

  public static NamedFunction<StateC, Long> iterations() {
    return f("iterations", "%4d", StateC::getNOfIterations);
  }

  public static <G, S, F>
      NamedFunction<
              POSetPopulationStateC<? extends G, ? extends S, ? extends F>,
              Collection<? extends Individual<? extends G, ? extends S, ? extends F>>>
          lasts() {
    return f("lasts", e -> e.getPopulation().lasts());
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

  public static <T> NamedFunction<List<? extends T>, T> nth(int index) {
    return f("[" + index + "]", l -> l.get(index));
  }

  public static <T> NamedFunction<Collection<? extends T>, T> one() {
    return f("one", Misc::first);
  }

  public static NamedFunction<StateC, Double> progress() {
    return f("progress", "%4.2f", s -> s.getProgress().rate());
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
    if (o instanceof Pair p) {
      Integer firstSize = size(p.first());
      Integer secondSize = size(p.second());
      if ((firstSize != null) && (secondSize != null)) {
        return firstSize + secondSize;
      }
    }
    return null;
  }

  public static <S> NamedFunction<Individual<?, ? extends S, ?>, S> solution() {
    return f("solution", Individual::solution);
  }

  public static NamedFunction<Collection<?>, Double> uniqueness() {
    return f(
        "uniqueness", "%4.2f", ts -> (double) ts.stream().distinct().count() / (double) ts.size());
  }
}
