/*
 * Copyright 2020 Eric Medvet <eric.medvet@gmail.com> (as eric)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it.units.malelab.jgea.problem.classification;

import it.units.malelab.jgea.core.ProblemWithValidation;
import it.units.malelab.jgea.core.fitness.ClassificationFitness;
import it.units.malelab.jgea.core.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * @author eric
 */
public class ClassificationProblem<O, L extends Enum<L>> implements ProblemWithValidation<Classifier<O, L>, List<Double>> {

  private final ClassificationFitness<O, L> fitnessFunction;
  private final ClassificationFitness<O, L> validationFunction;
  private final List<Pair<O, L>> learningData;
  private final List<Pair<O, L>> validationData;

  public ClassificationProblem(List<Pair<O, L>> data, int folds, int i, ClassificationFitness.Metric learningMetric, ClassificationFitness.Metric validationMetric) {
    validationData = DataUtils.fold(data, i, folds);
    learningData = new ArrayList<>(data);
    learningData.removeAll(validationData);
    this.fitnessFunction = new ClassificationFitness<>(learningData, learningMetric);
    this.validationFunction = new ClassificationFitness<>(validationData, validationMetric);
  }

  @Override
  public Function<Classifier<O, L>, List<Double>> getValidationFunction() {
    return validationFunction;
  }

  @Override
  public Function<Classifier<O, L>, List<Double>> getFitnessFunction() {
    return fitnessFunction;
  }

  public List<Pair<O, L>> getLearningData() {
    return learningData;
  }

  public List<Pair<O, L>> getValidationData() {
    return validationData;
  }

}
