/*-
 * ========================LICENSE_START=================================
 * jgea-problem
 * %%
 * Copyright (C) 2018 - 2024 Eric Medvet
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

package io.github.ericmedvet.jgea.problem.synthetic.numerical;

import static io.github.ericmedvet.jgea.core.util.VectorUtils.*;

import io.github.ericmedvet.jgea.core.distance.Distance;
import io.github.ericmedvet.jgea.core.problem.MultiTargetProblem;
import io.github.ericmedvet.jgea.core.problem.ProblemWithExampleSolution;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class PointsAiming implements ProblemWithExampleSolution<List<Double>>, MultiTargetProblem<List<Double>> {

  private final List<List<Double>> targets;
  private final int p;

  private final Distance<List<Double>> distance;

  public PointsAiming(List<List<Double>> targets) {
    if (targets.isEmpty()) {
      throw new IllegalArgumentException("No target points given");
    }
    checkLengthsList(targets);
    this.targets = Collections.unmodifiableList(targets);
    distance = (vs1, vs2) -> norm(diff(vs1, vs2), 2);
    p = targets.get(0).size();
  }

  @Override
  public Distance<List<Double>> distance() {
    return distance;
  }

  @Override
  public Collection<List<Double>> targets() {
    return targets;
  }

  @Override
  public List<Double> example() {
    return Collections.nCopies(p, 0d);
  }
}
