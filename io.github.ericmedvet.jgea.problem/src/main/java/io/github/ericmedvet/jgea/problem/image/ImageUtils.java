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
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

public class ImageUtils {

  private ImageUtils() {}

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

  public static Grid<double[]> toRGBGrid(BufferedImage img) {
    return Grid.create(img.getWidth(), img.getHeight(), (x, y) -> {
      Color c = new Color(img.getRGB(x, y));
      return new double[] {(double) c.getRed() / 255d, (double) c.getGreen() / 255d, (double) c.getBlue() / 255d};
    });
  }

  public static Grid<double[]> toGrayGrid(BufferedImage img) {
    return toRGBGrid(img).map(rgb -> new double[] {(rgb[0] + rgb[1] + rgb[2]) / 3d});
  }

  private static Rectangle2D bounds(String s, Font f, Graphics2D g) {
    return f.createGlyphVector(g.getFontRenderContext(), s).getOutline().getBounds2D();
  }

  public static Drawer<String> stringDrawer(Color color) {
    return (g, s) -> {
      double w = g.getClipBounds().getWidth();
      double h = g.getClipBounds().getHeight();
      Font font = g.getFont();
      float size = 1;
      font = font.deriveFont(size);
      Rectangle2D bounds = bounds(s, font, g);
      while (bounds.getWidth() > 0 && bounds.getWidth() < w && bounds.getHeight() < h) {
        size = size + 1;
        font = font.deriveFont(size);
        bounds = bounds(s, font, g);
      }
      font = font.deriveFont(size - 1);
      bounds = bounds(s, font, g);
      g.setColor(color);
      g.setFont(font);
      g.drawString(s, (float) (g.getClipBounds().getMinX() - bounds.getMinX()), (float)
          (g.getClipBounds().getMinY() - bounds.getMinY()));
    };
  }
}
