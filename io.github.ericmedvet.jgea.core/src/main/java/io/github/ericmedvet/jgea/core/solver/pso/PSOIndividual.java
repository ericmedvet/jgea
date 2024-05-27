package io.github.ericmedvet.jgea.core.solver.pso;

import io.github.ericmedvet.jgea.core.solver.Individual;
import java.util.Collection;
import java.util.List;

public interface PSOIndividual<S, Q> extends Individual<List<Double>, S, Q> {
  List<Double> bestKnownPosition();

  Q bestKnownQuality();

  List<Double> velocity();

  static <S, Q> PSOIndividual<S, Q> of(
      long id,
      List<Double> genotype,
      List<Double> velocity,
      List<Double> bestKnownPosition,
      Q bestKnownQuality,
      S solution,
      Q quality,
      long genotypeBirthIteration,
      long qualityMappingIteration,
      Collection<Long> parentIds) {
    record HardIndividual<S, Q>(
        long id,
        List<Double> genotype,
        List<Double> velocity,
        List<Double> bestKnownPosition,
        Q bestKnownQuality,
        S solution,
        Q quality,
        long genotypeBirthIteration,
        long qualityMappingIteration,
        Collection<Long> parentIds)
        implements PSOIndividual<S, Q> {}
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
