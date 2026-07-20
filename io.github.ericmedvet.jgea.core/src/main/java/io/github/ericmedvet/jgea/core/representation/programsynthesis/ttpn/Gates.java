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

package io.github.ericmedvet.jgea.core.representation.programsynthesis.ttpn;

import io.github.ericmedvet.jgea.core.representation.programsynthesis.type.Base;
import io.github.ericmedvet.jgea.core.representation.programsynthesis.type.Composed;
import io.github.ericmedvet.jgea.core.representation.programsynthesis.type.Generic;
import io.github.ericmedvet.jgea.core.representation.tree.numeric.Element;
import io.github.ericmedvet.jnb.datastructure.NamedFunction;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Gates {

  private static final int MAX_N_OF_OUT_TOKENS = 100;

  private Gates() {
  }

  public static Gate bAnd() {
    return Gate.of(
        List.of(Gate.Port.single(Base.BOOLEAN), Gate.Port.single(Base.BOOLEAN)),
        List.of(Base.BOOLEAN),
        NamedFunction.from(
            in -> Gate.Data.singleOne(in.one(0, Boolean.class) && in.one(1, Boolean.class)),
            "and"
        )
    );
  }

  public static Gate bConst(boolean value) {
    return Gate.of(
        List.of(Gate.Port.single(Generic.of("t"))),
        List.of(Base.BOOLEAN),
        NamedFunction.from(
            in -> Gate.Data.singleOne(value),
            "const[%s]".formatted(value)
        )
    );
  }

  public static Gate bNot() {
    return Gate.of(
        List.of(Gate.Port.single(Base.BOOLEAN)),
        List.of(Base.BOOLEAN),
        NamedFunction.from(
            in -> Gate.Data.singleOne(!in.one(0, Boolean.class)),
            "not"
        )
    );
  }

  public static Gate bOr() {
    return Gate.of(
        List.of(Gate.Port.single(Base.BOOLEAN), Gate.Port.single(Base.BOOLEAN)),
        List.of(Base.BOOLEAN),
        NamedFunction.from(
            in -> Gate.Data.singleOne(in.one(0, Boolean.class) || in.one(1, Boolean.class)),
            "or"
        )
    );
  }

  public static Gate bXor() {
    return Gate.of(
        List.of(Gate.Port.single(Base.BOOLEAN), Gate.Port.single(Base.BOOLEAN)),
        List.of(Base.BOOLEAN),
        NamedFunction.from(
            in -> {
              boolean b0 = in.one(0, Boolean.class);
              boolean b1 = in.one(1, Boolean.class);
              return Gate.Data.singleOne((b0 && !b1) || (b1 && !b0));
            },
            "xor"
        )
    );
  }

  public static Gate concat() {
    return Gate.of(
        List.of(
            Gate.Port.single(Composed.sequence(Generic.of("t"))),
            Gate.Port.single(Composed.sequence(Generic.of("t")))
        ),
        List.of(Composed.sequence(Generic.of("t"))),
        NamedFunction.from(
            in -> Gate.Data.singleOne(
                Stream.concat(in.one(0, List.class).stream(), in.one(1, List.class).stream()).toList()
            ),
            "concat"
        )
    );
  }

  public static Gate equal() {
    return Gate.of(
        List.of(Gate.Port.single(Generic.of("t")), Gate.Port.single(Generic.of("t"))),
        List.of(Base.BOOLEAN),
        NamedFunction.from(
            in -> Gate.Data.singleOne(in.one(0).equals(in.one(1))),
            "equal"
        )
    );
  }

  public static Gate iBefore() {
    return Gate.of(
        List.of(Gate.Port.single(Base.INT), Gate.Port.single(Base.INT)),
        List.of(Base.BOOLEAN),
        NamedFunction.from(
            in -> Gate.Data.singleOne(in.one(0, Integer.class).compareTo(in.one(1, Integer.class)) < 0),
            "iBefore"
        )
    );
  }

  public static Gate iConst(int value) {
    return Gate.of(
        List.of(Gate.Port.single(Generic.of("t"))),
        List.of(Base.INT),
        NamedFunction.from(
            in -> Gate.Data.singleOne(value),
            "const[%s]".formatted(value)
        )
    );
  }

  public static Gate iPMathOperator(Element.Operator operator) {
    return Gate.of(
        Collections.nCopies(operator.arity(), Gate.Port.single(Base.INT)),
        List.of(Base.INT),
        NamedFunction.from(
            in -> Gate.Data.singleOne(
                (int) operator.applyAsDouble(
                    in.ones(Integer.class)
                        .stream()
                        .mapToDouble(d -> d)
                        .toArray()
                )
            ),
            "%s".formatted(operator.toString())
        )
    );
  }

  public static Gate iRange() {
    return Gate.of(
        List.of(Gate.Port.single(Base.INT), Gate.Port.single(Base.INT)),
        List.of(Base.INT),
        NamedFunction.from(
            in -> {
              int n = in.one(1, Integer.class);
              if (n > MAX_N_OF_OUT_TOKENS) {
                throw new IllegalArgumentException(
                    "Size of token train is too large: %d>%d".formatted(
                        n,
                        MAX_N_OF_OUT_TOKENS
                    )
                );
              }
              return Gate.Data.single(
                  IntStream.range(0, n)
                      .boxed()
                      .map(Object.class::cast)
                      .toList()
              );
            },
            "iRange"
        )
    );
  }

  public static Gate iSMult() {
    return Gate.of(
        List.of(Gate.Port.atLeast(Base.INT, 2)),
        List.of(Base.INT),
        NamedFunction.from(
            in -> Gate.Data.singleOne(
                in.all(0, Integer.class)
                    .stream()
                    .reduce((n1, n2) -> n1 * n2)
                    .orElseThrow()
            ),
            "s*"
        )
    );
  }

  public static Gate iSPMult() {
    return Gate.of(
        List.of(Gate.Port.atLeast(Base.INT, 1), Gate.Port.atLeast(Base.INT, 0)),
        List.of(Base.INT),
        NamedFunction.from(
            in -> Gate.Data.singleOne(
                in.all(0, Integer.class)
                    .stream()
                    .reduce((n1, n2) -> n1 * n2)
                    .orElseThrow() * in.all(1, Integer.class)
                        .stream()
                        .reduce((n1, n2) -> n1 * n2)
                        .orElse(1)
            ),
            "sp*"
        )
    );
  }

  public static Gate iSPSum() {
    return Gate.of(
        List.of(Gate.Port.atLeast(Base.INT, 1), Gate.Port.atLeast(Base.INT, 0)),
        List.of(Base.INT),
        NamedFunction.from(
            in -> Gate.Data.singleOne(
                in.all(0, Integer.class)
                    .stream()
                    .reduce(Integer::sum)
                    .orElseThrow() + in.all(1, Integer.class)
                        .stream()
                        .reduce(Integer::sum)
                        .orElse(0)
            ),
            "sp+"
        )
    );
  }

  public static Gate iSSum() {
    return Gate.of(
        List.of(Gate.Port.atLeast(Base.INT, 2)),
        List.of(Base.INT),
        NamedFunction.from(
            in -> Gate.Data.singleOne(
                in.all(0, Integer.class)
                    .stream()
                    .reduce(Integer::sum)
                    .orElseThrow()
            ),
            "s+"
        )
    );
  }

  public static Gate iTh() {
    return Gate.of(
        List.of(
            Gate.Port.single(Composed.sequence(Generic.of("t"))),
            Gate.Port.single(Base.INT)
        ),
        List.of(Generic.of("t")),
        NamedFunction.from(in -> Gate.Data.singleOne(in.one(0, List.class).get(in.one(1, Integer.class))), "iTh")
    );
  }

  public static Gate iToR() {
    return Gate.of(
        List.of(Gate.Port.single(Base.INT)),
        List.of(Base.REAL),
        NamedFunction.from(
            in -> Gate.Data.singleOne(
                in.one(0, Integer.class).doubleValue()
            ),
            "iToR"
        )
    );
  }

  public static Gate length() {
    return Gate.of(
        List.of(Gate.Port.single(Composed.sequence(Generic.of("t")))),
        List.of(Base.INT),
        NamedFunction.from(in -> Gate.Data.singleOne(in.one(0, List.class).size()), "length")
    );
  }

  public static Gate noop() {
    return Gate.of(
        List.of(Gate.Port.single(Generic.of("t"))),
        List.of(Generic.of("t")),
        NamedFunction.from(in -> Gate.Data.singleOne(in.one(0)), "noop")
    );
  }

  public static Gate pairer() {
    return Gate.of(
        List.of(Gate.Port.single(Generic.of("f")), Gate.Port.single(Generic.of("s"))),
        List.of(Composed.tuple(List.of(Generic.of("f"), Generic.of("s")))),
        NamedFunction.from(in -> Gate.Data.singleOne(List.of(in.one(0), in.one(1))), "pairer")
    );
  }

  public static Gate queuer() {
    return Gate.of(
        List.of(Gate.Port.single(Generic.of("t")), Gate.Port.single(Generic.of("t"))),
        List.of(Generic.of("t")),
        NamedFunction.from(
            in -> Gate.Data.single(
                List.of(
                    in.one(0),
                    in.one(1)
                )
            ),
            "queuer"
        )
    );
  }

  public static Gate rBefore() {
    return Gate.of(
        List.of(Gate.Port.single(Base.REAL), Gate.Port.single(Base.REAL)),
        List.of(Base.BOOLEAN),
        NamedFunction.from(
            in -> Gate.Data.singleOne(in.one(0, Double.class).compareTo(in.one(1, Double.class)) < 0),
            "rBefore"
        )
    );
  }

  public static Gate rConst(double value) {
    return Gate.of(
        List.of(Gate.Port.single(Generic.of("t"))),
        List.of(Base.REAL),
        NamedFunction.from(
            in -> Gate.Data.singleOne(value),
            "const[%.1f]".formatted(value)
        )
    );
  }

  public static Gate rPMathOperator(Element.Operator operator) {
    return Gate.of(
        Collections.nCopies(operator.arity(), Gate.Port.single(Base.REAL)),
        List.of(Base.REAL),
        NamedFunction.from(
            in -> Gate.Data.singleOne(
                operator.applyAsDouble(
                    in.ones(Double.class)
                        .stream()
                        .mapToDouble(d -> d)
                        .toArray()
                )
            ),
            "%s".formatted(operator.toString())
        )
    );
  }

  public static Gate rSMult() {
    return Gate.of(
        List.of(Gate.Port.atLeast(Base.REAL, 2)),
        List.of(Base.REAL),
        NamedFunction.from(
            in -> Gate.Data.singleOne(
                in.all(0, Double.class)
                    .stream()
                    .reduce((n1, n2) -> n1 * n2)
                    .orElseThrow()
            ),
            "s*"
        )
    );
  }

  public static Gate rSPMult() {
    return Gate.of(
        List.of(Gate.Port.atLeast(Base.REAL, 1), Gate.Port.atLeast(Base.REAL, 0)),
        List.of(Base.REAL),
        NamedFunction.from(
            in -> Gate.Data.singleOne(
                in.all(0, Double.class)
                    .stream()
                    .reduce((n1, n2) -> n1 * n2)
                    .orElseThrow() * in.all(1, Double.class)
                        .stream()
                        .reduce((n1, n2) -> n1 * n2)
                        .orElse(1d)
            ),
            "sp*"
        )
    );
  }

  public static Gate rSPSum() {
    return Gate.of(
        List.of(Gate.Port.atLeast(Base.REAL, 1), Gate.Port.atLeast(Base.REAL, 0)),
        List.of(Base.REAL),
        NamedFunction.from(
            in -> Gate.Data.singleOne(
                in.all(0, Double.class)
                    .stream()
                    .reduce(Double::sum)
                    .orElseThrow() + in.all(1, Double.class)
                        .stream()
                        .reduce(Double::sum)
                        .orElse(0d)
            ),
            "sp+"
        )
    );
  }

  public static Gate rSSum() {
    return Gate.of(
        List.of(Gate.Port.atLeast(Base.REAL, 2)),
        List.of(Base.REAL),
        NamedFunction.from(
            in -> Gate.Data.singleOne(
                in.all(0, Double.class)
                    .stream()
                    .reduce(Double::sum)
                    .orElseThrow()
            ),
            "s+"
        )
    );
  }

  public static Gate rToI() {
    return Gate.of(
        List.of(Gate.Port.single(Base.REAL)),
        List.of(Base.INT),
        NamedFunction.from(
            in -> Gate.Data.singleOne(
                in.one(0, Double.class).intValue()
            ),
            "rToI"
        )
    );
  }

  public static Gate repeater() {
    return Gate.of(
        List.of(Gate.Port.single(Generic.of("t")), Gate.Port.single(Base.INT)),
        List.of(Generic.of("t")),
        NamedFunction.from(
            in -> {
              int n = in.one(1, Integer.class);
              if (n > MAX_N_OF_OUT_TOKENS) {
                throw new IllegalArgumentException(
                    "Size of token train is too large: %d>%d".formatted(
                        n,
                        MAX_N_OF_OUT_TOKENS
                    )
                );
              }
              return Gate.Data.single(Collections.nCopies(n, in.one(0)));
            },
            "repeater"
        )
    );
  }

  public static Gate sBefore() {
    return Gate.of(
        List.of(Gate.Port.single(Base.STRING), Gate.Port.single(Base.STRING)),
        List.of(Base.BOOLEAN),
        NamedFunction.from(
            in -> Gate.Data.singleOne(in.one(0, Double.class).compareTo(in.one(1, Double.class)) < 0),
            "sBefore"
        )
    );
  }

  public static Gate sConcat() {
    return Gate.of(
        List.of(Gate.Port.single(Base.STRING), Gate.Port.single(Base.STRING)),
        List.of(Base.STRING),
        NamedFunction.from(
            in -> Gate.Data.singleOne(in.one(0, String.class) + in.one(1, String.class)),
            "sConcat"
        )
    );
  }

  public static Gate sConst(String value) {
    return Gate.of(
        List.of(Gate.Port.single(Generic.of("t"))),
        List.of(Base.STRING),
        NamedFunction.from(
            in -> Gate.Data.singleOne(value),
            "const[%s]".formatted(value)
        )
    );
  }

  public static Gate sSplitter() {
    return Gate.of(
        List.of(Gate.Port.single(Base.STRING)),
        List.of(Composed.sequence(Base.STRING)),
        NamedFunction.from(
            in -> Gate.Data.singleOne(
                Arrays.stream(in.one(0, String.class).split(""))
                    .map(s -> (Object) s)
                    .toList()
            ),
            "sSplitter"
        )
    );
  }

  public static Gate select() {
    return Gate.of(
        List.of(Gate.Port.single(Generic.of("t")), Gate.Port.single(Generic.of("t")), Gate.Port.single(Base.BOOLEAN)),
        List.of(Generic.of("t")),
        NamedFunction.from(
            in -> Gate.Data.singleOne(in.one(2, Boolean.class) ? in.one(0) : in.one(1)),
            "select"
        )
    );
  }

  public static Gate sequencer() {
    return Gate.of(
        List.of(Gate.Port.atLeast(Generic.of("t"), 1)),
        List.of(Composed.sequence(Generic.of("t"))),
        NamedFunction.from(in -> Gate.Data.singleOne(in.all(0)), "sequencer")
    );
  }

  public static Gate sPSequencer() {
    return Gate.of(
        List.of(
            Gate.Port.atLeast(Generic.of("t"), 1),
            Gate.Port.atLeast(Composed.sequence(Generic.of("t")), 0)
        ),
        List.of(Composed.sequence(Generic.of("t"))),
        NamedFunction.from(
            in -> {
              List<Object> tokens = in.all(0);
              @SuppressWarnings({"unchecked", "rawtypes"}) List<List<Object>> sequences = (List) in.all(1, List.class);
              return Gate.Data.singleOne(
                  Stream.concat(
                      sequences.stream().flatMap(List::stream),
                      tokens.stream()
                  ).toList()
              );
            },
            "sPSequencer"
        )
    );
  }

  public static Gate sink() {
    return Gate.of(
        List.of(Gate.Port.single(Generic.of("t"))),
        List.of(),
        NamedFunction.from(
            in -> Gate.Data.empty(),
            "sink"
        )
    );
  }

  public static Gate splitter() {
    //noinspection unchecked
    return Gate.of(
        List.of(Gate.Port.single(Composed.sequence(Generic.of("t")))),
        List.of(Generic.of("t")),
        NamedFunction.from(in -> Gate.Data.single(in.one(0, List.class)), "splitter")
    );
  }

  public static Gate unpairer() {
    return Gate.of(
        List.of(Gate.Port.single(Composed.tuple(List.of(Generic.of("f"), Generic.of("s"))))),
        List.of(Generic.of("f"), Generic.of("s")),
        NamedFunction.from(
            in -> Gate.Data.pairOne(
                in.one(0, List.class).getFirst(),
                in.one(0, List.class).get(1)
            ),
            "unpairer"
        )
    );
  }


}