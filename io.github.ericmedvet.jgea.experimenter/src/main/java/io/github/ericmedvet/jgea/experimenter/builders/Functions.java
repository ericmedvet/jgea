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
import io.github.ericmedvet.jgea.core.problem.ProblemWithValidation;
import io.github.ericmedvet.jgea.core.representation.sequence.bit.BitString;
import io.github.ericmedvet.jgea.core.representation.sequence.integer.IntString;
import io.github.ericmedvet.jgea.core.solver.Individual;
import io.github.ericmedvet.jgea.core.solver.POCPopulationState;
import io.github.ericmedvet.jgea.core.solver.State;
import io.github.ericmedvet.jgea.core.solver.mapelites.MEIndividual;
import io.github.ericmedvet.jgea.core.solver.mapelites.MapElites;
import io.github.ericmedvet.jgea.core.util.Misc;
import io.github.ericmedvet.jgea.core.util.Progress;
import io.github.ericmedvet.jgea.core.util.Sized;
import io.github.ericmedvet.jgea.core.util.TextPlotter;
import io.github.ericmedvet.jgea.experimenter.Run;
import io.github.ericmedvet.jgea.experimenter.Utils;
import io.github.ericmedvet.jgea.problem.simulation.SimulationBasedProblem;
import io.github.ericmedvet.jnb.core.Discoverable;
import io.github.ericmedvet.jnb.core.Param;
import io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction;
import io.github.ericmedvet.jnb.datastructure.NamedFunction;
import io.github.ericmedvet.jsdynsym.control.Simulation;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

@Discoverable(prefixTemplate = "ea.function|f")
public class Functions {

  private Functions() {}

  @SuppressWarnings("unused")
  public static <X, I extends Individual<G, S, Q>, G, S, Q> NamedFunction<X, Collection<I>> all(
      @Param(value = "of", dNPM = "f.identity()") Function<X, POCPopulationState<I, G, S, Q, ?>> beforeF) {
    Function<POCPopulationState<I, G, S, Q, ?>, Collection<I>> f =
        state -> state.pocPopulation().all();
    return NamedFunction.from(f, "all").compose(beforeF);
  }

  @SuppressWarnings("unused")
  public static <X, I extends Individual<G, S, Q>, G, S, Q> NamedFunction<X, I> best(
      @Param(value = "of", dNPM = "f.identity()") Function<X, POCPopulationState<I, G, S, Q, ?>> beforeF) {
    Function<POCPopulationState<I, G, S, Q, ?>, I> f =
        state -> state.pocPopulation().firsts().iterator().next();
    return NamedFunction.from(f, "best").compose(beforeF);
  }

  @SuppressWarnings("unused")
  public static <X> FormattedNamedFunction<X, Double> elapsedSecs(
      @Param(value = "of", dNPM = "f.identity()") Function<X, State<?, ?>> beforeF,
      @Param(value = "format", dS = "%6.1f") String format) {
    Function<State<?, ?>, Double> f = s -> s.elapsedMillis() / 1000d;
    return FormattedNamedFunction.from(f, format, "elapsed.secs").compose(beforeF);
  }

  @SuppressWarnings("unused")
  public static <X, I extends Individual<G, S, Q>, G, S, Q> NamedFunction<X, Collection<I>> firsts(
      @Param(value = "of", dNPM = "f.identity()") Function<X, POCPopulationState<I, G, S, Q, ?>> beforeF) {
    Function<POCPopulationState<I, G, S, Q, ?>, Collection<I>> f =
        state -> state.pocPopulation().firsts();
    return NamedFunction.from(f, "firsts").compose(beforeF);
  }

  @SuppressWarnings("unused")
  public static <X, G> FormattedNamedFunction<X, G> genotype(
      @Param(value = "of", dNPM = "f.identity()") Function<X, Individual<G, ?, ?>> beforeF,
      @Param(value = "format", dS = "%s") String format) {
    Function<Individual<G, ?, ?>, G> f = Individual::genotype;
    return FormattedNamedFunction.from(f, format, "genotype").compose(beforeF);
  }

  @SuppressWarnings("unused")
  public static <X> FormattedNamedFunction<X, TextPlotter.Miniplot> hist(
      @Param(value = "nOfBins", dI = 8) int nOfBins,
      @Param(value = "of", dNPM = "f.identity()") Function<X, Collection<Number>> beforeF) {
    Function<Collection<Number>, TextPlotter.Miniplot> f =
        vs -> TextPlotter.histogram(vs.stream().toList(), nOfBins);
    return FormattedNamedFunction.from(f, "%" + nOfBins + "s", "hist").compose(beforeF);
  }

  @SuppressWarnings("unused")
  public static <X> FormattedNamedFunction<X, Double> hypervolume2D(
      @Param("minReference") List<Double> minReference,
      @Param("maxReference") List<Double> maxReference,
      @Param(value = "of", dNPM = "f.identity()") Function<X, Collection<List<Double>>> beforeF,
      @Param(value = "format", dS = "%.2f") String format) {
    Function<Collection<List<Double>>, Double> f = ps -> Misc.hypervolume2D(ps, minReference, maxReference);
    return FormattedNamedFunction.from(f, format, "hv").compose(beforeF);
  }

  @SuppressWarnings("unused")
  public static <X> FormattedNamedFunction<X, Long> id(
      @Param(value = "of", dNPM = "f.identity()") Function<X, Individual<?, ?, ?>> beforeF,
      @Param(value = "format", dS = "%6d") String format) {
    Function<Individual<?, ?, ?>, Long> f = Individual::id;
    return FormattedNamedFunction.from(f, format, "id").compose(beforeF);
  }

  @SuppressWarnings("unused")
  public static <X, I extends Individual<G, S, Q>, G, S, Q> NamedFunction<X, Collection<I>> lasts(
      @Param(value = "of", dNPM = "f.identity()") Function<X, POCPopulationState<I, G, S, Q, ?>> beforeF) {
    Function<POCPopulationState<I, G, S, Q, ?>, Collection<I>> f =
        state -> state.pocPopulation().lasts();
    return NamedFunction.from(f, "lasts").compose(beforeF);
  }

  @SuppressWarnings("unused")
  public static <X> FormattedNamedFunction<X, Integer> meBin(
      @Param(value = "of", dNPM = "f.identity()") Function<X, MapElites.Descriptor.Coordinate> beforeF,
      @Param(value = "format", dS = "%3d") String format) {
    Function<MapElites.Descriptor.Coordinate, Integer> f = MapElites.Descriptor.Coordinate::bin;
    return FormattedNamedFunction.from(f, format, "bin").compose(beforeF);
  }

  @SuppressWarnings("unused")
  public static <X> FormattedNamedFunction<X, List<MapElites.Descriptor.Coordinate>> meCoordinates(
      @Param(value = "of", dNPM = "f.identity()") Function<X, MEIndividual<?, ?, ?>> beforeF,
      @Param(value = "format", dS = "%s") String format) {
    Function<MEIndividual<?, ?, ?>, List<MapElites.Descriptor.Coordinate>> f = MEIndividual::coordinates;
    return FormattedNamedFunction.from(f, format, "coords").compose(beforeF);
  }

  @SuppressWarnings("unused")
  public static <X> FormattedNamedFunction<X, Double> meValue(
      @Param(value = "of", dNPM = "f.identity()") Function<X, MapElites.Descriptor.Coordinate> beforeF,
      @Param(value = "format", dS = "%.2f") String format) {
    Function<MapElites.Descriptor.Coordinate, Double> f = MapElites.Descriptor.Coordinate::value;
    return FormattedNamedFunction.from(f, format, "value").compose(beforeF);
  }

  @SuppressWarnings("unused")
  public static <X, I extends Individual<G, S, Q>, G, S, Q> NamedFunction<X, Collection<I>> mids(
      @Param(value = "of", dNPM = "f.identity()") Function<X, POCPopulationState<I, G, S, Q, ?>> beforeF) {
    Function<POCPopulationState<I, G, S, Q, ?>, Collection<I>> f =
        state -> state.pocPopulation().mids();
    return NamedFunction.from(f, "mids").compose(beforeF);
  }

  @SuppressWarnings("unused")
  public static <X> FormattedNamedFunction<X, Long> nOfBirths(
      @Param(value = "of", dNPM = "f.identity()") Function<X, POCPopulationState<?, ?, ?, ?, ?>> beforeF,
      @Param(value = "format", dS = "%5d") String format) {
    Function<POCPopulationState<?, ?, ?, ?, ?>, Long> f = POCPopulationState::nOfBirths;
    return FormattedNamedFunction.from(f, format, "n.births").compose(beforeF);
  }

  @SuppressWarnings("unused")
  public static <X> FormattedNamedFunction<X, Long> nOfEvals(
      @Param(value = "of", dNPM = "f.identity()") Function<X, POCPopulationState<?, ?, ?, ?, ?>> beforeF,
      @Param(value = "format", dS = "%5d") String format) {
    Function<POCPopulationState<?, ?, ?, ?, ?>, Long> f = POCPopulationState::nOfQualityEvaluations;
    return FormattedNamedFunction.from(f, format, "n.evals").compose(beforeF);
  }

  @SuppressWarnings("unused")
  public static <X> FormattedNamedFunction<X, Long> nOfIterations(
      @Param(value = "of", dNPM = "f.identity()") Function<X, State<?, ?>> beforeF,
      @Param(value = "format", dS = "%4d") String format) {
    Function<State<?, ?>, Long> f = State::nOfIterations;
    return FormattedNamedFunction.from(f, format, "n.iterations").compose(beforeF);
  }

  @SuppressWarnings("unused")
  public static <X, P extends MultiTargetProblem<S>, S> FormattedNamedFunction<X, Double> overallTargetDistance(
      @Param(value = "of", dNPM = "f.identity()") Function<X, POCPopulationState<?, ?, S, ?, P>> beforeF,
      @Param(value = "format", dS = "%.2f") String format) {
    Function<POCPopulationState<?, ?, S, ?, P>, Double> f = state -> state.problem().targets().stream()
        .mapToDouble(ts -> state.pocPopulation().all().stream()
            .mapToDouble(s -> state.problem().distance().apply(s.solution(), ts))
            .min()
            .orElseThrow())
        .average()
        .orElseThrow();
    return FormattedNamedFunction.from(f, format, "overall.target.distance").compose(beforeF);
  }

  @SuppressWarnings("unused")
  public static <X> FormattedNamedFunction<X, Collection<Long>> parentIds(
      @Param(value = "of", dNPM = "f.identity()") Function<X, Individual<?, ?, ?>> beforeF,
      @Param(value = "format", dS = "%s") String format) {
    Function<Individual<?, ?, ?>, Collection<Long>> f = Individual::parentIds;
    return FormattedNamedFunction.from(f, format, "parent.ids").compose(beforeF);
  }

  @SuppressWarnings("unused")
  public static <X, P extends MultiTargetProblem<S>, S> FormattedNamedFunction<X, List<Double>> popTargetDistances(
      @Param(value = "of", dNPM = "f.identity()") Function<X, POCPopulationState<?, ?, S, ?, P>> beforeF,
      @Param(value = "format", dS = "%.2f") String format) {
    Function<POCPopulationState<?, ?, S, ?, P>, List<Double>> f = state -> state.problem().targets().stream()
        .mapToDouble(ts -> state.pocPopulation().all().stream()
            .mapToDouble(s -> state.problem().distance().apply(s.solution(), ts))
            .min()
            .orElseThrow())
        .boxed()
        .toList();
    return FormattedNamedFunction.from(f, format, "pop.target.distances").compose(beforeF);
  }

  @SuppressWarnings("unused")
  public static <X, P extends Problem<S>, S> NamedFunction<X, P> problem(
      @Param(value = "of", dNPM = "f.identity()") Function<X, State<P, S>> beforeF) {
    Function<State<P, S>, P> f = State::problem;
    return NamedFunction.from(f, "problem").compose(beforeF);
  }

  @SuppressWarnings("unused")
  public static <X> NamedFunction<X, Progress> progress(
      @Param(value = "of", dNPM = "f.identity()") Function<X, State<?, ?>> beforeF) {
    Function<State<?, ?>, Progress> f = State::progress;
    return NamedFunction.from(f, "progress").compose(beforeF);
  }

  @SuppressWarnings("unused")
  public static <X, Q> FormattedNamedFunction<X, Q> quality(
      @Param(value = "of", dNPM = "f.identity()") Function<X, Individual<?, ?, Q>> beforeF,
      @Param(value = "format", dS = "%s") String format) {
    Function<Individual<?, ?, Q>, Q> f = Individual::quality;
    return FormattedNamedFunction.from(f, format, "quality").compose(beforeF);
  }

  @SuppressWarnings("unused")
  public static <X> NamedFunction<X, Double> rate(
      @Param(value = "of", dNPM = "f.identity()") Function<X, Progress> beforeF) {
    Function<Progress, Double> f = Progress::rate;
    return NamedFunction.from(f, "rate").compose(beforeF);
  }

  @SuppressWarnings("unused")
  public static <X> FormattedNamedFunction<X, String> runKey(
      @Param("runKey") Map.Entry<String, String> runKey,
      @Param(value = "of", dNPM = "f.identity()") Function<X, Run<?, ?, ?, ?>> beforeF,
      @Param(value = "format", dS = "%s") String format) {
    Function<Run<?, ?, ?, ?>, String> f = run -> Utils.interpolate(runKey.getValue(), run);
    return FormattedNamedFunction.from(f, format, runKey.getKey()).compose(beforeF);
  }

  @SuppressWarnings("unused")
  public static <X> FormattedNamedFunction<X, String> runString(
      @Param(value = "name", iS = "{s}") String name,
      @Param("s") String s,
      @Param(value = "of", dNPM = "f.identity()") Function<X, Run<?, ?, ?, ?>> beforeF,
      @Param(value = "format", dS = "%s") String format) {
    Function<Run<?, ?, ?, ?>, String> f = run -> Utils.interpolate(s, run);
    return FormattedNamedFunction.from(f, format, name).compose(beforeF);
  }

  @SuppressWarnings("unused")
  public static <X, B, O extends Simulation.Outcome<B>> FormattedNamedFunction<X, O> simOutcome(
      @Param(value = "of", dNPM = "f.identity()")
          Function<X, SimulationBasedProblem.QualityOutcome<B, O, ?>> beforeF,
      @Param(value = "format", dS = "%s") String format) {
    Function<SimulationBasedProblem.QualityOutcome<B, O, ?>, O> f = SimulationBasedProblem.QualityOutcome::outcome;
    return FormattedNamedFunction.from(f, format, "sim.outcome").compose(beforeF);
  }

  @SuppressWarnings("unused")
  public static <X, Q> FormattedNamedFunction<X, Q> simQuality(
      @Param(value = "of", dNPM = "f.identity()")
          Function<X, SimulationBasedProblem.QualityOutcome<?, ?, Q>> beforeF,
      @Param(value = "format", dS = "%s") String format) {
    Function<SimulationBasedProblem.QualityOutcome<?, ?, Q>, Q> f = SimulationBasedProblem.QualityOutcome::quality;
    return FormattedNamedFunction.from(f, format, "sim.quality").compose(beforeF);
  }

  @SuppressWarnings("unused")
  public static <X> FormattedNamedFunction<X, Integer> size(
      @Param(value = "of", dNPM = "f.identity()") Function<X, Object> beforeF,
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
    return FormattedNamedFunction.from(f, format, "size").compose(beforeF);
  }

  @SuppressWarnings("unused")
  public static <X, S> FormattedNamedFunction<X, S> solution(
      @Param(value = "of", dNPM = "f.identity()") Function<X, Individual<?, S, ?>> beforeF,
      @Param(value = "format", dS = "%s") String format) {
    Function<Individual<?, S, ?>, S> f = Individual::solution;
    return FormattedNamedFunction.from(f, format, "solution").compose(beforeF);
  }

  @SuppressWarnings("unused")
  public static <X, Z> NamedFunction<X, Z> supplied(
      @Param(value = "of", dNPM = "f.identity()") Function<X, Supplier<Z>> beforeF) {
    Function<Supplier<Z>, Z> f = Supplier::get;
    return NamedFunction.from(f, "supplied").compose(beforeF);
  }

  @SuppressWarnings("unused")
  public static <X, P extends MultiTargetProblem<S>, S> FormattedNamedFunction<X, List<Double>> targetDistances(
      @Param("problem") P problem,
      @Param(value = "of", dNPM = "f.identity()") Function<X, Individual<?, S, ?>> beforeF,
      @Param(value = "format", dS = "%.2f") String format) {
    Function<Individual<?, S, ?>, List<Double>> f = i -> problem.targets().stream()
        .map(t -> problem.distance().apply(i.solution(), t))
        .toList();
    return FormattedNamedFunction.from(f, format, "target.distances").compose(beforeF);
  }

  @SuppressWarnings("unused")
  public static <X, Z> NamedFunction<X, List<Double>> toDoubleString(
      @Param(value = "of", dNPM = "f.identity()") Function<X, Z> beforeF) {
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
    return NamedFunction.from(f, "to.double.string").compose(beforeF);
  }

  @SuppressWarnings("unused")
  public static <X, I extends Individual<?, S, Q>, S, Q, P extends ProblemWithValidation<S, Q>>
      FormattedNamedFunction<X, Q> validationQuality(
          @Param(value = "of", dNPM = "f.identity()") Function<X, POCPopulationState<?, ?, S, Q, P>> beforeF,
          @Param(value = "individual", dNPM = "ea.f.best()")
              Function<POCPopulationState<?, ?, S, Q, P>, Individual<?, S, Q>> individualF,
          @Param(value = "format", dS = "%s") String format) {
    Function<POCPopulationState<?, ?, S, Q, P>, Q> f = state -> state.problem()
        .validationQualityFunction()
        .apply(individualF.apply(state).solution());
    return FormattedNamedFunction.from(
            f, format, "validation.quality[%s]".formatted(NamedFunction.name(individualF)))
        .compose(beforeF);
  }
}
