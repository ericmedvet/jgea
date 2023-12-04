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
package io.github.ericmedvet.jgea.experimenter.listener;

import io.github.ericmedvet.jgea.core.listener.Accumulator;
import io.github.ericmedvet.jgea.core.listener.AccumulatorFactory;
import io.github.ericmedvet.jgea.core.solver.Individual;
import io.github.ericmedvet.jgea.core.solver.cabea.GridPopulationState;
import io.github.ericmedvet.jgea.core.util.Misc;
import io.github.ericmedvet.jgea.experimenter.Run;
import io.github.ericmedvet.jgea.experimenter.Utils;
import io.github.ericmedvet.jgea.experimenter.listener.plot.ImagePlotter;
import io.github.ericmedvet.jsdynsym.grid.Grid;
import io.github.ericmedvet.mrsim2d.viewer.VideoUtils;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.logging.Logger;

public class HeatMapVideoAccumulatorFactory<G, S, Q>
    implements AccumulatorFactory<GridPopulationState<G, S, Q>, File, Run<?, G, S, Q>> {

  private static final Logger L = Logger.getLogger(HeatMapVideoAccumulatorFactory.class.getName());

  private final Function<Individual<G, S, Q>, Number> quantifier;
  private final int w;
  private final int h;
  private final double frameRate;
  private final Color minColor;
  private final Color maxColor;
  private final Color nullColor;
  private final Color gridColor;
  private final String filePathTemplate;

  public HeatMapVideoAccumulatorFactory(
      Function<Individual<G, S, Q>, Number> quantifier,
      int w,
      int h,
      double frameRate,
      Color minColor,
      Color maxColor,
      Color nullColor,
      Color gridColor,
      String filePathTemplate) {
    this.quantifier = quantifier;
    this.w = w;
    this.h = h;
    this.frameRate = frameRate;
    this.minColor = minColor;
    this.maxColor = maxColor;
    this.nullColor = nullColor;
    this.gridColor = gridColor;
    this.filePathTemplate = filePathTemplate;
  }

  @Override
  public Accumulator<GridPopulationState<G, S, Q>, File> build(Run<?, G, S, Q> run) {
    List<Grid<Number>> grids = new ArrayList<>();
    return new Accumulator<>() {
      @Override
      public File get() {
        // create file
        try {
          File file;
          boolean tempFile = false;
          if (filePathTemplate.isEmpty()) {
            tempFile = true;
            file = File.createTempFile("video", ".mp4");
            file.deleteOnExit();
          } else {
            String fileName = Utils.interpolate(filePathTemplate, run);
            file = Misc.checkExistenceAndChangeName(new File(fileName));
          }
          // do video
          double min = grids.stream()
              .mapToDouble(g -> g.values().stream()
                  .filter(Objects::nonNull)
                  .mapToDouble(Number::doubleValue)
                  .min()
                  .orElse(0d))
              .min()
              .orElse(0d);
          double max = grids.stream()
              .mapToDouble(g -> g.values().stream()
                  .filter(Objects::nonNull)
                  .mapToDouble(Number::doubleValue)
                  .max()
                  .orElse(0d))
              .max()
              .orElse(0d);
          Function<Grid<? extends Number>, BufferedImage> plotter =
              ImagePlotter.heatMap(w, h, minColor, maxColor, nullColor, gridColor, min, max);
          L.info("Doing video for run %d on file %s"
              .formatted(run.index(), tempFile ? "temp" : file.getAbsolutePath()));
          VideoUtils.encodeAndSave(grids.stream().map(plotter).toList(), frameRate, file);
          L.info("Video done for run %d on file %s"
              .formatted(run.index(), tempFile ? "temp" : file.getAbsolutePath()));
          // return file
          return file;
        } catch (IOException e) {
          L.warning("Cannot create file: %s".formatted(e));
          return null;
        }
      }

      @Override
      public void listen(GridPopulationState<G, S, Q> state) {
        grids.add(state.gridPopulation().map(quantifier));
      }
    };
  }
}
