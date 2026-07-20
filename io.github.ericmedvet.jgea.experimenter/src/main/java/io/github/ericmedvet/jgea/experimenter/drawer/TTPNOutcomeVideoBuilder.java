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

import io.github.ericmedvet.jgea.core.representation.programsynthesis.ttpn.Runner;
import io.github.ericmedvet.jgea.core.representation.programsynthesis.ttpn.Wire;
import io.github.ericmedvet.jgea.core.representation.programsynthesis.type.Type;
import io.github.ericmedvet.jgea.problem.image.ImageUtils;
import io.github.ericmedvet.jviz.core.drawer.Drawer;
import io.github.ericmedvet.jviz.core.drawer.Video;
import io.github.ericmedvet.jviz.core.drawer.VideoBuilder;
import io.github.ericmedvet.jviz.core.util.VideoUtils;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TTPNOutcomeVideoBuilder implements VideoBuilder<Runner.TTPNInstrumentedOutcome> {
  private final Configuration configuration;

  public TTPNOutcomeVideoBuilder(Configuration configuration) {
    this.configuration = configuration;
  }

  public record Configuration(
      double frameRate,
      Color textInfoColor,
      Color tokenContentColor,
      double tokenWireSizeRate,
      double tokenSpacingRate,
      TTPNDrawer.Configuration drawerConfiguration
  ) {
    public static final Configuration DEFAULT = new Configuration(
        2d,
        Color.BLUE,
        Color.WHITE,
        2d,
        1.1d,
        TTPNDrawer.Configuration.DEFAULT
    );
  }

  private static double length(List<Point2D> points) {
    double l = 0;
    for (int i = 1; i < points.size(); i++) {
      l = l + Math.sqrt(
          Math.pow(points.get(i).getX() - points.get(i - 1).getX(), 2d) + Math.pow(
              points.get(i)
                  .getY() - points.get(i - 1).getY(),
              2d
          )
      );
    }
    return l;
  }

  private static Point2D onSegment(Point2D p1, Point2D p2, double l) {
    double a = Math.atan2(p2.getY() - p1.getY(), p2.getX() - p1.getX());
    return new Point2D.Double(
        p1.getX() + l * Math.cos(a),
        p1.getY() + l * Math.sin(a)
    );
  }

  @Override
  public Video build(VideoInfo videoInfo, Runner.TTPNInstrumentedOutcome outcome) {
    TTPNDrawer drawer = new TTPNDrawer(configuration.drawerConfiguration);
    // obtain network image and structure
    Drawer.ImageInfo imageInfo = new Drawer.ImageInfo(videoInfo.w(), videoInfo.h());
    BufferedImage networkImage = drawer.buildRaster(imageInfo, outcome.network());
    TTPNDrawer.Metrics m = TTPNDrawer.Metrics.of(
        networkImage.getWidth(),
        networkImage.getHeight(),
        outcome.network()
    );
    Map<Wire, List<Point2D>> wirePaths = outcome.network()
        .wires()
        .stream()
        .collect(
            Collectors.toMap(
                w -> w,
                w -> drawer.computeWirePoints(m, w, outcome.network())
            )
        );
    Map<Wire, Type> wireTypes = outcome.network()
        .wires()
        .stream()
        .collect(
            Collectors.toMap(
                w -> w,
                w -> outcome.network().concreteOutputType(w.src())
            )
        );
    // iterate over steps
    int steps = outcome.wireContents()
        .keySet()
        .stream()
        .mapToInt(Runner.TTPNInstrumentedOutcome.Key::step)
        .max()
        .orElse(0);
    return new Video(
        IntStream.range(0, steps + 1)
            .mapToObj(k -> buildImage(networkImage, wirePaths, wireTypes, k, outcome.wireContents()))
            .toList(),
        configuration.frameRate,
        videoInfo.encoder()
    );
  }

  @Override
  public VideoInfo videoInfo(Runner.TTPNInstrumentedOutcome outcome) {
    TTPNDrawer drawer = new TTPNDrawer(configuration.drawerConfiguration);
    Drawer.ImageInfo imageInfo = drawer.imageInfo(outcome.network());
    return new VideoInfo(
        imageInfo.w(),
        imageInfo.h(),
        VideoUtils.EncoderFacility.DEFAULT
    );
  }

  private BufferedImage buildImage(
      BufferedImage networkImage,
      Map<Wire, List<Point2D>> wirePaths,
      Map<Wire, Type> wireTypes,
      int k,
      Map<Runner.TTPNInstrumentedOutcome.Key, List<Object>> wireContents
  ) {
    BufferedImage image = new BufferedImage(
        networkImage.getWidth(),
        networkImage.getHeight(),
        BufferedImage.TYPE_3BYTE_BGR
    );
    Graphics2D g = image.createGraphics();
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g.setClip(new Rectangle2D.Double(0, 0, image.getWidth(), image.getHeight()));
    g.drawImage(networkImage, 0, 0, null);
    // draw time
    String str = "k = %d".formatted(k);
    Rectangle2D strR = ImageUtils.bounds(str, g.getFont(), g);
    g.setColor(configuration.textInfoColor);
    g.drawString(str, 1f, 1f + (float) strR.getHeight());
    // draw tokens
    wirePaths.forEach(
        (w, points) -> drawTokens(
            g,
            points,
            wireTypes.get(w),
            wireContents.getOrDefault(new Runner.TTPNInstrumentedOutcome.Key(w, k), List.of())
        )
    );
    g.dispose();
    return image;
  }

  private void drawTokens(Graphics2D g, List<Point2D> wirePoints, Type type, List<Object> tokens) {
    if (tokens.isEmpty()) {
      return;
    }
    double l = length(wirePoints);
    double tR = configuration.tokenWireSizeRate * configuration.drawerConfiguration.wireW();
    double tS = 2d * tR * configuration.tokenSpacingRate;
    double d0 = tS - tR;
    if (d0 + tS * (tokens.size() - 1) + tR > l) {
      tS = (l - d0 * wirePoints.size()) / (tokens.size() + 1);
    }
    @SuppressWarnings("SuspiciousMethodCalls") Color typeColor = configuration.drawerConfiguration.baseTypeColors()
        .getOrDefault(type, configuration.drawerConfiguration.otherTypeColor());
    int sC = wirePoints.size() - 1;
    double sL = length(List.of(wirePoints.get(sC), wirePoints.get(sC - 1)));
    double d = d0;
    Shape clip = g.getClip();
    for (Object token : tokens) {
      if (d > sL) {
        d = d0;
        sC = sC - 1;
        sL = length(List.of(wirePoints.get(sC), wirePoints.get(sC - 1)));
      }
      Point2D p = onSegment(wirePoints.get(sC), wirePoints.get(sC - 1), d);
      d = d + tS;
      Ellipse2D circle = new Ellipse2D.Double(p.getX() - tR, p.getY() - tR, 2d * tR, 2d * tR);
      g.setColor(typeColor);
      g.fill(circle);
      g.setColor(configuration.drawerConfiguration.borderColor());
      g.draw(circle);
      g.setClip(circle);
      String str = token.toString();
      Rectangle2D strR = ImageUtils.bounds(str, g.getFont(), g);
      g.setColor(configuration.tokenContentColor);
      g.drawString(
          str,
          (float) p.getX() - (float) strR.getWidth() / 2f,
          (float) p.getY() + (float) strR.getHeight() / 2f
      );
      g.setClip(clip);
    }
  }
}