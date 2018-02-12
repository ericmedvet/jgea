/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core.fitness;

import it.units.malelab.jgea.core.listener.Listener;
import it.units.malelab.jgea.core.mapper.BoundMapper;
import it.units.malelab.jgea.core.mapper.DeterministicMapper;
import it.units.malelab.jgea.core.mapper.MappingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author eric
 */
public abstract class CaseBasedFitness<S, O, OF, AF> implements BoundMapper<S, AF> {
  
  private final List<O> observations;
  private final BoundMapper<List<OF>, AF> aggregateMapper;

  public CaseBasedFitness(List<O> observations, BoundMapper<List<OF>, AF> aggregateMapper) {
    this.observations = observations;
    this.aggregateMapper = aggregateMapper;
  }

  public List<O> getObservations() {
    return observations;
  }

  public BoundMapper<List<OF>, AF> getAggregateMapper() {
    return aggregateMapper;
  }
  
  protected abstract OF fitnessOfCase(S solution, O observation);

  @Override
  public AF map(S solution, Random random, Listener listener) throws MappingException {
    List<OF> observationFitnesses = new ArrayList<>(observations.size());
    for (O observation : observations) {
      observationFitnesses.add(fitnessOfCase(solution, observation));
    }
    return aggregateMapper.map(observationFitnesses, random, listener);
  }

  @Override
  public AF worstValue() {
    return aggregateMapper.worstValue();
  }

  @Override
  public AF bestValue() {
    return aggregateMapper.bestValue();
  }
  
}
