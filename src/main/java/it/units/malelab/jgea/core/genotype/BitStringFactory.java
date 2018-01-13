/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core.genotype;

import it.units.malelab.jgea.IndependentFactory;
import java.util.Random;

/**
 *
 * @author eric
 */
public class BitStringFactory extends IndependentFactory<BitString> {

  private final int size;

  public BitStringFactory(int size) {
    this.size = size;
  }

  @Override
  protected BitString build(Random random) {
    BitString bitString = new BitString(size);
    for (int i = 0; i<size; i++) {
      bitString.set(i, random.nextBoolean());
    }
    return bitString;
  }
  
}
