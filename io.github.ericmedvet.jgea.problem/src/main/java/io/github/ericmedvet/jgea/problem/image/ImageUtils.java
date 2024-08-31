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

package io.github.ericmedvet.jgea.problem.image;

import io.github.ericmedvet.jnb.datastructure.Grid;
import io.github.ericmedvet.jsdynsym.core.numerical.UnivariateRealFunction;
import io.github.ericmedvet.jviz.core.drawer.Drawer;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;

public class ImageUtils {

  private ImageUtils() {}

  private static Rectangle2D bounds(String s, Font f, Graphics2D g) {
    return f.createGlyphVector(g.getFontRenderContext(), s).getOutline().getBounds2D();
  }

  public static BufferedImage render(UnivariateRealFunction f, int w, int h, boolean normalize) {
    BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
    for (int x = 0; x < w; x++) {
      for (int y = 0; y < h; y++) {
        double fOut = f.applyAsDouble(new double[] {(double) x / (double) w, (double) y / (double) h});
        if (normalize) {
          fOut = Math.tanh(fOut) / 2d + 0.5d;
        }
        int gray = (int) Math.round(fOut * 256d);
        int color = (gray << 16) | (gray << 8) | gray;
        bi.setRGB(x, y, color);
      }
    }
    return bi;
  }

  public static Drawer<String> stringDrawer(Color fgColor, Color bgColor, double marginRate) {
    return (g, s) -> {
      double gW = g.getClipBounds().getWidth();
      double gH = g.getClipBounds().getHeight();
      g.setColor(bgColor);
      g.fill(g.getClipBounds());
      Font font = g.getFont();
      float size = 1;
      font = font.deriveFont(size);
      Rectangle2D bounds = bounds(s, font, g);
      while (bounds.getWidth() > 0
          && bounds.getWidth() < gW * (1d - 2d * marginRate)
          && bounds.getHeight() < gH * (1d - 2d * marginRate)) {
        size = size + 1;
        font = font.deriveFont(size);
        bounds = bounds(s, font, g);
      }
      font = font.deriveFont(size - 1);
      bounds = bounds(s, font, g);
      g.setColor(fgColor);
      g.setFont(font);
      double gX0 = g.getClipBounds().getMinX();
      double gY0 = g.getClipBounds().getMinY();
      double sX0 = bounds.getMinX();
      double sY0 = bounds.getMinY();
      double sW = bounds.getWidth();
      double sH = bounds.getHeight();
      g.drawString(s, (float) (gX0 - sX0 + gW * marginRate + (gW * (1d - 2d * marginRate) - sW) / 2d), (float)
          (gY0 - sY0 + gH * marginRate + (gH * (1d - 2d * marginRate) - sH) / 2d));
    };
  }

  public static Grid<double[]> toGrayGrid(BufferedImage img) {
    return toRGBGrid(img).map(rgb -> new double[] {(rgb[0] + rgb[1] + rgb[2]) / 3d});
  }

  public static Grid<double[]> toRGBGrid(BufferedImage img) {
    return Grid.create(img.getWidth(), img.getHeight(), (x, y) -> {
      Color c = new Color(img.getRGB(x, y));
      return new double[] {(double) c.getRed() / 255d, (double) c.getGreen() / 255d, (double) c.getBlue() / 255d};
    });
  }

  public static Drawer<BufferedImage> imageDrawer(Color bgColor, double marginRate) {
    return (g, img) -> {
      double gW = g.getClipBounds().getWidth();
      double gH = g.getClipBounds().getHeight();
      g.setColor(bgColor);
      g.fill(g.getClipBounds());
      double gX0 = g.getClipBounds().getMinX();
      double gY0 = g.getClipBounds().getMinY();
      double iW = img.getWidth();
      double iH = img.getHeight();
      double s = Math.min((gW * (1d - 2d * marginRate) / iW), (gH * (1d - 2d * marginRate) / iH));
      g.drawImage(img, new AffineTransform(s, 0, 0, s, gX0 + gW * marginRate, gY0 + gH * marginRate), null);
    };
  }

  public static BufferedImage loadFromResource(String name) {
    try (InputStream is = ImageUtils.class.getResourceAsStream("/images/" + name)) {
      if (is == null) {
        throw new IllegalArgumentException("Cannot find image '%s'".formatted(name));
      }
      return ImageIO.read(is);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
