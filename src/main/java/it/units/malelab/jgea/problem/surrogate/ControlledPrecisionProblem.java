/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.problem.surrogate;

import it.units.malelab.jgea.lab.PrecisionController;
import it.units.malelab.jgea.core.Problem;
import it.units.malelab.jgea.core.function.NonDeterministicFunction;
import it.units.malelab.jgea.core.listener.Listener;
import java.util.Random;

/**
 *
 * @author eric
 */
public class ControlledPrecisionProblem<S, F> implements Problem<S, F> {

  private final TunablePrecisionProblem<S, F> innerProblem;
  private final PrecisionController<S> controller;

  private final NonDeterministicFunction<S, F> nonDeterministicFunction;

  public ControlledPrecisionProblem(TunablePrecisionProblem<S, F> innerProblem, PrecisionController<S> controller) {
    this.innerProblem = innerProblem;
    this.controller = controller;
    nonDeterministicFunction = (S s, Random random, Listener listener) -> {
      double precision = controller.apply(s);
      return innerProblem.getTunablePrecisionFitnessFunction().apply(s, precision, random, listener);
    };
  }

  @Override
  public NonDeterministicFunction<S, F> getFitnessFunction() {
    return nonDeterministicFunction;
  }

  public TunablePrecisionProblem<S, F> getInnerProblem() {
    return innerProblem;
  }

  public PrecisionController<S> getController() {
    return controller;
  }

}
