
package io.github.ericmedvet.jgea.problem.image;

import io.github.ericmedvet.jsdynsym.core.numerical.UnivariateRealFunction;

import java.awt.image.BufferedImage;
public class ImageUtils {
  private ImageUtils() {
  }

  public static BufferedImage render(UnivariateRealFunction f, int w, int h, boolean normalize) {
    BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
    for (int x = 0; x < w; x++) {
      for (int y = 0; y < h; y++) {
        double fOut = f.applyAsDouble(new double[]{
            (double) x / (double) w,
            (double) y / (double) h
        });
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
