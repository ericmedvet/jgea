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
}
