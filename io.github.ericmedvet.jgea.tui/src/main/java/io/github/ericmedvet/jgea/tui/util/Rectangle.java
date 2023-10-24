/*-
 * ========================LICENSE_START=================================
 * jgea-tui
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
package io.github.ericmedvet.jgea.tui.util;

import java.util.ArrayList;
import java.util.List;

public record Rectangle(Point min, Point max) {
  public int h() {
    return max().y() - min().y();
  }

  public Rectangle inner(int delta) {
    return new Rectangle(min().delta(delta, delta), max().delta(-delta, -delta));
  }

  public Point ne() {
    return min();
  }

  public Point nw() {
    return new Point(max().x() - 1, min().y());
  }

  public Point se() {
    return new Point(min().x(), max().y() - 1);
  }

  public List<Rectangle> splitHorizontally(float... rs) {
    List<Rectangle> rectangles = new ArrayList<>();
    for (int i = 0; i < rs.length; i++) {
      rectangles.add(new Rectangle(
          new Point(min().x() + (i == 0 ? 0 : Math.round(rs[i - 1] * w())), min().y()),
          new Point(min().x() + Math.round(rs[i] * w()), max().y())));
    }
    rectangles.add(new Rectangle(
        new Point(min().x() + Math.round(rs[rs.length - 1] * w()), min().y()), new Point(max.x(), max().y())));
    return rectangles;
  }

  public List<Rectangle> splitVertically(float... rs) {
    List<Rectangle> rectangles = new ArrayList<>();
    for (int i = 0; i < rs.length; i++) {
      rectangles.add(new Rectangle(
          new Point(min.x(), min().y() + (i == 0 ? 0 : Math.round(rs[i - 1] * h()))),
          new Point(max.x(), min().y() + Math.round(rs[i] * h()))));
    }
    rectangles.add(new Rectangle(
        new Point(min.x(), min().y() + Math.round(rs[rs.length - 1] * h())), new Point(max.x(), max.y())));
    return rectangles;
  }

  public Point sw() {
    return max().delta(-1, -1);
  }

  public int w() {
    return max().x() - min().x();
  }
}
