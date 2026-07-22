/*-
 * ========================LICENSE_START=================================
 * jgea-core
 * %%
 * Copyright (C) 2018 - 2026 Eric Medvet
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

package io.github.ericmedvet.jgea.core.util;

import io.github.ericmedvet.jnb.datastructure.Utils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.SequencedMap;
import java.util.Set;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.random.RandomGenerator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Misc {

  private static final Logger L = Logger.getLogger(Misc.class.getName());

  private Misc() {
  }

  public static <T> T pickRandomly(Collection<T> ts, RandomGenerator random) {
    if (ts.isEmpty()) {
      throw new IllegalArgumentException("Empty collection");
    }
    if (ts instanceof List<T> list) {
      return list.get(random.nextInt(ts.size()));
    }
    int chosenI = random.nextInt(ts.size());
    int i = 0;
    for (T t : ts) {
      if (i == chosenI) {
        return t;
      }
      i = i + 1;
    }
    throw new IllegalArgumentException(
        "Unexpectedly unable to pick a random element (chosen index=%d, size=%d)".formatted(
            chosenI,
            ts.size()
        )
    );
  }

  private static double area(Point a, Point b, Point c) {
    return 0.5 * Math.abs(a.x * (b.y - c.y) + b.x * (c.y - a.y) + c.x * (a.y - b.y));
  }

  public static <T> List<List<T>> cartesian(List<List<T>> tss) {
    return cartesian(tss, List.of(List.of()));
  }

  private static <T> List<List<T>> cartesian(List<List<T>> tss, List<List<T>> lists) {
    if (tss.size() == 1) {
      return tss.getFirst()
          .stream()
          .map(
              t -> lists.stream()
                  .map(l -> Stream.concat(l.stream(), Stream.of(t)).toList())
                  .toList()
          )
          .flatMap(List::stream)
          .toList();
    }
    return cartesian(tss.subList(1, tss.size()), cartesian(tss.subList(0, 1), lists));
  }

  public static <T> T first(Collection<T> ts) {
    if (ts.isEmpty()) {
      return null;
    }
    return ts.iterator().next();
  }

  public static double hypervolume2D(Collection<List<Double>> points, List<Double> reference) {
    Point min = new Point(reference.get(0), reference.get(1));
    List<Point> ps = points.stream()
        .map(vs -> new Point(vs.getFirst(), vs.get(1)))
        .sorted(Comparator.comparingDouble(Point::x))
        .toList();
    return IntStream.range(1, ps.size())
        .mapToDouble(i -> area(min, ps.get(i - 1), ps.get(i)))
        .sum();
  }

  public static double hypervolume2D(
      Collection<List<Double>> points,
      List<Double> minReference,
      List<Double> maxReference
  ) {
    return hypervolume2D(
        Stream.concat(
            Stream.of(
                List.of(minReference.get(0), maxReference.get(1)),
                List.of(minReference.get(1), maxReference.get(0))
            ),
            points.stream()
        )
            .toList(),
        maxReference
    );
  }

  public static <T> Set<T> intersection(Set<T> set1, Set<T> set2) {
    return union(set1, set2).stream()
        .filter(t -> set1.contains(t) && set2.contains(t))
        .collect(Collectors.toCollection(LinkedHashSet::new));
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

  public static <K, V> Map<K, Set<V>> merge(Collection<? extends Map<K, V>> maps) {
    return maps.stream()
        .map(Map::entrySet)
        .flatMap(Set::stream)
        .map(e -> Map.entry(e.getKey(), Set.of(e.getValue())))
        .collect(
            Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                Misc::union
            )
        );
  }

  public static <K> K percentile(Collection<K> ks, Comparator<? super K> comparator, double p) {
    List<K> collection = ks.stream().sorted(comparator).toList();
    int i = (int) Math.clamp(((double) collection.size()) * p, 0, collection.size() - 1);
    return collection.get(i);
  }

  public static <T> T pickRandomly(Map<T, Double> options, RandomGenerator random) {
    double sum = options.values().stream().mapToDouble(v -> v).sum();
    double d = random.nextDouble() * sum;
    for (Map.Entry<T, Double> option : options.entrySet()) {
      if (d < option.getValue()) {
        return option.getKey();
      }
      d = d - option.getValue();
    }
    return first(options.keySet());
  }

  public static <K, V, U> Map<K, U> transformValues(
      Map<K, V> map,
      Function<? super V, ? extends U> transformer
  ) {
    return map.entrySet()
        .stream()
        .collect(
            Collectors.toMap(
                Map.Entry::getKey,
                e -> transformer.apply(e.getValue())
            )
        );
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

  public static List<IntRange> slices(IntRange range, int pieces) {
    return slices(range, Collections.nCopies(pieces, 1));
  }

  public static List<IntRange> slices(IntRange range, List<Integer> sizes) {
    int length = range.extent();
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
    List<IntRange> ranges = new ArrayList<>(sizes.size());
    int offset = range.min();
    for (int j : rangeSize) {
      ranges.add(new IntRange(offset, offset + j));
      offset = offset + j;
    }
    return ranges;
  }

  private record Point(double x, double y) {

  }

  public static <K, V, U> SequencedMap<K, U> sequencedTransformValues(
      SequencedMap<K, V> map,
      Function<? super V, ? extends U> transformer
  ) {
    return map.entrySet()
        .stream()
        .collect(
            Utils.toSequencedMap(
                Map.Entry::getKey,
                e -> transformer.apply(e.getValue())
            )
        );
  }

  public static <T> Set<T> union(Set<T> set1, Set<T> set2) {
    return Stream.of(set1, set2).flatMap(Set::stream).collect(Collectors.toSet());
  }

}