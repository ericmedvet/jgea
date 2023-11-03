package io.github.ericmedvet.jgea.experimenter.listener.decoupled;

/**
 * @author "Eric Medvet" on 2023/11/03 for jgea
 */
public interface Sink {
  void clear();

  MachineKey machineKey();

  void push(MachineInfo machineInfo);
  void push(ExperimentKey experimentKey, ExperimentInfo experimentInfo);
  void push(ExperimentKey experimentKey, RunKey runKey, RunInfo runInfo);
  void push(ExperimentKey experimentKey, RunKey runKey, DataItemKey dataItemKey, DataItemInfo dataItemInfo);
}
