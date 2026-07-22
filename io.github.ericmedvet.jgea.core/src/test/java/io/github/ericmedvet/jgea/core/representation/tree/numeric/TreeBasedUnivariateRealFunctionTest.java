/*-
 * ========================LICENSE_START=================================
 * jgea-core
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
/*
 * Copyright 2026 eric
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.ericmedvet.jgea.core.representation.tree.numeric;

import static org.assertj.core.api.Assertions.*;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

class TreeBasedUnivariateRealFunctionTest {

  private static TreeBasedUnivariateRealFunction f(String expr) {
    return new TreeBasedUnivariateRealFunction(
        Element.stringParser(false).parse(expr),
        "y",
        false
    );
  }

  private static Map<String, Double> m(double... values) {
    return IntStream.range(0, values.length)
        .boxed()
        .collect(
            Collectors.toMap(
                "x%d"::formatted,
                i -> values[i]
            )
        );
  }

  @Test
  void computeAsDouble() {
    assertThat(f("+(x0;1)").computeAsDouble(m(2))).isEqualTo(3d);
    assertThat(f("*(+(x0;1);÷(4;x1))").computeAsDouble(m(2, 1))).isEqualTo(12d);
    assertThat(f("plog(exp(3))").computeAsDouble(m())).isEqualTo(3d);
  }
}