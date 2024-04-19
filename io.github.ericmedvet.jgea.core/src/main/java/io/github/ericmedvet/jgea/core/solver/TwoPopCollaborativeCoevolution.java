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

import io.github.ericmedvet.jgea.core.problem.QualityBasedProblem;
import io.github.ericmedvet.jgea.core.util.Pair;

public interface TwoPopCollaborativeCoevolution<
    T1 extends POCPopulationState<Individual<G1, S1, Q>, G1, S1, Q, QualityBasedProblem<S1, Q>>,
    T2 extends POCPopulationState<Individual<G2, S2, Q>, G2, S2, Q, QualityBasedProblem<S2, Q>>,
    G1,
    G2,
    S1,
    S2,
    T extends TwoCoCoevolutionPopulationState<G1, G2, S1, S2, S, Q, QualityBasedProblem<S, Q>>,
    S,
    Q>
    extends AbstractPopulationBasedIterativeSolver<
    T,
    QualityBasedProblem<S, Q>,
    Individual<Pair<G1, G2>, S, Q>,
    Pair<G1, G2>,
    S,
    Q
    > {
}
