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

package it.units.malelab.jgea.distance;

import java.util.ArrayList;
import java.util.List;

/**
 * @author eric
 */
public class Pairwise<T> implements Distance<List<T>> {

  private final Distance<T> innerDistance;

  public Pairwise(Distance<T> innerDistance) {
    this.innerDistance = innerDistance;
  }

  @Override
  public Double apply(List<T> l1, List<T> l2) {
    List<Double> distances = new ArrayList<>();
    for (int i = 0; i < Math.min(l1.size(), l2.size()); i++) {
      distances.add(innerDistance.apply(l1.get(i), l2.get(i)));
    }
    return distances.stream().mapToDouble(Double::doubleValue).average().orElse(Double.NaN);
  }

}
