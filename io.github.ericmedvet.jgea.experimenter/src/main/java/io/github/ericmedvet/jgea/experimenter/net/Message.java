package io.github.ericmedvet.jgea.experimenter.net;

import java.io.Serializable;
import java.util.List;
public record Message(
    long localTime,
    MachineInfo machineInfo,
    ProcessInfo processInfo,
    double pollInterval,
    int nOfRuns,
    List<Update> updates
) implements Serializable {}
