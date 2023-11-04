package io.github.ericmedvet.jgea.experimenter.listener.decoupled;

import io.github.ericmedvet.jgea.experimenter.listener.net.NetUtils;

/**
 * @author "Eric Medvet" on 2023/11/03 for jgea
 */
public record MachineKey(String value) {
  public static MachineKey get() {
    return new MachineKey(NetUtils.getMachineName());
  }
}
