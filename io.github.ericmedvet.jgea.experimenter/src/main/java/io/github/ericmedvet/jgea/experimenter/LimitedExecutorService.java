/*-
 * ========================LICENSE_START=================================
 * jgea-experimenter
 * %%
 * Copyright (C) 2018 - 2026 Eric Medvet
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
package io.github.ericmedvet.jgea.experimenter;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LimitedExecutorService extends AbstractExecutorService {

  private final int maxNOfConcurrentTasks;
  private final Semaphore semaphore;
  private final BlockingQueue<Runnable> tasks;
  private boolean isConsuming;
  private boolean isAccepting;
  private final CountDownLatch terminationSignal;
  private final Thread consumerThread;
  private long nOfCompleted;
  private long nOfFailed;
  private final boolean logEvents;

  public enum Event {
    TASK_SUBMITTED, TASK_STARTED, TASK_ENDED, TASK_REJECTED, TASK_FAILED, STARTED, SHUTDOWN, TERMINATED
  }

  private final Map<LocalDateTime, Event> events;

  public LimitedExecutorService(Executor innerExecutor, int maxNOfConcurrentTasks, boolean logEvents) {
    this.maxNOfConcurrentTasks = maxNOfConcurrentTasks;
    this.logEvents = logEvents;
    semaphore = new Semaphore(maxNOfConcurrentTasks);
    tasks = new LinkedBlockingQueue<>();
    isAccepting = true;
    isConsuming = true;
    terminationSignal = new CountDownLatch(1);
    events = new HashMap<>();
    events.put(LocalDateTime.now(), Event.STARTED);
    nOfCompleted = 0;
    consumerThread = new Thread(() -> {
      while (isConsuming) {
        if (!isAccepting && tasks.isEmpty()) {
          break;
        }
        semaphore.acquireUninterruptibly();
        try {
          Runnable task = tasks.take();
          innerExecutor.execute(() -> {
            try {
              if (logEvents) {
                events.put(LocalDateTime.now(), Event.TASK_STARTED);
              }
              task.run();
            } catch (Throwable t) {
              nOfFailed = nOfFailed + 1;
              if (logEvents) {
                events.put(LocalDateTime.now(), Event.TASK_FAILED);
              }
            } finally {
              semaphore.release();
              nOfCompleted = nOfCompleted + 1;
              if (logEvents) {
                events.put(LocalDateTime.now(), Event.TASK_ENDED);
              }
            }
          });
        } catch (RejectedExecutionException e) {
          // inner does not accept new tasks
          semaphore.release();
        } catch (InterruptedException e) {
          // take interrupted
          semaphore.release();
        }
      }
      events.put(LocalDateTime.now(), Event.TERMINATED);
      terminationSignal.countDown();
    });
    consumerThread.start();
  }

  @Override
  public void execute(Runnable command) {
    if (isShutdown()) {
      if (logEvents) {
        events.put(LocalDateTime.now(), Event.TASK_REJECTED);
      }
      throw new RejectedExecutionException("shutdown() has already been invoked");
    }
    if (logEvents) {
      events.put(LocalDateTime.now(), Event.TASK_SUBMITTED);
    }
    tasks.add(command);
  }

  @Override
  public String toString() {
    return "SFE(m=%d;w=%d;e=%d)".formatted(
        maxNOfConcurrentTasks,
        tasks.size(),
        maxNOfConcurrentTasks - semaphore.availablePermits()
    );
  }

  @Override
  public void shutdown() {
    isAccepting = false;
    consumerThread.interrupt();
    events.put(LocalDateTime.now(), Event.SHUTDOWN);
  }

  @Override
  public List<Runnable> shutdownNow() {
    // might include some tasks which are about to be executed
    isConsuming = false;
    consumerThread.interrupt();
    return tasks.stream().toList();
  }

  @Override
  public boolean isShutdown() {
    return !isAccepting;
  }

  @Override
  public boolean isTerminated() {
    return tasks.isEmpty() && semaphore.availablePermits() == maxNOfConcurrentTasks;
  }

  @Override
  public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
    if (isTerminated()) {
      return true;
    }
    consumerThread.interrupt();
    return terminationSignal.await(timeout, unit);
  }

  public SortedMap<LocalDateTime, Event> getEvents() {
    return Collections.unmodifiableSortedMap(new TreeMap<>(events));
  }

  public List<Long> waitingHistory(long interval, TimeUnit timeUnit) {
    return history(
        interval,
        timeUnit,
        counts -> counts.getOrDefault(Event.TASK_SUBMITTED, 0L) - counts.getOrDefault(
            Event.TASK_STARTED,
            0L
        )
    );
  }

  public List<Long> runningHistory(long interval, TimeUnit timeUnit) {
    return history(
        interval,
        timeUnit,
        counts -> counts.getOrDefault(Event.TASK_STARTED, 0L) - counts.getOrDefault(
            Event.TASK_ENDED,
            0L
        )
    );
  }

  private List<Long> history(
      long interval,
      TimeUnit timeUnit,
      ToLongFunction<Map<Event, Long>> computer
  ) {
    SortedMap<LocalDateTime, Event> sortedEvents = getEvents();
    ToLongFunction<LocalDateTime> countOf = t -> {
      Map<Event, Long> counts = sortedEvents.headMap(t)
          .values()
          .stream()
          .collect(
              Collectors.groupingBy(
                  java.util.function.Function.identity(),
                  Collectors.counting()
              )
          );
      return computer.applyAsLong(counts);
    };
    return Stream.iterate(
        sortedEvents.firstKey(),
        t -> t.isBefore(sortedEvents.lastKey()),
        t -> t.plus(interval, timeUnit.toChronoUnit())
    )
        .map(countOf::applyAsLong)
        .toList();
  }

  public long nOfWaiting() {
    return tasks.size();
  }

  public long nOfRunning() {
    return maxNOfConcurrentTasks - semaphore.availablePermits();
  }

  public long nOfCompleted() {
    return nOfCompleted;
  }

  public long nOfFailed() {
    return nOfFailed;
  }

  public long nOfSubmitted() {
    return nOfWaiting() + nOfRunning() + nOfCompleted() + nOfFailed();
  }

}