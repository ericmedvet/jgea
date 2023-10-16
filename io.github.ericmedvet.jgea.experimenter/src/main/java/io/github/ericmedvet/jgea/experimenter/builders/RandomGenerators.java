
package io.github.ericmedvet.jgea.experimenter.builders;

import io.github.ericmedvet.jnb.core.Param;

import java.util.Random;
import java.util.random.RandomGenerator;
public class RandomGenerators {

  private RandomGenerators() {
  }

  @SuppressWarnings("unused")
  public static RandomGenerator defaultRG(@Param(value = "seed", dI = 0) int seed) {
    return seed >= 0 ? new Random(seed) : new Random();
  }

}

