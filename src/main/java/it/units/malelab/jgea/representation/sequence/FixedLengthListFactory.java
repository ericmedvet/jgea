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

package it.units.malelab.jgea.representation.sequence;

import it.units.malelab.jgea.core.IndependentFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author eric
 */
public class FixedLengthListFactory<T> implements IndependentFactory<List<T>> {
  private final int length;
  private final IndependentFactory<T> factory;

  public FixedLengthListFactory(int length, IndependentFactory<T> factory) {
    this.length = length;
    this.factory = factory;
  }

  @Override
  public List<T> build(Random random) {
    return new ArrayList<>(factory.build(length, random));
  }
}
