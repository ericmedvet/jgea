/*-
 * ========================LICENSE_START=================================
 * jgea-experimenter
 * %%
 * Copyright (C) 2018 - 2025 Eric Medvet
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

import io.github.ericmedvet.jgea.core.problem.Problem;
import io.github.ericmedvet.jgea.core.representation.grammar.grid.GridGrammar;
import io.github.ericmedvet.jgea.core.representation.grammar.string.GrammarBasedProblem;
import io.github.ericmedvet.jgea.core.representation.grammar.string.StringGrammar;
import io.github.ericmedvet.jgea.core.representation.grammar.string.SymbolicRegressionGrammar;
import io.github.ericmedvet.jgea.core.representation.tree.numeric.Element;
import io.github.ericmedvet.jnb.core.Cacheable;
import io.github.ericmedvet.jnb.core.Discoverable;
import io.github.ericmedvet.jnb.core.Param;
import io.github.ericmedvet.jsdynsym.core.numerical.named.NamedUnivariateRealFunction;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Discoverable(prefixTemplate = "ea.grammar")
public class Grammars {

  private Grammars() {
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static StringGrammar<String> fromFile(@Param("path") String path) {
    try (FileInputStream fis = new FileInputStream(path)) {
      return StringGrammar.load(fis);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <N> StringGrammar<N> fromProblem(@Param("problem") GrammarBasedProblem<N, ?> problem) {
    return problem.grammar();
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static GridGrammar<Character> gridBundled(@Param("name") String name) {
    try {
      return GridGrammar.load(GridGrammar.class.getResourceAsStream("/grammars/2d/" + name + ".bnf"))
          .map(s -> s == null ? null : s.charAt(0));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static GridGrammar<String> gridFile(@Param("path") String path) {
    try (InputStream is = new FileInputStream(path)) {
      return GridGrammar.load(is);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static StringGrammar<String> regression(
      @Param(
          value = "constants", dDs = {0.1, 1, 10}) List<Double> constants,
      @Param(
          value = "operators", dSs = {"addition", "subtraction", "multiplication", "prot_division", "prot_log"}) List<Element.Operator> operators,
      @Param("problem") Problem<NamedUnivariateRealFunction> problem
  ) {
    return new SymbolicRegressionGrammar(operators, problem.example().orElseThrow().xVarNames(), constants);
  }
}
