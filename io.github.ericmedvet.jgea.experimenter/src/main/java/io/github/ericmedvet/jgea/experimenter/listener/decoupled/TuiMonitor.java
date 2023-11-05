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
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import io.github.ericmedvet.jgea.core.util.*;
import io.github.ericmedvet.jgea.experimenter.listener.tui.ListLogHandler;
import io.github.ericmedvet.jgea.experimenter.listener.tui.table.ColoredStringCell;
import io.github.ericmedvet.jgea.experimenter.listener.tui.util.Point;
import io.github.ericmedvet.jgea.experimenter.listener.tui.util.Rectangle;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
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
  private static final Logger L = Logger.getLogger(TuiMonitor.class.getName());
  private static final Configuration DEFAULT_CONFIGURATION =
      new Configuration(0.5f, 0.85f, 0.5f, 0.5f, 8, 12, 500, 60, 10, 10000, 2, 5, 20);
  private static final String STATUS_STRING = "⬤";
  private static final DateTimeFormatter SAME_DAY_DATETIME_FORMAT =
      DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault());
  private static final DateTimeFormatter COMPLETE_DATETIME_FORMAT =
      DateTimeFormatter.ofPattern("MM-dd HH:mm").withZone(ZoneId.systemDefault());
  private static final TextColor FRAME_COLOR = TextColor.Factory.fromString("#105010");
  private static final TextColor FRAME_LABEL_COLOR = TextColor.Factory.fromString("#10A010");
  private static final TextColor DATA_LABEL_COLOR = TextColor.Factory.fromString("#A01010");
  private static final TextColor MISSING_DATA_COLOR = TextColor.Factory.fromString("#404040");
  private static final TextColor DATA_COLOR = TextColor.Factory.fromString("#A0A0A0");
  private static final TextColor MAIN_DATA_COLOR = TextColor.Factory.fromString("#F0F0F0");
  private static final String LEVEL_FORMAT = "%4.4s";
  private static final String DATETIME_FORMAT = "%1$tm-%1$td %1$tH:%1$tM:%1$tS";

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
    SAME('=', TextColor.Factory.fromString("#888888")),
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

  private static <K, V> void forEach(Map<K, V> m, TriConsumer<Integer, K, V> f, boolean reversed) {
    List<Map.Entry<K, V>> entries = m.entrySet().stream().toList();
    for (int i = 0; i < entries.size(); i++) {
      Map.Entry<K, V> e = entries.get(reversed ? (entries.size() - i - 1) : i);
      f.accept(i, e.getKey(), e.getValue());
    }
  }

  private static <T> String last(SortedMap<Long, T> data, Function<T, ?> function, String format) {
    Object v = function.apply(data.get(data.lastKey()));
    try {
      return format.formatted(v);
    } catch (RuntimeException e) {
      return "F_ERR";
    }
  }

  private static String progressPlot(Progress p, int l) {
    if (p == null || Double.isNaN(p.rate())) {
      return EMPTY_CELL_CONTENT;
    }
    return TextPlotter.horizontalBar(p.rate(), 0, 1, l, false);
  }

  private static <T> Trend trend(SortedMap<Long, T> data, Function<T, Number> function) {
    if (data.size() == 1) {
      return Trend.NONE;
    }
    double[] ds = data.values().stream()
        .mapToDouble(v -> function.apply(v).doubleValue())
        .toArray();
    double d1 = ds[ds.length - 1];
    double d2 = ds[ds.length - 2];
    return Trend.from(d1, d2);
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
        TimeUnit.MILLISECONDS);
    isRunning = true;
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

  private synchronized void updateUI() {

    System.out.println("updating");

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
    TextGraphics tg = screen.newTextGraphics();
    Rectangle r;
    if (size == null) {
      size = screen.getTerminalSize();
    } else {
      screen.clear();
    }
    // adjust rectangles
    Rectangle all = new Rectangle(new Point(0, 0), new Point(size.getColumns(), size.getRows()));
    Rectangle nR = all.splitVertically(configuration.runsSplit, configuration.logsSplit)
        .get(0);
    Rectangle runsR = all.splitVertically(configuration.runsSplit, configuration.logsSplit)
        .get(1);
    Rectangle logsR = all.splitVertically(configuration.runsSplit, configuration.logsSplit)
        .get(2);
    Rectangle nwR = nR.splitHorizontally(configuration.legendSplit).get(0);
    Rectangle legendR = nR.splitHorizontally(configuration.legendSplit).get(1);
    Rectangle machinesR =
        nwR.splitVertically(configuration.machinesProcessesSplit).get(0);
    Rectangle processesR =
        nwR.splitVertically(configuration.machinesProcessesSplit).get(1);
    // refresh
    try {
      screen.refresh();
    } catch (IOException e) {
      L.warning(String.format("Cannot refresh screen: %s", e));
    }
  }
}
