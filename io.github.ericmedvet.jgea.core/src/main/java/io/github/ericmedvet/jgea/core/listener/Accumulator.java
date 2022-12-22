package io.github.ericmedvet.jgea.core.listener;


import java.util.function.Function;

public interface Accumulator<E, O> extends Listener<E> {

  O get();

  static <E> Accumulator<E, E> last() {
    return new Accumulator<>() {
      E last;

      @Override
      public E get() {
        return last;
      }

      @Override
      public void listen(E e) {
        last = e;
      }
    };
  }

  default <Q> Accumulator<E, Q> then(Function<O, Q> function) {
    Accumulator<E, O> thisAccumulator = this;
    return new Accumulator<>() {
      @Override
      public Q get() {
        return function.apply(thisAccumulator.get());
      }

      @Override
      public void listen(E e) {
        thisAccumulator.listen(e);
      }

      @Override
      public void done() {
        thisAccumulator.done();
      }
    };
  }

  default Listener<E> withAutoGet() {
    Accumulator<E, O> thisAccumulator = this;
    return new Listener<>() {
      @Override
      public void listen(E e) {
        thisAccumulator.listen(e);
      }

      @Override
      public void done() {
        thisAccumulator.get();
        thisAccumulator.done();
      }
    };
  }
}
