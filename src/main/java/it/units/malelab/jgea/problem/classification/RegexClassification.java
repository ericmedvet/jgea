/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.problem.classification;

import it.units.malelab.jgea.core.ProblemWithValidation;
import it.units.malelab.jgea.core.fitness.BinaryClassification;
import it.units.malelab.jgea.core.function.BiFunction;
import it.units.malelab.jgea.core.function.Function;
import it.units.malelab.jgea.core.function.FunctionException;
import it.units.malelab.jgea.core.function.NonDeterministicFunction;
import it.units.malelab.jgea.core.listener.Listener;
import it.units.malelab.jgea.core.util.Pair;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author eric
 */
public class RegexClassification extends AbstractProblem<String, String> {

  public RegexClassification(List<Pair<String, Boolean>> data, int folds, int i) {
    super(data, folds, i);
  }

  @Override
  public Boolean apply(String pattern, String string, Listener listener) throws FunctionException {
    return Pattern.compile(pattern).matcher(string).find();
  }

}
