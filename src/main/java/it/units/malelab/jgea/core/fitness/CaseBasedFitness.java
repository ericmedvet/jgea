/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core.fitness;

import it.units.malelab.jgea.core.function.BiFunction;
import it.units.malelab.jgea.core.function.Function;
import it.units.malelab.jgea.core.listener.Listener;
import it.units.malelab.jgea.core.function.FunctionException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author eric
 */
public class CaseBasedFitness<S, O, OF, AF> implements Function<S, AF> {
    
  private final List<O> observations;
  private final BiFunction<S, O, OF> observationFitnessFunction;
  private final Function<List<OF>, AF> aggregateFunction;

  public CaseBasedFitness(List<O> observations, BiFunction<S, O, OF> observationFitnessFunction, Function<List<OF>, AF> aggregateFunction) {
    this.observations = observations;
    this.observationFitnessFunction = observationFitnessFunction;
    this.aggregateFunction = aggregateFunction;
  }

  public List<O> getObservations() {
    return observations;
  }

  @Override
  public AF apply(S solution, Listener listener) throws FunctionException {
    List<OF> observationFitnesses = new ArrayList<>(observations.size());
    for (O observation : observations) {
      observationFitnesses.add(observationFitnessFunction.apply(solution, observation, listener));
    }
    return aggregateFunction.apply(observationFitnesses, listener);
  }

  public BiFunction<S, O, OF> getObservationFitnessFunction() {
    return observationFitnessFunction;
  }

  public Function<List<OF>, AF> getAggregateFunction() {
    return aggregateFunction;
  }

}
