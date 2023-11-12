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
import io.github.ericmedvet.jgea.experimenter.listener.net.NetUtils;
import io.github.ericmedvet.jgea.experimenter.listener.tui.table.*;
import io.github.ericmedvet.jgea.experimenter.listener.tui.util.TuiDrawer;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TuiMonitor implements Runnable {

  private static final Logger L = Logger.getLogger(TuiMonitor.class.getName());
  private static final Configuration DEFAULT_CONFIGURATION = new Configuration(8, 12, 1000, 60, 1000, 30);
  private static final String DATETIME_FORMAT = "%1$tm-%1$td %1$tH:%1$tM:%1$tS";

  private static final String VALUE_NAME = "VALUE";

  private final Configuration configuration;
  private final ScheduledExecutorService uiExecutorService;

  private Screen screen;
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
  private final LogCapturer logCapturer;
  private LocalDateTime lastRefreshLocalDateTime = LocalDateTime.MIN;
  private final Supplier<String> title;

  public TuiMonitor(
      Supplier<String> title,
      Source<MachineKey, MachineInfo> machineSource,
      Source<ProcessKey, ProcessInfo> processSource,
      Source<ProcessKey, LogInfo> logSource,
      Source<ExperimentKey, ExperimentInfo> experimentSource,
      Source<RunKey, RunInfo> runSource,
      Source<DataItemKey, DataItemInfo> dataItemSource) {
    this(
        title,
        DEFAULT_CONFIGURATION,
        machineSource,
        processSource,
        logSource,
        experimentSource,
        runSource,
        dataItemSource);
  }

  public TuiMonitor(
      Supplier<String> title,
      Configuration configuration,
      Source<MachineKey, MachineInfo> machineSource,
      Source<ProcessKey, ProcessInfo> processSource,
      Source<ProcessKey, LogInfo> logSource,
      Source<ExperimentKey, ExperimentInfo> experimentSource,
      Source<RunKey, RunInfo> runSource,
      Source<DataItemKey, DataItemInfo> dataItemSource) {
    this.title = title;
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
    logCapturer = new LogCapturer(
        lr -> logTable.set(
            new Pair<>(LocalDateTime.now(), ProcessKey.local()),
            VALUE_NAME,
            new LogInfo(lr.getLevel(), lr.getMessage())),
        true);
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
      int barLength,
      int areaPlotLength,
      int uiRefreshIntervalMillis,
      long machineHistorySeconds,
      int logHistorySize,
      double purgeThreshold) {}

  private static <T> T last(List<T> ts) {
    return ts.get(ts.size() - 1);
  }

  private static int compareRunTableItems(
      List<Map.Entry<Pair<LocalDateTime, RunKey>, RunInfo>> es1,
      List<Map.Entry<Pair<LocalDateTime, RunKey>, RunInfo>> es2) {
    if (es1.get(0).getValue().ended() && !es2.get(0).getValue().ended()) {
      return 1;
    }
    if (!es1.get(0).getValue().ended() && es2.get(0).getValue().ended()) {
      return -1;
    }
    return es1.get(0)
        .getValue()
        .startLocalDateTime()
        .compareTo(es2.get(0).getValue().startLocalDateTime());
  }

  private void refreshTables() {
    machineSource.pull(lastRefreshLocalDateTime).forEach((p, v) -> machineTable.set(p, VALUE_NAME, v));
    processSource.pull(lastRefreshLocalDateTime).forEach((p, v) -> processTable.set(p, VALUE_NAME, v));
    logSource.pull(lastRefreshLocalDateTime).forEach((p, v) -> logTable.set(p, VALUE_NAME, v));
    experimentSource.pull(lastRefreshLocalDateTime).forEach((p, v) -> experimentTable.set(p, VALUE_NAME, v));
    runSource.pull(lastRefreshLocalDateTime).forEach((p, v) -> runTable.set(p, VALUE_NAME, v));
    dataItemSource.pull(lastRefreshLocalDateTime).forEach((p, v) -> dataItemTable.set(p, VALUE_NAME, v));
    lastRefreshLocalDateTime = LocalDateTime.now();
    // TODO align remote and local times
    LocalDateTime now = LocalDateTime.now();
    // prune shallowly
    List<Pair<LocalDateTime, RunKey>> toRemoveRKs = runTable
        .aggregateByIndexSingle(Pair::second, Comparator.comparing(Pair::first), es -> es.stream()
            .map(Map.Entry::getKey)
            .sorted((p1, p2) -> p2.first().compareTo(p1.first()))
            .skip(3)
            .toList())
        .columnValues(VALUE_NAME)
        .stream()
        .flatMap(List::stream)
        .toList();
    toRemoveRKs.forEach(runTable::removeRow);
    List<Pair<LocalDateTime, DataItemKey>> toRemoveDIKs = dataItemTable
        .aggregateByIndexSingle(Pair::second, Comparator.comparing(Pair::first), es -> es.stream()
            .map(Map.Entry::getKey)
            .sorted((p1, p2) -> p2.first().compareTo(p1.first()))
            .skip(3)
            .toList())
        .columnValues(VALUE_NAME)
        .stream()
        .flatMap(List::stream)
        .toList();
    toRemoveDIKs.forEach(dataItemTable::removeRow);
    // prune deeply
    List<Pair<LocalDateTime, MachineKey>> toRemoveMKs = machineTable
        .aggregateByIndexSingle(Pair::second, (p1, p2) -> p2.first().compareTo(p1.first()), vs -> "")
        .rowIndexes()
        .stream()
        .filter(p -> p.first().until(now, ChronoUnit.SECONDS) > configuration.purgeThreshold)
        .map(Pair::second)
        .map(mk -> machineTable.rowIndexes().stream()
            .filter(p -> p.second().equals(mk))
            .toList())
        .flatMap(List::stream)
        .toList();
    List<Pair<LocalDateTime, ProcessKey>> toRemovePKs = processTable
        .aggregateByIndexSingle(Pair::second, (p1, p2) -> p2.first().compareTo(p1.first()), vs -> "")
        .rowIndexes()
        .stream()
        .filter(p -> p.first().until(now, ChronoUnit.SECONDS) > configuration.purgeThreshold)
        .map(Pair::second)
        .map(pk -> processTable.rowIndexes().stream()
            .filter(p -> p.second().equals(pk))
            .toList())
        .flatMap(List::stream)
        .toList();
    List<Pair<LocalDateTime, ExperimentKey>> toRemoveEKs = experimentTable.rowIndexes().stream()
        .filter(ep -> toRemovePKs.stream()
            .anyMatch(pp -> pp.second().equals(ep.second().processKey())))
        .toList();
    toRemoveRKs = runTable.rowIndexes().stream()
        .filter(rp -> toRemovePKs.stream().anyMatch(pp -> pp.second()
            .equals(rp.second().experimentKey().processKey())))
        .toList();
    toRemoveDIKs = dataItemTable.rowIndexes().stream()
        .filter(dip -> toRemovePKs.stream().anyMatch(pp -> pp.second()
            .equals(dip.second().runKey().experimentKey().processKey())))
        .toList();
    toRemoveMKs.forEach(machineTable::removeRow);
    toRemovePKs.forEach(processTable::removeRow);
    toRemoveEKs.forEach(experimentTable::removeRow);
    toRemoveRKs.forEach(runTable::removeRow);
    toRemoveDIKs.forEach(dataItemTable::removeRow);
    while (logTable.nRows() > configuration.logHistorySize) {
      logTable.removeRow(logTable.rowIndexes().get(0));
    }
  }

  @Override
  public void run() {
    // start painter task
    uiExecutorService.scheduleAtFixedRate(
        () -> {
          try {
            updateUI();
          } catch (Throwable e) {
            e.printStackTrace();
            L.warning("Unexpected exception: %s".formatted(e));
          }
        },
        0,
        configuration.uiRefreshIntervalMillis,
        TimeUnit.MILLISECONDS);
  }

  private void stop() {
    try {
      screen.stopScreen();
    } catch (IOException e) {
      L.warning(String.format("Cannot stop screen: %s", e));
    }
    uiExecutorService.shutdownNow();
    logCapturer.close();
    machineSource.close();
    processSource.close();
    logSource.close();
    experimentSource.close();
    runSource.close();
    dataItemSource.close();
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
    Map<ProcessKey, ProcessInfo> processInfos = processTable
        .aggregateByIndexSingle(Pair::second, Comparator.comparing(Pair::first), vs -> last(vs).getValue())
        .remapRowIndex(Pair::second)
        .column(VALUE_NAME);
    Map<RunKey, RunInfo> runInfos = runTable.aggregateByIndexSingle(
            Pair::second, Comparator.comparing(Pair::first), vs -> last(vs).getValue())
        .remapRowIndex(Pair::second)
        .column(VALUE_NAME);
    Map<ExperimentKey, ExperimentInfo> experimentInfos = experimentTable
        .aggregateByIndexSingle(Pair::second, Comparator.comparing(Pair::first), vs -> last(vs).getValue())
        .remapRowIndex(Pair::second)
        .column(VALUE_NAME);
    List<String> dataItemNames = experimentInfos.values().stream()
        .map(ei -> ei.formats().stream().map(Pair::first).toList())
        .flatMap(List::stream)
        .distinct()
        .toList();
    Map<Pair<ExperimentKey, String>, String> formats = experimentInfos.entrySet().stream()
        .map(e -> e.getValue().formats().stream()
            .map(p -> Map.entry(new Pair<>(e.getKey(), p.first()), p.second()))
            .toList())
        .flatMap(List::stream)
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (v1, v2) -> v1));
    // machines table
    Table<MachineKey, String, Cell> machines = machineTable
        .aggregateByIndexSingle(Pair::second, (p1, p2) -> p2.first().compareTo(p1.first()), vs -> vs)
        .expandColumn(
            VALUE_NAME,
            (p, vs) -> Map.ofEntries(
                Map.entry("S", new StatusCell(p.first())),
                Map.entry(
                    "Name",
                    new StringCell(last(vs).getValue().machineName())),
                Map.entry(
                    "CPUs",
                    new NumericCell(last(vs).getValue().numberOfProcessors(), "%d").rightAligned()),
                Map.entry(
                    "Load",
                    new TrendedNumericCell<>(
                            vs.stream()
                                .map(v -> v.getValue()
                                    .cpuLoad())
                                .toList(),
                            "%.1f")
                        .rightAligned()),
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
                                TreeMap::new)),
                        machineHistoryStartTime.toEpochSecond(ZoneOffset.UTC),
                        machineHistoryEndTime.toEpochSecond(ZoneOffset.UTC)))))
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
              Map.entry("#Proc", new NumericCell(vs.size(), "%d").rightAligned()));
        })
        .remapRowIndex(p -> p.second().machineKey());
    machines = machines.colLeftJoin(pCells)
        .expandRowIndex(
            "MK",
            m -> new StringCell(
                "M%02d".formatted(machineKeys.indexOf(m)),
                td.getConfiguration().secondaryStringColor()))
        .sorted(Comparator.comparing(MachineKey::value));
    // experiments table
    Table<ExperimentKey, String, Cell> experiments = experimentTable
        .aggregateByIndexSingle(Pair::second, (p1, p2) -> p2.first().compareTo(p1.first()), vs -> vs)
        .expandColumn(VALUE_NAME, (p, vs) -> {
          long nOfAll = vs.get(0).getValue().nOfRuns();
          List<RunInfo> eRunInfos = runInfos.entrySet().stream()
              .filter(e -> e.getKey().experimentKey().equals(p.second()))
              .map(Map.Entry::getValue)
              .toList();
          long nOfDone = eRunInfos.stream().filter(RunInfo::ended).count();
          long nOfDoing = eRunInfos.stream().filter(ri -> !ri.ended()).count();
          double sumOfProgresses = eRunInfos.stream()
              .mapToDouble(ri -> ri.progress().equals(Progress.NA)
                  ? 0
                  : ri.progress().rate())
              .sum();
          return Map.ofEntries(
              Map.entry("S", new StatusCell(p.first())),
              Map.entry(
                  "User",
                  new StringCell(processInfos
                      .get(p.second().processKey())
                      .username())),
              Map.entry("#Runs", (Cell) new CompositeCell(
                  "/",
                  new NumericCell(nOfAll, "%d").rightAligned(),
                  new NumericCell(nOfDoing, "%d").rightAligned(),
                  new NumericCell(nOfDone, "%d").rightAligned())),
              Map.entry(
                  "Progress",
                  new ProgressCell(
                      configuration.barLength, new Progress(0, nOfAll, sumOfProgresses))),
              Map.entry(
                  "ETA",
                  new EtaCell(
                      vs.get(0).getValue().startLocalDateTime(),
                      new Progress(0, nOfAll, sumOfProgresses))));
        })
        .remapRowIndex(Pair::second)
        .expandRowIndex(e -> Map.ofEntries(
            Map.entry(
                "EK",
                new StringCell(
                    "E%02d".formatted(experimentsKeys.indexOf(e)),
                    td.getConfiguration().secondaryStringColor())),
            Map.entry(
                "MK",
                new StringCell(
                    "M%02d"
                        .formatted(machineKeys.indexOf(
                            e.processKey().machineKey())),
                    td.getConfiguration().secondaryStringColor()))))
        .sorted(Comparator.comparing(ExperimentKey::value));
    // runs table
    Table<RunKey, String, Cell> runs = runTable.aggregateByIndexSingle(
            Pair::second, (p1, p2) -> p2.first().compareTo(p1.first()), vs -> vs)
        .sorted(VALUE_NAME, TuiMonitor::compareRunTableItems)
        .expandColumn(
            VALUE_NAME,
            (p, vs) -> Map.ofEntries(
                Map.entry("S", (Cell) new StatusCell(p.first())),
                Map.entry(
                    "Progress",
                    new ProgressCell(
                        configuration.barLength,
                        vs.get(0).getValue().progress())),
                Map.entry(
                    "ETA",
                    new EtaCell(
                        vs.get(0).getValue().startLocalDateTime(),
                        vs.get(0).getValue().progress()))))
        .remapRowIndex(Pair::second)
        .expandRowIndex(r -> Map.ofEntries(
            Map.entry(
                "RK",
                new StringCell(r.value(), td.getConfiguration().secondaryStringColor())),
            Map.entry(
                "EK",
                new StringCell(
                    "E%02d".formatted(experimentsKeys.indexOf(r.experimentKey())),
                    td.getConfiguration().secondaryStringColor())),
            Map.entry(
                "MK",
                new StringCell(
                    "M%02d"
                        .formatted(machineKeys.indexOf(r.experimentKey()
                            .processKey()
                            .machineKey())),
                    td.getConfiguration().secondaryStringColor()))));
    Table<RunKey, String, Cell> diCells = dataItemTable
        .aggregateByIndexSingle(Pair::second, Comparator.comparing(Pair::first), vs -> vs)
        .wider(p -> p.second().runKey(), VALUE_NAME, p -> p.second().name())
        .map((rk, name, es) -> {
          if (es == null || es.isEmpty()) {
            return new EmptyCell();
          }
          List<Object> vs =
              es.stream().map(e -> e.getValue().content()).toList();
          String format = formats.getOrDefault(new Pair<>(rk.experimentKey(), name), "%s");
          if (vs.get(0) instanceof Number) {
            //noinspection rawtypes,unchecked
            return new TrendedNumericCell<>((List) vs, format).rightAligned();
          }
          if (last(vs) instanceof TextPlotter.Miniplot miniplot) {
            return new MiniplotCell(miniplot);
          }
          return new StringCell(format.formatted(last(vs).toString()));
        });
    runs = runs.colLeftJoin(diCells);
    // log table
    Table<Pair<LocalDateTime, ProcessKey>, String, Cell> logs = logTable.sorted(
            (p1, p2) -> p2.first().compareTo(p1.first()))
        .expandColumn(
            VALUE_NAME,
            (p, li) -> Map.ofEntries(
                Map.entry("Level", (Cell) new LogLevelCell(li.level())),
                Map.entry(
                    "Time",
                    new StringCell(
                        DATETIME_FORMAT.formatted(p.first()),
                        td.getConfiguration().secondaryStringColor())),
                Map.entry("Message", new StringCell(li.message()))))
        .expandRowIndex(p -> Map.ofEntries(Map.entry(
            "MK",
            new StringCell(
                "M%02d".formatted(machineKeys.indexOf(p.second().machineKey())),
                td.getConfiguration().secondaryStringColor()))));
    // legend table
    Table<String, String, StringCell> legend = Table.of(dataItemNames.stream()
            .distinct()
            .collect(Collectors.toMap(
                n -> n,
                n -> Map.ofEntries(
                    Map.entry("Short", new StringCell(StringUtils.collapse(n))),
                    Map.entry("Long", new StringCell(n))))))
        .sorted(String::compareTo);
    // local machine table
    Table<Integer, String, Cell> local = new HashMapTable<>();
    local.set(1, "Name", new StringCell("#Machines", td.getConfiguration().secondaryStringColor()));
    local.set(2, "Name", new StringCell("#Processes", td.getConfiguration().secondaryStringColor()));
    local.set(3, "Name", new StringCell("#Logs", td.getConfiguration().secondaryStringColor()));
    local.set(
        4, "Name", new StringCell("#Experiments", td.getConfiguration().secondaryStringColor()));
    local.set(5, "Name", new StringCell("#Runs", td.getConfiguration().secondaryStringColor()));
    local.set(6, "Name", new StringCell("#DataItems", td.getConfiguration().secondaryStringColor()));
    local.set(1, "Value", new NumericCell(machineTable.nRows(), "%d").rightAligned());
    local.set(2, "Value", new NumericCell(processTable.nRows(), "%d").rightAligned());
    local.set(3, "Value", new NumericCell(logTable.nRows(), "%d").rightAligned());
    local.set(4, "Value", new NumericCell(experimentTable.nRows(), "%d").rightAligned());
    local.set(5, "Value", new NumericCell(runTable.nRows(), "%d").rightAligned());
    local.set(6, "Value", new NumericCell(dataItemTable.nRows(), "%d").rightAligned());
    local.set(7, "Name", new StringCell("Memory", td.getConfiguration().secondaryStringColor()));
    local.set(
        7,
        "Value",
        new HorizontalBarCell(8, 0, NetUtils.getProcessMaxMemory(), NetUtils.getProcessUsedMemory()));
    local.set(8, "Name", new StringCell("Load", td.getConfiguration().secondaryStringColor()));
    local.set(
        8,
        "Value",
        new CompositeCell(
            "/",
            new NumericCell(NetUtils.getCPULoad(), "%.1f"),
            new NumericCell(NetUtils.getNumberOfProcessors(), "%d")));
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
    // draw
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
                "%Mem"));
    td.inX(0, 0.4f)
        .inY(0.2f, 0.2f)
        .clear()
        .drawFrame("Experiments (%d)".formatted(experiments.nRows()))
        .inner(1)
        .drawTable(experiments, List.of("S", "EK", "MK", "User", "#Runs", "Progress", "ETA"));
    td.inX(0.4f, 0.2f)
        .inY(0.2f, 0.2f)
        .clear()
        .drawFrame("Local")
        .inner(1)
        .drawTable(local, List.of("Name", "Value"));
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
            Stream.of(List.of("S", "RK", "EK", "MK", "Progress", "ETA"), dataItemNames)
                .flatMap(List::stream)
                .toList(),
            s -> Character.isUpperCase(s.charAt(0)) ? s : StringUtils.collapse(s));
    td.inY(0.8f, 0.2f)
        .clear()
        .drawFrame("Logs (%d)".formatted(logs.nRows()))
        .inner(1)
        .drawTable(logs, List.of("MK", "Level", "Time", "Message"));
    td.drawString(
        1,
        -1,
        "[" + DATETIME_FORMAT.formatted(LocalDateTime.now()) + " - " + title.get() + "]",
        td.getConfiguration().frameLabelColor());
    // refresh
    try {
      screen.refresh();
    } catch (IOException e) {
      L.warning(String.format("Cannot refresh screen: %s", e));
    }
  }
}
