/*-
 * ========================LICENSE_START=================================
 * jgea-experimenter
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
package io.github.ericmedvet.jgea.experimenter.drawer;

import io.github.ericmedvet.jnb.datastructure.DoubleRange;
import io.github.ericmedvet.jnb.datastructure.Grid;
import io.github.ericmedvet.jviz.core.drawer.Drawer;
import java.awt.*;
import java.awt.geom.Rectangle2D;

public class CADrawer implements Drawer<Grid<double[]>> {

  public record Configuration(ColorType colorType, DoubleRange range, int sizeRate, double marginRate) {
    public enum ColorType {
      GRAY,
      RGB
    }

    public static Configuration DEFAULT = new Configuration(ColorType.RGB, DoubleRange.UNIT, 4, 0);
  }

  private final Configuration c;

  public CADrawer(Configuration configuration) {
    this.c = configuration;
  }

  public CADrawer() {
    this(Configuration.DEFAULT);
  }

  @Override
  public void draw(Graphics2D g, Grid<double[]> grid) {
    double w = grid.w();
    double h = grid.h();
    double cW = g.getClipBounds().getWidth() * (1d - 2 * c.marginRate()) / w;
    double cH = g.getClipBounds().getHeight() * (1d - 2 * c.marginRate()) / h;
    double x0 = g.getClipBounds().getMinX() + g.getClipBounds().getWidth() * c.marginRate();
    double y0 = g.getClipBounds().getMinY() + g.getClipBounds().getHeight() * c.marginRate();
    grid.entries().forEach(e -> {
      Color color =
          switch (c.colorType()) {
            case RGB -> new Color(
                (float) c.range().normalize(e.value()[0]),
                (float) c.range().normalize(e.value()[1]),
                (float) c.range().normalize(e.value()[2]));
            case GRAY -> new Color(
                (float) c.range().normalize(e.value()[0]),
                (float) c.range().normalize(e.value()[0]),
                (float) c.range().normalize(e.value()[0]));
          };
      g.setColor(color);
      g.fill(new Rectangle2D.Double(x0 + e.key().x() * cW, y0 + e.key().y() * cH, cW, cH));
    });
  }

  @Override
  public ImageInfo imageInfo(Grid<double[]> grid) {
    return new ImageInfo((int) ((1 + c.marginRate()) * grid.w() * c.sizeRate), (int)
        ((1 + c.marginRate()) * grid.h() * c.sizeRate));
  }
}
