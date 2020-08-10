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

import it.units.malelab.jgea.problem.symbolicregression.RealFunction;

import java.awt.image.BufferedImage;

/**
 * @author eric
 * @created 2020/08/10
 * @project jgea
 */
public class ImageUtils {
  private ImageUtils() {
  }

  public static BufferedImage render(RealFunction f, int w, int h, boolean normalize) {
    BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
    for (int x = 0; x < w; x++) {
      for (int y = 0; y < h; y++) {
        double fOut = f.apply((double) x / (double) w, (double) y / (double) h);
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
}
