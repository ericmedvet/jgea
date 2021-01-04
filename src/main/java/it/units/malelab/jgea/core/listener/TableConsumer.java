package it.units.malelab.jgea.core.listener;

import it.units.malelab.jgea.core.util.ArrayTable;
import it.units.malelab.jgea.core.util.Table;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author eric on 2021/01/04 for jgea
 */
public class TableConsumer<G, S, F, O> implements Consumer<G, S, F, Table<O>> {

  private final List<NamedFunction<Event<? extends G, ? extends S, ? extends F>, ? extends O>> functions;
  private final Table<O> table;

  public TableConsumer(List<NamedFunction<Event<? extends G, ? extends S, ? extends F>, ? extends O>> functions) {
    this.functions = functions;
    table = new ArrayTable<>(functions.stream().map(NamedFunction::getName).collect(Collectors.toList()));
  }

  @Override
  public void clear() {
    table.clear();
  }

  @Override
  public Table<O> produce() {
    return table;
  }

  @Override
  public void consume(Event<? extends G, ? extends S, ? extends F> event) {
    table.addRow(functions.stream()
        .map(f -> f.apply(event))
        .collect(Collectors.toList())
    );
  }

}
