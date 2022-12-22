package io.github.ericmedvet.jgea.core.listener;

import java.util.function.BiFunction;
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

}
