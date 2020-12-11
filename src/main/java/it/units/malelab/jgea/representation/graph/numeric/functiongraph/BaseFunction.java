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

package it.units.malelab.jgea.representation.graph.numeric.functiongraph;

import java.util.function.Function;

/**
 * @author eric
 */
public enum BaseFunction implements Function<Double, Double> {
  IDENTITY(x -> x),
  SQ(x -> x * x),
  EXP(x -> Math.exp(x)),
  SIN(Math::sin),
  RE_LU(x -> (x < 0) ? 0d : x),
  ABS(Math::abs),
  STEP(x -> (x > 0) ? 1d : 0d),
  SAW(x -> x - Math.floor(x)),
  GAUSSIAN(x -> Math.exp(-0.5d * x * x) / Math.sqrt(2d * Math.PI)),
  PROT_INVERSE(x -> (x != 0d) ? (1d / x) : 0d),
  TANH(Math::tanh);

  private final Function<Double, Double> function;

  BaseFunction(Function<Double, Double> function) {
    this.function = function;
  }

  @Override
  public Double apply(Double x) {
    return function.apply(x);
  }

}
