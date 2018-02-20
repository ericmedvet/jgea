/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core.fitness;

import it.units.malelab.jgea.core.function.BiFunction;
import it.units.malelab.jgea.core.function.ComposedFunction;
import it.units.malelab.jgea.core.function.Function;
import it.units.malelab.jgea.core.listener.Listener;
import it.units.malelab.jgea.core.function.FunctionException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author eric
 */
public class CaseBasedFitness<S, O, OF, AF> implements ComposedFunction<S, List<OF>, AF> {
    
  private final List<O> observations;
  private final BiFunction<S, O, OF> observationFunction;
  private final Function<List<OF>, AF> aggregateFunction;

  public CaseBasedFitness(List<O> observations, BiFunction<S, O, OF> observationFunction, Function<List<OF>, AF> aggregateFunction) {
    this.observations = observations;
    this.observationFunction = observationFunction;
    this.aggregateFunction = aggregateFunction;
  }

  public List<O> getObservations() {
    return observations;
  }

  @Override
  public Function<S, List<OF>> first() {
    return (S s, Listener l) -> observations.stream()
            .map((O o) -> observationFunction.apply(s, o, l))
            .collect(Collectors.toList());
  }

  @Override
  public Function<List<OF>, AF> second() {
    return aggregateFunction;
  }    

  public BiFunction<S, O, OF> observationFunction() {
    return observationFunction;
  }

}
