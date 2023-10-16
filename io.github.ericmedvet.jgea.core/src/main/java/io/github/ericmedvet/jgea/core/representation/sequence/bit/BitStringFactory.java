
package io.github.ericmedvet.jgea.core.representation.sequence.bit;

import io.github.ericmedvet.jgea.core.IndependentFactory;

import java.util.random.RandomGenerator;
public class BitStringFactory implements IndependentFactory<BitString> {

  private final int size;

  public BitStringFactory(int size) {
    this.size = size;
  }

  @Override
  public BitString build(RandomGenerator random) {
    BitString bitString = new BitString(size);
    for (int i = 0; i < bitString.size(); i++) {
      bitString.bits()[i] = random.nextBoolean();
    }
    return bitString;
  }

}
