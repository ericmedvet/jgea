/*-
 * ========================LICENSE_START=================================
 * jgea-problem
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

import io.github.ericmedvet.jgea.core.order.PartiallyOrderedCollection;
import io.github.ericmedvet.jgea.core.problem.MultiHomogeneousObjectiveProblem;
import io.github.ericmedvet.jgea.core.problem.ProblemWithExampleSolution;
import io.github.ericmedvet.jgea.core.representation.sequence.integer.IntString;
import io.github.ericmedvet.jgea.core.representation.sequence.integer.UniformIntStringFactory;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.random.RandomGenerator;
import java.util.stream.IntStream;

public class MultiObjectiveIntOneMax
    implements MultiHomogeneousObjectiveProblem<IntString, Double>, ProblemWithExampleSolution<IntString> {
  private final int p;
  private final int upperBound;

  public MultiObjectiveIntOneMax(int p, int upperBound) {
    this.p = p;
    this.upperBound = upperBound;
  }

  public static void main(String[] args) {
    RandomGenerator rg = new Random();
    MultiObjectiveIntOneMax prob = new MultiObjectiveIntOneMax(10, 3);
    for (int i = 0; i < 100; i++) {
      UniformIntStringFactory factory = new UniformIntStringFactory(0, 3, 10);
      PartiallyOrderedCollection<IntString> poc = PartiallyOrderedCollection.from(factory.build(100, rg), prob);
      System.out.println(poc.firsts());
    }
  }

  @Override
  public List<Comparator<Double>> comparators() {
    return Collections.nCopies(upperBound - 1, Comparator.reverseOrder());
  }

  @Override
  public IntString example() {
    return new IntString(Collections.nCopies(p, 0), 0, upperBound);
  }

  @Override
  public Function<IntString, List<Double>> qualityFunction() {
    return is -> IntStream.range(1, upperBound)
        .mapToObj(i ->
            (double) is.genes().stream().filter(gi -> gi.equals(i)).count() / (double) is.size())
        .toList();
  }
}
