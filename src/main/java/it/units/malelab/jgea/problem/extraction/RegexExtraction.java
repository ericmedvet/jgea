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
import it.units.malelab.jgea.core.listener.Listener;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * @author eric
 */
public class RegexExtraction extends AbstractExtractionProblem<String> {

  public RegexExtraction(String text, Set<String> extractors, int folds, int i, ExtractionFitness.Metric... metrics) {
    super(text, extractors, folds, i, metrics);
  }

  @Override
  public Set<Range<Integer>> apply(String pattern, String string) {
    try {
      Matcher matcher = Pattern.compile(pattern).matcher(string);
      Set<Range<Integer>> extractions = new LinkedHashSet<>();
      int s = 0;
      while (matcher.find(s)) {
        Range<Integer> extraction = Range.openClosed(matcher.start(), matcher.end());
        s = extraction.upperEndpoint();
        extractions.add(extraction);
      }
      return extractions;
    } catch (PatternSyntaxException ex) {
      //ignore
    }
    return Collections.EMPTY_SET;
  }

}
