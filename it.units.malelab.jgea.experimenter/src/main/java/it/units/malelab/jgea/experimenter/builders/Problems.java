/*
 * Copyright 2022 eric
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

package it.units.malelab.jgea.experimenter.builders;

import it.units.malelab.jgea.core.TotalOrderQualityBasedProblem;
import it.units.malelab.jnb.core.Param;

import java.util.Comparator;
import java.util.function.Function;

/**
 * @author "Eric Medvet" on 2022/11/21 for 2d-robot-evolution
 */
public class Problems {

  public enum OptimizationType {MINIMIZE, MAXIMIZE}

  private Problems() {
  }

  @SuppressWarnings("unused")
  public static <S, Q, C extends Comparable<C>> TotalOrderQualityBasedProblem<S, Q> totalOrder(
      @Param("qFunction") Function<S, Q> qualityFunction,
      @Param(value = "cFunction", dNPM = "ea.f.identity()") Function<Q, C> comparableFunction,
      @Param(value = "type", dS = "minimize") OptimizationType type
  ) {
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
}
