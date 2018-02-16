/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core.listener.collector;

import it.units.malelab.jgea.core.function.Function;
import java.util.Collections;
import java.util.Map;

/**
 *
 * @author eric
 */
public class SingleObjectiveBest<G, S> extends Best<G, S, Number> {

  private final String fitnessFormat;

  public SingleObjectiveBest(String fitnessFormat, boolean ancestry, Function<S, Number> validationFitnessMapper) {
    super(ancestry, validationFitnessMapper);
    this.fitnessFormat = fitnessFormat;
  }
 
 @Override
  protected Map<String, String> getFitnessFormattedNames() {
    return Collections.singletonMap("", fitnessFormat);
  }

  @Override
  protected Map<String, Object> getFitnessIndexes(Number fitness) {
    return (Map)Collections.singletonMap("", fitness);
  }

}
