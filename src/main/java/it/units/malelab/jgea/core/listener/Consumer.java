package it.units.malelab.jgea.core.listener;

import java.util.Collection;
import java.util.function.Function;

/**
 * @author eric on 2021/01/04 for jgea
 */
@FunctionalInterface
public interface Consumer<G, S, F, O> {
  void consume(Event<? extends G, ? extends S, ? extends F> event);

  default void consume(Collection<? extends S> solutions) {
  }

  default void clear() {
  }

  default O produce() {
    return null;
  }

  @FunctionalInterface
  interface Factory<G, S, F, O> {
    Consumer<G, S, F, O> build();

    default void shutdown() {
    }
  }

  default <Q> Consumer<G, S, F, Q> then(Function<O, Q> function) {
    Consumer<G, S, F, O> thisAccumulator = this;
    return new Consumer<>() {
      @Override
      public void consume(Event<? extends G, ? extends S, ? extends F> event) {
        thisAccumulator.consume(event);
      }

      @Override
      public void consume(Collection<? extends S> solutions) {
        thisAccumulator.consume(solutions);
      }

      @Override
      public void clear() {
        thisAccumulator.clear();
      }

      @Override
      public Q produce() {
        return function.apply(thisAccumulator.produce());
      }

    };
  }
}
