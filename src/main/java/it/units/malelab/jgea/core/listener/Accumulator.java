package it.units.malelab.jgea.core.listener;


import java.util.function.Function;

public interface Accumulator<E, O> extends Listener<E> {
  interface Factory<E, O, K> extends it.units.malelab.jgea.core.listener.Factory<E, K> {
    Accumulator<E, O> build(K k);

    static <E, K> Factory<E, E, K> last() {
      return k -> new Accumulator<>() {
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

    default <P> Factory<E, P, K> then(Function<? super O, P> function) {
      final Factory<E, O, K> thisFactory = this;
      return new Factory<>() {
        @Override
        public Accumulator<E, P> build(K k) {
          Accumulator<E, O> accumulator = thisFactory.build(k);
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

  }

  O get();

}
