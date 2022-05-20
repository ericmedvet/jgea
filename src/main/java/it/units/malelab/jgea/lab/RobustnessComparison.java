package it.units.malelab.jgea.lab;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Range;
import it.units.malelab.jgea.Worker;
import it.units.malelab.jgea.core.IndependentFactory;
import it.units.malelab.jgea.core.QualityBasedProblem;
import it.units.malelab.jgea.core.listener.*;
import it.units.malelab.jgea.core.selector.Last;
import it.units.malelab.jgea.core.selector.Tournament;
import it.units.malelab.jgea.core.solver.*;
import it.units.malelab.jgea.core.solver.coevolution.CollaboratorSelector;
import it.units.malelab.jgea.core.solver.coevolution.CooperativeSolver;
import it.units.malelab.jgea.core.solver.state.POSetPopulationState;
import it.units.malelab.jgea.problem.symbolicregression.*;
import it.units.malelab.jgea.representation.sequence.FixedLengthListFactory;
import it.units.malelab.jgea.representation.sequence.numeric.GaussianMutation;
import it.units.malelab.jgea.representation.sequence.numeric.GeometricCrossover;
import it.units.malelab.jgea.representation.sequence.numeric.UniformDoubleFactory;
import it.units.malelab.jgea.representation.tree.*;

import java.io.File;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

import static it.units.malelab.jgea.core.listener.NamedFunctions.*;
import static it.units.malelab.jgea.core.util.Args.*;

public class RobustnessComparison extends Worker {

  private record ValidationEvent(RealFunction realFunction, double fitness, double validationFitness) {
  }

  public static void main(String[] args) {
    new RobustnessComparison(args);
  }

  public RobustnessComparison(String[] args) {
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

  private static final Element.Operator[] OPERATORS = Arrays.stream(Element.Operator.values())
      .filter(o -> o.arity() == 2).toArray(Element.Operator[]::new);
  private static final double[] CONSTANTS = new double[]{0.1, 1d, 10d};

  private static final String TOKEN_SEPARATOR = ";";
  private static final String PARAM_VALUE_SEPARATOR = "=";
  private static final String NAME_KEY = "NAME";

  @Override
  public void run() {
    int nPop = i(a("nPop", "100"));
    int height = i(a("height", "10"));
    int nIterations = i(a("nIterations", "100"));
    int nTournament = 5;
    List<String> coopCoevoParams = l(a("params", "nPop=100;h=10;nIt=100;nTour=5;sel1=b;sel2=b;aggr=min"));

    int[] seeds = ri(a("seed", "0:1"));
    boolean output = a("output", "true").startsWith("t");
    String bestFile = a("bestFile", "best.txt");
    String validationFile = a("validationFile", "validation.txt");
    SymbolicRegressionFitness.Metric metric = SymbolicRegressionFitness.Metric.MSE;

    List<SymbolicRegressionProblem> problems = List.of(
        new Keijzer6(metric)/*,
        new Pagie1(metric),
        new Nguyen7(metric, 1)*/
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
        attribute("problem").reformat(NamedFunction.formatOfLongest(problems.stream()
            .map(p -> p.getClass().getSimpleName())
            .toList())),
        attribute("evolver").reformat("%20.20s")
    );
    List<NamedFunction<? super Map<String, Object>, ?>> validationKFunctions = List.of(
        attribute("seed").reformat("%2d"),
        attribute("problem").reformat(NamedFunction.formatOfLongest(problems.stream()
            .map(p -> p.getClass().getSimpleName())
            .toList())),
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
      listenerFactories.add(
          new CSVPrinter<>(functions, kFunctions, new File(bestFile)));
    }
    ListenerFactory<POSetPopulationState<?, ?, ? extends Double>, Map<String, Object>> listenerFactory =
        ListenerFactory.all(listenerFactories);
    ListenerFactory<ValidationEvent, Map<String, Object>>
        validationListenerFactory = validationFile == null ? ListenerFactory.deaf() :
        new CSVPrinter<>(validationFunctions, validationKFunctions, new File(validationFile));

    //evolvers
    Map<String, Function<SymbolicRegressionProblem, IterativeSolver<? extends POSetPopulationState<?, RealFunction,
        Double>, SymbolicRegressionProblem, RealFunction>>> solvers = new TreeMap<>();
    solvers.put("tree-ga", p -> {
      IndependentFactory<Element> termFact = IndependentFactory.oneOf(
          IndependentFactory.picker(Arrays.stream(
                  vars(p.qualityFunction().arity()))
              .sequential()
              .map(Element.Variable::new)
              .toArray(Element.Variable[]::new)),
          IndependentFactory.picker(Arrays.stream(CONSTANTS)
              .mapToObj(Element.Constant::new)
              .toArray(Element.Constant[]::new))
      );
      return new StandardEvolver<>(
          ((Function<Tree<Element>, RealFunction>) t -> new TreeBasedRealFunction(
              t,
              vars(p.qualityFunction().arity())
          )).andThen(MathUtils.linearScaler(p.qualityFunction())),
          new RampedHalfAndHalf<>(
              height,
              height,
              Element.Operator.arityFunction(),
              IndependentFactory.picker(OPERATORS),
              termFact
          ),
          nPop,
          StopConditions.nOfIterations(nIterations),
          Map.of(
              new SubtreeCrossover<>(height),
              0.8d,
              new SubtreeMutation<>(
                  height,
                  new GrowTreeBuilder<>(
                      Element.Operator.arityFunction(),
                      IndependentFactory.picker(OPERATORS),
                      termFact
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
    });
    for (String params : coopCoevoParams) {
      solvers.put("coop-coevo<" + params, buildCooperativeSolver(params));
    }

    Map<String, Function<SymbolicRegressionProblem, Function<RealFunction, Double>>> validators = new TreeMap<>();
    validators.put("default", SymbolicRegressionProblem::validationQualityFunction);

    L.info(String.format("Going to test with %d evolvers: %s%n", solvers.size(), solvers.keySet()));
    //run
    for (int seed : seeds) {
      for (SymbolicRegressionProblem problem : problems) {
        for (Map.Entry<String, Function<SymbolicRegressionProblem, IterativeSolver<? extends POSetPopulationState<?,
            RealFunction, Double>, SymbolicRegressionProblem, RealFunction>>> solverEntry : solvers.entrySet()) {
          Map<String, Object> keys = Map.ofEntries(
              Map.entry("seed", seed),
              Map.entry("problem", problem.getClass().getSimpleName().toLowerCase()),
              Map.entry("evolver", solverEntry.getKey())
          );
          try {
            Stopwatch stopwatch = Stopwatch.createStarted();
            IterativeSolver<? extends POSetPopulationState<?, RealFunction, Double>, SymbolicRegressionProblem,
                RealFunction> solver = solverEntry.getValue()
                .apply(problem);
            L.info(String.format("Starting %s", keys));
            Collection<RealFunction> solutions = solver.solve(
                problem,
                new Random(seed),
                executorService,
                listenerFactory.build(keys).deferred(executorService)
            );
            L.info(String.format(
                "Done %s: %d solutions in %4.1fs",
                keys,
                solutions.size(),
                (double) stopwatch.elapsed(TimeUnit.MILLISECONDS) / 1000d
            ));
            for (Map.Entry<String, Function<SymbolicRegressionProblem, Function<RealFunction, Double>>> validator
                : validators.entrySet()) {
              Map<String, Object> validationKeys = Map.ofEntries(
                  Map.entry("seed", seed),
                  Map.entry("problem", problem.getClass().getSimpleName().toLowerCase()),
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
    }
    listenerFactory.shutdown();

  }

  private Function<SymbolicRegressionProblem, IterativeSolver<? extends POSetPopulationState<?, RealFunction,
      Double>, SymbolicRegressionProblem, RealFunction>> buildCooperativeSolver(String stringParams) {
    Map<String, String> params = Arrays.stream(stringParams.split(TOKEN_SEPARATOR))
        .map(s -> s.split(PARAM_VALUE_SEPARATOR))
        .collect(Collectors.toMap(
            ss -> ss.length == 2 ? ss[0] : NAME_KEY,
            ss -> ss.length == 2 ? ss[1] : ss[0]
        ));
    return buildCooperativeSolver(params);
  }

  private Function<SymbolicRegressionProblem, IterativeSolver<? extends POSetPopulationState<?, RealFunction,
      Double>, SymbolicRegressionProblem, RealFunction>> buildCooperativeSolver(Map<String, String> params) {
    int nPop = Integer.parseInt(params.get("nPop"));
    int height = Integer.parseInt(params.get("h"));
    int nIterations = Integer.parseInt(params.get("nIt"));
    int nTournament = Integer.parseInt(params.get("nTour"));
    String collaboratorSelector1 = params.get("sel1");
    String collaboratorSelector2 = params.get("sel2");
    String qualityAggregator = params.get("aggr");

    return p -> {
      IndependentFactory<Element> terminalFactory = IndependentFactory.oneOf(
          IndependentFactory.picker(Arrays.stream(
                  vars(p.qualityFunction().arity()))
              .sequential()
              .map(Element.Variable::new)
              .toArray(Element.Variable[]::new)),
          r -> new Element.Placeholder()
      );
      double xOverProb = 0.8d;
      AbstractPopulationBasedIterativeSolver<POSetPopulationState<Tree<Element>, Tree<Element>, Double>, QualityBasedProblem<Tree<Element>, Double>, Tree<Element>, Tree<Element>, Double> solver1 =
          new StandardEvolver<>(
              Function.identity(),
              new RampedHalfAndHalf<>(
                  height,
                  height,
                  Element.Operator.arityFunction(),
                  IndependentFactory.picker(OPERATORS),
                  terminalFactory
              ),
              nPop,
              StopConditions.nOfIterations(nIterations),
              Map.of(
                  new SubtreeCrossover<>(height),
                  xOverProb,
                  new SubtreeMutation<>(
                      height,
                      new GrowTreeBuilder<>(
                          Element.Operator.arityFunction(),
                          IndependentFactory.picker(OPERATORS),
                          terminalFactory
                      )
                  ),
                  1d - xOverProb
              ),
              new Tournament(nTournament),
              new Last(),
              nPop,
              true,
              false,
              (srp, r) -> new POSetPopulationState<>()
          );
      AbstractPopulationBasedIterativeSolver<POSetPopulationState<List<Double>, List<Double>, Double>, QualityBasedProblem<List<Double>, Double>, List<Double>, List<Double>, Double> solver2 =
          new StandardEvolver<>(
              Function.identity(),
              new FixedLengthListFactory<>((int) Math.pow(2d, height + 1) - 1, new UniformDoubleFactory(-1d, 1d)),
              nPop,
              StopConditions.nOfIterations(nIterations),
              Map.of(
                  new GaussianMutation(.35d), 1d - xOverProb,
                  new GeometricCrossover(Range.closed(-.5d, 1.5d)).andThen(new GaussianMutation(.1d)), xOverProb
              ),
              new Tournament(nTournament),
              new Last(),
              nPop,
              true,
              false,
              (srp, r) -> new POSetPopulationState<>()
          );
      BiFunction<Tree<Element>, List<Double>, RealFunction> solutionAggregator = ((BiFunction<Tree<Element>, List<Double>, RealFunction>) (t, l) -> new TreeBasedRealFunction(
          Tree.mapFromIndex(
              t,
              (el, i) -> el.equals(new Element.Placeholder()) ? new Element.Constant(l.get(i)) : el
          ),
          vars(p.qualityFunction().arity())
      )).andThen(MathUtils.linearScaler(p.qualityFunction()));

      return new CooperativeSolver<>(
          solver1,
          solver2,
          solutionAggregator,
          CollaboratorSelector.build(collaboratorSelector1),
          CollaboratorSelector.build(collaboratorSelector2),
          qualityAggregator(qualityAggregator),
          StopConditions.nOfIterations(nIterations)
      );

    };
  }

  private static Function<Collection<Double>, Double> qualityAggregator(String aggregator) {
    return switch (aggregator) {
      case "min" -> c -> c.stream().mapToDouble(d -> d).min().orElse(0d);
      case "max" -> c -> c.stream().mapToDouble(d -> d).max().orElse(0d);
      case "avg" -> c -> c.stream().mapToDouble(d -> d).average().orElse(0d);
      case "median" -> c -> {
        DoubleStream doubleStream = c.stream().mapToDouble(d -> d).sorted();
        return c.size() % 2 == 0 ?
            doubleStream.skip(c.size() / 2 - 1).limit(2).average().orElse(0d) :
            doubleStream.skip(c.size() / 2).findFirst().orElse(0d);
      };
      default -> throw new IllegalArgumentException("Illegal aggregator specified");
    };
  }

}
