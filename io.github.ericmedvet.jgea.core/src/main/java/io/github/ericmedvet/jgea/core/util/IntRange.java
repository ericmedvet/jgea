/*-
 * ========================LICENSE_START=================================
 * jgea-core
 * %%
 * Copyright (C) 2018 - 2026 Eric Medvet
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */
package io.github.ericmedvet.jgea.core.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

public record IntRange(int min, int max) implements Serializable {

  public int clip(int value) {
    return Math.clamp(value, this.min, this.max);
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

  public List<IntRange> slices(int n) {
    if (n < 1) {
      throw new IllegalArgumentException("Cannot slice in %d pieces".formatted(n));
    }
    if (n == 1) {
      return List.of(this);
    }
    int l = extent() / n;
    return IntStream.iterate(0, s -> s < max, s -> s + l)
        .mapToObj(s -> new IntRange(s, Math.min(s + l, max)))
        .toList();
  }

  public List<IntRange> slices(List<Double> sizeProportions) {
    if (sizeProportions.isEmpty()) {
      throw new IllegalArgumentException("Cannot slice in zero pieces");
    }
    if (sizeProportions.size() == 1) {
      return List.of(this);
    }
    double sum = sizeProportions.stream().mapToDouble(v -> v).sum();
    List<IntRange> ranges = new ArrayList<>(sizeProportions.size());
    int s = 0;
    for (Double sizeProportion : sizeProportions) {
      int e = Math.min(s + (int) Math.round(extent() * sizeProportion / sum), max);
      ranges.add(new IntRange(s, e));
      s = e;
    }
    return ranges;
  }
}