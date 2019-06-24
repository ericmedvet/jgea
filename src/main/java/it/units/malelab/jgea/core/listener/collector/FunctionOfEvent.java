/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core.listener.collector;

import it.units.malelab.jgea.core.function.Function;
import it.units.malelab.jgea.core.listener.event.EvolutionEvent;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author eric
 */
public class FunctionOfEvent<T> implements DataCollector {
  
  private String name;
  private Function<EvolutionEvent, T> function;
  private String format;

  public FunctionOfEvent(String name, Function<EvolutionEvent, T> function, String format) {
    this.name = name;
    this.function = function;
    this.format = format;
  }
  
  @Override
  public List<Item> collect(EvolutionEvent evolutionEvent) {
    return Arrays.asList(
            new Item<>(name, function.apply(evolutionEvent), format)
    );
  }

}
