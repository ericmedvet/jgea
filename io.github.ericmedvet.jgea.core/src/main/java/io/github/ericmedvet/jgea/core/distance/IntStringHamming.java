/*-
 * ========================LICENSE_START=================================
 * jgea-core
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
package io.github.ericmedvet.jgea.core.distance;

import io.github.ericmedvet.jgea.core.representation.sequence.integer.IntString;

public class IntStringHamming implements Distance<IntString> {
  @Override
  public Double apply(IntString is1, IntString is2) {
    if (is1.size() != is2.size()) {
      throw new IllegalArgumentException(
          String.format("Sequences size should be the same (%d vs. %d)", is1.size(), is2.size()));
    }
    int s = 0;
    for (int i = 0; i < is1.size(); i++) {
      s = s + Math.abs(is1.genes().get(i) - is2.genes().get(i));
    }
    return (double) s;
  }
}
