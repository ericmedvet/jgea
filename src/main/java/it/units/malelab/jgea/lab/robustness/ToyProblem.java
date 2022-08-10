package it.units.malelab.jgea.lab.robustness;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Range;
import it.units.malelab.jgea.Worker;
import it.units.malelab.jgea.core.TotalOrderQualityBasedProblem;
import it.units.malelab.jgea.core.listener.*;
import it.units.malelab.jgea.core.listener.telegram.TelegramProgressMonitor;
import it.units.malelab.jgea.core.selector.Last;
import it.units.malelab.jgea.core.selector.Tournament;
import it.units.malelab.jgea.core.solver.*;
import it.units.malelab.jgea.core.solver.coevolution.CollaboratorSelector;
import it.units.malelab.jgea.core.solver.coevolution.CooperativeSolver;
import it.units.malelab.jgea.core.solver.coevolution.QualityAggregator;
import it.units.malelab.jgea.core.solver.state.POSetPopulationState;
import it.units.malelab.jgea.problem.synthetic.BimodalPointAiming;
import it.units.malelab.jgea.problem.synthetic.PointAiming;
import it.units.malelab.jgea.representation.sequence.FixedLengthListFactory;
import it.units.malelab.jgea.representation.sequence.numeric.GaussianMutation;
import it.units.malelab.jgea.representation.sequence.numeric.GeometricCrossover;
import it.units.malelab.jgea.representation.sequence.numeric.UniformDoubleFactory;

import java.io.File;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static it.units.malelab.jgea.core.listener.NamedFunctions.*;
import static it.units.malelab.jgea.core.util.Args.*;

public class ToyProblem extends Worker {

  public static void main(String[] args) {
    new ToyProblem(args);
  }

  public ToyProblem(String[] args) {
    super(args);
  }

  private static final String TOKEN_SEPARATOR = ";";
  private static final String PARAM_VALUE_SEPARATOR = "=";
  private static final String NAME_KEY = "NAME";

  @Override
  public void run() {
    String telegramBotId = a("telegramBotId", null);
    long telegramChatId = Long.parseLong(a("telegramChatId", "0"));

    int problemSize = i(a("size", "10"));
    int nPop = i(a("nPop", "25"));
    int nEvals = i(a("nEvals", "100000"));
    int nTournament = 5;

    List<String> eas = l(a("eas", "es,ga"));
    List<String> selectors = l(a("sel", "f0.1,f0.25,f0.5,f0.75,l0.1,l0.25,l0.5,l0.75,c"));
    List<String> aggregators = l(a("aggr", "f,l,m"));

    List<String> coopCoEvoParams = buildCoEvoParams(
        String.format("size=%d;nEvals=%d;nPop=%d;nTour=%d", problemSize, nEvals, nPop, nTournament),
        eas, selectors, aggregators
    );

    int[] seeds = ri(a("seed", "0:10"));
    boolean output = a("output", "true").startsWith("t");
    String bestFile = a("bestFile", "best.txt");
    String lastFile = a("lastFile", "last.txt");

    List<TotalOrderQualityBasedProblem<List<Double>, Double>> toyProblems = List.of(
        new PointAiming(),
        new BimodalPointAiming()
    );

    //consumers
    List<NamedFunction<? super POSetPopulationState<?, ?, ? extends Double>, ?>> functions = List.of(
        iterations(),
        fitnessEvaluations(),
        births(),
        elapsedSeconds(),
        size().of(all()),
        size().of(firsts()),
        size().of(lasts()),
        uniqueness().of(each(genotype())).of(all()),
        uniqueness().of(each(solution())).of(all()),
        uniqueness().of(each(fitness())).of(all()),
        size().of(genotype()).of(best()),
        size().of(solution()).of(best()),
        fitnessMappingIteration().of(best()),
        fitness().reformat("%5.3f").of(best()),
        hist(8).of(each(fitness())).of(all()),
        solution().reformat("%30.30s").of(best())
    );
    List<NamedFunction<? super Map<String, Object>, ?>> kFunctions = List.of(
        attribute("seed").reformat("%2d"),
        attribute("problem"),
        attribute("evolver").reformat("%20.20s")
    );


    List<ListenerFactory<POSetPopulationState<?, ?, ? extends Double>, Map<String, Object>>> listenerFactories =
        new ArrayList<>();
    if (bestFile == null || output) {
      listenerFactories.add(new TabularPrinter<>(functions, kFunctions));
    }
    if (bestFile != null) {
      listenerFactories.add(new CSVPrinter<>(functions, kFunctions, new File(bestFile)));
    }
    if (lastFile != null) {
      listenerFactories.add(new CSVPrinter<>(functions, kFunctions, new File(lastFile)).onLast());
    }

    ListenerFactory<POSetPopulationState<?, ?, ? extends Double>, Map<String, Object>> listenerFactory =
        ListenerFactory.all(listenerFactories);

    ProgressMonitor progressMonitor = new ScreenProgressMonitor(System.out);
    if (telegramBotId != null && telegramChatId != 0) {
      progressMonitor = progressMonitor.and(new TelegramProgressMonitor(telegramBotId, telegramChatId));
    }

    //evolvers
    Map<String, IterativeSolver<? extends POSetPopulationState<?, List<Double>, Double>,
        TotalOrderQualityBasedProblem<List<Double>, Double>, List<Double>>> solvers = new TreeMap<>();

    solvers.put(String.format("ga;size=%d;nEvals=%d;nPop=%d;nTour=%d", problemSize, nEvals, nPop, nTournament),
        buildGA(problemSize, nPop, nEvals, nTournament));
    solvers.put(String.format("es;size=%d;nEvals=%d;nPop=%d", problemSize, nEvals, nPop),
        buildES(problemSize, nPop, nEvals));

    for (String params : coopCoEvoParams) {
      solvers.put("coop-coevo<" + params, buildCooperativeSolver(params));
    }

    L.info(String.format("Going to test with %d evolvers: %s%n", solvers.size(), solvers.keySet()));
    int nOfRuns = seeds.length * solvers.size();
    int counter = 0;
    //run
    for (int seed : seeds) {
      for (TotalOrderQualityBasedProblem<List<Double>, Double> problem : toyProblems) {
        for (Map.Entry<String, IterativeSolver<? extends POSetPopulationState<?, List<Double>, Double>,
            TotalOrderQualityBasedProblem<List<Double>, Double>, List<Double>>> solverEntry : solvers.entrySet()) {
          Map<String, Object> keys = Map.ofEntries(
              Map.entry("seed", seed),
              Map.entry("problem", problem.getClass().getSimpleName().toLowerCase()),
              Map.entry("evolver", solverEntry.getKey())
          );
          try {
            counter = counter + 1;
            Stopwatch stopwatch = Stopwatch.createStarted();
            progressMonitor.notify(
                ((float) counter - 1) / nOfRuns,
                String.format("(%d/%d); Starting %s", counter, nOfRuns, keys)
            );
            IterativeSolver<? extends POSetPopulationState<?, List<Double>, Double>,
                TotalOrderQualityBasedProblem<List<Double>, Double>, List<Double>> solver = solverEntry.getValue();
            Collection<List<Double>> solutions = solver.solve(
                problem,
                new Random(seed),
                executorService,
                listenerFactory.build(keys).deferred(executorService)
            );
            progressMonitor.notify((float) counter / nOfRuns, String.format(
                "(%d/%d); Done %s: %d solutions in %4ds",
                counter,
                nOfRuns,
                keys,
                solutions.size(),
                stopwatch.elapsed(TimeUnit.SECONDS)
            ));

          } catch (SolverException e) {
            L.severe(String.format("Cannot complete %s due to %s", keys, e));
            e.printStackTrace();
          }
        }
      }
      listenerFactory.shutdown();
    }
  }

  private IterativeSolver<? extends POSetPopulationState<?, List<Double>, Double>,
      TotalOrderQualityBasedProblem<List<Double>, Double>, List<Double>> buildCooperativeSolver(String stringParams) {
    Map<String, String> params = Arrays.stream(stringParams.split(TOKEN_SEPARATOR))
        .map(s -> s.split(PARAM_VALUE_SEPARATOR))
        .collect(Collectors.toMap(
            ss -> ss.length == 2 ? ss[0] : NAME_KEY,
            ss -> ss.length == 2 ? ss[1] : ss[0]
        ));
    return buildCooperativeSolver(params);
  }

  private IterativeSolver<? extends POSetPopulationState<?, List<Double>, Double>,
      TotalOrderQualityBasedProblem<List<Double>, Double>, List<Double>> buildCooperativeSolver(
      Map<String, String> params
  ) {
    int problemSize = Integer.parseInt(params.get("size"));
    int nPop = Integer.parseInt(params.get("nPop"));
    int nTournament = Integer.parseInt(params.getOrDefault("nTour", "0"));
    String ea1 = params.get("ea1");
    String ea2 = params.get("ea2");
    String collaboratorSelector1 = params.get("sel1");
    String collaboratorSelector2 = params.get("sel2");
    String qualityAggregator = params.get("aggr");
    int nEvals = Integer.parseInt(params.get("nEvals"));

    BiFunction<List<Double>, List<Double>, List<Double>> solutionAggregator = (l1, l2) ->
        Stream.concat(l1.stream(), l2.stream()).toList();

    AbstractPopulationBasedIterativeSolver<? extends POSetPopulationState<List<Double>, List<Double>, Double>,
        TotalOrderQualityBasedProblem<List<Double>, Double>, List<Double>, List<Double>, Double> solver1 =
        ea1.equalsIgnoreCase("es") ? buildES(problemSize / 2, nPop, nEvals) :
            buildGA(problemSize / 2, nPop, nEvals, nTournament);
    AbstractPopulationBasedIterativeSolver<? extends POSetPopulationState<List<Double>, List<Double>, Double>,
        TotalOrderQualityBasedProblem<List<Double>, Double>, List<Double>, List<Double>, Double> solver2 =
        ea2.equalsIgnoreCase("es") ? buildES(problemSize - problemSize / 2, nPop, nEvals) :
            buildGA(problemSize - problemSize / 2, nPop, nEvals, nTournament);

    return new CooperativeSolver<>(
        solver1,
        solver2,
        solutionAggregator,
        CollaboratorSelector.build(collaboratorSelector1),
        CollaboratorSelector.build(collaboratorSelector2),
        QualityAggregator.build(qualityAggregator),
        StopConditions.nOfFitnessEvaluations(nEvals)
    );

  }

  private AbstractPopulationBasedIterativeSolver<? extends POSetPopulationState<List<Double>, List<Double>, Double>,
      TotalOrderQualityBasedProblem<List<Double>, Double>, List<Double>, List<Double>, Double> buildGA(
      int problemSize, int nPop, int nEvals, int nTournament) {
    return new StandardEvolver<>(
        Function.identity(),
        new FixedLengthListFactory<>(problemSize, new UniformDoubleFactory(0, 1)),
        nPop,
        StopConditions.nOfFitnessEvaluations(nEvals),
        Map.of(new GeometricCrossover(Range.open(0d, 1d)).andThen(new GaussianMutation(0.1)), 1d),
        new Tournament(nTournament),
        new Last(),
        nPop,
        true,
        false,
        (problem, random) -> new POSetPopulationState<>()
    );
  }

  private AbstractPopulationBasedIterativeSolver<? extends POSetPopulationState<List<Double>, List<Double>, Double>,
      TotalOrderQualityBasedProblem<List<Double>, Double>, List<Double>, List<Double>, Double> buildES(
      int problemSize, int nPop, int nEvals) {
    return new SimpleEvolutionaryStrategy<>(
        Function.identity(),
        new FixedLengthListFactory<>(problemSize, new UniformDoubleFactory(0, 1)),
        nPop,
        StopConditions.nOfFitnessEvaluations(nEvals),
        nPop / 4,
        1,
        0.1,
        false
    );
  }

  private List<String> buildCoEvoParams(String fixedElements, List<String> eas, List<String> selectors, List<String> aggregators) {
    List<String> parameters = new ArrayList<>();
    for (int i = 0; i < eas.size(); i++) {
      for (int j = i; j < eas.size(); j++) {
        for (String selector : selectors) {
          for (String aggregator : aggregators) {
            parameters.add(String.format("%s;ea1=%s;ea2=%s;sel1=%s;sel2=%s;aggr=%s",
                fixedElements, eas.get(i), eas.get(j), selector, selector, aggregator)
            );
          }
        }
      }
    }
    return parameters;
  }

}
