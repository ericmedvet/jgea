package io.github.ericmedvet.jgea.experimenter.listener.decoupled;

import io.github.ericmedvet.jgea.core.util.Pair;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author "Eric Medvet" on 2023/11/03 for jgea
 */
public abstract class AbstractAutoPurgingSource<K, V> implements Source<K, V> {

  protected final Map<Pair<LocalDateTime, K>, V> map;
  private LocalDateTime lastPullLocalDateTime = LocalDateTime.MIN;

  public AbstractAutoPurgingSource() {
    this.map = new LinkedHashMap<>();
  }

  @Override
  public Map<Pair<LocalDateTime, K>, V> pull(LocalDateTime t) {
    synchronized (map) {
      List<Pair<LocalDateTime, K>> toRemovePs = map.keySet().stream().filter(p -> p.first().isBefore(lastPullLocalDateTime)).toList();
      toRemovePs.forEach(map::remove);
    }
    lastPullLocalDateTime = t;
    return new LinkedHashMap<>(map);
  }
}
