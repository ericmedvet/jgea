/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.distance;

import it.units.malelab.jgea.core.function.FunctionException;
import it.units.malelab.jgea.core.genotype.BitString;
import it.units.malelab.jgea.core.listener.Listener;
import java.util.BitSet;

/**
 *
 * @author eric
 */
public class BitStringHamming implements Distance<BitString>{

  @Override
  public Double apply(BitString b1, BitString b2, Listener listener) throws FunctionException {
    if (b1.size()!=b2.size()) {
      throw new IllegalArgumentException(String.format("Sequences size should be the same (%d vs. %d)", b1.size(), b2.size()));
    }
    BitSet xored = b1.asBitSet();
    xored.xor(b2.asBitSet());
    return (double)xored.cardinality();
  }

  
  
}
