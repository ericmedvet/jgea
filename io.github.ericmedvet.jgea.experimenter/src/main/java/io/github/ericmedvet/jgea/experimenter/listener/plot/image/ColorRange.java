/*-
 * ========================LICENSE_START=================================
 * jgea-experimenter
 * %%
 * Copyright (C) 2018 - 2023 Eric Medvet
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
package io.github.ericmedvet.jgea.experimenter.listener.plot.image;

import io.github.ericmedvet.jsdynsym.core.DoubleRange;
import java.awt.*;
import java.util.Arrays;
import java.util.List;

/**
 * @author "Eric Medvet" on 2023/12/29 for jgea
 */
public record ColorRange(List<Color> colors) {

  public ColorRange(Color... colors) {
    this(Arrays.stream(colors).toList());
  }

  public Color interpolate(double v) {
    int i = (int) Math.floor(new DoubleRange(0, colors.size() - 1).denormalize(v));
    double extent = 1d / (colors.size() - 1d);
    double minV = extent * (double) i;
    double maxV = extent * (i + 1d);
    Color min = colors.get(Math.min(colors().size() - 1, i));
    Color max = colors.get(Math.min(colors().size() - 1, i + 1));
    return interpolate(new DoubleRange(minV, maxV).normalize(v), min, max);
  }

  public static Color interpolate(double v, Color min, Color max) {
    v = DoubleRange.UNIT.clip(v);
    double minR = min.getRed() / 255f;
    double maxR = max.getRed() / 255f;
    double minG = min.getGreen() / 255f;
    double maxG = max.getGreen() / 255f;
    double minB = min.getBlue() / 255f;
    double maxB = max.getBlue() / 255f;
    return new Color((float) (minR + (maxR - minR) * v), (float) (minG + (maxG - minG) * v), (float)
        (minB + (maxB - minB) * v));
  }
}
