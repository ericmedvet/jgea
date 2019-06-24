/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.problem.surrogate;

import it.units.malelab.jgea.core.Problem;

/**
 *
 * @author eric
 */
public interface TunablePrecisionProblem<S, F> extends Problem<S, F> {
  
  public TunablePrecisionFunction<S, F> getTunablePrecisionFitnessFunction();
  
}
