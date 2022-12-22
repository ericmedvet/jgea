package io.github.ericmedvet.jgea.core.listener;

import io.github.ericmedvet.jgea.core.util.Table;

import java.util.List;

/**
 * @author "Eric Medvet" on 2022/11/26 for jgea
 */
public interface PlotTableBuilder<E> extends AccumulatorFactory<E, Table<Number>, Object> {

  NamedFunction<? super E, ? extends Number> xFunction();

  List<NamedFunction<? super E, ? extends Number>> yFunctions();

  default String xFormat() {
    return xFunction().getFormat();
  }

  default String xName() {
    return xFunction().getName();
  }

  default List<String> yFormats() {
    return yFunctions().stream().map(NamedFunction::getFormat).toList();
  }

  default List<String> yNames() {
    return yFunctions().stream().map(NamedFunction::getName).toList();
  }
}
