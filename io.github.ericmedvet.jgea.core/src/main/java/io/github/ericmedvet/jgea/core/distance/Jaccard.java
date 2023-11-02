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

import io.github.ericmedvet.jgea.core.util.Misc;
import java.util.Set;

public class Jaccard<T> implements Distance<Set<T>> {
  @Override
  public Double apply(Set<T> s1, Set<T> s2) {
    if (s1.isEmpty() && s2.isEmpty()) {
      return 0d;
    }
    return 1d
        - (double) Misc.intersection(s1, s2).size()
            / (double) Misc.union(s1, s2).size();
  }
}
