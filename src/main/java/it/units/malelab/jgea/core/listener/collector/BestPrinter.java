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
import java.util.Collections;
import java.util.List;

/**
 *
 * @author eric
 */
public class BestPrinter<S> implements DataCollector {
  
  private final Function<S, String> printer;
  private final String format;

  public BestPrinter(Function<S, String> printer, String format) {
    if (printer==null) {
      printer = (s, l) -> s.toString();
    }
    this.printer = printer;   
    this.format = format;
  }
  
  public BestPrinter(String format) {
    this(null, format);
  }
  
  public BestPrinter() {
    this(null, "%s");
  }

  @Override
  public List<Item> collect(EvolutionEvent evolutionEvent) {
    List<Collection<Individual>> rankedPopulation = new ArrayList<>((List)evolutionEvent.getRankedPopulation());
    Individual best = Misc.first(rankedPopulation.get(0));
    return Collections.singletonList(new Item<>("best.solution", printer.apply((S)best.getSolution()), format));
  }  
    
}
