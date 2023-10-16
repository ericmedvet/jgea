
package io.github.ericmedvet.jgea.core.representation.sequence.bit;

import io.github.ericmedvet.jgea.core.operator.Crossover;

import java.util.random.RandomGenerator;

public class BitStringUniformCrossover implements Crossover<BitString> {

  @Override
  public BitString recombine(BitString p1, BitString p2, RandomGenerator random) {
    boolean[] bits = new boolean[Math.max(p1.size(), p2.size())];
    for (int i = 0; i < bits.length; i = i + 1) {
      if (i < p1.size() && i < p2.size()) {
        bits[i] = random.nextBoolean() ? p1.bits()[i] : p2.bits()[i];
      } else if (i < p1.size()) {
        bits[i] = p1.bits()[i];
      } else {
        bits[i] = p2.bits()[i];
      }
    }
    return new BitString(bits);
  }
}
