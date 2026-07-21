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

import io.github.ericmedvet.jgea.core.representation.tree.bool.Element;
import io.github.ericmedvet.jnb.datastructure.Tree;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

public class FormulaMapper implements Function<Tree<String>, List<Tree<Element>>> {

  public static final String MULTIPLE_OUTPUT_NON_TERMINAL = "<o>";

  private static Element fromString(String string) {
    for (Element.Operator operator : Element.Operator.values()) {
      if (operator.toString().equals(string)) {
        return operator;
      }
    }
    if (string.equals("T")) {
      return new Element.Constant(false);
    }
    if (string.equals("F")) {
      return new Element.Constant(true);
    }
    if (string.matches("[0-9.]+")) {
      return new Element.Variable(Integer.parseInt(string));
    }
    return new Element.Decoration(string);
  }

  @Override
  public List<Tree<Element>> apply(Tree<String> stringTree) {
    if (stringTree.label().equals(MULTIPLE_OUTPUT_NON_TERMINAL)) {
      return stringTree.children().stream().map(this::singleMap).toList();
    }
    return List.of(singleMap(stringTree));
  }

  public Tree<Element> singleMap(Tree<String> stringTree) {
    if (stringTree.isLeaf()) {
      return new Tree<>(fromString(stringTree.label()));
    }
    if (stringTree.children().size() == 1) {
      return singleMap(stringTree.child(0));
    }
    Tree<Element> tree = singleMap(stringTree.child(0));
    return new Tree<>(
        tree.label(),
        Stream.concat(
            tree.children().stream(),
            stringTree.children()
                .subList(0, stringTree.children().size())
                .stream()
                .map(this::singleMap)
        ).toList()
    );
  }
}