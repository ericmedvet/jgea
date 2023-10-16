package io.github.ericmedvet.jgea.core.util;

import java.io.Serializable;
public record Pair<F, S>(F first, S second) implements Serializable {

  public static <F, S> Pair<F, S> of(F first, S second) {
    return new Pair<>(first, second);
  }

}
