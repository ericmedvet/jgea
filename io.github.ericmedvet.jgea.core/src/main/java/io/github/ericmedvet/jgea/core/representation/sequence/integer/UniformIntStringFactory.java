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

import io.github.ericmedvet.jgea.core.IndependentFactory;

import java.util.random.RandomGenerator;
import java.util.stream.IntStream;

public class UniformIntStringFactory implements IndependentFactory<IntString> {
  private final int lowerBound;
  private final int upperBound;
  private final int size;

  public UniformIntStringFactory(int lowerBound, int upperBound, int size) {
    this.lowerBound = lowerBound;
    this.upperBound = upperBound;
    this.size = size;
  }

  @Override
  public IntString build(RandomGenerator random) {
    IntString s = new IntString(lowerBound, upperBound);
    IntStream.range(0, size).forEach(i -> s.add(random.nextInt(lowerBound, upperBound)));
    return s;
  }
}
