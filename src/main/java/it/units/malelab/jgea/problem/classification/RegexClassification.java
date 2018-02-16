/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.problem.classification;

import it.units.malelab.jgea.core.fitness.BinaryClassification;
import it.units.malelab.jgea.core.function.BiFunction;
import it.units.malelab.jgea.core.function.FunctionException;
import it.units.malelab.jgea.core.listener.Listener;
import it.units.malelab.jgea.core.util.Pair;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author eric
 */
public class RegexClassification extends BinaryClassification<Pattern, String> {
  
  private static class RegexMatcher implements BiFunction<Pattern, String, Boolean> {

    @Override
    public Boolean apply(Pattern pattern, String string, Listener listener) throws FunctionException {
      Matcher matcher = pattern.matcher(string);
      return matcher.find();
    }
    
  }

  public RegexClassification(List<Pair<String, Boolean>> data) {
    super(data, new RegexMatcher());
  }
      
}
