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

import it.units.malelab.jgea.core.listener.Event;

import java.util.Arrays;
import java.util.List;

/**
 * @author eric
 */
public class Basic implements DataCollector<Object, Object, Object> {

  @Override
  public List<Item> collect(Event<?, ?, ?> event) {
    return Arrays.asList(
        new Item("iterations", event.getState().getIterations(), "%4d"),
        new Item("births", event.getState().getBirths(), "%8d"),
        new Item("fitness.evaluations", event.getState().getFitnessEvaluations(), "%6d"),
        new Item("elapsed.sec", (double) event.getState().getElapsedMillis() / 1000d, "%6.1f")
    );
  }

}
