/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.representation.sequence.numeric;

import it.units.malelab.jgea.core.IndependentFactory;
import it.units.malelab.jgea.representation.sequence.FixedLengthSequence;
import it.units.malelab.jgea.representation.sequence.Sequence;
import java.util.Random;

/**
 *
 * @author Eric Medvet <eric.medvet@gmail.com>
 */
public class DoubleSequenceFactory implements IndependentFactory<Sequence<Double>> {
  
  private final double min;
  private final double max;
  private final int length;

  public DoubleSequenceFactory(double min, double max, int length) {
    this.min = min;
    this.max = max;
    this.length = length;
  }

  @Override
  public Sequence<Double> build(Random random) {
    FixedLengthSequence<Double> sequence = new FixedLengthSequence<>(length, 0d);
    for (int i = 0; i<sequence.size(); i++) {
      sequence.set(i, min+(max-min)*random.nextDouble());
    }
    return sequence;
  }
  
}
