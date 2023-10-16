package io.github.ericmedvet.jgea.experimenter.net;

import java.io.Serializable;
public record MachineInfo(String machineName, int numberOfProcessors, double cpuLoad) implements Serializable {}
