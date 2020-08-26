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

package it.units.malelab.jgea.core.listener;

import it.units.malelab.jgea.core.Individual;
import it.units.malelab.jgea.core.evolver.Evolver;
import it.units.malelab.jgea.core.order.PartiallyOrderedCollection;

import java.io.Serializable;

/**
 * @author eric
 * @created 2020/06/16
 * @project jgea
 */
public class Event<G, S, F> implements Serializable {

  private final Evolver.State state;
  private final PartiallyOrderedCollection<Individual<G, S, F>> orderedPopulation;

  public Event(Evolver.State state, PartiallyOrderedCollection<Individual<G, S, F>> orderedPopulation) {
    this.state = state;
    this.orderedPopulation = orderedPopulation;
  }

  public Evolver.State getState() {
    return state;
  }

  public PartiallyOrderedCollection<Individual<G, S, F>> getOrderedPopulation() {
    return orderedPopulation;
  }
}
