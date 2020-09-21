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
package it.units.malelab.jgea.problem.symbolicregression.element;

/**
 * @author eric
 */
public class Constant implements Element {

  private final double value;

  public Constant(double value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return Double.toString(value);
  }

  public double getValue() {
    return value;
  }

  @Override
  public int hashCode() {
    int hash = 3;
    hash = 73 * hash + (int) (Double.doubleToLongBits(this.value) ^ (Double.doubleToLongBits(this.value) >>> 32));
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
    final Constant other = (Constant) obj;
    if (Double.doubleToLongBits(this.value) != Double.doubleToLongBits(other.value)) {
      return false;
    }
    return true;
  }

}
