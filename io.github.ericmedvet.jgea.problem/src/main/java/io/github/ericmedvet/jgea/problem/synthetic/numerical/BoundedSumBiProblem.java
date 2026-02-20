/*-
 * ========================LICENSE_START=================================
 * jgea-problem
 * %%
 * Copyright (C) 2018 - 2026 Eric Medvet
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
package io.github.ericmedvet.jgea.problem.synthetic.numerical;

import io.github.ericmedvet.jgea.core.order.PartialComparator;
import io.github.ericmedvet.jgea.core.problem.SymmetricQualityBasedBiProblem;
import io.github.ericmedvet.jgea.core.representation.sequence.integer.IntString;
import java.util.Collections;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.UnaryOperator;

public record BoundedSumBiProblem(
    int d, int b, int k
) implements SymmetricQualityBasedBiProblem<IntString, Double, Double> {

  @Override
  public BiFunction<IntString, IntString, Double> outcomeFunction() {
    return (g0, g1) -> {
      int[] p0 = mapToPhenotype(g0);
      int[] p1 = mapToPhenotype(g1);

      int sumComparison = 0;
      for (int i = 0; i < d; i++) {
        sumComparison += Integer.compare(p0[i], p1[i]);
      }
      return ((double) sumComparison + d) / (2.0 * d);
    };
  }

  private int[] mapToPhenotype(IntString genotype) {
    int[] phenotype = new int[d];
    for (int gene : genotype.genes()) {
      if (gene >= 0 && gene < d) {
        phenotype[gene]++;
      }
    }
    return phenotype;
  }

  @Override
  public Optional<IntString> example() {
    return Optional.of(new IntString(Collections.nCopies(b, 0), -k, d));
  }

  @Override
  public UnaryOperator<Double> symmetryFunction() {
    return q -> 1.0 - q;
  }

  @Override
  public Function<Double, Double> firstQualityFunction() {
    return Function.identity();
  }

  @Override
  public PartialComparator<Double> qualityComparator() {
    return PartialComparator.from(Double.class).reversed();
  }
}
