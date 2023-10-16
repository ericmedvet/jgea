
package io.github.ericmedvet.jgea.core.distance;

import java.util.ArrayList;
import java.util.List;
public class Pairwise<T> implements Distance<List<T>> {

  private final Distance<T> innerDistance;

  public Pairwise(Distance<T> innerDistance) {
    this.innerDistance = innerDistance;
  }

  @Override
  public Double apply(List<T> l1, List<T> l2) {
    List<Double> distances = new ArrayList<>();
    for (int i = 0; i < Math.min(l1.size(), l2.size()); i++) {
      distances.add(innerDistance.apply(l1.get(i), l2.get(i)));
    }
    return distances.stream().mapToDouble(Double::doubleValue).average().orElse(Double.NaN);
  }

}
