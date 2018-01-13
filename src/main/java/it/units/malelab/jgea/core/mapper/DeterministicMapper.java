/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core.mapper;

import it.units.malelab.jgea.core.listener.Listener;
import java.util.List;
import java.util.Random;

/**
 *
 * @author eric
 */
public abstract class DeterministicMapper<A, B> implements Mapper<A, B> {

  @Override
  public B map(A a, Random random, Listener listener) throws MappingException {
    return map(a, listener);
  }
  
  protected abstract B map(A a, Listener listener) throws MappingException;
  
}
