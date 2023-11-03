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
import io.github.ericmedvet.jgea.core.util.Progress;
import io.github.ericmedvet.jgea.core.util.StringUtils;
import io.github.ericmedvet.jgea.core.util.TextPlotter;
import io.github.ericmedvet.jgea.experimenter.listener.net.Message;
import io.github.ericmedvet.jgea.experimenter.listener.net.Update;
import io.github.ericmedvet.jgea.experimenter.listener.tui.ListLogHandler;
import io.github.ericmedvet.jgea.experimenter.listener.tui.table.ColoredStringCell;
import io.github.ericmedvet.jgea.experimenter.listener.tui.util.DrawUtils;
import io.github.ericmedvet.jgea.experimenter.listener.tui.util.Point;
import io.github.ericmedvet.jgea.experimenter.listener.tui.util.Rectangle;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
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
      Map.entry(Level.CONFIG, TextColor.Factory.fromString("#6D8700"))
  );
  public static final String EMPTY_CELL_CONTENT = "-";
  private static final Logger L = Logger.getLogger(TuiMonitor.class.getName());
  private static final Configuration DEFAULT_CONFIGURATION =
      new Configuration(0.5f, 0.85f, 0.5f, 0.5f, 8, 12, 500, 60, 10, 10000, 2, 5, 20);
  private static final int SERVER_SOCKET_TIMEOUT_MILLIS = 1000;
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
  private Source source;

  public TuiMonitor(Source source) {
    this(
        new Configuration(
            DEFAULT_CONFIGURATION.runsSplit,
            DEFAULT_CONFIGURATION.logsSplit,
            DEFAULT_CONFIGURATION.legendSplit,
            DEFAULT_CONFIGURATION.machinesProcessesSplit,
            DEFAULT_CONFIGURATION.barLength,
            DEFAULT_CONFIGURATION.barLength,
            DEFAULT_CONFIGURATION.uiRefreshIntervalMillis,
            DEFAULT_CONFIGURATION.machineHistorySeconds,
            DEFAULT_CONFIGURATION.runDataHistorySize,
            DEFAULT_CONFIGURATION.runPlotHistorySize,
            DEFAULT_CONFIGURATION.laterThreshold,
            DEFAULT_CONFIGURATION.missingThreshold,
            DEFAULT_CONFIGURATION.purgeThreshold
        ),
        source
    );
  }

  public TuiMonitor(Configuration configuration, Source source) {
    super(true);
    this.configuration = configuration;
    this.source = source;
    uiExecutorService = Executors.newSingleThreadScheduledExecutor();
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
      double purgeThreshold
  ) {}


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
            refreshData();
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
    // start server
    try (ServerSocket serverSocket = new ServerSocket(configuration.port())) {
      serverSocket.setSoTimeout(SERVER_SOCKET_TIMEOUT_MILLIS);
      L.info("Server started on port %d".formatted(configuration.port()));
      while (isRunning) {
        try {
          Socket socket = serverSocket.accept();
          clientsExecutorService.submit(() -> {
            try (socket;
                 ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                 ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream())) {
              doHandshake(ois, oos);
              while (isRunning) {
                Message message = (Message) ois.readObject();
                L.fine("Msg received with %d updates"
                    .formatted(message.updates().size()));
                storeMessage(message);
              }
            } catch (IOException e) {
              L.warning("Cannot open input stream due to: %s".formatted(e));
            } catch (ClassNotFoundException e) {
              L.warning("Cannot read message due to: %s".formatted(e));
            }
          });
        } catch (SocketTimeoutException e) {
          // ignore
        } catch (IOException e) {
          L.warning("Cannot accept connection due to; %s".formatted(e));
        }
      }
    } catch (IOException e) {
      L.severe("Cannot start server due to: %s".formatted(e));
    }
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
    clientsExecutorService.shutdownNow();
  }

  private synchronized void storeMessage(Message message) {
    MachineKey machineKey = new MachineKey(message.machineInfo().machineName());
    machinesData.putIfAbsent(machineKey, new TreeMap<>());
    machinesData.get(machineKey).put(message.localTime(), message.machineInfo());
    machinesStatus.put(machineKey, new Status(Instant.now(), message.pollInterval(), TimeStatus.OK));
    ProcessKey processKey = new ProcessKey(
        message.machineInfo().machineName(), message.processInfo().processName());
    processesData.putIfAbsent(processKey, new TreeMap<>());
    processesData
        .get(processKey)
        .put(message.localTime(), new EnhancedProcessInfo(message.processInfo(), message.nOfRuns()));
    processesStatus.put(processKey, new Status(Instant.now(), message.pollInterval(), TimeStatus.OK));
    for (Update update : message.updates()) {
      RunKey runKey = new RunKey(
          message.machineInfo().machineName(), message.processInfo().processName(), update.runIndex());
      runsProgress.merge(
          runKey,
          new TimedProgress(
              Instant.now(),
              Instant.now(),
              update.runProgress(),
              update.runProgress(),
              update.isRunning()
          ),
          (otp, ntp) -> new TimedProgress(
              otp.initialContact(),
              Instant.now(),
              otp.initialProgress,
              update.runProgress(),
              update.isRunning()
          )
      );
      runsData.putIfAbsent(runKey, new LinkedHashMap<>());
      update.dataItems().forEach((dik, vs) -> runsData.get(runKey)
          .merge(dik, vs, (ovs, nvs) -> concatAndTrim(ovs, nvs, configuration.runDataHistorySize)));
      runsPlots.putIfAbsent(runKey, new LinkedHashMap<>());
      update.plotItems().forEach((pik, ps) -> runsPlots
          .get(runKey)
          .merge(pik, ps, (ovs, nvs) -> concatAndTrim(ovs, nvs, configuration.runPlotHistorySize)));
    }
  }

  private Status update(Status status, Instant now) {
    long elapsed = Duration.between(status.lastContact(), now).toMillis();
    long expected = Math.round(status.pollInterval() * 1000);
    TimeStatus timeStatus = TimeStatus.OK;
    if (elapsed > configuration.purgeThreshold() * expected) {
      timeStatus = TimeStatus.PURGE;
    } else if (elapsed > configuration.missingThreshold() * expected) {
      timeStatus = TimeStatus.MISSING;
    } else if (elapsed > configuration.laterThreshold() * expected) {
      timeStatus = TimeStatus.LATER;
    } else if (elapsed > expected) {
      timeStatus = TimeStatus.LATE;
    }
    return new Status(status.lastContact(), status.pollInterval(), timeStatus);
  }

  private synchronized void updateUI() {
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
    // draw structure
    DrawUtils.drawFrame(
        tg,
        runsR,
        "Runs (%d) - Local time: %s".formatted(runsData.size(), COMPLETE_DATETIME_FORMAT.format(Instant.now())),
        FRAME_COLOR,
        FRAME_LABEL_COLOR
    );
    DrawUtils.drawFrame(tg, legendR, "Legend", FRAME_COLOR, FRAME_LABEL_COLOR);
    DrawUtils.drawFrame(tg, logsR, "Logs", FRAME_COLOR, FRAME_LABEL_COLOR);
    DrawUtils.drawFrame(
        tg, machinesR, "Machines (%d)".formatted(machinesData.size()), FRAME_COLOR, FRAME_LABEL_COLOR);
    DrawUtils.drawFrame(
        tg, processesR, "Experiments (%d)".formatted(processesData.size()), FRAME_COLOR, FRAME_LABEL_COLOR);
    // compute and show legend
    r = legendR.inner(1);
    DrawUtils.clear(tg, r);
    record LegendItem(String collapsed, String name) {}
    List<LegendItem> legendItems = Stream.of(
            runsData.values().stream()
                .map(Map::keySet)
                .flatMap(Set::stream)
                .map(Update.DataItemKey::name)
                .toList(),
            runsPlots.values().stream()
                .map(Map::keySet)
                .flatMap(Set::stream)
                .map(pik -> List.of(pik.xName(), pik.yName()))
                .flatMap(List::stream)
                .toList()
        )
        .flatMap(List::stream)
        .sorted()
        .distinct()
        .map(s -> new LegendItem(StringUtils.collapse(s), s))
        .toList();
    int shortLabelW =
        legendItems.stream().mapToInt(p -> p.collapsed().length()).max().orElse(0);
    for (int i = 0; i < legendItems.size(); i = i + 1) {
      tg.setForegroundColor(DATA_LABEL_COLOR);
      DrawUtils.clipPut(tg, r, 0, i, legendItems.get(i).collapsed());
      tg.setForegroundColor(DATA_COLOR);
      DrawUtils.clipPut(tg, r, shortLabelW + 1, i, legendItems.get(i).name());
    }
    // draw data: logs
    synchronized (getLogRecords()) {
      DrawUtils.drawLogs(
          tg,
          logsR.inner(1),
          getLogRecords(),
          LEVEL_COLORS,
          DATA_COLOR,
          MAIN_DATA_COLOR,
          LEVEL_FORMAT,
          DATETIME_FORMAT
      );
    }
    // compute and show machines
    /*
    Table<Cell> machinesTable = new ArrayTable<>(List.of("", "", "Progress", "Machine", "Cores", "Load"));
    forEach(
    machinesData,
    (i, mk, v) -> machinesTable.addRow(List.of(
    new ColoredStringCell(
    STATUS_STRING, machinesStatus.get(mk).status().getColor()),
    new StringCell("M%02d".formatted(i)),
    new StringCell(
    machinesProgress.containsKey(mk)
    ? progressPlot(machinesProgress.get(mk).lastProgress(), configuration.barLength)
    : EMPTY_CELL_CONTENT),
    new StringCell(mk.machineName()),
    new StringCell(last(v, MachineInfo::numberOfProcessors, "%2d")),
    new CompositeCell(List.of(
    new StringCell(last(v, MachineInfo::cpuLoad, "%5.2f")),
    trend(v, MachineInfo::cpuLoad).cell(),
    new StringCell(areaPlot(
    v,
    MachineInfo::cpuLoad,
    v.lastKey() - configuration.machineHistorySeconds * 1000d,
    configuration.areaPlotLength)))))),
    false);
    DrawUtils.drawTable(tg, machinesR.inner(1), machinesTable, DATA_LABEL_COLOR, DATA_COLOR);
    // compute and show processes
    Table<Cell> processesTable = new ArrayTable<>(
    List.of("", "", "ETA", "Progress", "Process", "Mac.", "User", "Used mem.", "Max mem."));
    forEach(
    processesData,
    (i, pk, v) -> processesTable.addRow(List.of(
    new ColoredStringCell(
    STATUS_STRING, processesStatus.get(pk).status().getColor()),
    new StringCell("P%02d".formatted(i)),
    new StringCell(
    processesProgress.containsKey(pk)
    ? eta(processesProgress.get(pk).eta())
    : EMPTY_CELL_CONTENT),
    new StringCell(
    processesProgress.containsKey(pk)
    ? progressPlot(
    processesProgress.get(pk).lastProgress(), configuration.barLength)
    : EMPTY_CELL_CONTENT),
    new StringCell(pk.processName()),
    new StringCell("M%02d"
    .formatted(
    machinesData.keySet().stream().toList().indexOf(pk.machineKey()))),
    new StringCell(last(v, pi -> pi.processInfo().username(), "%s")),
    new CompositeCell(List.of(
    new StringCell(last(v, pi -> pi.processInfo().usedMemory() / 1024 / 1024, "%5d")),
    trend(v, pi -> pi.processInfo().usedMemory()).cell(),
    new StringCell(areaPlot(
    v,
    pi -> pi.processInfo().usedMemory(),
    v.lastKey() - configuration.machineHistorySeconds * 1000d,
    configuration.areaPlotLength)))),
    new CompositeCell(List.of(
    new StringCell(last(v, pi -> pi.processInfo().maxMemory() / 1024 / 1024, "%5d")),
    trend(v, pi -> pi.processInfo().maxMemory()).cell())))),
    false);
    DrawUtils.drawTable(tg, processesR.inner(1), processesTable, DATA_LABEL_COLOR, DATA_COLOR);
    // compute and show runs
    Table<Cell> runsTable;
    List<String> columns = new ArrayList<>(List.of("", "", "ETA", "Progress", "Proc."));
    List<Update.DataItemKey> dataItemKeys = runsData.values().stream()
    .map(Map::keySet)
    .flatMap(Collection::stream)
    .distinct()
    .toList();
    dataItemKeys.forEach(dik -> columns.add(StringUtils.collapse(dik.name())));
    List<Update.PlotItemKey> plotItemKeys = runsPlots.values().stream()
    .map(Map::keySet)
    .flatMap(Collection::stream)
    .distinct()
    .toList();
    plotItemKeys.forEach(
    pik -> columns.add(StringUtils.collapse(pik.xName()) + "/" + StringUtils.collapse(pik.yName())));
    runsTable = new ArrayTable<>(columns);
    forEach(
    runsData,
    (i, rk, v) -> {
    List<Cell> row = new ArrayList<>();
    row.add(new ColoredStringCell(
    STATUS_STRING,
    (runsProgress.containsKey(rk)
    && runsProgress.get(rk).isRunning())
    ? TimeStatus.OK.getColor()
    : TimeStatus.MISSING.getColor()));
    row.add(new StringCell("R%03d".formatted(rk.runIndex())));
    row.add(new StringCell(
    runsProgress.containsKey(rk)
    ? eta(runsProgress.get(rk).eta())
    : EMPTY_CELL_CONTENT));
    row.add(new StringCell(
    runsProgress.containsKey(rk)
    ? progressPlot(runsProgress.get(rk).lastProgress(), configuration.barLength)
    : EMPTY_CELL_CONTENT));
    row.add(new StringCell("P%02d"
    .formatted(processesData.keySet().stream().toList().indexOf(rk.processKey()))));
    dataItemKeys.forEach(dik -> {
    List<?> values = v.get(dik);
    if (values != null && !values.isEmpty()) {
    row.add(new StringCell(dik.format().formatted(values.get(values.size() - 1))));
    } else {
    row.add(new ColoredStringCell(EMPTY_CELL_CONTENT, MISSING_DATA_COLOR));
    }
    });
    plotItemKeys.forEach(pik -> {
    String s = "";
    if (runsPlots.containsKey(rk)) {
    List<Update.PlotPoint> ps = runsPlots.get(rk).get(pik);
    if (ps != null) {
    SortedMap<Double, Double> data = new TreeMap<>();
    ps.forEach(p -> data.put(p.x(), p.y()));
    s = TextPlotter.areaPlot(data, pik.minX(), pik.maxX(), configuration.areaPlotLength());
    }
    }
    row.add(new StringCell(s));
    });
    runsTable.addRow(row);
    },
    true);
    DrawUtils.drawTable(tg, runsR.inner(1), runsTable, DATA_LABEL_COLOR, DATA_COLOR);
    */
    // refresh
    try {
      screen.refresh();
    } catch (IOException e) {
      L.warning(String.format("Cannot refresh screen: %s", e));
    }
  }
}
