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
import java.util.List;
import java.util.Random;
import java.util.function.Function;

/**
 * @author eric
 */
public interface IndependentFactory<T> extends Factory<T> {

  @Override
  default List<T> build(int n, Random random) {
    List<T> ts = new ArrayList<>();
    for (int i = 0; i < n; i++) {
      ts.add(build(random));
    }
    return ts;
  }

  T build(Random random);

  static <K> IndependentFactory<K> picker(K... ks) {
    return random -> ks[random.nextInt(ks.length)];
  }

  @SafeVarargs
  static <K> IndependentFactory<K> oneOf(IndependentFactory<K>... factories) {
    return random -> factories[random.nextInt(factories.length)].build(random);
  }

  default <K> IndependentFactory<K> then(Function<T, K> f) {
    IndependentFactory<T> thisFactory = this;
    return random -> f.apply(thisFactory.build(random));
  }

}
