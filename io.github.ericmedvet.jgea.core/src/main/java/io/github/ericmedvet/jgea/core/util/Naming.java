/*-
 * ========================LICENSE_START=================================
 * jgea-core
 * %%
 * Copyright (C) 2018 - 2024 Eric Medvet
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

import io.github.ericmedvet.jgea.core.listener.Accumulator;
import io.github.ericmedvet.jgea.core.listener.AccumulatorFactory;
import io.github.ericmedvet.jgea.core.listener.Listener;
import io.github.ericmedvet.jgea.core.listener.ListenerFactory;
import io.github.ericmedvet.jnb.datastructure.TriConsumer;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.DoubleUnaryOperator;

public class Naming {

  private Naming() {}

  public static <I1, I2> BiConsumer<I1, I2> named(String name, BiConsumer<I1, I2> consumer) {
    return new BiConsumer<>() {
      @Override
      public void accept(I1 i1, I2 i2) {
        consumer.accept(i1, i2);
      }

      @Override
      public String toString() {
        return name;
      }
    };
  }

  public static <I> Consumer<I> named(String name, Consumer<I> consumer) {
    return new Consumer<>() {
      @Override
      public void accept(I i) {
        consumer.accept(i);
      }

      @Override
      public String toString() {
        return name;
      }
    };
  }

  public static <E, O> Accumulator<E, O> named(String name, Accumulator<E, O> accumulator) {
    return new Accumulator<>() {
      @Override
      public O get() {
        return accumulator.get();
      }

      @Override
      public void listen(E e) {
        accumulator.listen(e);
      }

      @Override
      public void done() {
        accumulator.done();
      }

      @Override
      public String toString() {
        return name;
      }
    };
  }

  public static <E> Listener<E> named(String name, Listener<E> listener) {
    return new Listener<>() {
      @Override
      public void listen(E e) {
        listener.listen(e);
      }

      @Override
      public void done() {
        listener.done();
      }

      @Override
      public String toString() {
        return name;
      }
    };
  }

  public static <E, K> ListenerFactory<E, K> named(String name, ListenerFactory<E, K> listenerFactory) {
    return new ListenerFactory<>() {
      @Override
      public Listener<E> build(K k) {
        return listenerFactory.build(k);
      }

      @Override
      public void shutdown() {
        listenerFactory.shutdown();
      }

      @Override
      public String toString() {
        return name;
      }
    };
  }

  public static <E, O, K> AccumulatorFactory<E, O, K> named(
      String name, AccumulatorFactory<E, O, K> accumulatorFactory) {
    return new AccumulatorFactory<>() {
      @Override
      public Accumulator<E, O> build(K k) {
        return accumulatorFactory.build(k);
      }

      @Override
      public void shutdown() {
        accumulatorFactory.shutdown();
      }

      @Override
      public String toString() {
        return name;
      }
    };
  }

  public static <I1, I2, I3> TriConsumer<I1, I2, I3> named(String name, TriConsumer<I1, I2, I3> consumer) {
    return new TriConsumer<>() {
      @Override
      public void accept(I1 i1, I2 i2, I3 i3) {
        consumer.accept(i1, i2, i3);
      }

      @Override
      public String toString() {
        return name;
      }
    };
  }

  public static DoubleUnaryOperator named(String name, DoubleUnaryOperator o) {
    return new DoubleUnaryOperator() {
      @Override
      public double applyAsDouble(double operand) {
        return o.applyAsDouble(operand);
      }

      @Override
      public String toString() {
        return name;
      }
    };
  }
}
