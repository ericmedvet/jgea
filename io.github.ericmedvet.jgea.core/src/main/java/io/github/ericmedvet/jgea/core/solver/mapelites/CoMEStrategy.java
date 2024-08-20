package io.github.ericmedvet.jgea.core.solver.mapelites;

import io.github.ericmedvet.jnb.datastructure.Pair;

import java.util.Map;

public interface CoMEStrategy<Q> {
  double[] getOtherCoords(double[] input);

  void update(
      Map<Pair<double[], double[]>, Q> newQs
  );
}






