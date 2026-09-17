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
import io.github.ericmedvet.jnb.datastructure.Tree;
import io.github.ericmedvet.jviz.core.geometry.Point;
import io.github.ericmedvet.jviz.core.geometry.Polygon;
import io.github.ericmedvet.jviz.core.geometry.Rectangle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DynamicBinaryTreeArchive<V, C> extends AbstractBestReplacerArchive<List<Double>, Point, V, C> implements TwoDKeyArchive<V, C> {

  private final DoubleRange xRange;
  private final DoubleRange yRange;
  private final int maxNOfPoints;
  private Tree<Object> tree;

  public DynamicBinaryTreeArchive(
      Function<V, C> contentInitializer,
      BiFunction<C, V, C> contentUpdater,
      BiPredicate<? super V, ? super C> isBetterThan,
      DoubleRange xRange,
      DoubleRange yRange,
      int maxNOfPoints
  ) {
    super(contentInitializer, contentUpdater, isBetterThan);
    this.xRange = xRange;
    this.yRange = yRange;
    this.maxNOfPoints = maxNOfPoints;
    tree = new Tree<>(new Point(xRange.center(), yRange.center()));
  }

  private static Stream<Integer> lineageFor(Point point, Tree<Object> tree) {
    if (tree.isLeaf()) {
      return Stream.of();
    }
    Condition condition = (Condition) tree.label();
    double value = switch (condition.variable) {
      case X -> point.x();
      case Y -> point.y();
    };
    if (value <= condition.threshold) {
      return Stream.concat(Stream.of(0), lineageFor(point, tree.child(0)));
    }
    return Stream.concat(Stream.of(1), lineageFor(point, tree.child(1)));
  }

  private static Point pointFor(Point point, Tree<Object> tree) {
    if (tree.isLeaf()) {
      return (Point) tree.label();
    }
    Condition condition = (Condition) tree.label();
    double value = switch (condition.variable) {
      case X -> point.x();
      case Y -> point.y();
    };
    if (value <= condition.threshold) {
      return pointFor(point, tree.child(0));
    }
    return pointFor(point, tree.child(1));
  }

  private static Rectangle rectangleFor(
      Point point,
      Tree<Object> tree,
      DoubleRange xRange,
      DoubleRange yRange
  ) {
    if (tree.isLeaf()) {
      return Rectangle.of(
          new Point(xRange.min(), yRange.min()),
          new Point(xRange.max(), yRange.max())
      );
    }
    Condition condition = (Condition) tree.label();
    if (condition.variable.equals(Variable.X)) {
      if (point.x() <= condition.threshold) {
        return rectangleFor(
            point,
            tree.child(0),
            new DoubleRange(xRange.min(), condition.threshold),
            yRange
        );
      }
      return rectangleFor(
          point,
          tree.child(1),
          new DoubleRange(condition.threshold, xRange.max()),
          yRange
      );
    }
    if (point.y() <= condition.threshold) {
      return rectangleFor(
          point,
          tree.child(0),
          xRange,
          new DoubleRange(yRange.min(), condition.threshold)
      );
    }
    return rectangleFor(
        point,
        tree.child(1),
        xRange,
        new DoubleRange(condition.threshold, yRange.max())
    );
  }

  @Override
  public int capacity() {
    return tree.leafLabels().size();
  }

  @Override
  public List<Double> deHash(Point hash) {
    return List.of(hash.x(), hash.y());
  }

  @Override
  public Optional<C> get(double x, double y) {
    return get(List.of(x, y));
  }

  @Override
  public Point hash(List<Double> key) {
    return pointFor(new Point(key.getFirst(), key.getLast()), tree);
  }

  @Override
  public Map<Polygon, C> localizedContents() {
    return map.entrySet()
        .stream()
        .collect(
            Collectors.toMap(
                e -> rectangleFor(e.getKey(), tree, xRange, yRange),
                Entry::getValue
            )
        );
  }

  @Override
  public void put(double x, double y, V value) {
    put(List.of(x, y), value);
  }

  @Override
  protected boolean updateHashingState(List<Double> key, Point existingPoint, V value) {
    Point newPoint = new Point(key.getFirst(), key.getLast());
    List<Integer> toSplitLineage = lineageFor(newPoint, tree).toList();
    double xRelGap = Math.abs(newPoint.x() - existingPoint.x()) / xRange.extent();
    double yRelGap = Math.abs(newPoint.y() - existingPoint.y()) / yRange.extent();
    if (xRelGap == 0 && yRelGap == 0) {
      return false;
    }
    Condition condition;
    Point lessPoint;
    Point morePoint;
    if (xRelGap > yRelGap) {
      condition = new Condition(Variable.X, (newPoint.x() + existingPoint.x()) / 2d);
      if (newPoint.x() < existingPoint.x()) {
        lessPoint = newPoint;
        morePoint = existingPoint;
      } else {
        lessPoint = existingPoint;
        morePoint = newPoint;
      }
    } else {
      condition = new Condition(Variable.Y, (newPoint.y() + existingPoint.y()) / 2d);
      if (newPoint.y() < existingPoint.y()) {
        lessPoint = newPoint;
        morePoint = existingPoint;
      } else {
        lessPoint = existingPoint;
        morePoint = newPoint;
      }
    }
    tree = tree.withAt(
        new Tree<>(condition, List.of(new Tree<>(lessPoint), new Tree<>(morePoint))),
        toSplitLineage
    );
    while (tree.leafLabels().size() > maxNOfPoints) {
      Point toRemoveHash = map.keySet()
          .stream()
          .min(
              Comparator.comparingLong(
                  h -> ageMap.getOrDefault(
                      h,
                      0L
                  )
              )
          )
          .orElseThrow();
      List<Integer> oldestLineage = lineageFor(toRemoveHash, tree).toList();
      List<Integer> toMergeLineage = oldestLineage.subList(0, oldestLineage.size() - 1);
      List<Integer> toKeepLineage = new ArrayList<>(oldestLineage);
      toKeepLineage.set(toKeepLineage.size() - 1, 1 - toKeepLineage.getLast());
      ageMap.remove(toRemoveHash);
      map.remove(toRemoveHash);
      tree = tree.withAt(
          tree.descendant(toKeepLineage),
          toMergeLineage
      );
    }
    bestHashes.clear();
    bestHashes.add(newPoint);
    return true;
  }

  private enum Variable { X, Y }

  private record Condition(Variable variable, double threshold) {

    @Override
    public String toString() {
      return "%s<%.3f".formatted(variable.name().toLowerCase(), threshold);
    }
  }
}