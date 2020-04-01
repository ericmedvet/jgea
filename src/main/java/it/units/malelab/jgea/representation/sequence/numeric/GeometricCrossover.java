/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.representation.sequence.numeric;

import com.google.common.collect.Range;
import it.units.malelab.jgea.representation.sequence.Sequence;
import it.units.malelab.jgea.core.listener.Listener;
import it.units.malelab.jgea.core.operator.Crossover;
import java.util.Random;

/**
 *
 * @author Eric Medvet <eric.medvet@gmail.com>
 */
public class GeometricCrossover implements Crossover<Sequence<Double>> {
  
  private final Range<Double> range;

  public GeometricCrossover(Range<Double> range) {
    this.range = range;
  }

  public GeometricCrossover() {
    this(Range.closedOpen(0d, 1d));
  }

  @Override
  public Sequence<Double> recombine(Sequence<Double> parent1, Sequence<Double> parent2, Random random, Listener listener) {
    Sequence<Double> child = parent1.clone();
    for (int i = 0; i<Math.min(child.size(), parent2.size()); i++) {
      double v1 = parent1.get(i);
      double v2 = parent2.get(i);
      double extent = range.upperEndpoint()-range.lowerEndpoint();
      double v = v1+(v2-v1)*(random.nextDouble()*extent+range.lowerEndpoint());
      child.set(i, v);
    }
    return child;
  }
  
}
