/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.problem.extraction;

import com.google.common.collect.Range;
import it.units.malelab.jgea.core.function.FunctionException;
import it.units.malelab.jgea.core.listener.Listener;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 *
 * @author eric
 */
public class RegexExtraction extends AbstractExtractionProblem<String> {

  public RegexExtraction(String text, Set<String> extractors, int folds, int i, ExtractionFitness.Metric... metrics) {
    super(text, extractors, folds, i, metrics);
  }

  @Override
  public Set<Range<Integer>> apply(String pattern, String string, Listener listener) throws FunctionException {
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
