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
import it.units.malelab.jgea.core.Sized;
import it.units.malelab.jgea.core.function.Function;
import it.units.malelab.jgea.core.listener.event.EvolutionEvent;
import it.units.malelab.jgea.core.util.Misc;
import it.units.malelab.jgea.core.util.Pair;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author Eric Medvet <eric.medvet@gmail.com>
 */
public class FirstRankIndividualInfo<G, S, F> implements DataCollector {
  
  private final String prefix;
  private final Function<Collection<Individual<G, S, F>>, Individual<G, S, F>> criterion;
  private final IndividualDataCollector<G, S, F> collector;

  public FirstRankIndividualInfo(String prefix, Function<Collection<Individual<G, S, F>>, Individual<G, S, F>> criterion, IndividualDataCollector<G, S, F> collector) {
    this.prefix = prefix;
    this.criterion = criterion;
    this.collector = collector;
  }

  @Override
  public List<Item> collect(EvolutionEvent evolutionEvent) {
    List<Collection<Individual>> rankedPopulation = new ArrayList<>((List)evolutionEvent.getRankedPopulation());
    Individual<G, S, F> chosen = criterion.apply((Collection)rankedPopulation.get(0));
    List<Item> items = new ArrayList<>();
    for (Item item : collector.collect(chosen)) {
      items.add(item.prefixed("r0."+prefix));
    }
    return items;
  }
  
}
