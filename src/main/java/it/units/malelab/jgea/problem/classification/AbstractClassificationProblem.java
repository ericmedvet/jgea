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

import it.units.malelab.jgea.core.ProblemWithValidation;
import it.units.malelab.jgea.core.fitness.ClassificationFitness;
import it.units.malelab.jgea.core.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * @author eric
 */
public abstract class AbstractClassificationProblem<C, O, E extends Enum<E>> implements ProblemWithValidation<C, List<Double>>, BiFunction<C, O, E> {

  private final ClassificationFitness<C, O, E> fitnessFunction;
  private final ClassificationFitness<C, O, E> validationFunction;
  private final List<Pair<O, E>> learningData;
  private final List<Pair<O, E>> validationData;

  public AbstractClassificationProblem(List<Pair<O, E>> data, int folds, int i, ClassificationFitness.Metric learningMetric, ClassificationFitness.Metric validationMetric) {
    validationData = DataUtils.fold(data, i, folds);
    learningData = new ArrayList<>(data);
    learningData.removeAll(validationData);
    this.fitnessFunction = new ClassificationFitness<>(learningData, this, learningMetric);
    this.validationFunction = new ClassificationFitness<>(validationData, this, validationMetric);
  }

  @Override
  public Function<C, List<Double>> getValidationFunction() {
    return validationFunction;
  }

  @Override
  public Function<C, List<Double>> getFitnessFunction() {
    return fitnessFunction;
  }

  public List<Pair<O, E>> getLearningData() {
    return learningData;
  }

  public List<Pair<O, E>> getValidationData() {
    return validationData;
  }

}
