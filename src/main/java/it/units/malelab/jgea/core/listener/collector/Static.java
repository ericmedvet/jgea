/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core.listener.collector;

import it.units.malelab.jgea.core.listener.event.EvolutionEvent;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 * @author eric
 */
public class Static implements DataCollector {
  
  private final Map<String, Object> values;

  public Static(Map<String, Object> values) {
    this.values = values;
  }

  @Override
  public List<Item> collect(EvolutionEvent evolutionEvent) {
    return values.entrySet().stream()
            .map(entry -> new Item<>(entry.getKey(), entry.getValue(), "%"+entry.getValue().toString().length()+"."+entry.getValue().toString().length()+"s"))
            .collect(Collectors.toList());
  }    
  
}
