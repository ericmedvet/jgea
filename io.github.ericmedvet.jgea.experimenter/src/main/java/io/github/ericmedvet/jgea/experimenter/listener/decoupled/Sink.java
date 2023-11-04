package io.github.ericmedvet.jgea.experimenter.listener.decoupled;

import io.github.ericmedvet.jgea.experimenter.listener.net.NetUtils;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * @author "Eric Medvet" on 2023/11/03 for jgea
 */
public interface Sink {
  void clear();

  void push(ProcessKey processKey, ProcessInfo processInfo);

  void push(ProcessKey processKey, LogInfo logInfo);

  void push(RunKey runKey, RunInfo runInfo);

  void push(MachineKey machineKey, MachineInfo machineInfo);

  void push(Map<DataItemKey, DataItemInfo> data);

  void push(ExperimentKey experimentKey, ExperimentInfo experimentInfo);

  default MachineKey machineKey() {
    return new MachineKey(NetUtils.getMachineName());
  }

  default ProcessKey processKey() {
    return new ProcessKey(machineKey(), NetUtils.getProcessName());
  }

  default void push() {
    push(new MachineInfo(NetUtils.getMachineName(), NetUtils.getNumberOfProcessors(), NetUtils.getCPULoad(),
        LocalDateTime.now()
    ));
    push(new ProcessInfo(
        NetUtils.getProcessName(),
        NetUtils.getUserName(),
        NetUtils.getProcessUsedMemory(),
        NetUtils.getProcessMaxMemory()
    ));
  }

  default void push(ProcessInfo processInfo) {
    push(processKey(), processInfo);
  }

  default void push(LogInfo logInfo) {
    push(processKey(), logInfo);
  }

  default void push(MachineInfo machineInfo) {
    push(machineKey(), machineInfo);
  }

}
