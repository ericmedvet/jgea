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

package io.github.ericmedvet.jgea.experimenter.builders;

import io.github.ericmedvet.jgea.core.listener.*;
import io.github.ericmedvet.jgea.core.solver.Individual;
import io.github.ericmedvet.jgea.core.solver.state.POSetPopulationState;
import io.github.ericmedvet.jgea.core.util.Misc;
import io.github.ericmedvet.jgea.core.util.Progress;
import io.github.ericmedvet.jgea.experimenter.Experiment;
import io.github.ericmedvet.jgea.experimenter.Run;
import io.github.ericmedvet.jgea.experimenter.net.NetListenerClient;
import io.github.ericmedvet.jgea.telegram.TelegramUpdater;
import io.github.ericmedvet.jgea.tui.TerminalMonitor;
import io.github.ericmedvet.jnb.core.NamedParamMap;
import io.github.ericmedvet.jnb.core.Param;
import io.github.ericmedvet.jnb.core.ParamMap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.function.BiFunction;
import java.util.logging.Logger;

public class Listeners {

  private final static Logger L = Logger.getLogger(Listeners.class.getName());

  private Listeners() {
  }

  private static class ListenerFactoryAndMonitor<E, K> implements ListenerFactory<E, K>, ProgressMonitor {
    private final ListenerFactory<E, K> innerListenerFactory;
    private final ListenerFactory<E, K> outerListenerFactory;

    public ListenerFactoryAndMonitor(
        ListenerFactory<E, K> innerListenerFactory, ExecutorService executorService, boolean onLast
    ) {
      this.innerListenerFactory = innerListenerFactory;
      if (onLast) {
        if (executorService != null) {
          outerListenerFactory = innerListenerFactory.onLast().deferred(executorService);
        } else {
          outerListenerFactory = innerListenerFactory.onLast();
        }
      } else {
        if (executorService != null) {
          outerListenerFactory = innerListenerFactory.deferred(executorService);
        } else {
          outerListenerFactory = innerListenerFactory;
        }
      }
    }

    @Override
    public Listener<E> build(K k) {
      return outerListenerFactory.build(k);
    }

    @Override
    public void shutdown() {
      innerListenerFactory.shutdown();
    }

    @Override
    public void notify(Progress progress, String message) {
      if (innerListenerFactory instanceof ProgressMonitor progressMonitor) {
        progressMonitor.notify(progress, message);
      }
    }
  }

  @SuppressWarnings("unused")
  public static <G, S, Q> BiFunction<Experiment, ExecutorService, ListenerFactory<? super POSetPopulationState<G, S,
      Q>, Run<?, G, S, Q>>> allCsv(
      @Param("filePath") String filePath,
      @Param(
          value = "defaultFunctions",
          dNPMs = {"ea.nf.iterations()"}
      ) List<NamedFunction<? super POSetPopulationState<G, S, Q>, ?>> defaultStateFunctions,
      @Param(value = "functions") List<NamedFunction<? super POSetPopulationState<G, S, Q>, ?>> stateFunctions,
      @Param("individualFunctions") List<NamedFunction<? super Individual<G, S, Q>, ?>> individualFunctions,
      @Param("runKeys") List<String> runKeys,
      @Param(value = "deferred") boolean deferred,
      @Param(value = "onlyLast") boolean onlyLast
  ) {
    record PopIndividualPair<G, S, Q>(POSetPopulationState<G, S, Q> pop, Individual<G, S, Q> individual) {}
    List<NamedFunction<? super POSetPopulationState<G, S, Q>, ?>> popFunctions = Misc.concat(List.of(
        defaultStateFunctions,
        stateFunctions
    ));
    List<NamedFunction<? super PopIndividualPair<G, S, Q>, ?>> pairFunctions = new ArrayList<>();
    popFunctions.stream()
        .map(f -> NamedFunction.build(
            f.getName(),
            f.getFormat(),
            (PopIndividualPair<G, S, Q> pair) -> f.apply(pair.pop())
        ))
        .forEach(pairFunctions::add);
    individualFunctions.stream().map(f -> NamedFunction.build(
        f.getName(),
        f.getFormat(),
        (PopIndividualPair<G, S, Q> pair) -> f.apply(pair.individual())
    )).forEach(pairFunctions::add);
    CSVPrinter<PopIndividualPair<G, S, Q>, Run<?, G, S, Q>> innerListenerFactory = new CSVPrinter<>(
        pairFunctions,
        buildRunNamedFunctions(runKeys),
        new File(filePath),
        true
    );
    ListenerFactory<? super POSetPopulationState<G, S, Q>, Run<?, G, S, Q>> allListenerFactory =
        new ListenerFactory<>() {
          @Override
          public Listener<POSetPopulationState<G, S, Q>> build(Run<?, G, S, Q> run) {
            Listener<PopIndividualPair<G, S, Q>> innerListener = innerListenerFactory.build(run);
            return new Listener<>() {
              @Override
              public void listen(POSetPopulationState<G, S, Q> state) {
                for (Individual<G, S, Q> individual : state.getPopulation().all()) {
                  innerListener.listen(new PopIndividualPair<>(state, individual));
                }
              }

              @Override
              public void done() {
                innerListener.done();
              }
            };
          }

          @Override
          public void shutdown() {
            innerListenerFactory.shutdown();
          }
        };
    return (experiment, executorService) -> new ListenerFactoryAndMonitor<>(
        allListenerFactory,
        deferred ? executorService : null,
        onlyLast
    );
  }

  @SuppressWarnings("unused")
  public static <G, S, Q> BiFunction<Experiment, ExecutorService, ListenerFactory<? super POSetPopulationState<G, S,
      Q>, Run<?, G, S, Q>>> bestCsv(
      @Param("filePath") String filePath,
      @Param(value = "defaultFunctions", dNPMs = {
          "ea.nf.iterations()",
          "ea.nf.evals()",
          "ea.nf.births()",
          "ea.nf.elapsed()",
          "ea.nf.size(f=ea.nf.all())",
          "ea.nf.size(f=ea.nf.firsts())",
          "ea.nf.size(f=ea.nf.lasts())",
          "ea.nf.uniqueness(collection=ea.nf.each(map=ea.nf.genotype();collection=ea.nf.all()))",
          "ea.nf.uniqueness(collection=ea.nf.each(map=ea.nf.solution();collection=ea.nf.all()))",
          "ea.nf.uniqueness(collection=ea.nf.each(map=ea.nf.fitness();collection=ea.nf.all()))"
      }) List<NamedFunction<? super POSetPopulationState<G, S, Q>, ?>> defaultStateFunctions,
      @Param(value = "functions") List<NamedFunction<? super POSetPopulationState<G, S, Q>, ?>> stateFunctions,
      @Param("runKeys") List<String> runKeys,
      @Param(value = "deferred") boolean deferred,
      @Param(value = "onlyLast") boolean onlyLast
  ) {
    return (experiment, executorService) -> new ListenerFactoryAndMonitor<>(
        new CSVPrinter<>(
            (List<NamedFunction<? super POSetPopulationState<G, S, Q>, ?>>) Misc.concat(List.of(
                defaultStateFunctions,
                stateFunctions
            )),
            buildRunNamedFunctions(runKeys),
            new File(filePath),
            true
        ),
        deferred ? executorService : null,
        onlyLast
    );
  }

  private static <G, S, Q> List<NamedFunction<? super Run<?, G, S, Q>, ?>> buildRunNamedFunctions(List<String> runKeys) {
    List<NamedFunction<? super Run<?, G, S, Q>, ?>> functions = new ArrayList<>();
    runKeys.stream()
        .map(k -> NamedFunction.build(
            k,
            "%s",
            (Run<?, G, S, Q> run) -> getKeyFromParamMap(run.map(), Arrays.stream(k.split("\\.")).toList())
        ))
        .forEach(functions::add);
    return Collections.unmodifiableList(functions);
  }

  @SuppressWarnings("unused")
  public static <G, S, Q> BiFunction<Experiment, ExecutorService, ListenerFactory<POSetPopulationState<G, S, Q>, Run<
      ?, G, S, Q>>> console(
      @Param(value = "defaultFunctions", dNPMs = {
          "ea.nf.iterations()",
          "ea.nf.evals()",
          "ea.nf.births()",
          "ea.nf.elapsed()",
          "ea.nf.size(f=ea.nf.all())",
          "ea.nf.size(f=ea.nf.firsts())",
          "ea.nf.size(f=ea.nf.lasts())",
          "ea.nf.uniqueness(collection=ea.nf.each(map=ea.nf.genotype();collection=ea.nf.all()))",
          "ea.nf.uniqueness(collection=ea.nf.each(map=ea.nf.solution();collection=ea.nf.all()))",
          "ea.nf.uniqueness(collection=ea.nf.each(map=ea.nf.fitness();collection=ea.nf.all()))"
      }) List<NamedFunction<? super POSetPopulationState<G, S, Q>, ?>> defaultStateFunctions,
      @Param(value = "functions") List<NamedFunction<? super POSetPopulationState<G, S, Q>, ?>> stateFunctions,
      @Param("runKeys") List<String> runKeys,
      @Param(value = "deferred") boolean deferred,
      @Param(value = "onlyLast") boolean onlyLast
  ) {
    return (experiment, executorService) -> new ListenerFactoryAndMonitor<>(
        new TabularPrinter<>(
            (List<NamedFunction<? super POSetPopulationState<G, S, Q>, ?>>) Misc.concat(List.of(
                defaultStateFunctions,
                stateFunctions
            )),
            buildRunNamedFunctions(runKeys)
        ),
        deferred ? executorService : null,
        onlyLast
    );
  }

  private static Object getKeyFromParamMap(ParamMap paramMap, List<String> keyPieces) {
    if (keyPieces.size() == 1) {
      return paramMap.value(keyPieces.get(0));
    }
    NamedParamMap namedParamMap = paramMap.npm(keyPieces.get(0));
    if (namedParamMap == null) {
      return null;
    }
    return getKeyFromParamMap(namedParamMap, keyPieces.subList(1, keyPieces.size()));
  }

  public static <G, S, Q> BiFunction<Experiment, ExecutorService, ListenerFactory<POSetPopulationState<G, S, Q>, Run<
      ?, G, S, Q>>> net(
      @Param(value = "defaultFunctions", dNPMs = {
          "ea.nf.iterations()",
          "ea.nf.evals()",
          "ea.nf.births()",
          "ea.nf.elapsed()",
          "ea.nf.size(f=ea.nf.all())",
          "ea.nf.size(f=ea.nf.firsts())",
          "ea.nf.size(f=ea.nf.lasts())",
          "ea.nf.uniqueness(collection=ea.nf.each(map=ea.nf.genotype();collection=ea.nf.all()))",
          "ea.nf.uniqueness(collection=ea.nf.each(map=ea.nf.solution();collection=ea.nf.all()))",
          "ea.nf.uniqueness(collection=ea.nf.each(map=ea.nf.fitness();collection=ea.nf.all()))"
      }) List<NamedFunction<? super POSetPopulationState<G, S, Q>, ?>> defaultStateFunctions,
      @Param(value = "functions") List<NamedFunction<? super POSetPopulationState<G, S, Q>, ?>> stateFunctions,
      @Param(value = "defaultPlots", dNPMs = {
          "ea.plot.elapsed()"
      }) List<PlotTableBuilder<? super POSetPopulationState<G, S, Q>>> defaultPlotTableBuilders,
      @Param("plots") List<PlotTableBuilder<? super POSetPopulationState<G, S, Q>>> plotTableBuilders,
      @Param(value = "deferred") boolean deferred,
      @Param(value = "onlyLast") boolean onlyLast,
      @Param(value = "serverAddress", dS = "127.0.0.1") String serverAddress,
      @Param(value = "serverPort", dI = 10979) int serverPort,
      @Param(value = "pollInterval", dD = 2) double pollInterval
  ) {
    return (experiment, executorService) -> new ListenerFactoryAndMonitor<>(
        new NetListenerClient<>(
            serverAddress,
            serverPort,
            pollInterval,
            Misc.concat(List.of(
                defaultStateFunctions,
                stateFunctions
            )),
            Misc.concat(List.of(
                defaultPlotTableBuilders,
                plotTableBuilders
            ))
        ),
        deferred ? executorService : null,
        onlyLast
    );
  }

  @SuppressWarnings("unused")
  public static <G, S, Q> BiFunction<Experiment, ExecutorService, ListenerFactory<POSetPopulationState<G, S, Q>, Run<
      ?, G, S, Q>>> telegram(
      @Param("chatId") String chatId,
      @Param("botIdFilePath") String botIdFilePath,
      @Param(value = "defaultPlots", dNPMs = {
          "ea.plot.elapsed()"
      }) List<PlotTableBuilder<? super POSetPopulationState<G, S, Q>>> defaultPlotTableBuilders,
      @Param("plots") List<PlotTableBuilder<? super POSetPopulationState<G, S, Q>>> plotTableBuilders,
      @Param("accumulators") List<AccumulatorFactory<? super POSetPopulationState<G, S, Q>, ?, Run<?, G, S, Q>>> accumulators,
      @Param(value = "deferred", dB = true) boolean deferred,
      @Param(value = "onlyLast") boolean onlyLast
  ) {
    //read credential files
    String botId;
    long longChatId;
    try (BufferedReader br = new BufferedReader(new FileReader(botIdFilePath))) {
      List<String> lines = br.lines().toList();
      if (lines.size() < 1) {
        throw new IllegalArgumentException("Invalid telegram credential file with 0 lines");
      }
      String[] pieces = lines.get(0).split("\\s");
      botId = pieces[0];
      L.config(String.format("Using provided telegram credentials: %s", botIdFilePath));
    } catch (IOException e) {
      throw new IllegalArgumentException(String.format("Cannot read telegram credentials at %s: %s", botIdFilePath, e));
    }
    try {
      longChatId = Long.parseLong(chatId);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Invalid chatId %s: not a number".formatted(chatId));
    }
    @SuppressWarnings({"unchecked", "rawtypes"})
    List<AccumulatorFactory<POSetPopulationState<G, S, Q>, ?, Run<?, G, S, Q>>> accumulatorFactories =
        (List) Misc.concat(
            List.of(
                defaultPlotTableBuilders,
                plotTableBuilders,
                accumulators
            ));
    return (experiment, executorService) -> new ListenerFactoryAndMonitor<>(
        new TelegramUpdater<>(
            accumulatorFactories,
            botId,
            longChatId
        ),
        deferred ? executorService : null,
        onlyLast
    );
  }

  @SuppressWarnings("unused")
  public static <G, S, Q> BiFunction<Experiment, ExecutorService, ListenerFactory<POSetPopulationState<G, S, Q>, Run<
      ?, G, S, Q>>> tui(
      @Param(value = "defaultFunctions", dNPMs = {
          "ea.nf.iterations()",
          "ea.nf.evals()",
          "ea.nf.births()",
          "ea.nf.elapsed()",
          "ea.nf.size(f=ea.nf.all())",
          "ea.nf.size(f=ea.nf.firsts())",
          "ea.nf.size(f=ea.nf.lasts())",
          "ea.nf.uniqueness(collection=ea.nf.each(map=ea.nf.genotype();collection=ea.nf.all()))",
          "ea.nf.uniqueness(collection=ea.nf.each(map=ea.nf.solution();collection=ea.nf.all()))",
          "ea.nf.uniqueness(collection=ea.nf.each(map=ea.nf.fitness();collection=ea.nf.all()))"
      }) List<NamedFunction<? super POSetPopulationState<G, S, Q>, ?>> defaultStateFunctions,
      @Param(value = "functions") List<NamedFunction<? super POSetPopulationState<G, S, Q>, ?>> stateFunctions,
      @Param("runKeys") List<String> runKeys,
      @Param(value = "defaultPlots", dNPMs = {
          "ea.plot.elapsed()"
      }) List<PlotTableBuilder<? super POSetPopulationState<G, S, Q>>> defaultPlotTableBuilders,
      @Param("plots") List<PlotTableBuilder<? super POSetPopulationState<G, S, Q>>> plotTableBuilders,
      @Param(value = "deferred") boolean deferred,
      @Param(value = "onlyLast") boolean onlyLast
  ) {
    return (experiment, executorService) -> new ListenerFactoryAndMonitor<>(
        new TerminalMonitor<>(
            (List<NamedFunction<? super POSetPopulationState<G, S, Q>, ?>>) Misc.concat(List.of(
                defaultStateFunctions,
                stateFunctions
            )),
            buildRunNamedFunctions(runKeys),
            Misc.concat(List.of(
                defaultPlotTableBuilders,
                plotTableBuilders
            ))
        ),
        deferred ? executorService : null,
        onlyLast
    );
  }

}
