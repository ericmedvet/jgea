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
package io.github.ericmedvet.jgea.problem.regression;

import static org.assertj.core.api.Assertions.*;

import io.github.ericmedvet.jgea.core.representation.tree.numeric.Element;
import io.github.ericmedvet.jgea.core.representation.tree.numeric.Element.Operator;
import io.github.ericmedvet.jgea.core.representation.tree.numeric.Element.Variable;
import io.github.ericmedvet.jnb.datastructure.Tree;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class FormulaMapperTest {

  @SafeVarargs
  private static <T> Tree<T> n(T t, Tree<T>... children) {
    return new Tree<>(t, Arrays.stream(children).toList());
  }

  private static Tree<Element> m(Tree<String> stringTree) {
    return new FormulaMapper().apply(stringTree);
  }

  @Test
  void apply() {
    assertThat(
        m(
            n(
                "<e>",
                n("<o2>", n("+")),
                n("<e>", n("<v>", n("x0"))),
                n("<e>", n("<v>", n("1")))
            )
        )
    ).as("<e>(<o2>(+);<e>(<v>(x0));<e>(<v>(1))) should give +(x0;1.0)")
        .isEqualTo(
            n(
                Operator.ADDITION,
                n(new Variable("x0")),
                n(new Element.Constant(1d))
            )
        );
    assertThat(
        m(
            n(
                "<e>",
                n(
                    "<o2>",
                    n("+")
                ),
                n(
                    "<e>",
                    n(
                        "<v>",
                        n("x0")
                    )
                ),
                n(
                    "<e>",
                    n(
                        "<o1>",
                        n("plog")
                    ),
                    n(
                        "<e>",
                        n(
                            "<v>",
                            n("x1")
                        )
                    )
                )
            )
        )
    ).as("<e>(<o2>(+);<e>(<v>(x0));<e>(<o1>(plog);<e>(<v>(x1)))) should give +(x0;plog(x1))")
        .isEqualTo(
            n(
                Operator.ADDITION,
                n(new Variable("x0")),
                n(
                    Operator.PROT_LOG,
                    n(new Variable("x1"))
                )
            )
        );
  }
}
