package it.units.malelab.jgea.core.listener;

import java.util.List;

@FunctionalInterface
public interface ProgressMonitor {

  void notify(double progress, String message);

  default void notify(double progress) {
    notify(progress, null);
  }

  static ProgressMonitor all(List<ProgressMonitor> progressMonitors) {
    return new ProgressMonitor() {
      @Override
      public void notify(double progress, String message) {
        progressMonitors.forEach(m -> m.notify(progress, message));
      }

      @Override
      public void notify(double progress) {
        progressMonitors.forEach(m -> m.notify(progress));
      }
    };
  }

  default ProgressMonitor and(ProgressMonitor other) {
    return all(List.of(this, other));
  }
}
