package it.units.malelab.jgea.tui;

import com.googlecode.lanterna.Symbols;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import it.units.malelab.jgea.core.listener.Listener;
import it.units.malelab.jgea.core.listener.ListenerFactory;
import it.units.malelab.jgea.core.listener.NamedFunction;
import it.units.malelab.jgea.core.listener.ProgressMonitor;
import it.units.malelab.jgea.core.util.Misc;
import it.units.malelab.jgea.core.util.Pair;
import it.units.malelab.jgea.core.util.StringUtils;
import it.units.malelab.jgea.tui.geom.Point;
import it.units.malelab.jgea.tui.geom.Rectangle;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.*;
import java.util.stream.Collectors;

import static it.units.malelab.jgea.tui.geom.DrawUtils.*;

/**
 * @author "Eric Medvet" on 2022/09/02 for jgea
 */
public class TerminalMonitor<E, K> extends Handler implements ListenerFactory<E, K>, ProgressMonitor {

  private final static Configuration DEFAULT_CONFIGURATION = new Configuration(0.7f, 0.8f, 0.5f, 250);

  private final static TextColor FRAME_COLOR = TextColor.Factory.fromString("#105010");
  private final static TextColor FRAME_LABEL_COLOR = TextColor.Factory.fromString("#10A010");
  private final static TextColor DATA_LABEL_COLOR = TextColor.Factory.fromString("#A01010");
  private final static TextColor MAIN_DATA_COLOR = TextColor.Factory.fromString("#F0F0F0");
  private final static TextColor DATA_COLOR = TextColor.Factory.fromString("#A0A0A0");
  private final static TextColor PLOT_BG_COLOR = TextColor.Factory.fromString("#101010");
  private final static TextColor PLOT1_COLOR = TextColor.Factory.fromString("#FF1010");
  private final static TextColor PLOT2_COLOR = TextColor.Factory.fromString("#10FF10");

  private final static String LEVEL_FORMAT = "%5.5s";
  private final static String DATETIME_FORMAT = "%1$tm-%1$td %1$tH:%1$tM:%1$tS";
  private final static String TIME_FORMAT = "%1$tH:%1$tM:%1$tS";

  private final static int LOG_HISTORY_SIZE = 100;
  private final static int RUN_HISTORY_SIZE = 200;

  private final static Map<Level, TextColor> LEVEL_COLORS = Map.ofEntries(
      Map.entry(Level.SEVERE, TextColor.Factory.fromString("#EE3E38")),
      Map.entry(Level.WARNING, TextColor.Factory.fromString("#FBA465")),
      Map.entry(Level.INFO, TextColor.Factory.fromString("#D8E46B")),
      Map.entry(Level.CONFIG, TextColor.Factory.fromString("#6D8700"))
  );
  private final static Logger L = Logger.getLogger(TerminalMonitor.class.getName());
  private final List<Pair<? extends NamedFunction<? super E, ?>, Integer>> ePairs;
  private final List<Pair<? extends NamedFunction<? super K, ?>, Integer>> kPairs;
  private final Configuration configuration;
  private final ScheduledFuture<?> painterTask;
  private final List<LogRecord> logRecords;
  private final List<List<String>> runRows;
  private final Instant startingInstant;

  private Screen screen;
  private double lastProgress;
  private String lastProgressMessage;
  private Instant lastProgressInstant;


  public TerminalMonitor(
      List<NamedFunction<? super E, ?>> eFunctions,
      List<NamedFunction<? super K, ?>> kFunctions
  ) {
    this(eFunctions, kFunctions, DEFAULT_CONFIGURATION);
  }

  public TerminalMonitor(
      List<NamedFunction<? super E, ?>> eFunctions,
      List<NamedFunction<? super K, ?>> kFunctions,
      Configuration configuration
  ) {
    //set functions
    ePairs = eFunctions.stream().map(f -> Pair.of(
        f,
        Math.max(StringUtils.collapse(f.getName()).length(), StringUtils.formatSize(f.getFormat()))
    )).collect(Collectors.toList());
    kPairs = kFunctions.stream().map(f -> Pair.of(
        f,
        Math.max(StringUtils.collapse(f.getName()).length(), StringUtils.formatSize(f.getFormat()))
    )).collect(Collectors.toList());
    //read configuration
    this.configuration = configuration;
    //prepare data object stores
    logRecords = new LinkedList<>();
    runRows = new LinkedList<>();
    //prepare screen
    DefaultTerminalFactory defaultTerminalFactory = new DefaultTerminalFactory();
    try {
      screen = defaultTerminalFactory.createScreen();
      screen.startScreen();
    } catch (IOException e) {
      L.severe(String.format("Cannot create or start screen: %s", e));
    }
    if (screen != null) {
      screen.setCursorPosition(null);
      repaint();
    }
    //start painting scheduler
    painterTask = Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(
        this::repaint,
        0,
        configuration.refreshIntervalMillis,
        TimeUnit.MILLISECONDS
    );
    //capture logs
    Logger mainLogger = Logger.getLogger("");
    mainLogger.setLevel(Level.CONFIG);
    mainLogger.addHandler(this);
    Arrays.stream(mainLogger.getHandlers()).filter(h -> h instanceof ConsoleHandler).forEach(mainLogger::removeHandler);
    //set default locale
    Locale.setDefault(Locale.ENGLISH);
    startingInstant = Instant.now();
  }

  public record Configuration(
      float verticalSplit,
      float leftHorizontalSplit,
      float rightHorizontalSplit,
      int refreshIntervalMillis
  ) {}

  public static void main(String[] args) {
    Instant start = Instant.now();
    TerminalMonitor<Object, Object> monitor = new TerminalMonitor<>(List.of(), List.of());
    ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
    service.scheduleAtFixedRate(() -> L.info("It's " + new Date()), 0, 2, TimeUnit.SECONDS);
    service.scheduleAtFixedRate(() -> {
      double p = Math.min(1d, ChronoUnit.MILLIS.between(start, Instant.now()) / 10000d);
      monitor.notify(p, "P\n" + new Date());
    }, 0, 1, TimeUnit.SECONDS);
  }

  @Override
  public Listener<E> build(K k) {
    List<String> kItems = kPairs.stream()
        .map(p -> String.format(p.first().getFormat(), p.first().apply(k)))
        .toList();
    return e -> {
      List<String> eItems = ePairs.stream()
          .map(p -> String.format(p.first().getFormat(), p.first().apply(e)))
          .toList();
      List<String> row = new ArrayList<>(eItems);
      row.addAll(kItems);
      runRows.add(row);
      while (runRows.size() > RUN_HISTORY_SIZE) {
        runRows.remove(0);
      }
    };
  }

  @Override
  public void shutdown() {
    stop();
  }

  @Override
  public synchronized void notify(double progress, String message) {
    lastProgress = progress;
    lastProgressMessage = message;
    lastProgressInstant = Instant.now();
  }

  @Override
  public synchronized void publish(LogRecord record) {
    logRecords.add(record);
    while (logRecords.size() > LOG_HISTORY_SIZE) {
      logRecords.remove(0);
    }
  }

  private void repaint() {
    if (screen == null) {
      return;
    }
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
    Rectangle e = all.splitHorizontally(configuration.verticalSplit).get(0);
    Rectangle w = all.splitHorizontally(configuration.verticalSplit).get(1);
    Rectangle runR = e.splitVertically(configuration.leftHorizontalSplit).get(0);
    Rectangle logR = e.splitVertically(configuration.leftHorizontalSplit).get(1);
    Rectangle legendR = w.splitVertically(configuration.rightHorizontalSplit).get(0);
    Rectangle statusR = w.splitVertically(configuration.rightHorizontalSplit).get(1);
    //draw structure
    drawFrame(tg, runR, "Ongoing run", FRAME_COLOR, FRAME_LABEL_COLOR);
    drawFrame(tg, legendR, "Legend", FRAME_COLOR, FRAME_LABEL_COLOR);
    drawFrame(tg, logR, "Log", FRAME_COLOR, FRAME_LABEL_COLOR);
    drawFrame(tg, statusR, "Status", FRAME_COLOR, FRAME_LABEL_COLOR);
    //draw data: logs
    int levelW = String.format(LEVEL_FORMAT, Level.WARNING).length();
    int dateW = String.format(DATETIME_FORMAT, Instant.now().getEpochSecond()).length();
    r = logR.inner(1);
    clear(tg, r);
    for (int i = 0; i < Math.min(r.h(), logRecords.size()); i = i + 1) {
      LogRecord record = logRecords.get(logRecords.size() - 1 - i);
      tg.setForegroundColor(LEVEL_COLORS.getOrDefault(record.getLevel(), DATA_COLOR));
      clipPut(tg, r, 0, i, String.format(LEVEL_FORMAT, record.getLevel()));
      tg.setForegroundColor(MAIN_DATA_COLOR);
      clipPut(tg, r, levelW + 1, i, String.format(DATETIME_FORMAT, record.getMillis()));
      tg.setForegroundColor(DATA_COLOR);
      clipPut(tg, r, levelW + 1 + dateW + 1, i, record.getMessage());
    }
    //draw data: status
    r = statusR.inner(1);
    clear(tg, r);
    tg.setForegroundColor(DATA_LABEL_COLOR);
    clipPut(tg, r, 0, 0, "Machine:");
    clipPut(tg, r, 0, 1, "Loc time:");
    clipPut(tg, r, 0, 2, "Memory:");
    clipPut(tg, r, 0, 3, "Progress:");
    clipPut(tg, r, 0, 4, "Last progress message:");
    tg.setForegroundColor(MAIN_DATA_COLOR);
    clipPut(tg, r, 10, 0, StringUtils.getMachineName());
    clipPut(tg, r, 10, 1, String.format(DATETIME_FORMAT, Date.from(Instant.now())));
    float maxGigaMemory = Runtime.getRuntime().maxMemory() / 1024f / 1024f / 1024f;
    float usedGigaMemory = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime()
        .freeMemory()) / 1024f / 1024f / 1024f;
    drawBar(tg, r, 10, 2, usedGigaMemory, 0, maxGigaMemory, 10, PLOT1_COLOR, PLOT_BG_COLOR);
    clipPut(tg, r, 21, 2, String.format("%.1fGB", maxGigaMemory));
    drawBar(tg, r, 10, 3, lastProgress, 0, 1, 10, PLOT1_COLOR, PLOT_BG_COLOR);
    if (lastProgressInstant != null) {
      if (lastProgress > 0) {
        Instant eta = startingInstant.plus(
            Math.round(ChronoUnit.MILLIS.between(startingInstant, lastProgressInstant) / lastProgress),
            ChronoUnit.MILLIS
        );
        clipPut(tg, r, 21, 3, String.format(Symbols.ARROW_RIGHT + DATETIME_FORMAT, Date.from(eta)));
      }
    }
    if (lastProgressMessage != null) {
      clipPut(tg, r, 0, 5, lastProgressMessage);
    }
    //draw data: legend
    r = legendR.inner(1);
    clear(tg, r);
    List<Pair<String, String>> items = Misc.concat(List.of(kPairs, ePairs))
        .stream()
        .map(p -> new Pair<>(
            StringUtils.collapse(p.first().getName()),
            p.first().getName()
        ))
        .toList();
    int shortLabelW = items.stream().mapToInt(p -> p.first().length()).max().orElse(0);
    for (int i = 0; i < items.size(); i = i + 1) {
      tg.setForegroundColor(DATA_LABEL_COLOR);
      clipPut(tg, r, 0, i, items.get(i).first());
      tg.setForegroundColor(DATA_COLOR);
      clipPut(tg, r, shortLabelW + 1, i, items.get(i).second());
    }
    //draw data: run
    r = runR.inner(1);
    clear(tg, r);
    List<String> headers = Misc.concat(List.of(kPairs, ePairs))
        .stream()
        .map(p -> StringUtils.justify(StringUtils.collapse(p.first().getName()), p.second()))
        .toList();
    tg.setForegroundColor(DATA_LABEL_COLOR);
    clipPut(tg, r, 0, 0, String.join(" ", headers));
    tg.setForegroundColor(DATA_COLOR);
    for (int i = 0; i < Math.min(runRows.size(), r.h()); i = i + 1) {
      List<String> row = runRows.get(runRows.size() - 1 - i);
      clipPut(tg, r, 0, i+1, String.join(" ", row));
    }
    //refresh
    try {
      screen.refresh();
    } catch (IOException ex) {
      L.warning(String.format("Cannot refresh screen: %s", ex));
    }
  }

  @Override
  public void flush() {

  }

  @Override
  public void close() throws SecurityException {

  }

  private void stop() {
    try {
      screen.stopScreen();
    } catch (IOException e) {
      L.warning(String.format("Cannot stop screen: %s", e));
    }
    painterTask.cancel(false);
    L.info("Closed");
    Logger.getLogger("").removeHandler(this);
    System.exit(1);
  }

}
