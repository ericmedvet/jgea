
package io.github.ericmedvet.jgea.experimenter.builders;

import io.github.ericmedvet.jgea.core.representation.grammar.grid.GridGrammar;
import io.github.ericmedvet.jnb.core.Param;

import java.io.IOException;

public class Grammars {

  private Grammars() {
  }

  @SuppressWarnings("unused")
  public static GridGrammar<Character> gridBundled(
      @Param("name") String name
  ) {
    try {
      return GridGrammar
          .load(GridGrammar.class.getResourceAsStream("/grammars/2d/" + name + ".bnf"))
          .map(s -> s == null ? null : s.charAt(0));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}
