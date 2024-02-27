/*-
 * ========================LICENSE_START=================================
 * jgea-core
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
package io.github.ericmedvet.jgea.core.distance;

import java.util.List;

public class LNorm implements Distance<List<Double>> {
  private final double d;

  public LNorm(double d) {
    this.d = d;
  }

  @Override
  public Double apply(List<Double> v1, List<Double> v2) {
    if (v1.size() != v2.size()) {
      throw new IllegalArgumentException(
          String.format("Args lengths do not match: %d and %d", v1.size(), v2.size()));
    }
    double s = 0d;
    for (int i = 0; i < v1.size(); i++) {
      s = s + Math.abs(Math.pow(v1.get(i) - v2.get(i), d));
    }
    return Math.pow(s, 1d / d);
  }
}
