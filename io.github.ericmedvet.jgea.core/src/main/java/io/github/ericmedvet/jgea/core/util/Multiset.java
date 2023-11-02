package io.github.ericmedvet.jgea.core.util;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @author "Eric Medvet" on 2023/11/02 for jgea
 */
public interface Multiset<E> extends Collection<E> {
  int count(E e);

  Set<E> elementSet();

  @SafeVarargs
  static <E> Multiset<E> of(E... es) {
    return new LinkedHashMultiset<>(List.of(es));
  }
}
