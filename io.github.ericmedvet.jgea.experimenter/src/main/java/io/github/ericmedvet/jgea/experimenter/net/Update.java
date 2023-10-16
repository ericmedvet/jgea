
package io.github.ericmedvet.jgea.experimenter.net;

import io.github.ericmedvet.jgea.core.util.Progress;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
public record Update(
    long localTime,
    String runMap,
    int runIndex,
    Progress runProgress,
    boolean isRunning,
    Map<DataItemKey, List<Object>> dataItems,
    Map<PlotItemKey, List<PlotPoint>> plotItems
) implements Serializable {
  public record DataItemKey(String name, String format) implements Serializable {}

  public record PlotItemKey(String xName, String yName, double minX, double maxX) implements Serializable {}

  public record PlotPoint(double x, double y) implements Serializable {}
}
