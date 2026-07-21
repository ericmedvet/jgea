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
package io.github.ericmedvet.jgea.core.representation.tree.numeric;

import io.github.ericmedvet.jgea.core.representation.tree.parsing.StringParser;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;

public interface Element {
  String CONSTANT_REGEX = "-?[0-9]+(\\.[0-9]+)?";
  String VAR_REGEX = "[A-Za-z][A-Za-z0-9_]*";

  static Element fromString(String string) {
    for (Element.Operator operator : Element.Operator.values()) {
      if (operator.toString().equals(string)) {
        return operator;
      }
    }
    try {
      double value = Double.parseDouble(string);
      return new Element.Constant(value);
    } catch (NumberFormatException ex) {
      // just ignore
    }
    if (string.matches("[a-zA-Z]\\w*")) {
      return new Element.Variable(string);
    }
    return new Element.Decoration(string);
  }

  static StringParser<Element, Element.Operator, Element> stringParser(boolean allowVoid) {
    return new StringParser<>(
        StringParser.NodeParser.fromEnum(Element.Operator.class, allowVoid),
        List.of(
            StringParser.NodeParser.fromRegex(
                CONSTANT_REGEX,
                s -> new Element.Constant(Double.parseDouble(s)),
                allowVoid
            ),
            StringParser.NodeParser.fromRegex(
                VAR_REGEX,
                Variable::new,
                allowVoid
            )
        ),
        (op, n) -> op.unlimitedArity ? (n >= op.arity) : (n == op.arity),
        StringParser.Configuration.DEFAULT
    );
  }

  enum Operator implements Element, ToDoubleFunction<double[]>, Serializable {
    ADDITION(
        "+",
        x -> switch (x.length) { // optimization to avoid streams
          case 2 -> x[0] + x[1];
          case 3 -> x[0] + x[1] + x[2];
          case 4 -> x[0] + x[1] + x[2] + x[3];
          default -> Arrays.stream(x).sum();
        },
        2,
        true
    ), SUBTRACTION("-", x -> x[0] - x[1], 2, false), DIVISION(
        "÷",
        x -> x[0] / x[1],
        2,
        false
    ), PROT_DIVISION("p÷", x -> (x[1] != 0d) ? (x[0] / x[1]) : 1E9, 2, false), MULTIPLICATION(
        "*",
        x -> switch (x.length) { // optimization to avoid streams
          case 2 -> x[0] * x[1];
          case 3 -> x[0] * x[1] * x[2];
          case 4 -> x[0] * x[1] * x[2] * x[3];
          default -> Arrays.stream(x).reduce((v1, v2) -> (v1 * v2)).orElseThrow();
        },
        2,
        true
    ), LOG(
        "log",
        x -> Math.log(x[0]),
        1,
        false
    ), PROT_LOG("plog", x -> (x[0] > 0d) ? Math.log(x[0]) : -1E9, 1, false), EXP(
        "exp",
        x -> Math.exp(x[0]),
        1,
        false
    ), SIN(
        "sin",
        x -> Math.sin(x[0]),
        1,
        false
    ), COS("cos", x -> Math.cos(x[0]), 1, false), INVERSE("1÷", x -> 1d / x[0], 1, false), OPPOSITE(
        "_",
        x -> -x[0],
        1,
        false
    ), SQRT(
        "√",
        x -> Math.sqrt(x[0]),
        1,
        false
    ), SQ("²", x -> Math.pow(x[0], 2d), 1, false), RE_LU("relu", x -> Math.max(x[0], 0d), 1, false), TERNARY(
        "ternary",
        x -> x[0] >= 0 ? x[1] : x[2],
        3,
        false
    ), MAX("max", x -> Math.max(x[0], x[1]), 2, false), MIN("min", x -> Math.min(x[0], x[1]), 2, false), TANH(
        "tanh",
        x -> Math.tanh(x[0]),
        1,
        false
    ), GT(">", x -> Math.signum(x[0] - x[1]), 2, false), LT("<", x -> Math.signum(x[1] - x[0]), 2, false), POW(
        "^",
        x -> Math.pow(x[0], x[1]),
        2,
        false
    );

    private final String string;
    private final ToDoubleFunction<double[]> function;
    private final int arity;
    private final boolean unlimitedArity;

    Operator(String string, ToDoubleFunction<double[]> function, int arity, boolean unlimitedArity) {
      this.string = string;
      this.function = function;
      this.arity = arity;
      this.unlimitedArity = unlimitedArity;
    }

    public static ToIntFunction<Element> arityFunction() {
      return e -> (e instanceof Operator) ? ((Operator) e).arity : 0;
    }

    @Override
    public double applyAsDouble(double... input) {
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
      return function.applyAsDouble(input);
    }

    public int arity() {
      return arity;
    }

    public boolean isUnlimitedArity() {
      return unlimitedArity;
    }

    @Override
    public String toString() {
      return string;
    }
  }

  record Constant(double value) implements Element, Serializable {

    @Override
    public String toString() {
      return Double.toString(value);
    }
  }

  record Decoration(String string) implements Element, Serializable {

    @Override
    public String toString() {
      return string;
    }
  }

  record Variable(String name) implements Element, Serializable {

    @Override
    public String toString() {
      return name;
    }
  }
}