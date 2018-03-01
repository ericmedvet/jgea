/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core.listener.collector;

import it.units.malelab.jgea.core.listener.event.EvolutionEvent;
import java.io.Serializable;
import java.util.List;

/**
 *
 * @author eric
 */
@FunctionalInterface
public interface DataCollector extends Serializable {
  
  public List<Item> collect(EvolutionEvent evolutionEvent);

}
