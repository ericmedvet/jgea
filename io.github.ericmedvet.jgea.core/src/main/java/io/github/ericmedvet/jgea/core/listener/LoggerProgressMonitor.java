
package io.github.ericmedvet.jgea.core.listener;

import io.github.ericmedvet.jgea.core.util.Progress;
import io.github.ericmedvet.jgea.core.util.TextPlotter;

import java.util.logging.Logger;

public class LoggerProgressMonitor implements ProgressMonitor {

  private final static Logger L = Logger.getLogger(LoggerProgressMonitor.class.getName());

  @Override
  public void notify(Progress progress, String message) {
    L.info(String.format("Progress: %s %s%n", TextPlotter.horizontalBar(progress.rate(), 0, 1, 8), message));
  }

}
