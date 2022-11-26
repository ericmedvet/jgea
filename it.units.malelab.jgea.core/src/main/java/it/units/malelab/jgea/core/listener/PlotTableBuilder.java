package it.units.malelab.jgea.core.listener;

import it.units.malelab.jgea.core.util.Table;

import java.util.List;

/**
 * @author "Eric Medvet" on 2022/11/26 for jgea
 */
public interface PlotTableBuilder<E> extends AccumulatorFactory<E, Table<Number>, Void> {
  List<NamedFunction<? super E, ? extends Number>> functions();

  default List<String> formats() {
    return functions().stream().map(NamedFunction::getFormat).toList();
  }

  default List<String> names() {
    return functions().stream().map(NamedFunction::getName).toList();
  }

  default String xFormat() {
    return formats().get(0);
  }

  default NamedFunction<? super E, ? extends Number> xFunction() {
    return functions().get(0);
  }

  default String xName() {
    return names().get(0);
  }

  default List<String> yFormats() {
    return formats().subList(1, functions().size());
  }

  default List<NamedFunction<? super E, ? extends Number>> yFunctions() {
    return functions().subList(1, functions().size());
  }

  default List<String> yNames() {
    return names().subList(1, functions().size());
  }
}
