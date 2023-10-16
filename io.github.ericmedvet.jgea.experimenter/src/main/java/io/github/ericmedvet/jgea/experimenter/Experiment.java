
package io.github.ericmedvet.jgea.experimenter;

import io.github.ericmedvet.jgea.core.listener.ListenerFactory;
import io.github.ericmedvet.jgea.core.solver.state.POSetPopulationState;
import io.github.ericmedvet.jnb.core.Param;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.function.BiFunction;
public record Experiment(
    @Param(value = "name", dS = "") String name,
    @Param("runs") List<Run<?, ?, ?, ?>> runs,
    @Param(value = "listeners", dNPMs = {"ea.l.console()"}) List<BiFunction<
        Experiment,
        ExecutorService,
        ListenerFactory<? super POSetPopulationState<?, ?, ?>, Run<?, ?, ?, ?>>
        >> listeners
) {}
