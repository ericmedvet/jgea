
package io.github.ericmedvet.jgea.problem.classification;

import io.github.ericmedvet.jgea.core.order.ParetoDominance;
import io.github.ericmedvet.jgea.core.order.PartialComparator;
import io.github.ericmedvet.jgea.core.problem.ProblemWithValidation;
import io.github.ericmedvet.jgea.core.util.Pair;
import io.github.ericmedvet.jgea.problem.DataUtils;

import java.util.ArrayList;
import java.util.List;
public class ClassificationProblem<O, L extends Enum<L>> implements ProblemWithValidation<Classifier<O, L>,
    List<Double>> {

  // TODO fix this
  private final static PartialComparator<List<Double>> COMPARATOR = ParetoDominance.build(Double.class, 1);

  private final ClassificationFitness<O, L> fitnessFunction;
  private final ClassificationFitness<O, L> validationFunction;
  private final List<Pair<O, L>> learningData;
  private final List<Pair<O, L>> validationData;

  public ClassificationProblem(
      List<Pair<O, L>> data,
      int folds,
      int i,
      ClassificationFitness.Metric learningMetric,
      ClassificationFitness.Metric validationMetric
  ) {
    validationData = DataUtils.fold(data, i, folds);
    learningData = new ArrayList<>(data);
    learningData.removeAll(validationData);
    fitnessFunction = new ClassificationFitness<>(learningData, learningMetric);
    validationFunction = new ClassificationFitness<>(validationData, validationMetric);
  }

  public List<Pair<O, L>> getLearningData() {
    return learningData;
  }

  public List<Pair<O, L>> getValidationData() {
    return validationData;
  }

  @Override
  public PartialComparator<List<Double>> qualityComparator() {
    return COMPARATOR;
  }

  @Override
  public ClassificationFitness<O, L> qualityFunction() {
    return fitnessFunction;
  }

  @Override
  public ClassificationFitness<O, L> validationQualityFunction() {
    return validationFunction;
  }

}
