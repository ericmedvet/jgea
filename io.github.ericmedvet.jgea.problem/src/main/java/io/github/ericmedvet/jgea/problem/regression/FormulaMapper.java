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

import io.github.ericmedvet.jgea.core.representation.tree.numeric.Element;
import io.github.ericmedvet.jnb.datastructure.Tree;
import java.util.function.Function;

public class FormulaMapper implements Function<Tree<String>, Tree<Element>> {

  @Override
  public Tree<Element> apply(Tree<String> stringTree) {
    if (stringTree.isLeaf()) {
      return new Tree<>(Element.fromString(stringTree.label()));
    }
    if (stringTree.children().size() == 1) {
      return apply(stringTree.child(0));
    }
    Tree<Element> tree = apply(stringTree.child(0));
    return new Tree<>(
        tree.label(),
        stringTree.children()
            .subList(1, stringTree.children().size())
            .stream()
            .map(this)
            .toList()
    );
  }

}