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

package it.units.malelab.jgea.problem.classification;

import it.units.malelab.jgea.core.fitness.ClassificationFitness;
import it.units.malelab.jgea.core.util.Pair;
import it.units.malelab.jgea.representation.grammar.RegexGrammar;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author eric
 */
public class FileRegexClassification extends GrammarBasedRegexClassification {

  private static List<Pair<String, Label>> buildData(String positiveFileName, String negativeFileName) throws IOException {
    List<Pair<String, Label>> data = new ArrayList<>();
    data.addAll(Files.lines(Paths.get(positiveFileName)).map(s -> Pair.of(s, Label.FOUND)).collect(Collectors.toList()));
    data.addAll(Files.lines(Paths.get(negativeFileName)).map(s -> Pair.of(s, Label.NOT_FOUND)).collect(Collectors.toList()));
    return data;
  }

  public FileRegexClassification(String positiveFileName, String negativeFileName, int folds, int i, ClassificationFitness.Metric learningErrorMetric, ClassificationFitness.Metric validationErrorMetric, RegexGrammar.Option... options) throws IOException {
    super(null,
        new LinkedHashSet<>(Arrays.asList(options)),
        buildData(positiveFileName, negativeFileName),
        folds, i,
        learningErrorMetric, validationErrorMetric);
  }

}
