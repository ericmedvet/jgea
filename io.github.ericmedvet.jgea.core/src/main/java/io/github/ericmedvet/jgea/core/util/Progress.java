
package io.github.ericmedvet.jgea.core.util;

import java.io.Serializable;

public record Progress(
    Number start,
    Number end,
    Number current
) implements Serializable {
  public static Progress NA = new Progress(0, 0, 0);

  public double rate() {
    return Math.min(
        1d,
        Math.max(0d, current.doubleValue() - start.doubleValue()) / (end.doubleValue() - start.doubleValue())
    );
  }

  public Progress(double normalizedRate) {
    this(0, 1, normalizedRate);
  }
}
