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

import io.github.ericmedvet.jgea.core.representation.sequence.bit.BitString;

public class BitStringHamming implements Distance<BitString> {

  @Override
  public Double apply(BitString b1, BitString b2) {
    if (b1.size() != b2.size()) {
      throw new IllegalArgumentException(
          String.format("Sequences size should be the same (%d vs. %d)", b1.size(), b2.size()));
    }
    int s = 0;
    for (int i = 0; i < b1.size(); i++) {
      s = s + (b1.bits()[i] != b2.bits()[i] ? 1 : 0);
    }
    return (double) s;
  }
}
