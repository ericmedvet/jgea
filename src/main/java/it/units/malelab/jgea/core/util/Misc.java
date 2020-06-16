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

package it.units.malelab.jgea.core.util;

import com.google.common.collect.Range;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
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

  public static <K, V> Map<K, V> merge(Map<K, V>... maps) {
    Map<K, V> map = new LinkedHashMap<>();
    for (Map<K, V> currentMap : maps) {
      map.putAll(currentMap);
    }
    return map;
  }

  public static <T> T pickRandomly(Map<T, Double> options, Random random) {
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

  public static List<Range<Integer>> slices(Range<Integer> range, int pieces) {
    List<Integer> sizes = new ArrayList<>(pieces);
    for (int i = 0; i < pieces; i++) {
      sizes.add(1);
    }
    return slices(range, sizes);
  }

  public static List<Range<Integer>> slices(Range<Integer> range, List<Integer> sizes) {
    int length = range.upperEndpoint() - range.lowerEndpoint();
    int sumOfSizes = 0;
    for (int size : sizes) {
      sumOfSizes = sumOfSizes + size;
    }
    if (sumOfSizes > length) {
      List<Integer> originalSizes = new ArrayList<>(sizes);
      sizes = new ArrayList<>(sizes.size());
      int oldSumOfSizes = sumOfSizes;
      sumOfSizes = 0;
      for (int originalSize : originalSizes) {
        int newSize = (int) Math.round((double) originalSize / (double) oldSumOfSizes);
        sizes.add(newSize);
        sumOfSizes = sumOfSizes + newSize;
      }
    }
    int minSize = (int) Math.floor((double) length / (double) sumOfSizes);
    int missing = length - minSize * sumOfSizes;
    int[] rangeSize = new int[sizes.size()];
    for (int i = 0; i < rangeSize.length; i++) {
      rangeSize[i] = minSize * sizes.get(i);
    }
    int c = 0;
    while (missing > 0) {
      rangeSize[c % rangeSize.length] = rangeSize[c % rangeSize.length] + 1;
      c = c + 1;
      missing = missing - 1;
    }
    List<Range<Integer>> ranges = new ArrayList<>(sizes.size());
    int offset = range.lowerEndpoint();
    for (int i = 0; i < rangeSize.length; i++) {
      ranges.add(Range.closedOpen(offset, offset + rangeSize[i]));
      offset = offset + rangeSize[i];
    }
    return ranges;
  }

  public static <T> T pickRandomly(Collection<T> ts, Random random) {
    return (T) ts.toArray()[random.nextInt(ts.size())];
  }

  public static <T> T first(Collection<T> ts) {
    if (ts.isEmpty()) {
      return null;
    }
    return ts.iterator().next();
  }

}
