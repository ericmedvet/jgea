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

package it.units.malelab.jgea.core.listener.collector;

import com.google.common.graph.ValueGraph;
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
    if (o instanceof ValueGraph) {
      return ((ValueGraph<?, ?>) o).nodes().size()+((ValueGraph<?, ?>) o).edges().size();
    }
    return null;
  }

}
