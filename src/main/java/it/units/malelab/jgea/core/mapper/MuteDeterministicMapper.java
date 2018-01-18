/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core.mapper;

import it.units.malelab.jgea.core.listener.Listener;

/**
 *
 * @author eric
 */
public abstract class MuteDeterministicMapper<A, B> extends DeterministicMapper<A, B> {

  @Override
  public B map(A a, Listener listener) throws MappingException {
    return map(a);
  }
  
  public abstract B map(A a) throws MappingException;
    
}
