/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core;

import it.units.malelab.jgea.core.mapper.BoundMapper;
import java.io.Serializable;

/**
 *
 * @author eric
 */
public class Problem<S, F> implements Serializable {
  
  private final BoundMapper<S, F> fitnessMapper;

  public Problem(BoundMapper<S, F> fitnessMapper) {
    this.fitnessMapper = fitnessMapper;
  }

  public BoundMapper<S, F> getFitnessMapper() {
    return fitnessMapper;
  }

}
