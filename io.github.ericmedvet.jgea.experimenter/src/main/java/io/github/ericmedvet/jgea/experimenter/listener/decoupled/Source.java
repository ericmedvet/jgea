package io.github.ericmedvet.jgea.experimenter.listener.decoupled;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.SortedMap;

/**
 * @author "Eric Medvet" on 2023/11/03 for jgea
 */
public interface Source {

  Collection<DataItemKey> dataItemKeys(RunKey runKey);

  Collection<ExperimentKey> experimentKeys(ProcessKey processKey);

  SortedMap<LocalDateTime, MachineInfo> pop(MachineKey machineKey, LocalDateTime from);

  SortedMap<LocalDateTime, ProcessInfo> pop(ProcessKey experimentKey, LocalDateTime from);

  Collection<MachineKey> machineKeys();

  SortedMap<LocalDateTime, ExperimentInfo> pop(ExperimentKey experimentKey, LocalDateTime from);

  SortedMap<LocalDateTime, RunInfo> pop(RunKey runKey, LocalDateTime from);

  SortedMap<LocalDateTime, DataItemInfo> pop(DataItemKey dataItemKey, LocalDateTime from);

  Collection<ProcessKey> processKeys(MachineKey machineKey);

  Collection<RunKey> runKeys(ExperimentKey experimentKey);
}
