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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author eric
 */
public class Static implements DataCollector<Object, Object, Object> {

  private final Map<String, ?> values;

  public Static(Map<String, ?> values) {
    this.values = values;
  }

  @Override
  public List<Item> collect(Event<?, ?, ?> event) {
    return values.entrySet().stream()
        .map(entry -> new Item(entry.getKey(), entry.getValue(), "%" + entry.getValue().toString().length() + "." + entry.getValue().toString().length() + "s"))
        .collect(Collectors.toList());
  }

}
