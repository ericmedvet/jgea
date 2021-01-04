package it.units.malelab.jgea.core.listener;

import java.util.Collection;
import java.util.function.Function;

/**
 * @author eric on 2021/01/04 for jgea
 */
public interface Accumulator<G, S, F, O> extends Listener<G, S, F> {
  void clear();

  O get();

  @Override
  default void listenSolutions(Collection<? extends S> solutions) {
    clear();
  }

  default <Q> Accumulator<G, S, F, Q> on(Function<O, Q> function) {
    Accumulator<G, S, F, O> thisAccumulator = this;
    return new Accumulator<>() {
      @Override
      public void clear() {
        thisAccumulator.clear();
      }

      @Override
      public Q get() {
        return function.apply(thisAccumulator.get());
      }

      @Override
      public void listen(Event<? extends G, ? extends S, ? extends F> event) {
        thisAccumulator.listen(event);
      }
    };
  }
}
