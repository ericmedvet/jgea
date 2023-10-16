
package io.github.ericmedvet.jgea.core.selector;

import io.github.ericmedvet.jgea.core.order.PartiallyOrderedCollection;

import java.util.random.RandomGenerator;
@FunctionalInterface
public interface Selector<T> {

  <K extends T> K select(PartiallyOrderedCollection<K> ks, RandomGenerator random);

}
