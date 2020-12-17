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

package it.units.malelab.jgea.core.evolver.speciation;

import java.util.Collection;
import java.util.Objects;

public class Species<T> {
  private final T representative;
  private final Collection<T> elements;

  public Species(T representative, Collection<T> elements) {
    this.representative = representative;
    this.elements = elements;
  }

  public T getRepresentative() {
    return representative;
  }

  public Collection<T> getElements() {
    return elements;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Species<?> species = (Species<?>) o;
    return Objects.equals(representative, species.representative) && Objects.equals(elements, species.elements);
  }

  @Override
  public int hashCode() {
    return Objects.hash(representative, elements);
  }

  @Override
  public String toString() {
    return "Species{" +
        "representative=" + representative +
        ", elements=" + elements +
        '}';
  }
}
