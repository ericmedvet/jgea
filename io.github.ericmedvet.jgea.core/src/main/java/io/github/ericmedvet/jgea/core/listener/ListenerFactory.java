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

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public interface ListenerFactory<E, K> {

  Listener<E> build(K k);

  static <E, K> ListenerFactory<E, K> all(List<? extends ListenerFactory<? super E, ? super K>> factories) {
    return new ListenerFactory<>() {
      @Override
      public Listener<E> build(K k) {
        return Listener.all(factories.stream().map(f -> f.build(k)).collect(Collectors.toList()));
      }

      @Override
      public void shutdown() {
        factories.forEach(ListenerFactory::shutdown);
      }
    };
  }

  default ListenerFactory<E, K> conditional(Predicate<K> predicate) {
    ListenerFactory<E, K> inner = this;
    return new ListenerFactory<>() {
      @Override
      public Listener<E> build(K k) {
        if (predicate.test(k)) {
          return inner.build(k);
        }
        return Listener.deaf();
      }

      @Override
      public void shutdown() {
        inner.shutdown();
      }
    };
  }

  static <E, K> ListenerFactory<E, K> deaf() {
    return k -> Listener.deaf();
  }

  default ListenerFactory<E, K> and(ListenerFactory<? super E, ? super K> other) {
    ListenerFactory<E, K> inner = this;
    return new ListenerFactory<>() {
      @Override
      public Listener<E> build(K k) {
        return inner.build(k).and(other.build(k));
      }

      @Override
      public void shutdown() {
        inner.shutdown();
        other.shutdown();
      }
    };
  }

  default ListenerFactory<E, K> deferred(ExecutorService executorService) {
    final ListenerFactory<E, K> thisFactory = this;
    final Logger L = Logger.getLogger(ListenerFactory.class.getName());
    return new ListenerFactory<>() {
      @Override
      public Listener<E> build(K k) {
        return thisFactory.build(k).deferred(executorService);
      }

      @Override
      public void shutdown() {
        thisFactory.shutdown();
      }
    };
  }

  default <F> ListenerFactory<F, K> forEach(Function<F, Collection<E>> splitter) {
    ListenerFactory<E, K> thisListenerFactory = this;
    return new ListenerFactory<>() {
      @Override
      public Listener<F> build(K k) {
        return thisListenerFactory.build(k).forEach(splitter);
      }

      @Override
      public void shutdown() {
        thisListenerFactory.shutdown();
      }
    };
  }

  default <F> ListenerFactory<F, K> on(Function<F, E> function) {
    ListenerFactory<E, K> inner = this;
    return new ListenerFactory<>() {
      @Override
      public Listener<F> build(K k) {
        return inner.build(k).on(function);
      }

      @Override
      public void shutdown() {
        inner.shutdown();
      }
    };
  }

  default ListenerFactory<E, K> onLast() {
    ListenerFactory<E, K> thisFactory = this;
    return new ListenerFactory<>() {
      @Override
      public Listener<E> build(K k) {
        return thisFactory.build(k).onLast();
      }

      @Override
      public void shutdown() {
        thisFactory.shutdown();
      }
    };
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
                        "Listener %s cannot done() event: %s",
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
    };
  }

  default void shutdown() {}
}
