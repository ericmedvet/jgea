package io.github.ericmedvet.jgea.experimenter.listener.decoupled;

import io.github.ericmedvet.jgea.core.listener.Listener;
import io.github.ericmedvet.jgea.core.listener.ListenerFactory;
import io.github.ericmedvet.jgea.core.listener.NamedFunction;
import io.github.ericmedvet.jgea.core.solver.POCPopulationState;
import io.github.ericmedvet.jgea.core.util.Progress;
import io.github.ericmedvet.jgea.experimenter.Experiment;
import io.github.ericmedvet.jgea.experimenter.Run;
import io.github.ericmedvet.jgea.experimenter.listener.net.NetUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author "Eric Medvet" on 2023/11/03 for jgea
 */
public class ListenerFactorySink<G, S, Q> implements ListenerFactory<POCPopulationState<?, G, S, Q>, Run<?, G, S, Q>> {
  private static final Logger L = Logger.getLogger(ListenerFactorySink.class.getName());
  private final List<NamedFunction<? super POCPopulationState<?, G, S, Q>, ?>> stateFunctions;
  private final List<NamedFunction<? super Run<?, G, S, Q>, ?>> runFunctions;
  private final Experiment experiment;
  private final Sink sink;

  public ListenerFactorySink(
      List<NamedFunction<? super POCPopulationState<?, G, S, Q>, ?>> stateFunctions,
      List<NamedFunction<? super Run<?, G, S, Q>, ?>> runFunctions,
      Experiment experiment,
      Sink sink
  ) {
    this.stateFunctions = stateFunctions;
    this.runFunctions = runFunctions;
    this.experiment = experiment;
    this.sink = sink;
    sink.push(
        new MachineInfo(
            NetUtils.getMachineName(),
            NetUtils.getNumberOfProcessors(),
            NetUtils.getCPULoad(),
            NetUtils.getProcessName(),
            NetUtils.getUserName(),
            NetUtils.getProcessUsedMemory(),
            NetUtils.getProcessMaxMemory(),
            LocalDateTime.now()
        )
    );
    sink.push(key(experiment), new ExperimentInfo(experiment.map().toString(), LocalDateTime.now()));
  }

  private static ExperimentKey key(Experiment experiment) {
    return new ExperimentKey(experiment.name());
  }

  private static RunKey key(Run<?, ?, ?, ?> run) {
    return new RunKey("r%04d".formatted(run.index()));
  }

  @Override
  public Listener<POCPopulationState<?, G, S, Q>> build(Run<?, G, S, Q> run) {
    ExperimentKey eKey = key(experiment);
    RunKey runKey = key(run);
    sink.push(eKey, runKey, new RunInfo(run.index(), run.map().toString(), LocalDateTime.now(), Progress.NA));
    runFunctions.forEach(f -> sink.push(
        eKey,
        runKey,
        new DataItemKey(f.getName(), f.getFormat()),
        new DataItemInfo(f.apply(run))
    ));
    return state -> {
      sink.push(eKey, runKey, new RunInfo(run.index(), run.map().toString(), LocalDateTime.now(), state.progress()));
      stateFunctions.forEach(f -> sink.push(
          eKey,
          runKey,
          new DataItemKey(f.getName(), f.getFormat()),
          new DataItemInfo(f.apply(state))
      ));
    };
  }
}
