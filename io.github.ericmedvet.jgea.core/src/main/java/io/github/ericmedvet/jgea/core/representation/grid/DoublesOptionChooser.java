package io.github.ericmedvet.jgea.core.representation.grid;

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
    List<GridGrammar.ReferencedGrid<T>> options = gridGrammar.getRules().get(t);
    int index = (int) Math.max(Math.round(DoubleRange.UNIT.clip(values.get(i)) * options.size()), options.size() - 1);
    i = i + 1;
    return Optional.of(options.get(index));
  }

}
