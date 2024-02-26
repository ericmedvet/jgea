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

package io.github.ericmedvet.jgea.core.representation.grammar.grid;

import io.github.ericmedvet.jgea.core.IndependentFactory;
import io.github.ericmedvet.jgea.core.distance.*;
import io.github.ericmedvet.jgea.core.representation.grammar.Chooser;
import io.github.ericmedvet.jgea.core.representation.grammar.Developer;
import io.github.ericmedvet.jgea.core.representation.grammar.GrammarOptionStringFactory;
import io.github.ericmedvet.jgea.core.representation.sequence.FixedLengthListFactory;
import io.github.ericmedvet.jgea.core.representation.sequence.bit.BitStringFactory;
import io.github.ericmedvet.jgea.core.representation.sequence.integer.UniformIntStringFactory;
import io.github.ericmedvet.jgea.core.representation.sequence.numeric.UniformDoubleFactory;
import io.github.ericmedvet.jnb.datastructure.Grid;
import io.github.ericmedvet.jnb.datastructure.GridUtils;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.ToDoubleBiFunction;
import java.util.function.ToDoubleFunction;
import java.util.random.RandomGenerator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;

public class GridBiasesAndProps {
  record FactoryChooser<X>(
      IndependentFactory<X> factory,
      Function<X, Chooser<String, GridGrammar.ReferencedGrid<String>>> chooserBuilder,
      Distance<X> distance) {
    public List<Mapped<X>> build(
        Developer<String, Grid<String>, GridGrammar.ReferencedGrid<String>> developer,
        int n,
        RandomGenerator randomGenerator) {
      return IntStream.range(0, n)
          .mapToObj(i -> {
            X x = factory.build(randomGenerator);
            return new Mapped<>(x, developer.develop(chooserBuilder.apply(x)));
          })
          .toList();
    }
  }

  record Mapped<X>(X genotype, Optional<Grid<String>> phenotype) {}

  public static void main(String[] args) throws IOException {
    Locale.setDefault(Locale.ROOT);
    // one-for-all params
    RandomGenerator rg = new Random(0);
    int n = 10000;
    int localityN = 1000;
    int minL = 10;
    int maxL = 250;
    int stepL = 5;
    PrintStream ps = new PrintStream(args[0]);
    // ps = System.out;
    // to-iterate params
    Map<String, GridGrammar<String>> grammars = Map.ofEntries(
        Map.entry("worm", GridGrammar.load(GridGrammar.class.getResourceAsStream("/grammars/2d/worm.bnf"))),
        Map.entry(
            "bi",
            GridGrammar.load(GridGrammar.class.getResourceAsStream("/grammars/2d/bidirectional.bnf"))),
        Map.entry(
            "mono",
            GridGrammar.load(GridGrammar.class.getResourceAsStream("/grammars/2d/monodirectional.bnf"))),
        Map.entry(
            "alt", GridGrammar.load(GridGrammar.class.getResourceAsStream("/grammars/2d/alternated.bnf"))),
        Map.entry(
            "dog", GridGrammar.load(GridGrammar.class.getResourceAsStream("/grammars/2d/dog-shape.bnf"))));
    Map<String, Function<GridGrammar<String>, Developer<String, Grid<String>, GridGrammar.ReferencedGrid<String>>>>
        developers = Map.ofEntries(
            Map.entry(
                "now-top_left",
                gg -> new StandardGridDeveloper<>(
                    gg,
                    false,
                    List.of(
                        StandardGridDeveloper.SortingCriterion.LOWEST_Y,
                        StandardGridDeveloper.SortingCriterion.LOWEST_X))),
            Map.entry(
                "now-least_recent",
                gg -> new StandardGridDeveloper<>(
                    gg,
                    false,
                    List.of(
                        StandardGridDeveloper.SortingCriterion.LEAST_RECENT,
                        StandardGridDeveloper.SortingCriterion.LOWEST_Y,
                        StandardGridDeveloper.SortingCriterion.LOWEST_X))),
            Map.entry(
                "ow-top_left",
                gg -> new StandardGridDeveloper<>(
                    gg,
                    true,
                    List.of(
                        StandardGridDeveloper.SortingCriterion.LOWEST_Y,
                        StandardGridDeveloper.SortingCriterion.LOWEST_X))),
            Map.entry(
                "ow-least_recent",
                gg -> new StandardGridDeveloper<>(
                    gg,
                    true,
                    List.of(
                        StandardGridDeveloper.SortingCriterion.LEAST_RECENT,
                        StandardGridDeveloper.SortingCriterion.LOWEST_Y,
                        StandardGridDeveloper.SortingCriterion.LOWEST_X))),
            Map.entry(
                "now-most_free_sides",
                gg -> new StandardGridDeveloper<>(
                    gg,
                    false,
                    List.of(
                        StandardGridDeveloper.SortingCriterion.MOST_FREE_SIDE,
                        StandardGridDeveloper.SortingCriterion.LOWEST_Y,
                        StandardGridDeveloper.SortingCriterion.LOWEST_X))),
            Map.entry(
                "ow-most_free_sides",
                gg -> new StandardGridDeveloper<>(
                    gg,
                    true,
                    List.of(
                        StandardGridDeveloper.SortingCriterion.MOST_FREE_SIDE,
                        StandardGridDeveloper.SortingCriterion.LOWEST_Y,
                        StandardGridDeveloper.SortingCriterion.LOWEST_X))));
    Map<String, BiFunction<Integer, GridGrammar<String>, FactoryChooser<?>>> factoryChoosers = Map.ofEntries(
        Map.entry(
            "random",
            (l, gg) ->
                new FactoryChooser<>(irg -> null, x -> new RandomChooser<>(rg, l, gg), (x1, x2) -> 0d)),
        Map.entry(
            "int-4",
            (l, gg) -> new FactoryChooser<>(
                new UniformIntStringFactory(0, 4, l),
                is -> new IntStringChooser<>(is, gg),
                new IntStringHamming())),
        Map.entry(
            "int-16",
            (l, gg) -> new FactoryChooser<>(
                new UniformIntStringFactory(0, 16, l),
                is -> new IntStringChooser<>(is, gg),
                new IntStringHamming())),
        Map.entry(
            "double",
            (l, gg) -> new FactoryChooser<>(
                new FixedLengthListFactory<>(l, new UniformDoubleFactory(0, 1)),
                vs -> new DoublesChooser<>(vs, gg),
                new LNorm(2d))),
        Map.entry(
            "gos-3",
            (l, gg) -> new FactoryChooser<>(
                new GrammarOptionStringFactory<>(gg, l, 3),
                gos -> new GOSChooser<>(gos, gg),
                new GrammarOptionStringDistance<>())),
        Map.entry(
            "bits",
            (l, gg) -> new FactoryChooser<>(
                new BitStringFactory(l),
                bs -> new BitStringChooser<>(bs, gg),
                new BitStringHamming())));
    List<Map.Entry<String, ToDoubleFunction<Grid<String>>>> gridMetrics = List.of(
        Map.entry("w", g -> GridUtils.w(g, Objects::nonNull)),
        Map.entry("h", g -> GridUtils.h(g, Objects::nonNull)),
        Map.entry("count", g -> GridUtils.count(g, Objects::nonNull)),
        Map.entry("compactness", g -> GridUtils.compactness(g, Objects::nonNull)),
        Map.entry("elongation", g -> GridUtils.elongation(g, Objects::nonNull)));
    Distance<Optional<Grid<String>>> gridDistance = (og1, og2) -> {
      if (og1.isEmpty() && og2.isEmpty()) {
        return 0d;
      }
      if (og2.isEmpty()) {
        return (double) GridUtils.count(og1.get(), Objects::nonNull);
      }
      if (og1.isEmpty()) {
        return (double) GridUtils.count(og2.get(), Objects::nonNull);
      }
      return (double) GridUtils.hammingDistance(og1.get(), og2.get(), true);
    };
    List<Map.Entry<String, ToDoubleBiFunction<List<Mapped<?>>, Distance<?>>>> mappingMetrics = List.of(
        Map.entry(
            "invalidity",
            (ms, d) -> (double) ms.stream()
                    .filter(m -> m.phenotype.isEmpty())
                    .count()
                / (double) ms.size()),
        Map.entry(
            "geno-uniqueness",
            (ms, d) -> (double) ms.stream()
                    .map(Mapped::genotype)
                    .distinct()
                    .count()
                / (double) ms.size()),
        Map.entry(
            "pheno-uniqueness",
            (ms, d) -> (double) ms.stream()
                    .map(Mapped::phenotype)
                    .distinct()
                    .count()
                / (double) ms.size()),
        Map.entry("locality", (ms, d) -> {
          double[] gDistances = IntStream.range(0, Math.min(ms.size(), localityN))
              .mapToObj(i -> IntStream.range(i + 1, Math.min(ms.size(), localityN))
                  .mapToObj(j -> (Double) ((Distance) d)
                      .apply(
                          ms.get(i).genotype(),
                          ms.get(j).genotype()))
                  .toList())
              .flatMap(Collection::stream)
              .mapToDouble(v -> v)
              .toArray();
          double[] pDistances = IntStream.range(0, Math.min(ms.size(), localityN))
              .mapToObj(i -> IntStream.range(i + 1, Math.min(ms.size(), localityN))
                  .mapToObj(j -> gridDistance.apply(
                      ms.get(i).phenotype(), ms.get(j).phenotype()))
                  .toList())
              .flatMap(Collection::stream)
              .mapToDouble(v -> v)
              .toArray();
          return new PearsonsCorrelation().correlation(gDistances, pDistances);
        }));
    // iterate
    ps.println("grammar\tdeveloper\tchooser\tl\t"
        + mappingMetrics.stream().map(Map.Entry::getKey).collect(Collectors.joining("\t"))
        + "\t"
        + gridMetrics.stream().map(Map.Entry::getKey).collect(Collectors.joining("\t")));
    ExecutorService executorService =
        Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() - 1);
    for (Map.Entry<String, GridGrammar<String>> grammarEntry : grammars.entrySet()) {
      for (Map.Entry<
              String,
              Function<
                  GridGrammar<String>,
                  Developer<String, Grid<String>, GridGrammar.ReferencedGrid<String>>>>
          developeEntry : developers.entrySet()) {
        for (Map.Entry<String, BiFunction<Integer, GridGrammar<String>, FactoryChooser<?>>>
            factoryChooserEntry : factoryChoosers.entrySet()) {
          for (int l = minL; l <= maxL; l = l + stepL) {
            int finalL = l;
            executorService.submit(() -> {
              Developer<String, Grid<String>, GridGrammar.ReferencedGrid<String>> developer =
                  developeEntry.getValue().apply(grammarEntry.getValue());
              FactoryChooser<?> factoryChooser =
                  factoryChooserEntry.getValue().apply(finalL, grammarEntry.getValue());
              List<? extends Mapped<?>> mappeds = factoryChooser.build(developer, n, rg);
              List<Grid<String>> grids = mappeds.stream()
                  .map(Mapped::phenotype)
                  .filter(Optional::isPresent)
                  .map(Optional::get)
                  .toList();
              StringBuilder line = new StringBuilder();
              line.append("%s\t%s\t%s\t%4d\t"
                  .formatted(
                      grammarEntry.getKey(),
                      developeEntry.getKey(),
                      factoryChooserEntry.getKey(),
                      finalL));
              //noinspection unchecked
              line.append(mappingMetrics.stream()
                  .map(Map.Entry::getValue)
                  .map(f -> f.applyAsDouble((List<Mapped<?>>) mappeds, factoryChooser.distance))
                  .map("%6.4f"::formatted)
                  .collect(Collectors.joining("\t")));
              line.append("\t");
              line.append(gridMetrics.stream()
                  .map(Map.Entry::getValue)
                  .map(f -> grids.stream()
                      .mapToDouble(f)
                      .average()
                      .orElse(Double.NaN))
                  .map("%6.4f"::formatted)
                  .collect(Collectors.joining("\t")));
              ps.println(line);
            });
          }
        }
      }
    }
    executorService.shutdown();
    boolean terminated = false;
    while (!terminated) {
      try {
        terminated = executorService.awaitTermination(1, TimeUnit.SECONDS);
      } catch (InterruptedException e) {
        // ignore
      }
    }
    System.out.println("Done");
    ps.close();
  }
}
