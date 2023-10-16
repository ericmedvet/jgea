
package io.github.ericmedvet.jgea.core.representation;

import io.github.ericmedvet.jsdynsym.core.composed.AbstractComposed;
import io.github.ericmedvet.jsdynsym.core.numerical.MultivariateRealFunction;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ComposedNamedMultivariateRealFunction extends AbstractComposed<MultivariateRealFunction> implements NamedMultivariateRealFunction {
  private final List<String> xVarNames;
  private final List<String> yVarNames;

  public ComposedNamedMultivariateRealFunction(
      MultivariateRealFunction inner,
      List<String> xVarNames,
      List<String> yVarNames
  ) {
    super(inner);
    if (xVarNames.size() != inner().nOfInputs()) {
      throw new IllegalArgumentException("Wrong input size: %d expected by inner, %d vars".formatted(
          inner().nOfInputs(),
          xVarNames.size()
      ));
    }
    if (yVarNames.size() != inner().nOfOutputs()) {
      throw new IllegalArgumentException("Wrong output size: %d produced by inner, %d vars".formatted(
          inner().nOfOutputs(),
          yVarNames.size()
      ));
    }
    this.xVarNames = xVarNames;
    this.yVarNames = yVarNames;
  }

  @Override
  public Map<String, Double> compute(Map<String, Double> input) {
    double[] in = xVarNames.stream().mapToDouble(input::get).toArray();
    if (in.length != inner().nOfInputs()) {
      throw new IllegalArgumentException("Wrong input size: %d expected, %d found".formatted(
          inner().nOfInputs(),
          in.length
      ));
    }
    double[] out = inner().compute(in);
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
  public int hashCode() {
    return Objects.hash(xVarNames, yVarNames, inner());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    ComposedNamedMultivariateRealFunction that = (ComposedNamedMultivariateRealFunction) o;
    return Objects.equals(xVarNames, that.xVarNames) && Objects.equals(yVarNames, that.yVarNames) && Objects.equals(
        inner(),
        that.inner()
    );
  }

  @Override
  public String toString() {
    return inner().toString();
  }
}
