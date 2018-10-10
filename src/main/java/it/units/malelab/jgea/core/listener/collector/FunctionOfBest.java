/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core.listener.collector;

import it.units.malelab.jgea.core.Individual;
import it.units.malelab.jgea.core.function.Function;
import it.units.malelab.jgea.core.listener.event.EvolutionEvent;
import it.units.malelab.jgea.core.util.Misc;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author eric
 */
public class FunctionOfBest<S, V> implements DataCollector {

  private final String name;
  private final Function<S, V> function;
  private final Function<V, List<Item>> splitter;

  public FunctionOfBest(String name, Function<S, V> function, Function<V, List<Item>> splitter, long cacheSize) {
    this.name = name;
    this.function = cacheSize > 0 ? function.cached(cacheSize) : function;
    this.splitter = splitter;
  }

  public FunctionOfBest(String name, Function<S, V> function, long cacheSize, String... formats) {
    this.name = name;
    this.function = cacheSize > 0 ? function.cached(cacheSize) : function;
    if (formats.length > 1) {
      splitter = Item.fromMultiobjective((Function) function, formats);
    } else {
      splitter = Item.fromSingle(function, formats[0]);
    }
  }

  @Override
  public List<Item> collect(EvolutionEvent evolutionEvent) {
    List<Collection<Individual>> rankedPopulation = new ArrayList<>((List) evolutionEvent.getRankedPopulation());
    Individual best = Misc.first(rankedPopulation.get(0));
    return splitter.apply(function.apply((S) best.getSolution())).stream()
            .map(item -> item.prefixed(name))
            .collect(Collectors.toList());
  }

}
