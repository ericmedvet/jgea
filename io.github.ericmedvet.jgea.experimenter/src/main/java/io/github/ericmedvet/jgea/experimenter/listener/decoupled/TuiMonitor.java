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
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import io.github.ericmedvet.jgea.core.util.*;
import io.github.ericmedvet.jgea.experimenter.listener.tui.table.*;
import io.github.ericmedvet.jgea.experimenter.listener.tui.util.TuiDrawer;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TuiMonitor implements Runnable {

  private static final Logger L = Logger.getLogger(TuiMonitor.class.getName());
  private static final Configuration DEFAULT_CONFIGURATION =
      new Configuration(0.5f, 0.85f, 0.5f, 0.5f, 8, 12, 1000, 60, 10, 10000, 2, 5, 20);
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
      Source<DataItemKey, DataItemInfo> dataItemSource
  ) {
    this(
        DEFAULT_CONFIGURATION,
        machineSource,
        processSource,
        logSource,
        experimentSource,
        runSource,
        dataItemSource
    );
  }

  public TuiMonitor(
      Configuration configuration,
      Source<MachineKey, MachineInfo> machineSource,
      Source<ProcessKey, ProcessInfo> processSource,
      Source<ProcessKey, LogInfo> logSource,
      Source<ExperimentKey, ExperimentInfo> experimentSource,
      Source<RunKey, RunInfo> runSource,
      Source<DataItemKey, DataItemInfo> dataItemSource
  ) {
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
      double purgeThreshold
  ) {}

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
          }
        },
        0,
        configuration.uiRefreshIntervalMillis,
        TimeUnit.MILLISECONDS
    );
    isRunning = true;
  }

  private synchronized void updateUI() {
    // update size
    TerminalSize size = screen.doResizeIfNecessary();
    if (size == null) {
      screen.clear();
    }
    TuiDrawer td = new TuiDrawer(screen.newTextGraphics());
    // prepare data
    refreshTables();
    List<MachineKey> machineKeys =
        machineTable.rowIndexes().stream().map(Pair::second).distinct().toList();
    List<ExperimentKey> experimentsKeys = experimentTable.rowIndexes().stream()
        .map(Pair::second)
        .distinct()
        .toList();
    LocalDateTime machineHistoryStartTime = LocalDateTime.now().minusSeconds(configuration.machineHistorySeconds);
    LocalDateTime machineHistoryEndTime = LocalDateTime.now();
    Map<ProcessKey, ProcessInfo> processInfos = processTable.aggregateByIndexSingle(
            Pair::second,
            Comparator.comparing(Pair::first),
            vs -> last(vs).getValue()
        )
        .remapRowIndex(Pair::second)
        .column(VALUE_NAME);
    Map<RunKey, RunInfo> runInfos = runTable.aggregateByIndexSingle(
            Pair::second,
            Comparator.comparing(Pair::first),
            vs -> last(vs).getValue()
        )
        .remapRowIndex(Pair::second)
        .column(VALUE_NAME);
    Map<ExperimentKey, ExperimentInfo> experimentInfos = experimentTable.aggregateByIndexSingle(
            Pair::second,
            Comparator.comparing(Pair::first),
            vs -> last(vs).getValue()
        )
        .remapRowIndex(Pair::second)
        .column(VALUE_NAME);
    List<String> dataItemNames = experimentInfos.values()
        .stream()
        .map(ei -> ei.formats().stream().map(Pair::first).toList())
        .flatMap(List::stream)
        .distinct()
        .toList();
    Map<Pair<ExperimentKey, String>, String> formats = experimentInfos.entrySet()
        .stream()
        .map(e -> e.getValue()
            .formats()
            .stream()
            .map(p -> Map.entry(new Pair<>(e.getKey(), p.first()), p.second()))
            .toList())
        .flatMap(List::stream)
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    // machines table
    Table<MachineKey, String, Cell> machines = machineTable
        .aggregateByIndexSingle(Pair::second, (p1, p2) -> p2.first().compareTo(p1.first()), vs -> vs)
        .expandColumn(
            VALUE_NAME,
            (p, vs) -> Map.ofEntries(
                Map.entry("S", new StatusCell(p.first())),
                Map.entry(
                    "Name",
                    new StringCell(last(vs).getValue().machineName())
                ),
                Map.entry(
                    "CPUs",
                    new NumericCell(last(vs).getValue().numberOfProcessors(), "%d").rightAligned()
                ),
                Map.entry(
                    "Load",
                    new TrendedNumericCell<>(
                        vs.stream()
                            .map(v -> v.getValue()
                                .cpuLoad())
                            .toList(),
                        "%.1f"
                    )
                        .rightAligned()
                ),
                Map.entry(
                    "~Load [%dm]".formatted(configuration.machineHistorySeconds / 60),
                    new AreaPlotCell(
                        configuration.areaPlotLength,
                        (SortedMap<? extends Number, ? extends Number>) vs.stream()
                            .filter(v -> v.getKey()
                                .first()
                                .isAfter(machineHistoryStartTime))
                            .collect(Collectors.toMap(
                                v -> v.getKey()
                                    .first()
                                    .toEpochSecond(ZoneOffset.UTC),
                                v -> v.getValue()
                                    .cpuLoad(),
                                (n1, n2) -> n1,
                                TreeMap::new
                            )),
                        machineHistoryStartTime.toEpochSecond(ZoneOffset.UTC),
                        machineHistoryEndTime.toEpochSecond(ZoneOffset.UTC)
                    )
                )
            )
        )
        .remapRowIndex(Pair::second);
    Table<MachineKey, String, Cell> pCells = processTable
        .aggregateByIndexSingle(Pair::second, Comparator.comparing(Pair::first), ps -> last(ps).getValue())
        .aggregateByIndexSingle(p -> p.second().machineKey(), Comparator.comparing(Pair::first), ps -> ps)
        .expandColumn(VALUE_NAME, (p, vs) -> {
          double usedMemory = vs.stream()
              .mapToDouble(v -> v.getValue().usedMemory() / 1024d / 1024d)
              .average()
              .orElse(0);
          double maxMemory = vs.stream()
              .mapToDouble(v -> v.getValue().maxMemory() / 1024d / 1024d)
              .average()
              .orElse(0);
          return Map.ofEntries(
              Map.entry("Used", new NumericCell(usedMemory, "%.0f", "MB").rightAligned()),
              Map.entry("Tot", new NumericCell(maxMemory, "%.0f", "MB").rightAligned()),
              Map.entry("%Mem", new HorizontalBarCell(configuration.barLength, 0, maxMemory, usedMemory)),
              Map.entry("#Proc", new NumericCell(vs.size(), "%d").rightAligned())
          );
        })
        .remapRowIndex(p -> p.second().machineKey());
    machines = machines.colLeftJoin(pCells)
        .expandRowIndex("MK", m -> new StringCell("M%02d".formatted(machineKeys.indexOf(m)), td.getConfiguration()
            .secondaryStringColor()))
        .sorted(Comparator.comparing(MachineKey::value));
    // experiments table
    Table<ExperimentKey, String, Cell> experiments = experimentTable
        .aggregateByIndexSingle(Pair::second, (p1, p2) -> p2.first().compareTo(p1.first()), vs -> vs)
        .expandColumn(
            VALUE_NAME,
            (p, vs) -> {
              long nOfAll = vs.get(0).getValue().nOfRuns();
              List<RunInfo> eRunInfos = runInfos.entrySet().stream().filter(e -> e.getKey()
                  .experimentKey()
                  .equals(p.second())).map(Map.Entry::getValue).toList();
              long nOfDone = eRunInfos.stream().filter(RunInfo::ended).count();
              long nOfDoing = eRunInfos.stream().filter(ri -> !ri.ended()).count();
              double sumOfProgresses = eRunInfos.stream()
                  .mapToDouble(ri -> ri.progress().equals(Progress.NA) ? 0 : ri.progress().rate())
                  .sum();
              return Map.ofEntries(
                  Map.entry("S", new StatusCell(p.first())),
                  Map.entry("User", new StringCell(processInfos.get(p.second().processKey()).username())),
                  Map.entry(
                      "#Runs",
                      (Cell) new CompositeCell(
                          "/",
                          new NumericCell(nOfAll, "%d").rightAligned(),
                          new NumericCell(nOfDoing, "%d").rightAligned(),
                          new NumericCell(nOfDone, "%d").rightAligned()
                      )
                  ),
                  Map.entry(
                      "Progress",
                      new ProgressCell(configuration.barLength, new Progress(0, nOfAll, sumOfProgresses))
                  ),
                  Map.entry(
                      "ETA",
                      new EtaCell(vs.get(0).getValue().startLocalDateTime(), new Progress(0, nOfAll, sumOfProgresses))
                  )
              );
            }
        )
        .remapRowIndex(Pair::second)
        .expandRowIndex(e -> Map.ofEntries(
            Map.entry("EK", new StringCell("E%02d".formatted(experimentsKeys.indexOf(e)), td.getConfiguration()
                .secondaryStringColor())),
            Map.entry(
                "MK",
                new StringCell(
                    "M%02d".formatted(machineKeys.indexOf(e.processKey().machineKey())),
                    td.getConfiguration()
                        .secondaryStringColor()
                )
            )
        ))
        .sorted(Comparator.comparing(ExperimentKey::value));
    // runs table
    Table<RunKey, String, Cell> runs = runTable.aggregateByIndexSingle(
            Pair::second,
            (p1, p2) -> p2.first().compareTo(p1.first()),
            vs -> vs
        )
        .sorted(
            VALUE_NAME,
            (es1, es2) -> es2.get(0)
                .getValue()
                .startLocalDateTime()
                .compareTo(es1.get(0).getValue().startLocalDateTime())
        )
        .expandColumn(
            VALUE_NAME,
            (p, vs) -> Map.ofEntries(
                Map.entry("S", (Cell) new StatusCell(p.first())),
                Map.entry(
                    "Progress",
                    new ProgressCell(configuration.barLength, vs.get(0).getValue().progress())
                ),
                Map.entry(
                    "ETA",
                    new EtaCell(vs.get(0).getValue().startLocalDateTime(), vs.get(0).getValue().progress())
                )
            )
        )
        .remapRowIndex(Pair::second)
        .expandRowIndex(r -> Map.ofEntries(
            Map.entry("RK", new StringCell(r.value(), td.getConfiguration()
                .secondaryStringColor())),
            Map.entry(
                "EK",
                new StringCell("E%02d".formatted(experimentsKeys.indexOf(r.experimentKey())), td.getConfiguration()
                    .secondaryStringColor())
            ),
            Map.entry(
                "MK",
                new StringCell(
                    "M%02d".formatted(machineKeys.indexOf(r.experimentKey().processKey().machineKey())),
                    td.getConfiguration()
                        .secondaryStringColor()
                )
            )
        ));
    Table<RunKey, String, Cell> diCells = dataItemTable.aggregateByIndexSingle(
            Pair::second,
            Comparator.comparing(Pair::first),
            vs -> vs
        ).wider(p -> p.second().runKey(), VALUE_NAME, p -> p.second().name())
        .map((rk, name, es) -> {
          List<Object> vs = es.stream().map(e -> e.getValue().content()).toList();
          String format = formats.getOrDefault(new Pair<>(rk.experimentKey(), name), "%s");
          if (vs.get(0) instanceof Number) {
            //noinspection rawtypes,unchecked
            return new TrendedNumericCell<>(
                (List) vs,
                format
            ).rightAligned();
          }
          if (last(vs) instanceof TextPlotter.Miniplot miniplot) {
            return new MiniplotCell(miniplot);
          }
          return new StringCell(format.formatted(last(vs).toString()));
        });
    runs = runs.colLeftJoin(diCells);
    // legend table
    Table<String, String, StringCell> legend = Table.of(dataItemNames.stream()
            .distinct()
            .collect(Collectors.toMap(
                n -> n,
                n -> Map.ofEntries(
                    Map.entry("Short", new StringCell(StringUtils.collapse(n))),
                    Map.entry("Long", new StringCell(n))
                )
            )))
        .sorted(String::compareTo);
    // log table
    Table<Pair<LocalDateTime, ProcessKey>, String, Cell> logs = logTable
        .sorted((p1, p2) -> p2.first().compareTo(p1.first()))
        .expandColumn(
            VALUE_NAME,
            (p, li) -> Map.ofEntries(
                Map.entry("Level", (Cell) new LogLevelCell(li.level())),
                Map.entry("Time", new StringCell(DATETIME_FORMAT.formatted(p.first()), td.getConfiguration()
                    .secondaryStringColor())),
                Map.entry("Message", new StringCell(li.message()))
            )
        )
        .expandRowIndex(p -> Map.ofEntries(
            Map.entry(
                "MK",
                new StringCell("M%02d".formatted(machineKeys.indexOf(p.second().machineKey())), td.getConfiguration()
                    .secondaryStringColor())
            )
        ));
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
    // draw structure
    td.inX(0, 0.6f)
        .inY(0, 0.2f)
        .clear()
        .drawFrame("Machines (%d)".formatted(machines.nRows()))
        .inner(1)
        .drawTable(
            machines,
            List.of(
                "S",
                "MK",
                "Name",
                "Load",
                "~Load [%dm]".formatted(configuration.machineHistorySeconds / 60),
                "CPUs",
                "#Proc",
                "Used",
                "Tot",
                "%Mem"
            )
        );
    td.inX(0, 0.6f)
        .inY(0.2f, 0.2f)
        .clear()
        .drawFrame("Experiments (%d)".formatted(experiments.nRows()))
        .inner(1)
        .drawTable(experiments, List.of("S", "EK", "MK", "User", "#Runs", "Progress", "ETA"));
    td.inX(0.6f, 0.4f)
        .inY(0, 0.4f)
        .clear()
        .drawFrame("Legend")
        .inner(1)
        .drawTable(legend, List.of("Short", "Long"));
    td.inY(0.4f, 0.4f)
        .clear()
        .drawFrame("Runs (%d)".formatted(runs.nRows()))
        .inner(1)
        .drawTable(
            runs,
            Stream.of(
                List.of("S", "RK", "EK", "MK", "Progress", "ETA"),
                dataItemNames
            ).flatMap(List::stream).toList(),
            s -> Character.isUpperCase(s.charAt(0)) ? s : StringUtils.collapse(s)
        );
    td.inY(0.8f, 0.2f)
        .clear()
        .drawFrame("Logs (%d)".formatted(logs.nRows()))
        .inner(1)
        .drawTable(logs, List.of("MK", "Level", "Time", "Message"));

    // refresh
    try {
      screen.refresh();
    } catch (IOException e) {
      L.warning(String.format("Cannot refresh screen: %s", e));
    }
  }
}
