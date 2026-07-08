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
import io.github.ericmedvet.jgea.core.representation.tree.numeric.Element;
import io.github.ericmedvet.jgea.core.representation.tree.numeric.Element.Constant;
import io.github.ericmedvet.jgea.core.representation.tree.numeric.Element.Operator;
import io.github.ericmedvet.jgea.core.representation.tree.numeric.Element.Variable;
import io.github.ericmedvet.jviz.core.drawer.Drawer;
import io.github.ericmedvet.jviz.core.geometry.Point;
import io.github.ericmedvet.jviz.core.geometry.Rectangle;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

public class FormulaDrawer implements Drawer<Tree<Element>> {

  private final Configuration c;

  public FormulaDrawer(Configuration configuration) {
    this.c = configuration;
  }

  private static Rectangle at(Point center, Point size) {
    return new Rectangle(center, size.x(), size.y());
  }

  private static Rectangle atMinX(double minX, double centerY, Rectangle r) {
    return new Rectangle(new Point(minX + r.width() / 2d, centerY), r.width(), r.height());
  }

  private static Rectangle atMinY(double centerX, double minY, Rectangle r) {
    return new Rectangle(new Point(centerX, minY + r.height() / 2d), r.width(), r.height());
  }

  private static Rectangle atMaxY(double centerX, double maxY, Rectangle r) {
    return new Rectangle(new Point(centerX, maxY - r.height() / 2d), r.width(), r.height());
  }

  private static Rectangle atMinXMaxY(double minX, double maxY, Rectangle r) {
    return new Rectangle(
        new Point(minX + r.width() / 2d, maxY - r.height() / 2d),
        r.width(),
        r.height()
    );
  }

  private static Rectangle atMaxXMinY(double maxX, double minY, Rectangle r) {
    return new Rectangle(
        new Point(maxX - r.width() / 2d, minY + r.height() / 2d),
        r.width(),
        r.height()
    );
  }

  private static boolean isConstant(Tree<Element> t) {
    return t.content() instanceof Element.Constant;
  }

  private static boolean hasParentheses(Element.Operator o, Tree<Element> child, int index) {
    if (child.nChildren() == 0) {
      return false;
    }
    if (o.equals(Operator.EXP) || o.equals(Operator.SQRT)) {
      return false;
    }
    if (o.equals(Operator.PROT_DIVISION) || o.equals(Operator.DIVISION)) {
      return false;
    }
    if (child.content().equals(Operator.LT) || child.content().equals(Operator.GT) || child.content()
        .equals(Operator.TERNARY)) {
      return true;
    }
    if (o.equals(Operator.ADDITION)) {
      return false;
    }
    if (child.content().equals(Operator.SIN) || child.content().equals(Operator.COS) || child.content()
        .equals(Operator.TANH) || child.content().equals(Operator.SQ) || child.content()
            .equals(Operator.SQRT) || child
                .content()
                .equals(Operator.LOG) || child.content().equals(Operator.PROT_LOG) || child.content()
                    .equals(Operator.RE_LU) || child.content().equals(Operator.DIVISION) || child.content()
                        .equals(Operator.PROT_DIVISION) || child.content().equals(Operator.EXP)) {
      return false;
    }
    if (o.equals(Operator.MULTIPLICATION) && child.content().equals(Operator.MULTIPLICATION)) {
      return false;
    }
    if (o.equals(Operator.POW) && index == 1) {
      return false;
    }
    return true;
  }

  @Override
  public void draw(Graphics2D g, Tree<Element> tree) {
    java.awt.Rectangle clipBounds = g.getClipBounds();
    draw(
        g,
        Rectangle.of(
            new Point(
                clipBounds.getMinX(),
                clipBounds.getMinY()
            ).sum(
                new Point(c.margin, c.margin)
            ),
            new Point(
                clipBounds.getMaxX(),
                clipBounds.getMaxY()
            ).diff(new Point(c.margin, c.margin))
        ),
        tree,
        1d
    );
  }

  private Rectangle openedParR(Rectangle r) {
    return Rectangle.of(
        r.min(),
        r.min().sum(new Point(r.height() * c.parenthesisWHRate, r.height()))
    );
  }

  private Rectangle closedParR(Rectangle r) {
    return Rectangle.of(
        r.max().diff(new Point(r.height() * c.parenthesisWHRate, r.height())),
        r.max()
    );
  }

  private Rectangle withPars(Rectangle r) {
    return new Rectangle(
        r.center(),
        r.width() + r.height() * c.parenthesisWHRate * 2d,
        r.height() * c.parenthesisHRate
    );
  }

  private Rectangle withoutPars(Rectangle r) {
    return new Rectangle(
        r.center(),
        r.width() - r.height() / c.parenthesisHRate * c.parenthesisWHRate * 2d,
        r.height() / c.parenthesisHRate
    );
  }

  private Rectangle withSqrt(Rectangle r) {
    return new Rectangle(
        r.center(),
        r.width() + r.height() * (c.sqrtOpenWHRate + c.sqrtCloseWHRate),
        r.height() + c.sqrtCeilYGap()
    );
  }

  private Rectangle withoutSqrt(Rectangle r) {
    double h = r.height() - c.sqrtCeilYGap;
    return Rectangle.of(
        r.min().sum(new Point(h * c.sqrtOpenWHRate, c.sqrtCeilYGap)),
        r.max().diff(Point.ofX(h * c.sqrtCloseWHRate))
    );
  }

  private void drawChild(
      Graphics2D g,
      double textScale,
      Rectangle r,
      Operator o,
      Tree<Element> child,
      int index
  ) {
    if (hasParentheses(o, child, index)) {
      draw(g, withoutPars(r), child, textScale);
      drawParenthesis(g, openedParR(r), true);
      drawParenthesis(g, closedParR(r), false);
    } else {
      draw(g, r, child, textScale);
    }
  }

  private void drawOp(Graphics2D g, double textScale, double x, double y, String s) {
    TreeDrawer.drawString(g, new Point(x, y), c.operatorColor, c.charH * textScale, c.debug, s);
  }

  private void drawParenthesis(Graphics2D g, Rectangle r, boolean open) {
    g.setStroke(new BasicStroke((float) c.fractionThickness));
    g.setColor(c.parenthesisColor);
    Path2D path = new Path2D.Double();
    if (open) {
      path.moveTo(r.max().x(), r.min().y());
      path.curveTo(
          r.min().x() + r.width() / 2d,
          r.min().y(),
          r.min().x(),
          r.min().y() + r.width() / 2d,
          r.min().x(),
          r.min().y() + 2 * r.width()
      );
      path.lineTo(r.min().x(), r.max().y() - 2 * r.width());
      path.curveTo(
          r.min().x(),
          r.max().y() - r.width() / 2d,
          r.min().x() + r.width() / 2d,
          r.max().y(),
          r.max().x(),
          r.max().y()
      );
    } else {
      path.moveTo(r.min().x(), r.min().y());
      path.curveTo(
          r.max().x() - r.width() / 2d,
          r.min().y(),
          r.max().x(),
          r.min().y() + r.width() / 2d,
          r.max().x(),
          r.min().y() + 2 * r.width()
      );
      path.lineTo(r.max().x(), r.max().y() - 2 * r.width());
      path.curveTo(
          r.max().x(),
          r.max().y() - r.width() / 2d,
          r.max().x() - r.width() / 2d,
          r.max().y(),
          r.min().x(),
          r.max().y()
      );
    }
    g.draw(path);
  }

  @Override
  public ImageInfo imageInfo(Tree<Element> tree) {
    Rectangle r = draw(null, new Rectangle(Point.ORIGIN, 1, 1), tree, 1d);
    return new ImageInfo(
        (int) Math.ceil(r.width() + c.margin * 2d),
        (int) Math.ceil(r.height() + c.margin * 2d)
    );
  }

  private Point stringSize(String s, double textScale) {
    return new Point(
        s.lines().mapToDouble(String::length).max().orElse(0) * c.charW * textScale,
        s.lines().count() * c.charH * textScale
    );
  }

  public record Configuration(
      double margin, double fractionThickness, double parenthesisThickness, double sqrtThickness,
      Color operatorColor, Color constColor, Color varFGColor,
      Color varBGColor, Color parenthesisColor, double charW, double charH,
      double fractionYGap, double fractionXGap, double operatorXGap, double operatorThinXGap,
      double powerRaiseRate,
      double parenthesisWHRate, double parenthesisHRate, double sqrtOpenWHRate,
      double sqrtCloseWHRate, double sqrtCeilYGap, double expTextScaleR, double minTextScale,
      String constFormat,
      boolean sortChildren,
      boolean debug
  ) {

    public static Configuration DEFAULT = new Configuration(
        5,
        1,
        0.5,
        1,
        Color.DARK_GRAY,
        Color.BLUE,
        Color.RED,
        Color.WHITE,
        Color.GRAY,
        10,
        16,
        2,
        2,
        3,
        1,
        0.5,
        0.1,
        1.15,
        0.2,
        0.1,
        2,
        0.7,
        0.4,
        "%.3f",
        true,
        false
    );

    public Configuration scaled(double r) {
      return new Configuration(
          margin * r,
          fractionThickness * r,
          parenthesisThickness * r,
          sqrtThickness * r,
          operatorColor,
          constColor,
          varFGColor,
          varBGColor,
          parenthesisColor,
          charW * r,
          charH * r,
          fractionYGap * r,
          fractionXGap * r,
          operatorXGap * r,
          operatorThinXGap * r,
          powerRaiseRate,
          parenthesisWHRate,
          parenthesisHRate,
          sqrtOpenWHRate,
          sqrtCloseWHRate,
          sqrtCeilYGap * r,
          expTextScaleR,
          minTextScale,
          constFormat,
          sortChildren,
          debug
      );
    }
  }

  private final static Comparator<Tree<Element>> ELEMENT_COMPARATOR = Comparator
      .comparingInt((Tree<Element> e) -> switch (e.content()) {
        case Constant c -> 0;
        case Variable v -> 1;
        default -> 2;
      })
      .thenComparing((t1, t2) -> switch (t1.content()) {
        case Constant c1 -> Double.compare(c1.value(), ((Constant) t2.content()).value());
        case Variable v1 -> v1.name().compareTo(((Variable) t2.content()).name());
        default -> Integer.compare(t1.size(), t2.size());
      });

  private Rectangle draw(Graphics2D g, Rectangle r, Tree<Element> t, double textScale) {
    Rectangle innerR = switch (t.content()) {
      case Element.Variable v -> {
        Rectangle nodeR = at(r.center(), stringSize(v.name(), textScale));
        if (Objects.nonNull(g)) {
          g.setColor(c.varBGColor);
          g.fill(TreeDrawer.drawable(nodeR));
          TreeDrawer.drawString(
              g,
              nodeR.center(),
              c.varFGColor,
              c.charH * textScale,
              c.debug,
              v.name()
          );
        }
        yield nodeR;
      }
      case Element.Constant n -> {
        String s;
        if (n.value() % 1d == 0) {
          s = Integer.toString((int) n.value());
        } else {
          s = c.constFormat.formatted(n.value());
          if (Double.toString(n.value()).length() < s.length()) {
            s = Double.toString(n.value());
          }
        }
        Rectangle nodeR = at(r.center(), stringSize(s, textScale));
        if (Objects.nonNull(g)) {
          TreeDrawer.drawString(
              g,
              nodeR.center(),
              c.constColor,
              c.charH * textScale,
              c.debug,
              s
          );
        }
        yield nodeR;
      }
      case Element.Operator o -> {
        List<Tree<Element>> children = new ArrayList<>(t.childStream().toList());
        if (c.sortChildren && (o.equals(Operator.MULTIPLICATION) || o.equals(Operator.ADDITION))) {
          children.sort(ELEMENT_COMPARATOR);
        }
        Rectangle opR = at(r.center(), stringSize(switch (o) {
          case MULTIPLICATION -> "·";
          case PROT_LOG -> "log*";
          default -> o.toString();
        }, textScale));
        double childTextScale = Math.max(c.minTextScale, textScale * c.expTextScaleR);
        List<Rectangle> childRs = IntStream.range(0, t.nChildren()).mapToObj(j -> {
          Rectangle childR = draw(null, r, children.get(j), switch (o) {
            case POW -> j == 0 ? textScale : childTextScale;
            case EXP -> childTextScale;
            default -> textScale;
          });
          if (hasParentheses(o, children.get(j), j)) {
            childR = withPars(childR);
          }
          return childR;
        }).toList();
        double childrenW = childRs.stream().mapToDouble(Rectangle::width).sum();
        double h = switch (o) {
          case POW -> childRs.get(0).height() * c.powerRaiseRate + childRs.get(1).height();
          case EXP ->
            stringSize("e", textScale).y() * c.powerRaiseRate + childRs.getFirst().height();
          case SQ -> childRs.getFirst().height() * c.powerRaiseRate + stringSize(
              "2",
              childTextScale
          ).y();
          case SQRT -> withSqrt(childRs.getFirst()).height();
          case DIVISION, PROT_DIVISION ->
            childRs.stream().mapToDouble(Rectangle::height).sum() + 2 * c.fractionYGap;
          default -> Math.max(
              opR.height(),
              childRs.stream().mapToDouble(Rectangle::height).max().orElse(0)
          );
        };
        double w = switch (o) {
          case POW -> childRs.getFirst().width() + c.operatorThinXGap + childRs.get(1).width();
          case EXP ->
            stringSize("e", textScale).x() + c.operatorThinXGap + childRs.getFirst().width();
          case SQ -> childRs.getFirst().width() + c.operatorThinXGap + stringSize(
              "2",
              childTextScale
          ).x();
          case SQRT -> withSqrt(childRs.getFirst()).width();
          case DIVISION, PROT_DIVISION ->
            childRs.stream().mapToDouble(Rectangle::width).max().orElseThrow() + 2 * c.fractionXGap;
          case MULTIPLICATION -> {
            int nOfDots = 0;
            for (int i = 1; i < t.nChildren(); i = i + 1) {
              if (isConstant(children.get(i - 1)) && isConstant(children.get(i))) {
                nOfDots = nOfDots + 1;
              }
            }
            yield (opR.width() + 2 * c.operatorXGap) * nOfDots + c.operatorXGap * (t
                .nChildren() - 1 - nOfDots) + childrenW;
          }
          case ADDITION, SUBTRACTION, GT, LT ->
            (opR.width() + 2 * c.operatorXGap) * (t.nChildren() - 1) + childrenW;
          default -> {
            if (t.nChildren() > 1) {
              Rectangle argsR = withPars(
                  new Rectangle(
                      Point.ORIGIN,
                      (c.operatorXGap + stringSize(",", textScale).x()) * (t.nChildren() - 1) + childrenW,
                      h
                  )
              );
              h = Math.max(argsR.height(), h);
              yield opR.width() + c.operatorThinXGap + argsR.width();
            }
            yield opR.width() + c.operatorThinXGap + childRs.getFirst().width();
          }
        };
        if (Objects.nonNull(g)) {
          double x = r.min().x() + (r.width() - w) / 2d;
          switch (o) {
            case POW -> {
              drawChild(
                  g,
                  textScale,
                  atMinXMaxY(r.min().x(), r.max().y(), childRs.getFirst()),
                  o,
                  children.getFirst(),
                  0
              );
              drawChild(
                  g,
                  childTextScale,
                  atMaxXMinY(r.max().x(), r.min().y(), childRs.get(1)),
                  o,
                  children.get(1),
                  1
              );
            }
            case EXP -> {
              Point size = stringSize("e", textScale);
              drawOp(g, textScale, r.min().x() + size.x() / 2d, r.max().y() - size.y() / 2d, "e");
              drawChild(
                  g,
                  childTextScale,
                  atMaxXMinY(r.max().x(), r.min().y(), childRs.getFirst()),
                  o,
                  children.getFirst(),
                  0
              );
            }
            case SQ -> {
              Point size = stringSize("2", childTextScale);
              drawChild(
                  g,
                  textScale,
                  atMinXMaxY(r.min().x(), r.max().y(), childRs.getFirst()),
                  o,
                  children.getFirst(),
                  0
              );
              drawOp(
                  g,
                  childTextScale,
                  r.max().x() - size.x() / 2d,
                  r.min().y() + size.y() / 2d,
                  "2"
              );
            }
            case SQRT -> {
              Rectangle argR = withoutSqrt(childRs.getFirst());
              double openW = argR.height() * c.sqrtOpenWHRate;
              drawChild(g, textScale, argR, o, children.getFirst(), 0);
              g.setColor(c.operatorColor);
              g.setStroke(new BasicStroke((float) c.sqrtThickness));
              Path2D path = new Path2D.Double();
              path.moveTo(r.min().x(), r.max().y() - 0.2 * r.height());
              path.lineTo(r.min().x() + openW / 3d, r.max().y() - 0.2 * r.height());
              path.lineTo(r.min().x() + openW * 2d / 3d, r.max().y());
              path.lineTo(r.min().x() + openW, r.min().y());
              path.lineTo(r.max().x(), r.min().y());
              path.lineTo(r.max().x(), r.min().y() + 0.1 * r.height());
              g.draw(path);
            }
            case DIVISION, PROT_DIVISION -> {
              drawChild(
                  g,
                  textScale,
                  atMinY(r.center().x(), r.min().y(), childRs.getFirst()),
                  o,
                  children.getFirst(),
                  0
              );
              drawChild(
                  g,
                  textScale,
                  atMaxY(r.center().x(), r.max().y(), childRs.get(1)),
                  o,
                  children.get(1),
                  1
              );
              g.setColor(c.operatorColor);
              g.setStroke(new BasicStroke((float) c.fractionThickness));
              Path2D path = new Path2D.Double();
              path.moveTo(r.min().x(), r.min().y() + childRs.getFirst().height() + c.fractionYGap);
              path.lineTo(r.max().x(), r.min().y() + childRs.getFirst().height() + c.fractionYGap);
              if (o.equals(Operator.PROT_DIVISION)) {
                path.lineTo(
                    r.max().x(),
                    r.min().y() + childRs.getFirst().height() + c.fractionYGap + r.height() * 0.1
                );
              }
              g.draw(path);
            }
            case MULTIPLICATION -> {
              for (int i = 0; i < t.nChildren(); i = i + 1) {
                if (i > 0) {
                  if (isConstant(children.get(i - 1)) && isConstant(children.get(i))) {
                    drawOp(
                        g,
                        textScale,
                        x + opR.width() / 2d + c.operatorXGap,
                        r.center().y(),
                        "·"
                    );
                    x = x + opR.width() + c.operatorXGap;
                  }
                  x = x + c.operatorXGap;
                }
                Rectangle childR = atMinX(x, r.center().y(), childRs.get(i));
                drawChild(g, textScale, childR, o, children.get(i), i);
                x = x + childR.width();
              }
            }
            case ADDITION, SUBTRACTION, GT, LT -> {
              for (int i = 0; i < t.nChildren(); i = i + 1) {
                if (i > 0) {
                  drawOp(
                      g,
                      textScale,
                      x + opR.width() / 2d + c.operatorXGap,
                      r.center().y(),
                      o.toString()
                  );
                  x = x + opR.width() + 2 * c.operatorXGap;
                }
                Rectangle childR = atMinX(x, r.center().y(), childRs.get(i));
                drawChild(g, textScale, childR, o, children.get(i), i);
                x = x + childR.width();
              }
            }
            default -> {
              drawOp(g, textScale, x + opR.width() / 2d, r.center().y(), o.toString());
              x = x + opR.width() + c.operatorThinXGap;
              if (t.nChildren() == 1) {
                drawChild(
                    g,
                    textScale,
                    atMinX(x, r.center().y(), childRs.getFirst()),
                    o,
                    children.getFirst(),
                    0
                );
              } else {
                Rectangle argsR = Rectangle.of(new Point(x, r.min().y()), r.max());
                drawParenthesis(g, openedParR(argsR), true);
                drawParenthesis(g, closedParR(argsR), false);
                argsR = withoutPars(argsR);
                x = argsR.min().x();
                Point sepSize = stringSize(",", textScale);
                for (int i = 0; i < t.nChildren(); i = i + 1) {
                  if (i > 0) {
                    drawOp(
                        g,
                        textScale,
                        x + sepSize.x() / 2d,
                        r.center().y() + opR.height() / 2d - sepSize.y() / 2d,
                        ","
                    );
                    x = x + sepSize.x() + c.operatorXGap;
                  }
                  Rectangle childR = atMinX(x, argsR.center().y(), childRs.get(i));
                  drawChild(g, textScale, childR, o, children.get(i), i);
                  x = x + childR.width();
                }
              }
            }
          }
        }
        yield new Rectangle(r.center(), w, h);
      }
      default ->
        throw new IllegalArgumentException("Unexpected node type: %s".formatted(t.content()));
    };
    if (Objects.nonNull(g) && c.debug) {
      TreeDrawer.drawDebugBox(g, innerR);
    }
    return innerR;
  }

}