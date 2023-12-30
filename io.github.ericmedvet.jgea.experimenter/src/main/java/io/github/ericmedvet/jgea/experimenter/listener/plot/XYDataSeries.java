/*-
 * ========================LICENSE_START=================================
 * jgea-experimenter
 * %%
 * Copyright (C) 2018 - 2023 Eric Medvet
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
package io.github.ericmedvet.jgea.experimenter.listener.plot;

import io.github.ericmedvet.jsdynsym.core.DoubleRange;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author "Eric Medvet" on 2023/12/01 for jgea
 */
public interface XYDataSeries {
  record Point(Value x, Value y) {
    @Override
    public String toString() {
      return "(%s;%s)".formatted(this.x, this.y);
    }
  }

  String name();

  List<Point> points();

  static XYDataSeries of(String name, List<Point> points) {
    record HardXYDataSeries(String name, List<Point> points) implements XYDataSeries {}
    return new HardXYDataSeries(name, points);
  }

  default XYDataSeries sorted() {
    return XYDataSeries.of(
        name(),
        points().stream()
            .sorted(Comparator.comparingDouble(p -> p.x().v()))
            .toList());
  }

  default XYDataSeries firstDifference() {
    List<Point> points = new ArrayList<>();
    for (int i = 1; i < points().size(); i = i + 1) {
      points.add(new Point(
          Value.of(points().get(i).x().v()),
          Value.of(points().get(i).y().v() - points().get(i - 1).y().v())));
    }
    return XYDataSeries.of(name(), Collections.unmodifiableList(points));
  }

  default DoubleRange xRange() {
    return points().stream()
        .map(p -> {
          if (p.x instanceof RangedValue rv) {
            return rv.range();
          }
          return new DoubleRange(p.x.v(), p.x.v());
        })
        .reduce(DoubleRange::largest)
        .orElseThrow();
  }

  default DoubleRange yRange() {
    return points().stream()
        .map(p -> {
          if (p.y instanceof RangedValue rv) {
            return rv.range();
          }
          return new DoubleRange(p.y.v(), p.y.v());
        })
        .reduce(DoubleRange::largest)
        .orElseThrow();
  }
}
