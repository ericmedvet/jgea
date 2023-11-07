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

package io.github.ericmedvet.jgea.experimenter.listener.decoupled;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import io.github.ericmedvet.jgea.core.util.HashMapTable;
import io.github.ericmedvet.jgea.core.util.Pair;
import io.github.ericmedvet.jgea.core.util.Table;
import io.github.ericmedvet.jgea.experimenter.listener.tui.ListLogHandler;
import io.github.ericmedvet.jgea.experimenter.listener.tui.table.*;
import io.github.ericmedvet.jgea.experimenter.listener.tui.util.TuiDrawer;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class TuiMonitor extends ListLogHandler implements Runnable {

  public static final Map<Level, TextColor> LEVEL_COLORS = Map.ofEntries(
      Map.entry(Level.SEVERE, TextColor.Factory.fromString("#EE3E38")),
      Map.entry(Level.WARNING, TextColor.Factory.fromString("#FBA465")),
      Map.entry(Level.INFO, TextColor.Factory.fromString("#D8E46B")),
      Map.entry(Level.CONFIG, TextColor.Factory.fromString("#6D8700")));
  public static final String EMPTY_CELL_CONTENT = "-";
  public static final int PROGRESS_BAR_LENGTH = 5;
  private static final Logger L = Logger.getLogger(TuiMonitor.class.getName());
  private static final Configuration DEFAULT_CONFIGURATION =
      new Configuration(0.5f, 0.85f, 0.5f, 0.5f, 8, 12, 500, 60, 10, 10000, 2, 5, 20);
  private static final String STATUS_STRING = "⬤";
  private static final DateTimeFormatter SAME_DAY_DATETIME_FORMAT =
      DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault());
  private static final DateTimeFormatter COMPLETE_DATETIME_FORMAT =
      DateTimeFormatter.ofPattern("MM-dd HH:mm").withZone(ZoneId.systemDefault());
  private static final String LEVEL_FORMAT = "%4.4s";
  private static final String DATETIME_FORMAT = "%1$tm-%1$td %1$tH:%1$tM:%1$tS";

  private static final String VALUE_NAME = "VALUE";

  private final Configuration configuration;
  private final ScheduledExecutorService uiExecutorService;

  private Screen screen;
  private boolean isRunning;

  private final Source<MachineKey, MachineInfo> machineSource;
  private final Source<ProcessKey, ProcessInfo> processSource;
  private final Source<ProcessKey, LogInfo> logSource;
  private final Source<ExperimentKey, ExperimentInfo> experimentSource;
  private final Source<RunKey, RunInfo> runSource;
  private final Source<DataItemKey, DataItemInfo> dataItemSource;
  private final Table<Pair<LocalDateTime, MachineKey>, String, MachineInfo> machineTable;
  private final Table<Pair<LocalDateTime, ProcessKey>, String, ProcessInfo> processTable;
  private final Table<Pair<LocalDateTime, ProcessKey>, String, LogInfo> logTable;
  private final Table<Pair<LocalDateTime, ExperimentKey>, String, ExperimentInfo> experimentTable;
  private final Table<Pair<LocalDateTime, RunKey>, String, RunInfo> runTable;
  private final Table<Pair<LocalDateTime, DataItemKey>, String, DataItemInfo> dataItemTable;
  private LocalDateTime lastRefreshLocalDateTime = LocalDateTime.MIN;

  public TuiMonitor(
      Source<MachineKey, MachineInfo> machineSource,
      Source<ProcessKey, ProcessInfo> processSource,
      Source<ProcessKey, LogInfo> logSource,
      Source<ExperimentKey, ExperimentInfo> experimentSource,
      Source<RunKey, RunInfo> runSource,
      Source<DataItemKey, DataItemInfo> dataItemSource) {

    this(
        DEFAULT_CONFIGURATION,
        machineSource,
        processSource,
        logSource,
        experimentSource,
        runSource,
        dataItemSource);
  }

  public TuiMonitor(
      Configuration configuration,
      Source<MachineKey, MachineInfo> machineSource,
      Source<ProcessKey, ProcessInfo> processSource,
      Source<ProcessKey, LogInfo> logSource,
      Source<ExperimentKey, ExperimentInfo> experimentSource,
      Source<RunKey, RunInfo> runSource,
      Source<DataItemKey, DataItemInfo> dataItemSource) {
    super(true);
    this.configuration = configuration;
    this.machineSource = machineSource;
    this.processSource = processSource;
    this.logSource = logSource;
    this.experimentSource = experimentSource;
    this.runSource = runSource;
    this.dataItemSource = dataItemSource;
    uiExecutorService = Executors.newSingleThreadScheduledExecutor();
    machineTable = new HashMapTable<>();
    processTable = new HashMapTable<>();
    logTable = new HashMapTable<>();
    experimentTable = new HashMapTable<>();
    runTable = new HashMapTable<>();
    dataItemTable = new HashMapTable<>();
    // prepare screen
    DefaultTerminalFactory defaultTerminalFactory = new DefaultTerminalFactory();
    try {
      screen = defaultTerminalFactory.createScreen();
      screen.startScreen();
    } catch (IOException e) {
      L.severe(String.format("Cannot create or start screen: %s", e));
      System.exit(-1);
    }
    if (screen != null) {
      screen.setCursorPosition(null);
    }
    // set default locale
    Locale.setDefault(Locale.ENGLISH);
  }

  private enum TimeStatus {
    OK(TextColor.Factory.fromString("#10A010")),
    LATE(TextColor.Factory.fromString("#FBA465")),
    LATER(TextColor.Factory.fromString("#EE3E38")),
    MISSING(TextColor.Factory.fromString("#AAAAAA")),
    PURGE(TextColor.Factory.fromString("#777777"));
    private final TextColor color;

    TimeStatus(TextColor color) {
      this.color = color;
    }

    public TextColor getColor() {
      return color;
    }
  }

  public enum Trend {
    NONE(' ', TextColor.Factory.fromString("#000000")),
    INCREASING('↑', TextColor.Factory.fromString("#22EE22")),
    SAME('=', TextColor.Factory.fromString("#666666")),
    DECREASING('↓', TextColor.Factory.fromString("#EE2222"));

    private final char string;
    private final TextColor color;

    Trend(char string, TextColor color) {
      this.string = string;
      this.color = color;
    }

    public static Trend from(double v1, double v2) {
      if (v1 < v2) {
        return Trend.DECREASING;
      }
      if (v1 > v2) {
        return Trend.INCREASING;
      }
      return Trend.SAME;
    }

    public ColoredStringCell cell() {
      return new ColoredStringCell(String.valueOf(string), color);
    }

    public char getString() {
      return string;
    }
  }

  @FunctionalInterface
  private interface TriConsumer<I1, I2, I3> {
    void accept(I1 i1, I2 i2, I3 i3);
  }

  public record Configuration(
      float runsSplit,
      float logsSplit,
      float legendSplit,
      float machinesProcessesSplit,
      int barLength,
      int areaPlotLength,
      int uiRefreshIntervalMillis,
      int machineHistorySeconds,
      int runDataHistorySize,
      int runPlotHistorySize,
      double laterThreshold,
      double missingThreshold,
      double purgeThreshold) {}

  private static <T> List<T> concatAndTrim(List<T> ts1, List<T> ts2, int n) {
    List<T> ts = Stream.of(ts1, ts2).flatMap(List::stream).toList();
    if (ts.size() <= n) {
      return ts;
    }
    return ts.subList(ts.size() - n, ts.size());
  }

  private static String eta(Instant eta) {
    if (eta.equals(Instant.MAX)) {
      return EMPTY_CELL_CONTENT;
    }
    if (!eta.truncatedTo(ChronoUnit.DAYS).equals(Instant.now().truncatedTo(ChronoUnit.DAYS))) {
      return COMPLETE_DATETIME_FORMAT.format(eta);
    }
    return SAME_DAY_DATETIME_FORMAT.format(eta);
  }

  private static <T> T last(List<T> ts) {
    return ts.get(ts.size() - 1);
  }

  private void stop() {
    isRunning = false;
    try {
      screen.stopScreen();
    } catch (IOException e) {
      L.warning(String.format("Cannot stop screen: %s", e));
    }
    close();
    uiExecutorService.shutdownNow();
  }

  private void refreshTables() {
    machineSource.pull(lastRefreshLocalDateTime).forEach((p, v) -> machineTable.set(p, VALUE_NAME, v));
    processSource.pull(lastRefreshLocalDateTime).forEach((p, v) -> processTable.set(p, VALUE_NAME, v));
    logSource.pull(lastRefreshLocalDateTime).forEach((p, v) -> logTable.set(p, VALUE_NAME, v));
    experimentSource.pull(lastRefreshLocalDateTime).forEach((p, v) -> experimentTable.set(p, VALUE_NAME, v));
    runSource.pull(lastRefreshLocalDateTime).forEach((p, v) -> runTable.set(p, VALUE_NAME, v));
    dataItemSource.pull(lastRefreshLocalDateTime).forEach((p, v) -> dataItemTable.set(p, VALUE_NAME, v));
    lastRefreshLocalDateTime = LocalDateTime.now();
    // TODO prune appropriately here
  }

  private static <K, V> void prune(Table<Pair<LocalDateTime, K>, String, V> table, int n) {
    List<Pair<LocalDateTime, K>> toRemovePs = table.rowIndexes().stream()
        .map(Pair::second)
        .distinct()
        .map(k -> table.rowIndexes().stream()
            .filter(p -> p.second().equals(k))
            .sorted((p1, p2) -> p2.first().compareTo(p1.first()))
            .skip(n)
            .toList())
        .flatMap(List::stream)
        .toList();
    toRemovePs.forEach(table::removeRow);
  }

  @Override
  public void run() {
    // start painter task
    uiExecutorService.scheduleAtFixedRate(
        () -> {
          try {
            updateUI();
          } catch (RuntimeException e) {
            L.warning("Unexpected exception: %s".formatted(e));
            e.printStackTrace(); // TODO remove
          }
        },
        0,
        configuration.uiRefreshIntervalMillis,
        TimeUnit.MILLISECONDS);
    isRunning = true;
  }

  private synchronized void updateUI() {
    refreshTables();
    List<MachineKey> machineKeys =
        machineTable.rowIndexes().stream().map(Pair::second).distinct().toList();
    List<ExperimentKey> experimentsKeys = experimentTable.rowIndexes().stream()
        .map(Pair::second)
        .distinct()
        .toList();

    Table<MachineKey, String, ? extends Cell> machines = machineTable
        .aggregateByIndexSingle(Pair::second, Comparator.comparing(Pair::first), vs -> vs)
        .expandColumn(
            VALUE_NAME,
            vs -> Map.ofEntries(
                Map.entry("Name", new StringCell(last(vs).machineName())),
                Map.entry("CPUs", new NumericCell(last(vs).numberOfProcessors(), "%d").rightAligned()),
                Map.entry(
                    "Load",
                    new TrendedNumericCell<>(
                            vs.stream()
                                .map(MachineInfo::cpuLoad)
                                .toList(),
                            "%.1f")
                        .rightAligned()),
                Map.entry(
                    "~Load",
                    new AreaPlotCell(
                        8,
                        vs.stream()
                            .map(MachineInfo::cpuLoad)
                            .toList()))))
        .remapRowIndex(Pair::second);
    Table<MachineKey, String, ? extends Cell> pCells = processTable
        .aggregateByIndexSingle(Pair::second, Comparator.comparing(Pair::first), TuiMonitor::last)
        .aggregateByIndexSingle(p -> p.second().machineKey(), Comparator.comparing(Pair::first), vs -> vs)
        .expandColumn(VALUE_NAME, vs -> {
          double usedMemory = vs.stream()
              .mapToDouble(v -> v.usedMemory() / 1024d / 1024d)
              .average()
              .orElse(0);
          double maxMemory = vs.stream()
              .mapToDouble(v -> v.maxMemory() / 1024d / 1024d)
              .average()
              .orElse(0);
          return Map.ofEntries(
              Map.entry("Used", new NumericCell(usedMemory, "%.0fMB").rightAligned()),
              Map.entry("Tot", new NumericCell(maxMemory, "%.0fMB").rightAligned()),
              Map.entry("%Mem", new BarPlotCell(6, 0, maxMemory, usedMemory)),
              Map.entry("#Proc", new NumericCell(vs.size(), "%d").rightAligned()));
        })
        .remapRowIndex(p -> p.second().machineKey());
    //noinspection rawtypes,unchecked
    machines = Table.colLeftJoin(machines, pCells).expandRowIndex(m ->
        (Map) Map.ofEntries(Map.entry("Key", new StringCell("M%02d".formatted(machineKeys.indexOf(m))))));

    // check keystrokes
    try {
      KeyStroke k = screen.pollInput();
      if (k != null
          && k.getCharacter() != null
          && ((k.getCharacter().equals('c') && k.isCtrlDown())
              || k.getKeyType().equals(KeyType.EOF))) {
        stop();
      }
    } catch (IOException e) {
      L.warning(String.format("Cannot check key strokes: %s", e));
    }
    // update size
    TerminalSize size = screen.doResizeIfNecessary();
    if (size == null) {
      screen.clear();
    }
    TuiDrawer td = new TuiDrawer(screen.newTextGraphics());
    // draw structure
    td.inX(0, 0.6f)
        .inY(0, 0.33f)
        .clear()
        .drawFrame("Machines (%d)".formatted(machines.nRows()))
        .inner(1)
        .drawTable(machines, List.of("Key", "Name", "Load", "~Load", "CPUs", "#Proc", "Used", "Tot", "%Mem"));
    // refresh
    try {
      screen.refresh();
    } catch (IOException e) {
      L.warning(String.format("Cannot refresh screen: %s", e));
    }
  }
}
