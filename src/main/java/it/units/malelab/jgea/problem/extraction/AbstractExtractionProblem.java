/*
 * Copyright (C) 2020 Eric Medvet <eric.medvet@gmail.com> (as eric)
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.units.malelab.jgea.problem.extraction;

import com.google.common.collect.Range;
import it.units.malelab.jgea.core.ProblemWithValidation;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
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
    Set<Range<Integer>> validationDesiredExtractions = extractors.stream()
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
