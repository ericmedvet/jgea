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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author eric
 */
public class Triplet<F, S, T> extends Pair<F, S> {

  private final T third;

  protected Triplet(F first, S second, T third) {
    super(first, second);
    this.third = third;
  }

  public T getThird() {
    return third;
  }

  public static <F, S, T> Triplet<F, S, T> of(F first, S second, T third) {
    return new Triplet<>(first, second, third);
  }

  public static <F, S, T> List<T> thirds(List<Triplet<F, S, T>> triplets) {
    List<T> thirds = new ArrayList<>(triplets.size());
    for (Triplet<F, S, T> third : triplets) {
      thirds.add(third.getThird());
    }
    return thirds;
  }

  @Override
  public int hashCode() {
    int hash = super.hashCode();
    hash = 67 * hash + Objects.hashCode(this.third);
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final Triplet<?, ?, ?> other = (Triplet<?, ?, ?>) obj;
    if (!Objects.equals(this.third, other.third)) {
      return false;
    }
    return super.equals(obj);
  }

  @Override
  public String toString() {
    return "<" + first() + ", " + second() + ", " + third + '>';
  }

}
