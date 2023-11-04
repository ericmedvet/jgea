package io.github.ericmedvet.jgea.experimenter.listener.decoupled;

import io.github.ericmedvet.jgea.core.util.Pair;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * @author "Eric Medvet" on 2023/11/03 for jgea
 */
public interface Source<K,V> {

  Map<Pair<LocalDateTime,K>, V> pull(LocalDateTime t);
}
