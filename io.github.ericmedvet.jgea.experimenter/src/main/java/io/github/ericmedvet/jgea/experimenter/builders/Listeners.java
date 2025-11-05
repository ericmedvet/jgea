/*-
 * ========================LICENSE_START=================================
 * jgea-experimenter
 * %%
 * Copyright (C) 2018 - 2025 Eric Medvet
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
import io.github.ericmedvet.jgea.core.util.Naming;
import io.github.ericmedvet.jgea.core.util.Progress;
import io.github.ericmedvet.jgea.experimenter.Experiment;
import io.github.ericmedvet.jgea.experimenter.Run;
import io.github.ericmedvet.jgea.experimenter.Utils;
import io.github.ericmedvet.jgea.experimenter.listener.CSVPrinter;
import io.github.ericmedvet.jgea.experimenter.listener.decoupled.*;
import io.github.ericmedvet.jgea.experimenter.listener.net.NetMultiSink;
import io.github.ericmedvet.jnb.core.*;
import io.github.ericmedvet.jnb.core.ParamMap.Type;
import io.github.ericmedvet.jnb.datastructure.FormattedFunction;
import io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction;
import io.github.ericmedvet.jnb.datastructure.NamedFunction;
import io.github.ericmedvet.jnb.datastructure.TriConsumer;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.stream.Stream;

@Discoverable(prefixTemplate = "ea.listener|l")
public class Listeners {

  private static final Logger L = Logger.getLogger(Listeners.class.getName());

  private Listeners() {
  }

  public static class ListenerFactoryAndMonitor<E, K> implements ListenerFactory<E, K>, ProgressMonitor {

    private final ListenerFactory<E, K> innerListenerFactory;

    public ListenerFactoryAndMonitor(
        ListenerFactory<E, K> innerListenerFactory,
        Predicate<K> kPredicate,
        Predicate<E> ePredicate,
        Executor executor,
        boolean onLast
    ) {
      if (executor != null) {
        innerListenerFactory = innerListenerFactory.deferred(executor);
      }
      if (onLast) {
        innerListenerFactory = innerListenerFactory.onLast();
      }
      this.innerListenerFactory = innerListenerFactory.conditional(kPredicate, ePredicate);
    }

    @Override
    public Listener<E> build(K k) {
      return innerListenerFactory.build(k);
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

    @Override
    public String toString() {
      return innerListenerFactory.toString();
    }
  }

  @SuppressWarnings("unused")
  public static <G, S, Q> BiFunction<Experiment, Executor, ListenerFactory<POCPopulationState<?, G, S, Q, ?>, Run<?, G, S, Q>>> allCsv(
      @Param("path") String path,
      @Param(value = "errorString", dS = "NA") String errorString,
      @Param(value = "intFormat", dS = "%d") String intFormat,
      @Param(value = "doubleFormat", dS = "%.5e") String doubleFormat,
      @Param(
          value = "defaultFunctions", dNPMs = {"ea.f.nOfIterations()"}) List<Function<? super POCPopulationState<?, G, S, Q, ?>, ?>> defaultStateFunctions,
      @Param(value = "functions") List<Function<? super POCPopulationState<?, G, S, Q, ?>, ?>> stateFunctions,
      @Param("individualFunctions") List<Function<? super Individual<G, S, Q>, ?>> individualFunctions,
      @Param(
          value = "defaultRunFunctions", dNPMs = {"ea.f.runKey(key = \"run.problem.name\")", "ea.f.runKey(key = \"run" + ".solver.name\")", "ea.f.runKey(key = " + "\"run.randomGenerator.seed\")"
          }) List<Function<? super Run<?, G, S, Q>, ?>> defaultRunFunctions,
      @Param("runFunctions") List<Function<? super Run<?, G, S, Q>, ?>> runFunctions,
      @Param(value = "deferred") boolean deferred,
      @Param(value = "onlyLast") boolean onlyLast,
      @Param(value = "runCondition", dNPM = "predicate.always()") Predicate<Run<?, G, S, Q>> runPredicate,
      @Param(value = "stateCondition", dNPM = "predicate.always()") Predicate<POCPopulationState<?, G, S, Q, ?>> statePredicate
  ) {
    record PopIndividualPair<G, S, Q>(
        POCPopulationState<?, G, S, Q, ?> pop,
        Individual<G, S, Q> individual
    ) {

    }
    Function<? super PopIndividualPair<G, S, Q>, POCPopulationState<?, G, S, Q, ?>> pairPopF = NamedFunction.from(
        PopIndividualPair::pop,
        "state"
    );
    Function<? super PopIndividualPair<G, S, Q>, Individual<G, S, Q>> pairIndividualF = NamedFunction.from(
        PopIndividualPair::individual,
        "individual"
    );
    return (experiment, executor) -> {
      List<Function<? super PopIndividualPair<G, S, Q>, ?>> pairFunctions = new ArrayList<>();
      Stream.concat(defaultStateFunctions.stream(), stateFunctions.stream())
          .map(
              f -> (Function<? super PopIndividualPair<G, S, Q>, ?>) FormattedNamedFunction.from(f)
                  .compose(pairPopF)
          )
          .forEach(pairFunctions::add);
      individualFunctions.stream()
          .map(f -> FormattedNamedFunction.from(f).compose(pairIndividualF))
          .forEach(pairFunctions::add);
      ListenerFactory<PopIndividualPair<G, S, Q>, Run<?, G, S, Q>> innerListenerFactory = new CSVPrinter<>(
          pairFunctions,
          Stream.concat(defaultRunFunctions.stream(), runFunctions.stream())
              .map(f -> reformatToFit(f, experiment.runs()))
              .toList(),
          Utils.interpolate(path, experiment, null),
          errorString,
          intFormat,
          doubleFormat
      );
      ListenerFactory<POCPopulationState<?, G, S, Q, ?>, Run<?, G, S, Q>> allListenerFactory = new ListenerFactory<>() {
        @Override
        public Listener<POCPopulationState<?, G, S, Q, ?>> build(Run<?, G, S, Q> run) {
          Listener<PopIndividualPair<G, S, Q>> innerListener = innerListenerFactory.build(run);
          return new Listener<>() {
            @Override
            public void listen(POCPopulationState<?, G, S, Q, ?> state) {
              for (Individual<G, S, Q> individual : state.pocPopulation().all()) {
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
          allListenerFactory,
          runPredicate,
          statePredicate,
          deferred ? executor : null,
          onlyLast
      );
    };
  }

  @SuppressWarnings("unused")
  public static <G, S, Q> BiFunction<Experiment, Executor, ListenerFactory<POCPopulationState<?, G, S, Q, ?>, Run<?, G, S, Q>>> bestCsv(
      @Param("path") String path,
      @Param(value = "errorString", dS = "NA") String errorString,
      @Param(value = "intFormat", dS = "%d") String intFormat,
      @Param(value = "doubleFormat", dS = "%.5e") String doubleFormat,
      @Param(
          value = "defaultFunctions", dNPMs = {"ea.f.nOfIterations()", "ea.f.nOfEvals()", "ea.f.nOfBirths()", "ea.f" + ".elapsedSecs()", "f.size(of=ea.f.all())", "f.size(of=ea.f.firsts())", "f.size(of=ea.f.lasts())", "f" + ".uniqueness(of=f.each(mapF=ea.f.genotype();of=ea.f.all()))", "f.uniqueness(of=f.each(mapF=ea.f.solution();" + "of=ea.f.all()))", "f.uniqueness(of=f.each(mapF=ea.f.quality();of=ea.f.all()))"
          }) List<Function<? super POCPopulationState<?, G, S, Q, ?>, ?>> defaultStateFunctions,
      @Param(value = "functions") List<Function<? super POCPopulationState<?, G, S, Q, ?>, ?>> stateFunctions,
      @Param(
          value = "defaultRunFunctions", dNPMs = {"ea.f.runKey(key = \"run.problem.name\")", "ea.f.runKey(key = \"run" + ".solver.name\")", "ea.f.runKey(key = " + "\"run.randomGenerator.seed\")"
          }) List<Function<? super Run<?, G, S, Q>, ?>> defaultRunFunctions,
      @Param("runFunctions") List<Function<? super Run<?, G, S, Q>, ?>> runFunctions,
      @Param(value = "deferred") boolean deferred,
      @Param(value = "onlyLast") boolean onlyLast,
      @Param(value = "runCondition", dNPM = "predicate.always()") Predicate<Run<?, G, S, Q>> runPredicate,
      @Param(value = "stateCondition", dNPM = "predicate.always()") Predicate<POCPopulationState<?, G, S, Q, ?>> statePredicate
  ) {
    return (experiment, executor) -> new ListenerFactoryAndMonitor<>(
        new CSVPrinter<>(
            Stream.of(defaultStateFunctions, stateFunctions)
                .flatMap(List::stream)
                .toList(),
            Stream.concat(defaultRunFunctions.stream(), runFunctions.stream())
                .map(f -> reformatToFit(f, experiment.runs()))
                .toList(),
            Utils.interpolate(path, experiment, null),
            errorString,
            intFormat,
            doubleFormat
        ),
        runPredicate,
        statePredicate,
        deferred ? executor : null,
        onlyLast
    );
  }

  @SuppressWarnings("unused")
  public static <G, S, Q> BiFunction<Experiment, Executor, ListenerFactory<POCPopulationState<?, G, S, Q, ?>, Run<?, G, S, Q>>> console(
      @Param(
          value = "defaultFunctions", dNPMs = {"ea.f.nOfIterations()", "ea.f.nOfEvals()", "ea.f.nOfBirths()", "ea.f" + ".elapsedSecs()", "f.size(of=ea.f.all())", "f.size(of=ea.f.firsts())", "f.size(of=ea.f.lasts())", "f" + ".uniqueness(of=f.each(mapF=ea.f.genotype();of=ea.f.all()))", "f.uniqueness(of=f.each(mapF=ea.f.solution();" + "of=ea.f.all()))", "f.uniqueness(of=f.each(mapF=ea.f.quality();of=ea.f.all()))"
          }) List<Function<? super POCPopulationState<?, G, S, Q, ?>, ?>> defaultStateFunctions,
      @Param(value = "functions") List<Function<? super POCPopulationState<?, G, S, Q, ?>, ?>> stateFunctions,
      @Param(
          value = "defaultRunFunctions", dNPMs = {"ea.f.runKey(key = \"run.problem.name\")", "ea.f.runKey(key = \"run" + ".solver.name\")", "ea.f.runKey(key = " + "\"run.randomGenerator.seed\")"
          }) List<Function<? super Run<?, G, S, Q>, ?>> defaultRunFunctions,
      @Param("runFunctions") List<Function<? super Run<?, G, S, Q>, ?>> runFunctions,
      @Param(value = "deferred") boolean deferred,
      @Param(value = "onlyLast") boolean onlyLast,
      @Param(value = "runCondition", dNPM = "predicate.always()") Predicate<Run<?, G, S, Q>> runPredicate,
      @Param(value = "stateCondition", dNPM = "predicate.always()") Predicate<POCPopulationState<?, G, S, Q, ?>> statePredicate,
      @Param("logExceptions") boolean logExceptions
  ) {
    return (experiment, executor) -> new ListenerFactoryAndMonitor<>(
        new TabularPrinter<>(
            Stream.of(defaultStateFunctions, stateFunctions)
                .flatMap(List::stream)
                .toList(),
            Stream.concat(defaultRunFunctions.stream(), runFunctions.stream())
                .map(f -> reformatToFit(f, experiment.runs()))
                .toList(),
            logExceptions
        ),
        runPredicate,
        statePredicate,
        deferred ? executor : null,
        onlyLast
    );
  }

  public static <G, S, Q> BiFunction<Experiment, Executor, ListenerFactory<POCPopulationState<?, G, S, Q, ?>, Run<?, G, S, Q>>> net(
      @Param(
          value = "defaultFunctions", dNPMs = {"ea.f.nOfIterations()", "ea.f.nOfEvals()", "ea.f.nOfBirths()", "ea.f" + ".elapsedSecs()", "f.size(of=ea.f.all())", "f.size(of=ea.f.firsts())", "f.size(of=ea.f.lasts())", "f" + ".uniqueness(of=f.each(mapF=ea.f.genotype();of=ea.f.all()))", "f.uniqueness(of=f.each(mapF=ea.f.solution();" + "of=ea.f.all()))", "f.uniqueness(of=f.each(mapF=ea.f.quality();of=ea.f.all()))"
          }) List<NamedFunction<? super POCPopulationState<?, G, S, Q, ?>, ?>> defaultStateFunctions,
      @Param(value = "functions") List<NamedFunction<? super POCPopulationState<?, G, S, Q, ?>, ?>> stateFunctions,
      @Param(
          value = "defaultRunFunctions", dNPMs = {"ea.f.runKey(key = \"run.problem.name\")", "ea.f.runKey(key = \"run" + ".solver.name\")", "ea.f.runKey(key = " + "\"run.randomGenerator.seed\")"
          }) List<Function<? super Run<?, G, S, Q>, ?>> defaultRunFunctions,
      @Param("runFunctions") List<Function<? super Run<?, G, S, Q>, ?>> runFunctions,
      @Param(value = "serverAddress", dS = "127.0.0.1") String serverAddress,
      @Param(value = "serverPort", dI = 10979) int serverPort,
      @Param(value = "serverKeyFilePath") String serverKeyFilePath,
      @Param(value = "pollInterval", dD = 1) double pollInterval,
      @Param(value = "runCondition", dNPM = "predicate.always()") Predicate<Run<?, G, S, Q>> runPredicate,
      @Param(value = "stateCondition", dNPM = "predicate.always()") Predicate<POCPopulationState<?, G, S, Q, ?>> statePredicate
  ) {
    NetMultiSink netMultiSink = new NetMultiSink(
        pollInterval,
        serverAddress,
        serverPort,
        new File(serverKeyFilePath)
    );
    return (experiment, executor) -> new ListenerFactoryAndMonitor<>(
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
            netMultiSink.getDatItemSink()
        ),
        runPredicate,
        statePredicate,
        executor,
        false
    );
  }

  @Alias(
      name = "saveForExp", passThroughParams = {@PassThroughParam(name = "path", type = ParamMap.Type.STRING, value = "{experiment.name}"), @PassThroughParam(name = "processor", type = ParamMap.Type.NAMED_PARAM_MAP), @PassThroughParam(name = "overwrite", type = ParamMap.Type.BOOLEAN, value = "false")
      }, value = // spotless:off
      """
          onExpDone(
            preprocessor = $processor;
            consumers = [ea.c.saver(path = $path; overwrite = $overwrite)]
          )
          """ // spotless:on
  )
  @Alias(
      name = "savePlotForExp", passThroughParams = {@PassThroughParam(name = "plot", type = ParamMap.Type.NAMED_PARAM_MAP), @PassThroughParam(name = "secondary", type = ParamMap.Type.BOOLEAN, value = "false"), @PassThroughParam(name = "type", type = ParamMap.Type.STRING, value = "png"), @PassThroughParam(name = "configuration", type = Type.NAMED_PARAM_MAP, value = "ea.plot.configuration.image()")
      }, value = // spotless:off
      """
          saveForExp(
            of = $plot;
            processor = ea.f.imagePlotter(secondary = $secondary; type = $type; configuration = $configuration)
          )
          """ // spotless:on
  )
  @Alias(
      name = "savePlotAndCsvForExp", passThroughParams = {@PassThroughParam(name = "secondary", type = ParamMap.Type.BOOLEAN, value = "false"), @PassThroughParam(name = "overwrite", type = ParamMap.Type.BOOLEAN, value = "false"), @PassThroughParam(name = "path", type = ParamMap.Type.STRING, value = "{experiment.name}"), @PassThroughParam(name = "plot", type = ParamMap.Type.NAMED_PARAM_MAP), @PassThroughParam(name = "type", type = ParamMap.Type.STRING, value = "png"), @PassThroughParam(name = "configuration", type = Type.NAMED_PARAM_MAP, value = "ea.plot.configuration.image()")
      }, value = // spotless:off
      """
          onExpDone(
            of = $plot;
            consumers = [
              ea.c.saver(path = $path; overwrite = $overwrite; of = ea.f.imagePlotter(secondary = $secondary; type = $type; configuration = $configuration));
              ea.c.saver(path = $path; overwrite = $overwrite; of = ea.f.csvPlotter(); suffix = ".tsv")
            ]
          )
          """ // spotless:on
  )
  @SuppressWarnings("unused")
  public static <E, O, P, R, EX> BiFunction<EX, Executor, ListenerFactory<E, R>> onExpDone(
      @Param("of") AccumulatorFactory<E, O, R> accumulatorFactory,
      @Param(value = "preprocessor", dNPM = "f.identity()") Function<? super O, ? extends P> preprocessor,
      @Param(
          value = "consumers", dNPMs = {"ea.consumer.deaf()"}) List<TriConsumer<? super P, R, EX>> consumers,
      @Param(value = "deferred") boolean deferred,
      @Param(value = "eCondition", dNPM = "predicate.always()") Predicate<E> ePredicate,
      @Param(value = "rCondition", dNPM = "predicate.always()") Predicate<R> rPredicate

  ) {
    return (experiment, executor) -> new ListenerFactoryAndMonitor<>(
        accumulatorFactory.thenOnShutdown(
            Naming.named(
                consumers.toString(),
                (Consumer<O>) (o -> {
                  if (o != null) {
                    P p = preprocessor.apply(o);
                    consumers.forEach(c -> c.accept(p, null, experiment));
                  }
                })
            )
        ),
        rPredicate,
        ePredicate,
        deferred ? executor : null,
        false
    );
  }

  @Alias(
      name = "saveForRun", passThroughParams = {@PassThroughParam(name = "path", type = ParamMap.Type.STRING, value = "run-{run.index:%04d}"), @PassThroughParam(name = "overwrite", type = ParamMap.Type.BOOLEAN, value = "false"), @PassThroughParam(name = "processor", type = ParamMap.Type.NAMED_PARAM_MAP)
      }, value = // spotless:off
      """
          onRunDone(
            preprocessor = $processor;
            consumers = [ea.c.saver(path = $path; overwrite = $overwrite)]
          )
          """ // spotless:on
  )
  @Alias(
      name = "savePlotForRun", passThroughParams = {@PassThroughParam(name = "secondary", type = ParamMap.Type.BOOLEAN, value = "false"), @PassThroughParam(name = "plot", type = ParamMap.Type.NAMED_PARAM_MAP), @PassThroughParam(name = "type", type = ParamMap.Type.STRING, value = "png"), @PassThroughParam(name = "configuration", type = Type.NAMED_PARAM_MAP, value = "ea.plot.configuration.image()")
      }, value = // spotless:off
      """
          saveForRun(
            of = $plot;
            processor = ea.f.imagePlotter(secondary = $secondary; type = $type; configuration = $configuration)
          )
          """ // spotless:on
  )
  @Alias(
      name = "saveLastPopulationForRun", value = // spotless:off
      """
          saveForRun(
            of = ea.acc.lastPopulationMap();
            path = "run-{run.index:%04d}-last-pop";
            processor = f.identity()
          )
          """) // spotless:on
  @Alias(
      name = "savePlotAndCsvForRun", passThroughParams = {@PassThroughParam(name = "secondary", type = ParamMap.Type.BOOLEAN, value = "false"), @PassThroughParam(name = "overwrite", type = ParamMap.Type.BOOLEAN, value = "false"), @PassThroughParam(name = "path", type = ParamMap.Type.STRING, value = "{experiment.name}"), @PassThroughParam(name = "plot", type = ParamMap.Type.NAMED_PARAM_MAP), @PassThroughParam(name = "type", type = ParamMap.Type.STRING, value = "png"), @PassThroughParam(name = "configuration", type = Type.NAMED_PARAM_MAP, value = "ea.plot.configuration.image()")
      }, value = // spotless:off
      """
          onRunDone(
            of = $plot;
            consumers = [
              ea.c.saver(path = $path; overwrite = $overwrite; of = ea.f.imagePlotter(secondary = $secondary; type = $type; configuration = $configuration));
              ea.c.saver(path = $path; overwrite = $overwrite; of = ea.f.csvPlotter(); suffix = ".tsv")
            ]
          )
          """ // spotless:on
  )
  @SuppressWarnings("unused")
  public static <E, O, P, R, EX> BiFunction<EX, Executor, ListenerFactory<E, R>> onRunDone(
      @Param("of") AccumulatorFactory<E, O, R> accumulatorFactory,
      @Param(value = "preprocessor", dNPM = "f.identity()") Function<? super O, ? extends P> preprocessor,
      @Param(
          value = "consumers", dNPMs = {"ea.consumer.deaf()"}) List<TriConsumer<? super P, R, EX>> consumers,
      @Param(value = "deferred") boolean deferred,
      @Param(value = "eCondition", dNPM = "predicate.always()") Predicate<E> ePredicate,
      @Param(value = "rCondition", dNPM = "predicate.always()") Predicate<R> rPredicate
  ) {
    return (experiment, executor) -> new ListenerFactoryAndMonitor<>(
        accumulatorFactory.thenOnDone(
            Naming.named(
                consumers.toString(),
                (run, a) -> {
                  P p = preprocessor.apply(a.get());
                  consumers.forEach(c -> c.accept(p, run, experiment));
                }
            )
        ),
        rPredicate,
        ePredicate,
        deferred ? executor : null,
        false
    );
  }

  private static <T, R> Function<T, R> reformatToFit(Function<T, R> f, Collection<?> ts) {
    //noinspection unchecked
    return FormattedFunction.from(f)
        .reformattedToFit(ts.stream().map(t -> (T) t).toList());
  }

  @SuppressWarnings("unused")
  public static <G, S, Q> BiFunction<Experiment, Executor, ListenerFactory<POCPopulationState<?, G, S, Q, ?>, Run<?, G, S, Q>>> tui(
      @Param(
          value = "defaultFunctions", dNPMs = {"ea.f.nOfIterations()", "ea.f.nOfEvals()", "ea.f.nOfBirths()", "ea.f" + ".elapsedSecs()", "f.size(of=ea.f.all())", "f.size(of=ea.f.firsts())", "f.size(of=ea.f.lasts())", "f" + ".uniqueness(of=f.each(mapF=ea.f.genotype();of=ea.f.all()))", "f.uniqueness(of=f.each(mapF=ea.f.solution();" + "of=ea.f.all()))", "f.uniqueness(of=f.each(mapF=ea.f.quality();of=ea.f.all()))"
          }) List<NamedFunction<? super POCPopulationState<?, G, S, Q, ?>, ?>> defaultStateFunctions,
      @Param(value = "functions") List<NamedFunction<? super POCPopulationState<?, G, S, Q, ?>, ?>> stateFunctions,
      @Param(
          value = "defaultRunFunctions", dNPMs = {"ea.f.runKey(key = \"run.problem.name\")", "ea.f.runKey(key = \"run" + ".solver.name\")", "ea.f.runKey(key = " + "\"run.randomGenerator.seed\")"
          }) List<Function<? super Run<?, G, S, Q>, ?>> defaultRunFunctions,
      @Param("runFunctions") List<Function<? super Run<?, G, S, Q>, ?>> runFunctions,
      @Param(value = "runCondition", dNPM = "predicate.always()") Predicate<Run<?, G, S, Q>> runPredicate,
      @Param(value = "stateCondition", dNPM = "predicate.always()") Predicate<POCPopulationState<?, G, S, Q, ?>> statePredicate
  ) {
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
        dataItemSinkSource
    )
        .run();
    return (experiment, executor) -> new ListenerFactoryAndMonitor<>(
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
            dataItemSinkSource
        ),
        runPredicate,
        statePredicate,
        executor,
        false
    );
  }
}
