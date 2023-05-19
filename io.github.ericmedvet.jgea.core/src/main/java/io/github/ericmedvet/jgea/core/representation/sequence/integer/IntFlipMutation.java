/*
 * Copyright 2023 eric
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.ericmedvet.jgea.core.representation.sequence.integer;

import io.github.ericmedvet.jgea.core.operator.Mutation;

import java.util.random.RandomGenerator;

public class IntFlipMutation implements Mutation<IntString> {

  private final double p;

  public IntFlipMutation(double p) {
    this.p = p;
  }

  @Override
  public IntString mutate(IntString parent, RandomGenerator random) {
    IntString child = new IntString(parent.getLowerBound(), parent.getUpperBound());
    for (Integer n : parent) {
      if (random.nextDouble() < p) {
        child.add(random.nextInt(child.getLowerBound(), child.getUpperBound()));
      } else {
        child.add(n);
      }
    }
    return child;
  }
}
