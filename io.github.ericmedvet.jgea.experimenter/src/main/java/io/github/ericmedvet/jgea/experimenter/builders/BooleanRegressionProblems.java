/*-
 * ========================LICENSE_START=================================
 * jgea-experimenter
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
package io.github.ericmedvet.jgea.experimenter.builders;

import io.github.ericmedvet.jgea.core.representation.tree.bool.Element;
import io.github.ericmedvet.jgea.core.representation.tree.bool.TreeBasedBooleanFunction;
import io.github.ericmedvet.jgea.core.util.IndexedProvider;
import io.github.ericmedvet.jgea.problem.bool.BooleanRegressionProblem;
import io.github.ericmedvet.jgea.problem.bool.BooleanUtils;
import io.github.ericmedvet.jgea.problem.bool.synthetic.EvenParity;
import io.github.ericmedvet.jgea.problem.bool.synthetic.MultipleOutputParallelMultiplier;
import io.github.ericmedvet.jgea.problem.bool.synthetic.PrecomputedSyntheticBRProblem;
import io.github.ericmedvet.jnb.core.Cacheable;
import io.github.ericmedvet.jnb.core.Discoverable;
import io.github.ericmedvet.jnb.core.Param;
import java.util.List;
import java.util.random.RandomGenerator;

@Discoverable(prefixTemplate = "ea.problem|p.booleanRegression|br")
public class BooleanRegressionProblems {

  private BooleanRegressionProblems() {
  }

  @Cacheable
  public static EvenParity evenParity(
      @Param(value = "name", iS = "parity[{n}]") String name,
      @Param("n") int n,
      @Param(value = "metrics", dSs = {"avg_dissimilarity"}) List<BooleanRegressionProblem.Metric> metrics,
      @Param(value = "randomGenerator", dNPM = "m.defaultRG()") RandomGenerator randomGenerator
  ) {
    return new EvenParity(
        metrics,
        n,
        randomGenerator
    );
  }

  @Cacheable
  public static MultipleOutputParallelMultiplier mopm(
      @Param(value = "name", iS = "momp[{n}]") String name,
      @Param("n") int n,
      @Param(value = "metrics", dSs = {"avg_dissimilarity"}) List<BooleanRegressionProblem.Metric> metrics,
      @Param(value = "randomGenerator", dNPM = "m.defaultRG()") RandomGenerator randomGenerator
  ) {
    return new MultipleOutputParallelMultiplier(
        metrics,
        n,
        randomGenerator
    );
  }

  @Cacheable
  public static PrecomputedSyntheticBRProblem targetBased(
      @Param(value = "name", iS = "target") String name,
      @Param("n") int n,
      @Param("exprs") List<String> exprs,
      @Param(value = "metrics", dSs = {"avg_dissimilarity"}) List<BooleanRegressionProblem.Metric> metrics,
      @Param(value = "randomGenerator", dNPM = "m.defaultRG()") RandomGenerator randomGenerator
  ) {
    TreeBasedBooleanFunction booleanFunction = new TreeBasedBooleanFunction(
        exprs.stream().map(e -> Element.stringParser(true).parse(e)).toList(),
        n
    );
    return new PrecomputedSyntheticBRProblem(
        booleanFunction,
        IndexedProvider.from(BooleanUtils.buildCompleteCases(n)),
        IndexedProvider.from(BooleanUtils.buildCompleteCases(n)),
        metrics,
        randomGenerator
    );
  }

}
