
package io.github.ericmedvet.jgea.core.problem;


import java.util.function.Function;
public interface ProblemWithValidation<S, Q> extends QualityBasedProblem<S, Q> {

  Function<S, Q> validationQualityFunction();

}
