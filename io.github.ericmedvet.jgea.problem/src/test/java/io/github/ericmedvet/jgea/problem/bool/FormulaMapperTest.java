/*-
 * ========================LICENSE_START=================================
 * jgea-problem
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
package io.github.ericmedvet.jgea.problem.bool;

import static org.assertj.core.api.Assertions.*;

import io.github.ericmedvet.jgea.core.representation.tree.bool.Element;
import io.github.ericmedvet.jgea.core.representation.tree.bool.Element.Operator;
import io.github.ericmedvet.jgea.core.representation.tree.bool.Element.Variable;
import io.github.ericmedvet.jnb.datastructure.Tree;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class FormulaMapperTest {

  @SafeVarargs
  private static <T> Tree<T> n(T t, Tree<T>... children) {
    return new Tree<>(t, Arrays.stream(children).toList());
  }

  private static List<Tree<Element>> m(Tree<String> stringTree) {
    return new FormulaMapper().apply(stringTree);
  }

  private static Tree<Element> sm(Tree<String> stringTree) {
    return new FormulaMapper().singleMap(stringTree);
  }

  private final static Tree<String> ST_BIG = n(
      "<e>",
      n("and"),
      n(
          "<e>",
          n("or"),
          n("<e>", n("<v>", n("1"))),
          n("<e>", n("<v>", n("2")))
      ),
      n("<e>", n("<v>", n("3")))
  );
  private final static Tree<? extends Element> T_BIG = n(
      Operator.AND,
      n(
          Operator.OR,
          n(new Variable(1)),
          n(new Variable(2))
      ),
      n(new Variable(3))
  );
  private final static Tree<String> ST_SMALL = n(
      "<e>",
      n("and"),
      n("<e>", n("<v>", n("0"))),
      n("<e>", n("<v>", n("1")))
  );
  private final static Tree<? extends Element> T_SMALL = n(
      Operator.AND,
      n(new Variable(0)),
      n(new Variable(1))
  );

  @Test
  void apply() {
    Tree<String> st2o = n(
        "<out>",
        ST_BIG,
        ST_SMALL
    );
    assertThat(m(st2o))
        .as("multiple output %s should give %s", st2o, List.of(T_BIG, T_SMALL))
        .isEqualTo(List.of(T_BIG, T_SMALL));
  }

  @Test
  void singleMap() {
    assertThat(sm(ST_SMALL))
        .as("single output %s should give %s", ST_SMALL, List.of(T_SMALL))
        .isEqualTo(T_SMALL);
    assertThat(sm(ST_BIG))
        .as("single output %s should give %s", ST_BIG, List.of(T_BIG))
        .isEqualTo(T_BIG);
  }
}