package io.github.ericmedvet.jgea.experimenter.listener.decoupled;

import io.github.ericmedvet.jgea.core.util.HashMapTable;
import io.github.ericmedvet.jgea.core.util.Table;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.SortedMap;

/**
 * @author "Eric Medvet" on 2023/11/03 for jgea
 */
public interface Source {

  SortedMap<LocalDateTime, DataItemInfo> dataItemInfos(DataItemKey dataItemKey, LocalDateTime from);

  Collection<DataItemKey> dataItemKeys(RunKey runKey);

  SortedMap<LocalDateTime, ExperimentInfo> experimentInfos(ExperimentKey experimentKey, LocalDateTime from);

  Collection<ExperimentKey> experimentKeys(ProcessKey processKey);

  SortedMap<LocalDateTime, LogInfo> logInfos(ProcessKey processKey, LocalDateTime from);

  SortedMap<LocalDateTime, MachineInfo> machineInfos(MachineKey machineKey, LocalDateTime from);

  Collection<MachineKey> machineKeys();

  SortedMap<LocalDateTime, ProcessInfo> processInfos(ProcessKey processKey, LocalDateTime from);

  Collection<ProcessKey> processKeys(MachineKey machineKey);

  Collection<RunKey> runKeys(ExperimentKey experimentKey);

  SortedMap<LocalDateTime, RunInfo> runInfos(RunKey runKey, LocalDateTime from);

  default Table<LocalDateTime, DataItemKey, Object> runData(RunKey runKey, LocalDateTime from) {
    Table<LocalDateTime, DataItemKey, Object> table = new HashMapTable<>();
    dataItemKeys(runKey).forEach(dik -> dataItemInfos(dik, from).forEach((t, dif) -> table.set(t, dik, dif.content())));
    return table;
  }
}
