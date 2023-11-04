package io.github.ericmedvet.jgea.experimenter.listener.decoupled;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author "Eric Medvet" on 2023/11/04 for jgea
 */
public class DirectSinkSource extends AbstractAutoPurgingSource implements Sink{
  private static <K, V> void put(Map<K, SortedMap<LocalDateTime, V>> map, LocalDateTime t, K k, V v) {
    map.computeIfAbsent(k, k1 -> new TreeMap<>()).put(t, v);
  }

  @Override
  public void clear() {
    getMachines().clear();
    getProcesses().clear();
    getLogs().clear();
    getExperiments().clear();
    getRuns().clear();
    getDataItems().clear();
  }

  @Override
  public void push(ProcessKey processKey, ProcessInfo processInfo) {
    put(getProcesses(), LocalDateTime.now(), processKey, processInfo);
  }

  @Override
  public void push(ProcessKey processKey, LogInfo logInfo) {
    put(getLogs(), LocalDateTime.now(), processKey, logInfo);
  }

  @Override
  public void push(RunKey runKey, RunInfo runInfo) {
    put(getRuns(), LocalDateTime.now(), runKey, runInfo);
  }

  @Override
  public void push(MachineKey machineKey, MachineInfo machineInfo) {
    put(getMachines(), LocalDateTime.now(), machineKey, machineInfo);
  }

  @Override
  public void push(Map<DataItemKey, DataItemInfo> data) {
    LocalDateTime t = LocalDateTime.now();
    data.forEach((k, v) -> put(getDataItems(), t, k, v));
  }

  @Override
  public void push(ExperimentKey experimentKey, ExperimentInfo experimentInfo) {
    put(getExperiments(), LocalDateTime.now(), experimentKey, experimentInfo);
  }

}
