package it.units.malelab.jgea.lab;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Range;
import it.units.malelab.jgea.Worker;
import it.units.malelab.jgea.core.IndependentFactory;
import it.units.malelab.jgea.core.QualityBasedProblem;
import it.units.malelab.jgea.core.listener.CSVPrinter;
import it.units.malelab.jgea.core.listener.ListenerFactory;
import it.units.malelab.jgea.core.listener.NamedFunction;
import it.units.malelab.jgea.core.listener.TabularPrinter;
import it.units.malelab.jgea.core.selector.Last;
import it.units.malelab.jgea.core.selector.Tournament;
import it.units.malelab.jgea.core.solver.*;
import it.units.malelab.jgea.core.solver.coevolution.CollaboratorSelector;
import it.units.malelab.jgea.core.solver.coevolution.CooperativeSolver;
import it.units.malelab.jgea.core.solver.coevolution.SingleCollaboratorSelectors;
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

import static it.units.malelab.jgea.core.listener.NamedFunctions.*;
import static it.units.malelab.jgea.core.util.Args.i;
import static it.units.malelab.jgea.core.util.Args.ri;

public class RobustnessComparison extends Worker {

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

  @Override
  public void run() {
    int nPop = i(a("nPop", "100"));
    int height = i(a("height", "10"));
    int nIterations = i(a("nIterations", "100"));
    int nTournament = 5;
    int[] seeds = ri(a("seed", "0:1"));
    boolean output = a("output", "true").startsWith("t");
    String bestFile = a("bestFile", null);
    String validationFile = a("validationFile", null);
    SymbolicRegressionFitness.Metric metric = SymbolicRegressionFitness.Metric.MSE;
    Element.Operator[] operators = Arrays.stream(Element.Operator.values())
        .filter(o -> o.arity() == 2).toArray(Element.Operator[]::new);
    double[] constants = new double[]{0.1, 1d, 10d};
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

    List<ListenerFactory<POSetPopulationState<?, ?, ? extends Double>, Map<String, Object>>> listenerFactories =
        new ArrayList<>();
    if (bestFile == null || output) {
      listenerFactories.add(new TabularPrinter<>(functions, kFunctions));
    }
    if (bestFile != null) {
      listenerFactories.add(
          new CSVPrinter<>(functions, kFunctions, new File(a("bestFile", null))));
    }
    if (validationFile != null) {
      // TODO implement validation to test robustness
    }
    ListenerFactory<POSetPopulationState<?, ?, ? extends Double>, Map<String, Object>> listenerFactory =
        ListenerFactory.all(listenerFactories);

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
          IndependentFactory.picker(Arrays.stream(constants)
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
              IndependentFactory.picker(operators),
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
                      IndependentFactory.picker(operators),
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
    solvers.put("coop-coevo", p -> {
      int problemArity = p.qualityFunction().arity();
      IndependentFactory<Element> terminalFactory = IndependentFactory.oneOf(
          IndependentFactory.picker(Arrays.stream(
                  vars(problemArity))
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
                  IndependentFactory.picker(operators),
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
                          IndependentFactory.picker(operators),
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
      int length = (int) Math.pow(2d, height + 1) - 1;
      AbstractPopulationBasedIterativeSolver<POSetPopulationState<List<Double>, List<Double>, Double>, QualityBasedProblem<List<Double>, Double>, List<Double>, List<Double>, Double> solver2 =
          new StandardEvolver<>(
              Function.identity(),
              new FixedLengthListFactory<>(length, new UniformDoubleFactory(-1d, 1d)),
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

      CollaboratorSelector<Individual<Tree<Element>, Tree<Element>, Double>> extractor1 = SingleCollaboratorSelectors.best();
      CollaboratorSelector<Individual<List<Double>, List<Double>, Double>> extractor2 = SingleCollaboratorSelectors.best();
      Function<Collection<Double>, Double> qualityAggregator = c -> c.stream().findFirst().orElse(0d);
      return new CooperativeSolver<>(
          solver1,
          solver2,
          solutionAggregator,
          extractor1,
          extractor2,
          qualityAggregator,
          StopConditions.nOfIterations(nIterations)
      );
    });

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
          } catch (SolverException e) {
            L.severe(String.format("Cannot complete %s due to %s", keys, e));
            e.printStackTrace();
          }
        }
      }
    }
    listenerFactory.shutdown();

  }

}
