package io.github.ericmedvet.jgea.core.representation.grid;

import io.github.ericmedvet.jsdynsym.grid.Grid;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Function;
import java.util.random.RandomGenerator;

/**
 * @author "Eric Medvet" on 2023/06/16 for jgea
 */
public class Main {
  public static void main(String[] args) throws IOException {
    RandomGenerator rg = new Random();
    GridGrammar<String> gg = GridGrammar.load(GridGrammar.class.getResourceAsStream("/grammars/2d/worm.bnf"));
    GridDeveloper<String> gd = new StandardGridDeveloper<>(
        gg,
        false,
        List.of(StandardGridDeveloper.SortingCriterion.LEAST_RECENT)
    );
    Grid<String> g = gd.develop(t -> Optional.of(gg.getRules().get(t).get(rg.nextInt(gg.getRules().get(t).size()))))
        .orElseThrow();
    System.out.println(Grid.toString(g, (Function<String, Character>) s -> s.charAt(0)));
  }
}
