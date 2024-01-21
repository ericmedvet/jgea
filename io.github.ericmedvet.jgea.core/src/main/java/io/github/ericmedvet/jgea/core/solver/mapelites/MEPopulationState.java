/*-
 * ========================LICENSE_START=================================
 * jgea-core
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
package io.github.ericmedvet.jgea.core.solver.mapelites;

import io.github.ericmedvet.jgea.core.problem.QualityBasedProblem;
import io.github.ericmedvet.jgea.core.solver.Individual;
import io.github.ericmedvet.jgea.core.solver.POCPopulationState;
import java.util.List;
import java.util.Map;

/**
 * @author "Eric Medvet" on 2023/10/28 for jgea
 */
public interface MEPopulationState<G, S, Q, P extends QualityBasedProblem<S, Q>>
    extends POCPopulationState<Individual<G, S, Q>, G, S, Q, P> {
  Map<List<Integer>, Individual<G, S, Q>> mapOfElites();

  List<MapElites.Descriptor<G, S, Q>> descriptors();
}
