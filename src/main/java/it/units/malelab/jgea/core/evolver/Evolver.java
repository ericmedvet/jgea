/*
 * Copyright (C) 2020 Eric Medvet <eric.medvet@gmail.com> (as eric)
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.units.malelab.jgea.core.evolver;

import it.units.malelab.jgea.core.Problem;
import it.units.malelab.jgea.core.listener.Event;
import it.units.malelab.jgea.core.listener.Listener;

import java.io.Serializable;
import java.util.Collection;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.function.Predicate;

/**
 * @author eric
 */
public interface Evolver<G, S, F> extends Serializable {

  Collection<S> solve(
      Problem<S, F> problem,
      Predicate<Event<? super G, ? super S, ? super F>> stopCondition,
      Random random,
      ExecutorService executor,
      Listener<? super G, ? super S, ? super F> listener) throws InterruptedException, ExecutionException;

  class State implements Serializable {
    private int iterations = 0;
    private int births = 0;
    private int fitnessEvaluations = 0;
    private long elapsedMillis = 0;

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

  }
}
