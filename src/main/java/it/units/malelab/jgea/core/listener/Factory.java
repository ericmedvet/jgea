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
public interface Factory<E> {
  Listener<E> build();

  static <E> Factory<E> all(List<? extends Factory<? super E>> factories) {
    return new Factory<>() {
      @Override
      public Listener<E> build() {
        return Listener.all(factories.stream().map(Factory::build).collect(Collectors.toList()));
      }

      @Override
      public void shutdown() {
        factories.forEach(Factory::shutdown);
      }
    };
  }

  static <E> Factory<E> deaf() {
    return Listener::deaf;
  }

  static <F, E> Factory<F> forEach(Function<F, Collection<E>> splitter, Factory<E> factory) {
    return new Factory<>() {
      @Override
      public Listener<F> build() {
        return Listener.forEach(splitter, factory.build());
      }

      @Override
      public void shutdown() {
        factory.shutdown();
      }
    };
  }

  default <F> Factory<F> on(Function<F, E> function) {
    Factory<E> inner = this;
    return new Factory<>() {
      @Override
      public Listener<F> build() {
        return inner.build().on(function);
      }

      @Override
      public void shutdown() {
        inner.shutdown();
      }
    };
  }

  default Factory<E> onLast() {
    Factory<E> thisFactory = this;
    return new Factory<>() {
      @Override
      public Listener<E> build() {
        return thisFactory.build().onLast();
      }

      @Override
      public void shutdown() {
        thisFactory.shutdown();
      }
    };
  }

  default Factory<E> robust() {
    final Factory<E> thisFactory = this;
    final Logger L = Logger.getLogger(Listener.class.getName());
    final AtomicInteger counter = new AtomicInteger(0);
    return new Factory<E>() {
      @Override
      public Listener<E> build() {
        final Listener<E> innerListener = thisFactory.build();
        return new Listener<E>() {
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
            return new Listener<E>() {
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
                    L.warning(String.format(
                        "Listener %s cannot done() event: %s",
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
