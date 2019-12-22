/*
 * Copyright (C) 2019 Eric Medvet <eric.medvet@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.units.malelab.jgea.core.listener.collector;

import it.units.malelab.jgea.core.Individual;
import it.units.malelab.jgea.core.function.Function;
import it.units.malelab.jgea.core.function.FunctionException;
import it.units.malelab.jgea.core.listener.Listener;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author Eric Medvet <eric.medvet@gmail.com>
 */
public class FirstOfNthObjective<G, S> implements Function<Collection<Individual<G, S, List<? extends Comparable>>>, Individual<G, S, List<? extends Comparable>>> {
  
  private final int n;
  private final boolean reverse;

  public FirstOfNthObjective(int n, boolean reverse) {
    this.n = n;
    this.reverse = reverse;
  }

  public FirstOfNthObjective(int n) {
    this(n, false);
  }

  @Override
  public Individual<G, S, List<? extends Comparable>> apply(Collection<Individual<G, S, List<? extends Comparable>>> individuals, Listener listener) throws FunctionException {
    Individual<G, S, List<? extends Comparable>> first = null;
    for (Individual<G, S, List<? extends Comparable>> individual : individuals) {
      if (first==null) {
        first = individual;
      } else {
        Comparable firstValue = first.getFitness().get(n);
        Comparable individualValue = individual.getFitness().get(n);
        if (individualValue.compareTo(firstValue)*(reverse?-1:1)<0) {
          first = individual;
        }
      }
    }
    return first;
  }
  
}
