package it.units.malelab.jgea.tui;

import com.googlecode.lanterna.SGR;
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
import it.units.malelab.jgea.core.util.TextPlotter;
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

/**
 * @author "Eric Medvet" on 2022/09/02 for jgea
 */
public class TerminalMonitor<E, K> extends Handler implements ListenerFactory<E, K>, ProgressMonitor {

  private final static Configuration DEFAULT_CONFIGURATION = new Configuration(0.7f, 0.8f, 0.5f, 250);

  private final static TextColor FRAME_COLOR = TextColor.ANSI.YELLOW;
  private final static TextColor FRAME_LABEL_COLOR = TextColor.ANSI.YELLOW_BRIGHT;
  private final static TextColor DATA_LABEL_COLOR = TextColor.ANSI.MAGENTA_BRIGHT;
  private final static TextColor MAIN_DATA_COLOR = TextColor.ANSI.WHITE_BRIGHT;
  private final static TextColor DATA_COLOR = TextColor.ANSI.WHITE;

  private final static String LEVEL_FORMAT = "%5.5s";
  private final static String DATETIME_FORMAT = "%1$tm-%1$td %1$tH:%1$tM:%1$tS";
  private final static String TIME_FORMAT = "%1$tH:%1$tM:%1$tS";

  private final static int MAX_LOG_RECORDS = 100;

  private final static Map<Level, TextColor> LEVEL_COLORS = Map.ofEntries(
      Map.entry(Level.SEVERE, TextColor.ANSI.RED_BRIGHT),
      Map.entry(Level.WARNING, TextColor.ANSI.RED),
      Map.entry(Level.INFO, TextColor.ANSI.GREEN),
      Map.entry(Level.CONFIG, TextColor.ANSI.MAGENTA)
  );
  private final static Logger L = Logger.getLogger(TerminalMonitor.class.getName());
  private final List<Pair<? extends NamedFunction<? super E, ?>, Integer>> ePairs;
  private final List<Pair<? extends NamedFunction<? super K, ?>, Integer>> kPairs;
  private final Configuration configuration;
  private final ScheduledFuture<?> painterTask;
  private final List<LogRecord> logRecords;
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

  private static void clipPut(TextGraphics tg, Rectangle r, int x, int y, String s, SGR... sgrs) {
    clipPut(tg, r, new Point(x, y), s, sgrs);
  }

  private static void clipPut(TextGraphics tg, Rectangle r, Point p, String s, SGR... sgrs) {
    if (p.y() >= r.h() || p.y() < 0) {
      return;
    }
    int headD = Math.max(0, -p.x());
    int tailD = Math.max(0, p.x() + s.length() - r.w());
    if (s.length() - headD - tailD <= 0) {
      return;
    }
    s = s.substring(headD, s.length() - tailD);
    if (sgrs.length == 0) {
      tg.putString(p.delta(headD + r.min().x(), r.min().y()).tp(), s);
    } else if (sgrs.length == 1) {
      tg.putString(p.delta(headD + r.min().x(), r.min().y()).tp(), s, sgrs[0]);
    } else {
      tg.putString(p.delta(headD + r.min().x(), r.min().y()).tp(), s, sgrs[0], sgrs);
    }
    //multiline
    if (s.lines().count() > 1) {
      List<String> lines = s.lines().toList();
      for (int i = 1; i < lines.size(); i++) {
        clipPut(tg, r, p.delta(0, i), lines.get(i), sgrs);
      }
    }
  }

  private static void drawFrame(TextGraphics tg, Rectangle r, String label) {
    tg.setForegroundColor(FRAME_COLOR);
    tg.drawLine(r.ne().delta(1, 0).tp(), r.nw().delta(-1, 0).tp(), Symbols.SINGLE_LINE_HORIZONTAL);
    tg.drawLine(r.se().delta(1, 0).tp(), r.sw().delta(-1, 0).tp(), Symbols.SINGLE_LINE_HORIZONTAL);
    tg.drawLine(r.ne().delta(0, 1).tp(), r.se().delta(0, -1).tp(), Symbols.SINGLE_LINE_VERTICAL);
    tg.drawLine(r.nw().delta(0, 1).tp(), r.sw().delta(0, -1).tp(), Symbols.SINGLE_LINE_VERTICAL);
    tg.setCharacter(r.ne().tp(), Symbols.SINGLE_LINE_TOP_LEFT_CORNER);
    tg.setCharacter(r.se().tp(), Symbols.SINGLE_LINE_BOTTOM_LEFT_CORNER);
    tg.setCharacter(r.nw().tp(), Symbols.SINGLE_LINE_TOP_RIGHT_CORNER);
    tg.setCharacter(r.sw().tp(), Symbols.SINGLE_LINE_BOTTOM_RIGHT_CORNER);
    tg.setForegroundColor(FRAME_LABEL_COLOR);
    clipPut(tg, r, 2, 0, "[" + label + "]");
  }

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
    return e -> {
    };
  }

  @Override
  public void shutdown() {
    if (screen != null) {
      try {
        screen.stopScreen();
      } catch (IOException e) {
        L.warning(String.format("Cannot stop screen: %s", e));
      }
    }
  }

  @Override
  public synchronized void notify(double progress, String message) {
    lastProgress = progress;
    lastProgressMessage = message;
    lastProgressInstant = Instant.now();
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
    drawFrame(tg, runR, "Ongoing run");
    drawFrame(tg, legendR, "Legend");
    drawFrame(tg, logR, "Log");
    drawFrame(tg, statusR, "Status");
    //draw data: logs
    int levelW = String.format(LEVEL_FORMAT, Level.WARNING).length();
    int dateW = String.format(DATETIME_FORMAT, Instant.now().getEpochSecond()).length();
    r = logR.inner(1);
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
    clipPut(tg, r, 10, 2, TextPlotter.horizontalBar(usedGigaMemory, 0, maxGigaMemory, 10));
    clipPut(tg, r, 21, 2, String.format("%.1fGB", maxGigaMemory));
    clipPut(tg, r, 10, 3, TextPlotter.horizontalBar(lastProgress, 0, 1, 10));
    if (lastProgressInstant != null) {
      Instant eta = startingInstant.plus(
          Math.round(ChronoUnit.MILLIS.between(startingInstant, lastProgressInstant) / lastProgress),
          ChronoUnit.MILLIS
      );
      clipPut(tg, r, 21, 3, String.format(Symbols.ARROW_RIGHT + DATETIME_FORMAT, Date.from(eta)));
    }
    if (lastProgressMessage != null) {
      clipPut(tg, r, 0, 5, lastProgressMessage);
    }
    //draw data: legend
    r = legendR;
    List<Pair<String, String>> items = Misc.concat(List.of(kPairs, ePairs))
        .stream()
        .map(p -> new Pair<>(
            StringUtils.collapse(p.first().getName()),
            p.first().getName()
        ))
        .toList();
    for (int i = 0; i < items.size(); i++) {
      tg.setForegroundColor(DATA_LABEL_COLOR);
      clipPut(tg, r, 0, i, items.get(i).first());
      tg.setForegroundColor(DATA_COLOR);
      clipPut(tg, r, 0, i, items.get(i).second());
    }
    //refresh
    try {
      screen.refresh();
    } catch (IOException ex) {
      L.warning(String.format("Cannot refresh screen: %s", ex));
    }
  }

  @Override
  public synchronized void publish(LogRecord record) {
    logRecords.add(record);
    while (logRecords.size() > MAX_LOG_RECORDS) {
      logRecords.remove(0);
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
