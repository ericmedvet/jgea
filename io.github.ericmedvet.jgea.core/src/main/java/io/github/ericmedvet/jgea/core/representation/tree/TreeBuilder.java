
package io.github.ericmedvet.jgea.core.representation.tree;

import java.util.random.RandomGenerator;
public interface TreeBuilder<N> {

  Tree<N> build(RandomGenerator random, int height);

}
