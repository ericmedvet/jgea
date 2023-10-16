package io.github.ericmedvet.jgea.experimenter.net;

import java.io.Serializable;
public record ProcessInfo(
    String processName, String username, long usedMemory, long maxMemory
) implements Serializable {}
