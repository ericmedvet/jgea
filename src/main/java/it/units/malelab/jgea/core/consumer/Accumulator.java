package it.units.malelab.jgea.core.consumer;

import it.units.malelab.jgea.core.util.Table;
import it.units.malelab.jgea.problem.symbolicregression.element.Element;
import it.units.malelab.jgea.representation.sequence.bit.BitString;
import it.units.malelab.jgea.representation.tree.Tree;

import java.util.List;

import static it.units.malelab.jgea.core.consumer.NamedFunctions.*;

public interface Accumulator<I, O> {
  default void take(I i) {
  }

  default O get() {
    return null;
  }

  class Evolver<G, S, F> {
    public void go(Accumulator<Event<? super G, ? super S, ? super F>, ?> accumulator) {
    }
  }

  class Tabler<I, C> implements Accumulator<I, Table<C>> {
    public Tabler(List<NamedFunction<I, C>> functions) {
    }
  }

  static void main(String[] args) {
    NamedFunction<Event<? super BitString, ? super Tree<Element>, ? super Double>, Number> bestSize = size().of(best());
    Evolver<BitString, Tree<Element>, Double> evolver = new Evolver<>();
    Tabler<Event<? super BitString, ? super Tree<Element>, ? super Double>, Number> tabler = new Tabler<>(List.of(
        bestSize,
        size().of(best()),
        size().of(genotype()).of(best())
    ));
    evolver.go(tabler);
    evolver.go(new Tabler<>(List.of(
        bestSize,
        size().of(best()),
        size().of(genotype()).of(best())
    )));
    evolver.go(new Tabler<>(List.of(
        size().of(best()),
        size().of(genotype()).of(best())
    )));
  }

}
