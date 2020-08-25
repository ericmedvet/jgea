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

package it.units.malelab.jgea.representation.sequence.numeric;

import com.google.common.collect.Range;
import it.units.malelab.jgea.representation.sequence.ElementWiseCrossover;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Eric Medvet <eric.medvet@gmail.com>
 */
public class GeometricCrossover extends ElementWiseCrossover<Double, List<Double>> {

  private final Range<Double> range;

  public GeometricCrossover(Range<Double> range) {
    super(
        random -> new ArrayList<>(),
        (v1, v2, random) -> v1 + (v2 - v1) * (random.nextDouble() * (range.upperEndpoint() - range.lowerEndpoint()) + range.lowerEndpoint())
    );
    this.range = range;
  }

  public GeometricCrossover() {
    this(Range.openClosed(0d, 1d));
  }
}
