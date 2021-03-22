package it.units.malelab.jgea.core.listener;

import it.units.malelab.jgea.core.util.ArrayTable;
import it.units.malelab.jgea.core.util.Table;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author eric on 2021/01/04 for jgea
 */
public class TableBuilder<E, O> implements Accumulator.Factory<E, Table<O>> {

  private final List<NamedFunction<? super E, ? extends O>> functions;

  public TableBuilder(List<NamedFunction<? super E, ? extends O>> functions) {
    this.functions = functions;
  }

  @Override
  public Accumulator<E, Table<O>> build() {
    return new Accumulator<>() {

      private final Table<O> table = new ArrayTable<>(functions.stream()
          .map(NamedFunction::getName)
          .collect(Collectors.toList())
      );

      @Override
      public Table<O> get() {
        return table;
      }

      @Override
      public void listen(E e) {
        table.addRow(functions.stream()
            .map(f -> f.apply(e))
            .collect(Collectors.toList())
        );
      }
    };
  }

}
