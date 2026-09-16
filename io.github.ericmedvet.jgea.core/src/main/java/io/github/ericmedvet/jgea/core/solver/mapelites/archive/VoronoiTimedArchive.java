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
import io.github.ericmedvet.jgea.core.util.Misc;
import io.github.ericmedvet.jnb.datastructure.DoubleRange;
import io.github.ericmedvet.jviz.core.geometry.GeometryUtils;
import io.github.ericmedvet.jviz.core.geometry.Point;
import io.github.ericmedvet.jviz.core.geometry.Polygon;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.random.RandomGenerator;

public class VoronoiTimedArchive<V, C> extends AbstractTimedDynamicHasherArchive<SpatialHash, List<Double>, Point, V, C> implements TwoDKeyArchive<V, C> {

  private final DoubleRange xRange;
  private final DoubleRange yRange;

  public VoronoiTimedArchive(
      DoubleRange xRange,
      DoubleRange yRange,
      int nOfPoints,
      int step,
      RandomGenerator randomGenerator,
      Function<V, C> contentInitializer,
      BiFunction<C, V, C> contentUpdater
  ) {
    this.xRange = xRange;
    this.yRange = yRange;
    // create points
    Set<Point> centroids = new LinkedHashSet<>(nOfPoints);
    while (centroids.size() < nOfPoints) {
      centroids.add(
          new Point(
              xRange.denormalize(randomGenerator.nextDouble()),
              yRange.denormalize(randomGenerator.nextDouble())
          )
      );
    }
    super(
        contentUpdater,
        contentInitializer,
        VoronoiTimedArchive::hash,
        VoronoiTimedArchive::deHash,
        () -> new SpatialHash(centroids),
        (spatialHash, newPoint, _, _, _, _) -> addPoint(spatialHash, newPoint),
        step
    );
  }

  private static Point hash(List<Double> key, SpatialHash spatialHash) {
    return spatialHash.closestTo(new Point(key.getFirst(), key.getLast()));
  }

  private static List<Double> deHash(Point point, SpatialHash spatialHash) {
    return List.of(point.x(), point.y());
  }

  private static SpatialHash addPoint(SpatialHash spatialHash, List<Double> newPoint) {
    return new SpatialHash(
        Misc.union(spatialHash.keyPoints(), Set.of(new Point(newPoint.getFirst(), newPoint.getLast())))
    );
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
        state().keyPoints(),
        xRange,
        yRange
    );
    Map<Polygon, C> map = new HashMap<>(tessellation.size());
    tessellation.forEach((c, p) -> map.put(p, get(c.x(), c.y()).orElse(null)));
    return map;
  }

  @Override
  public int capacity() {
    return state().keyPoints().size();
  }
}
