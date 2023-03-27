package io.github.ericmedvet.jgea.experimenter.net;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import io.github.ericmedvet.jgea.core.util.ArrayTable;
import io.github.ericmedvet.jgea.core.util.StringUtils;
import io.github.ericmedvet.jgea.core.util.Table;
import io.github.ericmedvet.jgea.tui.table.Cell;
import io.github.ericmedvet.jgea.tui.table.ColoredStringCell;
import io.github.ericmedvet.jgea.tui.table.StringCell;
import io.github.ericmedvet.jgea.tui.table.TrendingCell;
import io.github.ericmedvet.jgea.tui.util.DrawUtils;
import io.github.ericmedvet.jgea.tui.util.Point;
import io.github.ericmedvet.jgea.tui.util.Rectangle;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author "Eric Medvet" on 2023/03/26 for jgea
 */
public class NetListenerServer implements Runnable {

  private final static Logger L = Logger.getLogger(NetListenerServer.class.getName());
  private final static Configuration DEFAULT_CONFIGURATION = new Configuration(
      0.5f,
      0.5f,
      0.5f,
      20,
      250,
      10979,
      100,
      2,
      5,
      20
  );
  private final static String STATUS_STRING = "⬤";

  private final static TextColor FRAME_COLOR = TextColor.Factory.fromString("#105010");
  private final static TextColor FRAME_LABEL_COLOR = TextColor.Factory.fromString("#10A010");
  private final static TextColor DATA_LABEL_COLOR = TextColor.Factory.fromString("#A01010");
  private final static TextColor MAIN_DATA_COLOR = TextColor.Factory.fromString("#F0F0F0");
  private final static TextColor DATA_COLOR = TextColor.Factory.fromString("#A0A0A0");
  private final static TextColor PLOT_BG_COLOR = TextColor.Factory.fromString("#101010");
  private final static TextColor PLOT1_COLOR = TextColor.Factory.fromString("#FF1010");
  private final static TextColor PLOT2_COLOR = TextColor.Factory.fromString("#105010");

  private final Configuration configuration;
  private final Map<MachineKey, SortedMap<Long, MachineInfo>> machinesData;
  private final Map<ProcessKey, SortedMap<Long, ProcessInfo>> processesData;
  private final Map<RunKey, Map<ItemKey, SortedMap<Long, Object>>> runsData;
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
    machinesStatus = new HashMap<>();
    processesStatus = new HashMap<>();
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
    OK(TextColor.Factory.fromString("#D8E46B")),
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

  public record Configuration(
      float runsSplit, float legendSplit, float machinesProcessesSplit,
      int barLength, int refreshIntervalMillis,
      int port, int nOfClients, double laterThreshold, double missingThreshold, double purgeThreshold
  ) {}

  private record ItemKey(String name, String format) {}

  private record MachineKey(String machineName) {}

  private record ProcessKey(String machineName, String processName) {}

  private record RunKey(String machineName, String processName, int runIndex) {}

  private record Status(Instant lastContact, double pollInterval, TimeStatus status) {}

  public static void main(String[] args) {
    NetListenerServer server = new NetListenerServer(DEFAULT_CONFIGURATION);
    server.run();
  }

  private static <T> TrendingCell trendingCell(SortedMap<Long, T> data, Function<T, Number> function, String format) {
    Number d = function.apply(data.get(data.lastKey()));
    if (data.size() == 1) {
      return new TrendingCell(d, format, TrendingCell.Trend.NONE);
    }
    double[] ds = data.values().stream().mapToDouble(v -> function.apply(v).doubleValue()).toArray();
    double d1 = ds[ds.length - 1];
    double d2 = ds[ds.length - 2];
    return new TrendingCell(d, format, TrendingCell.Trend.from(d1, d2));
  }


  private void refreshData() {
    //update statuses
    Instant now = Instant.now();
    machinesStatus.replaceAll((k, s) -> update(s, now));
    processesStatus.replaceAll((k, s) -> update(s, now));
    //purge
    machinesStatus.entrySet().stream()
        .filter(e -> e.getValue().status().equals(TimeStatus.PURGE))
        .forEach(e -> machinesData.remove(e.getKey()));
    processesStatus.entrySet().stream()
        .filter(e -> e.getValue().status().equals(TimeStatus.PURGE))
        .forEach(e -> processesData.remove(e.getKey()));
    Set<String> upMachines = machinesStatus.keySet().stream().map(MachineKey::machineName).collect(Collectors.toSet());
    List<RunKey> downRunKeys = runsData.keySet().stream()
        .filter(rk -> !upMachines.contains(rk.machineName()) || processesStatus.keySet()
            .stream()
            .noneMatch(pk -> pk.machineName().equals(rk.machineName()) && pk.processName().equals(rk.processName()))
        )
        .toList();
    downRunKeys.forEach(runsData.keySet()::remove);
  }

  @Override
  public void run() {
    //start painter task
    uiExecutorService.scheduleAtFixedRate(
        () -> {
          refreshData();
          updateUI();
        },
        0,
        configuration.refreshIntervalMillis,
        TimeUnit.MILLISECONDS
    );
    //start server
    try (ServerSocket serverSocket = new ServerSocket(configuration.port())) {
      L.info("Server started on port %d".formatted(configuration.port()));
      while (true) {
        try {
          Socket socket = serverSocket.accept();
          clientsExecutorService.submit(() -> {
            try (socket; ObjectInputStream ois = new ObjectInputStream(socket.getInputStream())) {
              Message message = (Message) ois.readObject();
              L.fine("Msg received with %d updates".formatted(message.updates().size()));
              storeMessage(message);
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
    processesData.get(processKey).put(message.localTime(), message.processInfo());
    processesStatus.put(processKey, new Status(Instant.now(), message.pollInterval(), TimeStatus.OK));
    for (Update update : message.updates()) {
      RunKey runKey = new RunKey(
          message.machineInfo().machineName(),
          message.processInfo().processName(),
          update.runIndex()
      );
      runsData.putIfAbsent(runKey, new LinkedHashMap<>());
      Map<ItemKey, SortedMap<Long, Object>> runData = runsData.get(runKey);
      for (Item item : update.items()) {
        ItemKey itemKey = new ItemKey(item.name(), item.format());
        runData.putIfAbsent(itemKey, new TreeMap<>());
        runData.get(itemKey).put(update.localTime(), item.value());
      }
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

  private void updateUI() {
    //check keystrokes
    try {
      KeyStroke k = screen.pollInput();
      if (k != null && ((k.getCharacter().equals('c') && k.isCtrlDown()) || k.getKeyType().equals(KeyType.EOF))) {
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
    DrawUtils.drawFrame(tg, runsR, "Runs", FRAME_COLOR, FRAME_LABEL_COLOR);
    DrawUtils.drawFrame(tg, legendR, "Legend", FRAME_COLOR, FRAME_LABEL_COLOR);
    DrawUtils.drawFrame(tg, machinesR, "Machines", FRAME_COLOR, FRAME_LABEL_COLOR);
    DrawUtils.drawFrame(tg, processesR, "Processes", FRAME_COLOR, FRAME_LABEL_COLOR);
    //compute and show legend
    synchronized (runsData) {
      r = legendR.inner(1);
      record LegendItem(String collapsed, String name) {}
      List<LegendItem> legendItems = runsData.values().stream()
          .map(Map::keySet)
          .flatMap(Set::stream)
          .map(ik -> new LegendItem(StringUtils.collapse(ik.name()), ik.name()))
          .distinct()
          .toList();
      int shortLabelW = legendItems.stream().mapToInt(p -> p.collapsed().length()).max().orElse(0);
      for (int i = 0; i < legendItems.size(); i = i + 1) {
        tg.setForegroundColor(DATA_LABEL_COLOR);
        DrawUtils.clipPut(tg, r, 0, i, legendItems.get(i).collapsed());
        tg.setForegroundColor(DATA_COLOR);
        DrawUtils.clipPut(tg, r, shortLabelW + 1, i, legendItems.get(i).name());
      }
    }
    //compute and show machines
    Table<Cell> machinesTable = new ArrayTable<>(List.of(
        "",
        "Name",
        "Cores",
        "Load"
    ));
    synchronized (machinesData) {
      machinesData.forEach((key, value) -> machinesTable.addRow(List.of(
          new ColoredStringCell(STATUS_STRING, machinesStatus.get(key).status().getColor()),
          new StringCell(key.machineName()),
          trendingCell(value, MachineInfo::numberOfProcessors, "%2d"),
          trendingCell(value, MachineInfo::cpuLoad, "%5.2f")
      )));
    }
    DrawUtils.drawTable(tg, machinesR.inner(1), machinesTable, DATA_LABEL_COLOR, DATA_COLOR);

    //refresh
    try {
      screen.refresh();
    } catch (IOException e) {
      L.warning(String.format("Cannot refresh screen: %s", e));
    }
  }

}
