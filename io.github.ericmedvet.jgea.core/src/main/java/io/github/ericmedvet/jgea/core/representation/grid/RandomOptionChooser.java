package io.github.ericmedvet.jgea.core.representation.grid;

import java.util.List;
import java.util.Optional;
import java.util.random.RandomGenerator;

/**
 * @author "Eric Medvet" on 2023/06/16 for jgea
 */
public class RandomOptionChooser<T> implements GridDeveloper.Chooser<T> {
  private final RandomGenerator randomGenerator;
  private final int size;
  private final GridGrammar<T> gridGrammar;
  private int i = 0;

  public RandomOptionChooser(RandomGenerator randomGenerator, int size, GridGrammar<T> gridGrammar) {
    this.randomGenerator = randomGenerator;
    this.size = size;
    this.gridGrammar = gridGrammar;
  }

  @Override
  public Optional<GridGrammar.ReferencedGrid<T>> choose(T t) {
    if (i >= size) {
      return Optional.empty();
    }
    i = i + 1;
    List<GridGrammar.ReferencedGrid<T>> options = gridGrammar.getRules().get(t);
    return Optional.of(options.get(randomGenerator.nextInt(options.size())));
  }
}
