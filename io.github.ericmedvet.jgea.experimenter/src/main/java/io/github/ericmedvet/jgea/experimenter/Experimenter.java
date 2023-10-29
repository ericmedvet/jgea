/*-
 * ========================LICENSE_START=================================
 * jgea-experimenter
 * %%
 * Copyright (C) 2018 - 2023 Eric Medvet
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

import io.github.ericmedvet.jgea.core.listener.ListenerFactory;
import io.github.ericmedvet.jgea.core.listener.ProgressMonitor;
import io.github.ericmedvet.jgea.core.listener.ScreenProgressMonitor;
import io.github.ericmedvet.jgea.core.solver.POCPopulationState;
import io.github.ericmedvet.jnb.core.MapNamedParamMap;
import io.github.ericmedvet.jnb.core.NamedBuilder;
import io.github.ericmedvet.jnb.core.ProjectInfoProvider;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class Experimenter {

  private static final Logger L = Logger.getLogger(Experimenter.class.getName());

  private final NamedBuilder<?> namedBuilder;
  private final ExecutorService experimentExecutorService;
  private final ExecutorService runExecutorService;
  private final ExecutorService listenerExecutorService;
  private final boolean closeListeners;

  private Experimenter(
      NamedBuilder<?> namedBuilder,
      ExecutorService experimentExecutorService,
      ExecutorService runExecutorService,
      ExecutorService listenerExecutorService,
      boolean closeListeners) {
    this.namedBuilder = namedBuilder;
    this.experimentExecutorService = experimentExecutorService;
    this.runExecutorService = runExecutorService;
    this.listenerExecutorService = listenerExecutorService;
    this.closeListeners = closeListeners;
  }

  @SuppressWarnings("unused")
  public Experimenter(
      NamedBuilder<?> namedBuilder,
      ExecutorService experimentExecutorService,
      ExecutorService runExecutorService,
      ExecutorService listenerExecutorService) {
    this(namedBuilder, experimentExecutorService, runExecutorService, listenerExecutorService, false);
  }

  @SuppressWarnings("unused")
  public Experimenter(NamedBuilder<?> namedBuilder, int nOfConcurrentRuns, int nOfThreads) {
    this(
        namedBuilder,
        Executors.newFixedThreadPool(nOfConcurrentRuns),
        Executors.newFixedThreadPool(nOfThreads),
        Executors.newFixedThreadPool(nOfConcurrentRuns),
        true);
  }

  @SuppressWarnings("unused")
  public void run(File experimentFile, boolean verbose) {
    String experimentDescription;
    L.config(String.format("Using provided experiment description: %s", experimentFile));
    try (BufferedReader br = new BufferedReader(new FileReader(experimentFile))) {
      experimentDescription = br.lines().collect(Collectors.joining());
    } catch (IOException e) {
      throw new IllegalArgumentException(
          String.format("Cannot read provided experiment description at %s: %s", experimentFile, e));
    }
    run(experimentDescription, verbose);
  }

  public void run(String experimentDescription, boolean verbose) {
    run((Experiment) namedBuilder.build(experimentDescription), verbose);
  }

  public void run(Experiment experiment, boolean verbose) {
    ProjectInfoProvider.of(getClass()).ifPresent(pi -> L.info("Starting %s".formatted(pi)));
    // preapare factories
    List<? extends ListenerFactory<? super POCPopulationState<?, ?, ?, ?>, Run<?, ?, ?, ?>>> factories =
        experiment.listeners().stream()
            .map(l -> l.apply(experiment, listenerExecutorService))
            .toList();
    ListenerFactory<? super POCPopulationState<?, ?, ?, ?>, Run<?, ?, ?, ?>> factory =
        ListenerFactory.all(factories);
    List<ProgressMonitor> progressMonitors = factories.stream()
        .filter(f -> f instanceof ProgressMonitor)
        .map(f -> (ProgressMonitor) f)
        .toList();
    ProgressMonitor progressMonitor = progressMonitors.isEmpty()
        ? new ScreenProgressMonitor(System.out)
        : ProgressMonitor.all(progressMonitors);
    // start experiments
    record RunOutcome(Run<?, ?, ?, ?> run, Future<Collection<?>> future) {}
    List<RunOutcome> runOutcomes = experiment.runs().stream()
        .map(run -> new RunOutcome(run, experimentExecutorService.submit(() -> {
          progressMonitor.notify(
              run.index(),
              experiment.runs().size(),
              "Starting:%n%s".formatted(MapNamedParamMap.prettyToString(run.map(), 40)));
          Instant startingT = Instant.now();
          Collection<?> solutions = run.run(runExecutorService, factory.build(run));
          double elapsedT = Duration.between(startingT, Instant.now()).toMillis() / 1000d;
          String msg = String.format(
              "Run %d of %d done in %.2fs, found %d solutions",
              run.index() + 1, experiment.runs().size(), elapsedT, solutions.size());
          L.info(msg);
          progressMonitor.notify(run.index() + 1, experiment.runs().size(), msg);
          return solutions;
        })))
        .toList();
    // wait for results
    runOutcomes.forEach(runOutcome -> {
      try {
        runOutcome.future().get();
      } catch (InterruptedException | ExecutionException e) {
        L.warning(String.format("Cannot solve %s: %s", runOutcome.run().map(), e));
        if (verbose) {
          //noinspection CallToPrintStackTrace
          e.printStackTrace();
        }
      }
    });
    if (closeListeners) {
      L.info("Closing");
      experimentExecutorService.shutdown();
      runExecutorService.shutdown();
      listenerExecutorService.shutdown();
      while (true) {
        try {
          if (listenerExecutorService.awaitTermination(1, TimeUnit.SECONDS)) {
            break;
          }
        } catch (InterruptedException e) {
          // ignore
        }
      }
    }
    factory.shutdown();
  }
}
