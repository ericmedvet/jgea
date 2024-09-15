/*-
 * ========================LICENSE_START=================================
 * jgea-experimenter
 * %%
 * Copyright (C) 2018 - 2024 Eric Medvet
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

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.*;

public class LogCapturer extends Handler {
  private static final Logger L = Logger.getLogger(LogCapturer.class.getName());
  private static final int LOG_HISTORY_SIZE = 100;

  private final boolean replaceConsoleHandlers;
  private final List<Handler> originalHandlers;
  private final List<LogRecord> logRecords;

  private final Consumer<LogRecord> consumer;

  public LogCapturer(Consumer<LogRecord> consumer, boolean replaceConsoleHandlers) {
    this.consumer = consumer;
    this.replaceConsoleHandlers = replaceConsoleHandlers;
    // prepare data object stores
    logRecords = new LinkedList<>();
    // capture logs
    Logger mainLogger = Logger.getLogger("");
    mainLogger.setLevel(Level.CONFIG);
    mainLogger.addHandler(this);
    if (replaceConsoleHandlers) {
      originalHandlers = Arrays.stream(mainLogger.getHandlers())
          .filter(h -> h instanceof ConsoleHandler)
          .toList();
      originalHandlers.forEach(mainLogger::removeHandler);
    } else {
      originalHandlers = List.of();
    }
  }

  @Override
  public synchronized void publish(LogRecord record) {
    consumer.accept(record);
    if (replaceConsoleHandlers) {
      synchronized (logRecords) {
        logRecords.add(record);
        while (logRecords.size() > LOG_HISTORY_SIZE) {
          logRecords.removeFirst();
        }
      }
    }
  }

  @Override
  public void flush() {}

  @Override
  public void close() throws SecurityException {
    Logger.getLogger("").removeHandler(this);
    if (replaceConsoleHandlers) {
      originalHandlers.forEach(h -> Logger.getLogger("").addHandler(h));
      logRecords.forEach(L::log);
    }
  }
}
