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

import io.github.ericmedvet.jgea.core.representation.grammar.Chooser;
import io.github.ericmedvet.jgea.core.representation.grammar.Developer;
import io.github.ericmedvet.jsdynsym.grid.Grid;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class StandardGridDeveloper<T>
    implements Developer<T, Grid<T>, GridGrammar.ReferencedGrid<T>> {
  private final GridGrammar<T> grammar;
  private final boolean overwriting;
  private final Comparator<Grid.Entry<Decorated>> comparator;

  public StandardGridDeveloper(
      GridGrammar<T> grammar, boolean overwriting, List<SortingCriterion> criteria) {
    this.grammar = grammar;
    this.overwriting = overwriting;
    if (criteria.isEmpty()) {
      throw new IllegalArgumentException("Empty list of sorting criteria");
    }
    comparator =
        criteria.stream()
            .map(SortingCriterion::getComparator)
            .reduce(Comparator::thenComparing)
            .orElseThrow();
  }

  public enum SortingCriterion {
    LEAST_RECENT(Comparator.comparingInt(e -> e.value().iteration)),
    MOST_RECENT(LEAST_RECENT.comparator.reversed()),
    LEAST_FREE_SIDES(Comparator.comparingInt(e -> e.value().nOfFreeSides)),
    MOST_FREE_SIDE(LEAST_FREE_SIDES.comparator.reversed()),
    LOWEST_X(Comparator.comparingInt(e -> e.key().x())),
    LOWEST_Y(Comparator.comparingInt(e -> e.key().y()));
    private final Comparator<Grid.Entry<Decorated>> comparator;

    SortingCriterion(Comparator<Grid.Entry<Decorated>> comparator) {
      this.comparator = comparator;
    }

    public Comparator<Grid.Entry<Decorated>> getComparator() {
      return comparator;
    }
  }

  private record Aged<T>(int iteration, T t) {}

  public record Decorated(int iteration, int nOfFreeSides) {}

  private static int freeSides(Grid<?> g, Grid.Key k) {
    int n = 0;
    n = n + ((k.x() < 1 || g.get(k.x() - 1, k.y()) == null) ? 1 : 0);
    n = n + ((k.x() >= g.w() - 1 || g.get(k.x() + 1, k.y()) == null) ? 1 : 0);
    n = n + ((k.y() < 1 || g.get(k.x(), k.y() - 1) == null) ? 1 : 0);
    n = n + ((k.y() >= g.h() - 1 || g.get(k.x(), k.y() + 1) == null) ? 1 : 0);
    return n;
  }

  private static boolean isWriteable(
      Grid<?> original, GridGrammar.ReferencedGrid<?> replacement, Grid.Key k) {
    return replacement.grid().entries().stream()
        .filter(e -> e.value() != null && !e.key().equals(replacement.referenceKey()))
        .noneMatch(
            e -> {
              Grid.Key tK =
                  e.key()
                      .translated(k.x(), k.y())
                      .translated(-replacement.referenceKey().x(), -replacement.referenceKey().y());
              if (!original.isValid(tK)) {
                return false;
              }
              return !original.isValid(tK) || original.get(tK) != null;
            });
  }

  private static <T> Grid<Aged<T>> modify(
      Grid<Aged<T>> original,
      GridGrammar.ReferencedGrid<T> replacement,
      Grid.Key k,
      int iteration) {
    List<Grid.Entry<T>> repEntries =
        replacement.grid().entries().stream()
            .map(
                e ->
                    new Grid.Entry<>(
                        e.key()
                            .translated(k.x(), k.y())
                            .translated(
                                -replacement.referenceKey().x(), -replacement.referenceKey().y()),
                        e.value()))
            .toList();
    int minX = Math.min(repEntries.stream().mapToInt(e -> e.key().x()).min().orElse(0), 0);
    int maxX =
        Math.max(repEntries.stream().mapToInt(e -> e.key().x()).max().orElse(0), original.w() - 1);
    int minY = Math.min(repEntries.stream().mapToInt(e -> e.key().y()).min().orElse(0), 0);
    int maxY =
        Math.max(repEntries.stream().mapToInt(e -> e.key().y()).max().orElse(0), original.h() - 1);
    if (minX >= 0 && maxX < original.w() && minY >= 0 && maxY < original.h()) {
      // just write elements on the original grid
      repEntries.stream()
          .filter(e -> e.value() != null)
          .forEach(e -> original.set(e.key(), new Aged<>(iteration, e.value())));
      return original;
    }
    // build a new grid and fill it
    Grid<Aged<T>> enlarged = Grid.create(maxX - minX + 1, maxY - minY + 1);
    original.entries().forEach(e -> enlarged.set(e.key().translated(-minX, -minY), e.value()));
    repEntries.stream()
        .filter(e -> e.value() != null)
        .forEach(
            e -> enlarged.set(e.key().translated(-minX, -minY), new Aged<>(iteration, e.value())));
    return enlarged;
  }

  public Optional<Grid<T>> develop(Chooser<T, GridGrammar.ReferencedGrid<T>> optionChooser) {
    Set<T> nonTerminalSymbols = grammar.rules().keySet();
    int i = 0;
    // build a 1x1 grid with the starting symbol
    Grid<Aged<T>> polyomino = Grid.create(1, 1, new Aged<>(i, grammar.startingSymbol()));
    while (true) {
      // find the candidates
      final Grid<Aged<T>> finalPolyomino = polyomino;
      List<Grid.Entry<Decorated>> candidates =
          polyomino.entries().stream()
              .filter(e -> e.value() != null && nonTerminalSymbols.contains(e.value().t()))
              .map(
                  e ->
                      new Grid.Entry<>(
                          e.key(),
                          new Decorated(e.value().iteration, freeSides(finalPolyomino, e.key()))))
              .toList();
      // check if no non-terminal symbols
      if (candidates.isEmpty()) {
        return Optional.of(polyomino.map(a -> a == null ? null : a.t()));
      }
      // sort the candidates
      candidates = candidates.stream().sorted(comparator).toList();
      boolean modified = false;
      for (Grid.Entry<Decorated> candidate : candidates) {
        T symbol = polyomino.get(candidate.key()).t();
        Optional<GridGrammar.ReferencedGrid<T>> production = optionChooser.chooseFor(symbol);
        if (production.isEmpty()) {
          return Optional.empty();
        }
        if (overwriting || isWriteable(polyomino, production.get(), candidate.key())) {
          // modify grid
          polyomino = modify(polyomino, production.get(), candidate.key(), i);
          modified = true;
          break;
        }
      }
      if (!modified) {
        return Optional.empty();
      }
      i = i + 1;
    }
  }
}
