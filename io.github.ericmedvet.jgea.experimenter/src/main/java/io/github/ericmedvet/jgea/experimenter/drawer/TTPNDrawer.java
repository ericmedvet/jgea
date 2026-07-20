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

import io.github.ericmedvet.jgea.core.representation.programsynthesis.ttpn.Gate;
import io.github.ericmedvet.jgea.core.representation.programsynthesis.ttpn.Network;
import io.github.ericmedvet.jgea.core.representation.programsynthesis.ttpn.Wire;
import io.github.ericmedvet.jgea.core.representation.programsynthesis.type.Base;
import io.github.ericmedvet.jgea.core.representation.programsynthesis.type.Type;
import io.github.ericmedvet.jgea.problem.image.ImageUtils;
import io.github.ericmedvet.jnb.datastructure.DoubleRange;
import io.github.ericmedvet.jviz.core.drawer.Drawer;
import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TTPNDrawer implements Drawer<Network> {

  private final Configuration configuration;

  public TTPNDrawer(Configuration configuration) {
    this.configuration = configuration;
  }

  public record Configuration(
      Color gateBGColor, Color deadGateBGColor, Color borderColor, Map<Base, Color> baseTypeColors,
      Color otherTypeColor, double gateW, double wireW,
      double gateWHRatio, double portRadiusHRate, double gapWRate, double gapHRate, double marginWRate,
      double marginHRate, double gateOutputWRate, double gateOutputHRate, double ioGateOutputWRate,
      double ioGateOutputHRate, double textWMarginRate, double textHMarginRate,
      boolean showGateIndex, boolean showDeadGate
  ) {
    public static final Configuration DEFAULT = new Configuration(
        Color.LIGHT_GRAY,
        Color.PINK,
        Color.BLACK,
        Map.ofEntries(
            Map.entry(Base.INT, Color.BLUE),
            Map.entry(Base.REAL, Color.CYAN.darker().darker()),
            Map.entry(Base.BOOLEAN, Color.RED),
            Map.entry(Base.STRING, Color.GREEN.darker().darker())
        ),
        Color.DARK_GRAY,
        100,
        5,
        1.5d,
        0.1d,
        0.5d,
        0.5d,
        0.75d,
        0.75d,
        0.9d,
        0.75d,
        0.5d,
        0.5d,
        0.05,
        0.05,
        true,
        true
    );
  }

  protected record Metrics(
      double w, double h, int iW, int iH, Map<Integer, Point> gatePoints, Map<Integer, SequencedSet<Wire>> xGapWires,
      Map<Integer, SequencedSet<Wire>> yGapWires
  ) {
    public static Metrics of(double w, double h, Network network) {
      Map<Integer, Point> gatePoints = computeGatePoints(network);
      int iW = gatePoints.values().stream().mapToInt(Point::x).max().orElseThrow() + 1;
      int iH = gatePoints.values().stream().mapToInt(Point::y).max().orElseThrow() + 1;
      Map<Integer, SequencedSet<Wire>> xGapWires = new LinkedHashMap<>();
      Map<Integer, SequencedSet<Wire>> yGapWires = new LinkedHashMap<>();
      network.wires()
          .forEach(wire -> {
            Point srcPoint = gatePoints.get(wire.src().gateIndex());
            Point dstPoint = gatePoints.get(wire.dst().gateIndex());
            if (srcPoint.x + 1 == dstPoint.x) {
              xGapWires.computeIfAbsent(srcPoint.x, i -> new LinkedHashSet<>()).add(wire);
            } else {
              xGapWires.computeIfAbsent(srcPoint.x, i -> new LinkedHashSet<>()).add(wire);
              xGapWires.computeIfAbsent(dstPoint.x - 1, i -> new LinkedHashSet<>()).add(wire);
              yGapWires.computeIfAbsent(dstPoint.y, i -> new LinkedHashSet<>()).add(wire);
            }
          });
      return new Metrics(w, h, iW, iH, gatePoints, xGapWires, yGapWires);
    }
  }

  private record Point(int x, int y) {}

  private static boolean areXAligned(Point2D p1, Point2D p2, Point2D p3) {
    if (p1.getX() == p2.getX() && p2.getX() == p3.getX()) {
      if (p1.getY() <= p2.getY() && p2.getY() <= p3.getY()) {
        return true;
      }
      return p1.getY() >= p2.getY() && p2.getY() >= p3.getY();
    }
    return false;
  }

  private static boolean areYAligned(Point2D p1, Point2D p2, Point2D p3) {
    if (p1.getY() == p2.getY() && p2.getY() == p3.getY()) {
      if (p1.getX() <= p2.getX() && p2.getX() <= p3.getX()) {
        return true;
      }
      return p1.getX() >= p2.getX() && p2.getX() >= p3.getX();
    }
    return false;
  }

  private static Map<Integer, Point> computeGatePoints(Network network) {
    Map<Integer, Point> map = new TreeMap<>();
    network.gates()
        .keySet()
        .stream()
        .sorted(Comparator.comparingInt(gi -> network.wiresTo(gi).size()))
        .forEach(
            gi -> fillGatesPoints(
                network,
                gi,
                new Point(0, 0),
                map
            )
        );
    // pull output node to max right
    Set<Integer> oIndexes = network.outputGates().keySet();
    int maxX = map.entrySet()
        .stream()
        .filter(e -> !oIndexes.contains(e.getKey()))
        .mapToInt(e -> e.getValue().x)
        .max()
        .orElseThrow() + 1;
    oIndexes.forEach(oi -> map.put(oi, new Point(maxX, map.get(oi).y)));
    return map;
  }

  private static void fillGatesPoints(Network network, int gi, Point current, Map<Integer, Point> map) {
    Gate gate = network.gates().get(gi);
    if (map.containsKey(gi)) {
      return;
    }
    while (map.containsValue(current)) {
      current = new Point(current.x, current.y + 1);
    }
    map.put(gi, current);
    Point next = new Point(current.x + 1, current.y);
    IntStream.range(0, gate.outputTypes().size())
        .forEach(
            pi -> network.wiresFrom(gi, pi)
                .forEach(
                    w -> fillGatesPoints(
                        network,
                        w.dst().gateIndex(),
                        next,
                        map
                    )
                )
        );
  }

  private static int indexOf(Class<? extends Gate> gateClass, int gi, Network network) {
    return IntStream.range(0, network.gates().size())
        .filter(lgi -> gateClass.isInstance(network.gates().get(lgi)))
        .boxed()
        .toList()
        .indexOf(gi);
  }

  private static <T> int indexOf(T t, SequencedSet<T> s) {
    int c = 0;
    for (T currentT : s) {
      if (t.equals(currentT)) {
        return c;
      }
      c = c + 1;
    }
    return -1;
  }

  private static Path2D toPath(List<Point2D> points) {
    Path2D path = new Path2D.Double();
    path.moveTo(points.getFirst().getX(), points.getFirst().getY());
    IntStream.range(1, points.size()).forEach(i -> path.lineTo(points.get(i).getX(), points.get(i).getY()));
    return path;
  }

  protected List<Point2D> computeWirePoints(Metrics m, Wire w, Network network) {
    Point srcPoint = m.gatePoints.get(w.src().gateIndex());
    Point dstPoint = m.gatePoints.get(w.dst().gateIndex());
    double srcX = gateXRange(srcPoint.x, m).max();
    double srcY = nThPos(
        w.src().portIndex(),
        network.gates().get(w.src().gateIndex()).outputTypes().size(),
        gateYRange(srcPoint.y, m)
    );
    double dstX = gateXRange(dstPoint.x, m).min();
    double dstY = nThPos(
        w.dst().portIndex(),
        network.gates().get(w.dst().gateIndex()).inputPorts().size(),
        gateYRange(dstPoint.y, m)
    );
    List<Point2D> points = new ArrayList<>();
    points.add(new Point2D.Double(srcX, srcY));
    double wp1x = nThPos(
        indexOf(w, m.xGapWires().get(srcPoint.x)),
        m.xGapWires().get(srcPoint.x).size(),
        gapXRange(srcPoint.x, m)
    );
    points.add(new Point2D.Double(wp1x, srcY));
    if (srcPoint.x + 1 == dstPoint.x) {
      points.add(new Point2D.Double(wp1x, dstY));
    } else {
      double wp2y = nThPos(
          indexOf(w, m.yGapWires().get(dstPoint.y)),
          m.yGapWires().get(dstPoint.y).size(),
          gapYRange(dstPoint.y, m)
      );
      double wp3x = nThPos(
          indexOf(w, m.xGapWires().get(dstPoint.x - 1)),
          m.xGapWires().get(dstPoint.x - 1).size(),
          gapXRange(dstPoint.x - 1, m)
      );
      points.add(new Point2D.Double(wp1x, wp2y));
      points.add(new Point2D.Double(wp3x, wp2y));
      points.add(new Point2D.Double(wp3x, dstY));
    }
    points.add(new Point2D.Double(dstX, dstY));
    // simplify
    List<Point2D> simpliefiedPoints = new ArrayList<>();
    points.forEach(p -> {
      if (simpliefiedPoints.size() >= 2) {
        Point2D last = simpliefiedPoints.getLast();
        Point2D secondLast = simpliefiedPoints.get(simpliefiedPoints.size() - 2);
        if (areXAligned(secondLast, last, p) || areYAligned(secondLast, last, p)) {
          simpliefiedPoints.removeLast();
        }
      }
      simpliefiedPoints.add(p);
    });
    return simpliefiedPoints;
  }

  @Override
  public void draw(Graphics2D g, Network network) {
    Metrics m = Metrics.of(g.getClipBounds().getWidth(), g.getClipBounds().getHeight(), network);
    //draw gates
    network.gates().keySet().forEach(gi -> drawGate(g, m, gi, network));
    //draw wires
    network.wires().forEach(w -> drawWire(g, m, w, network));
  }

  protected void drawGate(Graphics2D g, Metrics m, int gi, Network network) {
    DoubleRange xR = gateXRange(m.gatePoints.get(gi).x, m);
    DoubleRange yR = gateYRange(m.gatePoints.get(gi).y, m);
    // draw gate
    Gate gate = network.gates().get(gi);
    Shape s = switch (gate) {
      case Gate.InputGate inputGate -> {
        Path2D p = new Path2D.Double();
        p.moveTo(xR.min(), yR.min());
        p.lineTo(xR.denormalize(configuration.ioGateOutputWRate), yR.min());
        p.lineTo(xR.max(), yR.denormalize((1d - configuration.ioGateOutputHRate) / 2d));
        p.lineTo(xR.max(), yR.denormalize(1d - (1d - configuration.ioGateOutputHRate) / 2d));
        p.lineTo(xR.denormalize(configuration.ioGateOutputWRate), yR.max());
        p.lineTo(xR.min(), yR.max());
        p.closePath();
        yield p;
      }
      case Gate.OutputGate outputGate -> {
        Path2D p = new Path2D.Double();
        p.moveTo(xR.min(), yR.denormalize((1d - configuration.ioGateOutputHRate) / 2d));
        p.lineTo(xR.denormalize(1d - configuration.ioGateOutputWRate), yR.min());
        p.lineTo(xR.max(), yR.min());
        p.lineTo(xR.max(), yR.max());
        p.lineTo(xR.denormalize(1d - configuration.ioGateOutputWRate), yR.max());
        p.lineTo(xR.min(), yR.denormalize(1d - (1d - configuration.ioGateOutputHRate) / 2d));
        p.closePath();
        yield p;
      }
      default -> {
        Path2D p = new Path2D.Double();
        p.moveTo(xR.min(), yR.min());
        p.lineTo(xR.denormalize(configuration.gateOutputWRate), yR.min());
        p.lineTo(xR.max(), yR.denormalize((1d - configuration.gateOutputHRate) / 2d));
        p.lineTo(xR.max(), yR.denormalize(1d - (1d - configuration.gateOutputHRate) / 2d));
        p.lineTo(xR.denormalize(configuration.gateOutputWRate), yR.max());
        p.lineTo(xR.min(), yR.max());
        p.closePath();
        yield p;
      }
    };
    if (configuration.showDeadGate && network.isDeadGate(gi)) {
      g.setColor(configuration.deadGateBGColor);
    } else {
      g.setColor(configuration.gateBGColor);
    }
    g.fill(s);
    g.setColor(configuration.borderColor);
    g.draw(s);
    // draw ports
    IntStream.range(0, gate.inputPorts().size())
        .forEach(
            pi -> drawPort(
                g,
                gate.inputPorts().get(pi).type(),
                gate.inputPorts().get(pi).toString(),
                pi,
                gate.inputPorts().size(),
                xR,
                yR,
                true
            )
        );
    IntStream.range(0, gate.outputTypes().size())
        .forEach(
            pi -> drawPort(
                g,
                gate.outputTypes().get(pi),
                gate.outputTypes().get(pi).toString(),
                pi,
                gate.outputTypes().size(),
                xR,
                yR,
                false
            )
        );
    // write name
    g.setColor(configuration.borderColor);
    Shape originalClip = g.getClip();
    g.setClip(s);
    String str = switch (gate) {
      case Gate.InputGate inputGate -> "I-%d".formatted(indexOf(Gate.InputGate.class, gi, network));
      case Gate.OutputGate outputGate -> "O-%d".formatted(indexOf(Gate.OutputGate.class, gi, network));
      default -> gate.operator().toString();
    };
    Rectangle2D strR = ImageUtils.bounds(str, g.getFont(), g);
    float labelY = (float) (yR.min() + strR.getHeight() + yR.extent() * configuration.textHMarginRate);
    float labelX = switch (gate) {
      case Gate.InputGate inputGate -> (float) (xR.min() + xR.extent() * configuration.textHMarginRate);
      case Gate.OutputGate outputGate ->
        (float) (xR.max() - strR.getWidth() - xR.extent() * configuration.textHMarginRate);
      default -> (float) (xR.center() - strR.getWidth() / 2d);
    };
    g.drawString(str, labelX, labelY);
    // write id
    if (configuration.showGateIndex) {
      str = "%d".formatted(gi);
      strR = ImageUtils.bounds(str, g.getFont(), g);
      g.drawString(str, (float) (xR.center() - strR.getWidth() / 2d), (float) (yR.center() + strR.getHeight() / 2d));
    }
    // write generics
    if (gate.hasGenerics()) {
      str = network.concreteMapping(gi)
          .entrySet()
          .stream()
          .sorted(Comparator.comparing(e -> e.getKey().toString()))
          .map(Object::toString)
          .collect(Collectors.joining(", "));
      strR = ImageUtils.bounds(str, g.getFont(), g);
      g.drawString(
          str,
          (float) (xR.center() - strR.getWidth() / 2d),
          (float) (yR.max() - yR.extent() * configuration.textHMarginRate)
      );
    }
    g.setClip(originalClip);
  }

  private void drawPort(
      Graphics2D g,
      Type type,
      String label,
      int pi,
      int nPorts,
      DoubleRange xR,
      DoubleRange yR,
      boolean isInput
  ) {
    double pR = yR.extent() * configuration.portRadiusHRate;
    //noinspection SuspiciousMethodCalls
    g.setColor(configuration.baseTypeColors.getOrDefault(type, configuration.otherTypeColor));
    Rectangle2D s = isInput ? new Rectangle2D.Double(
        xR.min(),
        nThPos(pi, nPorts, yR) - pR,
        2d * pR,
        2d * pR
    ) : new Rectangle2D.Double(xR.max() - 2d * pR, nThPos(pi, nPorts, yR) - pR, 2d * pR, 2d * pR);
    g.fill(s);
    g.setColor(configuration.borderColor);
    g.draw(s);
    g.setColor(configuration.borderColor);
    Rectangle2D strR = ImageUtils.bounds(label, g.getFont(), g);
    if (isInput) {
      g.drawString(label, (float) (xR.min() + 2.5d * pR), (float) (nThPos(pi, nPorts, yR) + 2d * pR + strR.getMinY()));
    } else {
      g.drawString(
          label,
          (float) (xR.max() - 2.5d * pR - strR.getWidth()),
          (float) (nThPos(pi, nPorts, yR) + 2d * pR + strR.getMinY())
      );
    }
  }

  protected void drawWire(Graphics2D g, Metrics m, Wire w, Network network) {
    Path2D path = toPath(computeWirePoints(m, w, network));
    Stroke stroke = g.getStroke();
    g.setColor(configuration.borderColor);
    g.setStroke(
        new BasicStroke(
            (float) configuration.wireW,
            BasicStroke.CAP_BUTT,
            BasicStroke.JOIN_BEVEL
        )
    );
    g.draw(path);
    g.setColor(configuration.gateBGColor);
    g.setStroke(
        new BasicStroke(
            (float) configuration.wireW - 2f,
            BasicStroke.CAP_BUTT,
            BasicStroke.JOIN_BEVEL
        )
    );
    g.draw(path);
    g.setStroke(stroke);
  }

  private DoubleRange gapXRange(int x, Metrics m) {
    return new DoubleRange(gateXRange(x, m).max(), gateXRange(x + 1, m).min());
  }

  private DoubleRange gapYRange(int y, Metrics m) {
    return new DoubleRange(gateYRange(y, m).max(), gateYRange(y + 1, m).min());
  }

  private DoubleRange gateXRange(int x, Metrics m) {
    double cells = (double) m.iW + ((double) (m.iW - 1)) * configuration.gapWRate + 2 * configuration.marginWRate;
    double xs = configuration.marginWRate + (double) x + (x * configuration.gapWRate);
    return new DoubleRange(xs / cells * m.w, (xs + 1) / cells * m.w);
  }

  private DoubleRange gateYRange(int y, Metrics m) {
    double cells = (double) m.iH + ((double) (m.iH - 1)) * configuration.gapHRate + 2 * configuration.marginHRate;
    double ys = configuration.marginHRate + (double) y + (y * configuration.gapHRate);
    return new DoubleRange(ys / cells * m.h, (ys + 1) / cells * m.h);
  }

  @Override
  public ImageInfo imageInfo(Network network) {
    Metrics m = Metrics.of(1d, 1d, network);
    return new ImageInfo(
        (int) (configuration.gateW * (m.iW() + configuration.gapWRate() * (m
            .iW() - 1d) + 2d * configuration.marginWRate)),
        (int) (configuration.gateW / configuration.gateWHRatio() * (m.iH() + configuration.gapHRate() * (m
            .iH() - 1d) + 2d * configuration.marginHRate))
    );
  }

  private double nThPos(int i, int n, DoubleRange range) {
    return range.denormalize((double) (i + 1) / (double) (n + 1));
  }

}