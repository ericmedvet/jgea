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

import io.github.ericmedvet.jgea.core.representation.grammar.GrammarOptionString;

public class GrammarOptionStringDistance<S> implements Distance<GrammarOptionString<S>> {
  @Override
  public Double apply(GrammarOptionString<S> gos1, GrammarOptionString<S> gos2) {
    for (S s : gos1.options().keySet()) {
      if (gos1.options().get(s).size() != gos2.options().get(s).size()) {
        throw new IllegalArgumentException(String.format(
            "Sequences size should be the same for symbol %d (%d vs. %d)",
            s, gos1.options().get(s).size(), gos2.options().get(s).size()));
      }
    }
    int sum = 0;
    for (S s : gos1.options().keySet()) {
      for (int i = 0; i < gos1.options().get(s).size(); i = i + 1) {
        sum = sum
            + Math.abs(gos1.options().get(s).get(i)
                - gos2.options().get(s).get(i));
      }
    }
    return (double) sum;
  }
}
