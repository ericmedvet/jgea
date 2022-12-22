/*
 * Copyright 2022 eric
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.ericmedvet.jgea.experimenter;

import io.github.ericmedvet.jgea.core.listener.ListenerFactory;
import io.github.ericmedvet.jgea.core.solver.state.POSetPopulationState;
import io.github.ericmedvet.jnb.core.Param;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.function.BiFunction;

/**
 * @author "Eric Medvet" on 2022/09/01 for 2d-robot-evolution
 */
public record Experiment(
    @Param("name") String name,
    @Param("runs") List<Run<?, ?, ?, ?>> runs,
    @Param(value = "listeners", dNPMs = {"ea.l.console()"}) List<BiFunction<
        Experiment,
        ExecutorService,
        ListenerFactory<? super POSetPopulationState<?, ?, ?>, Run<?, ?, ?, ?>>
        >> listeners
) {}
