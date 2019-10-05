/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core.operator;

import it.units.malelab.jgea.core.Sequence;
import it.units.malelab.jgea.core.listener.Listener;
import java.util.Random;

/**
 *
 * @author Eric Medvet <eric.medvet@gmail.com>
 */
public class UniformCrossover<T> implements Crossover<Sequence<T>>{

  @Override
  public Sequence<T> recombine(Sequence<T> parent1, Sequence<T> parent2, Random random, Listener listener) {
    Sequence<T> child = parent1.clone();
    for (int i = 0; i<Math.min(child.size(), parent2.size()); i++) {
      if (random.nextBoolean()) {
        child.set(i, parent2.get(i));
      }
    }
    return child;    
  }
  
}
