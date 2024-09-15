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
package io.github.ericmedvet.jgea.core.listener;

import io.github.ericmedvet.jgea.core.util.Misc;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@FunctionalInterface
public interface Listener<E> {

  void listen(E e);

  static <E> Listener<E> all(List<Listener<? super E>> listeners) {
    return from(
        "all[%s]".formatted(listeners.stream().map(Object::toString).collect(Collectors.joining(";"))),
        e -> listeners.forEach(l -> l.listen(e)),
        () -> listeners.forEach(Listener::done));
  }

  static <E> Listener<E> deaf() {
    return from("deaf", e -> {}, () -> {});
  }

  static <E> Listener<E> from(String name, Consumer<E> consumer, Runnable doneRunnable) {
    return new Listener<>() {
      @Override
      public void listen(E e) {
        consumer.accept(e);
      }

      @Override
      public void done() {
        doneRunnable.run();
      }

      @Override
      public String toString() {
        return name;
      }
    };
  }

  default Listener<E> and(Listener<? super E> other) {
    return all(List.of(this, other));
  }

  default Listener<E> deferred(ExecutorService executorService) {
    final Logger L = Logger.getLogger(Listener.class.getName());
    return from(
        "%s[deferered]".formatted(this),
        e -> executorService.submit(() -> Misc.doOrLog(
            () -> listen(e),
            Logger.getLogger(Listener.class.getName()),
            Level.WARNING,
            t -> String.format("Listener %s cannot listen() event: %s", this, t))),
        () -> executorService.submit(() -> Misc.doOrLog(
            this::done,
            Logger.getLogger(Listener.class.getName()),
            Level.WARNING,
            t -> String.format("Listener %s cannot done(): %s", this, t))));
  }

  default void done() {}

  default <F> Listener<F> forEach(Function<F, Collection<E>> splitter) {
    return from(
        "%s[forEach:%s]".formatted(this, splitter),
        f -> splitter.apply(f).forEach(this::listen),
        this::done);
  }

  default <F> Listener<F> on(Function<F, E> function) {
    return from("%s[on:%s]".formatted(this, function), f -> listen(function.apply(f)), this::done);
  }

  default Listener<E> onLast() {
    return Accumulator.from("%s[last]".formatted(this), () -> null, (e, oldE) -> e, this::listen);
  }
}
