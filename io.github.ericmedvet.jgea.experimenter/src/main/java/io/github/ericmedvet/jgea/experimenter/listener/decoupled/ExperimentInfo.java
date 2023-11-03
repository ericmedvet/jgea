package io.github.ericmedvet.jgea.experimenter.listener.decoupled;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * @author "Eric Medvet" on 2023/11/03 for jgea
 */
public record ExperimentInfo(String map, Map<String, String> formats, LocalDateTime startLocalDateTime) {}
