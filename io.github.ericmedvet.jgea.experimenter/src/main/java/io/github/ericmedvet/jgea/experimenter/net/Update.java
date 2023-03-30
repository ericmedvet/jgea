package io.github.ericmedvet.jgea.experimenter.net;

import io.github.ericmedvet.jgea.core.util.Progress;

import java.io.Serializable;
import java.util.List;

/**
 * @author "Eric Medvet" on 2023/03/27 for jgea
 */
public record Update(long localTime, String runMap, int runIndex, Progress runProgress, List<Item> items) implements Serializable {}
