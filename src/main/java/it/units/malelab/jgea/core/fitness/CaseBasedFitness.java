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
import it.units.malelab.jgea.core.util.Pair;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author eric
 */
public class CaseBasedFitness<S, O, OF, AF> extends DeterministicMapper<S, AF> implements BoundMapper<S, AF> {
  
  private final List<O> observations;
  private final DeterministicMapper<Pair<S, O>, OF> observationMapper;
  private final BoundMapper<List<OF>, AF> aggregateMapper;
  private final Random random;

  public CaseBasedFitness(List<O> observations, DeterministicMapper<Pair<S, O>, OF> observationMapper, BoundMapper<List<OF>, AF> aggregateMapper) {
    this.observations = observations;
    this.observationMapper = observationMapper;
    this.aggregateMapper = aggregateMapper;
    this.random = new Random(1);
  }

  public List<O> getObservations() {
    return observations;
  }

  public BoundMapper<List<OF>, AF> getAggregateMapper() {
    return aggregateMapper;
  }

  @Override
  public AF map(S solution, Listener listener) throws MappingException {
    List<OF> observationFitnesses = new ArrayList<>(observations.size());
    for (O observation : observations) {
      observationFitnesses.add(observationMapper.map(Pair.build(solution, observation), listener));
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
