package it.units.malelab.jgea.experimenter;

import it.units.malelab.jgea.core.listener.ListenerFactory;
import it.units.malelab.jgea.core.solver.state.POSetPopulationState;
import it.units.malelab.jnb.core.Param;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.function.BiFunction;

/**
 * @author "Eric Medvet" on 2022/09/01 for 2d-robot-evolution
 */
public record Experiment(
    @Param("name") String name,
    @Param("runs") List<Run<?, ?, ?, ?>> runs,
    @Param("listeners") List<BiFunction<
        Experiment,
        ExecutorService,
        ListenerFactory<? extends POSetPopulationState<?, ?, ?>, Run<?, ?, ?, ?>>
        >> listeners
) {}
