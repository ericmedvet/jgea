/*-
 * ========================LICENSE_START=================================
 * jgea-problem
 * %%
 * Copyright (C) 2018 - 2023 Eric Medvet
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */
package io.github.ericmedvet.jgea.problem.regression;

import java.util.List;
import java.util.Map;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public interface NumericalDataset {
  record Example(double[] xs, double[] ys) {
    public Example(double[] xs, double y) {
      this(xs, new double[] {y});
    }
  }

  record NamedExample(Map<String, Double> x, Map<String, Double> y) {
    public NamedExample(Example example, List<String> xVarNames, List<String> yVarNames) {
      this(
          IntStream.range(0, xVarNames.size())
              .mapToObj(i -> Map.entry(xVarNames.get(i), example.xs()[i]))
              .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)),
          IntStream.range(0, yVarNames.size())
              .mapToObj(i -> Map.entry(yVarNames.get(i), example.ys()[i]))
              .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    }
  }

  IntFunction<Example> exampleProvider();

  int size();

  List<String> xVarNames();

  List<String> yVarNames();

  default NumericalDataset folds(List<Integer> folds, int n) {
    NumericalDataset thisDataset = this;
    int[] indexes =
        IntStream.range(0, size()).filter(i -> folds.contains(i % n)).toArray();
    IntFunction<Example> provider = thisDataset.exampleProvider();
    return new NumericalDataset() {
      @Override
      public IntFunction<Example> exampleProvider() {
        return i -> provider.apply(indexes[i]);
      }

      @Override
      public int size() {
        return indexes.length;
      }

      @Override
      public List<String> xVarNames() {
        return thisDataset.xVarNames();
      }

      @Override
      public List<String> yVarNames() {
        return thisDataset.yVarNames();
      }
    };
  }

  default IntFunction<NamedExample> namedExampleProvider() {
    return i -> new NamedExample(exampleProvider().apply(i), xVarNames(), yVarNames());
  }
}
