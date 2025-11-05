/*-
 * ========================LICENSE_START=================================
 * jgea-core
 * %%
 * Copyright (C) 2018 - 2025 Eric Medvet
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
package io.github.ericmedvet.jgea.core.problem;

import io.github.ericmedvet.jgea.core.order.PartialComparator;
import java.util.Comparator;

public interface BBTOProblem<S, B, BQ> extends BehaviorBasedProblem<S, B, BQ>, TotalOrderQualityBasedProblem<S, BehaviorBasedProblem.Outcome<B, BQ>> {

  Comparator<BQ> totalOrderBehaviorQualityComparator();

  @Override
  default PartialComparator<BQ> behaviorQualityComparator() {
    return PartialComparator.from(totalOrderBehaviorQualityComparator());
  }

  @Override
  default Comparator<Outcome<B, BQ>> totalOrderComparator() {
    return Comparator.comparing(Outcome::behaviorQuality, totalOrderBehaviorQualityComparator());
  }

  @Override
  default PartialComparator<Outcome<B, BQ>> qualityComparator() {
    return PartialComparator.from(totalOrderComparator());
  }
}
