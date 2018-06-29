/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core.listener.collector;

import it.units.malelab.jgea.core.listener.event.EvolutionEvent;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author eric
 */
public class Suffix implements DataCollector {
  
  private final String suffix;
  private final DataCollector collector;

  public Suffix(String suffix, DataCollector collector) {
    this.suffix = suffix;
    this.collector = collector;
  }

  @Override
  public List<Item> collect(EvolutionEvent evolutionEvent) {
    List<Item> items = collector.collect(evolutionEvent);
    List<Item> renamedItems = new ArrayList<>(items.size());
    for (Item item : items) {
      renamedItems.add(new Item(item.getName()+"."+suffix, item.getValue(), item.getFormat()));
    }
    return renamedItems;
  }    
  
}
