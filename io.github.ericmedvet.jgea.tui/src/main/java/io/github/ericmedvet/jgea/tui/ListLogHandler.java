package io.github.ericmedvet.jgea.tui;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.*;
public class ListLogHandler extends Handler {
  private final static int LOG_HISTORY_SIZE = 100;
  private final static Logger L = Logger.getLogger(ListLogHandler.class.getName());

  private final boolean dumpLogAfterStop;
  private final List<Handler> originalHandlers;
  private final List<LogRecord> logRecords;

  public ListLogHandler(boolean dumpLogAfterStop) {
    this.dumpLogAfterStop = dumpLogAfterStop;
    //prepare data object stores
    logRecords = new LinkedList<>();
    //capture logs
    Logger mainLogger = Logger.getLogger("");
    mainLogger.setLevel(Level.CONFIG);
    mainLogger.addHandler(this);
    originalHandlers = Arrays.stream(mainLogger.getHandlers()).filter(h -> h instanceof ConsoleHandler).toList();
    originalHandlers.forEach(mainLogger::removeHandler);
  }

  protected List<LogRecord> getLogRecords() {
    return logRecords;
  }

  @Override
  public synchronized void publish(LogRecord record) {
    synchronized (logRecords) {
      logRecords.add(record);
      while (logRecords.size() > LOG_HISTORY_SIZE) {
        logRecords.remove(0);
      }
    }
  }

  @Override
  public void flush() {
  }

  @Override
  public void close() throws SecurityException {
    Logger.getLogger("").removeHandler(this);
    originalHandlers.forEach(h -> Logger.getLogger("").addHandler(h));
    if (dumpLogAfterStop) {
      logRecords.forEach(L::log);
    }
  }
}
