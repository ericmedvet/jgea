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

import io.github.ericmedvet.jgea.core.operator.Mutation;
import java.util.List;
import java.util.random.RandomGenerator;

public class ListProbabilisticMutation<E> implements Mutation<List<E>> {
  private final double p;
  private final Mutation<E> mutation;

  public ListProbabilisticMutation(double p, Mutation<E> mutation) {
    this.p = p;
    this.mutation = mutation;
  }

  @Override
  public List<E> mutate(List<E> parent, RandomGenerator random) {
    return parent.stream()
        .map(e -> random.nextDouble() < p ? mutation.mutate(e, random) : e)
        .toList();
  }
}
