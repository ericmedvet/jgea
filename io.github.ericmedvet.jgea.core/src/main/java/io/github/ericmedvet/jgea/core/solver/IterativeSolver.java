/*-
 * ========================LICENSE_START=================================
 * jgea-core
 * %%
 * Copyright (C) 2018 - 2024 Eric Medvet
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
package io.github.ericmedvet.jgea.core.solver;

import io.github.ericmedvet.jgea.core.listener.Listener;
import io.github.ericmedvet.jgea.core.problem.Problem;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.random.RandomGenerator;

public interface IterativeSolver<T extends State, P extends Problem<S>, S> extends Solver<P, S> {

  Collection<S> extractSolutions(P problem, RandomGenerator random, ExecutorService executor, T state)
      throws SolverException;

  T init(P problem, RandomGenerator random, ExecutorService executor) throws SolverException;

  boolean terminate(P problem, RandomGenerator random, ExecutorService executor, T state) throws SolverException;

  T update(P problem, RandomGenerator random, ExecutorService executor, T state) throws SolverException;

  @Override
  default Collection<S> solve(P problem, RandomGenerator random, ExecutorService executor) throws SolverException {
    return solve(problem, random, executor, Listener.deaf());
  }

  default Collection<S> solve(
      P problem, RandomGenerator random, ExecutorService executor, Listener<? super T> listener)
      throws SolverException {
    T state = init(problem, random, executor);
    listener.listen(state);
    while (!terminate(problem, random, executor, state)) {
      state = update(problem, random, executor, state);
      listener.listen(state);
    }
    listener.done();
    return extractSolutions(problem, random, executor, state);
  }

  default <P2 extends Problem<S>> IterativeSolver<T, P2, S> with(Function<P2, P> problemTransformer) {
    IterativeSolver<T, P, S> thisIterativeSolver = this;
    return new IterativeSolver<>() {
      @Override
      public Collection<S> extractSolutions(P2 problem, RandomGenerator random, ExecutorService executor, T state)
          throws SolverException {
        return thisIterativeSolver.extractSolutions(problemTransformer.apply(problem), random, executor, state);
      }

      @Override
      public T init(P2 problem, RandomGenerator random, ExecutorService executor) throws SolverException {
        return thisIterativeSolver.init(problemTransformer.apply(problem), random, executor);
      }

      @Override
      public boolean terminate(P2 problem, RandomGenerator random, ExecutorService executor, T state)
          throws SolverException {
        return thisIterativeSolver.terminate(problemTransformer.apply(problem), random, executor, state);
      }

      @Override
      public T update(P2 problem, RandomGenerator random, ExecutorService executor, T state)
          throws SolverException {
        return thisIterativeSolver.update(problemTransformer.apply(problem), random, executor, state);
      }
    };
  }
}
