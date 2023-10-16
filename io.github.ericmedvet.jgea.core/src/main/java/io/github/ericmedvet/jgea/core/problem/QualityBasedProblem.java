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
package io.github.ericmedvet.jgea.core.problem;

import io.github.ericmedvet.jgea.core.order.PartialComparator;
import java.util.function.Function;

public interface QualityBasedProblem<S, Q> extends Problem<S> {

  PartialComparator<Q> qualityComparator();

  Function<S, Q> qualityFunction();

  static <S, Q> QualityBasedProblem<S, Q> create(
      Function<S, Q> qualityFunction, PartialComparator<Q> qualityComparator) {
    return new QualityBasedProblem<>() {
      @Override
      public PartialComparator<Q> qualityComparator() {
        return qualityComparator;
      }

      @Override
      public Function<S, Q> qualityFunction() {
        return qualityFunction;
      }
    };
  }

  @Override
  default PartialComparatorOutcome compare(S s1, S s2) {
    return qualityComparator().compare(qualityFunction().apply(s1), qualityFunction().apply(s2));
  }

  default QualityBasedProblem<S, Q> withComparator(PartialComparator<Q> comparator) {
    QualityBasedProblem<S, Q> inner = this;
    return new QualityBasedProblem<>() {
      @Override
      public PartialComparator<Q> qualityComparator() {
        return comparator;
      }

      @Override
      public Function<S, Q> qualityFunction() {
        return inner.qualityFunction();
      }
    };
  }
}
