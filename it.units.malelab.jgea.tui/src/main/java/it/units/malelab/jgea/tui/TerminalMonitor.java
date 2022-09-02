package it.units.malelab.jgea.tui;

import com.googlecode.lanterna.*;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import it.units.malelab.jgea.core.listener.Listener;
import it.units.malelab.jgea.core.listener.ListenerFactory;
import it.units.malelab.jgea.core.listener.NamedFunction;
import it.units.malelab.jgea.core.listener.ProgressMonitor;
import it.units.malelab.jgea.core.util.Pair;
import it.units.malelab.jgea.core.util.StringUtils;

import java.io.IOException;
import java.time.Instant;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author "Eric Medvet" on 2022/09/02 for jgea
 */
public class TerminalMonitor<E, K> extends Handler implements ListenerFactory<E, K>, ProgressMonitor {

  private final static TextColor FRAME_COLOR = TextColor.ANSI.YELLOW;
  private final static TextColor FRAME_LABEL_COLOR = TextColor.ANSI.YELLOW_BRIGHT;
  private final static TextColor DATA_LABEL_COLOR = TextColor.ANSI.MAGENTA_BRIGHT;
  private final static TextColor MAIN_DATA_COLOR = TextColor.ANSI.WHITE_BRIGHT;
  private final static TextColor DATA_COLOR = TextColor.ANSI.WHITE;

  private final static String LEVEL_FORMAT = "%5.5s";
  private final static String LOG_DATE_FORMAT = "%1$tm-%1$td %1$tH:%1$tM:%1$tS";

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
  private final List<LogRecord> logRecords;
  private Screen screen;

  public TerminalMonitor(
      List<NamedFunction<? super E, ?>> eFunctions,
      List<NamedFunction<? super K, ?>> kFunctions
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
    configuration = new Configuration(0.7f, 0.8f, 0.5f);
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
  }

  private record Configuration(
      float verticalSplit,
      float leftHorizontalSplit,
      float rightHorizontalSplit
  ) {}

  private record Point(int x, int y) {
    public Point delta(int dx, int dy) {
      return new Point(x + dx, y + dy);
    }

    public TerminalPosition tp() {
      return new TerminalPosition(x, y);
    }
  }

  private record Rectangle(Point min, Point max) {
    public int h() {
      return max().y() - min().y();
    }

    public Point ne() {
      return min();
    }

    public Point nw() {
      return new Point(max().x() - 1, min().y());
    }

    public Point se() {
      return new Point(min().x(), max().y() - 1);
    }

    public Point sw() {
      return max().delta(-1, -1);
    }

    public int w() {
      return max().x() - min().x();
    }
  }

  private static void clipPut(Point p, Rectangle clip, String s, TextGraphics tg) {
    if (p.y() >= clip.max().y() || p.y() < clip.min().y()) {
      return;
    }
    int headD = Math.max(0, clip.min().x() - p.x());
    int tailD = Math.max(0, p.x() + s.length() - clip.max().x());
    if (s.length() - headD - tailD <= 0) {
      return;
    }
    s = s.substring(headD, s.length() - tailD);
    tg.putString(p.delta(headD, 0).tp(), s);
  }

  public static void main(String[] args) {
    System.out.println("starting");
    TerminalMonitor<Object, Object> tm = new TerminalMonitor<>(List.of(), List.of());
    Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(
        () -> {
          tm.publish(new LogRecord(Level.INFO, "It's " + new Date()));
        },
        0,
        2,
        TimeUnit.SECONDS
    );
  }

  @Override
  public Listener<E> build(K k) {
    return null;
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

  private void kill() {
    try {
      screen.stopScreen();
    } catch (IOException e) {
      L.warning(String.format("Cannot stop screen: %s", e));
    }
    System.exit(0);
  }

  @Override
  public void notify(double progress, String message) {

  }

  @Override
  public synchronized void publish(LogRecord record) {
    logRecords.add(record);
    while (logRecords.size() > MAX_LOG_RECORDS) {
      logRecords.remove(0);
    }
    repaint();
  }

  @Override
  public void flush() {

  }

  @Override
  public void close() throws SecurityException {

  }

  private void repaint() {
    //check keystrokes
    try {
      KeyStroke k = screen.pollInput();
      if (k != null && (k.getKeyType().equals(KeyType.Escape) || k.getKeyType().equals(KeyType.EOF))) {
        kill();
      }
    } catch (IOException e) {
      L.warning(String.format("Cannot check key strokes: %s", e));
    }
    //update size
    TerminalSize size = screen.doResizeIfNecessary();
    if (size == null) {
      size = screen.getTerminalSize();
    } else {
      screen.clear();
    }
    Rectangle all = new Rectangle(new Point(0, 0), new Point(size.getColumns(), size.getRows()));
    Rectangle ne = new Rectangle(
        all.min().delta(1, 1),
        new Point(
            (int) (all.max().x() * configuration.verticalSplit()),
            (int) (all.max().y() * configuration.leftHorizontalSplit())
        )
    );
    Rectangle se = new Rectangle(
        new Point(ne.min().x(), ne.max().y() + 1),
        new Point(ne.max.x(), all.max().y() - 1)
    );
    Rectangle nw = new Rectangle(
        new Point(ne.max().x() + 1, all.min().y() + 1),
        new Point(
            all.max().x() - 1,
            (int) (all.max().y() * configuration.rightHorizontalSplit())
        )
    );
    Rectangle sw = new Rectangle(
        new Point(nw.min().x(), nw.max().y() + 1),
        new Point(all.max.x() - 1, all.max().y() - 1)
    );
    //draw structure
    TextGraphics tg = screen.newTextGraphics();
    tg.setForegroundColor(FRAME_COLOR);
    tg.drawLine(all.ne().tp(), all.nw().tp(), Symbols.SINGLE_LINE_HORIZONTAL);
    tg.drawLine(all.se().tp(), all.sw().tp(), Symbols.SINGLE_LINE_HORIZONTAL);
    tg.drawLine(all.ne().tp(), all.se().tp(), Symbols.SINGLE_LINE_VERTICAL);
    tg.drawLine(all.nw().tp(), all.sw().tp(), Symbols.SINGLE_LINE_VERTICAL);
    tg.setCharacter(all.ne().tp(), Symbols.SINGLE_LINE_TOP_LEFT_CORNER);
    tg.setCharacter(all.nw().tp(), Symbols.SINGLE_LINE_TOP_RIGHT_CORNER);
    tg.setCharacter(all.se().tp(), Symbols.SINGLE_LINE_BOTTOM_LEFT_CORNER);
    tg.setCharacter(all.sw().tp(), Symbols.SINGLE_LINE_BOTTOM_RIGHT_CORNER);
    tg.drawLine(ne.nw().delta(1, 0).tp(), se.sw().delta(1, 0).tp(), Symbols.SINGLE_LINE_VERTICAL);
    tg.setCharacter(ne.nw().delta(1, -1).tp(), Symbols.SINGLE_LINE_T_DOWN);
    tg.setCharacter(se.sw().delta(1, 1).tp(), Symbols.SINGLE_LINE_T_UP);
    tg.drawLine(ne.se().delta(0, 1).tp(), ne.sw().delta(0, 1).tp(), Symbols.SINGLE_LINE_HORIZONTAL);
    tg.setCharacter(ne.se().delta(-1, 1).tp(), Symbols.SINGLE_LINE_T_RIGHT);
    tg.setCharacter(ne.sw().delta(1, 1).tp(), Symbols.SINGLE_LINE_T_LEFT);
    tg.drawLine(nw.se().delta(0, 1).tp(), nw.sw().delta(0, 1).tp(), Symbols.SINGLE_LINE_HORIZONTAL);
    tg.setCharacter(nw.se().delta(-1, 1).tp(), Symbols.SINGLE_LINE_T_RIGHT);
    tg.setCharacter(nw.sw().delta(1, 1).tp(), Symbols.SINGLE_LINE_T_LEFT);
    //draw labels
    tg.setForegroundColor(FRAME_LABEL_COLOR);
    tg.putString(ne.ne().delta(1, -1).tp(), "[Ongoing run]", SGR.BOLD);
    tg.putString(nw.ne().delta(1, -1).tp(), "[Legend]", SGR.BOLD);
    tg.putString(se.ne().delta(1, -1).tp(), "[Log]", SGR.BOLD);
    tg.putString(sw.ne().delta(1, -1).tp(), "[Progress msgs]", SGR.BOLD);
    //draw data: logs
    int levelW = String.format(LEVEL_FORMAT, Level.WARNING).length();
    int dateW = String.format(LOG_DATE_FORMAT, Instant.now().getEpochSecond()).length();
    for (int i = 0; i < Math.min(se.h(), logRecords.size()); i = i + 1) {
      LogRecord record = logRecords.get(logRecords.size() - 1 - i);
      tg.setForegroundColor(LEVEL_COLORS.getOrDefault(record.getLevel(), DATA_COLOR));
      clipPut(se.ne().delta(0, i), se, String.format(LEVEL_FORMAT, record.getLevel()), tg);
      tg.setForegroundColor(MAIN_DATA_COLOR);
      clipPut(
          se.ne().delta(levelW + 1, i),
          se,
          String.format(LOG_DATE_FORMAT, record.getMillis()),
          tg
      );
      tg.setForegroundColor(DATA_COLOR);
      clipPut(se.ne().delta(levelW + 1 + dateW + 1, i), se, record.getMessage(), tg);
    }
    //refresh
    try {
      screen.refresh();
    } catch (IOException e) {
      L.warning(String.format("Cannot refresh screen: %s", e));
    }
  }
}
