package it.units.malelab.jgea.core.listener;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author "Eric Medvet" on 2022/01/29 for jgea
 */
public interface Factory<E, K> {
  Listener<E> build(K k);

  static <E, K> Factory<E, K> all(List<? extends Factory<? super E, ? super K>> factories) {
    return new Factory<>() {
      @Override
      public Listener<E> build(K k) {
        return Listener.all(factories.stream().map(f -> f.build(k)).collect(Collectors.toList()));
      }

      @Override
      public void shutdown() {
        factories.forEach(Factory::shutdown);
      }
    };
  }

  static <E, K> Factory<E, K> deaf() {
    return k -> Listener.deaf();
  }

  static <F, E, K> Factory<F, K> forEach(Function<F, Collection<E>> splitter, Factory<E, K> factory) {
    return new Factory<>() {
      @Override
      public Listener<F> build(K k) {
        return Listener.forEach(splitter, factory.build(k));
      }

      @Override
      public void shutdown() {
        factory.shutdown();
      }
    };
  }

  default <F> Factory<F, K> on(Function<F, E> function) {
    Factory<E, K> inner = this;
    return new Factory<>() {
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

  default Factory<E, K> onLast() {
    Factory<E, K> thisFactory = this;
    return new Factory<>() {
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

  default Factory<E, K> robust() {
    final Factory<E, K> thisFactory = this;
    final Logger L = Logger.getLogger(Listener.class.getName());
    final AtomicInteger counter = new AtomicInteger(0);
    return new Factory<E, K>() {
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
                    L.warning(String.format("Listener %s cannot listen() event: %s",
                        innerListener.getClass().getSimpleName(),
                        ex
                    ));
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
                    L.warning(String.format("Listener %s cannot done() event: %s",
                        innerListener.getClass().getSimpleName(),
                        ex
                    ));
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
              //ignore
            }
          }
        }
        counter.set(-1);
        thisFactory.shutdown();
      }
    };
  }

  default void shutdown() {
  }

}
