/*
 * Copyright 2023 eric
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

package io.github.ericmedvet.jgea.core.representation.grammar.grid;

import io.github.ericmedvet.jgea.core.representation.grammar.GrammarOptionStringFactory;
import io.github.ericmedvet.jgea.core.representation.sequence.FixedLengthListFactory;
import io.github.ericmedvet.jgea.core.representation.sequence.integer.UniformIntStringFactory;
import io.github.ericmedvet.jgea.core.representation.sequence.numeric.UniformDoubleFactory;
import io.github.ericmedvet.jsdynsym.grid.Grid;
import io.github.ericmedvet.jsdynsym.grid.GridUtils;

import java.io.IOException;
import java.io.PrintStream;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.random.RandomGenerator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author "Eric Medvet" on 2023/06/16 for jgea
 */
public class GridBiasesAndProps {
  public static void main(String[] args) throws IOException {
    Locale.setDefault(Locale.ROOT);
    //one-for-all params
    RandomGenerator rg = new Random(0);
    int n = 1000;
    int minL = 10;
    int maxL = 200;
    int stepL = 10;
    PrintStream ps = new PrintStream("/home/eric/experiments/2023-EuroGP-GrammarBasedEvolutionOfPolyominoes/props.txt");
    //to-iterate params
    Map<String, GridGrammar<String>> grammars = Map.ofEntries(
        Map.entry("worm", GridGrammar.load(GridGrammar.class.getResourceAsStream("/grammars/2d/worm.bnf"))),
        Map.entry("simple", GridGrammar.load(GridGrammar.class.getResourceAsStream("/grammars/2d/simple.bnf"))),
        Map.entry(
            "non-compact",
            GridGrammar.load(GridGrammar.class.getResourceAsStream("/grammars/2d/cross.bnf"))
        ),
        Map.entry("dog-shape", GridGrammar.load(GridGrammar.class.getResourceAsStream("/grammars/2d/dog-shape.bnf")))
    );
    Map<String, Function<GridGrammar<String>, GridDeveloper<String>>> developers = Map.ofEntries(
        Map.entry(
            "now-least_recent",
            gg -> new StandardGridDeveloper<>(gg, true, List.of(StandardGridDeveloper.SortingCriterion.LEAST_RECENT))
        ),
        Map.entry(
            "ow-least_recent",
            gg -> new StandardGridDeveloper<>(gg, false, List.of(StandardGridDeveloper.SortingCriterion.LEAST_RECENT))
        ),
        Map.entry(
            "now-most_free_sides",
            gg -> new StandardGridDeveloper<>(
                gg,
                true,
                List.of(
                    StandardGridDeveloper.SortingCriterion.MOST_FREE_SIDE,
                    StandardGridDeveloper.SortingCriterion.LEAST_RECENT
                )
            )
        ),
        Map.entry(
            "ow-least_free_sides",
            gg -> new StandardGridDeveloper<>(
                gg,
                false,
                List.of(
                    StandardGridDeveloper.SortingCriterion.LEAST_FREE_SIDES,
                    StandardGridDeveloper.SortingCriterion.LEAST_RECENT
                )
            )
        )
    );
    Map<String, BiFunction<Integer, GridGrammar<String>, GridDeveloper.Chooser<String>>> choosers = Map.ofEntries(
        Map.entry("random", (l, gg) -> new RandomChooser<>(rg, l, gg)),
        Map.entry(
            "int-8",
            (l, gg) -> new IntStringChooser<>((new UniformIntStringFactory(0, 8, l)).build(rg), gg)
        ),
        Map.entry(
            "int-16",
            (l, gg) -> new IntStringChooser<>((new UniformIntStringFactory(0, 16, l)).build(rg), gg)
        ),
        Map.entry(
            "int-32",
            (l, gg) -> new IntStringChooser<>((new UniformIntStringFactory(0, 32, l)).build(rg), gg)
        ),
        Map.entry(
            "double",
            (l, gg) -> new DoublesChooser<>(new FixedLengthListFactory<>(
                l,
                new UniformDoubleFactory(0d, 1d)
            ).build(rg), gg)
        ),
        Map.entry(
            "gos-3",
            (l, gg) -> new GOSChooser<>(new GrammarOptionStringFactory<>(
                gg, l, 3
            ).build(rg), gg)
        )
    );
    List<Map.Entry<String, ToDoubleFunction<Grid<String>>>> metrics = List.of(
        Map.entry("w", g -> GridUtils.w(g, Objects::nonNull)),
        Map.entry("h", g -> GridUtils.h(g, Objects::nonNull)),
        Map.entry("count", g -> GridUtils.count(g, Objects::nonNull)),
        Map.entry("compactness", g -> GridUtils.compactness(g, Objects::nonNull)),
        Map.entry("elongation", g -> GridUtils.elongation(g, Objects::nonNull))
    );
    //iterate
    ps.println(
        "grammar\tdeveloper\tchooser\tl\tinvalidity\tuniqueness\t" +
            metrics.stream().map(Map.Entry::getKey).collect(Collectors.joining("\t"))
    );
    for (Map.Entry<String, GridGrammar<String>> grammarEntry : grammars.entrySet()) {
      for (Map.Entry<String, Function<GridGrammar<String>, GridDeveloper<String>>> developeEntry :
          developers.entrySet()) {
        for (Map.Entry<String, BiFunction<Integer, GridGrammar<String>, GridDeveloper.Chooser<String>>> chooserEntry
            : choosers.entrySet()) {
          for (int l = minL; l <= maxL; l = l + stepL) {
            final int localL = l;
            GridDeveloper<String> developer = developeEntry.getValue().apply(grammarEntry.getValue());
            List<Optional<Grid<String>>> oGrids = IntStream.range(0, n)
                .mapToObj(i -> developer.develop(chooserEntry.getValue().apply(localL, grammarEntry.getValue())))
                .toList();
            List<Grid<String>> grids = oGrids.stream().filter(Optional::isPresent).map(Optional::get).toList();
            double invalidity = 1d - (double) grids.size() / oGrids.size();
            double uniqueness = (double) grids.stream().distinct().count() / grids.size();
            ps.printf("%s\t%s\t%s\t%4d\t%.5f\t%.5f\t",
                grammarEntry.getKey(), developeEntry.getKey(), chooserEntry.getKey(), l,
                invalidity, uniqueness
            );
            ps.println(metrics.stream()
                .map(Map.Entry::getValue)
                .map(f -> grids.stream().mapToDouble(f).average().orElse(Double.NaN))
                .map("%3.3f"::formatted)
                .collect(Collectors.joining("\t")));
          }
        }
      }
    }
    ps.close();
  }
}
