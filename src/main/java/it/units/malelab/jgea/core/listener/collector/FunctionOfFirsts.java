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
import it.units.malelab.jgea.core.listener.Event;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/**
 * @author eric
 * @created 2020/06/17
 * @project jgea
 */
public class FunctionOfFirsts<G, S, F> implements DataCollector<G, S, F> {

  private final Function<Collection<Individual<? extends G, ? extends S, ? extends F>>, List<Item>> function;

  public FunctionOfFirsts(Function<Collection<Individual<? extends G, ? extends S, ? extends F>>, List<Item>> function) {
    this.function = function;
  }

  @Override
  public List<Item> collect(Event<? extends G, ? extends S, ? extends F> event) {
    Collection<Individual<? extends G, ? extends S, ? extends F>> firsts = new ArrayList<>(event.getOrderedPopulation().firsts());
    return function.apply(firsts);
  }
}
