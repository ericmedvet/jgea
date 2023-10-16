
package io.github.ericmedvet.jgea.problem.image;

import io.github.ericmedvet.jgea.core.problem.ComparableQualityBasedProblem;
import io.github.ericmedvet.jsdynsym.core.numerical.UnivariateRealFunction;

import java.awt.image.BufferedImage;
import java.util.function.Function;
public class ImageReconstruction implements ComparableQualityBasedProblem<UnivariateRealFunction, Double> {

  private final Function<UnivariateRealFunction, Double> fitnessFunction;

  public ImageReconstruction(BufferedImage image, boolean normalize) {
    fitnessFunction = f -> {
      double err = 0d;
      for (int x = 0; x < image.getWidth(); x++) {
        for (int y = 0; y < image.getHeight(); y++) {
          double fOut = f.applyAsDouble(new double[]{
              (double) x / (double) image.getWidth(),
              (double) y / (double) image.getHeight()
          });
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

  @Override
  public Function<UnivariateRealFunction, Double> qualityFunction() {
    return fitnessFunction;
  }
}
