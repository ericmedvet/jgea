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

package io.github.ericmedvet.jgea.experimenter.builders;

import io.github.ericmedvet.jgea.core.listener.*;
import io.github.ericmedvet.jgea.core.solver.Individual;
import io.github.ericmedvet.jgea.core.solver.POCPopulationState;
import io.github.ericmedvet.jgea.core.util.Misc;
import io.github.ericmedvet.jgea.core.util.Progress;
import io.github.ericmedvet.jgea.experimenter.Experiment;
import io.github.ericmedvet.jgea.experimenter.Run;
import io.github.ericmedvet.jgea.experimenter.Utils;
import io.github.ericmedvet.jgea.experimenter.listener.CSVPrinter;
import io.github.ericmedvet.jgea.experimenter.listener.decoupled.*;
import io.github.ericmedvet.jgea.experimenter.listener.net.NetMultiSink;
import io.github.ericmedvet.jgea.experimenter.listener.plot.PlotAccumulatorFactory;
import io.github.ericmedvet.jgea.experimenter.listener.telegram.TelegramUpdater;
import io.github.ericmedvet.jnb.core.*;
import io.github.ericmedvet.jnb.datastructure.FormattedFunction;
import io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction;
import io.github.ericmedvet.jnb.datastructure.NamedFunction;
import io.github.ericmedvet.jviz.core.drawer.ImageBuilder;
import io.github.ericmedvet.jviz.core.drawer.VideoBuilder;
import io.github.ericmedvet.jviz.core.util.VideoUtils;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javax.imageio.ImageIO;

@Discoverable(prefixTemplate = "ea.listener|l")
public class Listeners {

  private static final Logger L = Logger.getLogger(Listeners.class.getName());

  private Listeners() {}

  private static class ListenerFactoryAndMonitor<E, K> implements ListenerFactory<E, K>, ProgressMonitor {
    private final ListenerFactory<E, K> innerListenerFactory;
    private final ListenerFactory<E, K> outerListenerFactory;

    public ListenerFactoryAndMonitor(
        ListenerFactory<E, K> innerListenerFactory,
        Predicate<K> predicate,
        ExecutorService executorService,
        boolean onLast) {
      this.innerListenerFactory = innerListenerFactory;
      if (onLast) {
        if (executorService != null) {
          outerListenerFactory = innerListenerFactory
              .onLast()
              .deferred(executorService)
              .conditional(predicate);
        } else {
          outerListenerFactory = innerListenerFactory.onLast().conditional(predicate);
        }
      } else {
        if (executorService != null) {
          outerListenerFactory =
              innerListenerFactory.deferred(executorService).conditional(predicate);
        } else {
          outerListenerFactory = innerListenerFactory.conditional(predicate);
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
      BiFunction<Experiment, ExecutorService, ListenerFactory<POCPopulationState<?, G, S, Q, ?>, Run<?, G, S, Q>>>
          allCsv(
              @Param("filePath") String filePath,
              @Param(value = "errorString", dS = "NA") String errorString,
              @Param(value = "intFormat", dS = "%d") String intFormat,
              @Param(value = "doubleFormat", dS = "%.5e") String doubleFormat,
              @Param(
                      value = "defaultFunctions",
                      dNPMs = {"ea.f.nOfIterations()"})
                  List<Function<? super POCPopulationState<?, G, S, Q, ?>, ?>> defaultStateFunctions,
              @Param(value = "functions")
                  List<Function<? super POCPopulationState<?, G, S, Q, ?>, ?>> stateFunctions,
              @Param("individualFunctions")
                  List<Function<? super Individual<G, S, Q>, ?>> individualFunctions,
              @Param(
                      value = "defaultRunFunctions",
                      dNPMs = {
                        "ea.f.runKey(key = \"problem.name\")",
                        "ea.f.runKey(key = \"solver.name\")",
                        "ea.f.runKey(key = " + "\"randomGenerator.seed\")"
                      })
                  List<Function<? super Run<?, G, S, Q>, ?>> defaultRunFunctions,
              @Param("runFunctions") List<Function<? super Run<?, G, S, Q>, ?>> runFunctions,
              @Param(value = "deferred") boolean deferred,
              @Param(value = "onlyLast") boolean onlyLast,
              @Param(value = "condition", dNPM = "predicate.always()")
                  Predicate<Run<?, G, S, Q>> predicate) {
    record PopIndividualPair<G, S, Q>(POCPopulationState<?, G, S, Q, ?> pop, Individual<G, S, Q> individual) {}
    Function<? super PopIndividualPair<G, S, Q>, POCPopulationState<?, G, S, Q, ?>> pairPopF =
        NamedFunction.from(PopIndividualPair::pop, "state");
    Function<? super PopIndividualPair<G, S, Q>, Individual<G, S, Q>> pairIndividualF =
        NamedFunction.from(PopIndividualPair::individual, "individual");
    return (experiment, executorService) -> {
      List<Function<? super PopIndividualPair<G, S, Q>, ?>> pairFunctions = new ArrayList<>();
      Stream.concat(defaultStateFunctions.stream(), stateFunctions.stream())
          .map(f -> (Function<? super PopIndividualPair<G, S, Q>, ?>)
              FormattedNamedFunction.from(f).compose(pairPopF))
          .forEach(pairFunctions::add);
      individualFunctions.stream()
          .map(f -> FormattedNamedFunction.from(f).compose(pairIndividualF))
          .forEach(pairFunctions::add);
      ListenerFactory<PopIndividualPair<G, S, Q>, Run<?, G, S, Q>> innerListenerFactory = new CSVPrinter<>(
          pairFunctions,
          Stream.concat(defaultRunFunctions.stream(), runFunctions.stream())
              .map(f -> reformatToFit(f, experiment.runs()))
              .toList(),
          new File(filePath),
          errorString,
          intFormat,
          doubleFormat);
      ListenerFactory<POCPopulationState<?, G, S, Q, ?>, Run<?, G, S, Q>> allListenerFactory =
          new ListenerFactory<>() {
            @Override
            public Listener<POCPopulationState<?, G, S, Q, ?>> build(Run<?, G, S, Q> run) {
              Listener<PopIndividualPair<G, S, Q>> innerListener = innerListenerFactory.build(run);
              return new Listener<>() {
                @Override
                public void listen(POCPopulationState<?, G, S, Q, ?> state) {
                  for (Individual<G, S, Q> individual :
                      state.pocPopulation().all()) {
                    innerListener.listen(new PopIndividualPair<>(state, individual));
                  }
                }

                @Override
                public void done() {
                  innerListener.done();
                }

                @Override
                public String toString() {
                  return innerListener + "[all→individuals]";
                }
              };
            }

            @Override
            public void shutdown() {
              innerListenerFactory.shutdown();
            }
          };
      return new ListenerFactoryAndMonitor<>(
          allListenerFactory, predicate, deferred ? executorService : null, onlyLast);
    };
  }

  @SuppressWarnings("unused")
  public static <G, S, Q>
      BiFunction<Experiment, ExecutorService, ListenerFactory<POCPopulationState<?, G, S, Q, ?>, Run<?, G, S, Q>>>
          bestCsv(
              @Param("filePath") String filePath,
              @Param(value = "errorString", dS = "NA") String errorString,
              @Param(value = "intFormat", dS = "%d") String intFormat,
              @Param(value = "doubleFormat", dS = "%.5e") String doubleFormat,
              @Param(
                      value = "defaultFunctions",
                      dNPMs = {
                        "ea.f.nOfIterations()",
                        "ea.f.nOfEvals()",
                        "ea.f.nOfBirths()",
                        "ea.f.elapsedSecs()",
                        "f.size(of=ea.f.all())",
                        "f.size(of=ea.f.firsts())",
                        "f.size(of=ea.f.lasts())",
                        "f.uniqueness(of=f.each(mapF=ea.f.genotype();of=ea.f.all()))",
                        "f.uniqueness(of=f.each(mapF=ea.f.solution();of=ea.f.all()))",
                        "f.uniqueness(of=f.each(mapF=ea.f.quality();of=ea.f.all()))"
                      })
                  List<Function<? super POCPopulationState<?, G, S, Q, ?>, ?>> defaultStateFunctions,
              @Param(value = "functions")
                  List<Function<? super POCPopulationState<?, G, S, Q, ?>, ?>> stateFunctions,
              @Param(
                      value = "defaultRunFunctions",
                      dNPMs = {
                        "ea.f.runKey(key = \"problem.name\")",
                        "ea.f.runKey(key = \"solver.name\")",
                        "ea.f.runKey(key = " + "\"randomGenerator.seed\")"
                      })
                  List<Function<? super Run<?, G, S, Q>, ?>> defaultRunFunctions,
              @Param("runFunctions") List<Function<? super Run<?, G, S, Q>, ?>> runFunctions,
              @Param(value = "deferred") boolean deferred,
              @Param(value = "onlyLast") boolean onlyLast,
              @Param(value = "condition", dNPM = "predicate.always()")
                  Predicate<Run<?, G, S, Q>> predicate) {
    return (experiment, executorService) -> new ListenerFactoryAndMonitor<>(
        new CSVPrinter<>(
            Stream.of(defaultStateFunctions, stateFunctions)
                .flatMap(List::stream)
                .toList(),
            Stream.concat(defaultRunFunctions.stream(), runFunctions.stream())
                .map(f -> reformatToFit(f, experiment.runs()))
                .toList(),
            new File(filePath),
            errorString,
            intFormat,
            doubleFormat),
        predicate,
        deferred ? executorService : null,
        onlyLast);
  }

  @SuppressWarnings("unused")
  public static <G, S, Q>
      BiFunction<Experiment, ExecutorService, ListenerFactory<POCPopulationState<?, G, S, Q, ?>, Run<?, G, S, Q>>>
          console(
              @Param(
                      value = "defaultFunctions",
                      dNPMs = {
                        "ea.f.nOfIterations()",
                        "ea.f.nOfEvals()",
                        "ea.f.nOfBirths()",
                        "ea.f.elapsedSecs()",
                        "f.size(of=ea.f.all())",
                        "f.size(of=ea.f.firsts())",
                        "f.size(of=ea.f.lasts())",
                        "f.uniqueness(of=f.each(mapF=ea.f.genotype();of=ea.f.all()))",
                        "f.uniqueness(of=f.each(mapF=ea.f.solution();of=ea.f.all()))",
                        "f.uniqueness(of=f.each(mapF=ea.f.quality();of=ea.f.all()))"
                      })
                  List<Function<? super POCPopulationState<?, G, S, Q, ?>, ?>> defaultStateFunctions,
              @Param(value = "functions")
                  List<Function<? super POCPopulationState<?, G, S, Q, ?>, ?>> stateFunctions,
              @Param(
                      value = "defaultRunFunctions",
                      dNPMs = {
                        "ea.f.runKey(key = \"problem.name\")",
                        "ea.f.runKey(key = \"solver.name\")",
                        "ea.f.runKey(key = " + "\"randomGenerator.seed\")"
                      })
                  List<Function<? super Run<?, G, S, Q>, ?>> defaultRunFunctions,
              @Param("runFunctions") List<Function<? super Run<?, G, S, Q>, ?>> runFunctions,
              @Param(value = "deferred") boolean deferred,
              @Param(value = "onlyLast") boolean onlyLast,
              @Param(value = "condition", dNPM = "predicate.always()")
                  Predicate<Run<?, G, S, Q>> predicate) {
    return (experiment, executorService) -> new ListenerFactoryAndMonitor<>(
        new TabularPrinter<>(
            Stream.of(defaultStateFunctions, stateFunctions)
                .flatMap(List::stream)
                .toList(),
            Stream.concat(defaultRunFunctions.stream(), runFunctions.stream())
                .map(f -> reformatToFit(f, experiment.runs()))
                .toList()),
        predicate,
        deferred ? executorService : null,
        onlyLast);
  }

  @SuppressWarnings("unused")
  @Alias(
      name = "expPlotSaver",
      value = // spotless:off
          """
              expSaver(
                processors = [ea.f.imagePlotter()]
              )
              """) // spotless:on
  public static <E, O, R> BiFunction<Experiment, ExecutorService, ListenerFactory<E, R>> expSaver(
      @Param("of") AccumulatorFactory<E, O, R> accumulatorFactory,
      @Param(
              value = "processors",
              dNPMs = {"f.identity()"})
          List<Function<? super O, ?>> processors,
      @Param(value = "filePath", dS = "exp") String filePath,
      @Param(value = "condition", dNPM = "predicate.always()") Predicate<R> predicate) {
    return (experiment, executorService) -> new ListenerFactoryAndMonitor<>(
        accumulatorFactory.thenOnShutdown(
            os -> processors.forEach(f -> save(f.apply(os.get(os.size() - 1)), filePath))),
        predicate,
        executorService,
        false);
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

  public static <G, S, Q>
      BiFunction<Experiment, ExecutorService, ListenerFactory<POCPopulationState<?, G, S, Q, ?>, Run<?, G, S, Q>>>
          net(
              @Param(
                      value = "defaultFunctions",
                      dNPMs = {
                        "ea.f.nOfIterations()",
                        "ea.f.nOfEvals()",
                        "ea.f.nOfBirths()",
                        "ea.f.elapsedSecs()",
                        "f.size(of=ea.f.all())",
                        "f.size(of=ea.f.firsts())",
                        "f.size(of=ea.f.lasts())",
                        "f.uniqueness(of=f.each(mapF=ea.f.genotype();of=ea.f.all()))",
                        "f.uniqueness(of=f.each(mapF=ea.f.solution();of=ea.f.all()))",
                        "f.uniqueness(of=f.each(mapF=ea.f.quality();of=ea.f.all()))"
                      })
                  List<NamedFunction<? super POCPopulationState<?, G, S, Q, ?>, ?>>
                      defaultStateFunctions,
              @Param(value = "functions")
                  List<NamedFunction<? super POCPopulationState<?, G, S, Q, ?>, ?>> stateFunctions,
              @Param(
                      value = "defaultRunFunctions",
                      dNPMs = {
                        "ea.f.runKey(key = \"problem.name\")",
                        "ea.f.runKey(key = \"solver.name\")",
                        "ea.f.runKey(key = " + "\"randomGenerator.seed\")"
                      })
                  List<Function<? super Run<?, G, S, Q>, ?>> defaultRunFunctions,
              @Param("runFunctions") List<Function<? super Run<?, G, S, Q>, ?>> runFunctions,
              @Param(value = "serverAddress", dS = "127.0.0.1") String serverAddress,
              @Param(value = "serverPort", dI = 10979) int serverPort,
              @Param(value = "serverKeyFilePath") String serverKeyFilePath,
              @Param(value = "pollInterval", dD = 1) double pollInterval,
              @Param(value = "condition", dNPM = "predicate.always()")
                  Predicate<Run<?, G, S, Q>> predicate) {

    NetMultiSink netMultiSink =
        new NetMultiSink(pollInterval, serverAddress, serverPort, getCredentialFromFile(serverKeyFilePath));
    return (experiment, executorService) -> new ListenerFactoryAndMonitor<>(
        new SinkListenerFactory<>(
            Misc.concat(List.of(defaultStateFunctions, stateFunctions)),
            Stream.concat(defaultRunFunctions.stream(), runFunctions.stream())
                .map(f -> reformatToFit(f, experiment.runs()))
                .toList(),
            experiment,
            netMultiSink.getMachineSink(),
            netMultiSink.getProcessSink(),
            netMultiSink.getLogSink(),
            netMultiSink.getExperimentSink(),
            netMultiSink.getRunSink(),
            netMultiSink.getDatItemSink()),
        predicate,
        executorService,
        false);
  }

  private static <T, R> Function<T, R> reformatToFit(Function<T, R> f, Collection<?> ts) {
    //noinspection unchecked
    return FormattedFunction.from(f)
        .reformattedToFit(ts.stream().map(t -> (T) t).toList());
  }

  @SuppressWarnings("unused")
  public static <G, S, Q, K>
      BiFunction<Experiment, ExecutorService, ListenerFactory<POCPopulationState<?, G, S, Q, ?>, Run<?, G, S, Q>>>
          runAllIterationsVideoSaver(
              @Param(value = "function", dNPM = "ea.f.best()")
                  Function<POCPopulationState<?, G, S, Q, ?>, K> function,
              @Param("image") ImageBuilder<K> imageBuilder,
              @Param(value = "w", dI = 500) int w,
              @Param(value = "h", dI = 500) int h,
              @Param(value = "encoder", dS = "jcodec") VideoUtils.EncoderFacility encoder,
              @Param(value = "frameRate", dD = 20) double frameRate,
              @Param(value = "filePathTemplate", dS = "run-{index:%04d}.mp4") String filePathTemplate,
              @Param(value = "condition", dNPM = "predicate.always()")
                  Predicate<Run<?, G, S, Q>> predicate) {
    VideoBuilder<List<K>> videoBuilder = VideoBuilder.from(imageBuilder, ks -> ks, frameRate);
    return (experiment, executorService) -> new ListenerFactoryAndMonitor<>(
        AccumulatorFactory.<POCPopulationState<?, G, S, Q, ?>, K, Run<?, G, S, Q>>collector(function)
            .thenOnDone((run, ks) -> {
              File file = Misc.checkExistenceAndChangeName(
                  new File(Utils.interpolate(filePathTemplate, run)));
              try {
                videoBuilder.save(new VideoBuilder.VideoInfo(w, h, encoder), file, ks);
              } catch (IOException e) {
                L.severe("Cannot save video at '%s': %s".formatted(file.getPath(), e));
              }
            }),
        predicate,
        executorService,
        false);
  }

  @SuppressWarnings("unused")
  public static <G, S, Q, K>
      BiFunction<Experiment, ExecutorService, ListenerFactory<POCPopulationState<?, G, S, Q, ?>, Run<?, G, S, Q>>>
          runLastIterationImageSaver(
              @Param(value = "function", dNPM = "ea.f.best()")
                  Function<POCPopulationState<?, G, S, Q, ?>, K> function,
              @Param("image") ImageBuilder<K> imagerBuilder,
              @Param(value = "w", dI = 500) int w,
              @Param(value = "h", dI = 500) int h,
              @Param(value = "filePathTemplate", dS = "run-{index:%04d}.png") String filePathTemplate,
              @Param(value = "condition", dNPM = "predicate.always()")
                  Predicate<Run<?, G, S, Q>> predicate) {
    return (experiment, executorService) -> new ListenerFactoryAndMonitor<>(
        run -> Listener.<POCPopulationState<?, G, S, Q, ?>>named(
            state -> {
              File file = Misc.checkExistenceAndChangeName(
                  new File(Utils.interpolate(filePathTemplate, run)));
              try {
                imagerBuilder.save(new ImageBuilder.ImageInfo(w, h), file, function.apply(state));
              } catch (IOException e) {
                L.severe("Cannot save image at '%s': %s".formatted(file.getPath(), e));
              }
            },
            "runLastIterationImageSaver"),
        predicate,
        executorService,
        true);
  }

  @SuppressWarnings("unused")
  public static <G, S, Q, K>
      BiFunction<Experiment, ExecutorService, ListenerFactory<POCPopulationState<?, G, S, Q, ?>, Run<?, G, S, Q>>>
          runLastIterationVideoSaver(
              @Param(value = "function", dNPM = "ea.f.best()")
                  Function<POCPopulationState<?, G, S, Q, ?>, K> function,
              @Param("video") VideoBuilder<K> videoBuilder,
              @Param(value = "w", dI = 500) int w,
              @Param(value = "h", dI = 500) int h,
              @Param(value = "encoder", dS = "jcodec") VideoUtils.EncoderFacility encoder,
              @Param(value = "filePathTemplate", dS = "run-{index:%04d}.mp4") String filePathTemplate,
              @Param(value = "condition", dNPM = "predicate.always()")
                  Predicate<Run<?, G, S, Q>> predicate) {
    return (experiment, executorService) -> new ListenerFactoryAndMonitor<>(
        run -> Listener.<POCPopulationState<?, G, S, Q, ?>>named(
            state -> {
              File file = Misc.checkExistenceAndChangeName(
                  new File(Utils.interpolate(filePathTemplate, run)));
              try {
                videoBuilder.save(
                    new VideoBuilder.VideoInfo(w, h, encoder), file, function.apply(state));
              } catch (IOException e) {
                L.severe("Cannot save video at '%s': %s".formatted(file.getPath(), e));
              }
            },
            "runLastIterationVideoSaver"),
        predicate,
        executorService,
        true);
  }

  @SuppressWarnings("unused")
  @Alias(
      name = "runPlotSaver",
      value = // spotless:off
          """
              runSaver(
                processors = [ea.f.imagePlotter()]
              )
              """) // spotless:on
  @Alias(
      name = "lastPopulationSaver",
      value = // spotless:off
          """
              runSaver(
                of = ea.misc.lastPopulationMap()
              )
              """) // spotless:on
  public static <E, O> BiFunction<Experiment, ExecutorService, ListenerFactory<E, Run<?, ?, ?, ?>>> runSaver(
      @Param("of") AccumulatorFactory<E, O, Run<?, ?, ?, ?>> accumulatorFactory,
      @Param(
              value = "processors",
              dNPMs = {"f.identity()"})
          List<Function<? super O, ?>> processors,
      @Param(value = "filePathTemplate", dS = "run-{index:%04d}") String filePathTemplate,
      @Param(value = "condition", dNPM = "predicate.always()") Predicate<Run<?, ?, ?, ?>> predicate) {
    return (experiment, executorService) -> new ListenerFactoryAndMonitor<>(
        accumulatorFactory.thenOnDone(
            (r, o) -> processors.forEach(f -> save(f.apply(o), Utils.interpolate(filePathTemplate, r)))),
        predicate,
        executorService,
        false);
  }

  private static void save(Object o, String filePath) {
    File file = null;
    try {
      if (o instanceof BufferedImage image) {
        file = Misc.checkExistenceAndChangeName(new File(filePath + ".png"));
        ImageIO.write(image, "png", file);
      } else if (o instanceof String s) {
        file = Misc.checkExistenceAndChangeName(new File(filePath + ".txt"));
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
          for (String l : s.lines().toList()) {
            bw.append(l);
            bw.newLine();
          }
        }
      } else if (o instanceof VideoBuilder.Video video) {
        file = Misc.checkExistenceAndChangeName(new File(filePath + ".mp4"));
        VideoUtils.encodeAndSave(video.images(), video.frameRate(), file);
      } else if (o instanceof byte[] data) {
        file = Misc.checkExistenceAndChangeName(new File(filePath + ".bin"));
        try (OutputStream os = new FileOutputStream(file)) {
          os.write(data);
        }
      } else if (o instanceof NamedParamMap npm) {
        save(MapNamedParamMap.prettyToString(npm), filePath);
      } else {
        throw new IllegalArgumentException(
            "Cannot save data of type %s".formatted(o.getClass().getSimpleName()));
      }
    } catch (IOException e) {
      throw new RuntimeException("Cannot save '%s'".formatted(file.getPath()), e);
    }
  }

  @SuppressWarnings("unused")
  public static <G, S, Q>
      BiFunction<Experiment, ExecutorService, ListenerFactory<POCPopulationState<?, G, S, Q, ?>, Run<?, G, S, Q>>>
          telegram(
              @Param("chatId") String chatId,
              @Param("botIdFilePath") String botIdFilePath,
              @Param(
                      value = "defaultPlots",
                      dNPMs = {"ea.plot.elapsed()"})
                  List<
                          PlotAccumulatorFactory<
                              ? super POCPopulationState<?, G, S, Q, ?>,
                              ?,
                              Run<?, G, S, Q>,
                              ?>>
                      defaultPlotTableBuilders,
              @Param("plots")
                  List<
                          PlotAccumulatorFactory<
                              ? super POCPopulationState<?, G, S, Q, ?>,
                              ?,
                              Run<?, G, S, Q>,
                              ?>>
                      plotTableBuilders,
              @Param("accumulators")
                  List<
                          AccumulatorFactory<
                              ? super POCPopulationState<?, G, S, Q, ?>,
                              ?,
                              Run<?, G, S, Q>>>
                      accumulators,
              @Param(value = "deferred", dB = true) boolean deferred,
              @Param(value = "onlyLast") boolean onlyLast,
              @Param(value = "condition", dNPM = "predicate.always()")
                  Predicate<Run<?, G, S, Q>> predicate) {

    // read credential files
    long longChatId;
    String botId = getCredentialFromFile(botIdFilePath);
    try {
      longChatId = Long.parseLong(chatId);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Invalid chatId %s: not a number".formatted(chatId));
    }
    @SuppressWarnings({"unchecked", "rawtypes"})
    List<AccumulatorFactory<POCPopulationState<?, G, S, Q, ?>, ?, Run<?, G, S, Q>>> accumulatorFactories =
        (List) Misc.concat(List.of(defaultPlotTableBuilders, plotTableBuilders, accumulators));
    return (experiment, executorService) -> new ListenerFactoryAndMonitor<>(
        new TelegramUpdater<>(accumulatorFactories, botId, longChatId),
        predicate,
        deferred ? executorService : null,
        onlyLast);
  }

  @SuppressWarnings("unused")
  public static <G, S, Q>
      BiFunction<Experiment, ExecutorService, ListenerFactory<POCPopulationState<?, G, S, Q, ?>, Run<?, G, S, Q>>>
          tui(
              @Param(
                      value = "defaultFunctions",
                      dNPMs = {
                        "ea.f.nOfIterations()",
                        "ea.f.nOfEvals()",
                        "ea.f.nOfBirths()",
                        "ea.f.elapsedSecs()",
                        "f.size(of=ea.f.all())",
                        "f.size(of=ea.f.firsts())",
                        "f.size(of=ea.f.lasts())",
                        "f.uniqueness(of=f.each(mapF=ea.f.genotype();of=ea.f.all()))",
                        "f.uniqueness(of=f.each(mapF=ea.f.solution();of=ea.f.all()))",
                        "f.uniqueness(of=f.each(mapF=ea.f.quality();of=ea.f.all()))"
                      })
                  List<NamedFunction<? super POCPopulationState<?, G, S, Q, ?>, ?>>
                      defaultStateFunctions,
              @Param(value = "functions")
                  List<NamedFunction<? super POCPopulationState<?, G, S, Q, ?>, ?>> stateFunctions,
              @Param(
                      value = "defaultRunFunctions",
                      dNPMs = {
                        "ea.f.runKey(key = \"problem.name\")",
                        "ea.f.runKey(key = \"solver.name\")",
                        "ea.f.runKey(key = " + "\"randomGenerator.seed\")"
                      })
                  List<Function<? super Run<?, G, S, Q>, ?>> defaultRunFunctions,
              @Param("runFunctions") List<Function<? super Run<?, G, S, Q>, ?>> runFunctions,
              @Param(value = "condition", dNPM = "predicate.always()")
                  Predicate<Run<?, G, S, Q>> predicate) {
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
            Stream.concat(defaultRunFunctions.stream(), runFunctions.stream())
                .map(f -> reformatToFit(f, experiment.runs()))
                .toList(),
            experiment,
            machineSinkSource,
            processSinkSource,
            logSinkSource,
            experimentSinkSource,
            runSinkSource,
            dataItemSinkSource),
        predicate,
        executorService,
        false);
  }
}
