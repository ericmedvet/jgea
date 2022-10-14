package it.units.malelab.jgea.lab.robustness;

import com.google.common.base.Stopwatch;
import it.units.malelab.jgea.Worker;
import it.units.malelab.jgea.core.IndependentFactory;
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
import it.units.malelab.jgea.problem.symbolicregression.*;
import it.units.malelab.jgea.representation.tree.*;

import java.io.File;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Function;

import static it.units.malelab.jgea.core.listener.NamedFunctions.*;
import static it.units.malelab.jgea.core.util.Args.ri;

public class FunctionCompositionProblem extends Worker {

  private record ValidationEvent(RealFunction realFunction, double fitness, double validationFitness) {
  }

  public static void main(String[] args) {
    new FunctionCompositionProblem(args);
  }

  public FunctionCompositionProblem(String[] args) {
    super(args);
  }

  private static String[] vars(int n) {
    if (n == 1) {
      return new String[]{"x"};
    }
    String[] vars = new String[n];
    for (int i = 0; i < n; i++) {
      vars[i] = "x" + i;
    }
    return vars;
  }

  private static final Element.Operator[] OPERATORS = Element.Operator.values();
  private static final double[] CONSTANTS = new double[]{1, 2, 3};
  private static final IndependentFactory<Element> TERM_FACT = IndependentFactory.oneOf(
      IndependentFactory.picker(Arrays.stream(vars(1))
          .sequential()
          .map(Element.Variable::new)
          .toArray(Element.Variable[]::new)),
      IndependentFactory.picker(Arrays.stream(CONSTANTS)
          .mapToObj(Element.Constant::new)
          .toArray(Element.Constant[]::new))
  );

  private static final BiFunction<RealFunction, RealFunction, RealFunction> FUNCTION_COMPOSER = (f, g) -> {
    Function<Double, Double> f1 = f::apply;
    Function<Double, Double> g1 = g::apply;
    return d -> g1.compose(f1).apply(d[0]);
  };

  @Override
  public void run() {
    String telegramBotId = a("telegramBotId", null);
    long telegramChatId = Long.parseLong(a("telegramChatId", "0"));

    int[] seeds = ri(a("seed", "0:10"));
    int treeHeight = 3;
    int nPop = 100;
    int nTournament = 2;
    int nIterations = 1000;
    int nEvals = nPop * nIterations;

    double min = 0.1;
    double max = 5;
    int nTrain = 1024;
    int nTest = nTrain * 2;
    Function<Double, Double> f = x -> (3 * x * x * x + 2) / (x + 1);
    Function<Double, Double> g = x -> Math.log(x) + 1;
    RealFunction targetFunction = d -> f.compose(g).apply(d[0]);
    String problemName = "(3x^3+2)/(x+1) ° log(x)+1";

    boolean output = a("output", "true").startsWith("t");
    String bestFile = a("bestFile", "best.txt");
    String lastFile = a("lastFile", "last.txt");
    String validationFile = a("validationFile", "validation.txt");
    SymbolicRegressionFitness.Metric metric = SymbolicRegressionFitness.Metric.MSE;

    List<double[]> trainingPoints = Arrays.stream(MathUtils.equispacedValues(min, max, (max - min) / nTrain))
        .mapToObj(d -> new double[]{d}).toList();
    List<double[]> testPoints = Arrays.stream(MathUtils.equispacedValues(min, max, (max - min) / nTest))
        .mapToObj(d -> new double[]{d}).toList();
    SyntheticSymbolicRegressionProblem problem = new SyntheticSymbolicRegressionProblem(
        targetFunction, trainingPoints, testPoints, metric
    );

    //consumers
    List<NamedFunction<? super POSetPopulationState<?, ?, ? extends Double>, ?>> functions = List.of(
        iterations(),
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
    List<NamedFunction<? super Map<String, Object>, ?>> validationKFunctions = List.of(
        attribute("seed").reformat("%2d"),
        attribute("problem"),
        attribute("evolver").reformat("%20.20s"),
        attribute("validator").reformat("%20.20s")
    );
    List<NamedFunction<? super ValidationEvent, ?>> validationFunctions = List.of(
        NamedFunction.build("event.fitness", "%5.3f", ValidationEvent::fitness),
        NamedFunction.build("validation.fitness", "%5.3f", ValidationEvent::validationFitness),
        NamedFunction.build("solution", "%30.30s", ValidationEvent::realFunction)
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
    ListenerFactory<ValidationEvent, Map<String, Object>>
        validationListenerFactory = validationFile == null ? ListenerFactory.deaf() :
        new CSVPrinter<>(validationFunctions, validationKFunctions, new File(validationFile));

    ProgressMonitor progressMonitor = new ScreenProgressMonitor(System.out);
    if (telegramBotId != null && telegramChatId != 0) {
      progressMonitor = progressMonitor.and(new TelegramProgressMonitor(telegramBotId, telegramChatId));
    }

    // evolvers
    Map<String, IterativeSolver<? extends POSetPopulationState<?, RealFunction, Double>,
        SyntheticSymbolicRegressionProblem, RealFunction>> solvers = new TreeMap<>();
    solvers.put("tree-ga", new StandardEvolver<>(
        t -> new TreeBasedRealFunction(t, vars(1)),
        new RampedHalfAndHalf<>(
            treeHeight,
            treeHeight,
            Element.Operator.arityFunction(),
            IndependentFactory.picker(OPERATORS),
            TERM_FACT
        ),
        nPop,
        StopConditions.nOfFitnessEvaluations(nEvals),
        Map.of(
            new SubtreeCrossover<>(treeHeight),
            0.8d,
            new SubtreeMutation<>(
                treeHeight,
                new GrowTreeBuilder<>(
                    Element.Operator.arityFunction(),
                    IndependentFactory.picker(OPERATORS),
                    TERM_FACT
                )
            ),
            0.2d
        ),
        new Tournament(nTournament),
        new Last(),
        nPop,
        true,
        false,
        (srp, r) -> new POSetPopulationState<>()
    ));
    for (String selector : List.of("f0.1", "f0.25", "f0.5", "f0.75", "l0.1", "l0.25", "l0.5", "l0.75", "c")) {
      for (String aggregator : List.of("f", "m", "l")) {
        solvers.put(
            String.format("coevo-%s-%s", selector, aggregator),
            cooperativeSolverBuilder(nPop, treeHeight, nTournament, nEvals, selector, aggregator)
        );
      }
    }

    Map<String, Function<SymbolicRegressionProblem<?>, Function<RealFunction, Double>>> validators = new TreeMap<>();
    validators.put("default", SymbolicRegressionProblem::validationQualityFunction);

    L.info(String.format("Going to test with %d evolvers: %s%n", solvers.size(), solvers.keySet()));
    int nOfRuns = seeds.length * solvers.size();
    int counter = 0;
    //run
    for (int seed : seeds) {
      for (Map.Entry<String, IterativeSolver<? extends POSetPopulationState<?, RealFunction, Double>,
          SyntheticSymbolicRegressionProblem, RealFunction>> solverEntry : solvers.entrySet()) {
        Map<String, Object> keys = Map.ofEntries(
            Map.entry("seed", seed),
            Map.entry("problem", problemName),
            Map.entry("evolver", solverEntry.getKey())
        );
        try {
          counter = counter + 1;
          Stopwatch stopwatch = Stopwatch.createStarted();
          progressMonitor.notify(
              ((float) counter - 1) / nOfRuns,
              String.format("(%d/%d); Starting %s", counter, nOfRuns, keys)
          );
          IterativeSolver<? extends POSetPopulationState<?, RealFunction, Double>,
              SyntheticSymbolicRegressionProblem, RealFunction> solver = solverEntry.getValue();
          Collection<RealFunction> solutions = solver.solve(
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

          for (Map.Entry<String, Function<SymbolicRegressionProblem<?>, Function<RealFunction, Double>>> validator
              : validators.entrySet()) {
            Map<String, Object> validationKeys = Map.ofEntries(
                Map.entry("seed", seed),
                Map.entry("problem", problemName),
                Map.entry("evolver", solverEntry.getKey()),
                Map.entry("validator", validator.getKey())
            );

            Listener<ValidationEvent> validationListener =
                validationListenerFactory.build(validationKeys);
            Function<RealFunction, Double> fitnessFunction = problem.qualityFunction();
            Function<RealFunction, Double> validationFunction = validator.getValue().apply(problem);
            solutions.stream().map(
                s -> new ValidationEvent(
                    s, fitnessFunction.apply(s), validationFunction.apply(s))
            ).forEach(validationListener::listen);
            L.info(String.format(
                "Validation done %s",
                validationKeys
            ));

          }

        } catch (SolverException e) {
          L.severe(String.format("Cannot complete %s due to %s", keys, e));
          e.printStackTrace();
        }
      }

    }
    listenerFactory.shutdown();

  }

  private static IterativeSolver<? extends POSetPopulationState<?, RealFunction, Double>,
      SyntheticSymbolicRegressionProblem, RealFunction> cooperativeSolverBuilder(int nPop, int height, int nTournament,
                                                                                 int nEvals,
                                                                                 String collaboratorSelector,
                                                                                 String qualityAggregator) {
    AbstractPopulationBasedIterativeSolver<POSetPopulationState<Tree<Element>, RealFunction, Double>,
        TotalOrderQualityBasedProblem<RealFunction, Double>, Tree<Element>, RealFunction, Double> innerSolver =
        new StandardEvolver<>(
            t -> new TreeBasedRealFunction(t, vars(1)),
            new RampedHalfAndHalf<>(
                height,
                height,
                Element.Operator.arityFunction(),
                IndependentFactory.picker(OPERATORS),
                TERM_FACT
            ),
            nPop,
            StopConditions.nOfFitnessEvaluations(nEvals),
            Map.of(
                new SubtreeCrossover<>(height),
                0.8d,
                new SubtreeMutation<>(
                    height,
                    new GrowTreeBuilder<>(
                        Element.Operator.arityFunction(),
                        IndependentFactory.picker(OPERATORS),
                        TERM_FACT
                    )
                ),
                0.2d
            ),
            new Tournament(nTournament),
            new Last(),
            nPop,
            true,
            false,
            (srp, r) -> new POSetPopulationState<>()
        );

    return new CooperativeSolver<>(
        innerSolver,
        innerSolver,
        FUNCTION_COMPOSER,
        CollaboratorSelector.build(collaboratorSelector),
        CollaboratorSelector.build(collaboratorSelector),
        QualityAggregator.build(qualityAggregator),
        StopConditions.nOfFitnessEvaluations(nEvals)
    );
  }

}