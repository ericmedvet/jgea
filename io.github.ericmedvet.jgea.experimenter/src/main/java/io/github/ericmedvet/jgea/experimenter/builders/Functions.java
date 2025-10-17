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

import io.github.ericmedvet.jgea.core.InvertibleMapper;
import io.github.ericmedvet.jgea.core.order.PartialComparator;
import io.github.ericmedvet.jgea.core.problem.BehaviorBasedProblem;
import io.github.ericmedvet.jgea.core.problem.MultiTargetProblem;
import io.github.ericmedvet.jgea.core.problem.Problem;
import io.github.ericmedvet.jgea.core.problem.QualityBasedProblem;
import io.github.ericmedvet.jgea.core.representation.programsynthesis.ttpn.Network;
import io.github.ericmedvet.jgea.core.representation.sequence.bit.BitString;
import io.github.ericmedvet.jgea.core.representation.sequence.integer.IntString;
import io.github.ericmedvet.jgea.core.representation.tree.Tree;
import io.github.ericmedvet.jgea.core.solver.Individual;
import io.github.ericmedvet.jgea.core.solver.MultiFidelityPOCPopulationState;
import io.github.ericmedvet.jgea.core.solver.POCPopulationState;
import io.github.ericmedvet.jgea.core.solver.State;
import io.github.ericmedvet.jgea.core.solver.cabea.GridPopulationState;
import io.github.ericmedvet.jgea.core.solver.mapelites.*;
import io.github.ericmedvet.jgea.core.util.*;
import io.github.ericmedvet.jgea.experimenter.Run;
import io.github.ericmedvet.jgea.experimenter.Utils;
import io.github.ericmedvet.jnb.core.Cacheable;
import io.github.ericmedvet.jnb.core.Discoverable;
import io.github.ericmedvet.jnb.core.Param;
import io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction;
import io.github.ericmedvet.jnb.datastructure.Grid;
import io.github.ericmedvet.jnb.datastructure.NamedFunction;
import io.github.ericmedvet.jviz.core.drawer.Drawer;
import io.github.ericmedvet.jviz.core.drawer.Video;
import io.github.ericmedvet.jviz.core.drawer.VideoBuilder;
import io.github.ericmedvet.jviz.core.plot.*;
import io.github.ericmedvet.jviz.core.plot.csv.*;
import io.github.ericmedvet.jviz.core.plot.image.*;
import io.github.ericmedvet.jviz.core.plot.image.Configuration;
import io.github.ericmedvet.jviz.core.plot.video.*;
import io.github.ericmedvet.jviz.core.util.VideoUtils;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

@Discoverable(prefixTemplate = "ea.function|f")
public class Functions {

  private Functions() {
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <X, I extends Individual<G, S, Q>, G, S, Q> NamedFunction<X, Collection<I>> all(
      @Param(value = "of", dNPM = "f.identity()") Function<X, POCPopulationState<I, G, S, Q, ?>> beforeF
  ) {
    Function<POCPopulationState<I, G, S, Q, ?>, Collection<I>> f = state -> state.pocPopulation()
        .all();
    return NamedFunction.from(f, "all").compose(beforeF);
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <X, T> FormattedNamedFunction<X, Collection<T>> archiveContents(
      @Param(value = "of", dNPM = "f.identity()") Function<X, Archive<T>> beforeF,
      @Param(value = "format", dS = "%4.2f") String format
  ) {
    Function<Archive<T>, Collection<T>> f = a -> a.asMap().values();
    return FormattedNamedFunction.from(f, format, "contents").compose(beforeF);
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <X, T> FormattedNamedFunction<X, Double> archiveCoverage(
      @Param(value = "of", dNPM = "f.identity()") Function<X, Archive<T>> beforeF,
      @Param(value = "format", dS = "%4.2f") String format
  ) {
    Function<Archive<T>, Double> f = a -> (double) a.asMap().size() / (double) a.capacity();
    return FormattedNamedFunction.from(f, format, "coverage").compose(beforeF);
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <X, T> NamedFunction<X, Grid<T>> archiveToGrid(
      @Param(value = "of", dNPM = "f.identity()") Function<X, Archive<T>> beforeF
  ) {
    Function<Archive<T>, Grid<T>> f = a -> Grid.create(
        a.binUpperBounds().getFirst(),
        a.binUpperBounds().get(1),
        (x, y) -> a.get(List.of(x, y))
    );
    return NamedFunction.from(f, "archive.to.grid").compose(beforeF);
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <X> FormattedNamedFunction<X, Double> asDouble(
      @Param(value = "of", dNPM = "f.identity()") Function<X, String> beforeF,
      @Param(value = "format", dS = "%s") String format
  ) {
    Function<String, Double> f = Double::parseDouble;
    return FormattedNamedFunction.from(f, format, NamedFunction.UNNAMED_NAME).compose(beforeF);
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <X, B> FormattedNamedFunction<X, B> behavior(
      @Param(value = "of", dNPM = "f.identity()") Function<X, BehaviorBasedProblem.Outcome<B, ?>> beforeF,
      @Param(value = "format", dS = "%s") String format
  ) {
    Function<BehaviorBasedProblem.Outcome<B, ?>, B> f = BehaviorBasedProblem.Outcome::behavior;
    return FormattedNamedFunction.from(f, format, "behavior").compose(beforeF);
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <X, BQ> FormattedNamedFunction<X, BQ> behaviorQuality(
      @Param(value = "of", dNPM = "f.identity()") Function<X, BehaviorBasedProblem.Outcome<?, BQ>> beforeF,
      @Param(value = "format", dS = "%s") String format
  ) {
    Function<BehaviorBasedProblem.Outcome<?, BQ>, BQ> f = BehaviorBasedProblem.Outcome::behaviorQuality;
    return FormattedNamedFunction.from(f, format, "behavior.quality").compose(beforeF);
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <X, I extends Individual<G, S, Q>, G, S, Q> NamedFunction<X, I> best(
      @Param(value = "of", dNPM = "f.identity()") Function<X, POCPopulationState<I, G, S, Q, ?>> beforeF
  ) {
    Function<POCPopulationState<I, G, S, Q, ?>, I> f = state -> state.pocPopulation()
        .firsts()
        .iterator()
        .next();
    return NamedFunction.from(f, "best").compose(beforeF);
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <T, R> NamedFunction<T, R> cached(
      @Param(value = "of", dNPM = "f.identity()") Function<T, R> f
  ) {
    if (f instanceof FormattedNamedFunction<T, R> fnf) {
      return FormattedNamedFunction.from(FunctionUtils.cached(fnf), fnf.format(), fnf.name());
    }
    if (f instanceof NamedFunction<T, R> nf) {
      return NamedFunction.from(FunctionUtils.cached(nf), nf.name());
    }
    return NamedFunction.from(FunctionUtils.cached(f));
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <X, G, S, Q> NamedFunction<X, Archive<? extends MEIndividual<G, S, Q>>> coMeArchive1(
      @Param(value = "of", dNPM = "f.identity()") Function<X, CoMEPopulationState<G, ?, S, ?, ?, Q, ?>> beforeF
  ) {
    Function<CoMEPopulationState<G, ?, S, ?, ?, Q, ?>, Archive<? extends MEIndividual<G, S, Q>>> f = CoMEPopulationState::archive1;
    return NamedFunction.from(f, "archive1").compose(beforeF);
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <X, G, S, Q> NamedFunction<X, Archive<? extends MEIndividual<G, S, Q>>> coMeArchive2(
      @Param(value = "of", dNPM = "f.identity()") Function<X, CoMEPopulationState<?, G, ?, S, ?, Q, ?>> beforeF
  ) {
    Function<CoMEPopulationState<?, G, ?, S, ?, Q, ?>, Archive<? extends MEIndividual<G, S, Q>>> f = CoMEPopulationState::archive2;
    return NamedFunction.from(f, "archive2").compose(beforeF);
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <X> NamedFunction<X, Map<List<Double>, List<Double>>> coMeStrategy1Field(
      @Param(value = "of", dNPM = "f.identity()") Function<X, CoMEPopulationState<?, ?, ?, ?, ?, ?, ?>> beforeF,
      @Param(value = "relative", dB = true) boolean relative
  ) {
    Function<CoMEPopulationState<?, ?, ?, ?, ?, ?, ?>, Map<List<Double>, List<Double>>> f = state -> state.strategy1()
        .asField(
            state.descriptors1()
                .stream()
                .map(MapElites.Descriptor::nOfBins)
                .toList(),
            relative
        );
    return NamedFunction.from(f, "coMe.strategy1.field").compose(beforeF);
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <X> NamedFunction<X, Map<List<Double>, List<Double>>> coMeStrategy2Field(
      @Param(value = "of", dNPM = "f.identity()") Function<X, CoMEPopulationState<?, ?, ?, ?, ?, ?, ?>> beforeF,
      @Param(value = "relative", dB = true) boolean relative
  ) {
    Function<CoMEPopulationState<?, ?, ?, ?, ?, ?, ?>, Map<List<Double>, List<Double>>> f = state -> state.strategy2()
        .asField(
            state.descriptors1()
                .stream()
                .map(MapElites.Descriptor::nOfBins)
                .toList(),
            relative
        );
    return NamedFunction.from(f, "coMe.strategy2.field").compose(beforeF);
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <X, G, S, Q, I extends Individual<G, S, Q>> NamedFunction<X, Archive<MEIndividual<G, S, Q>>> computedArchive(
      @Param(value = "of", dNPM = "f.identity()") Function<X, Collection<I>> beforeF,
      @Param("descriptors") List<MapElites.Descriptor<G, S, Q>> descriptors,
      @Param(value = "qComparator", dNPM = "ea.f.qualityComparator(of = ea.f.problem())") Function<X, PartialComparator<? super Q>> qPartialComparatorFunction
  ) {
    Function<X, Archive<MEIndividual<G, S, Q>>> f = x -> {
      Collection<I> individuals = beforeF.apply(x);
      List<MEIndividual<G, S, Q>> meIndividuals = individuals.stream()
          .map(i -> MEIndividual.from(i, descriptors))
          .toList();
      Archive<MEIndividual<G, S, Q>> archive = new Archive<>(
          descriptors.stream().map(MapElites.Descriptor::nOfBins).toList()
      );
      PartialComparator<MEIndividual<G, S, Q>> iPartialComparator = qPartialComparatorFunction.apply(x)
          .comparing(MEIndividual::quality);
      meIndividuals.forEach(i -> archive.put(i.bins(), i, iPartialComparator));
      return archive;
    };
    return NamedFunction.from(f, "computed.archive");
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <X> FormattedNamedFunction<X, Double> crossEpistasis(
      @Param(value = "of", dNPM = "f.identity()") Function<X, IntString> beforeF,
      @Param(value = "startOffset", dI = 0) int startOffset,
      @Param(value = "endOffset", dI = 0) int endOffset,
      @Param(value = "splitOffset", dI = 0) int splitOffset,
      @Param(value = "format", dS = "%5.3f") String format
  ) {
    Function<IntString, Double> f = is -> {
      List<Integer> indexes = is.genes().subList(startOffset, is.genes().size() - endOffset);
      List<Integer> leftIndexes = indexes.subList(0, splitOffset);
      List<Integer> rightIndexes = indexes.subList(splitOffset, indexes.size());
      Set<Integer> commonIndexes = new HashSet<>(leftIndexes);
      commonIndexes.retainAll(rightIndexes);
      double leftRate = (double) commonIndexes.size() / (double) leftIndexes.size();
      double rightRate = (double) commonIndexes.size() / (double) rightIndexes.size();
      return (leftRate + rightRate) / 2d;
    };
    return FormattedNamedFunction.from(
        f,
        format,
        "cross.epistasis[%d;%d;%d]".formatted(startOffset, endOffset, splitOffset)
    ).compose(beforeF);
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <X, P extends XYPlot<D>, D> NamedFunction<X, String> csvPlotter(
      @Param(value = "of", dNPM = "f.identity()") Function<X, P> beforeF,
      @Param(value = "columnNameJoiner", dS = ".") String columnNameJoiner,
      @Param(value = "doubleFormat", dS = "%.3e") String doubleFormat,
      @Param(value = "delimiter", dS = "\t") String delimiter,
      @Param(value = "missingDataString", dS = "nan") String missingDataString,
      @Param(value = "mode", dS = "paper_friendly") io.github.ericmedvet.jviz.core.plot.csv.Configuration.Mode mode
  ) {
    io.github.ericmedvet.jviz.core.plot.csv.Configuration configuration = new io.github.ericmedvet.jviz.core.plot.csv.Configuration(
        columnNameJoiner,
        doubleFormat,
        delimiter,
        List.of(new io.github.ericmedvet.jviz.core.plot.csv.Configuration.Replacement("\\W+", ".")),
        missingDataString
    );
    Function<P, String> f = p -> {
      if (p instanceof DistributionPlot dp) {
        return new DistributionPlotCsvBuilder(configuration, mode).apply(dp);
      }
      if (p instanceof LandscapePlot lsp) {
        return new LandscapePlotCsvBuilder(configuration, mode).apply(lsp);
      }
      if (p instanceof XYDataSeriesPlot xyp) {
        return new XYDataSeriesPlotCsvBuilder(configuration, mode).apply(xyp);
      }
      if (p instanceof UnivariateGridPlot ugp) {
        return new UnivariateGridPlotCsvBuilder(configuration, mode).apply(ugp);
      }
      if (p instanceof VectorialFieldPlot vfp) {
        return new VectorialFieldPlotCsvBuilder(configuration, mode).apply(vfp);
      }
      throw new IllegalArgumentException(
          "Unsupported type of plot %s".formatted(p.getClass().getSimpleName())
      );
    };
    return NamedFunction.from(f, "csv.plotter").compose(beforeF);
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <X> FormattedNamedFunction<X, Double> cumulativeFidelity(
      @Param(value = "of", dNPM = "f.identity()") Function<X, POCPopulationState<?, ?, ?, ?, ?>> beforeF,
      @Param(value = "format", dS = "%8.1f") String format
  ) {
    Function<POCPopulationState<?, ?, ?, ?, ?>, Double> f = s -> {
      if (s instanceof MultiFidelityPOCPopulationState<?, ?, ?, ?, ?> mfS) {
        return mfS.cumulativeFidelity();
      }
      return (double) s.nOfQualityEvaluations();
    };
    return FormattedNamedFunction.from(f, format, "cumulative.fidelity").compose(beforeF);
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <X, G, S, Q> FormattedNamedFunction<X, Integer> descBin(
      @Param("descriptor") MapElites.Descriptor<G, S, Q> descriptor,
      @Param(value = "of", dNPM = "f.identity()") Function<X, Individual<G, S, Q>> beforeF,
      @Param(value = "format", dS = "%2d") String format
  ) {
    Function<Individual<G, S, Q>, Integer> f = i -> descriptor.coordinate(i).bin();
    return FormattedNamedFunction.from(
        f,
        format,
        "bin[%s]".formatted(NamedFunction.name(descriptor.function()))
    )
        .compose(beforeF);
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <X> FormattedNamedFunction<X, Double> elapsedSecs(
      @Param(value = "of", dNPM = "f.identity()") Function<X, State<?, ?>> beforeF,
      @Param(value = "format", dS = "%6.1f") String format
  ) {
    Function<State<?, ?>, Double> f = s -> s.elapsedMillis() / 1000d;
    return FormattedNamedFunction.from(f, format, "elapsed.secs").compose(beforeF);
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <X, I extends Individual<G, S, Q>, G, S, Q> NamedFunction<X, Collection<I>> firsts(
      @Param(value = "of", dNPM = "f.identity()") Function<X, POCPopulationState<I, G, S, Q, ?>> beforeF
  ) {
    Function<POCPopulationState<I, G, S, Q, ?>, Collection<I>> f = state -> state.pocPopulation()
        .firsts();
    return NamedFunction.from(f, "firsts").compose(beforeF);
  }

  @SuppressWarnings("unused")
  public static <X, G, S> FormattedNamedFunction<X, S> fromMapper(
      @Param(value = "of", dNPM = "f.identity()") Function<X, G> beforeF,
      @Param("mapper") InvertibleMapper<G, S> mapper,
      @Param("example") S example,
      @Param(value = "name", iS = "{mapper.name}") String name,
      @Param(value = "format", dS = "%s") String format
  ) {
    NamedFunction<G, S> f = NamedFunction.from(mapper.mapperFor(example));
    return FormattedNamedFunction.from(f, format, name).compose(beforeF);
  }

  @SuppressWarnings("unused")
  public static <X, S, Q> FormattedNamedFunction<X, Q> fromProblem(
      @Param(value = "of", dNPM = "f.identity()") Function<X, S> beforeF,
      @Param("problem") QualityBasedProblem<S, Q> problem,
      @Param(value = "name", iS = "{problem.name}") String name,
      @Param(value = "format", dS = "%s") String format
  ) {
    return FormattedNamedFunction.from(problem, format, name).compose(beforeF);
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <X, G> FormattedNamedFunction<X, G> genotype(
      @Param(value = "of", dNPM = "f.identity()") Function<X, Individual<G, ?, ?>> beforeF,
      @Param(value = "format", dS = "%s") String format
  ) {
    Function<Individual<G, ?, ?>, G> f = Individual::genotype;
    return FormattedNamedFunction.from(f, format, "genotype").compose(beforeF);
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <X> FormattedNamedFunction<X, TextPlotter.Miniplot> hist(
      @Param(value = "nOfBins", dI = 8) int nOfBins,
      @Param(value = "of", dNPM = "f.identity()") Function<X, Collection<Number>> beforeF
  ) {
    Function<Collection<Number>, TextPlotter.Miniplot> f = vs -> TextPlotter.histogram(
        vs.stream().toList(),
        nOfBins
    );
    return FormattedNamedFunction.from(f, "%" + nOfBins + "s", "hist").compose(beforeF);
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <X> FormattedNamedFunction<X, Double> hypervolume2D(
      @Param("minReference") List<Double> minReference,
      @Param("maxReference") List<Double> maxReference,
      @Param(value = "of", dNPM = "f.identity()") Function<X, Collection<List<Double>>> beforeF,
      @Param(value = "format", dS = "%.2f") String format
  ) {
    Function<Collection<List<Double>>, Double> f = ps -> Misc.hypervolume2D(
        ps,
        minReference,
        maxReference
    );
    return FormattedNamedFunction.from(f, format, "hv").compose(beforeF);
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <X> FormattedNamedFunction<X, Long> id(
      @Param(value = "of", dNPM = "f.identity()") Function<X, Individual<?, ?, ?>> beforeF,
      @Param(value = "format", dS = "%6d") String format
  ) {
    Function<Individual<?, ?, ?>, Long> f = Individual::id;
    return FormattedNamedFunction.from(f, format, "id").compose(beforeF);
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <X, P extends XYPlot<D>, D> NamedFunction<X, Object> imagePlotter(
      @Param(value = "of", dNPM = "f.identity()") Function<X, P> beforeF,
      @Param(value = "w", dI = -1) int w,
      @Param(value = "h", dI = -1) int h,
      @Param(value = "configuration", dNPM = "ea.plot.configuration.image()") Configuration configuration,
      @Param("secondary") boolean secondary,
      @Param(value = "type", dS = "png") String type
  ) {
    UnaryOperator<Drawer.ImageInfo> iiAdapter = ii -> new Drawer.ImageInfo(
        w == -1 ? ii.w() : w,
        h == -1 ? ii.h() : h
    );
    class ConditionedDrawer<Y> implements BiFunction<Drawer<Y>, Y, Object> {

      @Override
      public Object apply(Drawer<Y> drawer, Y y) {
        return switch (type.toLowerCase()) {
          case "png" -> drawer.buildRaster(iiAdapter.apply(drawer.imageInfo(y)), y);
          case "svg" -> drawer.buildVectorial(iiAdapter.apply(drawer.imageInfo(y)), y);
          default -> throw new IllegalArgumentException(
              "Invalid type '%s', which is not 'png' nor 'svg'".formatted(type)
          );
        };
      }
    }
    Function<P, Object> f = p -> {
      if (p instanceof DistributionPlot dp) {
        return new ConditionedDrawer<DistributionPlot>().apply(
            new BoxPlotDrawer(configuration),
            dp
        );
      }
      if (p instanceof LandscapePlot lsp) {
        return new ConditionedDrawer<LandscapePlot>().apply(
            new LandscapePlotDrawer(configuration),
            lsp
        );
      }
      if (p instanceof XYDataSeriesPlot xyp) {
        if (secondary) {
          return new ConditionedDrawer<XYDataSeriesPlot>().apply(
              new PointsPlotDrawer(configuration),
              xyp
          );
        }
        return new ConditionedDrawer<XYDataSeriesPlot>().apply(
            new LinesPlotDrawer(configuration),
            xyp
        );
      }
      if (p instanceof UnivariateGridPlot ugp) {
        return new ConditionedDrawer<UnivariateGridPlot>().apply(
            new UnivariateGridPlotDrawer(configuration),
            ugp
        );
      }
      if (p instanceof VectorialFieldPlot vfp) {
        return new ConditionedDrawer<VectorialFieldPlot>().apply(
            new VectorialFieldPlotDrawer(configuration),
            vfp
        );
      }
      throw new IllegalArgumentException(
          "Unsupported type of plot %s".formatted(p.getClass().getSimpleName())
      );
    };
    return NamedFunction.from(f, "image.plotter").compose(beforeF);
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <X> FormattedNamedFunction<X, Double> isCrossRedundancy(
      @Param(value = "of", dNPM = "f.identity()") Function<X, IntString> beforeF,
      @Param(value = "startOffset", dI = 0) int startOffset,
      @Param(value = "endOffset", dI = 0) int endOffset,
      @Param(value = "splitOffset", dI = 0) int splitOffset,
      @Param(value = "format", dS = "%5.3f") String format
  ) {
    Function<IntString, Double> f = is -> {
      List<Integer> indexes = is.genes().subList(startOffset, is.genes().size() - endOffset);
      List<Integer> leftIndexes = indexes.subList(0, splitOffset);
      List<Integer> rightIndexes = indexes.subList(splitOffset, indexes.size());
      Set<Integer> commonIndexes = new HashSet<>(leftIndexes);
      commonIndexes.retainAll(rightIndexes);
      double leftRate = (double) commonIndexes.size() / (double) leftIndexes.size();
      double rightRate = (double) commonIndexes.size() / (double) rightIndexes.size();
      return (leftRate + rightRate) / 2d;
    };
    return FormattedNamedFunction.from(
        f,
        format,
        "is.cross.redundancy[%d;%d;%d]".formatted(startOffset, endOffset, splitOffset)
    ).compose(beforeF);
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <X> FormattedNamedFunction<X, Double> isRedundancy(
      @Param(value = "of", dNPM = "f.identity()") Function<X, IntString> beforeF,
      @Param(value = "startOffset", dI = 0) int startOffset,
      @Param(value = "endOffset", dI = 0) int endOffset,
      @Param(value = "format", dS = "%5.3f") String format
  ) {
    Function<IntString, Double> f = is -> 1d - (double) is.genes()
        .subList(startOffset, is.genes().size() - endOffset)
        .stream()
        .distinct()
        .count() / (double) (is.size() - startOffset - endOffset);
    return FormattedNamedFunction.from(
        f,
        format,
        "is.redundancy[%d;%d]".formatted(startOffset, endOffset)
    )
        .compose(
            beforeF
        );
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <X, I extends Individual<G, S, Q>, G, S, Q> NamedFunction<X, Collection<I>> lasts(
      @Param(value = "of", dNPM = "f.identity()") Function<X, POCPopulationState<I, G, S, Q, ?>> beforeF
  ) {
    Function<POCPopulationState<I, G, S, Q, ?>, Collection<I>> f = state -> state.pocPopulation()
        .lasts();
    return NamedFunction.from(f, "lasts").compose(beforeF);
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <X, G, S, Q> NamedFunction<X, Archive<? extends MEIndividual<G, S, Q>>> maMeArchive(
      @Param(value = "of", dNPM = "f.identity()") Function<X, MAMEPopulationState<G, S, Q, ?>> beforeF,
      @Param("n") int n
  ) {
    Function<MAMEPopulationState<G, S, Q, ?>, Archive<? extends MEIndividual<G, S, Q>>> f = s -> s.archives()
        .get(n);
    return NamedFunction.from(f, "archive[%d]".formatted(n)).compose(beforeF);
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <X, G, S, Q> NamedFunction<X, Archive<MEIndividual<G, S, Q>>> meArchive(
      @Param(value = "of", dNPM = "f.identity()") Function<X, MEPopulationState<G, S, Q, ?>> beforeF
  ) {
    Function<MEPopulationState<G, S, Q, ?>, Archive<MEIndividual<G, S, Q>>> f = MEPopulationState::archive;
    return NamedFunction.from(f, "archive").compose(beforeF);
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <X> FormattedNamedFunction<X, Integer> meBin(
      @Param(value = "of", dNPM = "f.identity()") Function<X, MapElites.Descriptor.Coordinate> beforeF,
      @Param(value = "format", dS = "%3d") String format
  ) {
    Function<MapElites.Descriptor.Coordinate, Integer> f = MapElites.Descriptor.Coordinate::bin;
    return FormattedNamedFunction.from(f, format, "bin").compose(beforeF);
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <X> FormattedNamedFunction<X, List<MapElites.Descriptor.Coordinate>> meCoordinates(
      @Param(value = "of", dNPM = "f.identity()") Function<X, MEIndividual<?, ?, ?>> beforeF,
      @Param(value = "format", dS = "%s") String format
  ) {
    Function<MEIndividual<?, ?, ?>, List<MapElites.Descriptor.Coordinate>> f = MEIndividual::coordinates;
    return FormattedNamedFunction.from(f, format, "coords").compose(beforeF);
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <X> FormattedNamedFunction<X, Double> meValue(
      @Param(value = "of", dNPM = "f.identity()") Function<X, MapElites.Descriptor.Coordinate> beforeF,
      @Param(value = "format", dS = "%.2f") String format
  ) {
    Function<MapElites.Descriptor.Coordinate, Double> f = MapElites.Descriptor.Coordinate::value;
    return FormattedNamedFunction.from(f, format, "value").compose(beforeF);
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <X, G, S, Q> NamedFunction<X, Archive<MultiFidelityMEPopulationState.LocalState>> mfMeFidelityArchive(
      @Param(value = "of", dNPM = "f.identity()") Function<X, MultiFidelityMEPopulationState<G, S, Q, ?>> beforeF
  ) {
    Function<MultiFidelityMEPopulationState<G, S, Q, ?>, Archive<MultiFidelityMEPopulationState.LocalState>> f = MultiFidelityMEPopulationState::fidelityArchive;
    return NamedFunction.from(f, "fidelity.archive").compose(beforeF);
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <X> FormattedNamedFunction<X, Double> mfMeLsCumulativeFidelity(
      @Param(value = "of", dNPM = "f.identity()") Function<X, MultiFidelityMEPopulationState.LocalState> beforeF,
      @Param(value = "format", dS = "%5.3f") String format
  ) {
    Function<MultiFidelityMEPopulationState.LocalState, Double> f = MultiFidelityMEPopulationState.LocalState::cumulativeFidelity;
    return FormattedNamedFunction.from(f, format, "cumulative.fidelity").compose(beforeF);
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <X> FormattedNamedFunction<X, Double> mfMeLsFidelity(
      @Param(value = "of", dNPM = "f.identity()") Function<X, MultiFidelityMEPopulationState.LocalState> beforeF,
      @Param(value = "format", dS = "%5.3f") String format
  ) {
    Function<MultiFidelityMEPopulationState.LocalState, Double> f = MultiFidelityMEPopulationState.LocalState::fidelity;
    return FormattedNamedFunction.from(f, format, "fidelity").compose(beforeF);
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <X> FormattedNamedFunction<X, Long> mfMeLsNOfEvals(
      @Param(value = "of", dNPM = "f.identity()") Function<X, MultiFidelityMEPopulationState.LocalState> beforeF,
      @Param(value = "format", dS = "%4d") String format
  ) {
    Function<MultiFidelityMEPopulationState.LocalState, Long> f = MultiFidelityMEPopulationState.LocalState::nOfQualityEvaluations;
    return FormattedNamedFunction.from(f, format, "n.evals").compose(beforeF);
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <X, I extends Individual<G, S, Q>, G, S, Q> NamedFunction<X, Collection<I>> mids(
      @Param(value = "of", dNPM = "f.identity()") Function<X, POCPopulationState<I, G, S, Q, ?>> beforeF
  ) {
    Function<POCPopulationState<I, G, S, Q, ?>, Collection<I>> f = state -> state.pocPopulation()
        .mids();
    return NamedFunction.from(f, "mids").compose(beforeF);
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <X> FormattedNamedFunction<X, Long> nOfBirths(
      @Param(value = "of", dNPM = "f.identity()") Function<X, POCPopulationState<?, ?, ?, ?, ?>> beforeF,
      @Param(value = "format", dS = "%5d") String format
  ) {
    Function<POCPopulationState<?, ?, ?, ?, ?>, Long> f = POCPopulationState::nOfBirths;
    return FormattedNamedFunction.from(f, format, "n.births").compose(beforeF);
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <X> FormattedNamedFunction<X, Long> nOfEvals(
      @Param(value = "of", dNPM = "f.identity()") Function<X, POCPopulationState<?, ?, ?, ?, ?>> beforeF,
      @Param(value = "format", dS = "%5d") String format
  ) {
    Function<POCPopulationState<?, ?, ?, ?, ?>, Long> f = POCPopulationState::nOfQualityEvaluations;
    return FormattedNamedFunction.from(f, format, "n.evals").compose(beforeF);
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <X> FormattedNamedFunction<X, Long> nOfIterations(
      @Param(value = "of", dNPM = "f.identity()") Function<X, State<?, ?>> beforeF,
      @Param(value = "format", dS = "%4d") String format
  ) {
    Function<State<?, ?>, Long> f = State::nOfIterations;
    return FormattedNamedFunction.from(f, format, "n.iterations").compose(beforeF);
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <X, P extends MultiTargetProblem<S>, S> FormattedNamedFunction<X, Double> overallTargetDistance(
      @Param(value = "of", dNPM = "f.identity()") Function<X, POCPopulationState<?, ?, S, ?, P>> beforeF,
      @Param(value = "format", dS = "%.2f") String format
  ) {
    Function<POCPopulationState<?, ?, S, ?, P>, Double> f = state -> state.problem()
        .targets()
        .stream()
        .mapToDouble(
            ts -> state.pocPopulation()
                .all()
                .stream()
                .mapToDouble(s -> state.problem().distance().apply(s.solution(), ts))
                .min()
                .orElseThrow()
        )
        .average()
        .orElseThrow();
    return FormattedNamedFunction.from(f, format, "overall.target.distance").compose(beforeF);
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <X> FormattedNamedFunction<X, Collection<Long>> parentIds(
      @Param(value = "of", dNPM = "f.identity()") Function<X, Individual<?, ?, ?>> beforeF,
      @Param(value = "format", dS = "%s") String format
  ) {
    Function<Individual<?, ?, ?>, Collection<Long>> f = Individual::parentIds;
    return FormattedNamedFunction.from(f, format, "parent.ids").compose(beforeF);
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <X, P extends MultiTargetProblem<S>, S> FormattedNamedFunction<X, List<Double>> popTargetDistances(
      @Param(value = "of", dNPM = "f.identity()") Function<X, POCPopulationState<?, ?, S, ?, P>> beforeF,
      @Param(value = "format", dS = "%.2f") String format
  ) {
    Function<POCPopulationState<?, ?, S, ?, P>, List<Double>> f = state -> state.problem()
        .targets()
        .stream()
        .mapToDouble(
            ts -> state.pocPopulation()
                .all()
                .stream()
                .mapToDouble(s -> state.problem().distance().apply(s.solution(), ts))
                .min()
                .orElseThrow()
        )
        .boxed()
        .toList();
    return FormattedNamedFunction.from(f, format, "pop.target.distances").compose(beforeF);
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <X, P extends Problem<S>, S> NamedFunction<X, P> problem(
      @Param(value = "of", dNPM = "f.identity()") Function<X, State<P, S>> beforeF
  ) {
    Function<State<P, S>, P> f = State::problem;
    return NamedFunction.from(f, "problem").compose(beforeF);
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <X> NamedFunction<X, Progress> progress(
      @Param(value = "of", dNPM = "f.identity()") Function<X, State<?, ?>> beforeF
  ) {
    Function<State<?, ?>, Progress> f = State::progress;
    return NamedFunction.from(f, "progress").compose(beforeF);
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <X, Q> FormattedNamedFunction<X, Q> quality(
      @Param(value = "of", dNPM = "f.identity()") Function<X, Individual<?, ?, Q>> beforeF,
      @Param(value = "format", dS = "%s") String format
  ) {
    Function<Individual<?, ?, Q>, Q> f = Individual::quality;
    return FormattedNamedFunction.from(f, format, "quality").compose(beforeF);
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <X, Q> FormattedNamedFunction<X, PartialComparator<Q>> qualityComparator(
      @Param(value = "of", dNPM = "f.identity()") Function<X, QualityBasedProblem<?, Q>> beforeF,
      @Param(value = "format", dS = "%s") String format
  ) {
    Function<QualityBasedProblem<?, Q>, PartialComparator<Q>> f = QualityBasedProblem::qualityComparator;
    return FormattedNamedFunction.from(f, format, "quality.comparator").compose(beforeF);
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <X, S, Q, P extends QualityBasedProblem<S, Q>> NamedFunction<X, Function<S, Q>> qualityFunction(
      @Param(value = "of", dNPM = "f.identity()") Function<X, P> beforeF
  ) {
    Function<P, Function<S, Q>> f = QualityBasedProblem::qualityFunction;
    return NamedFunction.from(f, "quality").compose(beforeF);
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <X> NamedFunction<X, Double> rate(
      @Param(value = "of", dNPM = "f.identity()") Function<X, Progress> beforeF
  ) {
    Function<Progress, Double> f = Progress::rate;
    return NamedFunction.from(f, "rate").compose(beforeF);
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <X> FormattedNamedFunction<X, String> runKey(
      @Param(value = "name", iS = "{key}") String name,
      @Param("key") String key,
      @Param(value = "of", dNPM = "f.identity()") Function<X, Run<?, ?, ?, ?>> beforeF,
      @Param(value = "format", dS = "%s") String format
  ) {
    Function<Run<?, ?, ?, ?>, String> f = run -> Utils.interpolate(
        "{%s}".formatted(key),
        null,
        run
    );
    return FormattedNamedFunction.from(f, format, name).compose(beforeF);
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <X> FormattedNamedFunction<X, String> runString(
      @Param(value = "name", iS = "{s}") String name,
      @Param("s") String s,
      @Param(value = "of", dNPM = "f.identity()") Function<X, Run<?, ?, ?, ?>> beforeF,
      @Param(value = "format", dS = "%s") String format,
      @Param(value = "regex", dS = "") String regex
  ) {
    Function<Run<?, ?, ?, ?>, String> f = run -> Utils.interpolate(s, null, run);
    Function<Run<?, ?, ?, ?>, String> fr = f;
    if (!regex.isEmpty()) {
      final java.util.regex.Pattern p = java.util.regex.Pattern.compile(regex);
      fr = run -> {
        String raw = f.apply(run);
        java.util.regex.Matcher m = p.matcher(raw);
        if (m.find() && m.groupCount() >= 1) {
          String g = m.group(1);
          return g == null ? raw : g;
        }
        return raw;
      };
    }
    return FormattedNamedFunction.from(fr, format, name).compose(beforeF);
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <X> FormattedNamedFunction<X, Integer> size(
      @Param(value = "of", dNPM = "f.identity()") Function<X, Object> beforeF,
      @Param(value = "format", dS = "%d") String format
  ) {
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
          "Cannot compute size of %s".formatted(o.getClass().getSimpleName())
      );
    };
    return FormattedNamedFunction.from(f, format, "size").compose(beforeF);
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <X, S> FormattedNamedFunction<X, S> solution(
      @Param(value = "of", dNPM = "f.identity()") Function<X, Individual<?, S, ?>> beforeF,
      @Param(value = "format", dS = "%s") String format
  ) {
    Function<Individual<?, S, ?>, S> f = Individual::solution;
    return FormattedNamedFunction.from(f, format, "solution").compose(beforeF);
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <X, G, S, Q> NamedFunction<X, Grid<Individual<G, S, Q>>> stateGrid(
      @Param(value = "of", dNPM = "f.identity()") Function<X, GridPopulationState<G, S, Q, ?>> beforeF
  ) {
    Function<GridPopulationState<G, S, Q, ?>, Grid<Individual<G, S, Q>>> f = GridPopulationState::gridPopulation;
    return NamedFunction.from(f, "grid").compose(beforeF);
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <X, Z> NamedFunction<X, Z> supplied(
      @Param(value = "of", dNPM = "f.identity()") Function<X, Supplier<Z>> beforeF
  ) {
    Function<Supplier<Z>, Z> f = Supplier::get;
    return NamedFunction.from(f, "supplied").compose(beforeF);
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <X, P extends MultiTargetProblem<S>, S> FormattedNamedFunction<X, List<Double>> targetDistances(
      @Param("problem") P problem,
      @Param(value = "of", dNPM = "f.identity()") Function<X, Individual<?, S, ?>> beforeF,
      @Param(value = "format", dS = "%.2f") String format
  ) {
    Function<Individual<?, S, ?>, List<Double>> f = i -> problem.targets()
        .stream()
        .map(t -> problem.distance().apply(i.solution(), t))
        .toList();
    return FormattedNamedFunction.from(f, format, "target.distances").compose(beforeF);
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <X> FormattedNamedFunction<X, LocalDateTime> timestamp(
      @Param(value = "format", dS = "%1$tH:%1$tM:%1$tS") String format
  ) {
    return FormattedNamedFunction.from(x -> LocalDateTime.now(), format, "timestamp");
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <X, Z> NamedFunction<X, List<Double>> toDoubleString(
      @Param(value = "of", dNPM = "f.identity()") Function<X, Z> beforeF
  ) {
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
              throw new IllegalArgumentException(
                  "Cannot convert %s to double"
                      .formatted(i.getClass().getSimpleName())
              );
            })
            .toList();
      }
      throw new IllegalArgumentException(
          "Cannot convert %s to double string".formatted(z.getClass().getSimpleName())
      );
    };
    return NamedFunction.from(f, "to.double.string").compose(beforeF);
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <X, D> NamedFunction<X, Object> toImage(
      @Param(value = "of", dNPM = "f.identity()") Function<X, D> beforeF,
      @Param("drawer") Drawer<D> drawer,
      @Param(value = "w", dI = -1) int w,
      @Param(value = "h", dI = -1) int h,
      @Param(value = "type", dS = "png") String type
  ) {
    UnaryOperator<Drawer.ImageInfo> iiAdapter = ii -> new Drawer.ImageInfo(
        w == -1 ? ii.w() : w,
        h == -1 ? ii.h() : h
    );
    Function<D, Object> f = d -> switch (type.toLowerCase()) {
      case "png" -> drawer.buildRaster(iiAdapter.apply(drawer.imageInfo(d)), d);
      case "svg" -> drawer.buildVectorial(iiAdapter.apply(drawer.imageInfo(d)), d);
      default -> throw new IllegalArgumentException(
          "Invalid type '%s', which is not 'png' nor 'svg'".formatted(type)
      );
    };
    return NamedFunction.from(f, "to.image[%s]".formatted(drawer)).compose(beforeF);
  }

  public static <X, D> NamedFunction<X, Object> toMultiImage(
      @Param(value = "of", dNPM = "f.identity()") Function<X, List<D>> beforeF,
      @Param("drawer") Drawer<D> drawer,
      @Param(value = "w", dI = -1) int w,
      @Param(value = "h", dI = -1) int h,
      @Param(value = "type", dS = "png") String type,
      @Param(value = "arrangement", dS = "horizontal") Drawer.Arrangement arrangement
  ) {
    UnaryOperator<Drawer.ImageInfo> iiAdapter = ii -> new Drawer.ImageInfo(
        w == -1 ? ii.w() : w,
        h == -1 ? ii.h() : h
    );
    Drawer<List<D>> multiDrawer = drawer.multi(arrangement);
    Function<List<D>, Object> f = ds -> switch (type.toLowerCase()) {
      case "png" -> multiDrawer.buildRaster(iiAdapter.apply(multiDrawer.imageInfo(ds)), ds);
      case "svg" -> multiDrawer.buildVectorial(iiAdapter.apply(multiDrawer.imageInfo(ds)), ds);
      default -> throw new IllegalArgumentException(
          "Invalid type '%s', which is not 'png' nor 'svg'".formatted(type)
      );
    };
    return NamedFunction.from(f, "to.image[%s]".formatted(drawer)).compose(beforeF);
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <X, D> NamedFunction<X, Video> toImagesVideo(
      @Param(value = "of", dNPM = "f.identity()") Function<X, List<D>> beforeF,
      @Param("drawer") Drawer<D> drawer,
      @Param(value = "w", dI = -1) int w,
      @Param(value = "h", dI = -1) int h,
      @Param(value = "frameRate", dD = 10) double frameRate,
      @Param(value = "encoder", dS = "default") VideoUtils.EncoderFacility encoder
  ) {
    UnaryOperator<VideoBuilder.VideoInfo> viAdapter = vi -> new VideoBuilder.VideoInfo(
        w == -1 ? vi.w() : w,
        h == -1 ? vi.h() : h,
        encoder
    );
    VideoBuilder<List<D>> videoBuilder = VideoBuilder.from(
        drawer,
        Function.identity(),
        frameRate
    );
    Function<List<D>, Video> f = ds -> {
      if (w == -1 && h == -1) {
        return videoBuilder.apply(ds);
      }
      return videoBuilder.build(viAdapter.apply(videoBuilder.videoInfo(ds)), ds);
    };
    return NamedFunction.from(f, "to.images.video[%s]".formatted(drawer))
        .compose(beforeF);
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <X, D> NamedFunction<X, Video> toVideo(
      @Param(value = "of", dNPM = "f.identity()") Function<X, D> beforeF,
      @Param("video") VideoBuilder<D> videoBuilder,
      @Param(value = "w", dI = -1) int w,
      @Param(value = "h", dI = -1) int h,
      @Param(value = "encoder", dS = "default") VideoUtils.EncoderFacility encoder
  ) {
    UnaryOperator<VideoBuilder.VideoInfo> viAdapter = vi -> new VideoBuilder.VideoInfo(
        w == -1 ? vi.w() : w,
        h == -1 ? vi.h() : h,
        encoder
    );
    Function<D, Video> f = d -> {
      if (w == -1 && h == -1) {
        return videoBuilder.apply(d);
      }
      return videoBuilder.build(viAdapter.apply(videoBuilder.videoInfo(d)), d);
    };
    return NamedFunction.from(f, "to.video[%s]".formatted(videoBuilder)).compose(beforeF);
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <X, C> FormattedNamedFunction<X, Integer> treeDepth(
      @Param(value = "of", dNPM = "f.identity()") Function<X, Tree<C>> beforeF,
      @Param(value = "format", dS = "%3d") String format
  ) {
    Function<Tree<C>, Integer> f = Tree::depth;
    return FormattedNamedFunction.from(f, format, "tree.depth").compose(beforeF);
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <X, C> FormattedNamedFunction<X, Collection<C>> treeLabels(
      @Param(value = "of", dNPM = "f.identity()") Function<X, Tree<C>> beforeF,
      @Param(value = "format", dS = "%s") String format
  ) {
    Function<Tree<C>, Collection<C>> f = Tree::visitDepth;
    return FormattedNamedFunction.from(f, format, "tree.labels").compose(beforeF);
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <X, C> FormattedNamedFunction<X, Collection<C>> treeLeaves(
      @Param(value = "of", dNPM = "f.identity()") Function<X, Tree<C>> beforeF,
      @Param(value = "format", dS = "%s") String format
  ) {
    Function<Tree<C>, Collection<C>> f = Tree::visitLeaves;
    return FormattedNamedFunction.from(f, format, "tree.leaves").compose(beforeF);
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <X, C> FormattedNamedFunction<X, Integer> treeSize(
      @Param(value = "of", dNPM = "f.identity()") Function<X, Tree<C>> beforeF,
      @Param(value = "format", dS = "%3d") String format
  ) {
    Function<Tree<C>, Integer> f = Tree::size;
    return FormattedNamedFunction.from(f, format, "tree.size").compose(beforeF);
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <X> FormattedNamedFunction<X, Double> ttpnDeadGatesRate(
      @Param(value = "of", dNPM = "f.identity()") Function<X, Network> beforeF,
      @Param(value = "format", dS = "%5.3f") String format
  ) {
    Function<Network, Double> f = n -> (double) n.deadGates().size() / (double) n.gates().size();
    return FormattedNamedFunction.from(f, format, "ttpn.dead.gates.rate").compose(beforeF);
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <X> FormattedNamedFunction<X, Double> ttpnDeadOrIUnwiredOutputGatesRate(
      @Param(value = "of", dNPM = "f.identity()") Function<X, Network> beforeF,
      @Param(value = "format", dS = "%5.3f") String format
  ) {
    Function<Network, Double> f = n -> (double) n.deadOrIUnwiredOutputGates().size() / (double) n.gates().size();
    return FormattedNamedFunction.from(f, format, "ttpn.deadOrIUnwired.output.gates.rate")
        .compose(beforeF);
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <X> FormattedNamedFunction<X, Integer> ttpnNOfTypes(
      @Param(value = "of", dNPM = "f.identity()") Function<X, Network> beforeF,
      @Param(value = "format", dS = "%5.3f") String format
  ) {
    Function<Network, Integer> f = n -> (int) n.wires()
        .stream()
        .map(w -> n.concreteOutputType(w.src()))
        .distinct()
        .count();
    return FormattedNamedFunction.from(f, format, "ttpn.n.types").compose(beforeF);
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <X, I extends Individual<?, S, Q>, S, Q, P extends QualityBasedProblem<S, Q>> FormattedNamedFunction<X, Q> validationQuality(
      @Param(value = "of", dNPM = "f.identity()") Function<X, POCPopulationState<?, ?, S, Q, P>> beforeF,
      @Param(value = "individual", dNPM = "ea.f.best()") Function<POCPopulationState<?, ?, S, Q, P>, Individual<?, S, Q>> individualF,
      @Param(value = "format", dS = "%s") String format
  ) {
    Function<POCPopulationState<?, ?, S, Q, P>, Q> f = state -> state.problem()
        .validationQualityFunction()
        .apply(individualF.apply(state).solution());
    return FormattedNamedFunction.from(
        f,
        format,
        NamedFunction.composeNames(NamedFunction.name(individualF), "validation.quality")
    )
        .compose(beforeF);
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <X, S, Q, P extends QualityBasedProblem<S, Q>> NamedFunction<X, Function<S, Q>> validationQualityFunction(
      @Param(value = "of", dNPM = "f.identity()") Function<X, P> beforeF
  ) {
    Function<P, Function<S, Q>> f = QualityBasedProblem::validationQualityFunction;
    return NamedFunction.from(f, "validation.quality").compose(beforeF);
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <X, P extends XYPlot<D>, D> NamedFunction<X, Video> videoPlotter(
      @Param(value = "of", dNPM = "f.identity()") Function<X, P> beforeF,
      @Param(value = "w", dI = -1) int w,
      @Param(value = "h", dI = -1) int h,
      @Param(value = "encoder", dS = "default") VideoUtils.EncoderFacility encoder,
      @Param(value = "frameRate", dD = 10) double frameRate,
      @Param(value = "configuration", dNPM = "ea.plot.configuration.image()") Configuration iConfiguration,
      @Param("secondary") boolean secondary
  ) {
    UnaryOperator<VideoBuilder.VideoInfo> viAdapter = vi -> new VideoBuilder.VideoInfo(
        w == -1 ? vi.w() : w,
        h == -1 ? vi.h() : h,
        encoder
    );
    io.github.ericmedvet.jviz.core.plot.video.Configuration vConfiguration = new io.github.ericmedvet.jviz.core.plot.video.Configuration(
        io.github.ericmedvet.jviz.core.plot.video.Configuration.DEFAULT.splitType(),
        frameRate
    );
    Function<P, Video> f = p -> {
      if (p instanceof DistributionPlot dp) {
        BoxPlotVideoBuilder vb = new BoxPlotVideoBuilder(
            vConfiguration,
            iConfiguration
        );
        return vb.build(viAdapter.apply(vb.videoInfo(dp)), dp);
      }
      if (p instanceof LandscapePlot lsp) {
        LandscapePlotVideoBuilder vb = new LandscapePlotVideoBuilder(
            vConfiguration,
            iConfiguration
        );
        return vb.build(viAdapter.apply(vb.videoInfo(lsp)), lsp);
      }
      if (p instanceof XYDataSeriesPlot xyp) {
        AbstractXYDataSeriesPlotVideoBuilder vb = (!secondary) ? new LinesPlotVideoBuilder(
            vConfiguration,
            iConfiguration
        ) : new PointsPlotVideoBuilder(
            vConfiguration,
            iConfiguration
        );
        return vb.build(viAdapter.apply(vb.videoInfo(xyp)), xyp);
      }
      if (p instanceof UnivariateGridPlot ugp) {
        UnivariatePlotVideoBuilder vb = new UnivariatePlotVideoBuilder(
            vConfiguration,
            iConfiguration
        );
        return vb.build(viAdapter.apply(vb.videoInfo(ugp)), ugp);
      }
      if (p instanceof VectorialFieldPlot vfp) {
        VectorialFieldVideoBuilder vb = new VectorialFieldVideoBuilder(
            vConfiguration,
            iConfiguration
        );
        return vb.build(viAdapter.apply(vb.videoInfo(vfp)), vfp);
      }
      throw new IllegalArgumentException(
          "Unsupported type of plot %s".formatted(p.getClass().getSimpleName())
      );
    };
    return NamedFunction.from(f, "video.plotter").compose(beforeF);
  }
}
