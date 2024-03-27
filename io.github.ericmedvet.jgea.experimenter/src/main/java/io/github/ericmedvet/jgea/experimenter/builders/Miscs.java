/*-
 * ========================LICENSE_START=================================
 * jgea-experimenter
 * %%
 * Copyright (C) 2018 - 2024 Eric Medvet
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */
package io.github.ericmedvet.jgea.experimenter.builders;

import io.github.ericmedvet.jnb.core.Discoverable;
import io.github.ericmedvet.jnb.core.Param;
import java.awt.*;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Discoverable(prefixTemplate = "ea.misc")
public class Miscs {

  private Miscs() {}

  @SuppressWarnings("unused")
  public static Character ch(@Param("s") String s) {
    return s.charAt(0);
  }

  @SuppressWarnings("unused")
  public static Color colorByName(@Param("name") String name) {
    try {
      return (Color) Color.class.getField(name.toUpperCase()).get(null);
    } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
      throw new RuntimeException(e);
    }
  }

  @SuppressWarnings("unused")
  public static Color colorByRgb(@Param("r") int r, @Param("g") int g, @Param("b") int b) {
    return new Color(r, g, b);
  }

  @SuppressWarnings("unused")
  public static <K, V> Map.Entry<K, V> entry(@Param("key") K key, @Param("value") V value) {
    return Map.entry(key, value);
  }

  @SuppressWarnings("unused")
  public static <K, V> Map<K, V> map(@Param("entries") List<Map.Entry<K, V>> entries) {
    Map<K, V> map = new LinkedHashMap<>();
    entries.forEach(e -> map.put(e.getKey(), e.getValue()));
    return Collections.unmodifiableMap(map);
  }

  @SuppressWarnings("unused")
  public static Map.Entry<String, String> sEntry(@Param("key") String key, @Param("value") String value) {
    return Map.entry(key, value);
  }
}
