
package io.github.ericmedvet.jgea.core.representation.sequence.bit;

import io.github.ericmedvet.jgea.core.operator.Mutation;

import java.util.Arrays;
import java.util.random.RandomGenerator;
import java.util.stream.IntStream;
public class BitStringFlipMutation implements Mutation<BitString> {

  private final double p;

  public BitStringFlipMutation(double p) {
    this.p = p;
  }

  @Override
  public BitString mutate(BitString parent, RandomGenerator random) {
    boolean[] bits = Arrays.copyOf(parent.bits(), parent.size());
    IntStream.range(0, bits.length).forEach(i -> bits[i] = (random.nextDouble() < p) != bits[i]);
    return new BitString(bits);
  }

}
