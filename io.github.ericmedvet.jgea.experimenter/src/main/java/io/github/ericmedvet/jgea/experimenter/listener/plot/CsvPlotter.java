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
  public File boxplot(DistributionPlot p) {
    File actualFile = Misc.checkExistenceAndChangeName(file);
    try (CSVPrinter csvPrinter = new CSVPrinter(
        new PrintStream(actualFile),
        CSVFormat.Builder.create().setDelimiter(";").build())) {
      csvPrinter.printRecord(List.of(
          p.xTitleName(),
          p.yTitleName(),
          "series",
          p.xName(),
          p.yName() + "[min]",
          p.yName() + "[q1minus15IQR]",
          p.yName() + "[q1]",
          p.yName() + "[mean]",
          p.yName() + "[median]",
          p.yName() + "[q3]",
          p.yName() + "[q3plus15IQR]",
          p.yName() + "[max]"));
      for (XYPlot.TitledData<List<DistributionPlot.Data>> td :
          p.dataGrid().values()) {
        for (DistributionPlot.Data ds : td.data()) {
          csvPrinter.printRecord(List.of(
              td.xTitle(),
              td.yTitle(),
              ds.name(),
              ds.stats().min(),
              ds.stats().q1minus15IQR(),
              ds.stats().q1(),
              ds.stats().mean(),
              ds.stats().median(),
              ds.stats().q3(),
              ds.stats().q3plus15IQR(),
              ds.stats().max()));
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return actualFile;
  }

  @Override
  public File lines(XYDataSeriesPlot plot) {
    return xyDataSeries(plot);
  }

  @Override
  public File points(XYDataSeriesPlot plot) {
    return xyDataSeries(plot);
  }

  @Override
  public File univariateGrid(UnivariateGridPlot p) {
    File actualFile = Misc.checkExistenceAndChangeName(file);
    try (CSVPrinter csvPrinter = new CSVPrinter(
        new PrintStream(actualFile),
        CSVFormat.Builder.create().setDelimiter(";").build())) {
      csvPrinter.printRecord(List.of(p.xTitleName(), p.yTitleName(), "x", "y", "v"));
      for (XYPlot.TitledData<Grid<Double>> td : p.dataGrid().values()) {
        for (Grid.Entry<Double> e : td.data()) {
          if (e.value() != null) {
            csvPrinter.printRecord(List.of(
                td.xTitle(), td.yTitle(), e.key().x(), e.key().y(), e.value()));
          }
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return actualFile;
  }

  @Override
  public File landscape(LandscapePlot plot) {
    return points(plot.toXYDataSeriesPlot());
  }

  private File xyDataSeries(XYDataSeriesPlot p) {
    File actualFile = Misc.checkExistenceAndChangeName(file);
    try (CSVPrinter csvPrinter = new CSVPrinter(
        new PrintStream(actualFile),
        CSVFormat.Builder.create().setDelimiter(";").build())) {
      csvPrinter.printRecord(List.of(
          p.xTitleName(),
          p.yTitleName(),
          "series",
          p.xName() + "[min]",
          p.xName(),
          p.xName() + "[max]",
          p.yName() + "[min]",
          p.yName(),
          p.yName() + "[max]"));
      for (XYPlot.TitledData<List<XYDataSeries>> td : p.dataGrid().values()) {
        for (XYDataSeries ds : td.data()) {
          for (XYDataSeries.Point point : ds.points()) {
            csvPrinter.printRecord(List.of(
                td.xTitle(),
                td.yTitle(),
                ds.name(),
                RangedValue.range(point.x()).min(),
                point.x().v(),
                RangedValue.range(point.x()).max(),
                RangedValue.range(point.y()).min(),
                point.y().v(),
                RangedValue.range(point.y()).max()));
          }
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return actualFile;
  }
}
