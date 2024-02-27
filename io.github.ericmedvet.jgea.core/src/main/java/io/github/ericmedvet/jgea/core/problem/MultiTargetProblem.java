/*-
 * ========================LICENSE_START=================================
 * jgea-core
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

package io.github.ericmedvet.jgea.core.problem;

import io.github.ericmedvet.jgea.core.distance.Distance;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

public interface MultiTargetProblem<S> extends TotalOrderQualityBasedProblem<S, Double> {
  Distance<S> distance();

  Collection<S> targets();

  @Override
  default Function<S, Double> qualityFunction() {
    return s -> targets().stream()
        .mapToDouble(t -> distance().apply(s, t))
        .min()
        .orElseThrow();
  }

  @Override
  default Comparator<Double> totalOrderComparator() {
    return Double::compareTo;
  }

  default MultiHomogeneousObjectiveProblem<S, Double> toMHOProblem() {
    List<Comparator<Double>> comparators = Collections.nCopies(targets().size(), Double::compareTo);
    Function<S, List<Double>> f =
        s -> targets().stream().map(t -> distance().apply(s, t)).toList();
    record MHOProblem<S>(List<Comparator<Double>> comparators, Function<S, List<Double>> qualityFunction)
        implements MultiHomogeneousObjectiveProblem<S, Double> {}
    record MHOProblemWithExample<S>(
        List<Comparator<Double>> comparators, Function<S, List<Double>> qualityFunction, S example)
        implements MultiHomogeneousObjectiveProblem<S, Double>, ProblemWithExampleSolution<S> {}
    if (this instanceof ProblemWithExampleSolution<?> pwes) {
      //noinspection unchecked
      return new MHOProblemWithExample<>(comparators, f, (S) pwes.example());
    }
    return new MHOProblem<>(comparators, f);
  }
}
