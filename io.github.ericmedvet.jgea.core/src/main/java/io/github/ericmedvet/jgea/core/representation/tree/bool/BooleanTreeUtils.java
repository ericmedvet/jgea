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
package io.github.ericmedvet.jgea.core.representation.tree.bool;

import io.github.ericmedvet.jnb.datastructure.Tree;
import java.util.List;
import java.util.stream.Stream;

public class BooleanTreeUtils {
  private BooleanTreeUtils() {
  }

  private static boolean isFalse(Element e) {
    if (e instanceof Element.Constant(boolean value)) {
      return !value;
    }
    return false;
  }

  private static boolean isTrue(Element e) {
    if (e instanceof Element.Constant(boolean value)) {
      return value;
    }
    return false;
  }

  public static Tree<Element> simplify(Tree<Element> tree) {
    //not(not(x)) -> x
    if (tree.label().equals(Element.Operator.NOT)) {
      if (tree.child(0).label().equals(Element.Operator.NOT)) {
        return simplify(tree.child(0).child(0));
      }
    }
    //and(x) -> x; or(x) -> x
    if (tree.label().equals(Element.Operator.AND) || tree.label().equals(Element.Operator.OR)) {
      if (tree.children().size() == 1) {
        return simplify(tree.child(0));
      }
    }
    //and(x;x;...) -> and(x;...); and(T;...) -> and(...)
    if (tree.label().equals(Element.Operator.AND)) {
      List<Tree<Element>> reducedChildren = tree.children().stream()
          .distinct()
          .filter(c -> !isTrue(c.label()))
          .flatMap(c -> switch (c.label()) {
            case Element.Operator.AND -> c.children().stream();
            default -> Stream.of(c);
          })
          .toList();
      if (!reducedChildren.equals(tree.children())) {
        return simplify(new Tree<>(Element.Operator.AND, reducedChildren));
      }
    }
    //or(x;x;...) -> or(x;...); or(F;...) -> or(...)
    if (tree.label().equals(Element.Operator.OR)) {
      List<Tree<Element>> reducedChildren = tree.children().stream()
          .distinct()
          .filter(c -> !isFalse(c.label()))
          .flatMap(c -> switch (c.label()) {
            case Element.Operator.OR -> c.children().stream();
            default -> Stream.of(c);
          })
          .toList();
      if (!reducedChildren.equals(tree.children())) {
        return simplify(new Tree<>(Element.Operator.OR, reducedChildren));
      }
    }
    //and(F;...) -> F
    if (tree.label().equals(Element.Operator.AND)) {
      if (tree.children().stream().anyMatch(c -> isFalse(c.label()))) {
        return new Tree<>(new Element.Constant(false));
      }
    }
    //or(T;...) -> T
    if (tree.label().equals(Element.Operator.OR)) {
      if (tree.children().stream().anyMatch(c -> isTrue(c.label()))) {
        return new Tree<>(new Element.Constant(true));
      }
    }
    while (true) {
      Tree<Element> simplified = new Tree<>(
          tree.label(),
          tree.children().stream().map(BooleanTreeUtils::simplify).toList()
      );
      if (simplified.equals(tree)) {
        return simplified;
      }
      tree = simplify(simplified);
    }
  }
}