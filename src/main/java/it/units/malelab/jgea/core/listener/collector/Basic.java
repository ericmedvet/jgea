/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core.listener.collector;

import it.units.malelab.jgea.core.listener.event.EvolutionEvent;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author eric
 */
public class Basic implements DataCollector {

  @Override
  public List<Item> collect(EvolutionEvent evolutionEvent) {
    return Arrays.asList(
            new Item<>("iterations", evolutionEvent.getIteration(), "%4d"),
            new Item<>("births", evolutionEvent.getBirths(), "%8d"),
            new Item<>("fitness.evaluations", evolutionEvent.getFitnessEvaluations(), "%6d"),
            new Item<>("elapsed.sec", (double)evolutionEvent.getElapsedMillis()/1000d, "%6.1f")
    );
  }

}
