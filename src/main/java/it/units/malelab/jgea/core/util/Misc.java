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

package it.units.malelab.jgea.core.util;

import com.google.common.collect.Range;
import it.units.malelab.jgea.distance.Distance;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

/**
 * @author eric
 */
public class Misc {

  private static final String BARPLOT_VALS = "▁▂▃▄▅▆▇";

  private Misc() {
    /* prevent instantiation */
  }

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

  public static <T, R> CachedFunction<T, R> cached(Function<T, R> function, long size) {
    return new CachedFunction<>(function, size);
  }

  public static <T, U, R> CachedBiFunction<T, U, R> cached(BiFunction<T, U, R> function, long size) {
    return new CachedBiFunction<>(function, size);
  }

  public static <T> Distance<T> cached(Distance<T> function, long size) {
    final CachedBiFunction<T, T, Double> cached = new CachedBiFunction<>(function, size);
    return (t1, t2) -> cached.apply(t1, t2);
  }

  public static double median(double... values) {
    if (values.length == 1) {
      return values[0];
    }
    double[] vs = Arrays.copyOf(values, values.length);
    Arrays.sort(vs);
    if (vs.length % 2 == 0) {
      return (vs[values.length / 2 - 1] + vs[values.length / 2]) / 2d;
    }
    return vs[values.length / 2];
  }

  public static <K> K median(Collection<K> ks, Comparator<? super K> comparator) {
    List<K> all = new ArrayList<>(ks);
    all.sort(comparator);
    return all.get(all.size() / 2);
  }

  private static double[] resize(double[] values, int l) {
    double[] resized = new double[l];
    for (int i = 0; i < l; i++) {
      resized[i] = values[Math.min((int) Math.round((double) i / (double) l * (double) values.length), values.length - 1)];
    }
    return resized;
  }

  private static String barplot(double[] values, double min, double max) {
    StringBuilder sb = new StringBuilder();
    for (double value : values) {
      sb.append(BARPLOT_VALS.charAt((int) Math.round(Math.max(Math.min((value - min) / (max - min), 1d), 0d) * ((double) BARPLOT_VALS.length() - 1d))));
    }
    return sb.toString();
  }

  public static String barplot(double[] values) {
    double min = DoubleStream.of(values).min().orElse(0);
    double max = DoubleStream.of(values).max().orElse(0);
    return barplot(values, min, max);
  }

  public static String barplot(List<? extends Number> values) {
    return barplot(values.stream().mapToDouble(Number::doubleValue).toArray());
  }

  public static String barplot(List<? extends Number> values, int l) {
    return barplot(resize(values.stream().mapToDouble(Number::doubleValue).toArray(), l));
  }

  public static String histogram(List<? extends Number> values, int bins) {
    double[] vs = values.stream().mapToDouble(Number::doubleValue).toArray();
    double min = DoubleStream.of(vs).min().orElse(0);
    double max = DoubleStream.of(vs).max().orElse(0);
    double[] counts = new double[bins];
    for (double v : vs) {
      int i = Math.min((int) Math.floor((v - min) / (max - min) * (double) bins), bins - 1);
      counts[i] = counts[i] + 1;
    }
    return barplot(counts, 0, vs.length);
  }

}
