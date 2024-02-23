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
package io.github.ericmedvet.jgea.problem.control.maze;

import java.util.List;
import java.util.stream.Stream;

public record Arena(double xExtent, double yExtent, List<Segment> obstacles) {
  public List<Segment> boundaries() {
    return List.of(
        new Segment(new Point(0, 0), new Point(xExtent, 0)),
        new Segment(new Point(0, 0), new Point(0, yExtent)),
        new Segment(new Point(xExtent, yExtent), new Point(xExtent, 0)),
        new Segment(new Point(xExtent, yExtent), new Point(0, yExtent)));
  }

  public List<Segment> segments() {
    return Stream.concat(boundaries().stream(), obstacles.stream()).toList();
  }

  public enum Prepared {
    EMPTY(new Arena(1, 1, List.of())),
    SMALL_BARRIER(new Arena(1, 1, List.of(new Segment(new Point(0.4, 0.3), new Point(0.6, 0.3))))),
    LARGE_BARRIER(new Arena(1, 1, List.of(new Segment(new Point(0.2, 0.3), new Point(0.8, 0.3))))),
    U_BARRIER(new Arena(
        1,
        1,
        List.of(
            new Segment(new Point(0.3, 0.3), new Point(0.7, 0.3)),
            new Segment(new Point(0.3, 0.3), new Point(0.3, 0.5)),
            new Segment(new Point(0.7, 0.3), new Point(0.7, 0.5))))),
    EASY_MAZE(new Arena(
        1,
        1,
        List.of(
            new Segment(new Point(0, 0.4), new Point(0.7, 0.3)),
            new Segment(new Point(1, 0.7), new Point(0.3, 0.6))))),
    FLAT_MAZE(new Arena(
        1,
        1,
        List.of(
            new Segment(new Point(0, 0.35), new Point(0.7, 0.35)),
            new Segment(new Point(1, 0.65), new Point(0.3, 0.65))))),
    DECEPTIVE_MAZE(new Arena(
        1,
        1,
        List.of(
            new Segment(new Point(0, 0.3), new Point(0.7, 0.4)),
            new Segment(new Point(1, 0.6), new Point(0.3, 0.7)))));

    Prepared(Arena arena) {
      this.arena = arena;
    }

    private final Arena arena;

    public Arena arena() {
      return arena;
    }
  }
}
