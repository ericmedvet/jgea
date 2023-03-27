package io.github.ericmedvet.jgea.experimenter.net;

import java.io.Serializable;

/**
 * @author "Eric Medvet" on 2023/03/27 for jgea
 */
public record MachineInfo(String machineName, int numberOfProcessors, double cpuLoad) implements Serializable {}
