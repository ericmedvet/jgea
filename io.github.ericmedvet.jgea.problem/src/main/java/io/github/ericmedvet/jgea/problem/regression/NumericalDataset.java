package io.github.ericmedvet.jgea.problem.regression;

import java.util.List;
import java.util.Map;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author "Eric Medvet" on 2023/04/30 for jgea
 */
public interface NumericalDataset {
  record Example(double[] xs, double[] ys) {
    public Example(double[] xs, double y) {
      this(xs, new double[]{y});
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
              .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
      );
    }
  }

  IntFunction<Example> exampleProvider();

  int size();

  List<String> xVarNames();

  List<String> yVarNames();

  default NumericalDataset folds(List<Integer> folds, int n) {
    NumericalDataset thisDataset = this;
    int[] indexes = IntStream.range(0, size()).filter(i -> folds.contains(i % n)).toArray();
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
