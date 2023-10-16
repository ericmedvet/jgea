
package io.github.ericmedvet.jgea.core.operator;


import io.github.ericmedvet.jgea.core.util.Pair;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.random.RandomGenerator;
@FunctionalInterface
public interface Crossover<G> extends GeneticOperator<G> {

  G recombine(G g1, G g2, RandomGenerator random);

  static <G1, G2> Crossover<Pair<G1, G2>> pair(Crossover<G1> crossover1, Crossover<G2> crossover2) {
    return (p1, p2, random) -> Pair.of(
        crossover1.recombine(p1.first(), p2.first(), random),
        crossover2.recombine(p1.second(), p2.second(), random)
    );
  }

  static <K> Crossover<K> randomCopy() {
    return (g1, g2, random) -> random.nextBoolean() ? g1 : g2;
  }

  @Override
  default List<? extends G> apply(List<? extends G> gs, RandomGenerator random) {
    return Collections.singletonList(recombine(gs.get(0), gs.get(1), random));
  }

  @Override
  default int arity() {
    return 2;
  }

  default Crossover<G> withChecker(Predicate<G> checker) {
    Crossover<G> thisCrossover = this;
    return (parent1, parent2, random) -> {
      G child = thisCrossover.recombine(parent1, parent2, random);
      return checker.test(child) ? child : (random.nextBoolean() ? parent1 : parent2);
    };
  }

}
