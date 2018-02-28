/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.problem.extraction;

import com.google.common.collect.Range;
import it.units.malelab.jgea.core.ProblemWithValidation;
import it.units.malelab.jgea.core.function.BiFunction;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author eric
 */
public abstract class AbstractExtractionProblem<E> implements ProblemWithValidation<E, List<Double>>, BiFunction<E, String, Set<Range<Integer>>> {

  private final ExtractionFitness<E> fitnessFunction;
  private final ExtractionFitness<E> validationFunction;

  public AbstractExtractionProblem(String text, Set<E> extractors, int folds, int i, ExtractionFitness.Metric... metrics) {
    String learningText = "";
    String validationText = "";
    double foldLength = (double) text.length() / (double) folds;
    for (int n = 0; n < folds; n++) {
      String piece = text.substring(
              (int) Math.round(foldLength * (double) n),
              (n == folds - 1) ? text.length() : ((int) Math.round(foldLength * (double) (n + 1))));
      if (n == i) {
        validationText = piece;
      } else {
        learningText = learningText + piece;
      }
    }
    final String finalLearningText = learningText;
    final String finalValidationText = validationText;
    Set<Range<Integer>> learningDesiredExtractions = extractors.stream()
            .map(e -> apply(e, finalLearningText))
            .reduce((extractions1, extractions2) -> Stream.concat(extractions1.stream(), extractions2.stream()).collect(Collectors.toSet()))
            .orElse(Collections.EMPTY_SET);
    Set<Range<Integer>>  validationDesiredExtractions = extractors.stream()
            .map(e -> apply(e, finalValidationText))
            .reduce((extractions1, extractions2) -> Stream.concat(extractions1.stream(), extractions2.stream()).collect(Collectors.toSet()))
            .orElse(Collections.EMPTY_SET);
    fitnessFunction = new ExtractionFitness<>(learningText, learningDesiredExtractions, this, metrics);
    validationFunction = new ExtractionFitness<>(validationText, validationDesiredExtractions, this, metrics);
  }

  public ExtractionFitness<E> getFitnessFunction() {
    return fitnessFunction;
  }

  public ExtractionFitness<E> getValidationFunction() {
    return validationFunction;
  }

}
