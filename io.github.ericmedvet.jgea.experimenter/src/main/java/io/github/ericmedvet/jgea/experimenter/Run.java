
package io.github.ericmedvet.jgea.experimenter;

import io.github.ericmedvet.jgea.core.listener.Listener;
import io.github.ericmedvet.jgea.core.problem.ProblemWithExampleSolution;
import io.github.ericmedvet.jgea.core.problem.QualityBasedProblem;
import io.github.ericmedvet.jgea.core.solver.AbstractPopulationBasedIterativeSolver;
import io.github.ericmedvet.jgea.core.solver.IterativeSolver;
import io.github.ericmedvet.jgea.core.solver.SolverException;
import io.github.ericmedvet.jgea.core.solver.state.POSetPopulationState;
import io.github.ericmedvet.jnb.core.Param;
import io.github.ericmedvet.jnb.core.ParamMap;

import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.random.RandomGenerator;
public record Run<P extends QualityBasedProblem<S, Q>, G, S, Q>(
    @Param(value = "", injection = Param.Injection.INDEX) int index,
    @Param(value = "name", dS = "") String name,
    @Param("solver") Function<S, ? extends AbstractPopulationBasedIterativeSolver<? extends POSetPopulationState<G, S, Q>, P, G, S, Q>> solver,
    @Param("problem") P problem,
    @Param("randomGenerator") RandomGenerator randomGenerator,
    @Param(value = "", injection = Param.Injection.MAP_WITH_DEFAULTS) ParamMap map
) {

  public Collection<S> run(
      ExecutorService executorService,
      Listener<? super POSetPopulationState<G, S, Q>> listener
  ) throws SolverException {
    IterativeSolver<? extends POSetPopulationState<G, S, Q>, P, S> iterativeSolver;
    if (problem instanceof ProblemWithExampleSolution<?> pwes) {
      //noinspection unchecked
      iterativeSolver = solver.apply((S)pwes.example());
    } else {
      iterativeSolver = solver.apply(null);
    }
    return iterativeSolver.solve(
        problem,
        randomGenerator,
        executorService,
        listener
    );
  }

}
