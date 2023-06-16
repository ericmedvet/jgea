package io.github.ericmedvet.jgea.core.problem;

import io.github.ericmedvet.jgea.core.order.ParetoDominance;
import io.github.ericmedvet.jgea.core.order.PartialComparator;

import java.util.Comparator;
import java.util.List;

public interface MultiHomogeneousObjectiveProblem<S, O> extends QualityBasedProblem<S, List<O>> {

  List<Comparator<O>> comparators();

  @Override
  default PartialComparator<List<O>> qualityComparator() {
    // TODO fix too many new
    return new ParetoDominance<>(comparators());
  }

}
