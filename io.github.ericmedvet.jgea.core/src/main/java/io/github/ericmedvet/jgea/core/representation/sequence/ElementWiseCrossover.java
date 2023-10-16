
package io.github.ericmedvet.jgea.core.representation.sequence;

import io.github.ericmedvet.jgea.core.operator.Crossover;

import java.util.List;
import java.util.random.RandomGenerator;
import java.util.stream.IntStream;
public class ElementWiseCrossover<E> implements Crossover<List<E>> {
  private final Crossover<E> crossover;

  public ElementWiseCrossover(Crossover<E> crossover) {
    this.crossover = crossover;
  }

  public List<E> recombine(List<E> l1, List<E> l2, RandomGenerator random) {
    return IntStream.range(0, Math.max(l1.size(), l2.size()))
        .mapToObj(i -> {
          if (l1.size() > i && l2.size() > i) {
            return crossover.recombine(l1.get(i), l2.get(i), random);
          }
          if (l1.size() > i) {
            return l1.get(i);
          }
          return l2.get(i);
        })
        .toList();
  }

}
