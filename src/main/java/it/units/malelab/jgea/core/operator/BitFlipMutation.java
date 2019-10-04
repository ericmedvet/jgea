/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core.operator;

import it.units.malelab.jgea.core.genotype.BitString;
import it.units.malelab.jgea.core.listener.Listener;
import java.util.Random;

/**
 *
 * @author eric
 */
public class BitFlipMutation extends AbstractMutation<BitString> {
  
  private final double p;

  public BitFlipMutation(double p) {
    this.p = p;
  }

  @Override
  public BitString mutate(BitString g, Random random, Listener listener) {
    BitString newG = (BitString)g.clone();
    for (int i = 0; i<newG.size(); i++) {
      if (random.nextDouble()<=p) {
        newG.flip(i);
      }
    }
    return newG;
  }
  
}
