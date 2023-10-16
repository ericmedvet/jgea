
package io.github.ericmedvet.jgea.problem.classification;

import io.github.ericmedvet.jgea.core.util.Pair;

import java.util.List;
public class TextFlaggingProblem extends ClassificationProblem<String, TextFlaggingProblem.Label> {

  public TextFlaggingProblem(
      List<Pair<String, Label>> data,
      int folds,
      int i,
      ClassificationFitness.Metric learningErrorMetric,
      ClassificationFitness.Metric validationErrorMetric
  ) {
    super(data, folds, i, learningErrorMetric, validationErrorMetric);
  }

  public enum Label {
    FOUND, NOT_FOUND
  }

}
