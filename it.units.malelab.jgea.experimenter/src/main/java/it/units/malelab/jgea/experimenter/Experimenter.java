package it.units.malelab.jgea.experimenter;

import it.units.malelab.jgea.core.listener.ListenerFactory;
import it.units.malelab.jgea.core.listener.ProgressMonitor;
import it.units.malelab.jgea.core.listener.ScreenProgressMonitor;
import it.units.malelab.jgea.core.solver.SolverException;
import it.units.malelab.jgea.core.solver.state.POSetPopulationState;
import it.units.malelab.jnb.core.NamedBuilder;
import it.units.malelab.jnb.core.StringNamedParamMap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author "Eric Medvet" on 2022/11/24 for jgea
 */
public class Experimenter {

  private final static Logger L = Logger.getLogger(Experimenter.class.getName());

  private final NamedBuilder<?> namedBuilder;
  private final ExecutorService runExecutorService;
  private final ExecutorService listenerExecutorService;
  private final boolean closeListeners;

  private Experimenter(
      NamedBuilder<?> namedBuilder,
      ExecutorService runExecutorService,
      ExecutorService listenerExecutorService,
      boolean closeListeners
  ) {
    this.namedBuilder = namedBuilder;
    this.runExecutorService = runExecutorService;
    this.listenerExecutorService = listenerExecutorService;
    this.closeListeners = closeListeners;
  }

  public Experimenter(
      NamedBuilder<?> namedBuilder, ExecutorService runExecutorService, ExecutorService listenerExecutorService
  ) {
    this(namedBuilder, runExecutorService, listenerExecutorService, false);
  }


  public Experimenter(NamedBuilder<?> namedBuilder, int nOfThreads) {
    this(namedBuilder, Executors.newFixedThreadPool(nOfThreads), Executors.newSingleThreadExecutor(), true);
  }

  public void run(File experimentFile) {
    String experimentDescription;
    L.config(String.format("Using provided experiment description: %s", experimentFile));
    try (BufferedReader br = new BufferedReader(new FileReader(experimentFile))) {
      experimentDescription = br.lines().collect(Collectors.joining());
    } catch (IOException e) {
      throw new IllegalArgumentException(String.format(
          "Cannot read provided experiment description at %s: %s",
          experimentFile,
          e
      ));
    }
    run(experimentDescription);
  }

  public void run(String experimentDescription) {
    run((Experiment) namedBuilder.build(experimentDescription));
  }

  public void run(Experiment experiment) {
    //preapare factories
    List<? extends ListenerFactory<? super POSetPopulationState<?, ?, ?>, Run<?, ?, ?, ?>>> factories =
        experiment.listeners()
            .stream()
            .map(l -> l.apply(experiment, listenerExecutorService))
            .toList();
    ListenerFactory<? super POSetPopulationState<?, ?, ?>, Run<?, ?, ?, ?>> factory = ListenerFactory.all(factories);
    List<ProgressMonitor> progressMonitors = factories.stream()
        .filter(f -> f instanceof ProgressMonitor)
        .map(f -> (ProgressMonitor) f)
        .toList();
    ProgressMonitor progressMonitor = progressMonitors.isEmpty() ? new ScreenProgressMonitor(System.out) :
        ProgressMonitor.all(progressMonitors);
    //iterate over runs
    for (int i = 0; i < experiment.runs().size(); i++) {
      Run<?, ?, ?, ?> run = experiment.runs().get(i);
      //do optimization
      try {
        progressMonitor.notify(
            i,
            experiment.runs().size(),
            "Starting:%n%s".formatted(StringNamedParamMap.prettyToString(run.map(), 40))
        );
        Instant startingT = Instant.now();
        Collection<?> solutions = run.run(runExecutorService, factory.build(run));
        double elapsedT = Duration.between(startingT, Instant.now()).toMillis() / 1000d;
        String msg = String.format(
            "Run %d of %d done in %.2fs, found %d solutions",
            i + 1,
            experiment.runs().size(),
            elapsedT,
            solutions.size()
        );
        L.info(msg);
        progressMonitor.notify(i + 1, experiment.runs().size(), msg);
      } catch (SolverException | RuntimeException e) {
        L.warning(String.format("Cannot solve %s: %s", run.map(), e));
        break;
      }
    }
    if (closeListeners) {
      L.info("Closing");
      runExecutorService.shutdown();
      listenerExecutorService.shutdown();
      while (true) {
        try {
          if (listenerExecutorService.awaitTermination(1, TimeUnit.SECONDS)) {
            break;
          }
        } catch (InterruptedException e) {
          //ignore
        }
      }
      factory.shutdown();
    }
  }


}
