package it.units.malelab.jgea.core.consumer;

import it.units.malelab.jgea.core.util.ArrayTable;
import it.units.malelab.jgea.core.util.Table;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author eric on 2021/01/04 for jgea
 */
public class TableBuilder<G, S, F, O> implements Consumer.Factory<G, S, F, Table<O>> {

  private final List<NamedFunction<Event<? extends G, ? extends S, ? extends F>, ? extends O>> functions;

  public TableBuilder(List<NamedFunction<Event<? extends G, ? extends S, ? extends F>, ? extends O>> functions) {
    this.functions = functions;
  }

  @Override
  public Consumer<G, S, F, Table<O>> build() {
    return new Consumer<>() {
      private final Table<O> table = new ArrayTable<>(functions.stream().map(NamedFunction::getName).collect(Collectors.toList()));

      @Override
      public void consume(Event<? extends G, ? extends S, ? extends F> event) {
        table.addRow(functions.stream()
            .map(f -> f.apply(event))
            .collect(Collectors.toList()));
      }

      @Override
      public Table<O> produce() {
        return table;
      }
    };
  }

}
