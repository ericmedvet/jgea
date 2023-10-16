
package io.github.ericmedvet.jgea.core.representation.sequence;

import io.github.ericmedvet.jgea.core.IndependentFactory;
import io.github.ericmedvet.jgea.core.operator.Crossover;

import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGenerator;
public class SameTwoPointsCrossover<E, L extends List<E>> implements Crossover<L> {

  private final IndependentFactory<L> factory;

  public SameTwoPointsCrossover(IndependentFactory<L> factory) {
    this.factory = factory;
  }

  @Override
  public L recombine(L parent1, L parent2, RandomGenerator random) {
    int cut = random.nextInt(Math.min(parent1.size(), parent2.size()));
    List<E> list = new ArrayList<>(parent1.subList(0, cut));
    list.addAll(parent2.subList(cut, parent2.size()));
    L child = factory.build(random);
    for (int i = 0; i < list.size(); i++) {
      E e = list.get(i);
      if (child.size() > i) {
        child.set(i, e);
      } else {
        child.add(e);
      }
    }
    return child;
  }

}
