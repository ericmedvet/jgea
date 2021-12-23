package it.units.malelab.jgea.core.listener;

import it.units.malelab.jgea.core.util.TextPlotter;

import java.io.PrintStream;

public class ScreenProgressMonitor implements ProgressMonitor {

  private final PrintStream ps;

  public ScreenProgressMonitor(PrintStream ps) {
    this.ps = ps;
  }

  @Override
  public void notify(double progress, String message) {
    ps.printf(
        "Progress: %s %s%n",
        TextPlotter.horizontalBar(progress, 0, 1, 8),
        message
    );
  }

  @Override
  public void notify(double progress) {
    notify(progress, "");
  }
}
