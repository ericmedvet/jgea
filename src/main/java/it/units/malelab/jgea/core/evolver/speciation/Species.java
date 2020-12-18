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
import java.util.function.Function;

/**
 * @author eric
 */
public class Species<T> {
  private Collection<T> elements;
  private final Function<Collection<T>, T> representerSelector;

  public Species(Collection<T> elements, Function<Collection<T>, T> representerSelector) {
    this.elements = elements;
    this.representerSelector = representerSelector;
  }

  public T getRepresentative() {
    return representerSelector.apply(elements);
  }

  public Collection<T> getElements() {
    return elements;
  }

  public void addElement(T newElement) {
    elements.add(newElement);
  }

  @Override
  public String toString() {
    return "Species{" +
        "elements=" + elements +
        ", representerSelector=" + representerSelector +
        '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Species<?> species = (Species<?>) o;
    return Objects.equals(elements, species.elements) &&
        Objects.equals(representerSelector, species.representerSelector);
  }

  @Override
  public int hashCode() {
    return Objects.hash(elements, representerSelector);
  }

}
