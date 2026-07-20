/*-
 * ========================LICENSE_START=================================
 * jgea-experimenter
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

package io.github.ericmedvet.jgea.experimenter.drawer;

import io.github.ericmedvet.jgea.core.representation.tree.Tree;
import io.github.ericmedvet.jviz.core.drawer.Drawer;
import io.github.ericmedvet.jviz.core.geometry.Point;
import io.github.ericmedvet.jviz.core.geometry.Rectangle;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class TreeDrawer implements Drawer<Tree<?>> {

  private final static String CONTINUATION_STRING = "…";

  private final Configuration c;

  public TreeDrawer(Configuration configuration) {
    this.c = configuration;
  }

  protected static void drawString(
      Graphics2D g,
      Point p,
      Color color,
      double charH,
      boolean debug,
      String s
  ) {
    g.setFont(new Font("Monospaced", Font.PLAIN, (int) charH));
    List<String> lines = s.lines().toList();
    for (double l = 0; l < lines.size(); l = l + 1) {
      String line = lines.get((int) l);
      Point lineSize = stringSize(g, line);
      Point lineCenter = p.sum(Point.ofY(((lines.size() - 1d) / 2d - l) * charH));
      g.setColor(color);
      g.drawString(
          line,
          (float) (lineCenter.x() - lineSize.x() / 2),
          (float) (lineCenter.y() + g.getFontMetrics().getAscent() / 3d)
      );
      if (debug) {
        drawDebugBox(g, new Rectangle(lineCenter, lineSize.x(), lineSize.y()));
      }
    }
  }

  protected static void drawDebugBox(Graphics2D g, Rectangle r) {
    g.setStroke(new BasicStroke(1));
    g.setColor(Color.RED);
    g.draw(drawable(r));
  }

  protected static Rectangle2D.Double drawable(Rectangle nodeR) {
    return new Rectangle2D.Double(
        nodeR.min().x(),
        nodeR.min().y(),
        nodeR.width(),
        nodeR.height()
    );
  }

  protected static Point stringSize(Graphics2D g, String s) {
    Rectangle2D r = g.getFontMetrics().getStringBounds(s, g);
    return new Point(r.getWidth(), r.getHeight());
  }

  public record Configuration(
      double margin,
      double boxBorderThickness,
      double edgeThickness,
      Color boxBorderColor,
      Color boxBGColor,
      Color labelColor,
      Color edgeColor,
      double charW,
      double charH,
      double boxMargin,
      double nodeMinXGap,
      double nodeMinYGap,
      int maxLabelLength,
      boolean debug
  ) {

    public static final Configuration DEFAULT = new Configuration(
        5,
        1,
        1,
        Color.BLACK,
        Color.LIGHT_GRAY,
        Color.BLUE,
        Color.DARK_GRAY,
        10,
        16,
        2,
        5,
        20,
        7,
        false
    );

    public Configuration scaled(double r) {
      return new Configuration(
          margin * r,
          boxBorderThickness * r,
          edgeThickness * r,
          boxBorderColor,
          boxBGColor,
          labelColor,
          edgeColor,
          charW * r,
          charH * r,
          boxMargin * r,
          nodeMinXGap * r,
          nodeMinYGap * r,
          maxLabelLength,
          debug
      );
    }
  }

  @Override
  public void draw(Graphics2D g, Tree<?> tree) {
    java.awt.Rectangle clipBounds = g.getClipBounds();
    draw(
        g,
        Rectangle.of(
            new Point(clipBounds.getMinX(), clipBounds.getMinY()).sum(
                new Point(c.margin, c.margin)
            ),
            new Point(clipBounds.getMaxX(), clipBounds.getMaxY()).diff(
                new Point(c.margin, c.margin)
            )
        ),
        tree
    );
  }

  private Rectangle draw(Graphics2D g, Rectangle r, Tree<?> t) {
    Point nodeSize = nodeSize(t, this::stringSize);
    //draw children
    double[] centers = new double[t.nChildren()];
    double childrenW = t.childStream().mapToDouble(c -> treeSize(c, this::stringSize).x()).sum() + (t
        .nChildren() - 1) * c.nodeMinXGap;
    Point p = r.min()
        .sum(
            new Point(
                (r.width() - childrenW) / 2d,
                nodeSize.y() + c.nodeMinYGap
            )
        );
    for (int i = 0; i < t.nChildren(); i = i + 1) {
      Point childSize = treeSize(t.child(i), this::stringSize);
      Rectangle childR = Rectangle.of(p, p.sum(childSize));
      p = childR.min().sum(Point.ofX(childR.width() + c.nodeMinXGap));
      Rectangle rootR = draw(g, childR, t.child(i));
      centers[i] = rootR.center().x();
      if (c.debug) {
        drawDebugBox(g, childR);
      }
    }
    //draw node
    Rectangle nodeR = new Rectangle(
        new Point(
            Math.max(
                Arrays.stream(centers).average().orElse(r.center().x()),
                r.min().x() + nodeSize.x() / 2d
            ),
            r.min().y() + nodeSize.y() / 2d
        ),
        nodeSize.x(),
        nodeSize.y()
    );
    //draw box
    Rectangle2D.Double box = drawable(nodeR);
    g.setColor(c.boxBGColor());
    g.fill(box);
    g.setStroke(new BasicStroke((float) c.boxBorderThickness));
    g.setColor(c.boxBorderColor);
    g.draw(box);
    //draw string
    String labelString = t.content().toString();
    if (labelString.length() > c.maxLabelLength) {
      labelString = labelString.substring(0, c.maxLabelLength) + CONTINUATION_STRING;
    }
    drawString(g, nodeR.center(), c.labelColor, c.charH, c.debug, labelString);
    //draw edges
    g.setStroke(new BasicStroke((float) c.edgeThickness));
    g.setColor(c.edgeColor);
    for (double dstX : centers) {
      g.draw(
          new Line2D.Double(
              nodeR.center().x(),
              nodeR.max().y(),
              dstX,
              p.y()
          )
      );
    }
    return nodeR;
  }

  @Override
  public ImageInfo imageInfo(Tree<?> tree) {
    Point treeSize = treeSize(tree, this::stringSize);
    return new ImageInfo(
        (int) Math.ceil(treeSize.x() + 2 * c.margin),
        (int) Math.ceil(treeSize.y() + 2 * c.margin)
    );
  }

  private Point nodeSize(Tree<?> t, Function<String, Point> stringSizer) {
    Point sSize = stringSizer.apply(t.content().toString());
    return new Point(
        sSize.x() + 2 * c.boxMargin,
        sSize.y() + 2 * c.boxMargin
    );
  }

  private Point stringSize(String s) {
    return new Point(
        s.lines().mapToDouble(String::length).max().orElse(0) * c.charW,
        s.lines().count() * c.charH
    );
  }

  private Point treeSize(Tree<?> t, Function<String, Point> stringSizer) {
    if (t.nChildren() == 0) {
      return nodeSize(t, stringSizer);
    }
    Point nodeSize = nodeSize(t, stringSizer);
    double w = 0;
    double h = 0;
    for (int i = 0; i < t.nChildren(); i = i + 1) {
      Point childSize = treeSize(t.child(i), stringSizer);
      w = w + childSize.x() + ((i > 0) ? c.nodeMinXGap : 0);
      h = Math.max(h, childSize.y());
    }
    return new Point(
        Math.max(nodeSize.x(), w),
        nodeSize.y() + c.nodeMinYGap + h
    );
  }

}