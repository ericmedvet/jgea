/*-
 * ========================LICENSE_START=================================
 * jgea-experimenter
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

package io.github.ericmedvet.jgea.experimenter.builders;

import io.github.ericmedvet.jgea.core.listener.NamedFunction;
import io.github.ericmedvet.jgea.core.problem.MultiTargetProblem;
import io.github.ericmedvet.jgea.core.problem.ProblemWithValidation;
import io.github.ericmedvet.jgea.core.problem.QualityBasedProblem;
import io.github.ericmedvet.jgea.core.representation.sequence.bit.BitString;
import io.github.ericmedvet.jgea.core.representation.sequence.integer.IntString;
import io.github.ericmedvet.jgea.core.solver.Individual;
import io.github.ericmedvet.jgea.core.solver.POCPopulationState;
import io.github.ericmedvet.jgea.core.solver.State;
import io.github.ericmedvet.jgea.core.util.Misc;
import io.github.ericmedvet.jgea.core.util.TextPlotter;
import io.github.ericmedvet.jgea.experimenter.Run;
import io.github.ericmedvet.jgea.experimenter.Utils;
import io.github.ericmedvet.jnb.core.Discoverable;
import io.github.ericmedvet.jnb.core.NamedParamMap;
import io.github.ericmedvet.jnb.core.Param;
import io.github.ericmedvet.jnb.core.ParamMap;
import io.github.ericmedvet.jsdynsym.grid.Grid;
import io.github.ericmedvet.jsdynsym.grid.GridUtils;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.*;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Discoverable(prefixTemplate = "ea.namedFunctions|nf")
public class NamedFunctions {

  private static final Logger L = Logger.getLogger(NamedFunctions.class.getName());

  private NamedFunctions() {}

  public enum Op {
    PLUS("+"),
    MINUS("-"),
    PROD("*"),
    DIV("/");
    private final String rendered;

    Op(String rendered) {
      this.rendered = rendered;
    }
  }

  @SuppressWarnings("unused")
  public static <G, S, Q>
      NamedFunction<POCPopulationState<?, G, S, Q, ?>, Collection<? extends Individual<G, S, Q>>> all() {
    return NamedFunction.build("all", s -> s.pocPopulation().all());
  }

  @SuppressWarnings("unused")
  public static <X, S> NamedFunction<X, List<Double>> asDoubleString(
      @Param(value = "f", dNPM = "ea.nf.identity()") NamedFunction<X, S> f) {
    return NamedFunction.build(f.getName(), x -> {
      S s = f.apply(x);
      if (s instanceof IntString is) {
        return is.asDoubleString();
      }
      if (s instanceof BitString bs) {
        return bs.asDoubleString();
      }
      if (s instanceof List<?> list) {
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
          "Cannot convert %s to double string".formatted(s.getClass().getSimpleName()));
    });
  }

  @SuppressWarnings("unused")
  public static <X> NamedFunction<X, Number> avg(
      @Param(value = "collection", dNPM = "ea.nf.identity()") NamedFunction<X, Collection<Number>> collectionF,
      @Param(value = "s", dS = "%s") String s) {
    return NamedFunction.build(c("avg", collectionF.getName()), s, x -> collectionF.apply(x).stream()
        .mapToDouble(Number::doubleValue)
        .average()
        .orElse(Double.NaN));
  }

  @SuppressWarnings("unused")
  public static <X> NamedFunction<X, String> base64(
      @Param(value = "f", dNPM = "ea.nf.identity()") NamedFunction<X, Serializable> f) {
    return NamedFunction.build(c("base64", f.getName()), x -> {
      try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
          ObjectOutputStream oos = new ObjectOutputStream(baos)) {
        oos.writeObject(f.apply(x));
        oos.flush();
        return Base64.getEncoder().encodeToString(baos.toByteArray());
      } catch (Throwable t) {
        L.warning("Cannot serialize %s due to %s".formatted(f.getName(), t));
        return "not-serializable";
      }
    });
  }

  @SuppressWarnings("unused")
  public static <G, S, Q> NamedFunction<POCPopulationState<?, G, S, Q, ?>, Individual<G, S, Q>> best() {
    return NamedFunction.build("best", s -> Misc.first(s.pocPopulation().firsts()));
  }

  @SuppressWarnings("unused")
  public static <Q, T> NamedFunction<POCPopulationState<?, ?, ?, Q, ?>, T> bestFitness(
      @Param(value = "f", dNPM = "ea.nf.identity()") NamedFunction<Q, T> function,
      @Param(value = "s", dS = "%s") String s) {
    return NamedFunction.build(
        c(function.getName(), "fitness", "best"),
        s.equals("%s") ? function.getFormat() : s,
        state -> function.apply(
            Objects.requireNonNull(Misc.first(state.pocPopulation().firsts()))
                .quality()));
  }

  @SuppressWarnings("unused")
  public static NamedFunction<POCPopulationState<?, ?, ?, ?, ?>, Long> births() {
    return NamedFunction.build("births", "%6d", POCPopulationState::nOfBirths);
  }

  private static String c(String... names) {
    return Arrays.stream(names).reduce(NamedFunction.NAME_COMPOSER::apply).orElseThrow();
  }

  @SuppressWarnings("unused")
  public static <X, T, R> NamedFunction<X, Collection<R>> each(
      @Param("map") NamedFunction<T, R> mapF,
      @Param(value = "collection", dNPM = "ea.nf.identity()") NamedFunction<X, Collection<T>> collectionF,
      @Param(value = "s", dS = "%s") String s) {
    return NamedFunction.build(
        c("each[%s]".formatted(mapF.getName()), collectionF.getName()),
        s,
        x -> collectionF.apply(x).stream().map(mapF).toList());
  }

  @SuppressWarnings("unused")
  public static NamedFunction<POCPopulationState<?, ?, ?, ?, ?>, Double> elapsed() {
    return NamedFunction.build("elapsed.seconds", "%5.1f", s -> s.elapsedMillis() / 1000d);
  }

  @SuppressWarnings("unused")
  public static NamedFunction<POCPopulationState<?, ?, ?, ?, ?>, Long> evals() {
    return NamedFunction.build("evals", "%6d", POCPopulationState::nOfFitnessEvaluations);
  }

  @SuppressWarnings("unused")
  public static <X> NamedFunction<X, Double> expr(
      @Param("f1") NamedFunction<X, Number> f1, @Param("f2") NamedFunction<X, Number> f2, @Param("op") Op op) {
    return NamedFunction.build(
        "%s%s%s".formatted(f1.getName(), op.rendered, f2.getName()), f1.getFormat(), x -> switch (op) {
          case PLUS -> f1.apply(x).doubleValue() + f2.apply(x).doubleValue();
          case MINUS -> f1.apply(x).doubleValue() - f2.apply(x).doubleValue();
          case PROD -> f1.apply(x).doubleValue() * f2.apply(x).doubleValue();
          case DIV -> f1.apply(x).doubleValue() / f2.apply(x).doubleValue();
        });
  }

  @SuppressWarnings("unused")
  public static <X, T, R> NamedFunction<X, R> f(
      @Param("outerF") Function<T, R> outerFunction,
      @Param(value = "innerF", dNPM = "ea.nf.identity()") NamedFunction<X, T> innerFunction,
      @Param(value = "name", dS = "") String name,
      @Param(value = "s", dS = "%s") String s,
      @Param(value = "", injection = Param.Injection.MAP) ParamMap map) {
    if ((name == null) || name.isEmpty()) {
      name = ((NamedParamMap) map.value("outerF", ParamMap.Type.NAMED_PARAM_MAP)).getName();
    }
    return NamedFunction.build(
        c(name, innerFunction.getName()), s, x -> outerFunction.apply(innerFunction.apply(x)));
  }

  @SuppressWarnings("unused")
  public static <I extends Individual<G, S, Q>, G, S, Q, P extends QualityBasedProblem<S, Q>>
      NamedFunction<POCPopulationState<I, G, S, Q, P>, Collection<I>> firsts() {
    return io.github.ericmedvet.jgea.core.listener.NamedFunctions.firsts();
  }

  @SuppressWarnings("unused")
  public static <X, F, T> NamedFunction<X, T> fitness(
      @Param(value = "individual", dNPM = "ea.nf.identity()") NamedFunction<X, Individual<?, ?, F>> individualF,
      @Param(value = "f", dNPM = "ea.nf.identity()") NamedFunction<F, T> function,
      @Param(value = "s", dS = "%s") String s) {
    return NamedFunction.build(
        c(function.getName(), "fitness", individualF.getName()),
        s.equals("%s") ? function.getFormat() : s,
        x -> function.apply(individualF.apply(x).quality()));
  }

  @SuppressWarnings("unused")
  public static <Q> NamedFunction<POCPopulationState<?, ?, ?, Q, ?>, TextPlotter.Miniplot> fitnessHist(
      @Param(value = "f", dNPM = "ea.nf.identity()") NamedFunction<Q, Number> function,
      @Param(value = "nBins", dI = 8) int nBins) {
    return NamedFunction.build(
        c("hist", "each[%s]".formatted(c(function.getName(), "fitness")), "all"),
        "%" + nBins + "." + nBins + "s",
        state -> {
          List<Number> numbers = state.pocPopulation().all().stream()
              .map(i -> function.apply(i.quality()))
              .toList();
          return TextPlotter.histogram(numbers, nBins);
        });
  }

  @SuppressWarnings("unused")
  public static <T, R> NamedFunction<T, R> formatted(@Param("s") String s, @Param("f") NamedFunction<T, R> f) {
    return f.reformat(s);
  }

  @SuppressWarnings("unused")
  public static <X, G> NamedFunction<X, G> genotype(
      @Param(value = "individual", dNPM = "ea.nf.identity()") NamedFunction<X, Individual<G, ?, ?>> individualF,
      @Param(value = "s", dS = "%s") String s) {
    return NamedFunction.build(c("genotype", individualF.getName()), s, x -> individualF
        .apply(x)
        .genotype());
  }

  @SuppressWarnings("unused")
  public static <T> NamedFunction<T, String> grid(
      @Param(value = "f", dNPM = "ea.nf.identity()") NamedFunction<T, Grid<Object>> f,
      @Param(value = "s", dS = "%20.20s") String s,
      @Param(value = "cellSeparator", dS = "") String cellSeparator,
      @Param(value = "rowSeparator", dS = ";") String rowSeparator,
      @Param(value = "emptyCell", dS = "·") String emptyCell) {
    final String finalCellSeparator = cellSeparator == null ? "" : cellSeparator;
    return NamedFunction.build(c("grid", f.getName()), s, t -> {
      Grid<Object> g = f.apply(t);
      return IntStream.range(0, g.h())
          .mapToObj(y -> IntStream.range(0, g.w())
              .mapToObj(x -> g.get(x, y) == null
                  ? emptyCell
                  : g.get(x, y).toString())
              .collect(Collectors.joining(finalCellSeparator)))
          .collect(Collectors.joining(rowSeparator));
    });
  }

  @SuppressWarnings("unused")
  public static <X> NamedFunction<X, Integer> gridCount(
      @Param(value = "f", dNPM = "ea.nf.identity()") NamedFunction<X, Grid<?>> f,
      @Param(value = "s", dS = "%2d") String s) {
    return NamedFunction.build(c("grid.count", f.getName()), s, x -> GridUtils.count(f.apply(x), Objects::nonNull));
  }

  @SuppressWarnings("unused")
  public static <X> NamedFunction<X, Integer> gridH(
      @Param(value = "f", dNPM = "ea.nf.identity()") NamedFunction<X, Grid<?>> f,
      @Param(value = "s", dS = "%2d") String s) {
    return NamedFunction.build(c("grid.h", f.getName()), s, x -> GridUtils.h(f.apply(x), Objects::nonNull));
  }

  @SuppressWarnings("unused")
  public static <X> NamedFunction<X, Integer> gridW(
      @Param(value = "f", dNPM = "ea.nf.identity()") NamedFunction<X, Grid<?>> f,
      @Param(value = "s", dS = "%2d") String s) {
    return NamedFunction.build(c("grid.w", f.getName()), s, x -> GridUtils.w(f.apply(x), Objects::nonNull));
  }

  @SuppressWarnings("unused")
  public static <X> NamedFunction<X, TextPlotter.Miniplot> hist(
      @Param(value = "collection", dNPM = "ea.nf.identity()") NamedFunction<X, Collection<Number>> collectionF,
      @Param(value = "nBins", dI = 8) int nBins) {
    return NamedFunction.build(c("hist", collectionF.getName()), "%" + nBins + "." + nBins + "s", x -> {
      Collection<Number> collection = collectionF.apply(x);
      return TextPlotter.histogram(
          collection instanceof List<Number> list ? list : new ArrayList<>(collection), nBins);
    });
  }

  @SuppressWarnings("unused")
  public static <X> NamedFunction<X, Double> hypervolume2D(
      @Param(value = "collection", dNPM = "ea.nf.identity()")
          NamedFunction<X, Collection<List<Double>>> collectionF,
      @Param("minReference") List<Double> minReference,
      @Param("maxReference") List<Double> maxReference,
      @Param(value = "s", dS = "%s") String s) {
    return io.github.ericmedvet.jgea.core.listener.NamedFunctions.hypervolume2D(minReference, maxReference)
        .of(collectionF)
        .reformat(s);
  }

  @SuppressWarnings("unused")
  public static <T> NamedFunction<T, T> identity() {
    return NamedFunction.build("", t -> t);
  }

  @SuppressWarnings("unused")
  public static NamedFunction<POCPopulationState<?, ?, ?, ?, ?>, Long> iterations() {
    return NamedFunction.build("iterations", "%3d", State::nOfIterations);
  }

  @SuppressWarnings("unused")
  public static <I extends Individual<G, S, Q>, G, S, Q, P extends QualityBasedProblem<S, Q>>
      NamedFunction<POCPopulationState<I, G, S, Q, P>, Collection<I>> lasts() {
    return io.github.ericmedvet.jgea.core.listener.NamedFunctions.lasts();
  }

  @SuppressWarnings("unused")
  public static <X, T extends Comparable<T>> NamedFunction<X, T> max(
      @Param(value = "collection", dNPM = "ea.nf.identity()") NamedFunction<X, Collection<T>> collectionF,
      @Param(value = "s", dS = "%s") String s) {
    return NamedFunction.build(c("max", collectionF.getName()), s, x -> {
      List<T> collection = collectionF.apply(x).stream().sorted().toList();
      return collectionF.apply(x).stream().max(Comparable::compareTo).orElse(null);
    });
  }

  @SuppressWarnings("unused")
  public static <X, T extends Comparable<T>> NamedFunction<X, T> median(
      @Param(value = "collection", dNPM = "ea.nf.identity()") NamedFunction<X, Collection<T>> collectionF,
      @Param(value = "s", dS = "%s") String s) {
    return percentile(collectionF, 0.5, s);
  }

  @SuppressWarnings("unused")
  public static <I extends Individual<G, S, Q>, G, S, Q, P extends QualityBasedProblem<S, Q>>
      NamedFunction<POCPopulationState<I, G, S, Q, P>, Collection<I>> mids() {
    return io.github.ericmedvet.jgea.core.listener.NamedFunctions.mids();
  }

  @SuppressWarnings("unused")
  public static <X, T extends Comparable<T>> NamedFunction<X, T> min(
      @Param(value = "collection", dNPM = "ea.nf.identity()") NamedFunction<X, Collection<T>> collectionF,
      @Param(value = "s", dS = "%s") String s) {
    return NamedFunction.build(c("min", collectionF.getName()), s, x -> {
      List<T> collection = collectionF.apply(x).stream().sorted().toList();
      return collectionF.apply(x).stream().min(Comparable::compareTo).orElse(null);
    });
  }

  @SuppressWarnings("unused")
  public static <X, T> NamedFunction<? super X, T> nth(
      @Param(value = "list", dNPM = "ea.nf.identity()")
          NamedFunction<? super X, ? extends List<? extends T>> listF,
      @Param("n") int n,
      @Param(value = "s", dS = "%s") String s) {
    return io.github.ericmedvet.jgea.core.listener.NamedFunctions.<T>nth(n)
        .of(listF)
        .reformat(s)
        .rename(listF.getName() + "[%d]".formatted(n));
  }

  @SuppressWarnings("unused")
  public static <X, T> NamedFunction<X, List<T>> nthItems(
      @Param(value = "list", dNPM = "ea.nf.identity()") NamedFunction<X, List<T>> listF,
      @Param("n") int n,
      @Param("k") int k) {
    return NamedFunction.build(listF.getName() + "[%dn+%d]".formatted(n, k), x -> {
      List<T> l = listF.apply(x);
      return IntStream.range(0, l.size())
          .filter(i -> (i % n) == k)
          .mapToObj(l::get)
          .toList();
    });
  }

  @SuppressWarnings("unused")
  public static <I extends Individual<G, S, Double>, G, S, P extends MultiTargetProblem<S>>
      NamedFunction<POCPopulationState<I, G, S, Double, P>, Double> overallTargetDistance() {
    return io.github.ericmedvet.jgea.core.listener.NamedFunctions.overallTargetDistance();
  }

  @SuppressWarnings("unused")
  public static <X, T extends Comparable<T>> NamedFunction<X, T> percentile(
      @Param(value = "collection", dNPM = "ea.nf.identity()") NamedFunction<X, Collection<T>> collectionF,
      @Param("p") double p,
      @Param(value = "s", dS = "%s") String s) {
    return NamedFunction.build(
        c("perc[%2d]".formatted((int) Math.round(p * 100)), collectionF.getName()),
        s,
        x -> Misc.percentile(collectionF.apply(x), Comparable::compareTo, p));
  }

  @SuppressWarnings("unused")
  public static NamedFunction<POCPopulationState<?, ?, ?, ?, ?>, Double> progress() {
    return NamedFunction.build("progress", "%4.2f", s -> s.progress().rate());
  }

  @SuppressWarnings("unused")
  public static <X, N extends Number> NamedFunction<X, Double> quantized(
      @Param(value = "f", dNPM = "ea.nf.identity()") NamedFunction<X, N> f,
      @Param("r") double r,
      @Param(value = "s", dS = "%s") String s) {
    return io.github.ericmedvet.jgea.core.listener.NamedFunctions.quantized(r, s)
        .of(f)
        .reformat(s);
  }

  @SuppressWarnings("unused")
  public static NamedFunction<Run<?, ?, ?, ?>, ?> runKey(@Param("runKey") Map.Entry<String, String> entry) {
    return NamedFunction.build(entry.getKey(), "%s", r -> Utils.interpolate(entry.getValue(), r));
  }

  @SuppressWarnings("unused")
  public static <X> NamedFunction<X, Integer> size(
      @Param(value = "f", dNPM = "ea.nf.identity()") NamedFunction<X, ?> f,
      @Param(value = "s", dS = "%s") String s) {
    return NamedFunction.build(
        c("size", f.getName()),
        s,
        x -> io.github.ericmedvet.jgea.core.listener.NamedFunctions.size(f.apply(x)));
  }

  @SuppressWarnings("unused")
  public static <X, S> NamedFunction<X, S> solution(
      @Param(value = "individual", dNPM = "ea.nf.identity()") NamedFunction<X, Individual<?, S, ?>> individualF,
      @Param(value = "s", dS = "%s") String s) {
    return NamedFunction.build(c("solution", individualF.getName()), s, x -> individualF
        .apply(x)
        .solution());
  }

  @SuppressWarnings("unused")
  public static <X, T> NamedFunction<X, List<T>> subList(
      @Param(value = "list", dNPM = "ea.nf.identity()") NamedFunction<X, List<T>> listF,
      @Param("n") int n,
      @Param("i") int i) {
    return NamedFunction.build(listF.getName() + "[%d/%d]".formatted(i, n), x -> {
      List<T> l = listF.apply(x);
      return l.subList(l.size() / n * i, Math.min(l.size(), l.size() / n * (i + 1)));
    });
  }

  @SuppressWarnings("unused")
  public static <X, S> NamedFunction<X, List<Double>> targetDistances(
      @Param(value = "individual", dNPM = "ea.nf.identity()") NamedFunction<X, Individual<?, S, ?>> individualF,
      @Param("problem") MultiTargetProblem<S> problem) {
    return NamedFunction.build(c("targets.dist", individualF.getName()), (X x) -> {
      S s = individualF.apply(x).solution();
      return problem.targets().stream()
          .map(t -> problem.distance().apply(s, t))
          .toList();
    });
  }

  @SuppressWarnings("unused")
  public static <X, T> NamedFunction<X, Double> uniqueness(
      @Param(value = "collection", dNPM = "ea.nf.identity()") NamedFunction<X, Collection<T>> collectionF) {
    return NamedFunction.build(c("uniqueness", collectionF.getName()), "%4.2f", x -> {
      Collection<T> collection = collectionF.apply(x);
      return ((double) (new HashSet<>(collection).size())) / ((double) collection.size());
    });
  }

  @SuppressWarnings("unused")
  public static <I extends Individual<G, S, Q>, G, S, Q, P extends ProblemWithValidation<S, Q>>
      NamedFunction<POCPopulationState<I, G, S, Q, P>, Q> validationFitness(
          @Param(value = "individual", dNPM = "ea.nf.best()")
              NamedFunction<POCPopulationState<I, G, S, Q, P>, Individual<?, S, ?>> individualF,
          @Param(value = "s", dS = "%s") String s) {
    return NamedFunction.build(c("validation", individualF.getName()), s, state -> state.problem()
        .validationQualityFunction()
        .apply(individualF.apply(state).solution()));
  }
}
