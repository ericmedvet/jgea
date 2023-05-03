package io.github.ericmedvet.jgea.core.representation;

import io.github.ericmedvet.jsdynsym.core.numerical.MultivariateRealFunction;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author "Eric Medvet" on 2023/05/01 for jgea
 */
public interface NamedMultivariateRealFunction extends MultivariateRealFunction {
  Map<String, Double> compute(Map<String, Double> input);

  List<String> xVarNames();

  List<String> yVarNames();

  static NamedMultivariateRealFunction from(
      MultivariateRealFunction mrf,
      List<String> xVarNames,
      List<String> yVarNames
  ) {
    if (xVarNames.size() != mrf.nOfInputs()) {
      throw new IllegalArgumentException("Wrong input size: %d expected by inner, %d vars".formatted(
          mrf.nOfInputs(),
          xVarNames.size()
      ));
    }
    if (yVarNames.size() != mrf.nOfOutputs()) {
      throw new IllegalArgumentException("Wrong output size: %d produced by inner, %d vars".formatted(
          mrf.nOfOutputs(),
          yVarNames.size()
      ));
    }
    return new NamedMultivariateRealFunction() {
      @Override
      public Map<String, Double> compute(Map<String, Double> input) {
        double[] in = xVarNames.stream().mapToDouble(input::get).toArray();
        if (in.length != mrf.nOfInputs()) {
          throw new IllegalArgumentException("Wrong input size: %d expected, %d found".formatted(
              mrf.nOfInputs(),
              in.length
          ));
        }
        double[] out = mrf.compute(in);
        if (out.length != yVarNames.size()) {
          throw new IllegalArgumentException("Wrong output size: %d expected, %d found".formatted(
              yVarNames.size(),
              in.length
          ));
        }
        return IntStream.range(0, yVarNames().size())
            .mapToObj(i -> Map.entry(yVarNames.get(i), out[i]))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
      }

      @Override
      public List<String> xVarNames() {
        return xVarNames;
      }

      @Override
      public List<String> yVarNames() {
        return yVarNames;
      }

      @Override
      public String toString() {
        return mrf.toString();
      }
    };
  }

  @Override
  default double[] compute(double... xs) {
    if (xs.length != xVarNames().size()) {
      throw new IllegalArgumentException("Wrong number of inputs: %d expected, %d found".formatted(
          xVarNames().size(),
          xs.length
      ));
    }
    Map<String, Double> output = compute(IntStream.range(0, xVarNames().size())
        .mapToObj(i -> Map.entry(xVarNames().get(i), xs[i]))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    return yVarNames().stream().mapToDouble(output::get).toArray();
  }

  @Override
  default int nOfInputs() {
    return xVarNames().size();
  }

  @Override
  default int nOfOutputs() {
    return yVarNames().size();
  }

}
