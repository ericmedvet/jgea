
package io.github.ericmedvet.jgea.core.selector;

import io.github.ericmedvet.jgea.core.order.DAGPartiallyOrderedCollection;
import io.github.ericmedvet.jgea.core.order.PartiallyOrderedCollection;
import io.github.ericmedvet.jgea.core.util.Misc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.random.RandomGenerator;
public class Tournament implements Selector<Object> {

  private final int size;

  public Tournament(int size) {
    this.size = size;
  }

  @Override
  public <K extends Object> K select(PartiallyOrderedCollection<K> ks, RandomGenerator random) {
    Collection<K> all = ks.all();
    Collection<K> tournament = new ArrayList<>();
    for (int i = 0; i < size; i++) {
      tournament.add(Misc.pickRandomly(all, random));
    }
    if (ks instanceof DAGPartiallyOrderedCollection) {
      PartiallyOrderedCollection<K> poTournament = new DAGPartiallyOrderedCollection<>(
          tournament,
          ((DAGPartiallyOrderedCollection<K>) ks).getPartialComparator()
      );
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
