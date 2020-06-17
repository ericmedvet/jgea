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

package it.units.malelab.jgea.core.listener.collector;

import it.units.malelab.jgea.core.Individual;
import it.units.malelab.jgea.core.util.Sized;
import it.units.malelab.jgea.core.util.Misc;
import it.units.malelab.jgea.core.util.Pair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/**
 * @author Eric Medvet <eric.medvet@gmail.com>
 */
public class IndividualBasicInfo<F> implements Function<Individual<? extends Object, ? extends Object, ? extends F>, List<Item>> {

  private final Function<? super F, List<Item>> fitnessSplitter;

  public IndividualBasicInfo(Function<? super F, List<Item>> fitnessSplitter) {
    this.fitnessSplitter = fitnessSplitter;
  }

  @Override
  public List<Item> apply(Individual<? extends Object, ? extends Object, ? extends F> individual) {
    List<Item> items = new ArrayList<>();
    items.add(new Item("genotype.size", size(individual.getGenotype()), "%4d"));
    items.add(new Item("solution.size", size(individual.getSolution()), "%4d"));
    items.add(new Item("birth.iteration", individual.getBirthIteration(), "%3d"));
    for (Item fitnessItem : fitnessSplitter.apply(individual.getFitness())) {
      items.add(fitnessItem.prefixed("fitness"));
    }
    return items;
  }

  public static Integer size(Object o) {
    if (o instanceof Sized) {
      return ((Sized) o).size();
    }
    if (o instanceof Collection) {
      if (Misc.first((Collection<?>) o) instanceof Sized) {
        return ((Collection<?>) o).stream().mapToInt(i -> ((Sized) i).size()).sum();
      }
      return ((Collection<?>) o).size();
    }
    if (o instanceof String) {
      return ((String) o).length();
    }
    if (o instanceof Pair) {
      Integer firstSize = size(((Pair<?, ?>) o).first());
      Integer secondSize = size(((Pair<?, ?>) o).second());
      if ((firstSize != null) && (secondSize != null)) {
        return firstSize + secondSize;
      }
    }
    return null;
  }

}
