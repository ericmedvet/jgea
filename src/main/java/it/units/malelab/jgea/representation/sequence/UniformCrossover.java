/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.representation.sequence;

import it.units.malelab.jgea.core.listener.Listener;
import it.units.malelab.jgea.core.operator.Crossover;
import java.util.Random;

/**
 *
 * @author Eric Medvet <eric.medvet@gmail.com>
 */
public class UniformCrossover<T, S extends Sequence<T>> implements Crossover<S>{

  @Override
  public S recombine(S parent1, S parent2, Random random, Listener listener) {
    S child = (S)parent1.clone();
    for (int i = 0; i<Math.min(child.size(), parent2.size()); i++) {
      if (random.nextBoolean()) {
        child.set(i, parent2.get(i));
      }
    }
    return child;    
  }  
  
}
