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
package io.github.ericmedvet.jgea.experimenter.listener.plot.video;

import io.github.ericmedvet.jgea.core.util.Misc;
import io.github.ericmedvet.jgea.experimenter.listener.plot.*;
import io.github.ericmedvet.jgea.experimenter.listener.plot.image.ImagePlotter;
import io.github.ericmedvet.jsdynsym.grid.Grid;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.IntStream;

/**
 * @author "Eric Medvet" on 2024/02/14 for jgea
 */
public class VideoPlotter implements Plotter<List<File>> {

  private static final Logger L = Logger.getLogger(VideoPlotter.class.getName());

  private final File file;
  private final ImagePlotter imagePlotter;
  private final Configuration configuration;

  public VideoPlotter(File file, ImagePlotter imagePlotter, Configuration configuration) {
    this.file = file;
    this.imagePlotter = imagePlotter;
    this.configuration = configuration;
  }

  public VideoPlotter(File file, ImagePlotter imagePlotter) {
    this(file, imagePlotter, new Configuration(SplitType.COLUMNS, VideoUtils.EncoderFacility.JCODEC, 30));
  }

  public record Configuration(SplitType splitType, VideoUtils.EncoderFacility encoderFacility, double frameRate) {
    public static final Configuration DEFAULT =
        new Configuration(SplitType.COLUMNS, VideoUtils.EncoderFacility.JCODEC, 20d);
  }

  public enum SplitType {
    ROWS,
    COLUMNS
  }

  private <P extends XYPlot<D>, D> List<File> plot(P plot, Function<P, BufferedImage> framePlotter) {
    List<BufferedImage> images =
        split(plot, configuration.splitType).stream().map(framePlotter).toList();
    File actualFile = Misc.checkExistenceAndChangeName(file);
    try {
      VideoUtils.encodeAndSave(images, configuration.frameRate, actualFile, configuration.encoderFacility);
    } catch (IOException e) {
      L.warning("Cannot save csv to '%s': %s".formatted(file, e));
      throw new RuntimeException(e);
    }
    return List.of(actualFile);
  }

  @Override
  public List<File> boxplot(DistributionPlot plot) {
    return plot(plot, imagePlotter::boxplot);
  }

  @Override
  public List<File> landscape(LandscapePlot plot) {
    return plot(plot, imagePlotter::landscape);
  }

  @Override
  public List<File> lines(XYDataSeriesPlot plot) {
    return plot(plot, imagePlotter::lines);
  }

  @Override
  public List<File> points(XYDataSeriesPlot plot) {
    return plot(plot, imagePlotter::points);
  }

  @Override
  public List<File> univariateGrid(UnivariateGridPlot plot) {
    return plot(plot, imagePlotter::univariateGrid);
  }

  private static <T> List<Grid<T>> split(Grid<T> grid, SplitType type) {
    return switch (type) {
      case ROWS -> IntStream.range(0, grid.h())
          .mapToObj(y0 -> Grid.create(grid.w(), 1, (x, y) -> grid.get(x, y0)))
          .toList();
      case COLUMNS -> IntStream.range(0, grid.w())
          .mapToObj(x0 -> Grid.create(1, grid.h(), (x, y) -> grid.get(x0, y)))
          .toList();
    };
  }

  private static <P extends XYPlot<D>, D> List<P> split(P plot, SplitType type) {
    if (plot instanceof DistributionPlot p) {
      //noinspection unchecked
      return (List<P>) split(p.dataGrid(), type).stream()
          .map(dg -> new DistributionPlot(
              p.title(), p.xTitleName(), p.yTitleName(), p.xName(), p.yName(), p.yRange(), dg))
          .toList();
    }
    if (plot instanceof LandscapePlot p) {
      //noinspection unchecked
      return (List<P>) split(p.dataGrid(), type).stream()
          .map(dg -> new LandscapePlot(
              p.title(),
              p.xTitleName(),
              p.yTitleName(),
              p.xName(),
              p.yName(),
              p.xRange(),
              p.yRange(),
              p.valueRange(),
              dg))
          .toList();
    }
    if (plot instanceof XYDataSeriesPlot p) {
      //noinspection unchecked
      return (List<P>) split(p.dataGrid(), type).stream()
          .map(dg -> new XYDataSeriesPlot(
              p.title(),
              p.xTitleName(),
              p.yTitleName(),
              p.xName(),
              p.yName(),
              p.xRange(),
              p.yRange(),
              dg))
          .toList();
    }
    if (plot instanceof UnivariateGridPlot p) {
      //noinspection unchecked
      return (List<P>) split(p.dataGrid(), type).stream()
          .map(dg -> new UnivariateGridPlot(
              p.title(),
              p.xTitleName(),
              p.yTitleName(),
              p.xName(),
              p.yName(),
              p.xRange(),
              p.yRange(),
              p.valueRange(),
              dg))
          .toList();
    }
    throw new UnsupportedOperationException(
        "Cannot split plot of type %s".formatted(plot.getClass().getSimpleName()));
  }
}
