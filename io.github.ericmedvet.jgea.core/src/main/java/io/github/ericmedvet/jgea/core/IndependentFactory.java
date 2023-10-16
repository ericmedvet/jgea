
package io.github.ericmedvet.jgea.core;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.random.RandomGenerator;
public interface IndependentFactory<T> extends Factory<T> {

  T build(RandomGenerator random);

  @SafeVarargs
  static <K> IndependentFactory<K> oneOf(IndependentFactory<K>... factories) {
    return random -> factories[random.nextInt(factories.length)].build(random);
  }

  @SafeVarargs
  static <K> IndependentFactory<K> picker(K... ks) {
    return random -> ks[random.nextInt(ks.length)];
  }

  static <K> IndependentFactory<K> picker(List<? extends K> ks) {
    return random -> ks.get(random.nextInt(ks.size()));
  }

  @Override
  default List<T> build(int n, RandomGenerator random) {
    List<T> ts = new ArrayList<>();
    for (int i = 0; i < n; i++) {
      ts.add(build(random));
    }
    return ts;
  }

  default <K> IndependentFactory<K> then(Function<T, K> f) {
    IndependentFactory<T> thisFactory = this;
    return random -> f.apply(thisFactory.build(random));
  }

}
