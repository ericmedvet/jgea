/*-
 * ========================LICENSE_START=================================
 * jgea-problem
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

package io.github.ericmedvet.jgea.problem.synthetic;

import io.github.ericmedvet.jgea.core.distance.Distance;
import io.github.ericmedvet.jgea.core.distance.Hamming;
import io.github.ericmedvet.jgea.core.problem.MultiTargetProblem;
import io.github.ericmedvet.jgea.core.problem.ProblemWithExampleSolution;
import io.github.ericmedvet.jgea.core.representation.sequence.integer.IntString;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

public class MultiModalIntOneMax implements MultiTargetProblem<IntString>, ProblemWithExampleSolution<IntString> {
  private final int p;
  private final int upperBound;
  private final Distance<IntString> distance;
  private final List<IntString> targets;

  public MultiModalIntOneMax(int p, int upperBound, int nOfTargets) {
    this.p = p;
    this.upperBound = upperBound;
    Distance<IntString> innerD = new Hamming<Integer>().on(IntString::genes);
    distance = (is1, is2) -> innerD.apply(is1, is2) / (double) p;
    int s = p / nOfTargets;
    targets = IntStream.range(0, nOfTargets)
        .mapToObj(i -> new IntString(
            IntStream.range(0, p)
                .map(gi -> (i * s <= gi && gi < (i + 1) * s) ? (upperBound - 1) : 0)
                .boxed()
                .toList(),
            0,
            upperBound))
        .toList();
  }

  @Override
  public Distance<IntString> distance() {
    return distance;
  }

  @Override
  public Collection<IntString> targets() {
    return targets;
  }

  @Override
  public IntString example() {
    return new IntString(Collections.nCopies(p, 0), 0, upperBound);
  }
}
