/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core.function;

import it.units.malelab.jgea.core.listener.Listener;
import it.units.malelab.jgea.core.listener.ListenerUtils;
import java.io.Serializable;
import java.util.Random;

/**
 *
 * @author eric
 */
public interface NonDeterministicFunction<A, B> extends Serializable {

  public B apply(A a, Random random, Listener listener) throws FunctionException;

  default B apply(A a, Random random) throws FunctionException {
    return apply(a, random, ListenerUtils.deafListener());
  }

}
