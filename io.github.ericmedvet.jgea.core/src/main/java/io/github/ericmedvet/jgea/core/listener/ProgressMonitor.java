
package io.github.ericmedvet.jgea.core.listener;

import io.github.ericmedvet.jgea.core.util.Progress;

import java.util.List;

@FunctionalInterface
public interface ProgressMonitor {

  void notify(Progress progress, String message);

  static ProgressMonitor all(List<ProgressMonitor> progressMonitors) {
    return (progress, message) -> progressMonitors.forEach(m -> m.notify(progress, message));
  }

  default ProgressMonitor and(ProgressMonitor other) {
    return all(List.of(this, other));
  }

  default void notify(int i, int n, String message) {
    notify(new Progress(0, n, i), message);
  }

  default void notify(int i, int n) {
    notify(new Progress(0, n, i));
  }

  default void notify(Progress progress) {
    notify(progress, "");
  }
}
