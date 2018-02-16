/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core;

import it.units.malelab.jgea.core.function.NonDeterministicFunction;
import java.io.Serializable;

/**
 *
 * @author eric
 */
@FunctionalInterface
public interface Problem<S, F> extends Serializable {
  
  public NonDeterministicFunction<S, F> getFitnessFunction();

}
