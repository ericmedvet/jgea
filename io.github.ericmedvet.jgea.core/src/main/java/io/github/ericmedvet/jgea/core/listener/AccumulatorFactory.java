/*-
 * ========================LICENSE_START=================================
 * jgea-core
 * %%
 * Copyright (C) 2018 - 2025 Eric Medvet
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

import io.github.ericmedvet.jgea.core.util.Naming;
import io.github.ericmedvet.jnb.datastructure.NamedFunction;
import java.util.List;
import java.util.function.*;

public interface AccumulatorFactory<E, O, K> extends ListenerFactory<E, K> {

  Accumulator<E, O> build(K k);

  @Override
  default AccumulatorFactory<E, O, K> conditional(Predicate<K> kPredicate, Predicate<E> ePredicate) {
    return from(
        "%s[if:%s;%s]".formatted(this, kPredicate, ePredicate),
        k -> kPredicate.test(k) ? build(k).conditional(ePredicate) : Accumulator.nullAccumulator(),
        this::shutdown
    );
  }

  @Override
  default <X> AccumulatorFactory<X, O, K> on(Function<X, E> function) {
    return from("%s[on:%s]".formatted(this, function), k -> build(k).on(function), this::shutdown);
  }

  static <E, O, K> AccumulatorFactory<E, List<O>, K> collector(Function<E, O> function) {
    return from("collector[%s]".formatted(function), k -> Accumulator.collector(function), () -> {
    });
  }

  static <E, O, K> AccumulatorFactory<E, O, K> from(
      String name,
      Function<K, Accumulator<E, O>> aFunction,
      Runnable shutdownRunnable
  ) {
    return new AccumulatorFactory<>() {
      @Override
      public Accumulator<E, O> build(K k) {
        return aFunction.apply(k);
      }

      @Override
      public void shutdown() {
        shutdownRunnable.run();
      }

      @Override
      public String toString() {
        return name;
      }
    };
  }

  static <E, O, K> AccumulatorFactory<E, O, K> last(BiFunction<E, K, O> function) {
    return from(
        "last[then:%s]".formatted(function),
        k -> Accumulator.<E>last()
            .then(NamedFunction.from(e -> function.apply(e, k), function.toString())),
        () -> {
        }
    );
  }

  default <Q> AccumulatorFactory<E, Q, K> then(Function<O, Q> function) {
    return from(
        "%s[then:%s]".formatted(this, function),
        k -> build(k).then(function),
        this::shutdown
    );
  }

  default AccumulatorFactory<E, O, K> thenOnDone(BiConsumer<K, Accumulator<E, O>> consumer) {
    return from(
        "%s[thenOnDone:%s]".formatted(this, consumer),
        k -> build(k).thenOnDone(
            Naming.named(consumer.toString(), (Consumer<Accumulator<E, O>>) a -> consumer.accept(k, a))
        ),
        this::shutdown
    );
  }

  default <AO> AccumulatorFactory<E, O, K> thenOnShutdown(
      Supplier<AO> initializer,
      BiConsumer<Accumulator<E, O>, AO> updater,
      Consumer<AO> consumer
  ) {
    AO accumulatedOutput = initializer.get();
    return from(
        "%s[thenOnShutDown:%s]".formatted(this, consumer),
        k -> build(k).thenOnDone(
            Naming.named(updater.toString(), (Consumer<Accumulator<E, O>>) a -> updater.accept(a, accumulatedOutput))
        ),
        () -> {
          consumer.accept(accumulatedOutput);
          shutdown();
        }
    );
  }

  default AccumulatorFactory<E, O, K> thenOnShutdown(Consumer<O> consumer) {
    class Box<C> {
      C content;
    }
    return thenOnShutdown(
        Box::new,
        Naming.named(
            "keepForShutdown",
            (BiConsumer<Accumulator<E, O>, Box<Accumulator<E, O>>>) (o, box) -> box.content = o
        ),
        Naming.named(consumer.toString(), (Consumer<Box<Accumulator<E, O>>>) box -> consumer.accept(box.content.get()))
    );
  }
}
