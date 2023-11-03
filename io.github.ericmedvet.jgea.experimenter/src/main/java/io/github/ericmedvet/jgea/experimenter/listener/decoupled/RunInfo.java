package io.github.ericmedvet.jgea.experimenter.listener.decoupled;

import io.github.ericmedvet.jgea.core.util.Progress;

import java.time.LocalDateTime;

/**
 * @author "Eric Medvet" on 2023/11/03 for jgea
 */
public
record RunInfo(int index, String map, LocalDateTime startLocalDateTime, Progress progress) {}
