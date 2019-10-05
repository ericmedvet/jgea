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
public class SegmentCrossover implements Crossover<Sequence<Double>> {

  @Override
  public Sequence<Double> recombine(Sequence<Double> parent1, Sequence<Double> parent2, Random random, Listener listener) {
    Sequence<Double> child = parent1.clone();
    for (int i = 0; i<Math.min(child.size(), parent2.size()); i++) {
      double v1 = parent1.get(i);
      double v2 = parent2.get(i);
      double v = v1+(v2-v1)*random.nextDouble();
      child.set(i, v);
    }
    return child;
  }
  
}
