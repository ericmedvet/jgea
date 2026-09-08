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
package io.github.ericmedvet.jgea.core.solver.mapelites.archive;

import io.github.ericmedvet.jnb.datastructure.DoubleRange;
import io.github.ericmedvet.jviz.core.geometry.GeometryUtils;
import io.github.ericmedvet.jviz.core.geometry.Point;
import io.github.ericmedvet.jviz.core.geometry.Polygon;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.random.RandomGenerator;
import java.util.stream.Collectors;

public class VoronoiArchive<V, C> extends AbstractHasherArchive<List<Double>, List<Double>, V, C> implements TwoDKeyArchive<V, C> {

  private final int nOfPoints;

  private final Set<Point> centroids;
  private final Map<Key, Set<Point>> spatialHash;
  private final double cellW;
  private final double cellH;
  private final Map<Point, Polygon> tessellation;

  private record Key(int x, int y) {

    public static Key from(Point point, double cellW, double cellH) {
      return new Key((int) Math.floor(point.x() / cellW), (int) Math.floor(point.y() / cellH));
    }

    public Set<Key> neighbors() {
      return Set.of(
          new Key(x - 1, y - 1),
          new Key(x - 1, y),
          new Key(x - 1, y + 1),
          new Key(x, y - 1),
          new Key(x, y),
          new Key(x, y + 1),
          new Key(x + 1, y - 1),
          new Key(x + 1, y),
          new Key(x + 1, y + 1)
      );
    }
  }

  public VoronoiArchive(
      DoubleRange xRange,
      DoubleRange yRange,
      int nOfPoints,
      RandomGenerator randomGenerator,
      Function<V, C> contentInitializer,
      BiFunction<C, V, C> contentUpdater
  ) {
    super(contentInitializer, contentUpdater);
    this.nOfPoints = nOfPoints;
    // create points
    centroids = new LinkedHashSet<>(nOfPoints);
    while (centroids.size() < nOfPoints) {
      centroids.add(
          new Point(
              xRange.denormalize(randomGenerator.nextDouble()),
              yRange.denormalize(randomGenerator.nextDouble())
          )
      );
    }
    tessellation = GeometryUtils.voronoiTessellation(centroids, xRange, yRange);
    // fill spatial hash
    cellW = xRange.extent() / Math.sqrt(nOfPoints);
    cellH = yRange.extent() / Math.sqrt(nOfPoints);
    spatialHash = centroids.stream()
        .collect(
            Collectors.groupingBy(
                p -> Key.from(p, cellW, cellH),
                Collectors.toSet()
            )
        );
  }

  private Point closestCentroid(Point point) {
    Set<Key> keys = Key.from(point, cellW, cellH).neighbors();
    while (true) {
      Set<Point> closeCentroids = keys.stream()
          .flatMap(k -> spatialHash.getOrDefault(k, Set.of()).stream())
          .collect(Collectors.toSet());
      if (closeCentroids.isEmpty()) {
        keys = keys.stream().flatMap(k -> k.neighbors().stream()).collect(Collectors.toSet());
      } else {
        return closeCentroids.stream()
            .min(Comparator.comparingDouble(c -> c.distanceTo(point)))
            .orElseThrow();
      }
    }
  }

  @Override
  public List<Double> hash(List<Double> key) {
    Point centroid = closestCentroid(new Point(key.getFirst(), key.getLast()));
    return List.of(centroid.x(), centroid.y());
  }

  @Override
  public List<Double> deHash(List<Double> hash) {
    return hash;
  }

  @Override
  public void put(double x, double y, V value) {
    put(List.of(x, y), value);
  }

  @Override
  public Optional<C> get(double x, double y) {
    return get(List.of(x, y));
  }

  @Override
  public Map<Polygon, C> localizedContents() {
    Map<Polygon, C> map = new HashMap<>(tessellation.size());
    tessellation.forEach((c, p) -> map.put(p, get(c.x(), c.y()).orElse(null)));
    return map;
  }

  @Override
  public int capacity() {
    return nOfPoints;
  }
}