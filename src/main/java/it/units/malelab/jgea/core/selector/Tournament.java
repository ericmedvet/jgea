/*
 * Copyright (C) 2020 Eric Medvet <eric.medvet@gmail.com> (as eric)
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
