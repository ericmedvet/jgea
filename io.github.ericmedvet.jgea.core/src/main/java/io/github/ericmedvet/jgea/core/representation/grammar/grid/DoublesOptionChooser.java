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

import io.github.ericmedvet.jsdynsym.core.DoubleRange;
import io.github.ericmedvet.jsdynsym.grid.Grid;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * @author "Eric Medvet" on 2023/06/16 for jgea
 */
public final class DoublesOptionChooser<T> implements GridDeveloper.Chooser<T> {
  private final List<Double> values;
  private final GridGrammar<T> gridGrammar;
  private int i = 0;

  public DoublesOptionChooser(List<Double> values, GridGrammar<T> gridGrammar) {
    this.values = values;
    this.gridGrammar = gridGrammar;
  }

  public static <T> Function<List<Double>, Grid<T>> mapper(
      GridGrammar<T> gridGrammar,
      GridDeveloper<T> gridDeveloper,
      Grid<T> defaultGrid
  ) {
    return values -> {
      DoublesOptionChooser<T> chooser = new DoublesOptionChooser<>(values, gridGrammar);
      return gridDeveloper.develop(chooser).orElse(defaultGrid);
    };
  }

  @Override
  public Optional<GridGrammar.ReferencedGrid<T>> choose(T t) {
    if (i >= values.size()) {
      return Optional.empty();
    }
    List<GridGrammar.ReferencedGrid<T>> options = gridGrammar.rules().get(t);
    int index = (int) Math.min(Math.round(DoubleRange.UNIT.clip(values.get(i)) * options.size()), options.size() - 1);
    i = i + 1;
    return Optional.of(options.get(index));
  }

}
