
package io.github.ericmedvet.jgea.core.distance;

import io.github.ericmedvet.jgea.core.representation.sequence.bit.BitString;
public class BitStringHamming implements Distance<BitString> {

  @Override
  public Double apply(BitString b1, BitString b2) {
    if (b1.size() != b2.size()) {
      throw new IllegalArgumentException(String.format(
          "Sequences size should be the same (%d vs. %d)",
          b1.size(),
          b2.size()
      ));
    }
    int s = 0;
    for (int i = 0; i < b1.size(); i++) {
      s = s + (b1.bits()[i] != b2.bits()[i] ? 1 : 0);
    }
    return (double) s;
  }


}
