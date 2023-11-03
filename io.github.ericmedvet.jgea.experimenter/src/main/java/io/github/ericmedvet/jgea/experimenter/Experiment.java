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

import io.github.ericmedvet.jgea.core.listener.ListenerFactory;
import io.github.ericmedvet.jgea.core.solver.POCPopulationState;
import io.github.ericmedvet.jnb.core.Discoverable;
import io.github.ericmedvet.jnb.core.Param;
import io.github.ericmedvet.jnb.core.ParamMap;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.function.BiFunction;

@Discoverable(prefixTemplate = "ea")
public record Experiment(
    @Param(value = "name", dS = "") String name,
    @Param("runs") List<Run<?, ?, ?, ?>> runs,
    @Param(value = "", injection = Param.Injection.MAP_WITH_DEFAULTS) ParamMap map,
    @Param(
        value = "listeners",
        dNPMs = {"ea.l.console()"})
    List<
        BiFunction<
            Experiment,
            ExecutorService,
            ListenerFactory<? super POCPopulationState<?, ?, ?, ?>, Run<?, ?, ?, ?>>>>
        listeners
) {}
