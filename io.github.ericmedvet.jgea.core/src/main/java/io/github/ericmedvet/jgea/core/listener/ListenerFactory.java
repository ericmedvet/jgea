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
import io.github.ericmedvet.jnb.datastructure.NamedFunction;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public interface ListenerFactory<E, K> {

  Listener<E> build(K k);

  static <E, K> ListenerFactory<E, K> all(List<? extends ListenerFactory<? super E, ? super K>> factories) {
    return from(
        "all[%s]".formatted(factories.stream().map(Object::toString).collect(Collectors.joining(";"))),
        k -> Listener.all(factories.stream().map(f -> f.build(k)).collect(Collectors.toList())),
        () -> factories.forEach(listenerFactory -> Misc.doOrLog(
            listenerFactory::shutdown,
            Logger.getLogger(ListenerFactory.class.getName()),
            Level.WARNING,
            t -> "Cannot shutdown() listener factory %s: %s".formatted(listenerFactory, t))));
  }

  static <E, K> ListenerFactory<E, K> deaf() {
    return from("deaf", k -> Listener.deaf(), () -> {});
  }

  static <E, K> ListenerFactory<E, K> from(
      String name, Function<K, Listener<E>> lFunction, Runnable shutdownRunnable) {
    return new ListenerFactory<>() {
      @Override
      public Listener<E> build(K k) {
        return lFunction.apply(k);
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

  default ListenerFactory<E, K> and(ListenerFactory<? super E, ? super K> other) {
    return all(List.of(this, other));
  }

  default ListenerFactory<E, K> conditional(Predicate<K> predicate) {
    return from(
        "%s[if:%s]".formatted(this, predicate),
        k -> predicate.test(k) ? build(k) : Listener.deaf(),
        this::shutdown);
  }

  default ListenerFactory<E, K> deferred(ExecutorService executorService) {
    return from("%s[deferred]".formatted(this), k -> build(k).deferred(executorService), this::shutdown);
  }

  default <F> ListenerFactory<F, K> forEach(Function<F, Collection<E>> splitter) {
    return from(
        "%s[forEach:%s]".formatted(this, NamedFunction.name(splitter)),
        k -> build(k).forEach(splitter),
        this::shutdown);
  }

  default <F> ListenerFactory<F, K> on(Function<F, E> function) {
    return from("%s[on:%s]".formatted(this, function), k -> build(k).on(function), this::shutdown);
  }

  default ListenerFactory<E, K> onLast() {
    return from("%s[onLast]".formatted(this), k -> build(k).onLast(), this::shutdown);
  }

  default ListenerFactory<E, K> robust() {
    final ListenerFactory<E, K> thisFactory = this;
    final Logger L = Logger.getLogger(Listener.class.getName());
    final AtomicInteger counter = new AtomicInteger(0);
    return new ListenerFactory<>() {
      @Override
      public Listener<E> build(K k) {
        final Listener<E> innerListener = thisFactory.build(k);
        return new Listener<>() {
          @Override
          public void listen(E e) {
            if (counter.get() == -1) {
              L.warning("listen() invoked on a shutdown factory");
              return;
            }
            counter.incrementAndGet();
            try {
              innerListener.listen(e);
            } finally {
              synchronized (counter) {
                counter.decrementAndGet();
                counter.notifyAll();
              }
            }
          }

          @Override
          public Listener<E> deferred(ExecutorService executorService) {
            return new Listener<>() {
              @Override
              public void listen(E e) {
                if (counter.get() == -1) {
                  L.warning("listen() invoked on a shutdown factory");
                  return;
                }
                counter.incrementAndGet();
                executorService.submit(() -> {
                  try {
                    innerListener.listen(e);
                  } catch (RuntimeException ex) {
                    L.warning(String.format(
                        "Listener %s cannot listen() event: %s",
                        innerListener.getClass().getSimpleName(), ex));
                  } finally {
                    synchronized (counter) {
                      counter.decrementAndGet();
                      counter.notifyAll();
                    }
                  }
                });
              }

              @Override
              public void done() {
                counter.incrementAndGet();
                executorService.submit(() -> {
                  try {
                    innerListener.done();
                  } catch (RuntimeException ex) {
                    L.warning(String.format(
                        "Listener (from factory) %s cannot done() event: %s",
                        innerListener.getClass().getSimpleName(), ex));
                  } finally {
                    synchronized (counter) {
                      counter.decrementAndGet();
                      counter.notifyAll();
                    }
                  }
                });
              }
            };
          }

          @Override
          public void done() {
            if (counter.get() == -1) {
              L.warning("listen() invoked on a shutdown factory");
              return;
            }
            counter.incrementAndGet();
            try {
              innerListener.done();
            } finally {
              synchronized (counter) {
                counter.decrementAndGet();
                counter.notifyAll();
              }
            }
          }

          @Override
          public String toString() {
            return innerListener + "[robust]";
          }
        };
      }

      @Override
      public void shutdown() {
        while (counter.get() > 0) {
          synchronized (counter) {
            try {
              counter.wait();
            } catch (InterruptedException e) {
              // ignore
            }
          }
        }
        counter.set(-1);
        thisFactory.shutdown();
      }

      @Override
      public String toString() {
        return thisFactory + "[robust]";
      }
    };
  }

  default void shutdown() {}
}
