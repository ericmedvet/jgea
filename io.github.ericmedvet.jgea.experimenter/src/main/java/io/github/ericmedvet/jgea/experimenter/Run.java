/*-
 * ========================LICENSE_START=================================
 * jgea-experimenter
 * %%
 * Copyright (C) 2018 - 2023 Eric Medvet
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */

package io.github.ericmedvet.jgea.experimenter;

import io.github.ericmedvet.jgea.core.listener.Listener;
import io.github.ericmedvet.jgea.core.problem.ProblemWithExampleSolution;
import io.github.ericmedvet.jgea.core.problem.QualityBasedProblem;
import io.github.ericmedvet.jgea.core.solver.AbstractPopulationBasedIterativeSolver;
import io.github.ericmedvet.jgea.core.solver.IterativeSolver;
import io.github.ericmedvet.jgea.core.solver.POCPopulationState;
import io.github.ericmedvet.jgea.core.solver.SolverException;
import io.github.ericmedvet.jnb.core.Discoverable;
import io.github.ericmedvet.jnb.core.Param;
import io.github.ericmedvet.jnb.core.ParamMap;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.random.RandomGenerator;

@Discoverable(prefixTemplate = "ea")
public record Run<P extends QualityBasedProblem<S, Q>, G, S, Q>(
    @Param(value = "", injection = Param.Injection.INDEX) int index,
    @Param(value = "name", dS = "") String name,
    @Param("solver")
        Function<
                S,
                ? extends
                    AbstractPopulationBasedIterativeSolver<
                        ? extends POCPopulationState<?, G, S, Q>, P, ?, G, S, Q>>
            solver,
    @Param("problem") P problem,
    @Param("randomGenerator") RandomGenerator randomGenerator,
    @Param(value = "", injection = Param.Injection.MAP_WITH_DEFAULTS) ParamMap map) {

  public Collection<S> run(ExecutorService executorService, Listener<? super POCPopulationState<?, G, S, Q>> listener)
      throws SolverException {
    IterativeSolver<? extends POCPopulationState<?, G, S, Q>, P, S> iterativeSolver;
    if (problem instanceof ProblemWithExampleSolution<?> pwes) {
      //noinspection unchecked
      iterativeSolver = solver.apply((S) pwes.example());
    } else {
      iterativeSolver = solver.apply(null);
    }
    return iterativeSolver.solve(problem, randomGenerator, executorService, listener);
  }
}
