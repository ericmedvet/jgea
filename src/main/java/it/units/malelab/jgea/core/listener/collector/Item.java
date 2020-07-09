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

import it.units.malelab.jgea.core.listener.Listener;
import it.units.malelab.jgea.core.util.WithNames;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * @author eric
 */
public class Item {

  private final String name;
  private final Object value;
  private final String format;

  public Item(String name, Object value, String format) {
    this.name = name;
    this.value = value;
    this.format = format;
  }

  public String getName() {
    return name;
  }

  public Object getValue() {
    return value;
  }

  public String getFormat() {
    return format;
  }

  public Item prefixed(String prefix) {
    return new Item(name.isEmpty() ? prefix : (prefix + "." + name), value, format);
  }

  public Item suffixed(String suffix) {
    return new Item(name.isEmpty() ? suffix : (name + "." + suffix), value, format);
  }

  @Override
  public String toString() {
    return "Item{" + "name=" + name + ", value=" + value + ", format=" + format + '}';
  }

}
