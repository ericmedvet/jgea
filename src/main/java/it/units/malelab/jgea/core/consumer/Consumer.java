package it.units.malelab.jgea.core.consumer;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author eric on 2021/01/04 for jgea
 */
@FunctionalInterface
public interface Consumer<G, S, F, O> {

  void consume(Event<? extends G, ? extends S, ? extends F> event);

  default void consume(Collection<? extends S> solutions) {
  }

  default O produce() {
    return null;
  }

  default <Q> Consumer<G, S, F, Q> then(Function<O, Q> function) {
    Consumer<G, S, F, O> thisConsumer = this;
    return new Consumer<>() {
      @Override
      public void consume(Event<? extends G, ? extends S, ? extends F> event) {
        thisConsumer.consume(event);
      }

      @Override
      public void consume(Collection<? extends S> solutions) {
        thisConsumer.consume(solutions);
      }

      @Override
      public Q produce() {
        return function.apply(thisConsumer.produce());
      }

    };
  }

  default Consumer<G, S, F, O> deferred(ExecutorService executorService) {
    Consumer<G, S, F, O> thisConsumer = this;
    return new Consumer<>() {
      private final Logger L = Logger.getLogger(Consumer.class.getName());

      @Override
      public void consume(Event<? extends G, ? extends S, ? extends F> event) {
        executorService.submit(() -> {
          try {
            thisConsumer.consume(event);
          } catch (RuntimeException e) {
            L.warning(String.format(
                "Consumer %s cannot consume event: %s",
                thisConsumer.getClass().getSimpleName(),
                e
            ));
          }
        });
      }

      @Override
      public void consume(Collection<? extends S> solutions) {
        try {
          thisConsumer.consume(solutions);
        } catch (RuntimeException e) {
          L.warning(String.format(
              "Consumer %s cannot consume solutions: %s",
              thisConsumer.getClass().getSimpleName(),
              e
          ));
        }
      }

      @Override
      public O produce() {
        return thisConsumer.produce();
      }
    };
  }

  static <G, S, F, O> Consumer<G, S, F, List<O>> of(List<Consumer<? super G, ? super S, ? super F, ? extends O>> consumers) {
    return new Consumer<>() {
      @Override
      public void consume(Event<? extends G, ? extends S, ? extends F> event) {
        consumers.forEach(consumer -> consumer.consume(event));
      }

      @Override
      public void consume(Collection<? extends S> solutions) {
        consumers.forEach(consumer -> consumer.consume(solutions));
      }

      @Override
      public List<O> produce() {
        return consumers.stream().map(Consumer::produce).collect(Collectors.toList());
      }
    };
  }

  @FunctionalInterface
  interface Factory<G, S, F, O> {
    Consumer<G, S, F, O> build();

    default void shutdown() {
    }

    default <Q> Consumer.Factory<G, S, F, Q> then(Function<O, Q> function) {
      Factory<G, S, F, O> thisFactory = this;
      return new Factory<>() {
        @Override
        public Consumer<G, S, F, Q> build() {
          return thisFactory.build().then(function);
        }

        @Override
        public void shutdown() {
          thisFactory.shutdown();
        }
      };
    }

  }

}
