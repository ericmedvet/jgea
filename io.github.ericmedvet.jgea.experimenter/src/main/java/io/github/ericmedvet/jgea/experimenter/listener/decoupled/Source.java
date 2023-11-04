package io.github.ericmedvet.jgea.experimenter.listener.decoupled;

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

  default Collection<DataItemKey> dataItemKeys() {
    return runKeys().stream().map(this::dataItemKeys).flatMap(Collection::stream).toList();
  }

  default Collection<ExperimentKey> experimentKeys() {
    return processKeys().stream().map(this::experimentKeys).flatMap(Collection::stream).toList();
  }

  default Collection<ProcessKey> processKeys() {
    return machineKeys().stream().map(this::processKeys).flatMap(Collection::stream).toList();
  }

  default Collection<RunKey> runKeys() {
    return experimentKeys().stream().map(this::runKeys).flatMap(Collection::stream).toList();
  }

}
