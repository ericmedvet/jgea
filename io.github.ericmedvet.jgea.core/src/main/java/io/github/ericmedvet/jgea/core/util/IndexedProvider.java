/*-
 * ========================LICENSE_START=================================
 * jgea-core
 * %%
 * Copyright (C) 2018 - 2026 Eric Medvet
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */
package io.github.ericmedvet.jgea.core.util;

import io.github.ericmedvet.jnb.datastructure.Utils;
import java.util.*;
import java.util.function.Function;
import java.util.random.RandomGenerator;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public interface IndexedProvider<T> {
  T get(int i);

  List<Integer> indexes();

  static <T> IndexedProvider<T> from(List<T> ts) {
    List<Integer> indexes = IntStream.range(0, ts.size()).boxed().toList();
    return new IndexedProvider<>() {
      @Override
      public T get(int i) {
        return ts.get(i);
      }

      @Override
      public List<Integer> indexes() {
        return indexes;
      }
    };
  }

  default List<T> all() {
    return stream().toList();
  }

  default IndexedProvider<T> asSynchronized() {
    IndexedProvider<T> thisIndexedProvider = this;
    return new IndexedProvider<>() {
      @Override
      public synchronized T get(int i) {
        return thisIndexedProvider.get(i);
      }

      @Override
      public synchronized List<Integer> indexes() {
        return thisIndexedProvider.indexes();
      }
    };
  }

  default T first() {
    return get(indexes().getFirst());
  }

  default IndexedProvider<T> fold(int j, int n) {
    IndexedProvider<T> thisIndexedProvider = this;
    List<Integer> foldIndexes = Utils.fold(indexes(), j, n);
    return new IndexedProvider<>() {
      @Override
      public T get(int i) {
        return thisIndexedProvider.get(i);
      }

      @Override
      public List<Integer> indexes() {
        return foldIndexes;
      }
    };
  }

  default IndexedProvider<T> negatedFold(int j, int n) {
    IndexedProvider<T> thisIndexedProvider = this;
    List<Integer> foldIndexes = Utils.negatedFold(indexes(), j, n);
    return new IndexedProvider<>() {
      @Override
      public T get(int i) {
        return thisIndexedProvider.get(i);
      }

      @Override
      public List<Integer> indexes() {
        return foldIndexes;
      }
    };
  }

  default IndexedProvider<T> shuffled(RandomGenerator rnd) {
    IndexedProvider<T> thisIndexedProvider = this;
    List<Integer> shuffledIndexes = new ArrayList<>(indexes());
    Collections.shuffle(shuffledIndexes, rnd);
    List<Integer> finalShuffledIndexes = Collections.unmodifiableList(shuffledIndexes);
    return new IndexedProvider<>() {
      @Override
      public T get(int i) {
        return thisIndexedProvider.get(i);
      }

      @Override
      public List<Integer> indexes() {
        return finalShuffledIndexes;
      }
    };
  }

  default int size() {
    return indexes().size();
  }

  default Stream<T> stream() {
    return indexes().stream().map(this::get);
  }

  default <K> IndexedProvider<K> then(Function<? super T, ? extends K> function) {
    IndexedProvider<T> thisIndexedProvider = this;
    return new IndexedProvider<>() {
      @Override
      public K get(int i) {
        return function.apply(thisIndexedProvider.get(i));
      }

      @Override
      public List<Integer> indexes() {
        return thisIndexedProvider.indexes();
      }
    };
  }

  default IndexedProvider<T> cached() {
    return from(stream().toList());
  }
}