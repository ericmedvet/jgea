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
import io.github.ericmedvet.jgea.core.util.TextPlotter;
import io.github.ericmedvet.jgea.tui.table.Cell;
import io.github.ericmedvet.jgea.tui.table.ColoredStringCell;
import io.github.ericmedvet.jgea.tui.table.CompositeCell;
import io.github.ericmedvet.jgea.tui.table.StringCell;
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
      8,
      8,
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
      return new ColoredStringCell("" + string, color);
    }

    public TextColor getColor() {
      return color;
    }

    public char getString() {
      return string;
    }
  }

  private record ItemKey(String name, String format) {}

  private record MachineKey(String machineName) {}

  private record ProcessKey(String machineName, String processName) {}

  private record RunKey(String machineName, String processName, int runIndex) {}

  private record Status(Instant lastContact, double pollInterval, TimeStatus status) {}

  public record Configuration(
      float runsSplit, float legendSplit, float machinesProcessesSplit,
      int barLength, int plotLenght, int refreshIntervalMillis,
      int port, int nOfClients, double laterThreshold, double missingThreshold, double purgeThreshold
  ) {}

  public static void main(String[] args) {
    NetListenerServer server = new NetListenerServer(DEFAULT_CONFIGURATION);
    server.run();
  }

  private static <T> String last(SortedMap<Long, T> data, Function<T, ?> function, String format) {
    Object v = function.apply(data.get(data.lastKey()));
    try {
      return format.formatted(v);
    } catch (RuntimeException e) {
      return "F_ERR";
    }
  }

  private static <T> String plot(SortedMap<Long, T> data, Function<T, Number> function, int l) {
    return TextPlotter.barplot(data.values().stream().map(v -> function.apply(v).doubleValue()).toList(), l);
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
          new StringCell(last(value, MachineInfo::numberOfProcessors, "%2d")),
          new CompositeCell(List.of(
              trend(value, MachineInfo::cpuLoad).cell(),
              new StringCell(last(value, MachineInfo::cpuLoad, "%5.2f")),
              new StringCell(plot(value, MachineInfo::cpuLoad, configuration.barLength))
          ))
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
