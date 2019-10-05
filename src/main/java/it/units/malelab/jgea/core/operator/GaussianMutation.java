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
public class GaussianMutation implements Mutation<Sequence<Double>> {

  private final double sigma;

  public GaussianMutation(double sigma) {
    this.sigma = sigma;
  }

  @Override
  public Sequence<Double> mutate(Sequence<Double> parent, Random random, Listener listener) {
    Sequence<Double> child = parent.clone();
    for (int i = 0; i<child.size(); i++) {
      child.set(i, child.get(i)+random.nextGaussian()*sigma);
    }
    return child;
  }

}
