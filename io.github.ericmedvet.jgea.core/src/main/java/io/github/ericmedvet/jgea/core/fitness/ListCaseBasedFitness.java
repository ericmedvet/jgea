package io.github.ericmedvet.jgea.core.fitness;

import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntFunction;

public class ListCaseBasedFitness<S, C, CO, AF> implements CaseBasedFitness<S, C, CO, AF> {
  private final List<C> cases;
  private final BiFunction<S, C, CO> caseFunction;
  private final Function<List<CO>, AF> aggregateFunction;

  public ListCaseBasedFitness(
      List<C> cases,
      BiFunction<S, C, CO> caseFunction,
      Function<List<CO>, AF> aggregateFunction
  ) {
    this.cases = cases;
    this.caseFunction = caseFunction;
    this.aggregateFunction = aggregateFunction;
  }

  @Override
  public Function<List<CO>, AF> aggregateFunction() {
    return aggregateFunction;
  }

  @Override
  public BiFunction<S, C, CO> caseFunction() {
    return caseFunction;
  }

  @Override
  public IntFunction<C> caseProvider() {
    return i -> cases().get(i);
  }

  @Override
  public int nOfCases() {
    return cases().size();
  }

  public List<C> cases() {
    return cases;
  }

  @Override
  public int hashCode() {
    return Objects.hash(cases, caseFunction, aggregateFunction);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this)
      return true;
    if (obj == null || obj.getClass() != this.getClass())
      return false;
    @SuppressWarnings("rawtypes") var that = (ListCaseBasedFitness) obj;
    return Objects.equals(this.cases, that.cases) &&
        Objects.equals(this.caseFunction, that.caseFunction) &&
        Objects.equals(this.aggregateFunction, that.aggregateFunction);
  }

  @Override
  public String toString() {
    return "ListCaseBasedFitness[" +
        "cases=" + cases + ", " +
        "caseFunction=" + caseFunction + ", " +
        "aggregateFunction=" + aggregateFunction + ']';
  }

}
