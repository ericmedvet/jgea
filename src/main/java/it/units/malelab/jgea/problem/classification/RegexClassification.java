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
import it.units.malelab.jgea.core.listener.Listener;
import it.units.malelab.jgea.core.util.Pair;

import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * @author eric
 */
public class RegexClassification extends AbstractClassificationProblem<String, String, RegexClassification.Label> {

  public enum Label {
    FOUND, NOT_FOUND
  }

  public RegexClassification(List<Pair<String, Label>> data, int folds, int i, ClassificationFitness.Metric learningErrorMetric, ClassificationFitness.Metric validationErrorMetric) {
    super(data, folds, i, learningErrorMetric, validationErrorMetric);
  }

  @Override
  public RegexClassification.Label apply(String pattern, String string) {
    boolean found = false;
    try {
      found = Pattern.compile(pattern).matcher(string).find();
    } catch (PatternSyntaxException ex) {
      //ignore
    }
    return found ? Label.FOUND : Label.NOT_FOUND;
  }

}
