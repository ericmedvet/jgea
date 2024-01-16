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

package io.github.ericmedvet.jgea.experimenter.listener.plot;

import io.github.ericmedvet.jgea.core.util.HashMapTable;
import io.github.ericmedvet.jgea.core.util.Misc;
import io.github.ericmedvet.jgea.core.util.Table;
import io.github.ericmedvet.jsdynsym.grid.Grid;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

public class CsvPlotter implements Plotter<List<File>> {

  private static final Logger L = Logger.getLogger(CsvPlotter.class.getName());
  private final File file;
  private final Mode mode;
  private final Configuration c;

  public CsvPlotter(File file, Mode mode, Configuration configuration) {
    this.file = file;
    this.mode = mode;
    this.c = configuration;
  }

  public CsvPlotter(File file, Mode mode) {
    this(file, mode, Configuration.DEFAULTS.get(mode));
  }

  public enum Mode {
    NONE,
    NORMAL,
    PAPER_FRIENDLY
  }

  public record Configuration(
      String columnNameJoiner,
      String doubleFormat,
      String delimiter,
      List<Replacement> replacements,
      String missingDataString) {
    public static final Map<Mode, Configuration> DEFAULTS = Map.ofEntries(
        Map.entry(Mode.NORMAL, new Configuration(".", "%f", ";", List.of(), "")),
        Map.entry(
            Mode.PAPER_FRIENDLY,
            new Configuration(".", "%.3e", "\t", List.of(new Replacement("\\W+", ".")), "nan")));

    public record Replacement(String regex, String replacement) {}

    public CSVFormat getCSVFormat() {
      return CSVFormat.DEFAULT.builder().setDelimiter(delimiter).build();
    }
  }

  public static void main(String[] args) {
    double v1 = Math.PI / 10000d;
    double v2 = 1.5d;
    System.out.printf("%.3f %.3f%n", v1, v2);
    System.out.printf("%.3e %.3e%n", v1, v2);
  }

  @Override
  public List<File> boxplot(DistributionPlot p) {
    if (mode.equals(Mode.NONE)) {
      return List.of();
    }
    File actualFile = Misc.checkExistenceAndChangeName(file);
    try (CSVPrinter csvPrinter = new CSVPrinter(new PrintStream(actualFile), c.getCSVFormat())) {
      if (mode.equals(Mode.NORMAL)) {
        csvPrinter.printRecord(processRecord(List.of(
            p.xTitleName(),
            p.yTitleName(),
            p.xName(),
            String.join(c.columnNameJoiner, p.yName(), "min"),
            String.join(c.columnNameJoiner, p.yName(), "q1minus15IQR"),
            String.join(c.columnNameJoiner, p.yName(), "q1"),
            String.join(c.columnNameJoiner, p.yName(), "mean"),
            String.join(c.columnNameJoiner, p.yName(), "median"),
            String.join(c.columnNameJoiner, p.yName(), "q3"),
            String.join(c.columnNameJoiner, p.yName(), "q3plus15IQR"),
            String.join(c.columnNameJoiner, p.yName(), "max"))));
        for (XYPlot.TitledData<List<DistributionPlot.Data>> td :
            p.dataGrid().values()) {
          for (DistributionPlot.Data ds : td.data()) {
            csvPrinter.printRecord(processRecord(List.of(
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
                ds.stats().max())));
          }
        }
        return List.of(actualFile);
      }
      if (mode.equals(Mode.PAPER_FRIENDLY)) {
        Table<Integer, String, Number> t = new HashMapTable<>();
        for (XYPlot.TitledData<List<DistributionPlot.Data>> td :
            p.dataGrid().values()) {
          for (DistributionPlot.Data ds : td.data()) {
            for (int i = 0; i < ds.yValues().size(); i++) {
              t.set(
                  i,
                  String.join(c.columnNameJoiner, List.of(td.xTitle(), td.yTitle(), ds.name())),
                  ds.yValues().get(i));
            }
          }
        }
        csvPrinter.printRecord(processRecord(t.colIndexes()));
        for (int i : t.rowIndexes()) {
          csvPrinter.printRecord(processRecord(t.rowValues(i)));
        }
        return List.of(actualFile);
      }
    } catch (IOException e) {
      L.warning("Cannot save csv to '%s': %s".formatted(file, e));
      throw new RuntimeException(e);
    }
    return List.of();
  }

  @Override
  public List<File> landscape(LandscapePlot plot) {
    return points(plot.toXYDataSeriesPlot());
  }

  @Override
  public List<File> lines(XYDataSeriesPlot plot) {
    return xyDataSeries(plot);
  }

  @Override
  public List<File> points(XYDataSeriesPlot plot) {
    return xyDataSeries(plot);
  }

  @Override
  public List<File> univariateGrid(UnivariateGridPlot p) {
    if (mode.equals(Mode.NONE)) {
      return List.of();
    }
    File actualFile = Misc.checkExistenceAndChangeName(file);
    try (CSVPrinter csvPrinter = new CSVPrinter(new PrintStream(actualFile), c.getCSVFormat())) {
      if (mode.equals(Mode.NORMAL)) {
        csvPrinter.printRecord(processRecord(List.of(p.xTitleName(), p.yTitleName(), "x", "y", "v")));
        for (XYPlot.TitledData<Grid<Double>> td : p.dataGrid().values()) {
          for (Grid.Entry<Double> e : td.data()) {
            if (e.value() != null) {
              csvPrinter.printRecord(processRecord(processRecord(List.of(
                  td.xTitle(),
                  td.yTitle(),
                  e.key().x(),
                  e.key().y(),
                  e.value()))));
            }
          }
        }
        return List.of(actualFile);
      }
      if (mode.equals(Mode.PAPER_FRIENDLY)) {
        Table<Grid.Key, String, Number> t = new HashMapTable<>();
        for (XYPlot.TitledData<Grid<Double>> td : p.dataGrid().values()) {
          for (Grid.Entry<Double> e : td.data()) {
            t.set(e.key(), String.join(c.columnNameJoiner, List.of(td.xTitle(), td.yTitle())), e.value());
          }
        }
        csvPrinter.printRecord(processRecord(Misc.concat(List.of(List.of("x", "y"), t.colIndexes()))));
        for (Grid.Key k : t.rowIndexes()) {
          csvPrinter.printRecord(processRecord(Misc.concat(List.of(List.of(k.x(), k.y()), t.rowValues(k)))));
        }
        return List.of(actualFile);
      }
    } catch (IOException e) {
      L.warning("Cannot save csv to '%s': %s".formatted(file, e));
      throw new RuntimeException(e);
    }
    return List.of();
  }

  private List<Object> processRecord(List<? extends Object> record) {
    return record.stream()
        .map(o -> {
          if (o == null) {
            return c.missingDataString;
          }
          if (o instanceof Double d) {
            return c.doubleFormat.formatted(d);
          }
          if (o instanceof String s) {
            for (Configuration.Replacement replacement : c.replacements) {
              s = s.replaceAll(replacement.regex, replacement.replacement);
            }
            return s;
          }
          return o;
        })
        .toList();
  }

  private List<File> xyDataSeries(XYDataSeriesPlot p) {
    if (mode.equals(Mode.NONE)) {
      return List.of();
    }
    File actualFile = Misc.checkExistenceAndChangeName(file);
    try (CSVPrinter csvPrinter = new CSVPrinter(new PrintStream(actualFile), c.getCSVFormat())) {
      if (mode.equals(Mode.NORMAL)) {
        csvPrinter.printRecord(processRecord(List.of(
            p.xTitleName(),
            p.yTitleName(),
            "series",
            String.join(c.columnNameJoiner, p.xName(), "min"),
            p.xName(),
            String.join(c.columnNameJoiner, p.xName(), "max"),
            String.join(c.columnNameJoiner, p.yName(), "min"),
            p.yName(),
            String.join(c.columnNameJoiner, p.yName(), "max"))));
        for (XYPlot.TitledData<List<XYDataSeries>> td : p.dataGrid().values()) {
          for (XYDataSeries ds : td.data()) {
            for (XYDataSeries.Point point : ds.points()) {
              csvPrinter.printRecord(processRecord(List.of(
                  td.xTitle(),
                  td.yTitle(),
                  ds.name(),
                  RangedValue.range(point.x()).min(),
                  point.x().v(),
                  RangedValue.range(point.x()).max(),
                  RangedValue.range(point.y()).min(),
                  point.y().v(),
                  RangedValue.range(point.y()).max())));
            }
          }
        }
        return List.of(actualFile);
      }
      if (mode.equals(Mode.PAPER_FRIENDLY)) {
        Table<Number, String, Number> t = new HashMapTable<>();
        for (XYPlot.TitledData<List<XYDataSeries>> td : p.dataGrid().values()) {
          for (XYDataSeries ds : td.data()) {
            for (XYDataSeries.Point point : ds.points()) {
              t.set(
                  point.x().v(),
                  String.join(
                      c.columnNameJoiner,
                      List.of(td.xTitle(), td.yTitle(), ds.name(), p.yName())),
                  point.y().v());
              t.set(
                  point.x().v(),
                  String.join(
                      c.columnNameJoiner,
                      List.of(td.xTitle(), td.yTitle(), ds.name(), p.yName(), "min")),
                  RangedValue.range(point.y()).min());
              t.set(
                  point.x().v(),
                  String.join(
                      c.columnNameJoiner,
                      List.of(td.xTitle(), td.yTitle(), ds.name(), p.yName(), "max")),
                  RangedValue.range(point.y()).max());
            }
          }
        }
        csvPrinter.printRecord(processRecord(Misc.concat(List.of(List.of(p.xName()), t.colIndexes()))));
        for (Number x : t.rowIndexes()) {
          csvPrinter.printRecord(processRecord(Misc.concat(List.of(List.of(x), t.rowValues(x)))));
        }
        return List.of(actualFile);
      }
    } catch (IOException e) {
      L.warning("Cannot save csv to '%s': %s".formatted(file, e));
      throw new RuntimeException(e);
    }
    return List.of();
  }
}
