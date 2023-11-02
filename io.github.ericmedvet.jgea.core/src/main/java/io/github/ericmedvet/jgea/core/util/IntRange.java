package io.github.ericmedvet.jgea.core.util;

import java.io.Serializable;
import java.util.Optional;

/**
 * @author "Eric Medvet" on 2023/11/02 for jgea
 */
public record IntRange(int min, int max) implements Serializable {
  public int clip(int value) {
    return Math.min(Math.max(value, this.min), this.max);
  }

  public boolean contains(int d) {
    return this.min <= d && d <= this.max;
  }

  public boolean contains(IntRange other) {
    return contains(other.min) && contains(other.max);
  }

  public IntRange delta(int v) {
    return new IntRange(this.min + v, this.max + v);
  }

  public int extent() {
    return this.max - this.min;
  }

  public boolean overlaps(IntRange other) {
    if (max < other.min) {
      return false;
    }
    return !(min > other.max);
  }

  public Optional<IntRange> intersection(IntRange other) {
    if (!overlaps(other)) {
      return Optional.empty();
    }
    return Optional.of(new IntRange(Math.max(min, other.min), Math.min(max, other.max)));
  }
}
