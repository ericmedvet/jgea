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

package it.units.malelab.jgea.core.fitness;

import java.util.List;
import java.util.function.Function;

/**
 * @author eric
 */
public class Linearization implements Function<List<Double>, Double> {

  private final double[] coeffs;

  public Linearization(double... coeffs) {
    this.coeffs = coeffs;
  }

  @Override
  public Double apply(List<Double> values) {
    if (values.size() < coeffs.length) {
      return Double.NaN;
    }
    double sum = 0d;
    for (int i = 0; i < coeffs.length; i++) {
      sum = sum + coeffs[i] * values.get(i);
    }
    return sum;
  }

}
