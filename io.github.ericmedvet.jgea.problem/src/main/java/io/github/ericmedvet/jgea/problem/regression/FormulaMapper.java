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

import io.github.ericmedvet.jgea.core.representation.grammar.string.StringGrammar;
import io.github.ericmedvet.jgea.core.representation.tree.numeric.Element;
import io.github.ericmedvet.jgea.core.representation.tree.numeric.Element.Operator;
import io.github.ericmedvet.jnb.datastructure.Tree;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.SequencedMap;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FormulaMapper implements Function<Tree<String>, Tree<Element>> {

  public static final String EXPR_NON_TERMINAL = "<e>";
  public static final String VAR_NON_TERMINAL = "<v>";
  public static final String CONST_NON_TERMINAL = "<c>";
  public static final String OP_NON_TERMINAL = "<o>";
  public static final String OP_NON_TERMINAL_FORMAT = "<o%d>";

  public static Tree<String> allVarsTree(Set<String> variableNames) {
    return new Tree<>(
        EXPR_NON_TERMINAL,
        Stream.concat(
            Stream.of(
                new Tree<>(
                    OP_NON_TERMINAL_FORMAT.formatted(variableNames.size()),
                    List.of(new Tree<>(Operator.ADDITION.toString()))
                )
            ),
            variableNames
                .stream()
                .sorted()
                .map(v -> new Tree<>(VAR_NON_TERMINAL, List.of(new Tree<>(v))))

        ).toList()
    );
  }

  public static StringGrammar<String> grammar(
      List<Operator> operators,
      List<String> variables,
      List<Double> constants
  ) {
    SequencedMap<String, List<List<String>>> rules = new LinkedHashMap<>();
    SortedSet<Integer> arities = operators.stream()
        .map(Element.Operator::arity)
        .collect(Collectors.toCollection(TreeSet::new));
    rules.put(
        EXPR_NON_TERMINAL,
        List.of(
            List.of(OP_NON_TERMINAL),
            List.of(VAR_NON_TERMINAL),
            List.of(CONST_NON_TERMINAL)
        )
    );
    rules.put(
        OP_NON_TERMINAL,
        arities.stream()
            .map(
                a -> Stream.concat(
                    Stream.of(OP_NON_TERMINAL_FORMAT.formatted(a)),
                    Collections.nCopies(a, EXPR_NON_TERMINAL).stream()
                ).toList()
            )
            .toList()
    );
    arities.forEach(
        a -> rules.put(
            OP_NON_TERMINAL_FORMAT.formatted(a),
            operators.stream().filter(o -> o.arity() == a).map(o -> List.of(o.toString())).toList()
        )
    );
    rules.put(VAR_NON_TERMINAL, variables.stream().map(List::of).toList());
    rules.put(
        CONST_NON_TERMINAL,
        constants.stream().map(c -> List.of(Double.toString(c))).toList()
    );
    return StringGrammar.from(rules);
  }

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