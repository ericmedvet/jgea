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

package io.github.ericmedvet.jgea.core.representation.sequence;

import io.github.ericmedvet.jgea.core.operator.Crossover;
import java.util.List;
import java.util.random.RandomGenerator;
import java.util.stream.IntStream;

public class ElementWiseCrossover<E> implements Crossover<List<E>> {
  private final Crossover<E> crossover;

  public ElementWiseCrossover(Crossover<E> crossover) {
    this.crossover = crossover;
  }

  public List<E> recombine(List<E> l1, List<E> l2, RandomGenerator random) {
    return IntStream.range(0, Math.max(l1.size(), l2.size()))
        .mapToObj(
            i -> {
              if (l1.size() > i && l2.size() > i) {
                return crossover.recombine(l1.get(i), l2.get(i), random);
              }
              if (l1.size() > i) {
                return l1.get(i);
              }
              return l2.get(i);
            })
        .toList();
  }
}
