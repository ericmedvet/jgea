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
package io.github.ericmedvet.jgea.problem.control.navigation;

import io.github.ericmedvet.jgea.core.listener.Drawer;
import io.github.ericmedvet.jgea.problem.control.ControlProblem;
import java.awt.*;
import java.awt.geom.*;
import java.util.List;

/**
 * @author "Eric Medvet" on 2024/02/23 for jgea
 */
public class OutcomeDrawer implements Drawer<ControlProblem.Outcome<Navigation.Snapshot, Double>> {
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

  public OutcomeDrawer(Configuration configuration) {
    this.configuration = configuration;
  }

  @Override
  public void draw(Graphics2D g, ControlProblem.Outcome<Navigation.Snapshot, Double> outcome) {
    Navigation.Snapshot snapshot = outcome.behavior().get(outcome.behavior().lastKey());
    drawBackground(g, configuration.backgroundColor);
    Arena arena = snapshot.arena();
    AffineTransform previousTransform = setTransform(g, arena);
    // draw arena
    g.setStroke(new BasicStroke(
        (float) (configuration.segmentThickness / g.getTransform().getScaleX())));
    g.setColor(configuration.segmentColor);
    arena.segments().forEach(s -> g.draw(new Line2D.Double(s.p1().x(), s.p1().y(), s.p2().x(), s.p2().y())));
    // draw robot and trajectory
    drawTrajectory(
        g,
        configuration.robotColor,
        configuration.trajectoryThickness / g.getTransform().getScaleX(),
        outcome.behavior().values().stream()
            .map(Navigation.Snapshot::robotPosition)
            .toList());
    drawRobot(
        g,
        configuration.robotColor,
        configuration.robotThickness / g.getTransform().getScaleX(),
        snapshot.robotPosition(),
        snapshot.robotDirection(),
        snapshot.robotRadius());
    // draw target and trajectory
    drawTrajectory(
        g,
        configuration.targetColor,
        configuration.trajectoryThickness / g.getTransform().getScaleX(),
        outcome.behavior().values().stream()
            .map(Navigation.Snapshot::targetPosition)
            .toList());
    drawTarget(
        g,
        configuration.targetColor,
        configuration.targetThickness / g.getTransform().getScaleX(),
        configuration.targetSize / g.getTransform().getScaleX(),
        snapshot.targetPosition());
    // restore transformation
    g.setTransform(previousTransform);
  }

  private AffineTransform setTransform(Graphics2D g, Arena arena) {
    double cX = g.getClipBounds().x;
    double cY = g.getClipBounds().y;
    double cW = g.getClipBounds().width;
    double cH = g.getClipBounds().getHeight();
    // compute transformation
    double scale = Math.min(
        cW / (1 + 2 * configuration.marginRate) / arena.xExtent(),
        cH / (1 + 2 * configuration.marginRate) / arena.yExtent());
    AffineTransform previousTransform = g.getTransform();
    AffineTransform transform = AffineTransform.getScaleInstance(scale, scale);
    transform.translate(
        (cX / scale + cW / scale - arena.xExtent()) / 2d, (cY / scale + cH / scale - arena.yExtent()) / 2d);
    g.setTransform(transform);
    return previousTransform;
  }

  private static void drawBackground(Graphics2D g, Color c) {
    g.setColor(c);
    g.fill(new Rectangle2D.Double(0, 0, g.getClipBounds().width, g.getClipBounds().height));
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
}
