package it.units.malelab.jgea.core.listener.collector2;

import it.units.malelab.jgea.core.listener.Event;
import it.units.malelab.jgea.core.util.Misc;

import java.util.List;
import java.util.function.Function;

/**
 * @author eric on 2021/01/02 for jgea
 */
public interface Collector<G, S, F, O> extends Function<Event<? extends G, ? extends S, ? extends F>, O> {

  String getDefaultFormat();

  String getName();

  default Collector<G, S, F, String> f(String format) {
    Collector<G, S, F, O> thisCollector = this;
    return new Collector<>() {
      @Override
      public String getDefaultFormat() {
        return "%s";
      }

      @Override
      public String getName() {
        return thisCollector.getName();
      }

      @Override
      public String apply(Event<? extends G, ? extends S, ? extends F> event) {
        return String.format(format, thisCollector.apply(event));
      }
    };
  }

  default Collector<G, S, F, String> f() {
    return f(getDefaultFormat());
  }

  static <G, S, F, O> Collector<G, S, F, O> ofOneBest(IndividualCollector<G, S, F, O> individualCollector) {
    return new Collector<G, S, F, O>() {
      @Override
      public String getDefaultFormat() {
        return individualCollector.getDefaultFormat();
      }

      @Override
      public String getName() {
        return "best." + individualCollector.getName();
      }

      @Override
      public O apply(Event<? extends G, ? extends S, ? extends F> event) {
        return individualCollector.apply(Misc.first(event.getOrderedPopulation().firsts()));
      }
    };
  }

  static void main(String[] args) {
    List<Collector<?,?,?,?>> collectors = List.of(
      new Iterations(),
      new ElapsedSeconds(),
      Collector.ofOneBest(IndividualCollector.fitness("%5.3f"))
    );
  }

}
