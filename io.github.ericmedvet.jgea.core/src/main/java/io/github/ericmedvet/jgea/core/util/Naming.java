/*-
 * ========================LICENSE_START=================================
 * jgea-core
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
package io.github.ericmedvet.jgea.core.util;

import io.github.ericmedvet.jgea.core.problem.MultifidelityQualityBasedProblem.MultifidelityFunction;

public class Naming {

  private Naming() {
  }

  public static <T, R> MultifidelityFunction<T, R> named(
      String name,
      MultifidelityFunction<T, R> multifidelityFunction
  ) {
    return new MultifidelityFunction<>() {
      @Override
      public R apply(T t, double fidelity) {
        return multifidelityFunction.apply(t, fidelity);
      }

      @Override
      public String toString() {
        return name;
      }
    };
  }

}