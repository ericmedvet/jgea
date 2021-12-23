/*
 * Copyright 2020 Eric Medvet <eric.medvet@gmail.com> (as eric)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it.units.malelab.jgea.core.evolver;

import it.units.malelab.jgea.core.listener.Listener;
import it.units.malelab.jgea.core.order.PartiallyOrderedCollection;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.random.RandomGenerator;

/**
 * @author eric
 */
public interface Evolver<G, S, F> {

  interface State {
    State copy();

    int getBirths();

    long getElapsedMillis();

    int getFitnessEvaluations();

    int getIterations();
  }

  record Event<G, S, F>(State state, PartiallyOrderedCollection<Individual<G, S, F>> orderedPopulation,
                        Map<String, Object> attributes) implements Serializable {}

  record Individual<G, S, F>(G genotype, S solution, F fitness, int fitnessMappingIteration,
                             int genotypeBirthIteration) implements Serializable {}

  Collection<S> solve(
      Function<S, F> fitnessFunction,
      Predicate<? super Event<G, S, F>> stopCondition,
      RandomGenerator random,
      ExecutorService executor,
      Listener<? super Event<G, S, F>> listener
  ) throws InterruptedException, ExecutionException;
}
