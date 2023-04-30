package io.github.ericmedvet.jgea.problem.regression;

import java.util.List;
import java.util.stream.IntStream;

/**
 * @author "Eric Medvet" on 2023/04/30 for jgea
 */
public record Dataset(List<Example> examples, List<String> xVarNames, List<String> yVarNames) {
  public Dataset {
    List<Integer> xsSizes = examples().stream().map(e -> e.xs.length).distinct().toList();
    List<Integer> ysSizes = examples().stream().map(e -> e.ys.length).distinct().toList();
    if (xsSizes.size() > 1) {
      throw new IllegalArgumentException("Size of x is not consistent across examples, found sizes %s".formatted(xsSizes));
    }
    if (ysSizes.size() > 1) {
      throw new IllegalArgumentException("Size of y is not consistent across examples, found sizes %s".formatted(ysSizes));
    }
    if (xVarNames.size() != xsSizes.get(0)) {
      throw new IllegalArgumentException(("Number of names of x vars is different form size of x in examples: %d vs " +
          "%d").formatted(
          xVarNames().size(),
          xsSizes.get(0)
      ));
    }
    if (yVarNames.size() != ysSizes.get(0)) {
      throw new IllegalArgumentException(("Number of names of y vars is different form size of y in examples: %d vs " +
          "%d").formatted(
          xVarNames().size(),
          xsSizes.get(0)
      ));
    }
  }

  public Dataset(List<Example> examples) {
    this(
        examples,
        varNames("x", examples.get(0).xs().length),
        varNames("y", examples.get(0).ys().length)
    );
  }

  public record Example(double[] xs, double[] ys) {
    public Example(double[] xs, double y) {
      this(xs, new double[]{y});
    }
  }


  public static List<String> varNames(String name, int number) {
    int digits = (int) Math.ceil(Math.log10(number));
    return IntStream.range(1, number + 1).mapToObj((name + "%0" + digits + "d")::formatted).toList();
  }
}
