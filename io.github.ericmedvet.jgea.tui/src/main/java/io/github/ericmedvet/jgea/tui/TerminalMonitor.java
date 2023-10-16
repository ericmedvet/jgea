
package io.github.ericmedvet.jgea.tui;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import io.github.ericmedvet.jgea.core.listener.*;
import io.github.ericmedvet.jgea.core.solver.state.State;
import io.github.ericmedvet.jgea.core.util.*;
import io.github.ericmedvet.jgea.tui.util.DrawUtils;
import io.github.ericmedvet.jgea.tui.util.Point;
import io.github.ericmedvet.jgea.tui.util.Rectangle;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;
public class TerminalMonitor<E, K> extends ListLogHandler implements ListenerFactory<E, K>, ProgressMonitor {

  public final static Map<Level, TextColor> LEVEL_COLORS = Map.ofEntries(
      Map.entry(
          Level.SEVERE,
          TextColor.Factory.fromString("#EE3E38")
      ),
      Map.entry(Level.WARNING, TextColor.Factory.fromString("#FBA465")),
      Map.entry(Level.INFO, TextColor.Factory.fromString("#D8E46B")),
      Map.entry(Level.CONFIG, TextColor.Factory.fromString("#6D8700"))
  );
  private final static Configuration DEFAULT_CONFIGURATION = new Configuration(
      0.7f,
      0.8f,
      0.5f,
      0.65f,
      16,
      250,
      true,
      true
  );
  private final static TextColor FRAME_COLOR = TextColor.Factory.fromString("#105010");
  private final static TextColor FRAME_LABEL_COLOR = TextColor.Factory.fromString("#10A010");
  private final static TextColor DATA_LABEL_COLOR = TextColor.Factory.fromString("#A01010");
  private final static TextColor MAIN_DATA_COLOR = TextColor.Factory.fromString("#F0F0F0");
  private final static TextColor DATA_COLOR = TextColor.Factory.fromString("#A0A0A0");
  private final static TextColor PLOT_BG_COLOR = TextColor.Factory.fromString("#101010");
  private final static TextColor PLOT1_COLOR = TextColor.Factory.fromString("#FF1010");
  private final static TextColor PLOT2_COLOR = TextColor.Factory.fromString("#105010");
  private final static String LEVEL_FORMAT = "%4.4s";
  private final static String DATETIME_FORMAT = "%1$tm-%1$td %1$tH:%1$tM:%1$tS";
  private final static int RUN_HISTORY_SIZE = 1000;
  private final static Logger L = Logger.getLogger(TerminalMonitor.class.getName());
  private final List<? extends NamedFunction<? super E, ?>> eFunctions;
  private final List<? extends NamedFunction<? super K, ?>> kFunctions;
  private final List<PlotTableBuilder<? super E>> plotTableBuilders;
  private final List<String> formats;
  private final Configuration configuration;
  private final ScheduledFuture<?> painterTask;
  private final Table<Object> runTable;
  private final Instant startingInstant;

  private final List<Accumulator<? super E, Table<Number>>> plotAccumulators;
  private final ScheduledExecutorService uiExecutorService;
  private Screen screen;
  private Progress overallProgress;
  private Progress partialProgress;
  private String lastProgressMessage;

  public TerminalMonitor(
      List<NamedFunction<? super E, ?>> eFunctions,
      List<NamedFunction<? super K, ?>> kFunctions,
      List<PlotTableBuilder<? super E>> plotTableBuilders
  ) {
    this(eFunctions, kFunctions, plotTableBuilders, DEFAULT_CONFIGURATION);
  }

  public TerminalMonitor(
      List<NamedFunction<? super E, ?>> eFunctions,
      List<NamedFunction<? super K, ?>> kFunctions,
      List<PlotTableBuilder<? super E>> plotTableBuilders,
      Configuration configuration
  ) {
    super(configuration.dumpLogAfterStop());
    //set functions
    this.eFunctions = configuration.robust() ? eFunctions.stream().map(NamedFunction::robust).toList() : eFunctions;
    this.kFunctions = configuration.robust() ? kFunctions.stream().map(NamedFunction::robust).toList() : kFunctions;
    this.plotTableBuilders = plotTableBuilders;
    formats = Misc.concat(List.of(
        eFunctions.stream().map(NamedFunction::getFormat).toList(),
        kFunctions.stream().map(NamedFunction::getFormat).toList()
    ));
    plotAccumulators = new ArrayList<>();
    //read configuration
    this.configuration = configuration;
    //prepare data object stores
    runTable = new ArrayTable<>(Misc.concat(List.of(
        eFunctions.stream().map(NamedFunction::getName).toList(),
        kFunctions.stream().map(NamedFunction::getName).toList()
    )));
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
    uiExecutorService = Executors.newSingleThreadScheduledExecutor();
    painterTask = uiExecutorService.scheduleAtFixedRate(
        this::repaint,
        0,
        configuration.refreshIntervalMillis,
        TimeUnit.MILLISECONDS
    );
    //set default locale
    Locale.setDefault(Locale.ENGLISH);
    startingInstant = Instant.now();
  }

  public record Configuration(
      float verticalSplit, float leftHorizontalSplit, float rightHorizontalSplit, float plotHorizontalSplit,
      int barLength, int refreshIntervalMillis, boolean dumpLogAfterStop, boolean robust
  ) {}

  @Override
  public Listener<E> build(K k) {
    List<?> kItems = kFunctions.stream().map(f -> f.apply(k)).toList();
    List<? extends Accumulator<? super E, Table<Number>>> localPlotAccumulators = plotTableBuilders.stream()
        .map(b -> b.build(null))
        .toList();
    synchronized (runTable) {
      runTable.clear();
      plotAccumulators.clear();
      plotAccumulators.addAll(localPlotAccumulators);
    }
    return e -> {
      List<?> eItems = eFunctions.stream().map(f -> f.apply(e)).toList();
      List<Object> row = Misc.concat(List.of(eItems, kItems));
      synchronized (runTable) {
        runTable.addRow(row);
        while (runTable.nRows() > RUN_HISTORY_SIZE) {
          runTable.removeRow(0);
        }
        localPlotAccumulators.forEach(a -> a.listen(e));
      }
      if (e instanceof State state) {
        partialProgress = state.getProgress();
      }
    };
  }

  @Override
  public void shutdown() {
    stop();
  }

  private double getCPULoad() {
    return ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class).getSystemLoadAverage();
  }

  private int getNumberOfProcessors() {
    return ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class).getAvailableProcessors();
  }

  @Override
  public void notify(Progress progress, String message) {
    overallProgress = progress;
    lastProgressMessage = message;
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
    List<Rectangle> plotRs;
    if (plotTableBuilders.isEmpty()) {
      plotRs = List.of();
    } else {
      Rectangle plotsR = runR.splitVertically(configuration.plotHorizontalSplit).get(1);
      runR = runR.splitVertically(configuration.plotHorizontalSplit).get(0);
      if (plotTableBuilders.size() > 1) {
        float[] splits = new float[plotTableBuilders.size() - 1];
        splits[0] = 1f / (float) plotTableBuilders.size();
        for (int i = 1; i < splits.length; i++) {
          splits[i] = splits[i - 1] + 1f / (float) plotTableBuilders.size();
        }
        plotRs = plotsR.splitHorizontally(splits);
      } else {
        plotRs = List.of(plotsR);
      }
    }
    //draw structure
    DrawUtils.drawFrame(tg, runR, "Ongoing run", FRAME_COLOR, FRAME_LABEL_COLOR);
    DrawUtils.drawFrame(tg, legendR, "Legend", FRAME_COLOR, FRAME_LABEL_COLOR);
    DrawUtils.drawFrame(tg, logR, "Log", FRAME_COLOR, FRAME_LABEL_COLOR);
    DrawUtils.drawFrame(tg, statusR, "Status", FRAME_COLOR, FRAME_LABEL_COLOR);
    for (int i = 0; i < plotTableBuilders.size(); i++) {
      String plotName = plotTableBuilders.get(i).yNames().get(0) + " vs. " + plotTableBuilders.get(i).xName();
      DrawUtils.drawFrame(tg, plotRs.get(i), plotName, FRAME_COLOR, FRAME_LABEL_COLOR);
    }
    //draw data: logs
    synchronized (getLogRecords()) {
      DrawUtils.drawLogs(tg,
          logR.inner(1),
          getLogRecords(),
          LEVEL_COLORS,
          DATA_COLOR,
          MAIN_DATA_COLOR,
          LEVEL_FORMAT,
          DATETIME_FORMAT);
    }
    //draw data: status
    r = statusR.inner(1);
    DrawUtils.clear(tg, r);
    tg.setForegroundColor(DATA_LABEL_COLOR);
    DrawUtils.clipPut(tg, r, 0, 0, "Machine:");
    DrawUtils.clipPut(tg, r, 0, 1, "Loc time:");
    DrawUtils.clipPut(tg, r, 0, 2, "CPU load:");
    DrawUtils.clipPut(tg, r, 0, 3, "Memory:");
    DrawUtils.clipPut(tg, r, 0, 4, "Over. progr.:");
    DrawUtils.clipPut(tg, r, 0, 5, "Curr. progr.:");
    DrawUtils.clipPut(tg, r, 0, 6, "ETA:");
    final int labelLength = "Over. progr.:".length();
    DrawUtils.clipPut(tg, r, 0, 7, "Last progress message:");
    tg.setForegroundColor(MAIN_DATA_COLOR);
    DrawUtils.clipPut(tg, r, 14, 0, StringUtils.getUserMachineName());
    DrawUtils.clipPut(tg, r, 14, 1, String.format(DATETIME_FORMAT, Date.from(Instant.now())));
    float maxGigaMemory = Runtime.getRuntime().maxMemory() / 1024f / 1024f / 1024f;
    float usedGigaMemory = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime()
        .freeMemory()) / 1024f / 1024f / 1024f;
    double cpuLoad = getCPULoad();
    int nOfProcessors = getNumberOfProcessors();
    DrawUtils.drawHorizontalBar(
        tg,
        r,
        labelLength + 1,
        2,
        cpuLoad,
        0,
        nOfProcessors,
        configuration.barLength,
        PLOT1_COLOR,
        PLOT_BG_COLOR
    );
    DrawUtils.clipPut(
        tg,
        r,
        labelLength + configuration.barLength + 2,
        2,
        String.format("%.2f on %d cores", cpuLoad, 2 * nOfProcessors)
    );
    DrawUtils.drawHorizontalBar(
        tg,
        r,
        labelLength + 1,
        3,
        usedGigaMemory,
        0,
        maxGigaMemory,
        configuration.barLength,
        PLOT1_COLOR,
        PLOT_BG_COLOR
    );
    DrawUtils.clipPut(tg, r, labelLength + configuration.barLength + 2, 3, String.format("%.1fGB", maxGigaMemory));
    if (overallProgress != null) {
      Progress progress = overallProgress;
      if (partialProgress != null && !Double.isNaN(partialProgress.rate())) {
        progress = new Progress(
            progress.start(),
            progress.end(),
            Math.floor(progress.current().doubleValue()) + partialProgress.rate()
        );
      }
      DrawUtils.drawHorizontalBar(
          tg,
          r,
          labelLength + 1,
          4,
          progress.rate(),
          0,
          1,
          configuration.barLength,
          PLOT1_COLOR,
          PLOT_BG_COLOR
      );
      DrawUtils.clipPut(
          tg,
          r,
          labelLength + configuration.barLength + 2,
          4,
          "%3.0f%%".formatted(progress.rate() * 100)
      );
      if (progress.rate() > 0) {
        Instant eta = startingInstant.plus(Math.round(ChronoUnit.MILLIS.between(
            startingInstant,
            Instant.now()
        ) / progress.rate()), ChronoUnit.MILLIS);
        DrawUtils.clipPut(tg, r, labelLength + 1, 6, DATETIME_FORMAT.formatted(Date.from(eta)));
      }
    }
    if (partialProgress != null) {
      DrawUtils.drawHorizontalBar(
          tg,
          r,
          labelLength + 1,
          5,
          partialProgress.rate(),
          0,
          1,
          configuration.barLength,
          PLOT1_COLOR,
          PLOT_BG_COLOR
      );
      DrawUtils.clipPut(
          tg,
          r,
          labelLength + configuration.barLength + 2,
          5,
          "%3.0f%%".formatted(partialProgress.rate() * 100)
      );
    }
    if (lastProgressMessage != null) {
      tg.setForegroundColor(DATA_COLOR);
      DrawUtils.clipPut(tg, r, 0, 8, lastProgressMessage);
    }
    //draw data: legend
    synchronized (runTable) {
      r = legendR.inner(1);
      DrawUtils.clear(tg, r);
      List<Pair<String, String>> legendItems = runTable.names().stream().map(s -> new Pair<>(
          StringUtils.collapse(s),
          s
      )).toList();
      int shortLabelW = legendItems.stream().mapToInt(p -> p.first().length()).max().orElse(0);
      for (int i = 0; i < legendItems.size(); i = i + 1) {
        tg.setForegroundColor(DATA_LABEL_COLOR);
        DrawUtils.clipPut(tg, r, 0, i, legendItems.get(i).first());
        tg.setForegroundColor(DATA_COLOR);
        DrawUtils.clipPut(tg, r, shortLabelW + 1, i, legendItems.get(i).second());
      }
      //draw data: run
      r = runR.inner(1);
      DrawUtils.clear(tg, r);
      int[] colWidths = IntStream.range(0, runTable.nColumns()).map(x -> Math.max(
          legendItems.get(x).first().length(),
          runTable.column(x).stream().mapToInt(o -> String.format(formats.get(x), o).length()).max().orElse(0)
      )).toArray();
      tg.setForegroundColor(DATA_LABEL_COLOR);
      int x = 0;
      for (int i = 0; i < colWidths.length; i = i + 1) {
        DrawUtils.clipPut(tg, r, x, 0, legendItems.get(i).first());
        x = x + colWidths[i] + 1;
      }
      tg.setForegroundColor(DATA_COLOR);
      for (int j = 0; j < Math.min(runTable.nRows(), r.h()); j = j + 1) {
        x = 0;
        int rowIndex = runTable.nRows() - j - 1;
        for (int i = 0; i < colWidths.length; i = i + 1) {
          Object value = runTable.get(i, rowIndex);
          try {
            DrawUtils.clipPut(tg, r, x, j + 1, String.format(formats.get(i), value));
          } catch (IllegalFormatConversionException ex) {
            L.warning(String.format(
                "Cannot format %s %s as a \"%s\" with %s: %s",
                value.getClass().getSimpleName(),
                value,
                formats.get(rowIndex),
                legendItems.get(i).second(),
                ex
            ));
          }
          x = x + colWidths[i] + 1;
        }
      }
    }
    //draw plots
    if (!plotAccumulators.isEmpty()) {
      for (int i = 0; i < plotAccumulators.size(); i = i + 1) {
        double minX = Double.NaN;
        double maxX = Double.NaN;
        double minY = Double.NaN;
        double maxY = Double.NaN;
        if (plotTableBuilders.get(i) instanceof XYPlotTableBuilder<?> xyPlotTableBuilder) {
          minX = Double.isFinite(xyPlotTableBuilder.getMinX()) ? xyPlotTableBuilder.getMinX() : Double.NaN;
          maxX = Double.isFinite(xyPlotTableBuilder.getMaxX()) ? xyPlotTableBuilder.getMaxX() : Double.NaN;
          minY = Double.isFinite(xyPlotTableBuilder.getMinY()) ? xyPlotTableBuilder.getMinY() : Double.NaN;
          maxY = Double.isFinite(xyPlotTableBuilder.getMaxY()) ? xyPlotTableBuilder.getMaxY() : Double.NaN;
        }
        try {
          Table<Number> table = plotAccumulators.get(i).get().filter(row -> row.stream()
              .noneMatch(n -> Double.isNaN(n.doubleValue())));
          DrawUtils.drawPlot(
              tg,
              plotRs.get(i).inner(1),
              table,
              PLOT2_COLOR,
              MAIN_DATA_COLOR,
              PLOT_BG_COLOR,
              plotTableBuilders.get(i).xFormat(),
              plotTableBuilders.get(i).yFormats().get(0),
              minX,
              maxX,
              minY,
              maxY
          );
        } catch (RuntimeException ex) {
          L.warning(String.format(
              "Cannot do plot %s vs. %s: %s",
              plotTableBuilders.get(i).yNames().get(0),
              plotTableBuilders.get(i).xName(),
              ex
          ));
        }
      }
    }
    //refresh
    try {
      screen.refresh();
    } catch (IOException ex) {
      L.warning(String.format("Cannot refresh screen: %s", ex));
    }
  }

  private void stop() {
    try {
      screen.stopScreen();
    } catch (IOException e) {
      L.warning(String.format("Cannot stop screen: %s", e));
    }
    painterTask.cancel(false);
    L.info("Closed");
    close();
    uiExecutorService.shutdownNow();
  }

}
