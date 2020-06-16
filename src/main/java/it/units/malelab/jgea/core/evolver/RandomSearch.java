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

import it.units.malelab.jgea.core.Factory;
import it.units.malelab.jgea.core.Individual;
import it.units.malelab.jgea.core.PartiallyOrderedCollection;
import it.units.malelab.jgea.core.Problem;

import java.util.Collection;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;

/**
 * @author eric
 * @created 2020/06/16
 * @project jgea
 */
public class RandomSearch<G, S, F> extends AbstractIterativeEvolver<G, S, F> {

  public RandomSearch(Function<G, S> solutionMapper, Factory<? extends G> genotypeFactory, PartiallyOrderedCollection.PartialComparator<Individual<? super G, ? super S, ? super F>> individualComparator) {
    super(solutionMapper, genotypeFactory, individualComparator);
  }

  @Override
  protected Collection<Individual<G, S, F>> initPopulation(Problem<S, F> problem, Random random, ExecutorService executor, Metrics metrics) {
    return null;
  }

  @Override
  protected Collection<Individual<G, S, F>> updatePopulation(PartiallyOrderedCollection<Individual<G, S, F>> population, Problem<S, F> problem, Random random, ExecutorService executor, Metrics metrics) {
    return null;
  }
}
