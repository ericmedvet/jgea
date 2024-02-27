/*-
 * ========================LICENSE_START=================================
 * jgea-experimenter
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

package io.github.ericmedvet.jgea.experimenter.builders;

import io.github.ericmedvet.jgea.core.listener.NamedFunction;
import io.github.ericmedvet.jgea.core.problem.MultiHomogeneousObjectiveProblem;
import io.github.ericmedvet.jgea.core.problem.MultiTargetProblem;
import io.github.ericmedvet.jgea.core.problem.TotalOrderQualityBasedProblem;
import io.github.ericmedvet.jnb.core.Discoverable;
import io.github.ericmedvet.jnb.core.Param;
import java.util.Comparator;
import java.util.function.Function;

@Discoverable(prefixTemplate = "ea.problem|p")
public class Problems {

  private Problems() {}

  public enum OptimizationType {
    @SuppressWarnings("unused")
    MINIMIZE,
    MAXIMIZE
  }

  @SuppressWarnings("unused")
  public static <S, Q, C extends Comparable<C>> TotalOrderQualityBasedProblem<S, Q> totalOrder(
      @Param(value = "name", dS = "to") String name,
      @Param("qFunction") NamedFunction<S, Q> qualityFunction,
      @Param(value = "cFunction", dNPM = "ea.f.identity()") NamedFunction<Q, C> comparableFunction,
      @Param(value = "type", dS = "minimize") OptimizationType type) {
    return new TotalOrderQualityBasedProblem<>() {
      @Override
      public Function<S, Q> qualityFunction() {
        return qualityFunction;
      }

      @Override
      public Comparator<Q> totalOrderComparator() {
        if (type.equals(OptimizationType.MAXIMIZE)) {
          return Comparator.comparing(comparableFunction).reversed();
        }
        return Comparator.comparing(comparableFunction);
      }
    };
  }

  @SuppressWarnings("unused")
  public static <S> MultiHomogeneousObjectiveProblem<S, Double> mhoProblem(
      @Param(value = "name", iS = "mt2mo({mtProblem.name})") String name,
      @Param("mtProblem") MultiTargetProblem<S> mtProblem) {
    return mtProblem.toMHOProblem();
  }
}
