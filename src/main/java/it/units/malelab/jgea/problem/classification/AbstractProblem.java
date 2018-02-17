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
import it.units.malelab.jgea.core.function.NonDeterministicFunction;
import it.units.malelab.jgea.core.util.Pair;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author eric
 */
public abstract class AbstractProblem<C, O> implements ProblemWithValidation<C, List<Double>>, BiFunction<C, O, Boolean> {
  
  private final BinaryClassification<C, O> fitnessFunction;
  private final BinaryClassification<C, O> validationFunction;

  public AbstractProblem(List<Pair<O, Boolean>> data, int folds, int i) {
    List<Pair<O, Boolean>> learningData = new ArrayList<>(data);
    learningData.removeAll(DataUtils.fold(data, i, folds));
    this.fitnessFunction = new BinaryClassification<>(learningData, this);
    this.validationFunction = new BinaryClassification<>(DataUtils.fold(data, i, folds), this);
  }

  @Override
  public Function<C, List<Double>> getValidationFunction() {
    return validationFunction;
  }

  @Override
  public NonDeterministicFunction<C, List<Double>> getFitnessFunction() {
    return fitnessFunction;
  }
  
}
