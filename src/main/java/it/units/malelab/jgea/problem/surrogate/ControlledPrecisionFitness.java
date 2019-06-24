/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.problem.surrogate;

import it.units.malelab.jgea.core.function.BiFunction;
import it.units.malelab.jgea.core.function.Function;
import it.units.malelab.jgea.core.function.FunctionException;
import it.units.malelab.jgea.core.function.NonDeterministicFunction;
import it.units.malelab.jgea.core.listener.Listener;
import it.units.malelab.jgea.core.util.Pair;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 *
 * @author eric
 */
public class ControlledPrecisionFitness<A, B> implements NonDeterministicFunction<A, B> {
  
  private final TunablePrecisionFunction<A, B> tunablePrecisionFunction;
  private final BiFunction<A, List<Pair<A, Double>>, Double> controller;
  
  private final List<Pair<A, Double>> history;

  public ControlledPrecisionFitness(TunablePrecisionFunction<A, B> tunablePrecisionFitness, BiFunction<A, List<Pair<A, Double>>, Double> controller) {
    this.tunablePrecisionFunction = tunablePrecisionFitness;
    this.controller = controller;
    history = Collections.synchronizedList(new ArrayList<>()); //TODO: maybe choose a more efficient, size-limited implementation
  }

  @Override
  public B apply(A a, Random random, Listener listener) throws FunctionException {
    double precision = controller.apply(a, history);
    history.add(Pair.build(a, precision));
    return tunablePrecisionFunction.apply(a, precision, random, listener);
  }    
  
}
