
package io.github.ericmedvet.jgea.core.distance;

import java.util.function.BiFunction;
import java.util.function.Function;
@FunctionalInterface
public interface Distance<T> extends BiFunction<T, T, Double> {
  default <K> Distance<K> on(Function<K, T> f) {
    Distance<T> tDistance = this;
    return (k1, k2) -> tDistance.apply(f.apply(k1), f.apply(k2));
  }
}
