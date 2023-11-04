package io.github.ericmedvet.jgea.experimenter.listener.decoupled;

/**
 * @author "Eric Medvet" on 2023/11/03 for jgea
 */
public record ProcessInfo(String processName, String username, long usedMemory, long maxMemory) {}