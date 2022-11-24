package it.units.malelab.jgea.experimenter;

import it.units.malelab.jgea.core.QualityBasedProblem;
import it.units.malelab.jgea.core.listener.Listener;
import it.units.malelab.jgea.core.solver.AbstractPopulationBasedIterativeSolver;
import it.units.malelab.jgea.core.solver.SolverException;
import it.units.malelab.jgea.core.solver.state.POSetPopulationState;
import it.units.malelab.jnb.core.Param;
import it.units.malelab.jnb.core.ParamMap;

import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.random.RandomGenerator;

/**
 * @author "Eric Medvet" on 2022/09/01 for 2d-robot-evolution
 */
public record Run<P extends QualityBasedProblem<S, Q>, G, S, Q>(
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
