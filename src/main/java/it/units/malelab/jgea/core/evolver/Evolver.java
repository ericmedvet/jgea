/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core.evolver;

import it.units.malelab.jgea.core.Problem;
import it.units.malelab.jgea.core.listener.Listener;
import java.io.Serializable;
import java.util.Collection;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

/**
 *
 * @author eric
 */
public interface Evolver<G, S, F> extends Serializable {
  
  public Collection<S> solve(
          Problem<S, F> problem,
          Random random,
          ExecutorService executor,
          Listener listener) throws InterruptedException, ExecutionException;  
  
}
