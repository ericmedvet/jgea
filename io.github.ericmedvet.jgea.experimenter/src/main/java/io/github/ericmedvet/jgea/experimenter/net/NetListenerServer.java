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

package io.github.ericmedvet.jgea.experimenter.net;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import io.github.ericmedvet.jgea.core.util.*;
import io.github.ericmedvet.jgea.tui.table.Cell;
import io.github.ericmedvet.jgea.tui.table.ColoredStringCell;
import io.github.ericmedvet.jgea.tui.table.CompositeCell;
import io.github.ericmedvet.jgea.tui.table.StringCell;
import io.github.ericmedvet.jgea.tui.util.DrawUtils;
import io.github.ericmedvet.jgea.tui.util.Point;
import io.github.ericmedvet.jgea.tui.util.Rectangle;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.random.RandomGenerator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author "Eric Medvet" on 2023/03/26 for jgea
 */
public class NetListenerServer implements Runnable {

  public static final String EMPTY_CELL_CONTENT = "-";
  private final static Logger L = Logger.getLogger(NetListenerServer.class.getName());
  private final static Configuration DEFAULT_CONFIGURATION = new Configuration(
      0.5f,
      0.5f,
      0.5f,
      8,
      12,
      500,
      60,
      10,
      10000,
      10979,
      "key",
      100,
      2,
      5,
      20
  );
  private final static String STATUS_STRING = "⬤";
  private final static DateTimeFormatter SAME_DAY_DATETIME_FORMAT = DateTimeFormatter
      .ofPattern("HH:mm:ss")
      .withZone(ZoneId.systemDefault());
  private final static DateTimeFormatter COMPLETE_DATETIME_FORMAT = DateTimeFormatter
      .ofPattern("MM-dd HH:mm")
      .withZone(ZoneId.systemDefault());
  private final static TextColor FRAME_COLOR = TextColor.Factory.fromString("#105010");
  private final static TextColor FRAME_LABEL_COLOR = TextColor.Factory.fromString("#10A010");
  private final static TextColor DATA_LABEL_COLOR = TextColor.Factory.fromString("#A01010");
  private final static TextColor MISSING_DATA_COLOR = TextColor.Factory.fromString("#404040");
  private final static TextColor DATA_COLOR = TextColor.Factory.fromString("#A0A0A0");
  private final Configuration configuration;
  private final Map<MachineKey, SortedMap<Long, MachineInfo>> machinesData;
  private final Map<ProcessKey, SortedMap<Long, EnhancedProcessInfo>> processesData;
  private final Map<RunKey, Map<Update.DataItemKey, List<Object>>> runsData;
  private final Map<RunKey, Map<Update.PlotItemKey, List<Update.PlotPoint>>> runsPlots;
  private final Map<MachineKey, TimedProgress> machinesProgress;
  private final Map<ProcessKey, TimedProgress> processesProgress;
  private final Map<RunKey, TimedProgress> runsProgress;
  private final Map<MachineKey, Status> machinesStatus;
  private final Map<ProcessKey, Status> processesStatus;
  private final ExecutorService clientsExecutorService;
  private final ScheduledExecutorService uiExecutorService;

  private Screen screen;

  public NetListenerServer(
      Configuration configuration
  ) {
    this.configuration = configuration;
    machinesData = new LinkedHashMap<>();
    processesData = new LinkedHashMap<>();
    runsData = new LinkedHashMap<>();
    runsPlots = new LinkedHashMap<>();
    machinesStatus = new HashMap<>();
    processesStatus = new HashMap<>();
    machinesProgress = new HashMap<>();
    processesProgress = new HashMap<>();
    runsProgress = new HashMap<>();
    clientsExecutorService = Executors.newFixedThreadPool(configuration.nOfClients());
    uiExecutorService = Executors.newSingleThreadScheduledExecutor();
    //prepare screen
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

  private static class CommandLineConfiguration {
    @Parameter(
        names = {"--port", "-p"},
        description = "Server port."
    )
    public int port = DEFAULT_CONFIGURATION.port;
    @Parameter(
        names = {"--key", "-k"},
        description = "Handshake key."
    )
    public String key = DEFAULT_CONFIGURATION.key;

    @Parameter(
        names = {"--help", "-h"},
        description = "Show this help.",
        help = true
    )
    public boolean help;
  }

  private record Configuration(
      float runsSplit, float legendSplit, float machinesProcessesSplit,
      int barLength, int areaPlotLength,
      int uiRefreshIntervalMillis, int machineHistorySeconds, int runDataHistorySize, int runPlotHistorySize,
      int port, String key, int nOfClients, double laterThreshold, double missingThreshold, double purgeThreshold
  ) {}

  private record EnhancedProcessInfo(
      ProcessInfo processInfo,
      int nOfRuns
  ) {}

  private record MachineKey(String machineName) {}

  private record ProcessKey(String machineName, String processName) {
    public MachineKey machineKey() {
      return new MachineKey(machineName);
    }
  }

  private record RunKey(String machineName, String processName, int runIndex) {
    public ProcessKey processKey() {
      return new ProcessKey(machineName, processName);
    }
  }

  private record Status(Instant lastContact, double pollInterval, TimeStatus status) {}

  private record TimedProgress(
      Instant initialContact,
      Instant lastContact,
      Progress initialProgress,
      Progress lastProgress,
      boolean isRunning
  ) {
    public Instant eta() {
      if (!isRunning) {
        return lastContact;
      }
      if (lastProgress.equals(Progress.NA) || (lastProgress.rate() == 0) || (lastProgress.rate() == initialProgress.rate())) {
        return Instant.MAX;
      }
      return initialContact.plus(Math.round(ChronoUnit.MILLIS.between(
          initialContact,
          Instant.now()
      ) / (lastProgress.rate() - initialProgress.rate())), ChronoUnit.MILLIS);
    }
  }

  private static <T> String areaPlot(SortedMap<Long, T> data, Function<T, Number> function, double min, int l) {
    return TextPlotter.areaPlot(
        new TreeMap<>(data.entrySet().stream().collect(Collectors.toMap(
            Map.Entry::getKey,
            e -> function.apply(e.getValue())
        ))),
        min,
        data.lastKey(),
        l
    );
  }

  private static <T> List<T> concatAndTrim(List<T> ts1, List<T> ts2, int n) {
    List<T> ts = Stream.of(ts1, ts2)
        .flatMap(List::stream)
        .toList();
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

  public static void main(String[] args) {
    CommandLineConfiguration cmdConfiguration = new CommandLineConfiguration();
    JCommander jc = JCommander.newBuilder()
        .addObject(cmdConfiguration)
        .build();
    jc.setProgramName(NetListenerServer.class.getName());
    try {
      jc.parse(args);
    } catch (ParameterException e) {
      e.usage();
      L.severe(String.format("Cannot read command line options: %s", e));
      System.exit(-1);
    } catch (RuntimeException e) {
      L.severe(e.getClass().getSimpleName() + ": " + e.getMessage());
      System.exit(-1);
    }
    //check help
    if (cmdConfiguration.help) {
      jc.usage();
      System.exit(0);
    }
    NetListenerServer server = new NetListenerServer(new Configuration(
        DEFAULT_CONFIGURATION.runsSplit,
        DEFAULT_CONFIGURATION.legendSplit,
        DEFAULT_CONFIGURATION.machinesProcessesSplit,
        DEFAULT_CONFIGURATION.barLength,
        DEFAULT_CONFIGURATION.barLength,
        DEFAULT_CONFIGURATION.uiRefreshIntervalMillis,
        DEFAULT_CONFIGURATION.machineHistorySeconds,
        DEFAULT_CONFIGURATION.runDataHistorySize,
        DEFAULT_CONFIGURATION.runPlotHistorySize,
        cmdConfiguration.port,
        cmdConfiguration.key,
        DEFAULT_CONFIGURATION.nOfClients,
        DEFAULT_CONFIGURATION.laterThreshold,
        DEFAULT_CONFIGURATION.missingThreshold,
        DEFAULT_CONFIGURATION.purgeThreshold
    ));
    server.run();
  }

  private static String progressPlot(Progress p, int l) {
    if (p == null) {
      return "";
    }
    return TextPlotter.horizontalBar(p.rate(), 0, 1, l, false);
  }

  private static <T> Trend trend(SortedMap<Long, T> data, Function<T, Number> function) {
    if (data.size() == 1) {
      return Trend.NONE;
    }
    double[] ds = data.values().stream().mapToDouble(v -> function.apply(v).doubleValue()).toArray();
    double d1 = ds[ds.length - 1];
    double d2 = ds[ds.length - 2];
    return Trend.from(d1, d2);
  }

  private void doHandshake(
      ObjectInputStream ois,
      ObjectOutputStream oos
  ) throws IOException {
    RandomGenerator rg = new Random();
    int n = rg.nextInt();
    try {
      oos.writeObject(NetUtils.encrypt(Integer.toString(n), configuration.key));
      int m = Integer.parseInt(NetUtils.decrypt((String) ois.readObject(), configuration.key));
      if (m != n + 1) {
        throw new IOException("Wrong answer to challenge from client: %d vs. %d".formatted(m, n + 1));
      }
    } catch (Exception e) {
      throw new IOException(e);
    }
  }

  private synchronized void refreshData() {
    //update statuses
    Instant now = Instant.now();
    machinesStatus.replaceAll((k, s) -> update(s, now));
    processesStatus.replaceAll((k, s) -> update(s, now));
    //purge
    List<MachineKey> downMachineKeys = machinesStatus.entrySet().stream()
        .filter(e -> e.getValue().status().equals(TimeStatus.PURGE))
        .map(Map.Entry::getKey)
        .toList();
    List<ProcessKey> downProcessKeys = processesStatus.entrySet().stream()
        .filter(e -> e.getValue().status().equals(TimeStatus.PURGE))
        .map(Map.Entry::getKey)
        .toList();
    downMachineKeys.forEach(mk -> {
      machinesData.remove(mk);
      machinesStatus.remove(mk);
    });
    downProcessKeys.forEach(pk -> {
      processesData.remove(pk);
      processesStatus.remove(pk);
    });
    List<RunKey> downRunKeys = runsData.keySet().stream()
        .filter(rk -> downProcessKeys.stream().anyMatch(pk -> pk.equals(rk.processKey())))
        .toList();
    downRunKeys.forEach(k -> {
      runsData.remove(k);
      runsProgress.remove(k);
      runsPlots.remove(k);
    });
    //trim history
    machinesData.values()
        .forEach(h -> {
          while (h.firstKey() < h.lastKey() - 1000L * configuration.machineHistorySeconds) {
            h.remove(h.firstKey());
          }
        });
    processesData.values()
        .forEach(h -> {
          while (h.firstKey() < h.lastKey() - 1000L * configuration.machineHistorySeconds) {
            h.remove(h.firstKey());
          }
        });
    //update progresses
    runsProgress.entrySet().stream()
        .collect(Collectors.groupingBy(
            e -> e.getKey().processKey(),
            Collectors.toList()
        ))
        .forEach((pk, m) -> processesProgress.put(
            pk,
            new TimedProgress(
                m.stream()
                    .map(Map.Entry::getValue)
                    .map(TimedProgress::initialContact)
                    .min(Instant::compareTo)
                    .orElse(Instant.now()),
                Instant.now(),
                new Progress(
                    0,
                    processesData.get(pk).get(processesData.get(pk).lastKey()).nOfRuns(),
                    m.stream()
                        .map(Map.Entry::getValue)
                        .mapToDouble(tp -> tp.initialProgress().rate())
                        .sum()
                ),
                new Progress(
                    0,
                    processesData.get(pk).get(processesData.get(pk).lastKey()).nOfRuns(),
                    m.stream()
                        .map(Map.Entry::getValue)
                        .mapToDouble(tp -> tp.lastProgress().rate())
                        .sum()
                ),
                true
            )
        ));
    processesProgress.entrySet().stream()
        .collect(Collectors.groupingBy(
            e -> e.getKey().machineKey(),
            Collectors.toList()
        )).forEach((mk, m) -> machinesProgress.put(
            mk,
            new TimedProgress(
                m.stream()
                    .map(Map.Entry::getValue)
                    .map(TimedProgress::initialContact)
                    .min(Instant::compareTo)
                    .orElse(Instant.now()),
                Instant.now(),
                new Progress(
                    m.stream()
                        .map(Map.Entry::getValue)
                        .mapToDouble(tp -> tp.initialProgress().rate())
                        .average()
                        .orElse(0)
                ),
                new Progress(
                    m.stream()
                        .map(Map.Entry::getValue)
                        .mapToDouble(tp -> tp.lastProgress().rate())
                        .average()
                        .orElse(0)
                ),
                true
            )
        ));
  }

  @Override
  public void run() {
    //start painter task
    uiExecutorService.scheduleAtFixedRate(
        () -> {
          try {
            refreshData();
            updateUI();
          } catch (RuntimeException e) {
            L.warning("Unexpected exception: %s".formatted(e));
            e.printStackTrace(); // TODO remove
          }
        },
        0,
        configuration.uiRefreshIntervalMillis,
        TimeUnit.MILLISECONDS
    );
    //start server
    try (ServerSocket serverSocket = new ServerSocket(configuration.port())) {
      L.info("Server started on port %d".formatted(configuration.port()));
      while (true) {
        try {
          Socket socket = serverSocket.accept();
          clientsExecutorService.submit(() -> {
            try (
                socket;
                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            ) {
              doHandshake(ois, oos);
              while (true) {
                Message message = (Message) ois.readObject();
                L.fine("Msg received with %d updates".formatted(message.updates().size()));
                storeMessage(message);
              }
            } catch (IOException e) {
              L.warning("Cannot open input stream due to: %s".formatted(e));
            } catch (ClassNotFoundException e) {
              L.warning("Cannot read message due to: %s".formatted(e));
            }
          });
        } catch (IOException e) {
          L.warning("Cannot accept connection due to; %s".formatted(e));
        }
      }
    } catch (IOException e) {
      L.severe("Cannot start server due to: %s".formatted(e));
    }
  }

  private void stop() {
    try {
      screen.stopScreen();
    } catch (IOException e) {
      L.warning(String.format("Cannot stop screen: %s", e));
    }
    uiExecutorService.shutdownNow();
    clientsExecutorService.shutdownNow();
  }

  private synchronized void storeMessage(Message message) {
    MachineKey machineKey = new MachineKey(message.machineInfo().machineName());
    machinesData.putIfAbsent(machineKey, new TreeMap<>());
    machinesData.get(machineKey).put(message.localTime(), message.machineInfo());
    machinesStatus.put(machineKey, new Status(Instant.now(), message.pollInterval(), TimeStatus.OK));
    ProcessKey processKey = new ProcessKey(message.machineInfo().machineName(), message.processInfo().processName());
    processesData.putIfAbsent(processKey, new TreeMap<>());
    processesData.get(processKey).put(
        message.localTime(),
        new EnhancedProcessInfo(message.processInfo(), message.nOfRuns())
    );
    processesStatus.put(processKey, new Status(Instant.now(), message.pollInterval(), TimeStatus.OK));
    for (Update update : message.updates()) {
      RunKey runKey = new RunKey(
          message.machineInfo().machineName(),
          message.processInfo().processName(),
          update.runIndex()
      );
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
      update.dataItems().forEach((dik, vs) -> runsData.get(runKey).merge(
          dik,
          vs,
          (ovs, nvs) -> concatAndTrim(ovs, nvs, configuration.runDataHistorySize)
      ));
      runsPlots.putIfAbsent(runKey, new LinkedHashMap<>());
      update.plotItems().forEach((pik, ps) -> runsPlots.get(runKey).merge(
          pik,
          ps,
          (ovs, nvs) -> concatAndTrim(ovs, nvs, configuration.runPlotHistorySize)
      ));
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
    //check keystrokes
    try {
      KeyStroke k = screen.pollInput();
      if (k != null && k.getCharacter() != null && ((k.getCharacter().equals('c') && k.isCtrlDown()) || k.getKeyType()
          .equals(KeyType.EOF))) {
        stop();
      }
    } catch (IOException e) {
      L.warning(String.format("Cannot check key strokes: %s", e));
    }
    //update size
    TerminalSize size = screen.doResizeIfNecessary();
    TextGraphics tg = screen.newTextGraphics();
    Rectangle r;
    if (size == null) {
      size = screen.getTerminalSize();
    } else {
      screen.clear();
    }
    //adjust rectangles
    Rectangle all = new Rectangle(new Point(0, 0), new Point(size.getColumns(), size.getRows()));
    Rectangle nR = all.splitVertically(configuration.runsSplit).get(0);
    Rectangle runsR = all.splitVertically(configuration.runsSplit).get(1);
    Rectangle nwR = nR.splitHorizontally(configuration.legendSplit).get(0);
    Rectangle legendR = nR.splitHorizontally(configuration.legendSplit).get(1);
    Rectangle machinesR = nwR.splitVertically(configuration.machinesProcessesSplit).get(0);
    Rectangle processesR = nwR.splitVertically(configuration.machinesProcessesSplit).get(1);
    //draw structure
    DrawUtils.drawFrame(
        tg,
        runsR,
        "Runs (%d) - Local time: %s".formatted(runsData.size(), COMPLETE_DATETIME_FORMAT.format(Instant.now())),
        FRAME_COLOR,
        FRAME_LABEL_COLOR
    );
    DrawUtils.drawFrame(tg, legendR, "Legend", FRAME_COLOR, FRAME_LABEL_COLOR);
    DrawUtils.drawFrame(
        tg,
        machinesR,
        "Machines (%d)".formatted(machinesData.size()),
        FRAME_COLOR,
        FRAME_LABEL_COLOR
    );
    DrawUtils.drawFrame(
        tg,
        processesR,
        "Experiments (%d)".formatted(processesData.size()),
        FRAME_COLOR,
        FRAME_LABEL_COLOR
    );
    //compute and show legend
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
        ).flatMap(List::stream)
        .sorted()
        .distinct()
        .map(s -> new LegendItem(StringUtils.collapse(s), s))
        .toList();
    int shortLabelW = legendItems.stream().mapToInt(p -> p.collapsed().length()).max().orElse(0);
    for (int i = 0; i < legendItems.size(); i = i + 1) {
      tg.setForegroundColor(DATA_LABEL_COLOR);
      DrawUtils.clipPut(tg, r, 0, i, legendItems.get(i).collapsed());
      tg.setForegroundColor(DATA_COLOR);
      DrawUtils.clipPut(tg, r, shortLabelW + 1, i, legendItems.get(i).name());
    }
    //compute and show machines
    Table<Cell> machinesTable = new ArrayTable<>(List.of(
        "",
        "",
        "Progress",
        "Machine",
        "Cores",
        "Load"
    ));
    forEach(machinesData, (i, mk, v) -> machinesTable.addRow(List.of(
        new ColoredStringCell(STATUS_STRING, machinesStatus.get(mk).status().getColor()),
        new StringCell("M%02d".formatted(i)),
        new StringCell(machinesProgress.containsKey(mk) ? progressPlot(
            machinesProgress.get(mk).lastProgress(),
            configuration.barLength
        ) : EMPTY_CELL_CONTENT),
        new StringCell(mk.machineName()),
        new StringCell(last(v, MachineInfo::numberOfProcessors, "%2d")),
        new CompositeCell(List.of(
            new StringCell(last(v, MachineInfo::cpuLoad, "%5.2f")),
            trend(v, MachineInfo::cpuLoad).cell(),
            new StringCell(areaPlot(
                v,
                MachineInfo::cpuLoad,
                v.lastKey() - configuration.machineHistorySeconds * 1000d,
                configuration.areaPlotLength
            ))
        ))
    )), false);
    DrawUtils.drawTable(tg, machinesR.inner(1), machinesTable, DATA_LABEL_COLOR, DATA_COLOR);
    //compute and show processes
    Table<Cell> processesTable = new ArrayTable<>(List.of(
        "",
        "",
        "ETA",
        "Progress",
        "Process",
        "Mac.",
        "User",
        "Used mem.",
        "Max mem."
    ));
    forEach(processesData, (i, pk, v) -> processesTable.addRow(List.of(
        new ColoredStringCell(STATUS_STRING, processesStatus.get(pk).status().getColor()),
        new StringCell("P%02d".formatted(i)),
        new StringCell(processesProgress.containsKey(pk)?eta(processesProgress.get(pk).eta()): EMPTY_CELL_CONTENT),
        new StringCell(processesProgress.containsKey(pk)?progressPlot(processesProgress.get(pk).lastProgress(), configuration.barLength): EMPTY_CELL_CONTENT),
        new StringCell(pk.processName()),
        new StringCell("M%02d".formatted(
            machinesData.keySet().stream().toList().indexOf(pk.machineKey())
        )),
        new StringCell(last(v, pi -> pi.processInfo().username(), "%s")),
        new CompositeCell(List.of(
            new StringCell(last(v, pi -> pi.processInfo().usedMemory() / 1024 / 1024, "%5d")),
            trend(v, pi -> pi.processInfo().usedMemory()).cell(),
            new StringCell(areaPlot(
                v,
                pi -> pi.processInfo().usedMemory(),
                v.lastKey() - configuration.machineHistorySeconds * 1000d,
                configuration.areaPlotLength
            ))
        )),
        new CompositeCell(List.of(
            new StringCell(last(v, pi -> pi.processInfo().maxMemory() / 1024 / 1024, "%5d")),
            trend(v, pi -> pi.processInfo().maxMemory()).cell()
        ))
    )), false);
    DrawUtils.drawTable(tg, processesR.inner(1), processesTable, DATA_LABEL_COLOR, DATA_COLOR);
    //compute and show runs
    Table<Cell> runsTable;
    List<String> columns = new ArrayList<>(List.of(
        "",
        "",
        "ETA",
        "Progress",
        "Proc."
    ));
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
    plotItemKeys.forEach(pik -> columns.add(StringUtils.collapse(pik.xName()) + "/" + StringUtils.collapse(pik.yName())));
    runsTable = new ArrayTable<>(columns);
    forEach(runsData, (i, rk, v) -> {
      List<Cell> row = new ArrayList<>();
      row.add(new ColoredStringCell(
          STATUS_STRING,
          (runsProgress.containsKey(rk) && runsProgress.get(rk).isRunning()) ? TimeStatus.OK.getColor() : TimeStatus.MISSING.getColor()
      ));
      row.add(new StringCell("R%03d".formatted(rk.runIndex())));
      row.add(new StringCell(runsProgress.containsKey(rk)?eta(runsProgress.get(rk).eta()):EMPTY_CELL_CONTENT));
      row.add(new StringCell(runsProgress.containsKey(rk)?progressPlot(runsProgress.get(rk).lastProgress(), configuration.barLength):EMPTY_CELL_CONTENT));
      row.add(new StringCell("P%02d".formatted(
          processesData.keySet().stream().toList().indexOf(rk.processKey())
      )));
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
            s = TextPlotter.areaPlot(
                data,
                pik.minX(),
                pik.maxX(),
                configuration.areaPlotLength()
            );
          }
        }
        row.add(new StringCell(s));
      });
      runsTable.addRow(row);
    }, true);
    DrawUtils.drawTable(tg, runsR.inner(1), runsTable, DATA_LABEL_COLOR, DATA_COLOR);
    //refresh
    try {
      screen.refresh();
    } catch (IOException e) {
      L.warning(String.format("Cannot refresh screen: %s", e));
    }
  }
}
