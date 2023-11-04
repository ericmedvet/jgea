package io.github.ericmedvet.jgea.experimenter.listener.decoupled;

import java.time.LocalDateTime;

/**
 * @author "Eric Medvet" on 2023/11/03 for jgea
 */
public interface Sink<K, V> {

  void push(LocalDateTime t, K k, V v);

  default void push(K k, V v) {
    push(LocalDateTime.now(), k, v);
  }

}
