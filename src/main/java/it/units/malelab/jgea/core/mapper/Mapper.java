/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core.mapper;

import it.units.malelab.jgea.core.listener.Listener;
import java.io.Serializable;
import java.util.Random;

/**
 *
 * @author eric
 */
public interface Mapper<A, B> extends Serializable {
  
  public B map(A a, Random random, Listener listener) throws MappingException;
  
}
