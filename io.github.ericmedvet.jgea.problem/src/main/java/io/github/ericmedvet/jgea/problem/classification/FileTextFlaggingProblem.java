
package io.github.ericmedvet.jgea.problem.classification;

import io.github.ericmedvet.jgea.core.util.Pair;
import io.github.ericmedvet.jgea.problem.extraction.string.RegexGrammar;

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
      RegexGrammar.Option... options
  ) throws IOException {
    super(
        null,
        new LinkedHashSet<>(Arrays.asList(options)),
        buildData(positiveFileName, negativeFileName),
        folds,
        i,
        learningErrorMetric,
        validationErrorMetric
    );
  }

  private static List<Pair<String, Label>> buildData(
      String positiveFileName, String negativeFileName
  ) throws IOException {
    List<Pair<String, Label>> data = new ArrayList<>();
    data.addAll(Files.lines(Paths.get(positiveFileName)).map(s -> Pair.of(s, Label.FOUND)).toList());
    data.addAll(Files.lines(Paths.get(negativeFileName)).map(s -> Pair.of(s, Label.NOT_FOUND)).toList());
    return data;
  }

}
