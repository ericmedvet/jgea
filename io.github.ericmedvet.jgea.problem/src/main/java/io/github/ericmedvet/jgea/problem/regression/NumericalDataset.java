/*-
 * ========================LICENSE_START=================================
 * jgea-problem
 * %%
 * Copyright (C) 2018 - 2024 Eric Medvet
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

import io.github.ericmedvet.jgea.core.util.Misc;
import io.github.ericmedvet.jnb.datastructure.DoubleRange;
import java.util.List;
import java.util.Map;
import java.util.function.IntFunction;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public interface NumericalDataset {

  enum Scaling {
    NONE,
    MIN_MAX,
    SYMMETRIC_MIN_MAX,
    STANDARDIZATION
  }

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

  record VariableInfo(DoubleRange range, double mean, double sd, double q1, double q2, double q3) {
    static VariableInfo of(List<Double> values) {
      double mean = values.stream().mapToDouble(v -> v).average().orElseThrow();
      double sd = Math.sqrt(values.stream()
          .mapToDouble(v -> (v - mean) * (v - mean))
          .average()
          .orElseThrow());
      return new VariableInfo(
          new DoubleRange(
              values.stream().min(Double::compareTo).orElseThrow(),
              values.stream().max(Double::compareTo).orElseThrow()),
          mean,
          sd,
          Misc.percentile(values, Double::compareTo, 0.25),
          Misc.percentile(values, Double::compareTo, 0.50),
          Misc.percentile(values, Double::compareTo, 0.75));
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

  default NumericalDataset processed(UnaryOperator<Example> processor) {
    NumericalDataset thisDataset = this;
    return new NumericalDataset() {
      @Override
      public IntFunction<Example> exampleProvider() {
        return i -> processor.apply(thisDataset.exampleProvider().apply(i));
      }

      @Override
      public int size() {
        return thisDataset.size();
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

  default NumericalDataset scaled(Scaling scaling) {
    return xScaled(scaling).yScaled(scaling);
  }

  default String summary() {
    StringBuilder sb = new StringBuilder();
    sb.append("n=%d p=%d (p.x=%d p.y=%d)%n"
        .formatted(
            size(),
            xVarNames().size() + yVarNames().size(),
            xVarNames().size(),
            yVarNames().size()));
    sb.append("x vars:\n");
    xVarNames().forEach(n -> {
      VariableInfo vi = VariableInfo.of(xValues(n));
      sb.append("\t%s:\tmin=%.3f\tmax=%.3f\tmean=%.3f\tsd=%.3f\tq1=%.3f\tq2=%.3f\tq3=%.3f%n"
          .formatted(n, vi.range.min(), vi.range.max(), vi.mean, vi.sd, vi.q1, vi.q2, vi.q3));
    });
    sb.append("y vars:\n");
    yVarNames().forEach(n -> {
      VariableInfo vi = VariableInfo.of(yValues(n));
      sb.append("\t%s:\tmin=%.3f\tmax=%.3f\tmean=%.3f\tsd=%.3f\tq1=%.3f\tq2=%.3f\tq3=%.3f%n"
          .formatted(n, vi.range.min(), vi.range.max(), vi.mean, vi.sd, vi.q1, vi.q2, vi.q3));
    });
    return sb.toString();
  }

  default NumericalDataset xScaled(Scaling scaling) {
    if (scaling.equals(Scaling.NONE)) {
      return this;
    }
    List<VariableInfo> varInfos =
        xVarNames().stream().map(n -> VariableInfo.of(xValues(n))).toList();
    return processed(originalE -> new Example(
        IntStream.range(0, xVarNames().size())
            .mapToDouble(j -> switch (scaling) {
              case MIN_MAX -> varInfos.get(j).range.normalize(originalE.xs[j]);
              case SYMMETRIC_MIN_MAX -> DoubleRange.SYMMETRIC_UNIT.denormalize(
                  varInfos.get(j).range.normalize(originalE.xs[j]));
              case STANDARDIZATION -> (originalE.xs[j] - varInfos.get(j).mean) / varInfos.get(j).sd;
              default -> throw new IllegalStateException("Unexpected scaling: " + scaling);
            })
            .toArray(),
        originalE.ys));
  }

  default List<Double> xValues(String xName) {
    int xIndex = xVarNames().indexOf(xName);
    return IntStream.range(0, size())
        .mapToDouble(i -> exampleProvider().apply(i).xs[xIndex])
        .boxed()
        .toList();
  }

  default NumericalDataset yScaled(Scaling scaling) {
    if (scaling.equals(Scaling.NONE)) {
      return this;
    }
    List<VariableInfo> varInfos =
        yVarNames().stream().map(n -> VariableInfo.of(yValues(n))).toList();
    return processed(originalE -> new Example(
        originalE.xs,
        IntStream.range(0, yVarNames().size())
            .mapToDouble(j -> switch (scaling) {
              case MIN_MAX -> varInfos.get(j).range.normalize(originalE.ys[j]);
              case SYMMETRIC_MIN_MAX -> DoubleRange.SYMMETRIC_UNIT.denormalize(
                  varInfos.get(j).range.normalize(originalE.ys[j]));
              case STANDARDIZATION -> (originalE.ys[j] - varInfos.get(j).mean) / varInfos.get(j).sd;
              default -> throw new IllegalStateException("Unexpected scaling: " + scaling);
            })
            .toArray()));
  }

  default List<Double> yValues(String yName) {
    int yIndex = yVarNames().indexOf(yName);
    return IntStream.range(0, size())
        .mapToDouble(i -> exampleProvider().apply(i).ys[yIndex])
        .boxed()
        .toList();
  }
}
