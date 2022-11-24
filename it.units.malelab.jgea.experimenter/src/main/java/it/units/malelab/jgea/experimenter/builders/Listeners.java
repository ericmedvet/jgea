package it.units.malelab.jgea.experimenter.builders;

import it.units.malelab.jgea.core.listener.*;
import it.units.malelab.jgea.core.solver.Individual;
import it.units.malelab.jgea.core.solver.state.POSetPopulationState;
import it.units.malelab.jgea.core.util.ImagePlotters;
import it.units.malelab.jgea.experimenter.Experiment;
import it.units.malelab.jgea.experimenter.Run;
import it.units.malelab.jgea.telegram.TelegramUpdater;
import it.units.malelab.jnb.core.NamedParamMap;
import it.units.malelab.jnb.core.Param;
import it.units.malelab.jnb.core.ParamMap;
import it.units.malelab.jnb.core.StringNamedParamMap;

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

import static it.units.malelab.jgea.core.listener.NamedFunctions.*;

public class Listeners {

  public final static List<NamedFunction<? super POSetPopulationState<?, ?, ?>, ?>> BASIC_FUNCTIONS = List.of(
      iterations(),
      births(),
      fitnessEvaluations(),
      elapsedSeconds()
  );
  private final static Logger L = Logger.getLogger(Listeners.class.getName());

  private Listeners() {
  }

  @SuppressWarnings("unused")
  public static <G, S, Q> BiFunction<Experiment, ExecutorService, ListenerFactory<?
      super POSetPopulationState<G,
      S, Q>, Run<?, G, S, Q>>> allCsv(
      @Param("filePath") String filePath,
      @Param("individualFunctions") List<NamedFunction<? super Individual<? extends G, ? extends S, ? extends Q>, ?>> individualFunctions,
      @Param("runKeys") List<String> runKeys,
      @Param(value = "onlyLast", dB = false) boolean onlyLast
  ) {
    @SuppressWarnings({"rawtypes", "unchecked"}) List<NamedFunction<POSetPopulationState<G, S, Q>, ?>> popFunctions =
        new ArrayList<>(
            (List) BASIC_FUNCTIONS);
    List<NamedFunction<Run<?, G, S, Q>, Object>> runFunctions = runKeys.stream()
        .map(k -> NamedFunction.build(
            k,
            "%s",
            (Run<?, G, S, Q> run) -> getKeyFromParamMap(run.map(), Arrays.stream(k.split("\\.")).toList())
        ))
        .toList();
    record PopIndividualPair<G, S, Q>(POSetPopulationState<G, S, Q> pop, Individual<G, S, Q> individual) {}
    List<NamedFunction<? super PopIndividualPair<G, S, Q>, ?>> pairFunctions = new ArrayList<>();
    popFunctions.stream()
        .map(f -> NamedFunction.build(
            f.getName(),
            f.getFormat(),
            (PopIndividualPair<G, S, Q> pair) -> f.apply(pair.pop())
        ))
        .forEach(pairFunctions::add);
    individualFunctions.stream()
        .map(f -> NamedFunction.build(
            f.getName(),
            f.getFormat(),
            (PopIndividualPair<G, S, Q> pair) -> f.apply(pair.individual())
        ))
        .forEach(pairFunctions::add);
    @SuppressWarnings({"unchecked", "rawtypes"}) CSVPrinter<PopIndividualPair<G, S, Q>, Run<?, G, S, Q>> innerListenerFactory =
        new CSVPrinter<>(
            (List) Collections.unmodifiableList(pairFunctions),
            (List) runFunctions,
            new File(filePath)
        );
    return (experiment, executorService) -> {
      ListenerFactory<? super POSetPopulationState<G, S, Q>, Run<?, G, S, Q>> listenerFactory =
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
      if (onlyLast) {
        listenerFactory = listenerFactory.onLast();
      }
      return listenerFactory;
    };
  }

  @SuppressWarnings("unused")
  public static <G, S, Q> BiFunction<Experiment, ExecutorService, ListenerFactory<?
      super POSetPopulationState<G,
      S, Q>, Run<?, G, S, Q>>> bestCsv(
      @Param("filePath") String filePath,
      @Param("popFunctions") List<NamedFunction<? super POSetPopulationState<? extends G, ? extends S, ? extends Q>,
          ?>> popFunctions,
      @Param("individualFunctions") List<NamedFunction<? super Individual<? extends G, ? extends S, ? extends Q>, ?>> individualFunctions,
      @Param("runKeys") List<String> runKeys,
      @Param(value = "onlyLast", dB = false) boolean onlyLast
  ) {
    NamedFunction<POSetPopulationState<? extends G, ? extends S, ? extends Q>, Individual<? extends G, ? extends S, ?
        extends Q>> best = best();
    List<NamedFunction<? super POSetPopulationState<? extends G, ? extends S, ? extends Q>, ?>> functions =
        new ArrayList<>(
            BASIC_FUNCTIONS);
    functions.addAll(popFunctions);
    functions.addAll(best.then(individualFunctions));
    List<NamedFunction<Run<?, G, S, Q>, Object>> runFunctions = runKeys.stream()
        .map(k -> NamedFunction.build(
            k,
            "%s",
            (Run<?, G, S, Q> run) -> getKeyFromParamMap(run.map(), Arrays.stream(k.split("\\.")).toList())
        ))
        .toList();
    return (experiment, executorService) -> {
      // functions.add(best.then(fitness()).then(experiment.qExtractor())); //TODO
      //noinspection unchecked,rawtypes
      ListenerFactory<? super POSetPopulationState<G, S, Q>, Run<?, G, S, Q>> listenerFactory =
          new CSVPrinter<POSetPopulationState<G, S, Q>, Run<?, G, S, Q>>(
              Collections.unmodifiableList(functions),
              (List) runFunctions,
              new File(filePath)
          );
      if (onlyLast) {
        listenerFactory = listenerFactory.onLast();
      }
      return listenerFactory;
    };
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

  @SuppressWarnings("unused")
  public static <G, S, Q> BiFunction<Experiment, ExecutorService, ListenerFactory<?
      super POSetPopulationState<G,
      S, Q>, Run<?, G, S, Q>>> telegram(
      @Param("chatId") String chatId,
      @Param("botIdFilePath") String botIdFilePath,
      @Param(value = "deferred", dB = true) boolean deferred
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
      throw new IllegalArgumentException(String.format(
          "Cannot read telegram credentials at %s: %s",
          botIdFilePath,
          e
      ));
    }
    try {
      longChatId = Long.parseLong(chatId);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Invalid chatId %s: not a number".formatted(chatId));
    }
    return (experiment, executorService) -> {
      List<AccumulatorFactory<POSetPopulationState<G, S, Q>, ?, Run<?, G, S, Q>>> accumulators = new ArrayList<>();
      //prepare text accumulator
      accumulators.add(run -> new Accumulator<>() {
        @Override
        public String get() {
          return StringNamedParamMap.prettyToString(run.map(), 40);
        }

        @Override
        public void listen(POSetPopulationState<G, S, Q> state) {
        }
      });
      //prepare plotter accumulator
      accumulators.add(new TableBuilder<POSetPopulationState<G, S, Q>, Number, Run<?, G, S, Q>>(
          List.of(
              iterations(),
              iterations()/*,
              best().then(fitness()).then(qFunction),
              min(Double::compare).of(each(qFunction.of(fitness()))).of(all()),
              median(Double::compare).of(each(qFunction.of(fitness()))).of(all())*/ // TODO make plots a parameter
          ),
          List.of()
      ).then(t -> ImagePlotters.xyLines(100, 100).apply(t))); // TODO update
      //prepare listener
      TelegramUpdater<? super POSetPopulationState<G, S, Q>, Run<?, G, S, Q>> telegramUpdater = new TelegramUpdater<>(
          accumulators,
          botId,
          longChatId
      );
      L.info("Will send updates to Telegram chat `%s`".formatted(telegramUpdater.getChatInfo()));
      ListenerFactory<? super POSetPopulationState<G, S, Q>, Run<?, G, S, Q>> listenerFactory = telegramUpdater;
      if (deferred) {
        listenerFactory = listenerFactory.deferred(executorService);
      }
      return listenerFactory;
    };
  }

}
