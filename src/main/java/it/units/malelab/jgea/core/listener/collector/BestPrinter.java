/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core.listener.collector;

import it.units.malelab.jgea.core.Individual;
import it.units.malelab.jgea.core.function.Function;
import it.units.malelab.jgea.core.listener.event.EvolutionEvent;
import it.units.malelab.jgea.core.function.FunctionException;
import it.units.malelab.jgea.core.util.Misc;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author eric
 */
public class BestPrinter<S> implements DataCollector {
  
  private final Function<S, String> printer;
  private final String format;

  public BestPrinter(Function<S, String> printer, String format) {
    this.printer = printer;
    this.format = format;
  }

  @Override
  public Map<String, Object> collect(EvolutionEvent evolutionEvent) {
    List<Collection<Individual>> rankedPopulation = new ArrayList<>((List)evolutionEvent.getRankedPopulation());
    Individual best = Misc.first(rankedPopulation.get(0));
    if (printer!=null) {
      try {
        return (Map)Collections.singletonMap("best.solution", printer.apply((S)best.getSolution()));
      } catch (FunctionException ex) {
        Logger.getLogger(BestPrinter.class.getName()).log(Level.WARNING, "Cannot print best.", ex);
      }
    }
    return (Map)Collections.singletonMap("best.solution", best.getSolution().toString());
  }

  @Override
  public Map<String, String> getFormattedNames() {
    return Collections.singletonMap("best.solution", format);
  }  
    
}
