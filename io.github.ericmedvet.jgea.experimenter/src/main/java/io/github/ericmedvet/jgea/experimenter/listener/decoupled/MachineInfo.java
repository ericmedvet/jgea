package io.github.ericmedvet.jgea.experimenter.listener.decoupled;

import java.time.LocalDateTime;

/**
 * @author "Eric Medvet" on 2023/11/03 for jgea
 */
public record MachineInfo(String machineName, int numberOfProcessors, double cpuLoad, LocalDateTime localDateTime) {}
