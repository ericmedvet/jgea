package it.units.malelab.jgea.core.listener;

public interface AccumulatorFactory<E, O, K> extends ListenerFactory<E, K> {
  Accumulator<E, O> build(K k);
}
