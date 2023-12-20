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
/*
 * Copyright 2023 eric
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

package io.github.ericmedvet.jgea.experimenter.listener.plot;

import io.github.ericmedvet.jgea.core.util.Misc;
import io.github.ericmedvet.jsdynsym.grid.Grid;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.logging.Logger;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

public class CsvPlotter implements Plotter<File> {

  private static final Logger L = Logger.getLogger(CsvPlotter.class.getName());

  private final File file;

  public CsvPlotter(File file) {
    this.file = file;
  }

  @Override
  public File plot(XYPlot<?> plot) {
    if (plot instanceof XYDataSeriesPlot xyDataSeriesPlot) {
      File actualFile = Misc.checkExistenceAndChangeName(file);
      try (CSVPrinter csvPrinter = new CSVPrinter(
          new PrintStream(actualFile),
          CSVFormat.Builder.create().setDelimiter(";").build())) {
        csvPrinter.printRecord(List.of(
            xyDataSeriesPlot.xTitleName(),
            xyDataSeriesPlot.yTitleName(),
            "series",
            xyDataSeriesPlot.xName() + "[min]",
            xyDataSeriesPlot.xName(),
            xyDataSeriesPlot.xName() + "[max]",
            xyDataSeriesPlot.yName() + "[min]",
            xyDataSeriesPlot.yName(),
            xyDataSeriesPlot.yName() + "[max]"));
        for (XYPlot.TitledData<List<XYDataSeries>> td :
            xyDataSeriesPlot.dataGrid().values()) {
          for (XYDataSeries ds : td.data()) {
            for (XYDataSeries.Point p : ds.points()) {
              csvPrinter.printRecord(List.of(
                  td.xTitle(),
                  td.yTitle(),
                  ds.name(),
                  RangedValue.range(p.x()).min(),
                  p.x().v(),
                  RangedValue.range(p.x()).max(),
                  RangedValue.range(p.y()).min(),
                  p.y().v(),
                  RangedValue.range(p.y()).max()));
            }
          }
        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      return actualFile;
    }
    if (plot instanceof UnivariateGridPlot univariateGridPlot) {
      File actualFile = Misc.checkExistenceAndChangeName(file);
      try (CSVPrinter csvPrinter = new CSVPrinter(
          new PrintStream(actualFile),
          CSVFormat.Builder.create().setDelimiter(";").build())) {
        csvPrinter.printRecord(
            List.of(univariateGridPlot.xTitleName(), univariateGridPlot.yTitleName(), "x", "y", "v"));
        for (XYPlot.TitledData<Grid<Double>> td :
            univariateGridPlot.dataGrid().values()) {
          for (Grid.Entry<Double> e : td.data()) {
            if (e.value() != null) {
              csvPrinter.printRecord(List.of(
                  td.xTitle(),
                  td.yTitle(),
                  e.key().x(),
                  e.key().y(),
                  e.value()));
            }
          }
        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      return actualFile;
    }
    L.warning("Unknown type of plot: %s".formatted(plot.getClass().getSimpleName()));
    return null;
  }
}
