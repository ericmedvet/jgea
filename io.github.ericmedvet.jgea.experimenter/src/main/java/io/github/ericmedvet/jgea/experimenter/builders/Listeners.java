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

package io.github.ericmedvet.jgea.experimenter.builders;

import io.github.ericmedvet.jgea.core.listener.*;
import io.github.ericmedvet.jgea.core.solver.Individual;
import io.github.ericmedvet.jgea.core.solver.POCPopulationState;
import io.github.ericmedvet.jgea.core.solver.cabea.GridPopulationState;
import io.github.ericmedvet.jgea.core.util.Misc;
import io.github.ericmedvet.jgea.core.util.Progress;
import io.github.ericmedvet.jgea.experimenter.Experiment;
import io.github.ericmedvet.jgea.experimenter.Run;
import io.github.ericmedvet.jgea.experimenter.Utils;
import io.github.ericmedvet.jgea.experimenter.listener.HeatMapVideoAccumulatorFactory;
import io.github.ericmedvet.jgea.experimenter.listener.decoupled.*;
import io.github.ericmedvet.jgea.experimenter.listener.net.NetMultiSink;
import io.github.ericmedvet.jgea.experimenter.listener.telegram.TelegramUpdater;
import io.github.ericmedvet.jgea.experimenter.util.ImagePlotters;
import io.github.ericmedvet.jgea.experimenter.util.PlotTableBuilder;
import io.github.ericmedvet.jnb.core.*;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.function.BiFunction;
import java.util.logging.Logger;

@Discoverable(prefixTemplate = "ea.listener|l")
public class Listeners {

  private static final Logger L = Logger.getLogger(Listeners.class.getName());

  private Listeners() {}

  private static class ListenerFactoryAndMonitor<E, K> implements ListenerFactory<E, K>, ProgressMonitor {
    private final ListenerFactory<E, K> innerListenerFactory;
    private final ListenerFactory<E, K> outerListenerFactory;

    public ListenerFactoryAndMonitor(
        ListenerFactory<E, K> innerListenerFactory, ExecutorService executorService, boolean onLast) {
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
  public static <G, S, Q>
      BiFunction<
              Experiment,
              ExecutorService,
              ListenerFactory<? super POCPopulationState<?, G, S, Q>, Run<?, G, S, Q>>>
          allCsv(
              @Param("filePath") String filePath,
              @Param(
                      value = "defaultFunctions",
                      dNPMs = {"ea.nf.iterations()"})
                  List<NamedFunction<? super POCPopulationState<?, G, S, Q>, ?>>
                      defaultStateFunctions,
              @Param(value = "functions")
                  List<NamedFunction<? super POCPopulationState<?, G, S, Q>, ?>> stateFunctions,
              @Param("individualFunctions")
                  List<NamedFunction<? super Individual<G, S, Q>, ?>> individualFunctions,
              @Param("runKeys") List<String> runKeys,
              @Param(value = "deferred") boolean deferred,
              @Param(value = "onlyLast") boolean onlyLast) {
    record PopIndividualPair<G, S, Q>(POCPopulationState<?, G, S, Q> pop, Individual<G, S, Q> individual) {}
    return (experiment, executorService) -> {
      List<NamedFunction<? super POCPopulationState<?, G, S, Q>, ?>> popFunctions =
          Misc.concat(List.of(defaultStateFunctions, stateFunctions));
      List<NamedFunction<? super PopIndividualPair<G, S, Q>, ?>> pairFunctions = new ArrayList<>();
      popFunctions.stream()
          .map(f -> NamedFunction.build(
              f.getName(), f.getFormat(), (PopIndividualPair<G, S, Q> pair) -> f.apply(pair.pop())))
          .forEach(pairFunctions::add);
      individualFunctions.stream()
          .map(f -> NamedFunction.build(
              f.getName(),
              f.getFormat(),
              (PopIndividualPair<G, S, Q> pair) -> f.apply(pair.individual())))
          .forEach(pairFunctions::add);
      CSVPrinter<PopIndividualPair<G, S, Q>, Run<?, G, S, Q>> innerListenerFactory = new CSVPrinter<>(
          pairFunctions, buildRunNamedFunctions(runKeys, experiment), new File(filePath), true);
      ListenerFactory<? super POCPopulationState<?, G, S, Q>, Run<?, G, S, Q>> allListenerFactory =
          new ListenerFactory<>() {
            @Override
            public Listener<POCPopulationState<?, G, S, Q>> build(Run<?, G, S, Q> run) {
              Listener<PopIndividualPair<G, S, Q>> innerListener = innerListenerFactory.build(run);
              return new Listener<>() {
                @Override
                public void listen(POCPopulationState<?, G, S, Q> state) {
                  for (Individual<G, S, Q> individual :
                      state.pocPopulation().all()) {
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
      return new ListenerFactoryAndMonitor<>(allListenerFactory, deferred ? executorService : null, onlyLast);
    };
  }

  @SuppressWarnings("unused")
  public static <G, S, Q>
      BiFunction<
              Experiment,
              ExecutorService,
              ListenerFactory<? super POCPopulationState<?, G, S, Q>, Run<?, G, S, Q>>>
          bestCsv(
              @Param("filePath") String filePath,
              @Param(
                      value = "defaultFunctions",
                      dNPMs = {
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
                      })
                  List<NamedFunction<? super POCPopulationState<?, G, S, Q>, ?>>
                      defaultStateFunctions,
              @Param(value = "functions")
                  List<NamedFunction<? super POCPopulationState<?, G, S, Q>, ?>> stateFunctions,
              @Param("runKeys") List<String> runKeys,
              @Param(value = "deferred") boolean deferred,
              @Param(value = "onlyLast") boolean onlyLast) {
    return (experiment, executorService) -> new ListenerFactoryAndMonitor<>(
        new CSVPrinter<>(
            (List<NamedFunction<? super POCPopulationState<?, G, S, Q>, ?>>)
                Misc.concat(List.of(defaultStateFunctions, stateFunctions)),
            buildRunNamedFunctions(runKeys, experiment),
            new File(filePath),
            true),
        deferred ? executorService : null,
        onlyLast);
  }

  private static <G, S, Q> List<NamedFunction<? super Run<?, G, S, Q>, ?>> buildRunNamedFunctions(
      List<String> runKeys, Experiment experiment) {
    List<NamedFunction<? super Run<?, G, S, Q>, ?>> functions = new ArrayList<>();
    runKeys.stream()
        .map(k -> NamedFunction.build(
            String.join("+", Utils.interpolationKeys(k)),
            "%"
                .concat(""
                    + experiment.runs().stream()
                        .map(r -> Utils.interpolate(k, r.map()))
                        .mapToInt(String::length)
                        .max()
                        .orElse(10))
                .concat("s"),
            (Run<?, G, S, Q> run) -> Utils.interpolate(k, run.map())))
        .forEach(functions::add);
    return Collections.unmodifiableList(functions);
  }

  @SuppressWarnings("unused")
  public static <G, S, Q>
      BiFunction<Experiment, ExecutorService, ListenerFactory<POCPopulationState<?, G, S, Q>, Run<?, G, S, Q>>>
          console(
              @Param(
                      value = "defaultFunctions",
                      dNPMs = {
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
                      })
                  List<NamedFunction<? super POCPopulationState<?, G, S, Q>, ?>>
                      defaultStateFunctions,
              @Param(value = "functions")
                  List<NamedFunction<? super POCPopulationState<?, G, S, Q>, ?>> stateFunctions,
              @Param("runKeys") List<String> runKeys,
              @Param(value = "deferred") boolean deferred,
              @Param(value = "onlyLast") boolean onlyLast) {
    return (experiment, executorService) -> new ListenerFactoryAndMonitor<>(
        new TabularPrinter<>(
            (List<NamedFunction<? super POCPopulationState<?, G, S, Q>, ?>>)
                Misc.concat(List.of(defaultStateFunctions, stateFunctions)),
            buildRunNamedFunctions(runKeys, experiment)),
        deferred ? executorService : null,
        onlyLast);
  }

  private static String getCredentialFromFile(String credentialFilePath) {
    if (credentialFilePath == null) {
      throw new IllegalArgumentException("Credential file path not provided");
    }
    String credential;
    try (BufferedReader br = new BufferedReader(new FileReader(credentialFilePath))) {
      List<String> lines = br.lines().toList();
      if (lines.isEmpty()) {
        throw new IllegalArgumentException("Invalid credential file with 0 lines");
      }
      String[] pieces = lines.get(0).split("\\s");
      credential = pieces[0];
      L.config(String.format("Using provided credential: %s", credentialFilePath));
    } catch (IOException e) {
      throw new IllegalArgumentException(
          String.format("Cannot read credentials at %s: %s", credentialFilePath, e));
    }
    return credential;
  }

  @SuppressWarnings("unused")
  public static <G, S, Q>
      BiFunction<Experiment, ExecutorService, ListenerFactory<POCPopulationState<?, G, S, Q>, Run<?, G, S, Q>>>
          outcomeSaver(
              @Param(value = "filePathTemplate", dS = "run-outcome-{index:%04d}.txt")
                  String filePathTemplate,
              @Param(value = "deferred", dB = true) boolean deferred) {
    NamedFunction<Object, String> serializer = NamedFunctions.base64(x -> (Serializable) x);
    return (experiment, executorService) -> new ListenerFactoryAndMonitor<>(
        (ListenerFactory<POCPopulationState<?, G, S, Q>, Run<?, G, S, Q>>)
            run -> (Listener<POCPopulationState<?, G, S, Q>>) state -> {
              // obtain and serialize solutions
              List<String> serializedGenotypes = state.pocPopulation().firsts().stream()
                  .map(i -> serializer.apply(i.genotype()))
                  .toList();
              // prepare map
              NamedParamMap map = new MapNamedParamMap(
                  "ea.runOutcome",
                  Map.ofEntries(
                      Map.entry(
                          new MapNamedParamMap.TypedKey("index", ParamMap.Type.INT),
                          run.index()),
                      Map.entry(
                          new MapNamedParamMap.TypedKey("run", ParamMap.Type.NAMED_PARAM_MAP),
                          run.map()),
                      Map.entry(
                          new MapNamedParamMap.TypedKey(
                              "serializedGenotypes", ParamMap.Type.STRINGS),
                          serializedGenotypes)));
              // write on file
              File file = Misc.checkExistenceAndChangeName(
                  new File(Utils.interpolate(filePathTemplate, run)));
              try (BufferedWriter w = new BufferedWriter(new FileWriter(file))) {
                String prettyMap = MapNamedParamMap.prettyToString(map);
                w.append(prettyMap);
                w.flush();
              } catch (IOException e) {
                L.warning("Cannot save outcome file %s due to: %s".formatted(file.getPath(), e));
              }
            },
        deferred ? executorService : null,
        true);
  }

  @SuppressWarnings("unused")
  public static <G, S, Q>
      BiFunction<Experiment, ExecutorService, ListenerFactory<POCPopulationState<?, G, S, Q>, Run<?, G, S, Q>>>
          telegram(
              @Param("chatId") String chatId,
              @Param("botIdFilePath") String botIdFilePath,
              @Param(
                      value = "defaultPlots",
                      dNPMs = {"ea.plot.elapsed()"})
                  List<PlotTableBuilder<? super POCPopulationState<?, G, S, Q>>>
                      defaultPlotTableBuilders,
              @Param("plots")
                  List<PlotTableBuilder<? super POCPopulationState<?, G, S, Q>>> plotTableBuilders,
              @Param("accumulators")
                  List<AccumulatorFactory<? super POCPopulationState<?, G, S, Q>, ?, Run<?, G, S, Q>>>
                      accumulators,
              @Param("runKeys") List<String> runKeys, // TODO: these are currently ignored
              @Param(value = "deferred", dB = true) boolean deferred,
              @Param(value = "onlyLast") boolean onlyLast) {
    // read credential files
    long longChatId;
    String botId = getCredentialFromFile(botIdFilePath);
    try {
      longChatId = Long.parseLong(chatId);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Invalid chatId %s: not a number".formatted(chatId));
    }
    @SuppressWarnings({"unchecked", "rawtypes"})
    List<AccumulatorFactory<POCPopulationState<?, G, S, Q>, ?, Run<?, G, S, Q>>> accumulatorFactories =
        (List) Misc.concat(List.of(defaultPlotTableBuilders, plotTableBuilders, accumulators));
    return (experiment, executorService) -> new ListenerFactoryAndMonitor<>(
        new TelegramUpdater<>(accumulatorFactories, botId, longChatId),
        deferred ? executorService : null,
        onlyLast);
  }

  @SuppressWarnings("unused")
  public static <G, S, Q>
      BiFunction<
              Experiment,
              ExecutorService,
              ListenerFactory<GridPopulationState<G, S, Number>, Run<?, G, S, Number>>>
          heatMapVideo(
              @Param(value = "w", dI = 100) int w,
              @Param(value = "h", dI = 100) int h,
              @Param(value = "frameRate", dD = 4) double frameRate,
              @Param(value = "minColor", dS = "red") String minColor,
              @Param(value = "maxColor", dS = "white") String maxColor,
              @Param(value = "nullColor", dS = "blue") String nullColor,
              @Param(value = "gridColor", dS = "gray") String gridColor,
              @Param(value = "filePathTemplate", dS = "run-heatmap-{index:%04d}.mp4")
                  String filePathTemplate) {
    return (experiment, executorService) -> new ListenerFactoryAndMonitor<>(
        new HeatMapVideoAccumulatorFactory<G, S, Number>(
                Individual::quality,
                w,
                h,
                frameRate,
                ImagePlotters.getColorByName(minColor),
                ImagePlotters.getColorByName(maxColor),
                ImagePlotters.getColorByName(nullColor),
                ImagePlotters.getColorByName(gridColor),
                filePathTemplate)
            .withAutoGet(),
        executorService,
        false);
  }

  @SuppressWarnings("unused")
  public static <G, S, Q>
      BiFunction<Experiment, ExecutorService, ListenerFactory<POCPopulationState<?, G, S, Q>, Run<?, G, S, Q>>>
          net(
              @Param(
                      value = "defaultFunctions",
                      dNPMs = {
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
                      })
                  List<NamedFunction<? super POCPopulationState<?, G, S, Q>, ?>>
                      defaultStateFunctions,
              @Param(value = "functions")
                  List<NamedFunction<? super POCPopulationState<?, G, S, Q>, ?>> stateFunctions,
              @Param("runKeys") List<String> runKeys,
              @Param(value = "serverAddress", dS = "127.0.0.1") String serverAddress,
              @Param(value = "serverPort", dI = 10979) int serverPort,
              @Param(value = "serverKeyFilePath") String serverKeyFilePath,
              @Param(value = "pollInterval", dD = 1) double pollInterval) {
    NetMultiSink netMultiSink =
        new NetMultiSink(pollInterval, serverAddress, serverPort, getCredentialFromFile(serverKeyFilePath));
    return (experiment, executorService) -> new ListenerFactoryAndMonitor<>(
        new SinkListenerFactory<>(
            Misc.concat(List.of(defaultStateFunctions, stateFunctions)),
            buildRunNamedFunctions(runKeys, experiment),
            experiment,
            netMultiSink.getMachineSink(),
            netMultiSink.getProcessSink(),
            netMultiSink.getLogSink(),
            netMultiSink.getExperimentSink(),
            netMultiSink.getRunSink(),
            netMultiSink.getDatItemSink()),
        executorService,
        false);
  }

  @SuppressWarnings("unused")
  public static <G, S, Q>
      BiFunction<Experiment, ExecutorService, ListenerFactory<POCPopulationState<?, G, S, Q>, Run<?, G, S, Q>>>
          tui(
              @Param(
                      value = "defaultFunctions",
                      dNPMs = {
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
                      })
                  List<NamedFunction<? super POCPopulationState<?, G, S, Q>, ?>>
                      defaultStateFunctions,
              @Param(value = "functions")
                  List<NamedFunction<? super POCPopulationState<?, G, S, Q>, ?>> stateFunctions,
              @Param("runKeys") List<String> runKeys) {
    DirectSinkSource<MachineKey, MachineInfo> machineSinkSource = new DirectSinkSource<>();
    DirectSinkSource<ProcessKey, ProcessInfo> processSinkSource = new DirectSinkSource<>();
    DirectSinkSource<ProcessKey, LogInfo> logSinkSource = new DirectSinkSource<>();
    DirectSinkSource<ExperimentKey, ExperimentInfo> experimentSinkSource = new DirectSinkSource<>();
    DirectSinkSource<RunKey, RunInfo> runSinkSource = new DirectSinkSource<>();
    DirectSinkSource<DataItemKey, DataItemInfo> dataItemSinkSource = new DirectSinkSource<>();
    new TuiMonitor(
            () -> "Local",
            machineSinkSource,
            processSinkSource,
            logSinkSource,
            experimentSinkSource,
            runSinkSource,
            dataItemSinkSource)
        .run();
    return (experiment, executorService) -> new ListenerFactoryAndMonitor<>(
        new SinkListenerFactory<>(
            Misc.concat(List.of(defaultStateFunctions, stateFunctions)),
            buildRunNamedFunctions(runKeys, experiment),
            experiment,
            machineSinkSource,
            processSinkSource,
            logSinkSource,
            experimentSinkSource,
            runSinkSource,
            dataItemSinkSource),
        executorService,
        false);
  }
}
