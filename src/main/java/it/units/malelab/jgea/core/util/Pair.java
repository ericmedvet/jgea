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

import java.io.Serializable;
import java.util.Objects;

/**
 * @author eric
 */
public class Pair<F, S> implements Serializable {

  private final F first;
  private final S second;

  protected Pair(F first, S second) {
    this.first = first;
    this.second = second;
  }

  public F first() {
    return first;
  }

  public S second() {
    return second;
  }

  public static <F, S> Pair<F, S> of(F first, S second) {
    return new Pair<>(first, second);
  }

  @Override
  public int hashCode() {
    int hash = 5;
    hash = 83 * hash + Objects.hashCode(this.first);
    hash = 83 * hash + Objects.hashCode(this.second);
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
    final Pair<?, ?> other = (Pair<?, ?>) obj;
    if (!Objects.equals(this.first, other.first)) {
      return false;
    }
    if (!Objects.equals(this.second, other.second)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "<" + first + ", " + second + '>';
  }

}
