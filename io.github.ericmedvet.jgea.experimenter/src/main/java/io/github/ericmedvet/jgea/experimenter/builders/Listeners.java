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

package io.github.ericmedvet.jgea.experimenter.builders;

import io.github.ericmedvet.jgea.core.solver.POCPopulationState;
import io.github.ericmedvet.jgea.core.util.Progress;
import io.github.ericmedvet.jgea.experimenter.Run;
import io.github.ericmedvet.jgea.experimenter.listener.ProgressMonitor;
import io.github.ericmedvet.jgea.experimenter.listener.decoupled.*;
import io.github.ericmedvet.jgea.experimenter.listener.net.NetMultiSink;
import io.github.ericmedvet.jnb.core.*;
import io.github.ericmedvet.jnb.core.ParamMap.Type;
import io.github.ericmedvet.jnb.datastructure.Listener;
import io.github.ericmedvet.jnb.datastructure.ListenerFactory;
import io.github.ericmedvet.jnb.datastructure.NamedFunction;
import io.github.ericmedvet.jnb.datastructure.Utils;
import java.io.File;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.Predicate;

@Discoverable(prefixTemplate = "ea.listener|l")
@Alias(
    name = "saveForExp", passThroughParams = {@PassThroughParam(name = "path", type = ParamMap.Type.STRING), @PassThroughParam(name = "processor", type = ParamMap.Type.NAMED_PARAM_MAP), @PassThroughParam(name = "overwrite", type = ParamMap.Type.BOOLEAN, value = "false"), @PassThroughParam(name = "verbose", type = ParamMap.Type.BOOLEAN, value = "false")
    }, value = // spotless:off
    """
        listener.onDone(
          preprocessor = $processor;
          consumers = [consumer.saver(path = $path; overwrite = $overwrite; verbose = $verbose)]
        )
        """ // spotless:on
)
@Alias(
    name = "savePlotForExp", passThroughParams = {@PassThroughParam(name = "plot", type = ParamMap.Type.NAMED_PARAM_MAP), @PassThroughParam(name = "secondary", type = ParamMap.Type.BOOLEAN, value = "false"), @PassThroughParam(name = "type", type = ParamMap.Type.STRING, value = "png"), @PassThroughParam(name = "configuration", type = Type.NAMED_PARAM_MAP, value = "viz.plot.configuration.image()")
    }, value = // spotless:off
    """
        ea.listener.saveForExp(
          of = $plot;
          processor = viz.f.imagePlotter(secondary = $secondary; type = $type; configuration = $configuration)
        )
        """ // spotless:on
)
@Alias(
    name = "savePlotAndCsvForExp", passThroughParams = {@PassThroughParam(name = "secondary", type = ParamMap.Type.BOOLEAN, value = "false"), @PassThroughParam(name = "overwrite", type = ParamMap.Type.BOOLEAN, value = "false"), @PassThroughParam(name = "path", type = ParamMap.Type.STRING), @PassThroughParam(name = "plot", type = ParamMap.Type.NAMED_PARAM_MAP), @PassThroughParam(name = "type", type = ParamMap.Type.STRING, value = "png"), @PassThroughParam(name = "configuration", type = Type.NAMED_PARAM_MAP, value = "viz.plot.configuration.image()")
    }, value = // spotless:off
    """
        listener.onDone(
          of = $plot;
          consumers = [
            consumer.saver(path = $path; overwrite = $overwrite; of = viz.f.imagePlotter(secondary = $secondary; type = $type; configuration = $configuration));
            consumer.saver(path = $path; overwrite = $overwrite; of = viz.f.csvPlotter(); suffix = ".tsv")
          ]
        )
        """ // spotless:on
)
@Alias(
    name = "saveForRun", passThroughParams = {@PassThroughParam(name = "path", type = ParamMap.Type.STRING), @PassThroughParam(name = "overwrite", type = ParamMap.Type.BOOLEAN, value = "false"), @PassThroughParam(name = "processor", type = ParamMap.Type.NAMED_PARAM_MAP), @PassThroughParam(name = "verbose", type = ParamMap.Type.BOOLEAN, value = "false")
    }, value = // spotless:off
    """
        listener.onKDone(
          preprocessor = $processor;
          consumers = [consumer.saver(path = $path; overwrite = $overwrite; verbose = $verbose)]
        )
        """ // spotless:on
)
@Alias(
    name = "savePlotForRun", passThroughParams = {@PassThroughParam(name = "secondary", type = ParamMap.Type.BOOLEAN, value = "false"), @PassThroughParam(name = "plot", type = ParamMap.Type.NAMED_PARAM_MAP), @PassThroughParam(name = "type", type = ParamMap.Type.STRING, value = "png"), @PassThroughParam(name = "configuration", type = Type.NAMED_PARAM_MAP, value = "viz.plot.configuration.image()")
    }, value = // spotless:off
    """
        ea.listener.saveForRun(
          of = $plot;
          processor = viz.f.imagePlotter(secondary = $secondary; type = $type; configuration = $configuration)
        )
        """ // spotless:on
)
@Alias(
    name = "saveLastPopulationForRun", value = // spotless:off
    """
        ea.listener.saveForRun(
          of = ea.acc.lastPopulationMap();
          path = "run-{index:%04d}-last-pop";
          processor = f.identity()
        )
        """) // spotless:on
@Alias(
    name = "savePlotAndCsvForRun", passThroughParams = {@PassThroughParam(name = "secondary", type = ParamMap.Type.BOOLEAN, value = "false"), @PassThroughParam(name = "overwrite", type = ParamMap.Type.BOOLEAN, value = "false"), @PassThroughParam(name = "path", type = ParamMap.Type.STRING), @PassThroughParam(name = "plot", type = ParamMap.Type.NAMED_PARAM_MAP), @PassThroughParam(name = "type", type = ParamMap.Type.STRING, value = "png"), @PassThroughParam(name = "configuration", type = Type.NAMED_PARAM_MAP, value = "viz.plot.configuration.image()"), @PassThroughParam(name = "verbose", type = ParamMap.Type.BOOLEAN, value = "false")
    }, value = // spotless:off
    """
        listener.onKDone(
          of = $plot;
          consumers = [
            consumer.saver(path = $path; overwrite = $overwrite; of = viz.f.imagePlotter(secondary = $secondary; type = $type; configuration = $configuration); verbose = $verbose);
            consumer.saver(path = $path; overwrite = $overwrite; of = viz.f.csvPlotter(); suffix = ".tsv"; verbose = $verbose)
          ]
        )
        """ // spotless:on
)
@Alias(
    name = "console", value = // spotless:off
    """       
       listener.console(
         defaultEFunctions = [
           ea.f.nOfIterations();
           ea.f.nOfEvals();
           ea.f.nOfBirths();
           ea.f.elapsedSecs();
           f.size(of = ea.f.all());
           f.size(of = ea.f.firsts());
           f.size(of = ea.f.lasts());
           f.uniqueness(of = f.each(mapF = ea.f.genotype(); of = ea.f.all()));
           f.uniqueness(of = f.each(mapF = ea.f.solution(); of = ea.f.all()));
           f.uniqueness(of = f.each(mapF = ea.f.quality(); of = ea.f.all()))
         ];
         defaultKFunctions = [
           ea.f.globalProgress();
           f.mappableKey(key = "problem.name");
           f.mappableKey(key = "solver.name");
           f.mappableKey(key = "randomGenerator.seed")
         ]
       )
       """ // spotless:on
)
@Alias(
    name = "csv", value = // spotless:off
    """
       listener.csv(
         defaultEFunctions = [
           ea.f.nOfIterations();
           ea.f.nOfEvals();
           ea.f.nOfBirths();
           ea.f.elapsedSecs();
           f.size(of = ea.f.all());
           f.size(of = ea.f.firsts());
           f.size(of = ea.f.lasts());
           f.uniqueness(of = f.each(mapF = ea.f.genotype(); of = ea.f.all()));
           f.uniqueness(of = f.each(mapF = ea.f.solution(); of = ea.f.all()));
           f.uniqueness(of = f.each(mapF = ea.f.quality(); of = ea.f.all()));
           ea.f.id(of = ea.f.best())
         ];
         defaultKFunctions = [
           f.mappableKey(key = "problem.name");
           f.mappableKey(key = "solver.name");
           f.mappableKey(key = "randomGenerator.seed")
         ]
       )
       """ // spotless:on
)
@Alias( // spotless:off
    name = "individualCsv",
    passThroughParams = {
        @PassThroughParam(name = "splitterF", type = Type.NAMED_PARAM_MAP),
        @PassThroughParam(name = "defaultOEFunctions", type = Type.NAMED_PARAM_MAPS, value = """
            [
              ea.f.nOfIterations();
              ea.f.nOfEvals();
              ea.f.nOfBirths();
              ea.f.elapsedSecs()
            ]
            """),
        @PassThroughParam(name = "defaultIEFunctions", type = Type.NAMED_PARAM_MAPS, value = """
            [
              ea.f.id()
            ]
            """),
        @PassThroughParam(name = "oeFunctions", type = Type.NAMED_PARAM_MAPS, value = "[]"),
        @PassThroughParam(name = "ieFunctions", type = Type.NAMED_PARAM_MAPS, value = "[]"),
        @PassThroughParam(name = "kFunctions", type = Type.NAMED_PARAM_MAPS, value = "[]"),
        @PassThroughParam(name = "path", type = Type.STRING),
        @PassThroughParam(name = "errorString", type = Type.STRING, value = "NA"),
        @PassThroughParam(name = "intFormat", type = Type.STRING, value = "%d"),
        @PassThroughParam(name = "doubleFormat", type = Type.STRING, value = "%.5e")
    },
    value =
    """
       listener.splitPair(
         splitter = $splitterF;
         inner = listener.csv(
           defaultEFunctions =
             (then = $defaultOEFunctions) * [f.composition(of = f.pairFirst(name = "identity"))] +
             (then = $defaultIEFunctions) * [f.composition(of = f.pairSecond(name = "identity"))];
           defaultKFunctions = [
             f.mappableKey(key = "problem.name");
             f.mappableKey(key = "solver.name");
             f.mappableKey(key = "randomGenerator.seed")
           ];
           eFunctions =
             (then = $oeFunctions) * [f.composition(of = f.pairFirst(name = "identity"))] +
             (then = $ieFunctions) * [f.composition(of = f.pairSecond(name = "identity"))];
           kFunctions = $kFunctions;
           path = $path;
           errorString = $errorString;
           intFormat = $intFormat;
           doubleFormat = $doubleFormat
         )
       )
       """ // spotless:on
)
@Alias(name = "allCsv", value = "ea.listener.individualCsv(splitterF = ea.f.all())")
@Alias(name = "firstsCsv", value = "ea.listener.individualCsv(splitterF = ea.f.firsts())")
public class Listeners {

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

  public static <G, S, Q> Function<Executor, ListenerFactory<POCPopulationState<?, G, S, Q, ?>, Run<?, G, S, Q>>> net(
      @Param(
          value = "defaultFunctions", dNPMs = {"ea.f.nOfIterations()", "ea.f.nOfEvals()", "ea.f.nOfBirths()", "ea.f" + ".elapsedSecs()", "f.size(of=ea.f.all())", "f.size(of=ea.f.firsts())", "f.size(of=ea.f.lasts())", "f" + ".uniqueness(of=f.each(mapF=ea.f.genotype();of=ea.f.all()))", "f.uniqueness(of=f.each(mapF=ea.f.solution();" + "of=ea.f.all()))", "f.uniqueness(of=f.each(mapF=ea.f.quality();of=ea.f.all()))"
          }) List<NamedFunction<? super POCPopulationState<?, G, S, Q, ?>, ?>> defaultStateFunctions,
      @Param(value = "functions") List<NamedFunction<? super POCPopulationState<?, G, S, Q, ?>, ?>> stateFunctions,
      @Param(
          value = "defaultRunFunctions", dNPMs = {"f.mappableKey(key = \"problem.name\")", "f.mappableKey(key = \"solver.name\")", "f.mappableKey(key = \"randomGenerator.seed\")"
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
    return executor -> new ListenerFactoryAndMonitor<>(
        new SinkListenerFactory<>(
            Utils.concat(defaultStateFunctions, stateFunctions),
            Utils.concat(defaultRunFunctions, runFunctions),
            null, // TODO circumvent this, as experiment is not available anymore
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

  @SuppressWarnings("unused")
  public static <G, S, Q> Function<Executor, ListenerFactory<POCPopulationState<?, G, S, Q, ?>, Run<?, G, S, Q>>> tui(
      @Param(
          value = "defaultFunctions", dNPMs = {"ea.f.nOfIterations()", "ea.f.nOfEvals()", "ea.f.nOfBirths()", "ea.f" + ".elapsedSecs()", "f.size(of=ea.f.all())", "f.size(of=ea.f.firsts())", "f.size(of=ea.f.lasts())", "f" + ".uniqueness(of=f.each(mapF=ea.f.genotype();of=ea.f.all()))", "f.uniqueness(of=f.each(mapF=ea.f.solution();" + "of=ea.f.all()))", "f.uniqueness(of=f.each(mapF=ea.f.quality();of=ea.f.all()))"
          }) List<NamedFunction<? super POCPopulationState<?, G, S, Q, ?>, ?>> defaultStateFunctions,
      @Param(value = "functions") List<NamedFunction<? super POCPopulationState<?, G, S, Q, ?>, ?>> stateFunctions,
      @Param(
          value = "defaultRunFunctions", dNPMs = {"f.mappableKey(key = \"problem.name\")", "f.mappableKey(key = \"solver.name\")", "f.mappableKey(key = \"randomGenerator.seed\")"
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
    return executor -> new ListenerFactoryAndMonitor<>(
        new SinkListenerFactory<>(
            Utils.concat(defaultStateFunctions, stateFunctions),
            Utils.concat(defaultRunFunctions, runFunctions),
            null, // TODO circumvent this, as experiment is not available anymore
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