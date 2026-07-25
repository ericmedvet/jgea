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

import io.github.ericmedvet.jnb.datastructure.Sized;
import io.github.ericmedvet.jnb.datastructure.Tree;
import io.github.ericmedvet.jsdynsym.core.bool.BooleanFunction;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

public class TreeBasedBooleanFunction implements BooleanFunction, Sized {

  private final List<Tree<Element>> trees;
  private final int nOfInputs;

  public TreeBasedBooleanFunction(List<Tree<Element>> trees, int nOfInputs) {
    this.trees = trees;
    this.nOfInputs = nOfInputs;
    // check inputs consistency
    List<Integer> indexes = trees.stream()
        .flatMap(t -> t.leafLabels().stream())
        .filter(e -> e instanceof Element.Variable)
        .map(e -> ((Element.Variable) e).index())
        .distinct()
        .toList();
    Integer minIndex = indexes.stream().min(Integer::compareTo).orElse(0);
    Integer maxIndex = indexes.stream().max(Integer::compareTo).orElse(0);
    if (minIndex < 0 || maxIndex >= nOfInputs) {
      throw new IllegalArgumentException(
          "Illegal variable indexes: [0-%d] expected, [%d-%d] found".formatted(
              nOfInputs - 1,
              minIndex,
              maxIndex
          )
      );
    }
  }

  public TreeBasedBooleanFunction(List<Tree<Element>> trees) {
    this(
        trees,
        (int) trees.stream()
            .flatMap(t -> t.leafLabels().stream())
            .filter(e -> e instanceof Element.Variable)
            .map(e -> ((Element.Variable) e).index())
            .distinct()
            .count()
    );
  }

  private static boolean compute(Tree<Element> tree, boolean[] input) {
    return switch (tree.label()) {
      case Element.Variable v -> input[v.index()];
      case Element.Operator op -> op.test(
          unbox(
              tree.children()
                  .stream()
                  .filter(c -> !(c.label() instanceof Element.Decoration))
                  .map(c -> compute(c, input))
                  .toArray(Boolean[]::new)
          )
      );
      case Element.Constant c -> c.value();
      default -> throw new IllegalArgumentException("Unexpected node type %s".formatted(tree.label()));
    };
  }

  public static List<Tree<Element>> exampleFor(BooleanFunction booleanFunction) {
    return Collections.nCopies(
        booleanFunction.nOfOutputs(),
        new Tree<>(
            Element.Operator.AND,
            IntStream.range(0, booleanFunction.nOfInputs())
                .mapToObj(i -> new Tree<>((Element) new Element.Variable(i)))
                .toList()
        )
    );
  }

  private static boolean[] unbox(Boolean[] boxed) {
    boolean[] unboxed = new boolean[boxed.length];
    for (int i = 0; i < boxed.length; i = i + 1) {
      unboxed[i] = boxed[i];
    }
    return unboxed;
  }

  @Override
  public boolean[] compute(boolean... input) {
    return unbox(trees.stream().map(t -> compute(t, input)).toArray(Boolean[]::new));
  }

  @Override
  public int nOfInputs() {
    return nOfInputs;
  }

  @Override
  public int nOfOutputs() {
    return trees.size();
  }

  @Override
  public int size() {
    return trees.stream().mapToInt(Tree::size).sum();
  }
}