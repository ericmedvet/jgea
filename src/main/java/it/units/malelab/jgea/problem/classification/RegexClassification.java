/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.problem.classification;

import it.units.malelab.jgea.core.fitness.Classification;
import it.units.malelab.jgea.core.function.FunctionException;
import it.units.malelab.jgea.core.listener.Listener;
import it.units.malelab.jgea.core.util.Pair;
import java.util.List;
import java.util.regex.Pattern;

/**
 *
 * @author eric
 */
public class RegexClassification extends AbstractClassificationProblem<String, String, RegexClassification.Label> {
  
  public static enum Label {FOUND, NOT_FOUND};

  public RegexClassification(List<Pair<String, Label>> data, int folds, int i, Classification.ErrorMetric learningErrorMetric, Classification.ErrorMetric validationErrorMetric) {
    super(data, folds, i, learningErrorMetric, validationErrorMetric);
  }

  @Override
  public RegexClassification.Label apply(String pattern, String string, Listener listener) throws FunctionException {
    boolean found = Pattern.compile(pattern).matcher(string).find();
    return found?Label.FOUND:Label.NOT_FOUND;
  }

}
