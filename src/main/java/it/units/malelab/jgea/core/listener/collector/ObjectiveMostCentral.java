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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Eric Medvet <eric.medvet@gmail.com>
 */
public class ObjectiveMostCentral<G, S> implements Function<Collection<Individual<G, S, List<? extends Comparable>>>, Individual<G, S, List<? extends Comparable>>> {

  @Override
  public Individual<G, S, List<? extends Comparable>> apply(Collection<Individual<G, S, List<? extends Comparable>>> individuals, Listener listener) throws FunctionException {
    //copy rank and prepare map
    List<Individual<G, S, List<? extends Comparable>>> rank = new ArrayList<>(individuals);
    Map<Individual<G, S, List<? extends Comparable>>, Double> scores = new LinkedHashMap<>();
    for (Individual<G, S, List<? extends Comparable>> individual : rank) {
      scores.put(individual, 0d);
    }
    //count objectives
    int nObjs = rank.get(0).getFitness().size();
    //iterate over objectives
    for (int i = 0; i < nObjs; i++) {
      final int j = i;
      Collections.sort(rank, (Individual<G, S, List<? extends Comparable>> i1, Individual<G, S, List<? extends Comparable>> i2) -> {
        return i1.getFitness().get(j).compareTo(i2.getFitness().get(j));
      });
      for (int k = 0; k < rank.size(); k++) {
        Individual<G, S, List<? extends Comparable>> individual = rank.get(k);
        scores.put(individual, scores.get(individual) + Math.abs((double) k - (double) rank.size()));
      }
    }
    //sort by scores
    Collections.sort(rank, (Individual<G, S, List<? extends Comparable>> i1, Individual<G, S, List<? extends Comparable>> i2)
            -> scores.get(i1).compareTo(scores.get(i2))
    );
    return rank.get(0);
  }

}
