package it.units.malelab.jgea.core.listener;

import it.units.malelab.jgea.core.util.TextPlotter;

import java.util.logging.Logger;

public class LoggerProgressMonitor implements ProgressMonitor {

  private final static Logger L = Logger.getLogger(LoggerProgressMonitor.class.getName());

  @Override
  public void notify(double progress, String message) {
    L.info(String.format("Progress: %s %s%n", TextPlotter.horizontalBar(progress, 0, 1, 8), message));
  }

  @Override
  public void notify(double progress) {
    notify(progress, "");
  }
}
