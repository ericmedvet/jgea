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

public interface DataSeries<V extends Value> {
  record Point<V extends Value>(Value x, V y) {
    @Override
    public String toString() {
      return "(%s;%s)".formatted(this.x, this.y);
    }
  }

  String yName();

  List<Point<V>> points();

  static <V extends Value> DataSeries<V> from(String yName, List<Point<V>> points) {
    record HardDataSeries<V extends Value>(String yName, List<Point<V>> points) implements DataSeries<V> {}
    return new HardDataSeries<>(yName, points);
  }
}
