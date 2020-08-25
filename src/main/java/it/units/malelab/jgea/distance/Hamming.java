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

import java.util.List;

/**
 * @author eric
 */
public class Hamming<T> implements Distance<List<T>> {

  @Override
  public Double apply(List<T> t1, List<T> t2) {
    if (t1.size() != t2.size()) {
      throw new IllegalArgumentException(String.format("Sequences size should be the same (%d vs. %d)", t1.size(), t2.size()));
    }
    int count = 0;
    for (int i = 0; i < t1.size(); i++) {
      if (!t1.get(i).equals(t2.get(i))) {
        count = count + 1;
      }
    }
    return (double) count;
  }


}
