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
/*
 * Copyright 2024 eric
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.ericmedvet.jgea.experimenter.drawer;

import io.github.ericmedvet.jnb.datastructure.Grid;
import io.github.ericmedvet.jviz.core.drawer.Drawer;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PolyominoDrawer implements Drawer<Grid<Character>> {

  private final Configuration configuration;

  public PolyominoDrawer(Configuration configuration) {
    this.configuration = configuration;
  }

  public record Configuration(
      Integer maxW, Integer maxH, Map<Character, Color> colors, Color borderColor, double marginRate) {

    private static final char[] ALPHABET = "abcdefghilmnopqrstuvz".toCharArray();
    public static final Configuration DEFAULT = new Configuration(
        null,
        null,
        IntStream.range(0, ALPHABET.length)
            .boxed()
            .collect(Collectors.toMap(
                i -> ALPHABET[i],
                i -> io.github.ericmedvet.jviz.core.plot.image.Configuration.Colors.DEFAULT
                    .dataColors()
                    .get(i
                        % io.github.ericmedvet.jviz.core.plot.image.Configuration.Colors.DEFAULT
                            .dataColors()
                            .size()))),
        Color.BLACK,
        0.05);
  }

  @Override
  public void draw(Graphics2D g, Grid<Character> grid) {
    double w = configuration.maxW == null ? grid.w() : configuration.maxW;
    double h = configuration.maxH == null ? grid.h() : configuration.maxH;
    double cW = g.getClipBounds().getWidth() * (1d - 2 * configuration.marginRate) / w;
    double cH = g.getClipBounds().getHeight() * (1d - 2 * configuration.marginRate) / h;
    double cS = Math.min(cW, cH);
    double x0 = g.getClipBounds().getMinX() + g.getClipBounds().getWidth() * configuration.marginRate;
    double y0 = g.getClipBounds().getMinY() + g.getClipBounds().getHeight() * configuration.marginRate;
    grid.entries().stream().filter(e -> e.value() != null).forEach(e -> {
      Color fillColor = configuration.colors.get(e.value());
      if (fillColor != null) {
        g.setColor(fillColor);
        g.fill(new Rectangle2D.Double(
            x0 + e.key().x() * cS, y0 + e.key().y() * cS, cS, cS));
      }
      g.setColor(configuration.borderColor);
      g.draw(new Rectangle2D.Double(x0 + e.key().x() * cS, y0 + e.key().y() * cS, cS, cS));
    });
  }
}
