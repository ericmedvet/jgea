package it.units.malelab.core.listener;


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
    Accumulator<E, O> inner = this;
    return new Accumulator<>() {
      @Override
      public Q get() {
        return function.apply(inner.get());
      }

      @Override
      public void listen(E e) {
        inner.listen(e);
      }

      @Override
      public void done() {
        inner.done();
      }
    };
  }
}
