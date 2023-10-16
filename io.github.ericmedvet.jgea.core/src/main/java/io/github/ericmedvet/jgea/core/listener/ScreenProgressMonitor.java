
package io.github.ericmedvet.jgea.core.listener;

import io.github.ericmedvet.jgea.core.util.Progress;
import io.github.ericmedvet.jgea.core.util.TextPlotter;

import java.io.PrintStream;

public class ScreenProgressMonitor implements ProgressMonitor {

  private final PrintStream ps;

  public ScreenProgressMonitor(PrintStream ps) {
    this.ps = ps;
  }

  @Override
  public void notify(Progress progress, String message) {
    ps.printf("Progress: %s %s%n", TextPlotter.horizontalBar(progress.rate(), 0, 1, 8), message);
  }

}
