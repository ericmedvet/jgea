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

package io.github.ericmedvet.jgea.problem.regression.multivariate;

import io.github.ericmedvet.jgea.core.problem.SimpleEBMOProblem;
import io.github.ericmedvet.jgea.core.util.IndexedProvider;
import io.github.ericmedvet.jgea.problem.regression.univariate.UnivariateRegressionProblem;
import io.github.ericmedvet.jnb.datastructure.TriFunction;
import io.github.ericmedvet.jnb.datastructure.Utils;
import io.github.ericmedvet.jsdynsym.core.numerical.MultivariateRealFunction;
import io.github.ericmedvet.jsdynsym.core.numerical.named.NamedMultivariateRealFunction;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SequencedMap;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public interface MultivariateRegressionProblem extends SimpleEBMOProblem<NamedMultivariateRealFunction, Map<String, Double>, Map<String, Double>, MultivariateRegressionProblem.Outcome, Double> {

  @Override
  default SequencedMap<String, Objective<List<Outcome>, Double>> aggregateObjectives() {
    return metrics().stream()
        .collect(
            Utils.toSequencedMap(
                Enum::toString,
                m -> new Objective<>(
                    outcomes -> {
                      Map<String, List<UnivariateRegressionProblem.Outcome>> urOutcomes = outcomes.getFirst().actual
                          .keySet()
                          .stream()
                          .collect(
                              Collectors.toMap(
                                  k -> k,
                                  k -> outcomes.stream()
                                      .map(
                                          outcome -> new UnivariateRegressionProblem.Outcome(
                                              outcome.actual.get(k),
                                              outcome.predicted.get(k)
                                          )
                                      )
                                      .toList()
                              )
                          );
                      return urOutcomes.values()
                          .stream()
                          .mapToDouble(m::apply)
                          .average()
                          .orElseThrow();
                    },
                    m.comparator()
                )
            )
        );
  }

  List<UnivariateRegressionProblem.Metric> metrics();

  static MultivariateRegressionProblem from(
      List<UnivariateRegressionProblem.Metric> metrics,
      IndexedProvider<Example<Map<String, Double>, Map<String, Double>>> caseProvider,
      IndexedProvider<Example<Map<String, Double>, Map<String, Double>>> validationCaseProvider
  ) {
    record HardMultivariateRegressionProblem(
        List<UnivariateRegressionProblem.Metric> metrics,
        IndexedProvider<Example<Map<String, Double>, Map<String, Double>>> caseProvider,
        IndexedProvider<Example<Map<String, Double>, Map<String, Double>>> validationCaseProvider
    ) implements MultivariateRegressionProblem {

    }
    return new HardMultivariateRegressionProblem(metrics, caseProvider, validationCaseProvider);
  }

  record Outcome(Map<String, Double> actual, Map<String, Double> predicted) {

    public Map<String, UnivariateRegressionProblem.Outcome> toUROutcomes() {
      return actual.keySet()
          .stream()
          .collect(
              Utils.toSequencedMap(
                  yVarName -> new UnivariateRegressionProblem.Outcome(
                      actual.get(yVarName),
                      predicted.get(yVarName)
                  )
              )
          );
    }
  }

  @Override
  default TriFunction<Map<String, Double>, Map<String, Double>, Map<String, Double>, Outcome> errorFunction() {
    return (input, actual, predicted) -> new Outcome(actual, predicted);
  }

  @Override
  default BiFunction<NamedMultivariateRealFunction, Map<String, Double>, Map<String, Double>> predictFunction() {
    if (example().isEmpty()) {
      return NamedMultivariateRealFunction::compute;
    }
    NamedMultivariateRealFunction exampleNmrf = example().get();
    return (nmrf, input) -> {
      if (nmrf.nOfOutputs() != exampleNmrf.nOfOutputs()) {
        throw new IllegalArgumentException(
            "Wrong number of outputs: %d found, %d expected".formatted(
                nmrf.nOfOutputs(),
                exampleNmrf.nOfOutputs()
            )
        );
      }
      if (nmrf.nOfInputs() != exampleNmrf.nOfInputs()) {
        throw new IllegalArgumentException(
            "Wrong number of inputs: %d found, %d expected".formatted(
                nmrf.nOfInputs(),
                exampleNmrf.nOfInputs()
            )
        );
      }
      return nmrf.compute(input);
    };
  }

  @Override
  default Optional<NamedMultivariateRealFunction> example() {
    Example<Map<String, Double>, Map<String, Double>> example = caseProvider().first();
    return Optional.of(
        NamedMultivariateRealFunction.from(
            MultivariateRealFunction.from(
                vs -> new double[example.output().size()],
                example.input().size(),
                example.output().size()
            ),
            example.input().keySet().stream().sorted().toList(),
            example.output().keySet().stream().sorted().toList()
        )
    );
  }
}