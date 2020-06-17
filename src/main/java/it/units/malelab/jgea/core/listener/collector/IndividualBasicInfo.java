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
import it.units.malelab.jgea.core.util.Sized;
import it.units.malelab.jgea.core.function.Function;
import it.units.malelab.jgea.core.util.Misc;
import it.units.malelab.jgea.core.util.Pair;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Eric Medvet <eric.medvet@gmail.com>
 */
public class IndividualBasicInfo<G, S, F> implements IndividualDataCollector<G, S, F> {

  private final Function<F, List<Item>> fitnessSplitter;

  public IndividualBasicInfo(Function<F, List<Item>> fitnessSplitter) {
    this.fitnessSplitter = fitnessSplitter;
  }
  
  public IndividualBasicInfo(Function<?, F> function, String... formats) {
    this(Item.fromMultiobjective((Function)function, formats));
  }
  
  public IndividualBasicInfo(String format) {
    this((f, listener) -> Collections.singletonList(new Item<>("", f, format)));
  }
  
  
  @Override
  public List<Item> collect(Individual<G, S, F> chosen) {
    List<Item> items = new ArrayList<>();
    items.add(new Item<>("genotype.size", size(chosen.getGenotype()), "%4d"));
    items.add(new Item<>("solution.size", size(chosen.getSolution()), "%4d"));
    items.add(new Item<>("birth.iteration", chosen.getBirthIteration(), "%3d"));
    for (Item fitnessItem : fitnessSplitter.apply((F)chosen.getFitness())) {
      items.add(fitnessItem.prefixed("fitness"));
    }
    return items;
  }
  
  public static Integer size(Object o) {
    if (o instanceof Sized) {
      return ((Sized)o).size();
    }
    if (o instanceof Collection) {
      if (Misc.first((Collection)o) instanceof Sized) {
        return ((Collection)o).stream().mapToInt(i -> ((Sized)i).size()).sum();
      }
      return ((Collection)o).size();
    }
    if (o instanceof String) {
      return ((String)o).length();
    }
    if (o instanceof Pair) {
      Integer firstSize = size(((Pair)o).first());
      Integer secondSize = size(((Pair)o).second());
      if ((firstSize!=null)&&(secondSize!=null)) {
        return firstSize+secondSize;
      }
    }
    return null;
  }

}
