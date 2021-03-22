/*
 * Copyright 2020 Eric Medvet <eric.medvet@gmail.com> (as eric)
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

package it.units.malelab.jgea.problem.image;

import it.units.malelab.jgea.core.Problem;
import it.units.malelab.jgea.problem.symbolicregression.RealFunction;

import java.awt.image.BufferedImage;
import java.util.function.Function;

/**
 * @author eric
 */
public class ImageReconstruction implements Problem<RealFunction, Double> {

  private final BufferedImage image;
  private final boolean normalize;

  public ImageReconstruction(BufferedImage image, boolean normalize) {
    this.image = image;
    this.normalize = normalize;
  }

  @Override
  public Function<RealFunction, Double> getFitnessFunction() {
    return f -> {
      double err = 0d;
      for (int x = 0; x < image.getWidth(); x++) {
        for (int y = 0; y < image.getHeight(); y++) {
          double fOut = f.apply(
              (double) x / (double) image.getWidth(),
              (double) y / (double) image.getHeight()
          );
          if (normalize) {
            fOut = Math.tanh(fOut) / 2d + 0.5d;
          }
          int color = image.getRGB(x, y);
          double red = (color & 0x00ff0000) >> 16;
          double green = (color & 0x0000ff00) >> 8;
          double blue = color & 0x000000ff;
          double imgOut = (red / 256d + green / 256d + blue / 256d) / 3d;
          err = err + (imgOut - fOut) * (imgOut - fOut);
        }
      }
      return err / ((double) (image.getWidth() * image.getHeight()));
    };
  }
}
