package io.github.ericmedvet.jgea.experimenter.builders;

import io.github.ericmedvet.jgea.experimenter.util.ImagePlotters;
import io.github.ericmedvet.jnb.core.Discoverable;
import io.github.ericmedvet.jnb.core.Param;

import java.awt.*;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Discoverable(prefixTemplate = "ea.misc")
public class Misc {

  private Misc() {
  }

  @SuppressWarnings("unused")
  public static Color colorByName(
      @Param("name") String name
  ) {
    try {
      return (Color) Color.class.getField(name.toUpperCase()).get(null);
    } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
      throw new RuntimeException(e);
    }
  }

  @SuppressWarnings("unused")
  public static Color colorByRGB(
      @Param("r") double r,
      @Param("g") double g,
      @Param("b") double b
  ) {
    return new Color(
        (float) r, (float) g, (float) b
    );
  }

  @SuppressWarnings("unused")
  public static <K, V> Map.Entry<K, V> entry(
      @Param("key") K key,
      @Param("value") V value
  ) {
    return Map.entry(key, value);
  }

  @SuppressWarnings("unused")
  public static <K, V> Map<K, V> map(
      @Param("entries") List<Map.Entry<K, V>> entries
  ) {
    Map<K, V> map = new LinkedHashMap<>();
    entries.forEach(e -> map.put(e.getKey(), e.getValue()));
    return Collections.unmodifiableMap(map);
  }
}
