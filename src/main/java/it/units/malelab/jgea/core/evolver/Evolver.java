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

import java.util.Collection;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author eric
 */
public interface Evolver<G, S, F> {

  Collection<S> solve(
      Function<S, F> fitnessFunction,
      Predicate<? super Event<G, S, F>> stopCondition,
      Random random,
      ExecutorService executor,
      Listener<? super Event<G, S, F>> listener) throws InterruptedException, ExecutionException;

  class State {
    private int iterations = 0;
    private int births = 0;
    private int fitnessEvaluations = 0;
    private long elapsedMillis = 0;

    public State() {
    }

    public State(int iterations, int births, int fitnessEvaluations, long elapsedMillis) {
      this.iterations = iterations;
      this.births = births;
      this.fitnessEvaluations = fitnessEvaluations;
      this.elapsedMillis = elapsedMillis;
    }

    public int getIterations() {
      return iterations;
    }

    public void setIterations(int iterations) {
      this.iterations = iterations;
    }

    public int getBirths() {
      return births;
    }

    public void setBirths(int births) {
      this.births = births;
    }

    public int getFitnessEvaluations() {
      return fitnessEvaluations;
    }

    public void setFitnessEvaluations(int fitnessEvaluations) {
      this.fitnessEvaluations = fitnessEvaluations;
    }

    public void incFitnessEvaluations(int fitnessEvaluations) {
      setFitnessEvaluations(getFitnessEvaluations() + fitnessEvaluations);
    }

    public long getElapsedMillis() {
      return elapsedMillis;
    }

    public void setElapsedMillis(long elapsedMillis) {
      this.elapsedMillis = elapsedMillis;
    }

    public void incIterations(int n) {
      setIterations(getIterations() + n);
    }

    public void incBirths(int n) {
      setBirths(getBirths() + n);
    }

    public State copy() {
      return new State(iterations, births, fitnessEvaluations, elapsedMillis);
    }
  }
}
