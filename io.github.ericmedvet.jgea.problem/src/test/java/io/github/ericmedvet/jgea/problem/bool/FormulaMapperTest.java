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

  @Test
  void apply() {
    Tree<String> stBig = n(
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
    Tree<? extends Element> tBig = n(
        Operator.AND,
        n(
            Operator.OR,
            n(new Variable(1)),
            n(new Variable(2))
        ),
        n(new Variable(3))
    );
    Tree<String> stSmall = n(
        "<e>",
        n("and"),
        n("<e>", n("<v>", n("0"))),
        n("<e>", n("<v>", n("1")))
    );
    Tree<? extends Element> tSmall = n(
        Operator.AND,
        n(new Variable(0)),
        n(new Variable(1))
    );
    Tree<String> st2o = n(
        "<o>",
        stBig,
        stSmall
    );
    assertThat(m(stSmall))
        .as("single output %s should give %s", stSmall, List.of(tSmall))
        .isEqualTo(List.of(tSmall));
    assertThat(m(stBig))
        .as("single output %s should give %s", stBig, List.of(tBig))
        .isEqualTo(List.of(tBig));
    assertThat(m(st2o))
        .as("single output %s should give %s", st2o, List.of(tBig, tSmall))
        .isEqualTo(List.of(tBig, tSmall));
  }
}