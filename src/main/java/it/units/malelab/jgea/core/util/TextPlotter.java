package it.units.malelab.jgea.core.util;

import java.util.Arrays;
import java.util.List;
import java.util.stream.DoubleStream;

public class TextPlotter {

  private static final String VERTICAL_PART_FILLER = "▁▂▃▄▅▆▇█";
  private static final String HORIZONTAL_PART_FILLER = "▏▎▍▌▋▊▉█";
  private static final char FILLER = '█';
  private static final char EMPTY = '░';

  public TextPlotter() {
  }

  private static double[] resize(double[] values, int l) {
    double[] resized = new double[l];
    for (int i = 0; i < l; i++) {
      resized[i] = values[Math.min((int) Math.round((double) i / (double) l * (double) values.length), values.length - 1)];
    }
    return resized;
  }

  private static String barplot(double[] values, double min, double max) {
    StringBuilder sb = new StringBuilder();
    for (double value : values) {
      sb.append(VERTICAL_PART_FILLER.charAt((int) Math.round(Math.max(Math.min((value - min) / (max - min), 1d), 0d) * ((double) VERTICAL_PART_FILLER.length() - 1d))));
    }
    return sb.toString();
  }

  public static String barplot(double[] values) {
    double min = DoubleStream.of(values).min().orElse(0);
    double max = DoubleStream.of(values).max().orElse(0);
    return barplot(values, min, max);
  }

  public static String barplot(double[] values, int l) {
    return barplot(resize(values, l));
  }

  public static String barplot(List<? extends Number> values) {
    return barplot(values.stream().mapToDouble(Number::doubleValue).toArray());
  }

  public static String barplot(List<? extends Number> values, int l) {
    return barplot(values.stream().mapToDouble(Number::doubleValue).toArray(), l);
  }

  public static String horizontalBar(double value, double min, double max, int l) {
    StringBuilder sb = new StringBuilder();
    double r = (max - min) / (double) l;
    for (double i = 0; i < l; i++) {
      double localMin = min + r * i;
      double localMax = min + r * (i + 1d);
      if (value < localMin) {
        sb.append(EMPTY);
      } else if (value < localMax) {
        sb.append(HORIZONTAL_PART_FILLER.charAt((int) Math.round(Math.max(Math.min((value - localMin) / r, 1d), 0d) * ((double) VERTICAL_PART_FILLER.length() - 1d))));
      } else {
        sb.append(FILLER);
      }
    }
    return sb.toString();
  }

  public static String histogram(List<? extends Number> values, int bins) {
    double[] vs = values.stream().mapToDouble(Number::doubleValue).toArray();
    double min = DoubleStream.of(vs).min().orElse(0);
    double max = DoubleStream.of(vs).max().orElse(0);
    double[] counts = new double[bins];
    for (double v : vs) {
      int i = Math.min((int) Math.floor((v - min) / (max - min) * (double) bins), bins - 1);
      counts[i] = counts[i] + 1;
    }
    return barplot(counts, 0, Arrays.stream(counts).max().orElse(0));
  }

}
