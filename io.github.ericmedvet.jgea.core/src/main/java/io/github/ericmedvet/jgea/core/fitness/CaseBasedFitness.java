
package io.github.ericmedvet.jgea.core.fitness;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.IntStream;
public interface CaseBasedFitness<S, C, CO, AF> extends Function<S, AF> {

  Function<List<CO>, AF> aggregateFunction();

  BiFunction<S, C, CO> caseFunction();

  IntFunction<C> caseProvider();

  int nOfCases();

  @Override
  default AF apply(S s) {
    List<CO> outcomes = IntStream.range(0, nOfCases())
        .mapToObj(i -> caseFunction().apply(s, caseProvider().apply(i)))
        .toList();
    return aggregateFunction().apply(outcomes);
  }

}
