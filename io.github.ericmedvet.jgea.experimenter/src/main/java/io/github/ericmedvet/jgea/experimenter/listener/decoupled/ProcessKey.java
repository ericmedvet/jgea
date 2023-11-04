package io.github.ericmedvet.jgea.experimenter.listener.decoupled;

import io.github.ericmedvet.jgea.experimenter.listener.net.NetUtils;

/**
 * @author "Eric Medvet" on 2023/11/03 for jgea
 */
public record ProcessKey(MachineKey machineKey, String value) {
  public static ProcessKey get() {
    return new ProcessKey(MachineKey.get(), NetUtils.getProcessName());
  }
}
