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

import java.util.Optional;

public record Semiline(Point p, double a) {
  public Optional<Point> interception(Segment s) {
    Line l = Line.from(p, a);
    Optional<Point> oIP = l.interception(s);
    if (oIP.isEmpty()) {
      return oIP;
    }
    Point iP = oIP.orElseThrow();
    if (Math.abs(iP.diff(p).direction() - a) > Math.PI / 2d) {
      return Optional.empty();
    }
    return oIP;
  }
}
