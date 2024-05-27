package io.github.ericmedvet.jgea.core.solver.es;

import io.github.ericmedvet.jgea.core.solver.Individual;
import io.github.ericmedvet.jgea.core.solver.pso.PSOIndividual;

import java.util.Collection;
import java.util.List;

public interface CMAESIndividual<S, Q> extends Individual<List<Double>, S, Q> {
  List<Double> z();

  List<Double> y();

  List<Double> x();

  static <S, Q> CMAESIndividual<S, Q> of(
      long id,
      List<Double> genotype,
      S solution,
      Q quality,
      long genotypeBirthIteration,
      long qualityMappingIteration,
      Collection<Long> parentIds,
      List<Double> x,
      List<Double> y,
      List<Double> z
  ) {
    record HardIndividual<S, Q>(
        long id,
        List<Double> genotype,
        S solution,
        Q quality,
        long genotypeBirthIteration,
        long qualityMappingIteration,
        Collection<Long> parentIds,
        List<Double> x,
        List<Double> y,
        List<Double> z
    ) implements CMAESIndividual<S, Q> {}
    return new HardIndividual<>(
        id, genotype, solution, quality, genotypeBirthIteration, qualityMappingIteration, parentIds, x, y, z
    );
  }

}
