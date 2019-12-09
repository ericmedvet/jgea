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
public class LowestNormalizedSum<G, S> implements Function<Collection<Individual<G, S, List<Double>>>, Individual<G, S, List<Double>>> {

  @Override
  public Individual<G, S, List<Double>> apply(Collection<Individual<G, S, List<Double>>> individuals, Listener listener) throws FunctionException {
    //copy rank and prepare map
    List<Individual<G, S, List<Double>>> rank = new ArrayList<>(individuals);
    Map<Individual<G, S, List<Double>>, Double> scores = new LinkedHashMap<>();
    for (Individual<G, S, List<Double>> individual : rank) {
      scores.put(individual, 0d);
    }
    //count objectives
    int nObjs = rank.get(0).getFitness().size();
    //iterate over objectives
    for (int i = 0; i < nObjs; i++) {
      double min = Double.POSITIVE_INFINITY;
      double max = Double.NEGATIVE_INFINITY;
      for (Individual<G, S, List<Double>> individual : rank) {
        double value = individual.getFitness().get(i);
        if (value < min) {
          min = value;
        }
        if (value > max) {
          max = value;
        }
      }
      if (max > min) {
        for (Individual<G, S, List<Double>> individual : rank) {
          double relativeValue = (individual.getFitness().get(i) - min) / (max - min);          
          scores.put(individual, scores.get(individual) + Math.abs(relativeValue-0.5d));
        }
      }
    }
    //sort by scores
    Collections.sort(rank, (Individual<G, S, List<Double>> i1, Individual<G, S, List<Double>> i2)
            -> scores.get(i1).compareTo(scores.get(i2))
    );
    return rank.get(0);
  }

}
