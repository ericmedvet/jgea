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
package it.units.malelab.jgea.problem.classification;

import it.units.malelab.jgea.core.fitness.CaseBasedFitness;
import it.units.malelab.jgea.core.util.Pair;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author eric
 */
public class ClassificationFitness<O, L> extends CaseBasedFitness<Classifier<O, L>, O, Label<L>, List<Double>> {

  private final List<Pair<O, Label<L>>> data;
  private final List<String> names;

  public ClassificationFitness(List<Pair<O, Label<L>>> data, Metric errorMetric) {
    super(
        data.stream().map(Pair::first).toList(),
        Classifier::classify,
        getAggregator(data.stream().map(Pair::second).toList(), errorMetric)
    );
    this.data = data;
    names = new ArrayList<>();
    if (errorMetric.equals(Metric.CLASS_ERROR_RATE)) {
      Collection<Label<L>> classes = data.get(0).second().values();
      for (Label<L> label : classes) {
        names.add(label.toString().toLowerCase() + ".error.rate");
      }
    } else if (errorMetric.equals(Metric.ERROR_RATE)) {
      names.add("error.rate");
    } else if (errorMetric.equals(Metric.BALANCED_ERROR_RATE)) {
      names.add("balanced.error.rate");
    }
  }

  public enum Metric {
    CLASS_ERROR_RATE, ERROR_RATE, BALANCED_ERROR_RATE
  }

  private static class ClassErrorRate<E> implements Function<List<Label<E>>, Map<Label<E>, Pair<Integer, Integer>>> {

    private final List<Label<E>> actualLabels;

    public ClassErrorRate(List<Label<E>> actualLabels) {
      this.actualLabels = actualLabels;
    }

    @Override
    public Map<Label<E>, Pair<Integer, Integer>> apply(List<Label<E>> predictedLabels) {
      Map<Label<E>, Integer> counts = new HashMap<>();
      Map<Label<E>, Integer> errors = new HashMap<>();

      IntStream.range(0, actualLabels.size()).forEach(i -> {
            Label<E> actualLabel = actualLabels.get(i);
            counts.put(actualLabel, counts.getOrDefault(actualLabel, 0) + 1);
            if (!actualLabel.equals(predictedLabels.get(i))) {
              errors.put(actualLabel, errors.getOrDefault(actualLabel, 0) + 1);
            }
          }
      );

      return actualLabels.get(0).values().stream().collect(Collectors.toMap(
          Function.identity(),
          v -> Pair.of(counts.getOrDefault(v, 0), errors.getOrDefault(v, 0))
      ));
    }

  }

  private static <E> Function<List<Label<E>>, List<Double>> getAggregator(List<Label<E>> actualLabels, Metric metric) {
    final ClassErrorRate<E> classErrorRate = new ClassErrorRate<>(actualLabels);
    if (metric.equals(Metric.CLASS_ERROR_RATE)) {
      return (List<Label<E>> predictedLabels) -> {
        Collection<Pair<Integer, Integer>> pairs = classErrorRate.apply(predictedLabels).values();
        return pairs.stream().map(p -> ((double) p.first() / (double) p.second())).toList();
      };
    }
    if (metric.equals(Metric.ERROR_RATE)) {
      return (List<Label<E>> predictedLabels) -> {
        Collection<Pair<Integer, Integer>> pairs = classErrorRate.apply(predictedLabels).values();
        int errors = pairs.stream().map(Pair::first).mapToInt(Integer::intValue).sum();
        int count = pairs.stream().map(Pair::second).mapToInt(Integer::intValue).sum();
        return List.of((double) errors / (double) count);
      };
    }
    if (metric.equals(Metric.BALANCED_ERROR_RATE)) {
      return (List<Label<E>> predictedLabels) -> {
        Collection<Pair<Integer, Integer>> pairs = classErrorRate.apply(predictedLabels).values();
        return List.of(pairs.stream()
            .map(p -> ((double) p.first() / (double) p.second()))
            .mapToDouble(Double::doubleValue)
            .average()
            .orElse(Double.NaN));
      };
    }
    return null;
  }

  public ClassificationFitness<O, L> changeMetric(Metric metric) {
    return new ClassificationFitness<>(data, metric);
  }

}
