
package io.github.ericmedvet.jgea.core.operator;

import java.util.List;
import java.util.random.RandomGenerator;
public interface GeneticOperator<G> {

  List<? extends G> apply(List<? extends G> parents, RandomGenerator random);

  int arity();

  default GeneticOperator<G> andThen(GeneticOperator<G> other) {
    final GeneticOperator<G> thisOperator = this;
    return new GeneticOperator<>() {
      @Override
      public List<? extends G> apply(List<? extends G> parents, RandomGenerator random) {
        List<? extends G> intermediate = thisOperator.apply(parents, random);
        if (intermediate.size() < other.arity()) {
          throw new IllegalArgumentException(String.format(
              "Cannot apply composed operator: 2nd operator expected %d parents and found %d",
              other.arity(),
              intermediate.size()
          ));
        }
        return other.apply(intermediate, random);
      }

      @Override
      public int arity() {
        return thisOperator.arity();
      }
    };
  }

}
