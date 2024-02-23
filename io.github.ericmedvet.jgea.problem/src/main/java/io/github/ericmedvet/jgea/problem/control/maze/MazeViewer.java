/*-
 * ========================LICENSE_START=================================
 * jgea-problem
 * %%
 * Copyright (C) 2018 - 2024 Eric Medvet
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
package io.github.ericmedvet.jgea.problem.control.maze;

import io.github.ericmedvet.jgea.problem.control.OutcomeViewer;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.SortedMap;

public class MazeViewer implements OutcomeViewer<MazeNavigation.Snapshot> {
  private final Configuration configuration;

  public record Configuration(
      Color backgroundColor,
      Color robotColor,
      Color targetColor,
      Color segmentColor,
      Color infoColor,
      double robotThickness,
      double targetThickness,
      double segmentThickness,
      double trajectoryThickness,
      double targetSize,
      double marginRate) {
    public static final Configuration DEFAULT = new Configuration(
        Color.WHITE, Color.MAGENTA, Color.RED, Color.DARK_GRAY, Color.BLUE, 2, 2, 3, 1, 5, 0.01);
  }

  public MazeViewer(Configuration configuration) {
    this.configuration = configuration;
  }

  private static void drawTrajectory(Graphics2D g, Color c, double th, List<Point> points) {
    g.setStroke(new BasicStroke((float) th));
    g.setColor(c);
    Path2D path = new Path2D.Double();
    path.moveTo(points.get(0).x(), points.get(0).y());
    points.forEach(p -> path.lineTo(p.x(), p.y()));
    g.draw(path);
  }

  private static void drawRobot(Graphics2D g, Color c, double th, Point p, double a, double r) {
    g.setStroke(new BasicStroke((float) th));
    Shape shape = new Ellipse2D.Double(p.x() - r, p.y() - r, 2d * r, 2d * r);
    g.setColor(c);
    g.draw(shape);
    Point hP = p.sum(new Point(a).scale(r));
    g.draw(new Line2D.Double(p.x(), p.y(), hP.x(), hP.y()));
  }

  private static void drawTarget(Graphics2D g, Color c, double th, double l, Point p) {
    g.setStroke(new BasicStroke((float) th));
    g.setColor(c);
    g.draw(new Line2D.Double(p.x() - l / 2d, p.y(), p.x() + l / 2d, p.y()));
    g.draw(new Line2D.Double(p.x(), p.y() - l / 2d, p.x(), p.y() + l / 2d));
  }

  @Override
  public Graphics2D prepare(BufferedImage image) {
    Graphics2D g = image.createGraphics();
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    // draw info
    // draw background
    g.setColor(configuration.backgroundColor);
    g.fill(new Rectangle2D.Double(0, 0, image.getWidth(), image.getHeight()));
    return g;
  }

  @Override
  public void drawOne(Graphics2D g, int w, int h, double t, MazeNavigation.Snapshot snapshot) {
    // compute transformation
    Arena arena = snapshot.arena();
    double scale = Math.min(
        w / (1 + 2 * configuration.marginRate) / arena.xExtent(),
        h / (1 + 2 * configuration.marginRate) / arena.yExtent());
    AffineTransform previousTransform = g.getTransform();
    AffineTransform transform = AffineTransform.getScaleInstance(scale, scale);
    transform.translate((w / scale - arena.xExtent()) / 2d, (h / scale - arena.yExtent()) / 2d);
    g.setTransform(transform);
    // draw arena
    g.setStroke(new BasicStroke((float) (configuration.segmentThickness / scale)));
    g.setColor(configuration.segmentColor);
    arena.segments().forEach(s -> g.draw(new Line2D.Double(s.p1().x(), s.p1().y(), s.p2().x(), s.p2().y())));
    drawRobot(
        g,
        configuration.robotColor,
        configuration.robotThickness / scale,
        snapshot.robotPosition(),
        snapshot.robotDirection(),
        snapshot.robotRadius());
    // draw target and trajectory
    drawTarget(
        g,
        configuration.targetColor,
        configuration.targetThickness / scale,
        configuration.targetSize / scale,
        snapshot.targetPosition());
    // restore transformation
    g.setTransform(previousTransform);
  }

  @Override
  public void drawHistory(Graphics2D g, int w, int h, SortedMap<Double, MazeNavigation.Snapshot> snapshots) {
    // compute transformation
    Arena arena = snapshots.values().iterator().next().arena();
    double scale = Math.min(
        w / (1 + 2 * configuration.marginRate) / arena.xExtent(),
        h / (1 + 2 * configuration.marginRate) / arena.yExtent());
    AffineTransform previousTransform = g.getTransform();
    AffineTransform transform = AffineTransform.getScaleInstance(scale, scale);
    transform.translate((w / scale - arena.xExtent()) / 2d, (h / scale - arena.yExtent()) / 2d);
    g.setTransform(transform);
    // draw robot trajectory
    drawTrajectory(
        g,
        configuration.robotColor,
        configuration.trajectoryThickness / scale,
        snapshots.values().stream()
            .map(MazeNavigation.Snapshot::robotPosition)
            .toList());
    // draw target trajectory
    drawTrajectory(
        g,
        configuration.targetColor,
        configuration.trajectoryThickness / scale,
        snapshots.values().stream()
            .map(MazeNavigation.Snapshot::targetPosition)
            .toList());
    // restore transformation
    g.setTransform(previousTransform);
  }
}
