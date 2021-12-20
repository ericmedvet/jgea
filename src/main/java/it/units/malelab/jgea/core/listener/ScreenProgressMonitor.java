package it.units.malelab.jgea.core.listener;

import it.units.malelab.jgea.core.util.TextPlotter;

import java.util.logging.Logger;

public class ScreenProgressMonitor implements ProgressMonitor {

  private final Logger L;

  public ScreenProgressMonitor(Logger L) {
    this.L = L;
  }

  public ScreenProgressMonitor() {
    L = Logger.getLogger(ProgressMonitor.class.getName());
  }

  @Override
  public void notify(double progress, String message) {
    L.info(String.format("Progress %s %s",
        TextPlotter.horizontalBar(progress, 0, 1, 8),
        message
    ));
  }

  @Override
  public void notify(double progress) {
    L.info(String.format("Progress %s",
        TextPlotter.horizontalBar(progress, 0, 1, 8)
    ));
  }
}
