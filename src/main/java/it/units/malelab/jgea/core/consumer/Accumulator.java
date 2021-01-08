package it.units.malelab.jgea.core.consumer;

import it.units.malelab.jgea.core.Individual;
import it.units.malelab.jgea.core.evolver.Evolver;
import it.units.malelab.jgea.core.order.DAGPartiallyOrderedCollection;
import it.units.malelab.jgea.core.order.PartialComparator;
import it.units.malelab.jgea.core.order.PartiallyOrderedCollection;
import it.units.malelab.jgea.core.util.Table;

import java.util.List;

import static it.units.malelab.jgea.core.consumer.NamedFunctions.*;

public interface Accumulator<I, O> {
  default void take(I i) {
  }

  default O get() {
    return null;
  }

  class Evolver2<G, S, F> {
    private final G g;
    private final S s;
    private final F f;

    public Evolver2(G g, S s, F f) {
      this.g = g;
      this.s = s;
      this.f = f;
    }

    public void go(Accumulator<Event<? super G, ? super S, ? super F>, ?> accumulator) {
      Evolver.State state = new Evolver.State();
      PartiallyOrderedCollection<Individual<G, S, F>> pop = new DAGPartiallyOrderedCollection<>((k1, k2) -> PartialComparator.PartialComparatorOutcome.NOT_COMPARABLE);
      pop.add(new Individual<>(g, s, f, 0));
      accumulator.take(new Event<>(state, pop));
    }
  }

  class Tabler<I, C> implements Accumulator<I, Table<C>> {
    private final List<NamedFunction<? super I, ? extends C>> functions;

    public Tabler(List<NamedFunction<? super I, ? extends C>> functions) {
      this.functions = functions;
    }

    @Override
    public void take(I i) {
      functions.forEach(f -> System.out.printf("%s: " + f.getFormat() + "%n", f.getName(), f.apply(i)));
    }
  }

  static void main(String[] args) {
    NamedFunction<Event<? super String, ? super String, ? super Double>, ? extends Number> bestSize = size().of(best());
    Evolver2<String, String, Double> evolver = new Evolver2<>("g", "s", 2.1d);
    Tabler<Event<? super String, ? super String, ? super Double>, ? extends Number> tabler = new Tabler<>(List.of(
        size().of(solution()).of(best()),
        size().of(genotype()).of(best())
    ));
    evolver.go(tabler);
    evolver.go(new Tabler<>(List.of(
        size().of(solution()).of(best()),
        size().of(genotype()).of(best()),
        as(Double.class).of(fitness()).of(best()),
        max(Double::compare).of(each(as(Double.class).of(fitness()))).of(all())
    )));
  }

}
