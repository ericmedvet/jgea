package io.github.ericmedvet.jgea.tui.util;

import java.util.ArrayList;
import java.util.List;
public record Rectangle(Point min, Point max) {
  public int h() {
    return max().y() - min().y();
  }

  public Rectangle inner(int delta) {
    return new Rectangle(min().delta(delta, delta), max().delta(-delta, -delta));
  }

  public Point ne() {
    return min();
  }

  public Point nw() {
    return new Point(max().x() - 1, min().y());
  }

  public Point se() {
    return new Point(min().x(), max().y() - 1);
  }

  public List<Rectangle> splitHorizontally(float... rs) {
    List<Rectangle> rectangles = new ArrayList<>();
    for (int i = 0; i < rs.length; i++) {
      rectangles.add(new Rectangle(
          new Point(min().x() + (i == 0 ? 0 : Math.round(rs[i - 1] * w())), min().y()),
          new Point(min().x() + Math.round(rs[i] * w()), max().y())
      ));
    }
    rectangles.add(new Rectangle(
        new Point(min().x() + Math.round(rs[rs.length - 1] * w()), min().y()),
        new Point(max.x(), max().y())
    ));
    return rectangles;
  }

  public List<Rectangle> splitVertically(float... rs) {
    List<Rectangle> rectangles = new ArrayList<>();
    for (int i = 0; i < rs.length; i++) {
      rectangles.add(new Rectangle(
          new Point(min.x(), min().y() + (i == 0 ? 0 : Math.round(rs[i - 1] * h()))),
          new Point(max.x(), min().y() + Math.round(rs[i] * h()))
      ));
    }
    rectangles.add(new Rectangle(
        new Point(min.x(), min().y() + Math.round(rs[rs.length - 1] * h())),
        new Point(max.x(), max.y())
    ));
    return rectangles;
  }

  public Point sw() {
    return max().delta(-1, -1);
  }

  public int w() {
    return max().x() - min().x();
  }

}
