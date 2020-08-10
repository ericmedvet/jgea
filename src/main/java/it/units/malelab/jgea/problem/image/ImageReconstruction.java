/*
 * Copyright (C) 2020 Eric Medvet <eric.medvet@gmail.com> (as eric)
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.units.malelab.jgea.problem.image;

import it.units.malelab.jgea.core.Problem;
import it.units.malelab.jgea.problem.symbolicregression.RealFunction;

import java.awt.image.BufferedImage;
import java.util.function.Function;

/**
 * @author eric
 * @created 2020/08/10
 * @project jgea
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
