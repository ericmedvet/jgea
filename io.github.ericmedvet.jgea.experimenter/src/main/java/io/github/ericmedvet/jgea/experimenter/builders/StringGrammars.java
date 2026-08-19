/*-
 * ========================LICENSE_START=================================
 * jgea-experimenter
 * %%
 * Copyright (C) 2018 - 2026 Eric Medvet
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */

package io.github.ericmedvet.jgea.experimenter.builders;

import io.github.ericmedvet.jgea.core.representation.grammar.string.StringGrammar;
import io.github.ericmedvet.jgea.core.representation.grammar.string.StringGrammarBasedProblem;
import io.github.ericmedvet.jgea.core.representation.tree.numeric.Element;
import io.github.ericmedvet.jgea.core.representation.tree.numeric.Element.Variable;
import io.github.ericmedvet.jgea.problem.regression.FormulaMapper;
import io.github.ericmedvet.jnb.core.Cacheable;
import io.github.ericmedvet.jnb.core.Discoverable;
import io.github.ericmedvet.jnb.core.Param;
import io.github.ericmedvet.jnb.datastructure.Tree;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Discoverable(prefixTemplate = "ea.grammar.string")
public class StringGrammars {

  private StringGrammars() {
  }

  @Cacheable
  public static Function<Tree<String>, StringGrammar<String>> booleanRegression(
      @Param(
          value = "operators", dSs = {"and", "or", "not"}) List<io.github.ericmedvet.jgea.core.representation.tree.bool.Element.Operator> operators
  ) {
    return eST -> {
      List<Tree<io.github.ericmedvet.jgea.core.representation.tree.bool.Element>> trees = new io.github.ericmedvet.jgea.problem.bool.FormulaMapper()
          .apply(
              eST
          );
      return io.github.ericmedvet.jgea.problem.bool.FormulaMapper.grammar(
          operators,
          (int) trees.stream()
              .flatMap(
                  t -> t.leafLabels()
                      .stream()
                      .filter(
                          l -> l instanceof io.github.ericmedvet.jgea.core.representation.tree.bool.Element.Variable
                      )
                      .map(
                          l -> ((io.github.ericmedvet.jgea.core.representation.tree.bool.Element.Variable) l).index()
                      )

              )
              .distinct()
              .count(),
          trees.size()
      );
    };
  }

  @Cacheable
  public static Function<Tree<String>, StringGrammar<String>> bundled(@Param("name") String name) {
    return _ -> {
      try {
        return StringGrammar.load(
            StringGrammar.class.getResourceAsStream("/grammars/1d/" + name + ".bnf")
        );
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    };
  }

  @Cacheable
  public static Function<Tree<String>, StringGrammar<String>> fromFile(@Param("path") String path) {
    return _ -> {
      try (FileInputStream fis = new FileInputStream(path)) {
        return StringGrammar.load(fis);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    };
  }

  @Cacheable
  public static <N> Function<Tree<String>, StringGrammar<N>> fromProblem(
      @Param("problem") StringGrammarBasedProblem<N, ?> problem
  ) {
    return _ -> problem.grammar();
  }

  @Cacheable
  public static Function<Tree<String>, StringGrammar<String>> regression(
      @Param(
          value = "constants", dDs = {0.1, 1, 10}) List<Double> constants,
      @Param(
          value = "operators", dSs = {"addition", "subtraction", "multiplication", "prot_division", "prot_log"}) List<Element.Operator> operators
  ) {
    return eST -> FormulaMapper.grammar(
        operators,
        new FormulaMapper().apply(eST)
            .leafLabels()
            .stream()
            .filter(l -> l instanceof Variable)
            .distinct()
            .map(l -> ((Variable) l).name())
            .sorted()
            .toList(),
        constants
    );
  }

  @Cacheable
  public static Function<Tree<String>, StringGrammar<String>> simpleFixedArity(
      @Param("arity") int arity
  ) {
    return _ -> StringGrammar.from(
        "<n>",
        Map.of(
            "<n>",
            List.of(
                Collections.nCopies(arity, "<n>"),
                List.of("t")
            )
        )
    );
  }

}