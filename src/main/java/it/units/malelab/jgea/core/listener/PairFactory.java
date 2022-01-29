package it.units.malelab.jgea.core.listener;

import it.units.malelab.jgea.core.util.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

/**
 * @author "Eric Medvet" on 2022/01/29 for jgea
 */
public interface PairFactory<E, C> extends Factory<Pair<E, C>> {

  static <E, C> PairFactory<E, C> all(List<? extends PairFactory<? super E, ? super C>> factories) {
    return new PairFactory<>() {

      @SuppressWarnings({"rawtypes", "unchecked", "CollectionAddAllCanBeReplacedWithConstructor"})
      @Override
      public Listener<Pair<E, C>> build() {
        Function<Pair<E, C>, Pair<? super E, ? super C>> f = p -> Pair.of(p.first(), p.second());
        List<Listener<? super Pair<E, C>>> listeners = new ArrayList<>();
        listeners.addAll((List) factories.stream()
            .map(factory -> factory.build().on((Function) f))
            .toList());
        return Listener.all(listeners);
      }

      @Override
      public void shutdown() {
        factories.forEach(Factory::shutdown);
      }
    };
  }

  static <E, C> PairFactory<E, C> deaf() {
    return Listener::deaf;
  }

  static void main(String[] args) {
    PairFactory<String, Number> pf1 = () -> p -> System.out.printf("F1:%3.0f-%s%n", p.second().floatValue(), p.first());
    PairFactory<Object, Integer> pf2 = () -> p -> System.out.printf("F2:%3d-%s%n", p.second(), p.first().toString());
    PairFactory<String, Integer> pfa = PairFactory.all(List.of(pf1, pf2)).onLast();
    for (int i : new int[]{1, 2}) {
      Listener<String> l = pfa.build(i);
      Arrays.stream("abc".split("")).forEach(l::listen);
      l.done();
    }

  }

  default Listener<E> build(C constant) {
    Listener<Pair<E, C>> inner = build();
    return new Listener<>() {
      @Override
      public void listen(E e) {
        inner.listen(Pair.of(e, constant));
      }

      @Override
      public void done() {
        inner.done();
      }
    };
  }

  @Override
  default PairFactory<E, C> onLast() {
    PairFactory<E, C> inner = this;
    return new PairFactory<>() {
      @Override
      public Listener<Pair<E, C>> build() {
        return inner.build().onLast();
      }

      @Override
      public void shutdown() {
        inner.shutdown();
      }
    };
  }

}
