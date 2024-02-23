/*-
 * ========================LICENSE_START=================================
 * jgea-problem
 * %%
 * Copyright (C) 2018 - 2024 Eric Medvet
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
package io.github.ericmedvet.jgea.problem.control.navigation;

import java.util.Optional;

public record Line(double a, double b, double c) {

  private static Line from(Point p1, Point p2) {
    if (p1.x() == p2.x()) {
      return new Line(1d, 0d, -p1.x());
    }
    if (p1.y() == p2.y()) {
      return new Line(0d, 1d, -p1.y());
    }
    double dX = (p2.x() - p1.x());
    double dY = (p2.y() - p1.y());
    return new Line(1d / dX, -1d / dY, -p1.x() / dX + p1.y() / dY);
  }

  public static Line from(Point p, double a) {
    return from(p, p.sum(new Point(a)));
  }

  public static Line from(Segment s) {
    return from(s.p1(), s.p2());
  }

  public boolean contains(Point p) {
    return a * p.x() + b * p.y() + c == 0;
  }

  public boolean contains(Segment s) {
    return contains(s.p1()) && contains(s.p2());
  }

  public Optional<Point> interception(Line l) {
    if (l.equals(this)) {
      return Optional.empty();
    }
    if (a / l.a == b / l.b) {
      return Optional.empty();
    }
    double d = a * l.b - l.a * b;
    double x = (b * l.c - l.b * c) / d;
    double y = (c * l.a - l.c * a) / d;
    return Optional.of(new Point(x, y));
  }

  public Optional<Point> interception(Segment s) {
    Optional<Point> oP = interception(from(s));
    if (oP.isEmpty()) {
      return oP;
    }
    Point p = oP.orElseThrow();
    if (p.x() < Math.min(s.p1().x(), s.p2().x())) {
      return Optional.empty();
    }
    if (p.x() > Math.max(s.p1().x(), s.p2().x())) {
      return Optional.empty();
    }
    if (p.y() < Math.min(s.p1().y(), s.p2().y())) {
      return Optional.empty();
    }
    if (p.y() > Math.max(s.p1().y(), s.p2().y())) {
      return Optional.empty();
    }
    return oP;
  }
}
