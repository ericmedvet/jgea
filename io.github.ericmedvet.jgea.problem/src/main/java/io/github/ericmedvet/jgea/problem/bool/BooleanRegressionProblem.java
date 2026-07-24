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

import io.github.ericmedvet.jgea.core.problem.SimpleEBMOProblem;
import io.github.ericmedvet.jnb.datastructure.TriFunction;
import io.github.ericmedvet.jnb.datastructure.Utils;
import io.github.ericmedvet.jsdynsym.core.bool.BooleanFunction;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.SequencedMap;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.IntStream;

public interface BooleanRegressionProblem extends SimpleEBMOProblem<BooleanFunction, boolean[], boolean[], BooleanRegressionProblem.Outcome, Double> {
  record Outcome(boolean[] actual, boolean[] predicted) {}

  enum Metric implements Function<List<Outcome>, Double> {
    ERROR_RATE(
        outcomes -> (double) outcomes.stream()
            .filter(o -> Arrays.equals(o.actual, o.predicted))
            .count() / (double) outcomes.size()
    ), AVG_DISSIMILARITY(
        outcomes -> outcomes.stream()
            .mapToDouble(
                outcome -> IntStream.range(0, outcome.actual.length)
                    .mapToDouble(i -> outcome.actual[i] == outcome.predicted[i] ? 0d : 1d)
                    .average()
                    .orElseThrow()
            )
            .average()
            .orElseThrow()
    );

    private final Function<List<Outcome>, Double> function;

    Metric(Function<List<Outcome>, Double> function) {
      this.function = function;
    }

    @Override
    public Double apply(List<Outcome> ys) {
      return function.apply(ys);
    }

    @Override
    public String toString() {
      return name().toLowerCase();
    }
  }

  List<Metric> metrics();

  @Override
  default SequencedMap<String, Objective<List<Outcome>, Double>> aggregateObjectives() {
    return metrics().stream()
        .collect(
            Utils.toSequencedMap(
                Enum::toString,
                m -> new Objective<>(m, Double::compareTo)
            )
        );
  }

  @Override
  default TriFunction<boolean[], boolean[], boolean[], Outcome> errorFunction() {
    return (input, actual, predicted) -> new Outcome(actual, predicted);
  }

  @Override
  default BiFunction<BooleanFunction, boolean[], boolean[]> predictFunction() {
    if (example().isEmpty()) {
      return BooleanFunction::apply;
    }
    BooleanFunction eBooleanFunction = example().get();
    return (booleanFunction, t) -> {
      if (booleanFunction.nOfOutputs() != eBooleanFunction.nOfOutputs()) {
        throw new IllegalArgumentException(
            "Wrong number of outputs: %d found, %d expected".formatted(
                booleanFunction.nOfOutputs(),
                eBooleanFunction.nOfOutputs()
            )
        );
      }
      if (booleanFunction.nOfInputs() != eBooleanFunction.nOfInputs()) {
        throw new IllegalArgumentException(
            "Wrong number of inputs: %d found, %d expected".formatted(
                booleanFunction.nOfInputs(),
                eBooleanFunction.nOfInputs()
            )
        );
      }
      return booleanFunction.apply(t);
    };
  }

  @Override
  default Optional<BooleanFunction> example() {
    Example<boolean[], boolean[]> example = caseProvider().first();
    return Optional.of(
        BooleanFunction.from(
            inputs -> new boolean[example.output().length],
            example.input().length,
            example.output().length
        )
    );
  }

}