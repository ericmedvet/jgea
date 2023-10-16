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

package io.github.ericmedvet.jgea.core.representation.sequence.integer;

import io.github.ericmedvet.jgea.core.operator.Mutation;
import java.util.random.RandomGenerator;

public class IntStringFlipMutation implements Mutation<IntString> {

  private final double p;

  public IntStringFlipMutation(double p) {
    this.p = p;
  }

  @Override
  public IntString mutate(IntString parent, RandomGenerator random) {
    if (parent.lowerBound() == parent.upperBound()) {
      return parent;
    }
    return new IntString(
        parent.genes().stream()
            .map(
                n -> {
                  if (random.nextDouble() < p) {
                    int newN = random.nextInt(parent.lowerBound(), parent.upperBound() - 1);
                    if (newN >= n) {
                      newN = newN + 1;
                    }
                    return newN;
                  }
                  return n;
                })
            .toList(),
        parent.lowerBound(),
        parent.upperBound());
  }
}
