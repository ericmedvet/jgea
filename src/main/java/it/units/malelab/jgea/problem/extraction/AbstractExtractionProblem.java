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
import com.google.common.collect.Sets;
import it.units.malelab.jgea.core.ProblemWithValidation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author eric
 */
public abstract class AbstractExtractionProblem<S> implements ProblemWithValidation<Extractor<S>, List<Double>> {

  private final ExtractionFitness<S> fitnessFunction;
  private final ExtractionFitness<S> validationFunction;

  public AbstractExtractionProblem(List<S> sequence, Set<Extractor<S>> extractors, int folds, int i, ExtractionFitness.Metric... metrics) {
    List<S> learningSequence = new ArrayList<>();
    List<S> validationSequence = new ArrayList<>();
    double foldLength = (double) sequence.size() / (double) folds;
    for (int n = 0; n < folds; n++) {
      List<S> piece = sequence.subList(
          (int) Math.round(foldLength * (double) n),
          (n == folds - 1) ? sequence.size() : ((int) Math.round(foldLength * (double) (n + 1))));
      if (n == i) {
        validationSequence = piece;
      } else {
        learningSequence.addAll(piece);
      }
    }
    final List<S> finalLearningSequence = learningSequence;
    final List<S> finalValidationSequence = validationSequence;
    Set<Range<Integer>> learningDesiredExtractions = extractors.stream()
        .map(e -> e.extractLargest(finalLearningSequence))
        .reduce(Sets::union)
        .orElse((Set<Range<Integer>>) Collections.EMPTY_SET);
    Set<Range<Integer>> validationDesiredExtractions = extractors.stream()
        .map(e -> e.extractLargest(finalValidationSequence))
        .reduce(Sets::union)
        .orElse((Set<Range<Integer>>) Collections.EMPTY_SET);
    fitnessFunction = new ExtractionFitness<>(finalLearningSequence, learningDesiredExtractions, metrics);
    validationFunction = new ExtractionFitness<>(finalValidationSequence, validationDesiredExtractions, metrics);
  }

  public ExtractionFitness<S> getFitnessFunction() {
    return fitnessFunction;
  }

  public ExtractionFitness<S> getValidationFunction() {
    return validationFunction;
  }

}
