/*-
 * ========================LICENSE_START=================================
 * jgea-core
 * %%
 * Copyright (C) 2018 - 2023 Eric Medvet
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
package io.github.ericmedvet.jgea.core.listener;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public interface AccumulatorFactory<E, O, K> extends ListenerFactory<E, K> {
  Accumulator<E, O> build(K k);

  static <E, O, K> AccumulatorFactory<E, O, K> last(BiFunction<E, K, O> function) {
    return k -> Accumulator.<E>last().then(e -> function.apply(e, k));
  }

  default <Q> AccumulatorFactory<E, Q, K> then(Function<O, Q> function) {
    AccumulatorFactory<E, O, K> inner = this;
    return new AccumulatorFactory<>() {
      @Override
      public Accumulator<E, Q> build(K k) {
        return inner.build(k).then(function);
      }

      @Override
      public void shutdown() {
        inner.shutdown();
      }
    };
  }

  default ListenerFactory<E, K> withAutoGet() {
    AccumulatorFactory<E, O, K> thisFactory = this;
    return new ListenerFactory<>() {
      @Override
      public Listener<E> build(K k) {
        return thisFactory.build(k).withAutoGet();
      }

      @Override
      public void shutdown() {
        thisFactory.shutdown();
      }
    };
  }

  default ListenerFactory<E, K> thenOnShutdown(Consumer<List<O>> consumer) {
    AccumulatorFactory<E, O, K> thisFactory = this;
    List<O> os = new ArrayList<>();
    return new ListenerFactory<>() {
      @Override
      public Listener<E> build(K k) {
        Accumulator<E, O> accumulator = thisFactory.build(k);
        return new Listener<>() {
          @Override
          public void listen(E e) {
            accumulator.listen(e);
          }

          @Override
          public void done() {
            os.add(accumulator.get());
          }
        };
      }

      @Override
      public void shutdown() {
        consumer.accept(os);
        thisFactory.shutdown();
      }
    };
  }
}
