
package io.github.ericmedvet.jgea.core.representation;

import io.github.ericmedvet.jsdynsym.core.composed.AbstractComposed;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SingleVarNamedMultivariateRealFunction extends AbstractComposed<NamedMultivariateRealFunction> implements NamedUnivariateRealFunction {

  public SingleVarNamedMultivariateRealFunction(NamedMultivariateRealFunction inner) {
    super(inner);
  }

  @Override
  public double computeAsDouble(Map<String, Double> input) {
    return inner().compute(input).get(yVarName());
  }

  @Override
  public String yVarName() {
    return inner().yVarNames().get(0);
  }

  @Override
  public int hashCode() {
    return Objects.hash(inner().hashCode());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    SingleVarNamedMultivariateRealFunction that = (SingleVarNamedMultivariateRealFunction) o;
    return inner().equals(that.inner());
  }

  @Override
  public String toString() {
    return inner().toString();
  }

  @Override
  public List<String> xVarNames() {
    return inner().xVarNames();
  }
}
