
package io.github.ericmedvet.jgea.core.distance;

import io.github.ericmedvet.jgea.core.representation.tree.Tree;

import java.util.List;
public class TreeLeaves<T> implements Distance<Tree<T>> {

  private final Distance<List<T>> innerDistance;

  public TreeLeaves(Distance<List<T>> innerDistance) {
    this.innerDistance = innerDistance;
  }

  @Override
  public Double apply(Tree<T> t1, Tree<T> t2) {
    return innerDistance.apply(t1.visitLeaves(), t2.visitLeaves());
  }


}
