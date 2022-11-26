/*
 * Copyright 2022 eric
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it.units.malelab.jgea.experimenter.builders;

import it.units.malelab.jgea.core.listener.NamedFunction;
import it.units.malelab.jgea.core.solver.Individual;
import it.units.malelab.jgea.core.solver.state.POSetPopulationState;
import it.units.malelab.jgea.core.solver.state.State;
import it.units.malelab.jgea.core.util.Misc;
import it.units.malelab.jgea.core.util.TextPlotter;
import it.units.malelab.jnb.core.Param;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class NamedFunctions {

  private NamedFunctions() {
  }

  @SuppressWarnings("unused")
  public static <G, S, Q> NamedFunction<POSetPopulationState<G, S, Q>, Collection<Individual<G, S, Q>>> all() {
    return NamedFunction.build("all", s -> s.getPopulation().all());
  }

  @SuppressWarnings("unused")
  public static <G, S, Q> NamedFunction<POSetPopulationState<G, S, Q>, Individual<G, S, Q>> best() {
    return NamedFunction.build("best", s -> Misc.first(s.getPopulation().firsts()));
  }

  @SuppressWarnings("unused")
  public static NamedFunction<POSetPopulationState<?, ?, ?>, Long> births() {
    return NamedFunction.build("births", "%6d", POSetPopulationState::getNOfBirths);
  }

  private static String c(String nAfter, String nBefore) {
    return NamedFunction.NAME_COMPOSER.apply(nAfter, nBefore);
  }

  @SuppressWarnings("unused")
  public static <X, T, R> NamedFunction<X, Collection<R>> each(
      @Param("map") NamedFunction<T, R> mapF,
      @Param("collection") NamedFunction<X, Collection<T>> collectionF,
      @Param(value = "s", dS = "%s") String s
  ) {
    return NamedFunction.build(
        c("each[%s]".formatted(mapF.getName()), collectionF.getName()),
        s,
        x -> collectionF.apply(x).stream().map(mapF).toList()
    );
  }

  @SuppressWarnings("unused")
  public static NamedFunction<POSetPopulationState<?, ?, ?>, Double> elapsed() {
    return NamedFunction.build("elapsed.seconds", "%5.1f", s -> s.getElapsedMillis() / 1000d);
  }

  @SuppressWarnings("unused")
  public static NamedFunction<POSetPopulationState<?, ?, ?>, Long> evals() {
    return NamedFunction.build("evals", "%6d", POSetPopulationState::getNOfFitnessEvaluations);
  }

  @SuppressWarnings("unused")
  public static <G, S, Q> NamedFunction<POSetPopulationState<G, S, Q>, Collection<Individual<G, S, Q>>> firsts() {
    return NamedFunction.build("firsts", s -> s.getPopulation().firsts());
  }

  @SuppressWarnings("unused")
  public static <X, F> NamedFunction<X, F> fitness(
      @Param(value = "individual", dNPM = "ea.nf.identity()") NamedFunction<X, Individual<?, ?, F>> individualF,
      @Param(value = "s", dS = "%s") String s
  ) {
    return NamedFunction.build(
        c("fitness", individualF.getName()),
        s,
        x -> individualF.apply(x).fitness()
    );
  }

  @SuppressWarnings("unused")
  public static <T, R> NamedFunction<T, R> formatted(
      @Param("s") String s,
      @Param("f") NamedFunction<T, R> f
  ) {
    return f.reformat(s);
  }

  @SuppressWarnings("unused")
  public static <X, G> NamedFunction<X, G> genotype(
      @Param(value = "individual", dNPM = "ea.nf.identity()") NamedFunction<X, Individual<G, ?, ?>> individualF,
      @Param(value = "s", dS = "%s") String s
  ) {
    return NamedFunction.build(
        c("genotype", individualF.getName()),
        s,
        x -> individualF.apply(x).genotype()
    );
  }

  @SuppressWarnings("unused")
  public static <X> NamedFunction<X, String> hist(
      @Param("collection") NamedFunction<X, Collection<Number>> collectionF,
      @Param(value = "nBins", dI = 8) int nBins
  ) {
    return NamedFunction.build(
        c("hist", collectionF.getName()),
        "%8.8s",
        x -> {
          Collection<Number> collection = collectionF.apply(x);
          return TextPlotter.histogram(
              collection instanceof List<Number> list ? list : new ArrayList<>(collection),
              nBins
          );
        }
    );
  }

  @SuppressWarnings("unused")
  public static <T> NamedFunction<T, T> identity() {
    return NamedFunction.build("id", t -> t);
  }

  @SuppressWarnings("unused")
  public static NamedFunction<POSetPopulationState<?, ?, ?>, Long> iterations() {
    return NamedFunction.build("iterations", "%3d", State::getNOfIterations);
  }

  @SuppressWarnings("unused")
  public static <G, S, Q> NamedFunction<POSetPopulationState<G, S, Q>, Collection<Individual<G, S, Q>>> lasts() {
    return NamedFunction.build("lasts", s -> s.getPopulation().lasts());
  }

  @SuppressWarnings("unused")
  public static <X, T extends Comparable<T>> NamedFunction<X, T> percentile(
      @Param("collection") NamedFunction<X, Collection<T>> collectionF,
      @Param("p") double p,
      @Param(value = "s", dS = "%s") String s
  ) {
    return NamedFunction.build(
        c("perc[%2d]".formatted((int) Math.round(p * 100)), collectionF.getName()),
        s,
        x -> {
          List<T> collection = collectionF.apply(x).stream().sorted().toList();
          int i = (int) Math.max(Math.min(((double) collection.size()) * p, collection.size() - 1), 0);
          return collection.get(i);
        }
    );
  }

  @SuppressWarnings("unused")
  public static <X> NamedFunction<X, Integer> size(
      @Param("f") NamedFunction<X, ?> f
  ) {
    return NamedFunction.build(
        c("size", f.getName()),
        x -> it.units.malelab.jgea.core.listener.NamedFunctions.size(f.apply(x))
    );
  }

  @SuppressWarnings("unused")
  public static <X, S> NamedFunction<X, S> solution(
      @Param(value = "individual", dNPM = "ea.nf.identity()") NamedFunction<X, Individual<?, S, ?>> individualF,
      @Param(value = "s", dS = "%s") String s
  ) {
    return NamedFunction.build(
        c("solution", individualF.getName()),
        s,
        x -> individualF.apply(x).solution()
    );
  }

  @SuppressWarnings("unused")
  public static <X, T> NamedFunction<X, Double> uniqueness(
      @Param("collection") NamedFunction<X, Collection<T>> collectionF
  ) {
    return NamedFunction.build(
        c("uniqueness", collectionF.getName()),
        "%4.2f",
        x -> {
          Collection<T> collection = collectionF.apply(x);
          return ((double) (new HashSet<>(collection).size())) / ((double) collection.size());
        }
    );
  }

}
