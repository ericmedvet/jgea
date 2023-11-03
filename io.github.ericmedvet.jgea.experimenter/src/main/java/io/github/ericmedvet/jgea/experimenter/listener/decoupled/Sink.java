package io.github.ericmedvet.jgea.experimenter.listener.decoupled;

import io.github.ericmedvet.jgea.experimenter.listener.net.NetUtils;

/**
 * @author "Eric Medvet" on 2023/11/03 for jgea
 */
public interface Sink {
  void clear();

  void push(ProcessKey processKey, ProcessInfo processInfo);

  void push(RunKey runKey, RunInfo runInfo);
  ;

  void push(MachineInfo machineInfo);

  void push(DataItemKey dataItemKey, DataItemInfo dataItemInfo);

  void push(ExperimentKey experimentKey, ExperimentInfo experimentInfo);

  default MachineKey machineKey() {
    return new MachineKey(NetUtils.getMachineName());
  }

  default ProcessKey processKey() {
    return new ProcessKey(machineKey(), NetUtils.getProcessName());
  }
}
