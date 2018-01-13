/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 *
 * @author eric
 */
public class Misc {

  public static <V> Map<String, V> keyPrefix(String prefix, Map<String, V> original) {
    Map<String, V> modified = new LinkedHashMap<>();
    for (Map.Entry<String, V> entry : original.entrySet()) {
      modified.put(prefix + entry.getKey(), entry.getValue());
    }
    return modified;
  }

  public static <T> List<T> getAll(List<Future<T>> futures) throws InterruptedException, ExecutionException {
    List<T> results = new ArrayList<>();
    for (Future<T> future : futures) {
      results.add(future.get());
    }
    return results;
  }

  public static <K, V> Map<K, V> merge(Map<K, V>... maps) {
    Map<K, V> map = new LinkedHashMap<>();
    for (Map<K, V> currentMap : maps) {
      map.putAll(currentMap);
    }
    return map;
  }

  public static <T> T selectRandom(Map<T, Double> options, Random random) {
    double sum = 0;
    for (Double rate : options.values()) {
      sum = sum + rate;
    }
    double d = random.nextDouble() * sum;
    for (Map.Entry<T, Double> option : options.entrySet()) {
      if (d < option.getValue()) {
        return option.getKey();
      }
      d = d - option.getValue();
    }
    return (T) options.keySet().toArray()[0];
  }

}
