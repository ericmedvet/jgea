package it.units.malelab.jgea.core.listener.collector;

import it.units.malelab.jgea.core.Individual;
import it.units.malelab.jgea.core.listener.Event;
import it.units.malelab.jgea.core.util.TextPlotter;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author eric on 2020/12/13 for jgea
 */
public class Histogram<G, S, F> implements DataCollector<G, S, F> {

  private final Function<Individual<? extends G, ? extends S, ? extends F>, ? extends Number> function;
  private final String name;
  private final int bins;

  public Histogram(Function<Individual<? extends G, ? extends S, ? extends F>, ? extends Number> function, String name, int bins) {
    this.function = function;
    this.name = name;
    this.bins = bins;
  }

  @Override
  public List<Item> collect(Event<? extends G, ? extends S, ? extends F> event) {
    List<Number> values = event.getOrderedPopulation().all().stream()
        .map(function)
        .collect(Collectors.toList());
    return List.of(
        new Item("population." + name + ".histogram", TextPlotter.histogram(values, bins), "%" + bins + "s")
    );
  }
}
