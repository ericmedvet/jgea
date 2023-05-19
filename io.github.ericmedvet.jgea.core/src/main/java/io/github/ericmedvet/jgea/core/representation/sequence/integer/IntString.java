/*
 * Copyright 2023 eric
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

package io.github.ericmedvet.jgea.core.representation.sequence.integer;

import io.github.ericmedvet.jgea.core.representation.sequence.ThinList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class IntString implements ThinList<Integer> {

  private final int lowerBound;
  private final int upperBound;

  private final List<Integer> list;

  public IntString(int lowerBound, int upperBound) {
    this.lowerBound = lowerBound;
    this.upperBound = upperBound;
    list = new ArrayList<>();
  }

  public IntString(int lowerBound, int upperBound, int size) {
    this(lowerBound, upperBound);
    list.addAll(Collections.nCopies(size, lowerBound));
  }

  public int getLowerBound() {
    return lowerBound;
  }

  public int getUpperBound() {
    return upperBound;
  }

  @Override
  public int hashCode() {
    return Objects.hash(lowerBound, upperBound, list);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    IntString integers = (IntString) o;
    return lowerBound == integers.lowerBound && upperBound == integers.upperBound && Objects.equals(
        list,
        integers.list
    );
  }

  @Override
  public String toString() {
    return list.toString();
  }

  @Override
  public int size() {
    return list.size();
  }

  @Override
  public boolean add(Integer n) {
    if (n < lowerBound || n >= upperBound) {
      throw new IllegalArgumentException("Value %d to add not withing valid bounds [%d;%d[".formatted(
          n,
          lowerBound,
          upperBound
      ));
    }
    return list.add(n);
  }

  @Override
  public Integer get(int index) {
    return list.get(index);
  }

  @Override
  public Integer set(int index, Integer n) {
    if (n < lowerBound || n >= upperBound) {
      throw new IllegalArgumentException("Value %d to add not withing valid bounds [%d;%d[".formatted(
          n,
          lowerBound,
          upperBound
      ));
    }
    return list.set(index, n);
  }

  @Override
  public Integer remove(int index) {
    return list.remove(index);
  }
}
