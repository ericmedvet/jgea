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

package io.github.ericmedvet.jgea.problem.synthetic.numerical;

import io.github.ericmedvet.jgea.core.problem.MultiHomogeneousObjectiveProblem;
import io.github.ericmedvet.jgea.core.problem.ProblemWithExampleSolution;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

public class Cones
    implements MultiHomogeneousObjectiveProblem<List<Double>, Double>, ProblemWithExampleSolution<List<Double>> {

  @Override
  public List<Comparator<Double>> comparators() {
    Comparator<Double> comparator = Double::compareTo;
    return List.of(comparator, comparator, comparator.reversed());
  }

  @Override
  public List<Double> example() {
    return List.of(0d, 0d);
  }

  @Override
  public Function<List<Double>, List<Double>> qualityFunction() {
    return list -> {
      double r = list.get(0);
      double h = list.get(1);
      double s = Math.sqrt(r * r + h * h);
      double lateralSurface = Math.PI * r * s;
      double totalSurface = Math.PI * r * (r + s);
      double volume = Math.PI * r * r * h / 3;
      return List.of(lateralSurface, totalSurface, volume);
    };
  }
}
