package io.github.ericmedvet.jgea.core.solver.es;

import io.github.ericmedvet.jgea.core.solver.Individual;
import java.util.Collection;
import java.util.List;

public interface CMAESIndividual<S, Q> extends Individual<List<Double>, S, Q> {
  double[] x();

  double[] y();

  double[] z();

  static <S, Q> CMAESIndividual<S, Q> of(
      long id,
      List<Double> genotype,
      S solution,
      Q quality,
      long genotypeBirthIteration,
      long qualityMappingIteration,
      Collection<Long> parentIds,
      double[] x,
      double[] y,
      double[] z) {
    record HardIndividual<S, Q>(
        long id,
        List<Double> genotype,
        S solution,
        Q quality,
        long genotypeBirthIteration,
        long qualityMappingIteration,
        Collection<Long> parentIds,
        double[] x,
        double[] y,
        double[] z)
        implements CMAESIndividual<S, Q> {}
    return new HardIndividual<>(
        id, genotype, solution, quality, genotypeBirthIteration, qualityMappingIteration, parentIds, x, y, z);
  }
}
