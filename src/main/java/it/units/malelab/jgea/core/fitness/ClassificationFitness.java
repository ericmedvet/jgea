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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core.fitness;

import com.google.common.collect.EnumMultiset;
import com.google.common.collect.Multiset;
import it.units.malelab.jgea.core.util.Pair;
import it.units.malelab.jgea.core.util.WithNames;
import it.units.malelab.jgea.problem.classification.Classifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author eric
 */
public class ClassificationFitness<O, L extends Enum<L>> extends CaseBasedFitness<Classifier<O, L>, O, L, List<Double>> implements WithNames {

  public enum Metric {
    CLASS_ERROR_RATE, ERROR_RATE, BALANCED_ERROR_RATE
  }

  private static class ClassErrorRate<E extends Enum<E>> implements Function<List<E>, List<Pair<Integer, Integer>>> {

    private final List<E> actualLabels;

    public ClassErrorRate(List<E> actualLabels) {
      this.actualLabels = actualLabels;
    }

    @Override
    public List<Pair<Integer, Integer>> apply(List<E> predictedLabels) {
      E protoLabel = actualLabels.get(0);
      E[] allLabels = (E[]) protoLabel.getClass().getEnumConstants();
      Multiset<E> counts = EnumMultiset.create((Class<E>) protoLabel.getClass());
      Multiset<E> errors = EnumMultiset.create((Class<E>) protoLabel.getClass());
      for (int i = 0; i < actualLabels.size(); i++) {
        counts.add(actualLabels.get(i));
        if (!actualLabels.get(i).equals(predictedLabels.get(i))) {
          errors.add(actualLabels.get(i));
        }
      }
      List<Pair<Integer, Integer>> pairs = new ArrayList<>(allLabels.length);
      for (E currentLabel : allLabels) {
        pairs.add(Pair.of(errors.count(currentLabel), counts.count(currentLabel)));
      }
      return pairs;
    }

  }

  private static <E extends Enum<E>> Function<List<E>, List<Double>> getAggregator(List<E> actualLabels, Metric metric) {
    final ClassErrorRate<E> classErrorRate = new ClassErrorRate<>(actualLabels);
    if (metric.equals(Metric.CLASS_ERROR_RATE)) {
      return (List<E> predictedLabels) -> {
        List<Pair<Integer, Integer>> pairs = classErrorRate.apply(predictedLabels);
        return pairs.stream()
            .map(p -> ((double) p.first() / (double) p.second()))
            .collect(Collectors.toList());
      };
    }
    if (metric.equals(Metric.ERROR_RATE)) {
      return (List<E> predictedLabels) -> {
        List<Pair<Integer, Integer>> pairs = classErrorRate.apply(predictedLabels);
        int errors = pairs.stream().map(Pair::first).mapToInt(Integer::intValue).sum();
        int count = pairs.stream().map(Pair::second).mapToInt(Integer::intValue).sum();
        return Arrays.asList((double) errors / (double) count);
      };
    }
    if (metric.equals(Metric.BALANCED_ERROR_RATE)) {
      return (List<E> predictedLabels) -> {
        List<Pair<Integer, Integer>> pairs = classErrorRate.apply(predictedLabels);
        return Arrays.asList(pairs.stream()
            .map(p -> ((double) p.first() / (double) p.second()))
            .mapToDouble(Double::doubleValue)
            .average().orElse(Double.NaN));
      };
    }
    return null;
  }

  private final List<Pair<O, L>> data;
  private final List<String> names;

  public ClassificationFitness(List<Pair<O, L>> data, Metric errorMetric) {
    super(
        data.stream().map(Pair::first).collect(Collectors.toList()),
        (c, o) -> c.classify(o),
        getAggregator(data.stream().map(Pair::second).collect(Collectors.toList()), errorMetric)
    );
    this.data = data;
    names = new ArrayList<>();
    if (errorMetric.equals(Metric.CLASS_ERROR_RATE)) {
      L protoLabel = data.get(0).second();
      for (L label : (L[]) protoLabel.getClass().getEnumConstants()) {
        names.add(label.toString().toLowerCase() + ".error.rate");
      }
    } else if (errorMetric.equals(Metric.ERROR_RATE)) {
      names.add("error.rate");
    } else if (errorMetric.equals(Metric.BALANCED_ERROR_RATE)) {
      names.add("balanced.error.rate");
    }
  }

  public ClassificationFitness<O, L> changeMetric(Metric metric) {
    return new ClassificationFitness<>(data, metric);
  }

  @Override
  public List<String> names() {
    return names;
  }

}
