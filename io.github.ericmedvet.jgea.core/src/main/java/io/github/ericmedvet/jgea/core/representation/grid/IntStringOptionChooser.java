package io.github.ericmedvet.jgea.core.representation.grid;

import io.github.ericmedvet.jgea.core.representation.sequence.integer.IntString;
import io.github.ericmedvet.jsdynsym.grid.Grid;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * @author "Eric Medvet" on 2023/06/16 for jgea
 */
public final class IntStringOptionChooser<T> implements GridDeveloper.Chooser<T> {
  private final IntString intString;
  private final GridGrammar<T> gridGrammar;
  private int i = 0;

  public IntStringOptionChooser(IntString intString, GridGrammar<T> gridGrammar) {
    this.intString = intString;
    this.gridGrammar = gridGrammar;
  }

  public static <T> Function<IntString, Grid<T>> mapper(
      GridGrammar<T> gridGrammar,
      GridDeveloper<T> gridDeveloper,
      Grid<T> defaultGrid
  ) {
    return is -> {
      IntStringOptionChooser<T> chooser = new IntStringOptionChooser<>(is, gridGrammar);
      return gridDeveloper.develop(chooser).orElse(defaultGrid);
    };
  }

  @Override
  public Optional<GridGrammar.ReferencedGrid<T>> choose(T t) {
    if (i >= intString.size()) {
      return Optional.empty();
    }
    List<GridGrammar.ReferencedGrid<T>> options = gridGrammar.getRules().get(t);
    int index = intString.get(i) % options.size();
    i = i + 1;
    return Optional.of(options.get(index));
  }

}
