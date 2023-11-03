package io.github.ericmedvet.jgea.experimenter.listener.decoupled;

import java.time.LocalDateTime;
import java.util.*;

/**
 * @author "Eric Medvet" on 2023/11/03 for jgea
 */
public class DirectSinkSource implements Sink, Source {

  private final Map<MachineKey, SortedMap<LocalDateTime, MachineInfo>> machines;
  private final Map<ProcessKey, SortedMap<LocalDateTime, ProcessInfo>> processes;
  private final Map<ProcessKey, SortedMap<LocalDateTime, LogInfo>> logs;
  private final Map<ExperimentKey, SortedMap<LocalDateTime, ExperimentInfo>> experiments;
  private final Map<RunKey, SortedMap<LocalDateTime, RunInfo>> runs;
  private final Map<DataItemKey, SortedMap<LocalDateTime, DataItemInfo>> dataItems;

  public DirectSinkSource() {
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

  private static <K, V> void put(Map<K, SortedMap<LocalDateTime, V>> map, LocalDateTime t, K k, V v) {
    map.computeIfAbsent(k, k1 -> new TreeMap<>()).put(t, v);
  }

  @Override
  public void clear() {
    machines.clear();
    processes.clear();
    logs.clear();
    experiments.clear();
    runs.clear();
    dataItems.clear();
  }

  @Override
  public void push(ProcessKey processKey, ProcessInfo processInfo) {
    put(processes, LocalDateTime.now(), processKey, processInfo);
  }

  @Override
  public void push(ProcessKey processKey, LogInfo logInfo) {
    put(logs, LocalDateTime.now(), processKey, logInfo);
  }

  @Override
  public void push(RunKey runKey, RunInfo runInfo) {
    put(runs, LocalDateTime.now(), runKey, runInfo);
  }

  @Override
  public void push(MachineInfo machineInfo) {
    put(machines, LocalDateTime.now(), machineKey(), machineInfo);
  }

  @Override
  public void push(Map<DataItemKey, DataItemInfo> data) {
    LocalDateTime t = LocalDateTime.now();
    data.forEach((k, v) -> put(dataItems, t, k, v));
  }

  @Override
  public void push(ExperimentKey experimentKey, ExperimentInfo experimentInfo) {
    put(experiments, LocalDateTime.now(), experimentKey, experimentInfo);
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
}
