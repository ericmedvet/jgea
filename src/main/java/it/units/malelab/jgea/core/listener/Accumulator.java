package it.units.malelab.jgea.core.listener;


import java.util.function.Function;

public interface Accumulator<E, O> extends Listener<E> {
  O get();

  interface Factory<E, O> extends Listener.Factory<E> {
    Accumulator<E, O> build();

    default <P> Factory<E, P> then(Function<? super O, P> function) {
      final Factory<E, O> thisFactory = this;
      return new Factory<>() {
        @Override
        public Accumulator<E, P> build() {
          Accumulator<E, O> accumulator = thisFactory.build();
          return new Accumulator<>() {
            @Override
            public P get() {
              return function.apply(accumulator.get());
            }

            @Override
            public void listen(E e) {
              accumulator.listen(e);
            }
          };
        }

        @Override
        public void shutdown() {
          thisFactory.shutdown();
        }
      };
    }

    static <E> Factory<E, E> last() {
      return () -> new Accumulator<>() {
        private E lastE;

        @Override
        public E get() {
          return lastE;
        }

        @Override
        public void listen(E e) {
          lastE = e;
        }
      };
    }

  }

}
