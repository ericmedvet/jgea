package io.github.ericmedvet.jgea.experimenter.listener.decoupled;

import io.github.ericmedvet.jgea.core.util.Pair;
import io.github.ericmedvet.jgea.core.util.Table;

import java.time.LocalDateTime;
import java.util.Collection;

/**
 * @author "Eric Medvet" on 2023/11/04 for jgea
 */
public class MostRecentTableSource extends TabledSource {

  public MostRecentTableSource(Source source) {
    super(source);
  }

  private static <K> void prune(Table<Pair<LocalDateTime, K>, String, ?> table, int n) {
    Collection<K> ks = table.rowIndexes().stream().map(Pair::second).distinct().toList();
    ks.forEach(k -> table.rowIndexes().stream()
        .filter(p -> p.second().equals(k))
        .sorted((p1,p2)-> p2.first().compareTo(p1.first()))
        .skip(n)
        .forEach(table::removeRow));
  }

  @Override
  public void refresh() {
    super.refresh();
    prune(machines,3);
    prune(processes,3);
    prune(logs,1);
    prune(experiments,3);
    prune(runs,3);
    prune(dataItems,3);
  }
}
