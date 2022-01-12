/*
 * Copyright 2020 Eric Medvet <eric.medvet@gmail.com> (as eric)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it.units.malelab.jgea.problem.extraction;

import com.google.common.collect.Range;
import com.google.common.collect.Sets;
import it.units.malelab.jgea.core.ProblemWithValidation;
import it.units.malelab.jgea.core.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author eric
 */
public class ExtractionProblem<S> extends ExtractionFitness<S> implements ProblemWithValidation<Extractor<S>, List<Double>> {

  private final ExtractionFitness<S> validationFunction;

  public ExtractionProblem(
      Set<Extractor<S>> extractors, List<S> sequence, int folds, int i, ExtractionFitness.Metric... metrics
  ) {
    super(
        buildDataset(extractors, sequence, folds, i, true).first(),
        buildDataset(extractors, sequence, folds, i, true).second(),
        metrics
    );
    Pair<List<S>, Set<Range<Integer>>> validationDataset = buildDataset(extractors, sequence, folds, i, false);
    validationFunction = new ExtractionFitness<>(validationDataset.first(), validationDataset.second(), metrics);
  }

  private static <S> Pair<List<S>, Set<Range<Integer>>> buildDataset(
      Set<Extractor<S>> extractors, List<S> sequence, int folds, int i, boolean takeAllButIth
  ) {
    List<S> builtSequence = new ArrayList<>();
    double foldLength = (double) sequence.size() / (double) folds;
    for (int n = 0; n < folds; n++) {
      List<S> piece = sequence.subList(
          (int) Math.round(foldLength * (double) n),
          (n == folds - 1) ? sequence.size() : ((int) Math.round(foldLength * (double) (n + 1)))
      );
      if (takeAllButIth && (n != i)) {
        builtSequence.addAll(piece);
      } else if (!takeAllButIth && (n == i)) {
        builtSequence.addAll(piece);
      }
    }
    Set<Range<Integer>> desiredExtractions = extractors.stream()
        .map(e -> e.extractNonOverlapping(builtSequence))
        .reduce(Sets::union)
        .orElse(Set.of());
    return Pair.of(builtSequence, desiredExtractions);
  }

  public ExtractionFitness<S> getValidationFunction() {
    return validationFunction;
  }

}
