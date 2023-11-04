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
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author "Eric Medvet" on 2023/11/03 for jgea
 */
public class SinkListenerFactory<G, S, Q> implements ListenerFactory<POCPopulationState<?, G, S, Q>, Run<?, G, S, Q>> {
  private final List<NamedFunction<? super POCPopulationState<?, G, S, Q>, ?>> stateFunctions;
  private final List<NamedFunction<? super Run<?, G, S, Q>, ?>> runFunctions;
  private final Experiment experiment;
  private final Sink<MachineKey, MachineInfo> machineSink;
  private final Sink<ProcessKey, ProcessInfo> processSink;
  private final Sink<ProcessKey, LogInfo> logSink;
  private final Sink<ExperimentKey, ExperimentInfo> experimentSink;
  private final Sink<RunKey, RunInfo> runSink;
  private final Sink<DataItemKey, DataItemInfo> dataItemSink;

  public SinkListenerFactory(
      List<NamedFunction<? super POCPopulationState<?, G, S, Q>, ?>> stateFunctions,
      List<NamedFunction<? super Run<?, G, S, Q>, ?>> runFunctions,
      Experiment experiment,
      Sink<MachineKey, MachineInfo> machineSink,
      Sink<ProcessKey, ProcessInfo> processSink,
      Sink<ProcessKey, LogInfo> logSink,
      Sink<ExperimentKey, ExperimentInfo> experimentSink,
      Sink<RunKey, RunInfo> runSink,
      Sink<DataItemKey, DataItemInfo> dataItemSink
  ) {
    // TODO add process and log entries forwarding
    this.stateFunctions = stateFunctions;
    this.runFunctions = runFunctions;
    this.experiment = experiment;
    this.machineSink = machineSink;
    this.processSink = processSink;
    this.logSink = logSink;
    this.experimentSink = experimentSink;
    this.runSink = runSink;
    this.dataItemSink = dataItemSink;
    pushMachineAndProcess();
    experimentSink.push(
        experimentKey(),
        new ExperimentInfo(
            experiment.map().toString(),
            Stream.of(stateFunctions, runFunctions)
                .flatMap(List::stream)
                .collect(Collectors.toMap(NamedFunction::getName, NamedFunction::getFormat)),
            LocalDateTime.now()
        )
    );
  }

  @Override
  public Listener<POCPopulationState<?, G, S, Q>> build(Run<?, G, S, Q> run) {
    RunKey runKey = runKey(run);
    LocalDateTime startDateTime = LocalDateTime.now();
    pushMachineAndProcess();
    runSink.push(runKey, new RunInfo(run.index(), startDateTime, Progress.NA, false));
    runFunctions.forEach(f -> dataItemSink.push(new DataItemKey(runKey, f.getName()), new DataItemInfo(f.apply(run))));
    return new Listener<>() {
      @Override
      public void listen(POCPopulationState<?, G, S, Q> state) {
        pushMachineAndProcess();
        runSink.push(runKey, new RunInfo(run.index(), startDateTime, state.progress(), false));
        stateFunctions.forEach(f -> dataItemSink.push(
            new DataItemKey(runKey, f.getName()),
            new DataItemInfo(f.apply(state))
        ));
      }

      @Override
      public void done() {
        pushMachineAndProcess();
        runSink.push(runKey, new RunInfo(run.index(), startDateTime, Progress.NA, true));
      }
    };
  }

  private ExperimentKey experimentKey() {
    return new ExperimentKey(ProcessKey.get(), experiment.name());
  }

  private void pushMachineAndProcess() {
    machineSink.push(
        MachineKey.get(),
        new MachineInfo(
            NetUtils.getMachineName(),
            NetUtils.getNumberOfProcessors(),
            NetUtils.getCPULoad(),
            LocalDateTime.now()
        )
    );
    processSink.push(
        ProcessKey.get(),
        new ProcessInfo(
            NetUtils.getProcessName(),
            NetUtils.getUserName(),
            NetUtils.getProcessUsedMemory(),
            NetUtils.getProcessMaxMemory()
        )
    );
  }

  private RunKey runKey(Run<?, ?, ?, ?> run) {
    return new RunKey(experimentKey(), "r%04d".formatted(run.index()));
  }
}
