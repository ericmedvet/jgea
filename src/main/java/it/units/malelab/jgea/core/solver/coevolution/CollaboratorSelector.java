package it.units.malelab.jgea.core.solver.coevolution;

import it.units.malelab.jgea.core.order.PartiallyOrderedCollection;

import java.util.Collection;
import java.util.random.RandomGenerator;

@FunctionalInterface
public interface CollaboratorSelector<K> {
  Collection<K> select(PartiallyOrderedCollection<K> ks, RandomGenerator random);

}
