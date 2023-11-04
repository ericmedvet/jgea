package io.github.ericmedvet.jgea.experimenter.listener.decoupled;

import java.time.LocalDateTime;
import java.util.*;

/**
 * @author "Eric Medvet" on 2023/11/03 for jgea
 */
public abstract class AbstractAutoPurgingSource implements Source {

  private final Map<MachineKey, SortedMap<LocalDateTime, MachineInfo>> machines;
  private final Map<ProcessKey, SortedMap<LocalDateTime, ProcessInfo>> processes;
  private final Map<ProcessKey, SortedMap<LocalDateTime, LogInfo>> logs;
  private final Map<ExperimentKey, SortedMap<LocalDateTime, ExperimentInfo>> experiments;
  private final Map<RunKey, SortedMap<LocalDateTime, RunInfo>> runs;
  private final Map<DataItemKey, SortedMap<LocalDateTime, DataItemInfo>> dataItems;

  public AbstractAutoPurgingSource() {
    this.machines = new LinkedHashMap<>();
    this.processes = new LinkedHashMap<>();
    this.logs = new LinkedHashMap<>();
    this.experiments = new LinkedHashMap<>();
    this.runs = new LinkedHashMap<>();
    this.dataItems = new LinkedHashMap<>();
  }

  private static <K, V> SortedMap<LocalDateTime, V> pop(Map<K, SortedMap<LocalDateTime, V>> map, LocalDateTime t, K k) {
    SortedMap<LocalDateTime, V> kMap = map.getOrDefault(k, Collections.emptySortedMap());
    if (kMap.isEmpty()) {
      return kMap;
    }
    SortedMap<LocalDateTime, V> rMap = kMap.subMap(t, LocalDateTime.MAX);
    kMap.headMap(t).keySet().forEach(kMap::remove);
    return rMap;
  }

  @Override
  public SortedMap<LocalDateTime, DataItemInfo> dataItemInfos(DataItemKey dataItemKey, LocalDateTime from) {
    return pop(dataItems, from, dataItemKey);
  }

  @Override
  public Collection<DataItemKey> dataItemKeys(RunKey runKey) {
    return dataItems.keySet().stream().filter(dik -> dik.runKey().equals(runKey)).toList();
  }

  @Override
  public SortedMap<LocalDateTime, ExperimentInfo> experimentInfos(ExperimentKey experimentKey, LocalDateTime from) {
    return pop(experiments, from, experimentKey);
  }

  @Override
  public Collection<ExperimentKey> experimentKeys(ProcessKey processKey) {
    return experiments.keySet().stream().filter(ek -> ek.processKey().equals(processKey)).toList();
  }

  @Override
  public SortedMap<LocalDateTime, LogInfo> logInfos(ProcessKey processKey, LocalDateTime from) {
    return pop(logs, from, processKey);
  }

  @Override
  public SortedMap<LocalDateTime, MachineInfo> machineInfos(MachineKey machineKey, LocalDateTime from) {
    return pop(machines, from, machineKey);
  }

  @Override
  public Collection<MachineKey> machineKeys() {
    return machines.keySet();
  }

  @Override
  public SortedMap<LocalDateTime, ProcessInfo> processInfos(ProcessKey processKey, LocalDateTime from) {
    return pop(processes, from, processKey);
  }

  @Override
  public Collection<ProcessKey> processKeys(MachineKey machineKey) {
    return processes.keySet().stream().filter(pk -> pk.machineKey().equals(machineKey)).toList();
  }

  @Override
  public Collection<RunKey> runKeys(ExperimentKey experimentKey) {
    return runs.keySet().stream().filter(rk -> rk.experimentKey().equals(experimentKey)).toList();
  }

  @Override
  public SortedMap<LocalDateTime, RunInfo> runInfos(RunKey runKey, LocalDateTime from) {
    return pop(runs, from, runKey);
  }

  @Override
  public Collection<ProcessKey> processKeys() {
    return processes.keySet();
  }

  @Override
  public Collection<ExperimentKey> experimentKeys() {
    return experiments.keySet();
  }

  @Override
  public Collection<RunKey> runKeys() {
    return runs.keySet();
  }

  @Override
  public Collection<DataItemKey> dataItemKeys() {
    return dataItems.keySet();
  }

  protected Map<DataItemKey, SortedMap<LocalDateTime, DataItemInfo>> getDataItems() {
    return dataItems;
  }

  protected Map<ExperimentKey, SortedMap<LocalDateTime, ExperimentInfo>> getExperiments() {
    return experiments;
  }

  protected Map<ProcessKey, SortedMap<LocalDateTime, LogInfo>> getLogs() {
    return logs;
  }

  protected Map<MachineKey, SortedMap<LocalDateTime, MachineInfo>> getMachines() {
    return machines;
  }

  protected Map<ProcessKey, SortedMap<LocalDateTime, ProcessInfo>> getProcesses() {
    return processes;
  }

  protected Map<RunKey, SortedMap<LocalDateTime, RunInfo>> getRuns() {
    return runs;
  }
}
