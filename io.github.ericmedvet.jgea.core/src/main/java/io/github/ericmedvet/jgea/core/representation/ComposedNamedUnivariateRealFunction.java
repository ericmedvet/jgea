
package io.github.ericmedvet.jgea.core.representation;

import io.github.ericmedvet.jsdynsym.core.composed.AbstractComposed;
import io.github.ericmedvet.jsdynsym.core.numerical.UnivariateRealFunction;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ComposedNamedUnivariateRealFunction extends AbstractComposed<UnivariateRealFunction> implements NamedUnivariateRealFunction {
  private final List<String> xVarNames;
  private final String yVarName;

  public ComposedNamedUnivariateRealFunction(UnivariateRealFunction inner, List<String> xVarNames, String yVarName) {
    super(inner);
    if (xVarNames.size() != inner.nOfInputs()) {
      throw new IllegalArgumentException("Wrong input size: %d expected by inner, %d vars".formatted(
          inner.nOfInputs(),
          xVarNames.size()
      ));
    }
    this.xVarNames = xVarNames;
    this.yVarName = yVarName;
  }

  @Override
  public double computeAsDouble(Map<String, Double> input) {
    double[] in = xVarNames.stream().mapToDouble(input::get).toArray();
    if (in.length != inner().nOfInputs()) {
      throw new IllegalArgumentException("Wrong input size: %d expected, %d found".formatted(
          inner().nOfInputs(),
          in.length
      ));
    }
    return inner().applyAsDouble(in);
  }

  @Override
  public String yVarName() {
    return yVarName;
  }

  @Override
  public int hashCode() {
    return Objects.hash(xVarNames, yVarName, inner());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    ComposedNamedUnivariateRealFunction that = (ComposedNamedUnivariateRealFunction) o;
    return Objects.equals(xVarNames, that.xVarNames) && Objects.equals(yVarName, that.yVarName) && Objects.equals(inner(), that.inner()) ;
  }

  @Override
  public String toString() {
    return inner().toString();
  }

  @Override
  public List<String> xVarNames() {
    return xVarNames;
  }
}