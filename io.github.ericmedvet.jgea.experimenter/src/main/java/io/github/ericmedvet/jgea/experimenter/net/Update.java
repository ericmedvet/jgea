package io.github.ericmedvet.jgea.experimenter.net;

import java.io.Serializable;
import java.util.List;

/**
 * @author "Eric Medvet" on 2023/03/27 for jgea
 */
public record Update(long localTime, String runMap, int runIndex, List<Item> items) implements Serializable {}
