/*
 * Copyright 2023 eric
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

package io.github.ericmedvet.jgea.core.util;

import com.google.common.collect.Range;
import io.github.ericmedvet.jgea.core.distance.Distance;

import java.io.File;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.random.RandomGenerator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author eric
 */
public class Misc {

  private static final Logger L = Logger.getLogger(Misc.class.getName());

  private Misc() {
    /* prevent instantiation */
  }

  public static <T, R> CachedFunction<T, R> cached(Function<T, R> function, long size) {
    return new CachedFunction<>(function, size);
  }

  public static <T, U, R> CachedBiFunction<T, U, R> cached(BiFunction<T, U, R> function, long size) {
    return new CachedBiFunction<>(function, size);
  }

  public static <T> Distance<T> cached(Distance<T> function, long size) {
    final CachedBiFunction<T, T, Double> cached = new CachedBiFunction<>(function, size);
    return cached::apply;
  }

  public static <K> List<K> concat(List<List<? extends K>> lists) {
    return lists.stream().flatMap(List::stream).collect(Collectors.toList());
  }

  public static <T> T first(Collection<T> ts) {
    if (ts.isEmpty()) {
      return null;
    }
    return ts.iterator().next();
  }

  public static <V> Map<String, V> keyPrefix(String prefix, Map<String, V> original) {
    Map<String, V> modified = new LinkedHashMap<>();
    for (Map.Entry<String, V> entry : original.entrySet()) {
      modified.put(prefix + entry.getKey(), entry.getValue());
    }
    return modified;
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

  @SafeVarargs
  public static <K, V> Map<K, V> merge(Map<K, V>... maps) {
    Map<K, V> map = new LinkedHashMap<>();
    for (Map<K, V> currentMap : maps) {
      map.putAll(currentMap);
    }
    return map;
  }

  public static <T> T pickRandomly(Map<T, Double> options, RandomGenerator random) {
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
    return first(options.keySet());
  }

  @SuppressWarnings("unchecked")
  public static <T> T pickRandomly(Collection<T> ts, RandomGenerator random) {
    return (T) ts.toArray()[random.nextInt(ts.size())];
  }

  public static <K> List<K> shuffle(List<K> list, RandomGenerator random) {
    List<Integer> indexes = new ArrayList<>(IntStream.range(0, list.size()).boxed().toList());
    List<Integer> shuffledIndexes = new ArrayList<>(indexes.size());
    while (!indexes.isEmpty()) {
      int indexOfIndex = indexes.size() == 1 ? 0 : random.nextInt(indexes.size());
      shuffledIndexes.add(indexes.get(indexOfIndex));
      indexes.remove(indexOfIndex);
    }
    return shuffledIndexes.stream().map(list::get).toList();
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
    for (int j : rangeSize) {
      ranges.add(Range.closedOpen(offset, offset + j));
      offset = offset + j;
    }
    return ranges;
  }

  public static File checkExistenceAndChangeName(File file) {
    String originalFileName = file.getPath();
    while (file.exists()) {
      String newName = null;
      Matcher mNum = Pattern.compile("\\((?<n>[0-9]+)\\)\\.\\w+$").matcher(file.getPath());
      if (mNum.find()) {
        int n = Integer.parseInt(mNum.group("n"));
        newName = new StringBuilder(file.getPath()).replace(mNum.start("n"), mNum.end("n"), Integer.toString(n + 1))
            .toString();
      }
      Matcher mExtension = Pattern.compile("\\.\\w+$").matcher(file.getPath());
      if (newName == null && mExtension.find()) {
        newName = new StringBuilder(file.getPath()).replace(
            mExtension.start(),
            mExtension.end(),
            ".(1)" + mExtension.group()
        ).toString();
      }
      if (newName == null) {
        newName = file.getPath() + ".newer";
      }
      file = new File(newName);
    }
    if (!file.getPath().equals(originalFileName)) {
      L.log(
          Level.WARNING,
          String.format("Given file name (%s) exists; will write on %s", originalFileName, file.getPath())
      );
    }
    return file;
  }

}
