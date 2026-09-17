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

import io.github.ericmedvet.jgea.core.solver.mapelites.archive.VoronoiArchive.SpatialHash;
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
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.random.RandomGenerator;

public class DynamicVoronoiArchive<V, C> extends AbstractBestReplacerArchive<List<Double>, Point, V, C> implements TwoDKeyArchive<V, C> {

  private final DoubleRange xRange;
  private final DoubleRange yRange;
  private final int maxNOfPoints;
  private SpatialHash spatialHash;

  public DynamicVoronoiArchive(
      DoubleRange xRange,
      DoubleRange yRange,
      int initialNOfPoints,
      int maxNOfPoints,
      RandomGenerator randomGenerator,
      Function<V, C> contentInitializer,
      BiFunction<C, V, C> contentUpdater,
      BiPredicate<? super V, ? super C> isBetterThan
  ) {
    this.xRange = xRange;
    this.yRange = yRange;
    this.maxNOfPoints = maxNOfPoints;
    // create points
    Set<Point> centroids = new LinkedHashSet<>(initialNOfPoints);
    while (centroids.size() < initialNOfPoints) {
      centroids.add(
          new Point(
              xRange.denormalize(randomGenerator.nextDouble()),
              yRange.denormalize(randomGenerator.nextDouble())
          )
      );
    }
    spatialHash = new SpatialHash(centroids);
    super(
        contentInitializer,
        contentUpdater,
        isBetterThan
    );
  }

  @Override
  public int capacity() {
    return spatialHash.keyPoints().size();
  }

  @Override
  public List<Double> deHash(Point point) {
    return List.of(point.x(), point.y());
  }

  @Override
  public Point hash(List<Double> key) {
    return spatialHash.closestTo(new Point(key.getFirst(), key.getLast()));
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
    Map<Point, Polygon> tessellation = GeometryUtils.voronoiTessellation(
        spatialHash.keyPoints(),
        xRange,
        yRange
    );
    Map<Polygon, C> map = new HashMap<>(tessellation.size());
    tessellation.forEach((c, p) -> map.put(p, get(c.x(), c.y()).orElse(null)));
    return map;
  }

  @Override
  protected boolean updateHashingState(List<Double> key, Point point, V value) {
    Point newPoint = new Point(key.getFirst(), key.getLast());
    if (!spatialHash.keyPoints().contains(newPoint)) {
      Set<Point> newHashes = new LinkedHashSet<>(spatialHash.keyPoints());
      newHashes.add(newPoint);
      while (newHashes.size() > maxNOfPoints) {
        Point toRemoveHash = newHashes.stream()
            .min(
                Comparator.comparingLong(
                    h -> ageMap.getOrDefault(
                        h,
                        0L
                    )
                )
            )
            .orElseThrow();
        newHashes.remove(toRemoveHash);
        ageMap.remove(toRemoveHash);
        map.remove(toRemoveHash);
      }
      spatialHash = new SpatialHash(newHashes);
      bestHashes.clear();
      bestHashes.add(newPoint);
      return true;
    }
    return false;
  }
}