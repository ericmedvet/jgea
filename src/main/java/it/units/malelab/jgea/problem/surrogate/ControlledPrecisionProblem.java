/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.problem.surrogate;

import it.units.malelab.jgea.core.Problem;
import it.units.malelab.jgea.core.function.BiFunction;
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
public class ControlledPrecisionProblem<S, F> implements Problem<S, F> {

  private final TunablePrecisionProblem<S, F> innerProblem;
  private final BiFunction<S, List<Pair<S, Double>>, Double> controller;

  private final List<Pair<S, Double>> history;
  private final NonDeterministicFunction<S, F> nonDeterministicFunction;

  private double overallCost;

  public ControlledPrecisionProblem(TunablePrecisionProblem<S, F> innerProblem, BiFunction<S, List<Pair<S, Double>>, Double> controller) {
    this.innerProblem = innerProblem;
    this.controller = controller;
    history = Collections.synchronizedList(new ArrayList<>()); //TODO: maybe choose a more efficient, size-limited implementation
    overallCost = 0d;
    nonDeterministicFunction = (S s, Random random, Listener listener) -> { //TODO: make thread safe
      double precision = controller.apply(s, history);
      overallCost = overallCost + (1d-precision);
      history.add(Pair.build(s, precision));
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

  public synchronized double overallCost() {
    return overallCost;
  }

}
