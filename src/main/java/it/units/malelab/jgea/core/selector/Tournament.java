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

package it.units.malelab.jgea.core.selector;

import it.units.malelab.jgea.core.order.DAGPartiallyOrderedCollection;
import it.units.malelab.jgea.core.order.PartiallyOrderedCollection;
import it.units.malelab.jgea.core.util.Misc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

/**
 * @author eric
 */
public class Tournament implements Selector<Object> {

  private final int size;

  public Tournament(int size) {
    this.size = size;
  }

  @Override
  public <K extends Object> K select(PartiallyOrderedCollection<K> ks, Random random) {
    Collection<K> all = ks.all();
    Collection<K> tournament = new ArrayList<>();
    for (int i = 0; i < size; i++) {
      tournament.add(Misc.pickRandomly(all, random));
    }
    if (ks instanceof DAGPartiallyOrderedCollection) {
      PartiallyOrderedCollection<K> poTournament = new DAGPartiallyOrderedCollection<>(tournament, ((DAGPartiallyOrderedCollection<K>) ks).getPartialComparator());
      tournament.clear();
      tournament.addAll(poTournament.firsts());
    }
    return Misc.pickRandomly(tournament, random);
  }

  @Override
  public String toString() {
    return "Tournament{" + "size=" + size + '}';
  }

}
