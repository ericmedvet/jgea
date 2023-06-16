package io.github.ericmedvet.jgea.core.representation.grid;

import io.github.ericmedvet.jsdynsym.grid.Grid;

import java.util.Optional;

public interface GridDeveloper<T> {

  interface Chooser<T> {
    Optional<GridGrammar.ReferencedGrid<T>> choose(T t);
  }

  Optional<Grid<T>> develop(Chooser<T> chooser);

}
