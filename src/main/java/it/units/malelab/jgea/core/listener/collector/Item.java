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
