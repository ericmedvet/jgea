/*
 * Copyright 2020 Eric Medvet <eric.medvet@gmail.com> (as eric)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it.units.malelab.jgea.core;

import it.units.malelab.jgea.core.util.Pair;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.Function;
import java.util.random.RandomGenerator;

/**
 * @author eric
 */
@FunctionalInterface
public interface Factory<T> {

  static <T1, T2> Factory<Pair<T1, T2>> pair(Factory<T1> factory1, Factory<T2> factory2) {
    return (n, random) -> {
      List<T1> t1s = factory1.build(n, random);
      List<T2> t2s = factory2.build(n, random);
      if (t1s.size() != n || t2s.size() != n) {
        throw new RuntimeException(String.format(
            "The two internal factories produced a different number of elements: %d and %d",
            t1s.size(),
            t2s.size()
        ));
      }
      List<Pair<T1, T2>> pairs = new ArrayList<>(n);
      for (int i = 0; i < n; i++) {
        pairs.add(Pair.of(t1s.get(i), t2s.get(i)));
      }
      return pairs;
    };
  }

  List<T> build(int n, RandomGenerator random);

  default IndependentFactory<T> independent() {
    Factory<T> thisFactory = this;
    return random -> thisFactory.build(1, random).get(0);
  }

  default <K> Factory<K> then(Function<T, K> f) {
    Factory<T> thisFactory = this;
    return (n, random) -> thisFactory.build(n, random).stream()
        .map(f)
        .toList();
  }

  default Factory<T> withOptimisticUniqueness(int maxAttempts) {
    Factory<T> innerFactory = this;
    return (n, random) -> {
      int attempts = 0;
      List<T> ts = new ArrayList<>(n);
      while ((ts.size() < n) && (attempts < maxAttempts)) {
        attempts = attempts + 1;
        ts.addAll(new LinkedHashSet<>(innerFactory.build(n - ts.size(), random)));
      }
      ts.addAll(innerFactory.build(n - ts.size(), random));
      return ts;
    };
  }

}
