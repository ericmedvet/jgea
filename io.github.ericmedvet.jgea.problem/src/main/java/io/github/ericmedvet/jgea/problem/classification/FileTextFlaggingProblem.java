/*-
 * ========================LICENSE_START=================================
 * jgea-problem
 * %%
 * Copyright (C) 2018 - 2024 Eric Medvet
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */

package io.github.ericmedvet.jgea.problem.classification;

import io.github.ericmedvet.jgea.problem.extraction.string.RegexGrammar;
import io.github.ericmedvet.jnb.datastructure.Pair;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;

public class FileTextFlaggingProblem extends GrammarBasedTextFlaggingProblem {

  public FileTextFlaggingProblem(
      String positiveFileName,
      String negativeFileName,
      int folds,
      int i,
      ClassificationFitness.Metric learningErrorMetric,
      ClassificationFitness.Metric validationErrorMetric,
      RegexGrammar.Option... options)
      throws IOException {
    super(
        null,
        new LinkedHashSet<>(Arrays.asList(options)),
        buildData(positiveFileName, negativeFileName),
        folds,
        i,
        learningErrorMetric,
        validationErrorMetric);
  }

  private static List<Pair<String, Label>> buildData(String positiveFileName, String negativeFileName)
      throws IOException {
    List<Pair<String, Label>> data = new ArrayList<>();
    data.addAll(Files.lines(Paths.get(positiveFileName))
        .map(s -> new Pair<>(s, Label.FOUND))
        .toList());
    data.addAll(Files.lines(Paths.get(negativeFileName))
        .map(s -> new Pair<>(s, Label.NOT_FOUND))
        .toList());
    return data;
  }
}
