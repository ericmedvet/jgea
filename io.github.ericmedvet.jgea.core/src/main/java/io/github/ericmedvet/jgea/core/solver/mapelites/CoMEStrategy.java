/*-
 * ========================LICENSE_START=================================
 * jgea-core
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
package io.github.ericmedvet.jgea.core.solver.mapelites;

import io.github.ericmedvet.jgea.core.order.PartialComparator;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public interface CoMEStrategy {

  enum Prepared implements Supplier<CoMEStrategy> {
    IDENTITY(() -> tc -> tc),
    CENTRAL(() -> tc -> Collections.nCopies(tc.size(), 0.5d)),
    BEST(BestStrategy::new),
    M1(M1Strategy::new);
    private final Supplier<CoMEStrategy> supplier;

    Prepared(Supplier<CoMEStrategy> supplier) {
      this.supplier = supplier;
    }

    @Override
    public CoMEStrategy get() {
      return supplier.get();
    }
  }

  record Observation<Q>(List<Double> theseCoords, List<Double> otherCoords, Q q) {}

  List<Double> getOtherCoords(List<Double> theseCoords);

  default <Q> void update(Collection<Observation<Q>> newObservations, PartialComparator<Q> qComparator) {}
}
