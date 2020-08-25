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

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author eric
 */
@FunctionalInterface
public interface Factory<T> {

  List<T> build(int n, Random random);

  default Factory<T> withOptimisticUniqueness(int maxAttempts) {
    Factory<T> innerFactory = this;
    return new Factory<T>() {
      @Override
      public List<T> build(int n, Random random) {
        int attempts = 0;
        List<T> ts = new ArrayList<>(n);
        while ((ts.size() < n) && (attempts < maxAttempts)) {
          attempts = attempts + 1;
          ts.addAll(new LinkedHashSet<>(innerFactory.build(n - ts.size(), random)));
        }
        ts.addAll(innerFactory.build(n - ts.size(), random));
        return ts;
      }
    };
  }

  default IndependentFactory<T> independent() {
    Factory<T> thisFactory = this;
    return (IndependentFactory<T>) random -> thisFactory.build(1, random).get(0);
  }

  default <K> Factory<K> then(Function<T, K> f) {
    Factory<T> thisFactory = this;
    return (n, random) -> thisFactory.build(n, random).stream()
        .map(f::apply)
        .collect(Collectors.toList());
  }

}
