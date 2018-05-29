/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core.listener.collector;

import it.units.malelab.jgea.core.function.Function;
import it.units.malelab.jgea.core.listener.Listener;
import it.units.malelab.jgea.core.util.WithNames;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author eric
 */
public class Item<T> {
  
  private final String name;
  private final T value;
  private final String format;

  public Item(String name, T value, String format) {
    this.name = name;
    this.value = value;
    this.format = format;
  }

  public String getName() {
    return name;
  }

  public T getValue() {
    return value;
  }

  public String getFormat() {
    return format;
  }
  
  public Item<T> prefixed(String prefix) {
    return new Item<>(name.isEmpty()?prefix:(prefix+"."+name), value, format);
  }
  
  public static <F extends List> Function<F, List<Item>> fromMultiobjective(Function<?, F> function, String... formats) {
    return (F f, Listener listener) -> {
      List<Item> items = new ArrayList<>();
      for (int i = 0; i<f.size(); i++) {
        String name1 = "f"+i;
        if (function instanceof WithNames) {
          name1 = ((WithNames)function).names().get(i);
        }
        String format1 = formats[i % formats.length];
        items.add(new Item(name1, f.get(i), format1));
      }
      return items;
    };
  }
  
}
