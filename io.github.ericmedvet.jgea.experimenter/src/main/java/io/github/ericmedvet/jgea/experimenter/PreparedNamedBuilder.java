
package io.github.ericmedvet.jgea.experimenter;

import io.github.ericmedvet.jgea.experimenter.builders.*;
import io.github.ericmedvet.jnb.core.NamedBuilder;

import java.util.List;
public class PreparedNamedBuilder {

  private final static NamedBuilder<Object> NB = NamedBuilder.empty()
      .and(List.of("ea"), NamedBuilder.empty()
          .and(List.of("randomGenerator", "rg"), NamedBuilder.fromUtilityClass(RandomGenerators.class))
          .and(List.of("problem", "p"), NamedBuilder.empty()
              .and(NamedBuilder.fromUtilityClass(Problems.class))
              .and(List.of("univariateRegression", "ur"), NamedBuilder.fromUtilityClass(UnivariateRegressionProblems.class))
              .and(List.of("multivariateRegression", "mr"), NamedBuilder.fromUtilityClass(MultivariateRegressionProblems.class))
              .and(List.of("synthetic", "s"), NamedBuilder.fromUtilityClass(SyntheticProblems.class))
          )
          .and(List.of("dataset", "d"), NamedBuilder.empty()
              .and(List.of("numerical", "num"), NamedBuilder.fromUtilityClass(NumericalDatasets.class))
          )
          .and(List.of("solver", "s"), NamedBuilder.fromUtilityClass(Solvers.class))
          .and(List.of("grammar"), NamedBuilder.fromUtilityClass(Grammars.class))
          .and(List.of("mapper", "m"), NamedBuilder.fromUtilityClass(Mappers.class))
          .and(List.of("listener", "l"), NamedBuilder.fromUtilityClass(Listeners.class))
          .and(List.of("function", "f"), NamedBuilder.fromUtilityClass(Functions.class))
          .and(List.of("namedFunction", "nf"), NamedBuilder.fromUtilityClass(NamedFunctions.class))
          .and(List.of("plot"), NamedBuilder.fromUtilityClass(Plots.class))
          .and(NamedBuilder.fromClass(Experiment.class))
          .and(NamedBuilder.fromClass(Run.class))
          .and(NamedBuilder.fromClass(RunOutcome.class))
      );

  private PreparedNamedBuilder() {
  }

  public static NamedBuilder<Object> get() {
    return NB;
  }
}
