package io.github.ericmedvet.jgea.experimenter.listener.decoupled;

import io.github.ericmedvet.jgea.core.util.HashMapTable;
import io.github.ericmedvet.jgea.core.util.Pair;
import io.github.ericmedvet.jgea.core.util.Table;
import io.github.ericmedvet.jgea.core.util.TextPlotter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author "Eric Medvet" on 2023/11/04 for jgea
 */
public class TSChecker<K, V> {

  private final Source<K, V> source;
  private final Table<Pair<LocalDateTime, K>, String, V> table;

  public TSChecker(Source<K, V> source) {
    this.source = source;
    table = new HashMapTable<>();
    Executors.newScheduledThreadPool(1).scheduleAtFixedRate(this::check, 0, 1, TimeUnit.SECONDS);
  }

  private static <K, V> void prune(Table<Pair<LocalDateTime, K>, String, V> table, int n) {
    List<Pair<LocalDateTime, K>> toRemovePs = table.rowIndexes().stream()
        .map(Pair::second)
        .distinct()
        .map(k -> table.rowIndexes().stream()
            .filter(p -> p.second().equals(k))
            .sorted((p1, p2) -> p2.first().compareTo(p1.first()))
            .skip(n)
            .toList())
        .flatMap(List::stream)
        .toList();
    toRemovePs.forEach(table::removeRow);
  }

  private void check() {
    try {
      source.pull(LocalDateTime.now())
          .forEach((p, v) -> table.set(p, "value", v));
      prune(table, 2);
      System.out.println(table.expandColumn("value", v -> Map.ofEntries(
              Map.entry("progress", TextPlotter.horizontalBar(((RunInfo) v).progress().rate(), 0, 1, 8)),
              Map.entry("ended", ((RunInfo) v).ended())
          ))
          .prettyPrint(
              p -> "%tT %s".formatted(p.first(), ((RunKey) p.second()).value()),
              "%5.5s"::formatted,
              Object::toString
          ));
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }

}
