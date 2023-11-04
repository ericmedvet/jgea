package io.github.ericmedvet.jgea.experimenter.listener.decoupled;

import io.github.ericmedvet.jgea.core.util.Pair;

import java.time.LocalDateTime;

/**
 * @author "Eric Medvet" on 2023/11/04 for jgea
 */
public class DirectSinkSource<K, V> extends AbstractAutoPurgingSource<K, V> implements Sink<K, V> {

  @Override
  public void push(LocalDateTime t, K k, V v) {
    map.put(new Pair<>(t, k), v);
  }
}
