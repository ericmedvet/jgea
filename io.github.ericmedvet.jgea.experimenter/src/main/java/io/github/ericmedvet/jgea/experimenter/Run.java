package io.github.ericmedvet.jgea.experimenter;

import io.github.ericmedvet.jgea.core.QualityBasedProblem;
import io.github.ericmedvet.jgea.core.listener.Listener;
import io.github.ericmedvet.jgea.core.solver.AbstractPopulationBasedIterativeSolver;
import io.github.ericmedvet.jgea.core.solver.SolverException;
import io.github.ericmedvet.jgea.core.solver.state.POSetPopulationState;
import io.github.ericmedvet.jnb.core.Param;
import io.github.ericmedvet.jnb.core.ParamMap;

import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.random.RandomGenerator;

/**
 * @author "Eric Medvet" on 2022/09/01 for 2d-robot-evolution
 */
public record Run<P extends QualityBasedProblem<S, Q>, G, S, Q>(
    @Param("name") String name,
    @Param("solver") AbstractPopulationBasedIterativeSolver<? extends POSetPopulationState<G, S, Q>, P, G, S, Q> solver,
    @Param("problem") P problem,
    @Param("randomGenerator") RandomGenerator randomGenerator,
    @Param(value = "", injection = Param.Injection.MAP) ParamMap map
) {

  public Collection<S> run(
      ExecutorService executorService,
      Listener<? super POSetPopulationState<G, S, Q>> listener
  ) throws SolverException {
    return solver.solve(
        problem,
        randomGenerator,
        executorService,
        listener
    );
  }

}
