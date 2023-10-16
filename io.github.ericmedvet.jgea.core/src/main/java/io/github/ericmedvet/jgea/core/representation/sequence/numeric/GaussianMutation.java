
package io.github.ericmedvet.jgea.core.representation.sequence.numeric;

import io.github.ericmedvet.jgea.core.representation.sequence.ListProbabilisticMutation;
public class GaussianMutation extends ListProbabilisticMutation<Double> {

  public GaussianMutation(double sigma) {
    super(1d, (v, random) -> v + random.nextGaussian() * sigma);
  }
}
