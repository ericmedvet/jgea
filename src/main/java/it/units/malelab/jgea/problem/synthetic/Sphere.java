/*
 * Copyright 2020 Eric Medvet <eric.medvet@gmail.com> (as eric)
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

package it.units.malelab.jgea.problem.synthetic;

import it.units.malelab.jgea.core.Problem;

import java.util.List;
import java.util.function.Function;

public class Sphere implements Problem<List<Double>, Double> {

  private static class FitnessFunction implements Function<List<Double>, Double> {

    @Override
    public Double apply(List<Double> s) {
      double sum = 0.0;
      for (int i = 0; i < s.size(); i++) {
        sum += s.get(i) * s.get(i);
      }
      return sum;
    }
  }

  private final FitnessFunction fitnessFunction = new FitnessFunction();

  @Override
  public Function<List<Double>, Double> getFitnessFunction() {
    return fitnessFunction;
  }
}
