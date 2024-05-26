package io.github.ericmedvet.jgea.core.solver.pso;

import io.github.ericmedvet.jgea.core.solver.Individual;
import java.util.Collection;
import java.util.List;

public interface PSOIndividual<S, Q> extends Individual<List<Double>, S, Q> {
  List<Double> bestKnownPosition();

  Q bestKnownQuality();

  List<Double> velocity();

  static <S1, Q1> PSOIndividual<S1, Q1> of(
      long id,
      List<Double> genotype,
      List<Double> velocity,
      List<Double> bestKnownPosition,
      Q1 bestKnownQuality,
      S1 solution,
      Q1 quality,
      long genotypeBirthIteration,
      long qualityMappingIteration,
      Collection<Long> parentIds) {
    record HardIndividual<S1, Q1>(
        long id,
        List<Double> genotype,
        List<Double> velocity,
        List<Double> bestKnownPosition,
        Q1 bestKnownQuality,
        S1 solution,
        Q1 quality,
        long genotypeBirthIteration,
        long qualityMappingIteration,
        Collection<Long> parentIds)
        implements PSOIndividual<S1, Q1> {}
    return new HardIndividual<>(
        id,
        genotype,
        velocity,
        bestKnownPosition,
        bestKnownQuality,
        solution,
        quality,
        genotypeBirthIteration,
        qualityMappingIteration,
        parentIds);
  }

  default List<Double> position() {
    return genotype();
  }
}
