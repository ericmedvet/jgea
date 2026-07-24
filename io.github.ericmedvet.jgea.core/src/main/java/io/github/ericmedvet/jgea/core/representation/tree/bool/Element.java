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

import io.github.ericmedvet.jnb.datastructure.Tree.StringParser;
import java.io.Serializable;
import java.util.List;
import java.util.function.Predicate;

public interface Element {
  String CONSTANT_REGEX = "[TF]";
  String VAR_REGEX = "[0-9]+";

  static Element fromString(String string) {
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
    if (string.matches(VAR_REGEX)) {
      return new Element.Variable(Integer.parseInt(string));
    }
    return new Element.Decoration(string);
  }

  static StringParser<Element, Operator, Element> stringParser(boolean allowVoid) {
    return new StringParser<>(
        StringParser.NodeParser.fromEnum(Element.Operator.class, allowVoid),
        List.of(
            StringParser.NodeParser.fromRegex(
                CONSTANT_REGEX,
                s -> new Constant(switch (s) {
                  case "T" -> true;
                  case "F" -> false;
                  default ->
                    throw new IllegalArgumentException(
                        "Unexpected constant value %s not matching %s".formatted(
                            s,
                            CONSTANT_REGEX
                        )
                    );
                }),
                allowVoid
            ),
            StringParser.NodeParser.fromRegex(
                VAR_REGEX,
                s -> new Variable(Integer.parseInt(s)),
                allowVoid
            )
        ),
        (op, n) -> op.unlimitedArity ? (n >= op.arity) : (n == op.arity),
        StringParser.Configuration.DEFAULT
    );
  }

  String toString();

  enum Operator implements Element, Serializable, Predicate<boolean[]> {
    AND(
        "and",
        input -> {
          if (input.length == 2) {
            return input[0] && input[1];
          }
          for (boolean b : input) {
            if (!b) {
              return false;
            }
          }
          return true;
        },
        2,
        true
    ), AND1NOT("and1not", input -> !input[0] && input[1], 2, false), OR(
        "or",
        input -> {
          if (input.length == 2) {
            return input[0] || input[1];
          }
          for (boolean b : input) {
            if (b) {
              return true;
            }
          }
          return false;
        },
        2,
        true
    ), XOR("xor", input -> input[0] != input[1], 2, false), NOT("not", input -> !input[0], 1, false), IF(
        "if",
        input -> input[0] ? input[1] : input[2],
        3,
        false
    );

    private final String string;
    private final Predicate<boolean[]> predicate;
    private final int arity;
    private final boolean unlimitedArity;

    Operator(String string, Predicate<boolean[]> predicate, int arity, boolean unlimitedArity) {
      this.string = string;
      this.predicate = predicate;
      this.arity = arity;
      this.unlimitedArity = unlimitedArity;
    }


    public int arity() {
      return arity;
    }

    public boolean isUnlimitedArity() {
      return unlimitedArity;
    }

    @Override
    public boolean test(boolean[] input) {
      if (input.length != arity && !unlimitedArity) {
        throw new IllegalArgumentException(
            "Wrong number of inputs: %d expected, %d found".formatted(arity, input.length)
        );
      }
      if (input.length < arity) {
        throw new IllegalArgumentException(
            "Wrong number of inputs: >=%d expected, %d found".formatted(arity, input.length)
        );
      }
      return predicate.test(input);
    }

    @Override
    public String toString() {
      return string;
    }

  }

  record Constant(boolean value) implements Element, Serializable {

    @Override
    public String toString() {
      return value ? "T" : "F";
    }
  }

  record Decoration(String string) implements Element, Serializable {}

  record Variable(int index) implements Element, Serializable {
    @Override
    public String toString() {
      return Integer.toString(index);
    }
  }
}